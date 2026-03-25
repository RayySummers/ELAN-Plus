/*
 * Created on Jun 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mpi.eudico.server.corpora.clomimpl.dobes;

import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * A record to store properties of a {@link LinguisticType} object.
 * The concept of {@link LinguisticType} has been renamed to {@code tier type}
 * in the user interface.
 * 
 * @author hennie
 */
public class LingTypeRecord {
	/** constant for the alignable annotation type */
	public static final String ALIGNABLE = "alignable";
	/** constant for the reference annotation type */
	public static final String REFERENCE = "reference";
	
	private String lingTypeId;
	private String timeAlignable;
	private String stereotype;
	private String controlledVocabulary;
	private String extRefId;
	private String lexiconService;
	
	/**
	 * Creates a linguistic type record.
	 */
	public LingTypeRecord() {
		super();
	}

	/**
	 * Returns the id of the linguistic type.
	 * 
	 * @return the id of the linguistic type
	 */
	public String getLingTypeId() {
		return lingTypeId;
	}
	
	/**
	 * Sets the id of the linguistic type.
	 *  
	 * @param lingTypeId the id of the linguistic type
	 */
	public void setLingTypeId(String lingTypeId) {
		this.lingTypeId = lingTypeId;
	}
	
	/**
	 * Returns whether this type is time alignable or not.
	 * 
	 * @return {@code true} if this type is time alignable, {@code false} 
	 * otherwise
	 */
	public String getTimeAlignable() {
		return timeAlignable;
	}
	
	/**
	 * Sets whether this type is time alignable or not.
	 *  
	 * @param timeAlignable if {@code true} this type is time alignable
	 */
	public void setTimeAlignable(String timeAlignable) {
		this.timeAlignable = timeAlignable;
	}
	
	/**
	 * Returns the stereotype string of this type.
	 * 
	 * @return the stereotype string
	 */
	public String getStereoType() {
		return stereotype;
	}
	
	/**
	 * Sets the stereotype of this type.
	 * 
	 * @param stereotype the stereotype string
	 */
	public void setStereoType(String stereotype) {
		this.stereotype = stereotype;
	}
	
	
	/**
	 * The name of the Controlled Vocabulary to be used by this type.
	 * 
	 * @return name of the Controlled Vocabulary to be used by this type
	 */
	public String getControlledVocabulary() {
		return controlledVocabulary;
	}

	/**
	 * Sets the name of the Controlled Vocabulary to be used by this type.
	 * 
	 * @param name he name of the Controlled Vocabulary to be used by this type
	 */
	public void setControlledVocabulary(String name) {
		controlledVocabulary = name;
	}

	/**
	 * Returns the id of an external reference object
	 * 
	 * @return the extRefId the id of an external reference, e.g. a concept defined in ISO DCR
	 */
	public String getExtRefId() {
		return extRefId;
	}

	/**
	 * Sets the external reference id.
	 * 
	 * @param extRefId the extRefId to set
	 */
	public void setExtRefId(String extRefId) {
		this.extRefId = extRefId;
	}
	
	/**
	 * Sets the reference to a lexicon id.
	 * 
	 * @param name name of the lexicon
	 */
	public void setLexiconReference(String name) {
		this.lexiconService = name;
	}

	/**
	 * Returns the reference to a lexicon id.
	 * 
	 * @return the name of the lexicon
	 */
	public String getLexiconReference() {
		return lexiconService;
	}

}
