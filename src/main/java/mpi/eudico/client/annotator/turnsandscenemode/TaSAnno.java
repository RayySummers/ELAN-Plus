package mpi.eudico.client.annotator.turnsandscenemode;

import mpi.eudico.server.corpora.clom.Annotation;

/**
 * A wrapper class for a real or a "virtual" annotation in a table- or list model. 
 * A virtual annotation represents a gap, an empty space between annotations, a space
 * where annotations can be created.
 * 
 * @author Han Sloetjes
 */
public class TaSAnno {
	private Annotation ann;
	private String participant;
	private long bt;
	private long et;
	private String curValue;
	
	/**
	 * Creates a wrapper instance for an actual annotation.
	 * 
	 * @param annotation the annotation
	 */
	public TaSAnno(Annotation annotation) {
		ann = annotation;
		setParticipant(annotation);
	}
	
	/**
	 * Creates a wrapper for a gap on the time line.
	 * 
	 * @param begin the gap's begin time
	 * @param end the gap's end time
	 */
	public TaSAnno(long begin, long end) {
		bt = begin;
		et = end;
		setParticipant(null);
	}

	/**
	 * Sets the participant based on the tier of the specified annotation
	 * or to the empty string.
	 * 
	 * @param annotation an annotation or {@code null}
	 */
	private void setParticipant(Annotation annotation) {
		if (annotation != null) {
			participant = annotation.getTier().getParticipant();
		} else {
			participant = "";
		}
	}
	
	/**
	 * Returns the participant string.
	 * 
	 * @return the participant
	 */
	public String getParticipant() {
		return participant;
	}
	
	/**
	 * Returns the wrapped annotation.
	 * 
	 * @return the actual annotation or {@code null}
	 */
	public Annotation getAnnotation() {
		return ann;
	}

	/**
	 * Sets the annotation for this wrapper and updates the participant.
	 * 
	 * @param ann the annotation to wrap
	 */
	public void setAnnotation(Annotation ann) {
		this.ann = ann;
		setParticipant(ann);
	}

	/**
	 * Returns the begin time of this wrapper annotation.
	 * 
	 * @return the begin time of the annotation or of the gap
	 */
	public long getBeginTime() {
		if (ann != null) {
			return ann.getBeginTimeBoundary();
		}
		return bt;
	}

	/**
	 * Sets the begin time or this gap.
	 * 
	 * @param bt the begin time for this gap
	 */
	public void setBeginTime(long bt) {
		this.bt = bt;
	}

	/**
	 * Returns the end time of the annotation or of this gap.
	 * 
	 * @return the end time of the annotation, or of this gap
	 */
	public long getEndTime() {
		if (ann != null) {
			return ann.getEndTimeBoundary();
		}
		return et;
	}

	/**
	 * Sets the end time of this gap.
	 * 
	 * @param et the end time
	 */
	public void setEndTime(long et) {
		this.et = et;
	}
	
	/**
	 * Returns the text of the annotation, if there, or the current text value
	 * entered for this gap.
	 * 
	 * @return the text of this wrapper annotation
	 */
	public String getText() {
		if (ann != null) {
			return ann.getValue();
		}
		else if (curValue != null) {
			return curValue;
		}
		
		return "";
	}
	
	/**
	 * This can or could be used to store text that has been entered in a segment 
	 * where there is no annotation yet.
	 * 
	 * @param text the entered text
	 */
	public void setText(String text) {
		curValue = text;
	}
}