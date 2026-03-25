package mpi.eudico.server.corpora.clomimpl.wordlist;

import java.io.File;
import java.util.List;

import mpi.eudico.server.corpora.clom.EncoderInfo;

/**
 * An {@code EncoderInfo} class containing settings for the export of a word
 * list or list of unique annotation values from a single or multiple {@code EAF}
 * file(s). The list is saved as a text file.
 * 
 */
public class WordListEncoderInfo implements EncoderInfo {
	private File exportFile;
	private List<String> selectedTiers;
	private String encoding;
	private String delimiters;
	private boolean countOccurrences;
	private boolean includeCounts;
	private boolean includeFreqPercent;
	private int exportMode;// annotations or words
	
	/** constant for the export of words (after tokenization of annotations */
    public static final int WORDS = 0;
    /** constant for export of unique annotation values */
    public static final int ANNOTATIONS = 1;
    
    /**
     * Constructor, initializes some default values.
     */
	public WordListEncoderInfo() {
		encoding = "UTF-8";
		delimiters = "";
	}

	/**
	 * Returns the destination file.
	 * 
	 * @return the file to write the results to, not {@code null}
	 */
	public File getExportFile() {
		return exportFile;
	}

	/**
	 * Sets the destination file
	 * 
	 * @param exportFile the file to write to, not {@code null}
	 */
	public void setExportFile(File exportFile) {
		this.exportFile = exportFile;
	}

	/**
	 * Returns a list of names of selected tiers. If {@code null} is returned
	 * or an empty list, all tiers will be processed.
	 * 
	 * @return a list of selected tier names
	 */
	public List<String> getSelectedTiers() {
		return selectedTiers;
	}

	/**
	 * Sets the list of names of tiers to include in the word list generation.
	 * 
	 * @param selectedTiers the tiers to export
	 */
	public void setSelectedTiers(List<String> selectedTiers) {
		this.selectedTiers = selectedTiers;
	}

	/**
	 * Returns the character encoding to be used for the export file, defaults
	 * to "UTF-8".
	 * 
	 * @return the character encoding for the export
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Sets the character encoding or the export file.
	 * 
	 * @param encoding the name of the encoding to use
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Returns the characters to be used as delimiters for separating tokens
	 * in annotations. The empty string is equivalent to no tokenization, as
	 * is the case in {@code WordListEncoderInfo#ANNOTATIONS} mode.
	 * 
	 * @return the delimiters embedded in a string object 
	 */
	public String getDelimiters() {
		return delimiters;
	}

	/**
	 * Sets custom delimiters to be used for the tokenization process.
	 * 
	 * @param delimiters the delimiters to use
	 */
	public void setDelimiters(String delimiters) {
		this.delimiters = delimiters;
	}

	/**
	 * Returns whether word counting should be performed.
	 * 
	 * @return the word counting flag
	 */
	public boolean isCountOccurrences() {
		return countOccurrences;
	}

	/**
	 * Sets whether words or annotations should be counted.
	 * 
	 * @param countOccurrences the word counting flag
	 */
	public void setCountOccurrences(boolean countOccurrences) {
		this.countOccurrences = countOccurrences;
	}

	/**
	 * Returns whether the word count and other statistics should be included
	 * in the output file.
	 * 
	 * @return if {@code true} some statistics are written in the output file
	 */
	public boolean isIncludeCounts() {
		return includeCounts;
	}

	/**
	 * Sets whether the word count should be included in the output.
	 * 
	 * @param includeCounts if {@code true} some statistics are written in the
	 * output file
	 */
	public void setIncludeCounts(boolean includeCounts) {
		this.includeCounts = includeCounts;
	}

	/**
	 * Returns whether frequency percentages for each word should be included
	 * in a column in the word list.
	 *  
	 * @return if {@code true} frequency percentages are exported too
	 */
	public boolean isIncludeFreqPercent() {
		return includeFreqPercent;
	}

	/**
	 * Sets whether frequency percentages should be included in the export file.
	 * 
	 * @param includeFreqPercent if {@code true} frequency percentages are 
	 * calculated for each word and included in the output
	 */
	public void setIncludeFreqPercent(boolean includeFreqPercent) {
		this.includeFreqPercent = includeFreqPercent;
	}

	/**
	 * Returns the selected export mode.
	 * 
	 * @return the export mode, one of {@code #WORDS} and {@code #ANNOTATIONS}
	 */
	public int getExportMode() {
		return exportMode;
	}

	/**
	 * Sets the mode for this export.
	 * 
	 * @param exportMode one of {@code #WORDS} and {@code #ANNOTATIONS}
	 */
	public void setExportMode(int exportMode) {
		this.exportMode = exportMode;
	}

	
}
