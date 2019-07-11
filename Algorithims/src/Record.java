import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.mfcc.MFCC;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
/**
 * This class is responsible for acquiring the attributes of a unknown sample using the microphone.
 *
 * @author Githendu Mukiri
 * @version 1.1 2019-07-11
 *
 */

public class Record {


    private File audioFile;

    public Record() {

        audioFile = new File("recording.wav");
    }

    public Sample getSample(){

        try {

            audioFile.createNewFile();
        }
        catch (IOException e){
            e.getStackTrace();
        }

        int audioBufferSize = 1024;
        int bufferOverlap = 128;
        int sampleRate = 44100;
        int sampleFrame = 1024;

        //Get test data

        System.out.println("Capturing sound");
        try{

            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 2,4,sampleRate,false); //create new wav file with sampleRate 16bit, stereo 4 bytes
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);    //retrieve audio from computer
            TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info); //gets mic
            targetLine.open();

            System.out.println("Starting recording");
            targetLine.start();

            Thread thread = new Thread(() -> {    //anonymous gets mic input and writes to file

                AudioInputStream audioInputStream = new AudioInputStream(targetLine);
                try {
                    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile);   //writes bytes as long as target data line is retrieving data from microphone

                }
                catch (IOException e){

                    e.printStackTrace();
                }
                System.out.println("Ended recording");
            });
            thread.start();
            Thread.sleep(5000);
            targetLine.stop();
            targetLine.close();

            System.out.println("Test Data Captured");
        }
        catch (InterruptedException | LineUnavailableException e){
            e.printStackTrace();
        }


        //calculate mfcc for test data


        AudioDispatcher dispatcher = null;  //syntax to import wav
        try {
            dispatcher = AudioDispatcherFactory.fromFile(audioFile, audioBufferSize, bufferOverlap);
        } catch (UnsupportedAudioFileException | IOException e) {   //checks if file is wav, throws exception thrown if it is not
            e.printStackTrace();
        }

        MFCC testMfcc = new MFCC(sampleFrame, sampleRate,40 , 50,300, 30000);

        dispatcher.addAudioProcessor(testMfcc);
        dispatcher.addAudioProcessor(new AudioProcessor() {

            @Override
            public void processingFinished() {

                System.out.printf("Calculated mfcc for %s%n", audioFile.getName());
                float[] cleanMfcc = Arrays.copyOfRange(testMfcc.getMFCC(), 1,testMfcc.getMFCC().length);    //omit 0th column
                System.out.println(Arrays.toString(cleanMfcc));
            }

            @Override
            public boolean process(AudioEvent audioEvent) {
                return true;
            }
        });

        dispatcher.run();

        float[] query = Arrays.copyOfRange(testMfcc.getMFCC(),1,testMfcc.getMFCC().length); //omit 0 column

        return new Sample(query);
    }

}
