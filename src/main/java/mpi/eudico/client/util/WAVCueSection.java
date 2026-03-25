package mpi.eudico.client.util;

/**
 * This class contains the information of one Cue Section in the tail of a
 * WAV-file: a reference to a Cue Point (i.e. time), duration, language and
 * text/label.
 *
 * @author Alexander Klassmann
 * @version Mar 17, 2004
 */
public class WAVCueSection {
    private final WAVCuePoint cuePoint;
    private final int sampleLength;
    private final String purposeID;
    private final short country;
    private final short language;
    private final short dialect;
    private final short codePage;
    private final String label;

    /**
     * Creates a new WAVCueSection instance.
     *
     * @param cuePoint the cue point
     * @param sampleLength the length of a sample
     * @param purposeID the purpose ID
     * @param country a country ic
     * @param language a language id
     * @param dialect a dialect id
     * @param codePage the code page
     * @param label the label
     */
    public WAVCueSection(WAVCuePoint cuePoint, int sampleLength,
        String purposeID, short country, short language, short dialect,
        short codePage, String label) {
        this.cuePoint = cuePoint;
        this.sampleLength = sampleLength;
        this.purposeID = purposeID;
        this.country = country;
        this.language = language;
        this.dialect = dialect;
        this.codePage = codePage;
        this.label = label;
    }

    /**
     * Returns the WAVCuePoint.
     *
     * @return the WAVCuePoint
     */
    public WAVCuePoint getCuePoint() {
        return cuePoint;
    }

    /**
     * Returns the sample length.
     *
     * @return the sample length
     */
    public int getSampleLength() {
        return sampleLength;
    }

    /**
     * Returns the purpose ID e.g. a value of "scrp" means script text, "capt"
     * means close-caption.
     *
     * @return the purpose id as string
     */
    public String getPurposeID() {
        return purposeID;
    }

    /**
     * Returns the country code.
     *
     * @return the country code
     */
    public short getCountry() {
        return country;
    }

    /**
     * Returns the language code.
     *
     * @return the language code
     */
    public short getLanguage() {
        return language;
    }

    /**
     * Returns the dialect code.
     *
     * @return the dialect code
     */
    public short getDialect() {
        return dialect;
    }

    /**
     * Returns the Code Page.
     *
     * @return the code page
     */
    public short getCodePage() {
        return codePage;
    }

    /**
     * Returns the label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns a parameterized string.                 
     *
     * @return a parameterized String
     */
    @Override
	public String toString() {
        return "Cue Point ID  : " + cuePoint.getID() + "\nSample Length : " +
        sampleLength + "\nPurpose ID    : " + purposeID + "\nCountry       : " +
        country + "\nLanguage      : " + language + "\nDialect       : " +
        dialect + "\nCode Page     : " + codePage + "\nLabel         : " +
        label + "\n";
    }
}
