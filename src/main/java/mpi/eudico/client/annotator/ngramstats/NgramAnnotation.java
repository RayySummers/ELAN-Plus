package mpi.eudico.client.annotator.ngramstats;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
 * Stores the data from an {@link AbstractAnnotation} so we can mangle it internally.
 * 
 * @author Larwan Berke, DePaul University
 * @version 1.0
 * @since August 2013
 */
public class NgramAnnotation {
	/** the annotation value */
	protected final String value;
	/** the end time */
	protected final Long endTime;
	/** the begin time */
	protected long beginTime; // not final as we might need to fix it!
	
	/** stores whether there is an interval before/after this annotation */
	protected boolean hasBeforeInterval = false, hasAfterInterval = false;
	/** stores the interval duration before/after this annotation */
	protected long beforeInterval, afterInterval;

	/**
	 * Constructor.
	 * 
	 * @param ann the source annotation
	 */
	public NgramAnnotation(AbstractAnnotation ann) {
		value = ann.getValue();
		beginTime = ann.getBeginTimeBoundary();
		endTime = ann.getEndTimeBoundary();
	}

	@Override
	public String toString() {
		StringBuilder rv = new StringBuilder();
		rv.append("Annotation(" + value + ")[" + this.hashCode() + "]");
		rv.append("\n\t beginTime= " + beginTime);
		rv.append("\n\t endTime= " + endTime);
		rv.append("\n\t beforeInterval= " + ( hasBeforeInterval ? beforeInterval : "NONE" ));
		rv.append("\n\t afterInterval= " + ( hasAfterInterval ? afterInterval : "NONE" ));
		return rv.toString();
	}
}