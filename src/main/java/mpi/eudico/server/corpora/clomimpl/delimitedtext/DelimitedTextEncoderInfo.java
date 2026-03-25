package mpi.eudico.server.corpora.clomimpl.delimitedtext;

import java.io.File;
import java.util.List;

import mpi.eudico.server.corpora.clom.EncoderInfo;

/**
 * An encoder class for export to tab delimited text.
 * 
 * Follows other encoder classes that have private fields 
 * with getters and setters.
 * 
 * Options that could be added in the future: other delimiters (e.g. ; or ,),
 * text values in quotes (""), new line character(s) (\n, \r\n), writing BOM etc.
 *  
 */
public abstract class DelimitedTextEncoderInfo implements EncoderInfo {
	/** the list of tiers to export */
	private List<String> tierNames;
	/** the file to export to */
	private File exportFile;
	/** the encoding to use, defaults to UTF-8 */
	private String charEncoding          = "UTF-8";
	/** begin time if only part of the annotations have to be exported */
	private long beginTime               = 0L; 
	/** end time if only part of the annotations have to be exported */
	private long endTime                 = Long.MAX_VALUE;
	/** flags whether the time values should be corrected based on the offset of the master media */
	private boolean addMasterMediaOffset = false;
	/** flags whether the cv description should be included in the output*/
	private boolean includeCVDescription = false;
	/** flags whether the begin time should be included in the output */
	private boolean includeBeginTime     = true;
	/** flags whether the end time should be included in the output */
	private boolean includeEndTime       = true;
	/** flags whether the duration should be included in the output */
	private boolean includeDuration      = true;
	/** flags whether the time should be formatted as hh:mm:ss.ms */
	private boolean includeHHMM          = true;
	/** flags whether the time should be formatted as ss.ms */
	private boolean includeSSMS          = true;
	/** flags whether the time should be in milliseconds */
	private boolean includeMS            = false;
	/** flags whether the time should be in SMPTE code */
	private boolean includeSMPTE         = false;
	/** flags whether the time should be in PAL code */
	private boolean palFormat            = false;
	/** flags whether the time should be in PAL-50fps code */
	private boolean pal50Format            = false;
	/** flags whether the tier names should be included */
	private boolean includeNames         = true;
	/** flags whether the participant attribute should be included */
	private boolean includeParticipants  = true;
	/** if true the annotation id is exported with the value */
	private boolean includeAnnotationId  = false;
	
	// fields and defaults for the case of separate column per tier export
	//private boolean separateColumnPerTier = false;
	/**
     * flags whether values of annotations spanning other annotations should be
     * repeated
     */
    private boolean repeatValues  = true;
    /**
     * flags whether annotations of different "blocks" should be combined in
     * the same row  and if values should be repeated
     */
    private boolean combineBlocks = true;
    /** the default assumption is that the export is to a tab delimited .txt file,
     * when the format is .csv all text fields need to be enclosed in "" etc. */
    private boolean exportCSVFormat = false;
	
    /**
     * Creates a new encoder info instance.
     */
    public DelimitedTextEncoderInfo() {
		super();
	}

	/**
     * Returns a list of tier names.
     * 
     * @return a list of tier names
     */
	public List<String> getTierNames() {
		return tierNames;
	}
	
	/**
	 * Sets the list of tier names.
	 * 
	 * @param tierNames the list of tier names
	 */
	public void setTierNames(List<String> tierNames) {
		this.tierNames = tierNames;
	}
	
	/**
	 * Returns the {@code File} to export to.
	 * 
	 * @return the export file
	 */
	public File getExportFile() {
		return exportFile;
	}
	
	/**
	 * Sets the {@code File} object to export to.
	 * 
	 * @param exportFile the export file
	 */
	public void setExportFile(File exportFile) {
		this.exportFile = exportFile;
	}
	
