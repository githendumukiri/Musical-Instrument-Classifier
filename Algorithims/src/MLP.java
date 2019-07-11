import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.mfcc.MFCC;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import scala.concurrent.java8.FuturesConvertersImpl;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.*;
/**
 * This is an unfinished class that implements the Classifier Interface.
 * The goal is to create a neural network that will classify an unknown sample from
 * known samples more accurately than the KNN implementation.
 *
 *
 * @author Githendu Mukiri
 * @version 1.1 2019-07-11
 *
 */
public class MLP implements Classifier {

    private List<Instrument> instrumentList;

    private int seed;  //randomness
    private double learningRate;
    private int batchSize;    //size of how much data is taken in
    private int nEpochs;       //total passes through data
    private int numInputs;
    private int numOutputs;
    private int numHiddenNodes;

    private INDArray featureVectors;
    private INDArray labels;
    private DataSet trainDataSet;

    public MLP() {

        instrumentList = new ArrayList<>();
        seed = 123;
        learningRate = 0.01;
        batchSize = 100;
        nEpochs = 30;
        numInputs = 15;
        numOutputs = 5;
        numHiddenNodes = 30;
        trainDataSet = new DataSet();
    }

    /**
     *
     * @param labels string array
     * @return  int array
     */
    public int[] encode(List<String> labels){
       int[] encodedLabels = new int[labels.size()];

       for (int i = 0; i < labels.size();i++) {
           switch (labels.get(i)){
               case ("cello"): encodedLabels[i] = 1; break;
               case ("clarinet"): encodedLabels[i] = 2; break;
               case ("flute"): encodedLabels[i] = 3; break;
               case ("sax"): encodedLabels[i] = 4; break;
               case ("viola"): encodedLabels[i] = 5; break;

           }
       }
       return encodedLabels;
    }

    public void train(File trainData){

        String[] listOfFiles = trainData.list((dir, name) -> name.endsWith("wav"));
        int num_music = 0;
        for (String i : listOfFiles) {

            System.out.println(i);
            num_music++;
        }
        System.out.printf("Found %d audio files in %s%n", num_music, trainData);

        //Prepare labels from filenames


        List<String> labelNames = new ArrayList();
        String[] classes = {"cello", "clarinet", "flute", "sax", "viola"};

        // color dictionary for labels using hashmap
        Map<String, String> color_dict = new HashMap<>();
        color_dict.put("cello", "blue");
        color_dict.put("clarinet", "red");
        color_dict.put("flute", "green");
        color_dict.put("sax", "black");
        color_dict.put("viola", "magenta");

        List<String> color_list = new ArrayList();

        //loops through files, retrieves labels and color
        for (int i = 0; i < listOfFiles.length; i++) {
            for (int j = 0; j < classes.length; j++) {

                if (listOfFiles[i].contains(classes[j])) {

                    labelNames.add(classes[j]);
                    color_list.add(color_dict.get(classes[j]));
                }
            }

        }
        //get label data
        int[] labelNums = encode(labelNames);

        for(int i : labelNums){

            labels.add(i);
        }

        //calculate MFCC

        int audioBufferSize = 1024;
        int bufferOverlap = 128;
        int sampleRate = 44100;
        int sampleFrame = 1024;

        ArrayList<float[]> features = new ArrayList<>();

        int counter = 0;    //track items processed
        for (File child : trainData.listFiles()) {  //loops through dir
            File audioFile = new File(child.getAbsolutePath());
            AudioDispatcher dispatcher = null;  //syntax to import wav
            try {
                dispatcher = AudioDispatcherFactory.fromFile(audioFile, audioBufferSize, bufferOverlap);
            } catch (UnsupportedAudioFileException e) {   //checks if file is wav, throws exception thrown if it is not
                e.printStackTrace();
                System.out.println(child.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            MFCC mfcc = new MFCC(sampleFrame, sampleRate,40 , 50, 300, 30000);
            dispatcher.addAudioProcessor(mfcc);
            dispatcher.addAudioProcessor(new AudioProcessor() {

                @Override
                public void processingFinished() {
                    float[] cleanMfcc = Arrays.copyOfRange(mfcc.getMFCC(), 1, mfcc.getMFCC().length);;
                    for (String i : classes){

                        if (audioFile.getName().contains(i)){
                            System.out.println(i);
                            instrumentList.add(new Instrument((cleanMfcc), i));
                            features.add(cleanMfcc);
                        }
                    }
                    System.out.printf("Calculated mfcc for %s%n", audioFile.getName());
                    System.out.println(Arrays.toString(cleanMfcc));
                }

                @Override
                public boolean process(AudioEvent audioEvent) {
                    return true;
                }
            });
            counter++;
            dispatcher.run();
        }
        System.out.printf("%nProcessed %d total items%n", counter);

        //build network
        labels = Nd4j.create(encode(labelNames));

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(learningRate, 0.9))
                .list()
                .layer(new DenseLayer.Builder()
                        .nIn(numInputs)
                        .nOut(numHiddenNodes)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunction.XENT)
                        .nIn(numHiddenNodes)
                        .nOut(numOutputs)
                        .activation(Activation.SIGMOID)
                        .build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));  //Print score every 10 parameter updates


    }

    /**
     *
     * @param s a sample object
     * @return  string identifying the object name
     */
    public String classify(Sample s){




        return s.sampleName;
    }

}
