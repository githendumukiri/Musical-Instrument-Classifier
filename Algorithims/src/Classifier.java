import java.io.File;
/**
 * This interface outlines the basic behavior of a given Classifier.
 *
 * @author Githendu Mukiri
 * @version 1.0 2019-05-16
 *
 */
public interface Classifier {

    void train(File trainData);
    String classify(Sample s);
}