	/**
	 * Returns the character encoding as a {@code String}.
	 * 
	 * @return the character encoding of the export
	 */
	public String getCharEncoding() {
		return charEncoding;
	}
	
	/**
	 * Sets the character encoding for the export.
	 * 
	 * @param charEncoding the encoding to use for the export
	 */
	public void setCharEncoding(String charEncoding) {
		this.charEncoding = charEncoding;
	}
	
	/**
	 * Returns the begin time.
	 * 
	 * @return the begin time
	 */
	public long getBeginTime() {
		return beginTime;
	}
	
	/**
	 * Sets the begin time of a selection to export.
	 * 
	 * @param beginTime the begin time
	 */
	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}
	
	/**
	 * Returns the end time.
	 * 
	 * @return the end time
	 */
	public long getEndTime() {
		return endTime;
	}
	
	/**
	 * Sets the end time of a selection to export.
	 * 
	 * @param endTime the end time
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	/**
	 * Returns whether the media offset has to be added to the
	 * annotation times, {@code false} by default.
	 * 
	 * @return {@code true} if the annotation times need to be recalculated,
	 * {@code false} otherwise
	 */
	public boolean isAddMasterMediaOffset() {
		return addMasterMediaOffset;
	}
	
	/**
	 * Sets whether the media offset has to be added to the
	 * annotation times.
	 * 
	 * @param addMasterMediaOffset if {@code true} annotation times will be 
	 * recalculated on export
	 */
	public void setAddMasterMediaOffset(boolean addMasterMediaOffset) {
		this.addMasterMediaOffset = addMasterMediaOffset;
	}
	
	/**
	 * Returns whether CV entry descriptions are included in the export, 
	 * {@code false} by default.
	 * 
	 * @return {@code true} if CV entry descriptions are part of the exported
	 * text, {@code false} otherwise
	 */
	public boolean isIncludeCVDescription() {
		return includeCVDescription;
	}
	
	/**
	 * Sets whether CV entry descriptions should be included in the export.
	 * 
	 * @param includeCVDescription if ({@code true} CV entry descriptions are
	 * included in the export
	 */
	public void setIncludeCVDescription(boolean includeCVDescription) {
		this.includeCVDescription = includeCVDescription;
	}
	
	/**
	 * Returns whether the annotation begin time is included in the export, 
	 * {@code true} by default.
	 * 
	 * @return {@code true} if the begin time of annotations is part of the
	 * export, {@code false} otherwise
	 */
	public boolean isIncludeBeginTime() {
		return includeBeginTime;
	}
	
	/**
	 * Sets whether the annotation begin time should be included in the export.
	 * 
	 * @param includeBeginTime if {@code false} the begin time is not included
	 * in the export
	 */
	public void setIncludeBeginTime(boolean includeBeginTime) {
		this.includeBeginTime = includeBeginTime;
	}
	
	/**
	 * Returns whether the annotation end time is included in the export, 
	 * {@code true} by default.
	 * 
	 * @return {@code true} if the end time of annotations is part of the
	 * export, {@code false} otherwise
	 */
	public boolean isIncludeEndTime() {
		return includeEndTime;
	}
	
	/**
	 * Sets whether the annotation end time should be included in the export.
	 * 
	 * @param includeEndTime if {@code false} the end time is not included in 
	 * the export
	 */
	public void setIncludeEndTime(boolean includeEndTime) {
		this.includeEndTime = includeEndTime;
	}
	
	/**
	 * Returns whether the annotation duration is included in the export, 
	 * {@code true} by default.
	 * 
	 * @return {@code true} if the duration of annotations is included in the
	 * export, {@code false} otherwise
	 */
	public boolean isIncludeDuration() {
		return includeDuration;
	}
	
	/**
	 * Sets whether the annotation duration should be included in the export.
	 * 
	 * @param includeDuration if {@code false} the duration is not included in
	 * the export
	 */
	public void setIncludeDuration(boolean includeDuration) {
		this.includeDuration = includeDuration;
	}
	
	/**
	 * Returns whether time values in hour:minute:second.ms format are included
	 * in the export, {@code true} by default.
	 * 
	 * @return {@code true} if time values in hh:mm:ss.ms format are included
	 * in the export, {@code false} otherwise
	 */
	public boolean isIncludeHHMM() {
		return includeHHMM;
	}
	
	/**
	 * Sets whether time values in hour:minute:second.ms format should be 
	 * included in the export.
	 * 
	 * @param includeHHMM if {@code false} the hh:mm:ss.ms format is not 
	 * included in the export
	 */
	public void setIncludeHHMM(boolean includeHHMM) {
		this.includeHHMM = includeHHMM;
	}
	
	/**
	 * Returns whether time values in decimal fraction seconds format are
	 * included in the export, {@code true} by default.
	 * 
	 * @return {@code true} if time values in ss.ms format are included in the
	 * export, {@code false} otherwise
	 */
	public boolean isIncludeSSMS() {
		return includeSSMS;
	}
	
	/**
	 * Sets whether time values in decimal fraction seconds format should be
	 * included in the export.
	 * 
	 * @param includeSSMS if {@code false} the ss.ms format is not included
	 * in the export
	 */
	public void setIncludeSSMS(boolean includeSSMS) {
		this.includeSSMS = includeSSMS;
	}
	
	/**
	 * Returns whether time values in integer milliseconds format are included
	 * in the export, {@code false} by default.
	 *  
	 * @return {@code true} if time values in ms format are included in the the
	 * export, {@code false} otherwise  
	 */
	public boolean isIncludeMS() {
		return includeMS;
	}
	
	/**
	 * Sets whether time values in integer milliseconds format should be
	 * included in the export.
	 * 
	 * @param includeMS if {@code true} time values in ms format are included
	 * in the export
	 */
	public void setIncludeMS(boolean includeMS) {
		this.includeMS = includeMS;
	}
	
	/**
	 * Returns whether timestamps in {@code SMPTE} format are included
	 * in the export, {@code false} by default.
	 *  
	 * @return {@code true} if timestamps in {@code SMPTE} format are included
	 * in the the export, {@code false} otherwise  
	 */
	public boolean isIncludeSMPTE() {
		return includeSMPTE;
	}
	
	/**
	 * Sets whether timestamps in {@code SMPTE} format should be included in
	 * the export.
	 * 
	 * @param includeSMPTE if {@code true} timestamps in {@code SMPTE} format
	 * are included in the export
	 */
	public void setIncludeSMPTE(boolean includeSMPTE) {
		this.includeSMPTE = includeSMPTE;
	}
	
	/**
	 * Returns whether timestamps in {@code PAL} format are included
	 * in the export, {@code false} by default.
	 *  
	 * @return {@code true} if timestamps in {@code PAL} format are included
	 * in the the export, {@code false} otherwise  
	 */
	public boolean isPalFormat() {
		return palFormat;
	}
	
	/**
	 * Sets whether timestamps in {@code PAL} format should be included in
	 * the export.
	 * 
	 * @param palFormat if {@code true} timestamps in {@code PAL} format are
	 * included in the export
	 */
	public void setPalFormat(boolean palFormat) {
		this.palFormat = palFormat;
	}
	
	/**
	 * Returns whether timestamps in {@code PAL 50fps} format are included
	 * in the export, {@code false} by default.
	 *  
	 * @return {@code true} if timestamps in {@code PAL 50fps} format are
	 * included in the the export, {@code false} otherwise  
	 */
	public boolean isPal50Format() {
		return pal50Format;
	}
	
	/**
	 * Sets whether timestamps in {@code PAL 50fps} format should be included
	 * in the export.
	 * 
	 * @param pal50Format if {@code true} timestamps in {@code PAL 50fps}
	 * format are included in the export
	 */
	public void setPal50Format(boolean pal50Format) {
		this.pal50Format = pal50Format;
	}
	
	/**
	 * Returns whether tier names are included in the export, {@code true} by
	 * default.
	 * 
	 * @return {@code true} if tier names are included in the export, 
	 * {@code false} otherwise
	 */
	public boolean isIncludeNames() {
		return includeNames;
	}
	
	/**
	 * Sets whether tier names should be included in the export.
	 * 
	 * @param includeNames if {@code false} tier names are not included in the
	 * export
	 */
	public void setIncludeNames(boolean includeNames) {
		this.includeNames = includeNames;
	}
	
	/**
	 * Returns whether participant names are included in the export, 
	 * {@code true} by default.
	 * 
	 * @return {@code true} if participant names are included in the export, 
	 * {@code false} otherwise
	 */
	public boolean isIncludeParticipants() {
		return includeParticipants;
	}
	
	/**
	 * Sets whether participant names should be included in the export.
	 * 
	 * @param includeParticipants if {@code false} participant names are not 
	 * included in the export
	 */
	public void setIncludeParticipants(boolean includeParticipants) {
		this.includeParticipants = includeParticipants;
	}
	
	// getters and setters for the column-per-tier export with repeating of values
	/**
	 * Returns whether annotation values are repeated in multiple rows
	 * based on overlaps with other annotations, {@code true} by default.
	 * 
	 * @return {@code true} if annotation values are repeated in the
	 * export, {@code false} otherwise
	 */
	public boolean isRepeatValues() {
		return repeatValues;
	}
	
	/**
	 * Sets whether annotation values should be repeated in multiple rows
	 * based on overlaps with other annotations.
	 * 
	 * @param repeatValues if {@code false} each annotation values will appear 
	 * only once in the export
	 */
	public void setRepeatValues(boolean repeatValues) {
		this.repeatValues = repeatValues;
	}
	
	/**
	 * Returns whether annotations in different annotation trees are 
	 * combined in the same row (in case of overlap), {@code true} by default.
	 * 
	 * @return {@code true} if annotations of different blocks or trees are 
	 * combined on the same row in the export, {@code false} otherwise
	 */
	public boolean isCombineBlocks() {
		return combineBlocks;
	}
	
	/**
	 * Sets whether annotations of different annotation trees should be
	 * combined in the same row in case of overlap.
	 * 
	 * @param combineBlocks if {@code false} annotations in one annotation tree
	 * will not be combined with annotations in other trees
	 */
	public void setCombineBlocks(boolean combineBlocks) {
		this.combineBlocks = combineBlocks;
	}

	/**
	 * Returns whether annotation ID's are included in the export (in their own
	 * column), {@code false} by default.
	 * 
	 * @return {@code true} if annotation ID's are included in the export
	 */
	public boolean isIncludeAnnotationId() {
		return includeAnnotationId;
	}
	
	/**
	 * Sets whether annotation ID's should be included in a separate column
	 *  in the export.
	 *   
	 * @param includeAnnotationId if {@code true} annotation ID's will be 
	 * included in the export
	 */
	public void setIncludeAnnotationId(boolean includeAnnotationId) {
		this.includeAnnotationId = includeAnnotationId;
	}
	
	/**
	 * Returns whether the delimited text is exported as a {@code .csv} (comma
	 * separated values) file, {@code false} by default. The default format is
	 * tab-delimited text ({@code .txt}). 
	 * 
	 * @return {@code true} if the export is in {@code .csv} format, 
	 * {@code false} otherwise
	 */
	public boolean isExportCSVFormat() {
		return exportCSVFormat;
	}
	
	/**
	 * Sets whether the text should be exported as comma separated values 
	 * {@code .csv} file.
	 * 
	 * @param exportCSVFormat if {@code true} the text will be exported to a 
	 * {@code .csv} format file.
	 */
	public void setExportCSVFormat(boolean exportCSVFormat) {
		this.exportCSVFormat = exportCSVFormat;
	}
	
}
