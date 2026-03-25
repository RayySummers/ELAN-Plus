package mpi.search.content.query.model;

import java.util.Objects;

import mpi.search.content.result.model.ContentResult;

/**
 * A constraint for a follow up query on the results of a previous query.
 * This allows to narrow down the results and to stack queries.
 * 
 * Created on Oct 7, 2004
 * @author Alexander Klassmann
 * @version Oct 7, 2004
 */
public class RestrictedAnchorConstraint extends AnchorConstraint {
	final private ContentResult result;
	final private String comment;

	/**
	 * Constructor
	 * @param result the content result, the source for the next query
	 * @param comment a comment string
	 */
	public RestrictedAnchorConstraint(ContentResult result, String comment) {
		super(result.getTierNames());
		this.result = result;
		this.comment = comment;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof RestrictedAnchorConstraint)) {
			return false;
		}
		RestrictedAnchorConstraint other = (RestrictedAnchorConstraint) o;
		
		if ((result == null && other.getResult() != null) || 
				(result != null && other.getResult() == null)) {
			//!result.equals(other.getResult())
			return false;
		}
		
		if ((comment == null && other.getComment() != null) ||
				(comment != null && !comment.equals(other.getComment()))) {
			return false;
		}
		
		return super.equals(other);
		// old implementation
		//return o instanceof RestrictedAnchorConstraint ? super.equals(o) : false;
	}
	

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(result, comment);
	}

	/**
	 * Returns the content result.
	 * 
	 * @return the content result
	 */
	public ContentResult getResult() {
		return result;
	}

	@Override
	public boolean isEditable(){
		return false;
	}
	
	/**
	 * Return the comment.
	 * 
	 * @return the comment string
	 */
	public String getComment() {
		return comment;
	}
}
