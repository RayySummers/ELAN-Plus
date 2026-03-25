package mpi.eudico.client.annotator.util;

import mpi.eudico.server.corpora.clom.Annotation;

import java.awt.*;



/**
 * Stores an Annotation and some additional display specific information.
 *
 * @author Han Sloetjes
 * @version 0.2 17/9/2003
 * @version 0.3 13/9/2004
 * @version 0.4 Dec 2009 added a boolean member for truncation and a color member
 * @version 0.5 July 2021 begin and end time are now cached for performance 
 *          reasons. If the alignment changes either a new Tag2D has to be
 *          created or the cached values need to be updated.
 */
public class Tag2D {
    private Annotation annotation;
    // cache begin and end time
    private long bt = -1, et = -1;
    /* the Tier2D this Tag2D belongs to */
    private Tier2D tier2d;
    private String truncatedValue;
    private int x;
    private int width;
    private boolean isTruncated = false;
    private Color color;


    /**
     * Constructor; a Tag2D does not exist without a annotation object.<br>
     *
     * @param annotation the Annotation
     */
    public Tag2D(Annotation annotation) {
        this.annotation = annotation;
        if (annotation != null) {
        	bt = annotation.getBeginTimeBoundary();
        	et = annotation.getEndTimeBoundary();
        }
    }

    /**
     * Returns the Annotation.
     *
     * @return the Annotation
     */
    public Annotation getAnnotation() {
        return annotation;
    }

    /**
     * Sets the Tier2D.
     *
     * @param tier2d the Tier2D
     */
    public void setTier2D(Tier2D tier2d) {
        this.tier2d = tier2d;
    }

    /**
     * Returns the Tier2D.
     * 
     * @return the Tier2D
     */
    public Tier2D getTier2D() {
        return tier2d;
    }
	
    /**
     * Returns the unmodified value of the enclosed Annotation.<br>
     *
     * @return the value of the Annotation
     *
     * @see #getTruncatedValue
     */
    public String getValue() {
    	if (annotation != null) {
			return annotation.getValue();
    	} else  {
    		return null;
    	}        
    }

    /**
     * Returns the truncated value of the enclosed Tag.<br>
     * The length of the truncated string depends on the width  that is
     * available for this Tag2D.
     *
     * @return the truncated value
     */
    public String getTruncatedValue() {
    	if (truncatedValue != null) {
			return truncatedValue;
    	} else {
    		return "";
    	}        
    }

    /**
     * Sets the truncated value.
     *
     * @param truncatedValue the truncated value
     */
    public void setTruncatedValue(String truncatedValue) {
        this.truncatedValue = truncatedValue;
        
    	if (annotation == null || truncatedValue == null || annotation.getValue() == null) {
    		isTruncated = false;
    	} else {
    		isTruncated = (truncatedValue.length() < annotation.getValue().length());
    	}
    }

    /**
     * Returns the begin time of the Annotation.
     * The cached value is returned, assuming that when the times of an
     * annotation change, a new Tag2D is created.
     *
     * @return the begin time of the Annotation
     */
    public long getBeginTime() {
    	if (annotation != null) {
    		if (bt > -1) {
    			return bt;
    		}
			return annotation.getBeginTimeBoundary();
    	} else {
    		return 0;
    	}        
    }

    /**
     * Returns the end time of the Annotation.
     * The cached value is returned, assuming that when the times of an
     * annotation change, a new Tag2D is created.
     * 
     * @return the end time of the Annotation
     */
    public long getEndTime() {
    	if (annotation != null) {
    		if (et > -1) {
    			return et;
    		}
			return annotation.getEndTimeBoundary();
    	} else {
    		return 0;
    	}
        
    }
    
    /**
     * Notification of a possible change in the annotation begin and/or end
     * time. Cached values need to be updated.
     */
    public void annotationTimesChanged() {
    	if (annotation != null) {
    		bt = annotation.getBeginTimeBoundary();
    		et = annotation.getEndTimeBoundary();
    	}
    }

    /**
     * Sets the current width in pixels.
     *
     * @param width the current width in pixels
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns the current width in pixels.
     *
     * @return the current width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the x coordinate for this annotation.
     *
     * @param x the x coordinate for this annotation
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Returns the x coordinate of this annotation.
     *
     * @return the x coordinate of this annotation
     */
    public int getX() {
        return x;
    }
    
    /**
     * Returns whether the text is truncated to fit in available rendering
     * space.
     * 
     * @return {@code true} if a truncated version of the value has been set,
     * {@code false} otherwise  
     */
    public boolean isTruncated() {
    	return isTruncated;
    }
    
    /**
     * Returns the preferred display color, or {@code null}.
     * 
     * @return the color
     */
    public Color getColor() {
    	return color;
    }
    
    /**
     * Sets the preferred color for this annotation.
     * 
     * @param color the preferred color
     */
    public void setColor (Color color) {
    	this.color = color;
    }
}
