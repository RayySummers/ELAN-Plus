/**
 * 
 */
package mpi.eudico.server.corpora.clomimpl.subtitletext;

import mpi.eudico.server.corpora.clom.DecoderInfo;

/**
 * An information class for decoding supported subtitle text formats.
 * 
 * @author Han Sloetjes
 */
public class SubtitleDecoderInfo implements DecoderInfo {
	private String filePath;
	private SubtitleFormat format;
	private String fileEncoding;
	private int defaultDuration = 1000;
	private boolean removeHTML = true;
	
	/**
	 * Creates a new decoder info instance.
	 */
	public SubtitleDecoderInfo() {
		super();
	}

	/**
	 * @return the source file path
	 */
	@Override
	public String getSourceFilePath() {
		return filePath;
	}
	
	/**
	 * Sets the path to the source file.
	 * 
	 * @param sourceFilePath the path to the source path
	 */
	public void setSourceFilePath(String sourceFilePath) {
		this.filePath = sourceFilePath;
	}

	/**
	 * Returns the default annotation duration.
	 * 
	 * @return the duration of annotations that have a start time but no
	 * end time
	 */
	public int getDefaultDuration() {
		return defaultDuration;
	}

	/**
	 * Sets the duration to apply to annotation without an end time
	 * 
	 * @param defaultDuration the default duration for single point annotations
	 */
	public void setDefaultDuration(int defaultDuration) {
		this.defaultDuration = defaultDuration;
	}

	/**
	 * Returns the file encoding.
	 * 
	 * @return the encoding of file to import
	 */
	public String getFileEncoding() {
		return fileEncoding;
	}

	/**
	 * Sets the encoding of the import file.
	 * 
	 * @param fileEncoding the text encoding of the file to import
	 */
	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

	/**
	 * Returns the format the subtitle file probably is in.
	 * 
	 * @return the (probable) format of the subtitle file 
	 */
	public SubtitleFormat getFormat() {
		return format;
	}

	/**
	 * Sets the format f the file to import.
	 * 
	 * @param format the format the file is in (based on the file extension
	 *  or the information provided by the user)
	 */
	public void setFormat(SubtitleFormat format) {
		this.format = format;
	}

	/**
	 * Returns whether HTML tags will be removed from the input.
	 * 
	 * @return true if X/HTML tags should be removed from the subtitle lines
	 */
	public boolean isRemoveHTML() {
		return removeHTML;
	}

	/**
	 * Sets whether HTML tags should be removed when importing the subtitles.
	 * 
	 * @param removeHTML if true, X/HTML tags should be removed from the lines
	 */
	public void setRemoveHTML(boolean removeHTML) {
		this.removeHTML = removeHTML;
	}
	
}
