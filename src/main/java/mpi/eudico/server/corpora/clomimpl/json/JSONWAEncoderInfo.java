package mpi.eudico.server.corpora.clomimpl.json;

import java.util.List;

import mpi.eudico.server.corpora.clom.EncoderInfo;

/**
 * Encoder info class for export to (WebAnnotation) JSON text. 
 */
public class JSONWAEncoderInfo implements EncoderInfo {
	/** a list of names of tiers to export */
	private List<String> selectedTiers;
	
    /** 0 or the begin value of the selection */
    private long beginTime = 0;
    /** The end of the selection or the duration of the media file */
    private long endTime = Long.MAX_VALUE;
    /** generate incremental id's  or use ELAN annotation id's */
    private boolean incrementalIDs;
    /** e.g. text/plain or text/html */
    private String bodyFormat;
    /** determines how time intervals should be formatted, using a FragmentSelector type or not */
    private boolean fragmentSelector;
    /** the name of the generator of the output */
    private String generator;
    /** an optional string to add as the purpose of annotations */
    private String purpose;
    /** sets the indentation level of the resulting json */
    private int indentationLevel;
    /** create a target for the first media file only or for all media files */
    private boolean singleTargetExport;
    /** indicates that the output is for a preview and can therefore be a partial
     * encoding of the file */
    private boolean encodePreview;
    
    /**
     * Constructor
     */
	public JSONWAEncoderInfo() {
		super();
	}

	/**
	 * Returns a list of names of selected tiers.
	 * 
	 * @return the selected tiers
	 */
	public List<String> getSelectedTiers() {
		return selectedTiers;
	}

	/**
	 * Sets the list of names of selected tiers.
	 * 
	 * @param selectedTiers the selected tiers
	 */
	public void setSelectedTiers(List<String> selectedTiers) {
		this.selectedTiers = selectedTiers;
	}

	/**
	 * Returns the begin time of the selected interval.
	 * 
	 * @return the begin time
	 */
	public long getBeginTime() {
		return beginTime;
	}

	/**
	 * Sets the begin time of the selected interval.
	 * 
	 * @param beginTime the begin time
	 */
	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	/**
	 * Returns the end time of the selected time interval.
	 * 
	 * @return the end time
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * Sets the end time of the selected time interval.
	 * 
	 * @param endTime the end time
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * Returns whether incremental ID's will be created, {@code false} by
	 * default.
	 * 
	 * @return {@code true} if incremental ID's are generated, {@code false}
	 * otherwise
	 */
	public boolean isIncrementalIDs() {
		return incrementalIDs;
	}

	/**
	 * Sets whether incremental ID's should be generated.
	 * 
	 * @param incrementalIDs if {@code true} incremental ID's will be generated
	 */
	public void setIncrementalIDs(boolean incrementalIDs) {
		this.incrementalIDs = incrementalIDs;
	}

	/**
	 * Returns the format or mime type of the annotation body.
	 * 
	 * @return the format of the body or {@code null} if not set
	 */
	public String getBodyFormat() {
		return bodyFormat;
	}

	/**
	 * Sets the format or mime type of the annotation body.
	 * 
	 * @param bodyFormat the format for the annotation body
	 */
	public void setBodyFormat(String bodyFormat) {
		this.bodyFormat = bodyFormat;
	}

	/**
	 * Returns whether a {@code FragmentSelector} type is used for media 
	 * fragments.
	 * 
	 * @return {@code true} if a {@code FragmentSelector} type is used for media 
	 * fragments, {@code false} otherwise 
	 */
	public boolean isFragmentSelector() {
		return fragmentSelector;
	}

	/**
	 * Sets whether a {@code FragmentSelector} type should be used for media fragments.
	 * 
	 * @param fragmentSelector if {@code true} {@code FragmentSelector}s will be used
	 */
	public void setFragmentSelector(boolean fragmentSelector) {
		this.fragmentSelector = fragmentSelector;
	}

	/**
	 * Returns the {@code generator} string.
	 * 
	 * @return the {@code generator} string or {@code null}
	 */
	public String getGenerator() {
		return generator;
	}

	/**
	 * Sets the {@code generator} string.
	 * 
	 * @param generator the {@code generator} to use
	 */
	public void setGenerator(String generator) {
		this.generator = generator;
	}

	/**
	 * Returns the (single) {@code purpose} used for all annotations.
	 * 
	 * @return the {@code purpose} used for the annotations 
	 */
	public String getPurpose() {
		return purpose;
	}

	/**
	 * Sets the {@code purpose} to be used for all annotations.
	 * 
	 * @param purpose the {@code purpose} to use
	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	/**
	 * Returns the indentation level for the {@code json} formatting.
	 * 
	 * @return the indentation level
	 */
	public int getIndentationLevel() {
		return indentationLevel;
	}

	/**
	 * Sets the indentation level for the {@code json} formatting..
	 * 
	 * @param indentationLevel the indentation level
	 */
	public void setIndentationLevel(int indentationLevel) {
		this.indentationLevel = indentationLevel;
	}

	/**
	 * Returns whether only one target is exported (for the main media file).
	 * 
	 * @return {@code true} if only one target is created, for the main media
	 * file, {@code false} if a target is created for every linked media file 
	 */
	public boolean isSingleTargetExport() {
		return singleTargetExport;
	}

	/**
	 * Sets whether one or multiple targets should be created in the export.
	 * 
	 * @param singleTargetExport if {@code true} only one target is created 
	 * in the export
	 */
	public void setSingleTargetExport(boolean singleTargetExport) {
		this.singleTargetExport = singleTargetExport;
	}

	/**
	 * Returns whether the encoding is for generating a preview.
	 * 
	 * @return {@code true} if the encoding is for a preview, {@code false} 
	 * by default
	 */
	public boolean isEncodePreview() {
		return encodePreview;
	}

	/**
	 * Sets whether the encoding is for generating a preview.
	 * 
	 * @param encodePreview if {@code true} the encoder can decide to limit
	 * the number of tiers and annotations to encode
	 */
	public void setEncodePreview(boolean encodePreview) {
		this.encodePreview = encodePreview;
	}
    
}
