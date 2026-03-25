package mpi.eudico.server.corpora.clomimpl.delimitedtext;

import java.io.File;
import java.util.List;

/**
 * EncoderInfo for export from a list of files.
 * Contains a reference to the list of eaf files to export and several booleans to 
 * determine if and how the transcription file names or paths should be exported.
 */
public class DelimitedTextEncoderInfoFiles extends DelimitedTextEncoderInfo {
	/** the list of eaf file to export */
	private List<File> files;
	private boolean fileNameInRow        = false;
	/** flags whether the file name should be included */
	private boolean includeFileName      = false;
	/** flags whether the file path should be included */
	private boolean includeFilePath      = true;
	/** flags whether the media headers should be included, 
	 *  consisting only of file path and media offset */
	private boolean includeMediaHeaders  = false;
	
	/**
	 * Constructor with the list of files as parameter, not null.
	 * 
	 * @param files the files to export
	 */
	public DelimitedTextEncoderInfoFiles(List<File> files) {
		super();
		if (files == null || files.isEmpty()) throw new NullPointerException("The list of files is null or empty.");
		this.files = files;
	}

	/**
	 * Returns the list of files to be exported.
	 * 
	 * @return the list of files to export
	 */
	public List<File> getFiles() {
		return files;
	}
	
	/**
	 * Returns whether the source file name is included on each row in the 
	 * export, {@code false} by default.
	 * 
	 * @return {@code true} if the file name is exported in each row, 
	 * {@code false} otherwise
	 */
	public boolean isFileNameInRow() {
		return fileNameInRow;
	}
	
	/**
	 * Sets whether the file name should be included on each row of the export.
	 * 
	 * @param fileNameInRow if {@code true} the file name will be exported in
	 * each row
	 */
	public void setFileNameInRow(boolean fileNameInRow) {
		this.fileNameInRow = fileNameInRow;
	}
	
	/**
	 * Returns whether the name of each file is included in the export, 
	 * {@code false} by default.
	 *  
	 * @return {@code true} if the file name is included in the export,
	 * {@code false} otherwise. 
	 */
	public boolean isIncludeFileName() {
		return includeFileName;
	}
	
	/**
	 * Sets whether the file name should be included in the export.
	 * 
	 * @param includeFileName if {@code true} the name of each file will
	 * be in the export
	 */
	public void setIncludeFileName(boolean includeFileName) {
		this.includeFileName = includeFileName;
	}
	
	/**
	 * Returns whether the absolute path of each file is included in the 
	 * export, {@code true} by default.
	 * 
	 * @return {@code true} if the path of each file is included in the export,
	 * {@code false} otherwise
	 */
	public boolean isIncludeFilePath() {
		return includeFilePath;
	}
	
	/**
	 * Sets whether the file path should be included in the export.
	 * 
	 * @param includeFilePath if {@code false} the file path will not be 
	 * included in the export
	 */
	public void setIncludeFilePath(boolean includeFilePath) {
		this.includeFilePath = includeFilePath;
	}
	
	/**
	 * Returns whether information about the linked media files is exported
	 * as a kind of sub-headers, preceding the annotation of each annotation
	 * file, {@code false} by default.
	 *   
	 * @return {@code true} if information about the media files is included
	 * in the export, {@code false} otherwise
	 */
	public boolean isIncludeMediaHeaders() {
		return includeMediaHeaders;
	}
	
	/**
	 * Sets whether information about linked media files should be included in
	 * the export.
	 * 
	 * @param includeMediaHeaders if {@code true} information about the media
	 * files will be included in the export
	 */
	public void setIncludeMediaHeaders(boolean includeMediaHeaders) {
		this.includeMediaHeaders = includeMediaHeaders;
	}

}
