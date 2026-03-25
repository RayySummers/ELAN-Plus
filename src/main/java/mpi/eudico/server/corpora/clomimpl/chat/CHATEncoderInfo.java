/*
 * Created on Jan 26, 2005
 */
package mpi.eudico.server.corpora.clomimpl.chat;

import mpi.eudico.server.corpora.clom.EncoderInfo;

/**
 * CHAT specific EncoderInfo implementation.
 * may 06 (HS): added booleans for two export options:<br>
 * - whether or not to recalculate begin and end times of annotations based on the master media offset<br>
 * - if true the filename and begin and end time should be on a separate line (%snd or %mov) otherwise 
 * or this info is appended to the main annotation value 
 * apr 08 (HS): added boolean for inclusion of "%lan:" lines
 * @author hennie
 */
public class CHATEncoderInfo implements EncoderInfo {
    private boolean correctAnnotationTimes = true;
    private boolean timesOnSeparateLine = false;
    private boolean includeLangLine = false;
    private long mediaOffset = 0L;

	private String[][] mainTierInfo;
	private String[][] dependentTierInfo;
	
	/**
	 * Constructor. 
	 * @param mainTierInfo a two dimensional array of top level tier information
	 * @param dependentTierInfo a two dimensional array of dependent tier 
	 * information
	 */
	public CHATEncoderInfo(String[][] mainTierInfo, String[][] dependentTierInfo) {
		this.mainTierInfo = mainTierInfo;
		this.dependentTierInfo = dependentTierInfo;
	}
	
	/**
	 * Returns the information or top level tiers.
	 * 
	 * @return the top level tier information
	 */
	public String[][] getMainTierInfo() {
		return mainTierInfo;
	}
	
	/**
	 * Returns the information for dependent tiers.
	 * 
	 * @return the dependent tier information
	 */
	public String[][] getDependentTierInfo() {
		return dependentTierInfo;
	}
	
	/**
	 * Returns the flag for corrected, recalculated annotation times.
	 * 
	 * @return the corrected annotation times flag
	 */
    public boolean getCorrectAnnotationTimes() {
        return correctAnnotationTimes;
    }
    
    /**
     * Sets whether annotation times need to be recalculated based on media 
     * offset.
     * 
     * @param correctAnnotationTimes the new correct times flag
     */
    public void setCorrectAnnotationTimes(boolean correctAnnotationTimes) {
        this.correctAnnotationTimes = correctAnnotationTimes;
    }
    
    /**
     * Returns whether time information has to be placed on a separate line.
     * 
     * @return if {@code true} the time information is on a separate output
     * line, otherwise it is appended to the main tier
     */
    public boolean isTimesOnSeparateLine() {
        return timesOnSeparateLine;
    }
    
    /**
     * Sets whether the time information is on a separate output line.
     * 
     * @param timesOnSeparateLine if {@code true} the time information will be
     * placed on a separate output line, otherwise it is appended to the main
     * tier
     */
    public void setTimesOnSeparateLine(boolean timesOnSeparateLine) {
        this.timesOnSeparateLine = timesOnSeparateLine;
    }
    
    /**
     * Returns the media offset of the main media file.
     * 
     * @return the media offset
     */
    public long getMediaOffset() {
        return mediaOffset;
    }
    
    /**
     * Sets the media offset of the main media file.
     * 
     * @param mediaOffset the media offset
     */
    public void setMediaOffset(long mediaOffset) {
        this.mediaOffset = mediaOffset;
    }

    /**
     * Returns whether a language line has to be included in the output.
     * 
     * @return whether a language line is in the output
     */
	public boolean isIncludeLangLine() {
		return includeLangLine;
	}

	/**
	 * Sets whether a language line has to be included in the output.
	 * 
	 * @param includeLangLine if {@code true} a language identifier will be
	 * part of the output, otherwise it won't
	 */
	public void setIncludeLangLine(boolean includeLangLine) {
		this.includeLangLine = includeLangLine;
	}
}
