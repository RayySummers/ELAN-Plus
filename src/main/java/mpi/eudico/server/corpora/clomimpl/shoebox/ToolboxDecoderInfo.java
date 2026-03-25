package mpi.eudico.server.corpora.clomimpl.shoebox;

import java.util.List;

import mpi.eudico.server.corpora.clom.DecoderInfo;

/**
 * A decoder info object for Toolbox/Shoebox files.
 */
public class ToolboxDecoderInfo implements DecoderInfo {
	/** a default duration value per block or record */
    public static final int DEFAULT_BLOCK_DURATION = 1000;

    private boolean timeInRefMarker;
    private boolean allUnicode;
    private long blockDuration;
    private String shoeboxFilePath;
    private String typeFile = "";
    private List<MarkerRecord> shoeboxMarkers;
    
    /**
     * Creates a new ToolboxDecoderInfo object.
     */
    public ToolboxDecoderInfo() {
        
    }
    
    /**
     * Creates a new ToolboxDecoderInfo object.
     * 
     * @param shoeboxFilePath the path to the Toolbox/Shoebox file
     */
    public ToolboxDecoderInfo(String shoeboxFilePath) {
        this.shoeboxFilePath = shoeboxFilePath;
    }
    
    /**
     * Returns the block duration, the duration of a record when no time
     * information is present in the record.
     * 
     * @return the block duration
     */
    public long getBlockDuration() {
        return blockDuration;
    }
    
    /**
     * Sets the preferred/default block duration.
     * 
     * @param blockDuration the new block duration
     */
    public void setBlockDuration(long blockDuration) {
        this.blockDuration = blockDuration;
    }
    
    /**
     * Returns the path to the Shoebox/Toolbox file.
     * 
     * @return the path to the Shoebox/Toolbox file
     */
    @Override
	public String getSourceFilePath() {
        return shoeboxFilePath;
    }
    
    /**
     * Sets the path to the Shoebox/Toolbox file.
     * 
     * @param shoeboxFilePath the path to the Shoebox/Toolbox file
     */
    public void setSourceFilePath(String shoeboxFilePath) {
        this.shoeboxFilePath = shoeboxFilePath;
    }
    
    /**
     * Returns whether or not the begin time of a marker unit/annotation should
     * be extracted from the record marker.
     * 
     * @return {@code true} if the time is encoded in the {@code \ref}
     * marker, {@code false} otherwise
     */
    public boolean isTimeInRefMarker() {
        return timeInRefMarker;
    }
    
    /**
     * Sets whether the begin time of a marker unit/annotation should be
     * extracted from the record marker.
     * 
     * @param timeInRefMarker if {@code true} the time will be extracted from
     * the {@code \ref} marker
     */
    public void setTimeInRefMarker(boolean timeInRefMarker) {
        this.timeInRefMarker = timeInRefMarker;
    }
    
    /**
     * Returns the list of Shoebox/Toolbox Markers.
     * 
     * @return the Shoebox/Toolbox {@code MarkerRecord}s
     */
    public List<MarkerRecord> getShoeboxMarkers() {
        return shoeboxMarkers;
    }
    
    /**
     * Sets the list of Shoebox/Toolbox markers.
     * 
     * @param shoeboxMarkers the Shoebox/Toolbox markers 
     */
    public void setShoeboxMarkers(List<MarkerRecord> shoeboxMarkers) {
        this.shoeboxMarkers = shoeboxMarkers;
    }
    
    /**
     * Returns the path to the {@code .typ} file.
     * 
     * @return the {@code .typ} file.
     */
    public String getTypeFile() {
        return typeFile;
    }
    
    /**
     * Sets the path to the .typ file.
     * 
     * @param typeFile the type file
     */
    public void setTypeFile(String typeFile) {
        this.typeFile = typeFile;
    }
    
    /**
     * Returns whether all marker fields in the Toolbox file should be 
     * considered as Unicode, instead of the default assumption of ISO Latin.
     * 
     * @return {@code true} if all marker field are Unicode
     */
    public boolean isAllUnicode() {
        return allUnicode;
    }
    
    /**
     * Sets whether all markers should be treated as Unicode markers.
     * 
     * @param allUnicode if true all fields should be parsed as Unicode fields 
     */
    public void setAllUnicode(boolean allUnicode) {
        this.allUnicode = allUnicode;
    }
}
