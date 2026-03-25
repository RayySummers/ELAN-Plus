package mpi.eudico.server.corpora.clom;

/**
 * Search results may arise from outside the ACM model.
 * For this purpose it is important to have a "minimalist" annotation,
 * containing only time information and value, but no hierachical structure
 * 
 * Created on Jul 23, 2004
 * @author Alexander Klassmann
 * @version Jul 23, 2004
 */
public interface AnnotationCore {
	/**
	 * Only subclass AlignableAnnotation (ALA) has a getBegin() method, 
	 * which returns the begin time in milliseconds.
	 * RefAnnotions (REA) don't have a begin time. REA still need something 
	 * like a begin time, because they have to be drawn somewhere on screen. 
	 * This time is not the begin time but the begin time boundary.
	 * Note 1:<br>
	 * The begin time of the parent/root AlignableAnnotation cannot always be
	 * used for the RefAnnotation, e.g. if two REA's have the same parent ALA,
	 * the second REA starts in the middle of the ALA. <br>
	 * Note 2:<br>
	 * For an ALA, which begin timeslot is timealigned, 
	 * getBegin() and getBeginTimeBoundary() are identical.
	 * 
	 * @return the begin time
	 * */	
	public long getBeginTimeBoundary();
	
	/**
	 * Returns the end time of an annotation.
	 * 
	 * @return the end time
	 * @see #getBeginTimeBoundary()
	 * */	
	public long getEndTimeBoundary();
	
	/**
	 * Returns the value of the annotation.
	 * 
	 * @return the textual value of the annotation
	 */
	public String getValue();

}
