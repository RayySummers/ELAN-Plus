package mpi.eudico.server.corpora.clomimpl.html;

import java.util.List;

import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.util.TimeFormatter;

/**
 * A class to hold properties and settings for export to time aligned
 * interlinear text (e.g. in html).
 */
public class TAIEncoderInfo implements EncoderInfo {
	/** a list of tiers to export and the style information for each tier,
	 * should not be null or empty */
	private List<TAITierSetting> tierSettings;
    /**
     * 0 or the begin value of the selection
     */
    private long beginTime = 0;

    /**
     * The end of the selection or the duration of the media file
     */
    private long endTime = Long.MAX_VALUE;
    /** width for one time unit, the number of milliseconds represented by one character position */
    private int timeUnit;
    /** maximal space for one block of annotations, in number of characters */
    private int blockSpace;
    /** space for tier names, number of characters */
    private int leftMargin;
    /** font size in HTML */
    private int fontSize;
    /** alignment of the annotation value within the area of its time span, left or right */
    private int textAlignment;// left or right
    /** wrap lines within one block (if reference tier is used) */
    private boolean wrapWithinBlock;
    /** whether or not a line should be added for time codes and a time line */
    private boolean showTimeLine;
    /** if time values are to be printed, use this format */
    private TimeFormatter.TIME_FORMAT timeFormat;
    /** if annotations are not underlined, mark boundaries of annotations with special characters */
    private boolean showAnnotationBoundaries; // for non-underlined annotations
    /** the name of the reference tier, in case of output based on a reference tier, null otherwise */
    private String refTierName;
    
    /**
     * Constructor.
     */
	public TAIEncoderInfo() {
		super();
	}

	/**
	 * Sets per tier settings.
	 * 
	 * @param tierSettings the output settings per tier
	 */
	public void setTierSettings(List<TAITierSetting> tierSettings) {
		this.tierSettings = tierSettings;
	}
	
	/**
	 * Returns the tier settings.
	 * 
	 * @return the tier settings
	 */
	public List<TAITierSetting> getTierSettings() {
		return tierSettings;
	}
	
	/**
	 * Returns the name of the reference tier.
	 * 
	 * @return the name of the reference tier or {@code null}
	 */
    public String getRefTierName() {
		return refTierName;
	}

    /**
     * Sets the name of the reference tier.
     * 
     * @param refTierName the name of the reference tiers
     */
	public void setRefTierName(String refTierName) {
		this.refTierName = refTierName;
	}

	/**
     * Returns the (selection) begin time.
     *
     * @return the begin time.
     */
    public long getBeginTime() {
        return beginTime;
    }

    /**
     * Sets the (selection) begin time.
     *
     * @param beginTime he begin time
     */
    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    /**
     * Returns the (selection) end time.
     *
     * @return the end time.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Sets the (selection) end time.
     *
     * @param endTime The end time
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * Returns the time unit (milliseconds per character).
     * 
     * @return the time unit
     */
	public int getTimeUnit() {
		return timeUnit;
	}

	/**
	 * Sets the time unit.
	 *  
	 * @param timeUnit the new number of milliseconds per character
	 */
	public void setTimeUnit(int timeUnit) {
		this.timeUnit = timeUnit;
	}
	
	/**
	 * Returns the time format.
	 * 
	 * @return the format used for time values
	 */
	public TimeFormatter.TIME_FORMAT getTimeFormat() {
		return timeFormat;
	}

	/**
	 * Sets the format to use for time values.
	 * 
	 * @param timeFormat the time format to use
	 */
	public void setTimeFormat(TimeFormatter.TIME_FORMAT timeFormat) {
		this.timeFormat = timeFormat;
	}

	/**
	 * Returns the space between interlinear blocks.
	 * 
	 * @return the block space
	 */
	public int getBlockSpace() {
		return blockSpace;
	}

	/**
	 * Sets the space between blocks.
	 * 
	 * @param blockSpace the space between blocks
	 */
	public void setBlockSpace(int blockSpace) {
		this.blockSpace = blockSpace;
	}

	/**
	 * Returns the left margin size.
	 * 
	 * @return the width of the left margin
	 */
	public int getLeftMargin() {
		return leftMargin;
	}

	/**
	 * Sets the width of the left margin.
	 * 
	 * @param leftMargin the margin on the left side
	 */
	public void setLeftMargin(int leftMargin) {
		this.leftMargin = leftMargin;
	}

	/**
	 * Returns the font size.
	 * 
	 * @return the font size
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * Sets the font size.
	 * 
	 * @param fontSize the size of the font
	 */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * Returns whether line wrapping is performed within a block.
	 * 
	 * @return {@code true} if line wrapping is performed within the block,
	 * {@code false} otherwise
	 */
	public boolean isWrapWithinBlock() {
		return wrapWithinBlock;
	}

	/**
	 * Sets whether line wrapping should be performed within the block.
	 * 
	 * @param wrapWithinBlock if {@code true} long lines will be wrapped within
	 * its block
	 */
	public void setWrapWithinBlock(boolean wrapWithinBlock) {
		this.wrapWithinBlock = wrapWithinBlock;
	}

	/**
	 * Returns a constant for the text alignment.
	 * 
	 * @return a constant for the text alignment
	 */
	public int getTextAlignment() {
		return textAlignment;
	}

	/**
	 * Sets the text alignment.
	 * 
	 * @param textAlignment the new text alignment
	 */
	public void setTextAlignment(int textAlignment) {
		this.textAlignment = textAlignment;
	}

	/**
	 * Returns if a visualization of a time line is part of the output.
	 * 
	 * @return {@code true} if a (virtual) timeline is part of the output,
	 * {@code false} otherwise
	 */
	public boolean isShowTimeLine() {
		return showTimeLine;
	}
	
	/**
	 * Sets whether a (virtual) timeline should be part of the output.
	 * 
	 * @param showTimeLine if {@code true} a timeline visualization will be part
	 * of the output
	 */
	public void setShowTimeLine(boolean showTimeLine) {
		this.showTimeLine = showTimeLine;
	}

	/**
	 * Returns whether visual marking of annotation boundaries should be 
	 * applied. 
	 * 
	 * @return {@code true} if annotation boundaries are shown, {@code false} 
	 * otherwise.
	 */
	public boolean isShowAnnotationBoundaries() {
		return showAnnotationBoundaries;
	}

	/**
	 * Sets whether annotation boundaries should be marked in the output.
	 * 
	 * @param showAnnotationBoundaries if {@code true} annotation boundaries
	 * are shown in the output
	 */
	public void setShowAnnotationBoundaries(boolean showAnnotationBoundaries) {
		this.showAnnotationBoundaries = showAnnotationBoundaries;
	}
    
}
