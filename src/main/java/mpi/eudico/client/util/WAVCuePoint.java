package mpi.eudico.client.util;

/**
 * This class contains the information of one Cue Point in the tail of a
 * WAV-file, mainly the position (i.e. time) and a possible label (null, if
 * not specified in the file).
 *
 * @author Alexander Klassmann
 * @version Mar 16, 2004
 */
public class WAVCuePoint {
    private final int ID;

    private final int position;

    private final int chunkStart;

    private final int blockStart;

    private final int sampleOffset;
    private String label = null;
    private String note = null;

    /**
     * Creates a new WAVCuePoint instance.
     *
     * @param ID the ID
     * @param position the position
     * @param chunkStart the start of the chunk
     * @param blockStart the start of the block
     * @param sampleOffset the sample offset
     */
    public WAVCuePoint(int ID, int position, int chunkStart, int blockStart,
        int sampleOffset) {
        this.ID = ID;
        this.position = position;
        this.chunkStart = chunkStart;
        this.blockStart = blockStart;
        this.sampleOffset = sampleOffset;
    }

    /**
     * Sets the label.
     *
     * @param label the label
     */
    protected void setLabel(String label) {
        this.label = label;
    }

    /**
     * Sets the note.
     *
     * @param note the note
     */
    protected void setNote(String note) {
        this.note = note;
    }

    /**
     * Returns the unique identification value ({@code int}!)
     *
     * @return the ID
     */
    public int getID() {
        return ID;
    }

    /**
     * Returns the play order position.
     *
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Returns the Byte Offset of the DataChunk.
     *
     * @return the chunk start
     */
    public int getChunkStart() {
        return chunkStart;
    }

    /**
     * Returns the Byte Offset to sample of First Channel.
     *
     * @return the block start
     */
    public int getBlockStart() {
        return blockStart;
    }

    /**
     * Returns the Byte Offset to sample byte of First Channel.
     *
     * @return the sample offset
     */
    public int getSampleOffset() {
        return sampleOffset;
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
     * Returns the note.
     *
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * Returns a string representation of the cue point.
     *
     * @return a parameterized string
     */
    @Override
	public String toString() {
        return "ID            : " + ID + "\nPosition      : " + position +
        "\nChunk Start   : " + chunkStart + "\nBlock Start   : " + blockStart +
        "\nSample Offset : " + sampleOffset +
        ((label != null) ? ("\nLabel         : " + label) : "") +
        ((note != null) ? ("\nNote          : " + note) : "") + "\n";
    }
}
