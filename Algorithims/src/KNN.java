//TarsosDSP packages used for extracting MFCC
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.mfcc.MFCC;
//Java Sound & io packages used for file handling
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class implements the Classifier Interface and is used for identifying the
 * K-Nearest Neighbors of a given an unknown sample from known samples.
 *
 * @author Githendu Mukiri
 * @version 1.1 2019-07-11
 */

public class KNN implements Classifier {

    private List<Instrument> instrumentList;
    private List<Result> resultList;

    public KNN() {
        instrumentList = new ArrayList<>();
        resultList = new ArrayList<>();
    }

    /**
     * @param trainData file that contains wav files
     */
    public void train(File trainData) {

        String[] listOfFiles = trainData.list((dir, name) -> name.endsWith("wav"));
        int num_music = 0;
        for (String i : listOfFiles) {

            System.out.println(i);
            num_music++;
        }
        System.out.printf("Found %d audio files in %s%n", num_music, trainData);

        //Prepare labels from filenames


        List<String> labels = new ArrayList();
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
        for (String listOfFile : listOfFiles) {
            for (String aClass : classes) {

                if (listOfFile.contains(aClass)) {

                    labels.add(aClass);
                    color_list.add(color_dict.get(aClass));
                }
            }

        }

        //calculate MFCC

        int audioBufferSize = 1024;
        int bufferOverlap = 128;
        int sampleRate = 44100;
        int sampleFrame = 1024;


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

            MFCC mfcc = new MFCC(sampleFrame, sampleRate, 40, 50, 30, 30000);
            dispatcher.addAudioProcessor(mfcc);
            dispatcher.addAudioProcessor(new AudioProcessor() {

                @Override
                public void processingFinished() {
                    float[] cleanMfcc = Arrays.copyOfRange(mfcc.getMFCC(), 1, mfcc.getMFCC().length);
                    for (String i : classes) {

                        if (audioFile.getName().contains(i)) {
                            System.out.println(i);
                            instrumentList.add(new Instrument((cleanMfcc), i)); //omit 0th column
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

    }

    /**
     * Returns the majority value in an array of strings
     * majority value is the most frequent value (the mode)
     * handles multiple majority values (ties broken at random)
     *
     * @param array input array
     * @return String
     */
    private static String findMajorityClass(String[] array) {

        //add the String array to a HashSet to get unique String values
        Set<String> h = new HashSet<>(Arrays.asList(array));
        //System.out.println(h);
        //convert the HashSet back to array
        String[] uniqueValues = h.toArray(new String[0]);
        //counts for unique strings
        int[] counts = new int[uniqueValues.length];
        // loop thru unique strings and count how many times they appear in origianl array
        for (int i = 0; i < uniqueValues.length; i++) {
            for (String s : array) {
                if (s.equals(uniqueValues[i])) {
                    counts[i]++;
                }
            }
        }

        for (String uniqueValue : uniqueValues) System.out.println(uniqueValue);
        for (int value : counts) System.out.println(value);


        int max = counts[0];
        for (int counter = 1; counter < counts.length; counter++) {
            if (counts[counter] > max) {
                max = counts[counter];
            }
        }
        System.out.println("max # of occurences: " + max);

        //how many times max appears
        //we know that max will appear at least once in counts
        //so the value of freq will be 1 at minimum after this loop
        int freq = 0;
        for (int count : counts) {
            if (count == max) {
                freq++;
            }
        }

        //index of most freq value if we have only one mode
        int index = -1;
        if (freq == 1) {
            for (int counter = 0; counter < counts.length; counter++) {
                if (counts[counter] == max) {
                    index = counter;
                    break;
                }
            }
            //System.out.println("one majority class, index is: "+index);
            return uniqueValues[index];
        } else {//we have multiple modes
            int[] ix = new int[freq];//array of indices of modes
            System.out.println("multiple majority classes: " + freq + " classes");
            int ixi = 0;
            for (int counter = 0; counter < counts.length; counter++) {
                if (counts[counter] == max) {
                    ix[ixi] = counter;//save index of each max count value
                    ixi++; // increase index of ix array
                }
            }

            for (int i : ix) System.out.println("class index: " + i);

            //now choose one at random
            Random generator = new Random();
            //get random number 0 <= rIndex < size of ix
            int rIndex = generator.nextInt(ix.length);
            System.out.println("random index: " + rIndex);
            int nIndex = ix[rIndex];
            //return unique value at that index
            return uniqueValues[nIndex];
        }

    }

    /**
     * Returns a string with the algorithm's result
     *
     * @param s sample object
     * @return String
     */

    public String classify(Sample s) {


        // Implement KNN
        int k = 1;
        for (Instrument instrument : instrumentList) {

            double dist = 0.0;
            for (int j = 0; j < instrument.instrumentAttributes.length; j++) {

                dist += Math.pow(instrument.instrumentAttributes[j] - s.sampleAttributes[j], 2);
            }
            double distance = Math.sqrt(dist);
            //System.out.println(distance);
            resultList.add(new Result(distance, instrument.instrumentName));
        }
        resultList.sort(new DistanceComparator());
        String[] neighbors = new String[k];
        for (int i = 0; i < k; i++) {

            System.out.println(resultList.get(i).instrumentName + "...." + resultList.get(i).distance);
            neighbors[i] = resultList.get(i).instrumentName;
        }
        s.sampleName = findMajorityClass(neighbors);

        return s.sampleName;
    }


    /**
     * Generic class to get distance values and compare labels
     */

    class Result {
        double distance;
        String instrumentName;

        Result(double distance, String instrumentName) {
            this.instrumentName = instrumentName;
            this.distance = distance;
        }
    }

    /**
     * Comparator class used to compare results via distances
     */

    class DistanceComparator implements Comparator<Result> {
        @Override
        public int compare(Result a, Result b) {
            return a.distance < b.distance ? -1 : a.distance == b.distance ? 0 : 1; //compares distances
        }
    }
}
