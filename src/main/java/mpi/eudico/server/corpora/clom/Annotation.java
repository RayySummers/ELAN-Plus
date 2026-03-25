package mpi.eudico.server.corpora.clom;

import java.util.List;

import mpi.eudico.server.corpora.event.ParentAnnotationListener;
import mpi.eudico.server.corpora.util.ACMEditableObject;

/**
 * Interface for an annotation declaring methods for changing the value, 
 * getting the tier, getting/setting the id, a reference to a CVEntry, 
 * the parent annotation and child annotations on a specific tier. 
 *
 * The "content" or value of an annotation in the ACM model is a single 
 * String object, i.e. Unicode text, unlike some other tools or models
 * in which an annotation can consist of complex data structures, a
 * feature vector etc.
 */
public interface Annotation
	extends AnnotationCore, Comparable<Annotation>, ACMEditableObject, 
	ParentAnnotationListener, ParentAnnotation {

	/**
	 * Sets the textual value of the annotation.
	 * 
	 * @param theValue the value of the annotation
	 */
	public void setValue(String theValue);
	
	/**
	 * Updates the textual value of the annotation.
	 * 
	 * @param theValue the new value of the annotation
	 */
	public void updateValue(String theValue);
	
	/**
	 * Returns the tier this annotation is part of.
	 * 
	 * @return the tier this annotation is part of
	 */
	public Tier getTier();
	
	/**
	 * Marks this annotation as being 'deleted'.
	 * 
	 * @param deleted the new flag for the 'deleted' state of the annotation
	 */
	public void markDeleted(boolean deleted);
	
	/** 
	 * Returns the 'deleted' flag.
	 * 
	 * @return the value of the 'deleted' flag. If true the annotation has 
	 * been or is in the process of being deleted
	 */
	public boolean isMarkedDeleted();

	/**
	 * An annotation can have dependent annotations on multiple tiers. 
	 * This method returns the child annotations belonging to the specified tier.
	 * 
	 * @param tier the tier to get the child annotations of this annotation for
	 * @return a list of child annotations
	 */
	public List<Annotation> getChildrenOnTier(Tier tier);

	/**
	* Checks if this Annotation has a parent Annotation.
	* 
	* @return if true, there is a parent annotation, 
	*/
	public boolean hasParentAnnotation();

	/**
	 * Returns this Annotation's parent Annotation.
	 * 
	 * @return the parent annotation or null
	 */
	public Annotation getParentAnnotation();
	
	/**
	 * Returns the id of the annotation
	 * @return the id of the annotation
	 */
	public String getId();
	
	/**
	 * Sets the id of the annotation
	 * @param s the id string for the annotation
	 */
	public void setId(String s);
	
	// By Micha:
	/**
	 * Returns the id of a CVEntry if this annotation is associated with one
	 * 
	 * @return the id of a CVEntry or null
	 */
	public String getCVEntryId();
	
	/**
	 * Sets the id of a CVEntry
	 * 
	 * @param cVEntryId the CVEntry id
	 */
	public void setCVEntryId(String cVEntryId);

}
