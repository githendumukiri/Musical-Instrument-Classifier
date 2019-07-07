/**
 * This class the describes the attributes of the generic class Instrument.
 *
 * @author Githendu Mukiri
 * @version 1.0 2019-05-16
 *
 */
class Instrument {

    float[] instrumentAttributes;
    String instrumentName;

    Instrument(float[] instrumentAttributes, String instrumentName){
        this.instrumentName = instrumentName;
        this.instrumentAttributes = instrumentAttributes;
    }
}
