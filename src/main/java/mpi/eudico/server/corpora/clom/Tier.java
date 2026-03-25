package mpi.eudico.server.corpora.clom;

import java.util.List;

import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.util.ACMEditableObject;


/**
 * A Tier represents a layer or 'stream' of sequential events/phenomena.
 * Transcriptions can contain multiple Tiers. Tiers can contain Annotations, 
 * Annotations have a label and can possibly refer to a time interval.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 1-Jun-1999
 * @version Aug 2005 Identity removed
 * @version April 2015 several methods moved from TierImpl to this interface
 */
public interface Tier extends ACMEditableObject {
	
	/**
	 * Returns the transcription.
	 * <br>
	 * Version: Dec 2012 introduced<br>
	 * Version: April 2015 getParent renamed to getTranscription
	 * @return the transcription this tier is part of
	 */
	public Transcription getTranscription();
	
    /**
     * Returns the Tier's name.
     *
     * @return the name of the Tier
     * @see #setName(String)
     */
    public String getName();

    /**
     * Sets the tier's name, must be unique in the collection of tier names.
     * 
     * Note: up to now the name is also the id of a tier, so changing the name
     * is also changing the id of the tier.
     * 
     * @param theName the new name of the tier
     */
    public void setName(String theName);

    /**
     * Returns the id of an {@link ExternalReference}.
     * 
     * @return the id of an {@link ExternalReference} or null
     */
	public String getExtRef();
	
	/**
	 * Links this tier to an external reference.
	 * 
	 * @param extRef the id of an {@link ExternalReference}
	 */
	public void setExtRef(String extRef);

	/**
	 * Returns a content language reference.
	 * 
	 * @return the id of a content language object
	 */
	public String getLangRef();
	
	/**
	 * Links this tier to a language identification object specifying the 
	 * (main) content language of this tier.
	 * 
	 * @param langRef the id of a content language object
	 */
	public void setLangRef(String langRef);
	
	/**
	 * Returns a list of annotations belonging to this tier.
	 * 
	 * @return the list of annotation that have been added to this tier and
	 * are considered to belong to this tier
	 */
	public List<? extends Annotation> getAnnotations();
	
	/** 
	 * Returns the number of annotations of this tier.
	 * 
	 * @return the number of annotations on this tier
	 */
	public int getNumberOfAnnotations();
	
	/**
	 * When (or before) an annotation is added to a tier, conflicts may have
	 * to be resolved in line with the constraints that apply to the tier.
	 * A basic constraint is that annotations on the same tier are not allowed
	 * to overlap.
	 * 
	 * @param theAnnotation the annotation to add to this tier
	 */
	public void addAnnotation(Annotation theAnnotation);
	
	/**
	 * When an annotation is removed, all depending annotations are removed 
	 * from their tier. 
	 *  
	 * @param theAnnotation the annotation to remove 
	 */
	public void removeAnnotation(Annotation theAnnotation);
	
	/**
	 * Shorthand to remove all annotation from this tier.
	 */
	public void removeAllAnnotations();
	
	/**
	 * The {@code LinguisticType} (or Tier Type) contains the {@link Constraint}
	 * object determining the "stereotype" of a tier. Changing the type might 
	 * require removal of all annotations and /or a change of the tier hierarchy.
	 * 
	 * @param theType the linguistic or tier type of the tier, not null
	 */
	public void setLinguisticType(LinguisticType theType);
	
	/**
	 * Returns the {@code LinguisticType} of this tier.
	 * 
	 * @return the linguistic or tier type of the tier, not null
	 * @see #setLinguisticType(LinguisticType)
	 */
	public LinguisticType getLinguisticType();
	
	/**
	 * Sets the {@code annotator} attribute.
	 * 
	 * @param annotator name or code of the annotator who or that produced the 
	 * annotations on this tier
	 */
	public void setAnnotator(String annotator);
	
	/**
	 * Returns the {@code annotator} attribute.
	 * 
	 * @return the annotator of this tier
	 * @see #setAnnotator(String)
	 */
	public String getAnnotator();
	
	/** 
	 * Sets the {@code participant} attribute of this tier.
	 * 
	 * @param theParticipant the subject or participant in the recording the 
	 * annotations on this tier are about
	 */
	public void setParticipant(String theParticipant);
	
	/**
	 * Returns the {@code participant} attribute of this tier.
	 * 
	 * @return the participant
	 */
	public String getParticipant();
	
	// Tier hierarchy
	/**
	 * Sets the parent tier for this tier. Hierarchical relations should be in
	 * line with the constraints defined for both tiers in their tier type.
	 * Existing annotations on this tier might need to be removed.
	 * 
	 * @param newParent the new parent tier for this tier.
	 */
	public void setParentTier(Tier newParent);
	
	/**
	 * Returns the parent tier or {@code null} if this is a top level tier.
	 * 
	 * @return the parent tier or null
	 */
	public Tier getParentTier();
	
	/**
	 * Returns whether this tier has a parent tier.
	 * 
	 * @return {@code true} if this tier has a parent tier, {@code false} if 
	 * the parent tier is {@code null}
	 */
	public boolean hasParentTier();
	
	/** 
	 * Returns the top level or root tier of the hierarchy this tier is part of.
	 * 
	 * @return the top level ancestor tier, the root of the tier tree this tier
	 * is part of 
	 */
	public Tier getRootTier();
	
	/**
	 * Returns whether the specified tier is an ancestor of this tier.
	 * 
	 * @param ancestor the tier to test
	 * @return {@code true} if the specified tier is an ancestor of this tier,
	 * {@code false} otherwise
	 */
	public boolean hasAncestor(Tier ancestor);
	
	/**
	 * Returns a list of all direct dependent tiers of this tier (one level).
	 * 
	 * @return a list of all direct children of this tier. 
	 */
	public List<? extends Tier> getChildTiers();
	
	/**
	 * Returns a list of all direct and recursive dependent tiers.
	 * 
	 * @return a list of all direct and indirect children of this tier.
	 */
	public List<? extends Tier> getDependentTiers();

}
