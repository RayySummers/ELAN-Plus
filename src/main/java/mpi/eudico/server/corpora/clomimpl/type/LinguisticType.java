package mpi.eudico.server.corpora.clomimpl.type;

import java.util.Objects;

import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;

/**
 * A class to store several properties that tiers of that type have in common.
 * In user interfaces etc. now referred to as "Tier Type". 
 */
public class LinguisticType {
	/** an enumeration of property keys */
	public enum PropKey {
		/** the ID */
		ID,
		/** the name */
		NAME,
		/** the constraint */
		CONSTRAINT,
		/** a CV name */
		CV_NAME,
		/** a data category */
		DC,
		/** a lexicon bundle */
		LEX_BUNDLE,
		/** a lexicon link */
		LEX_LINK,
		/** a lexical field id */
		LEX_FIELD
	};
    /** the name of the type, also the id */
    String typeName;

    /** the constraint instance, possibly composite */
    Constraint constraints; // can be composite

    /** the time alignable flag, true by default */
    boolean timeAlignable = true;

    /* HS: added jun 04 support for controlled vocabularies */
    /** the identifier of the Controlled Vocabulary in use by this tier type */
    String controlledVocabularyName;
    
    /*HS: added april 08 support for reference to a ISO Data Category. Initially a string,
     * might need an Object later */
    /** a reference to a (ISO) Data Category. Can be a simple id (when it is a category from the ISO DCR)
     * or a combination of DCR identifier + id, e.g. ISO12620#32 */
    String dataCategory;
    
	/**
	 * Holds a lexicon connection
	 */
	LexiconQueryBundle2 lexiconQueryBundle = null;

    /**
     * Creates a new LinguisticType instance
     *
     * @param theName the name of the type
     */
    public LinguisticType(String theName) {
        typeName = theName;
    }
    
    /**
     * Duplicates a linguistic type instance using a new name.
     * Note that this does not clone the CV if that's desired.
     * 
     * @param theName the (new) name of the type
     * @param orig the type to copy
     */
    public LinguisticType(String theName, LinguisticType orig) {
    	this(theName);
    	if (orig.hasConstraints()) {
    		try {
    			addConstraint(orig.getConstraints().clone());
    		}
    		catch(CloneNotSupportedException ex) {
    			ex.printStackTrace(); // can't happen
    		}
    	}
    	setTimeAlignable(orig.isTimeAlignable());
    	// The caller should clone CV but for now, use the same name
    	setControlledVocabularyName(orig.getControlledVocabularyName());
    	setDataCategory(orig.getDataCategory());
    	// add a copy of the lexicon query bundle, if any
    	if (orig.getLexiconQueryBundle() != null) {
	    	LexiconQueryBundle2 copyBundle = new LexiconQueryBundle2(orig.getLexiconQueryBundle());
	    	// the copy constructor does not copy the service client, set it separately
	    	if (copyBundle.getLink() != null) {
	    		copyBundle.getLink().setSrvcClient(orig.getLexiconQueryBundle().getLink().getSrvcClient());
	    	}
	    	setLexiconQueryBundle(copyBundle);
    	}
    }

    /**
     *
     * @return a description string
     */
    @Override
	public String toString() {
        String stereotype = "";

        if (constraints != null) {
            stereotype = Constraint.stereoTypes[constraints.getStereoType()];
        }

        return typeName + ", " + timeAlignable + ", " + stereotype;
    }

    /**
     * Returns the name of the type.
     * 
     * @return the name of the type
     */
    public String getLinguisticTypeName() {
        return typeName;
    }

    /**
     * Sets the name of the type.
     * 
     * @param theName the new name of the type
     */
    public void setLinguisticTypeName(String theName) {
        typeName = theName;
    }

    /**
     * If a type returns false, no constraints, it is or should be used by a
     * root tier.
     *
     * @return true if the type has a {@code Constraint} object set, false 
     * otherwise
     */
    public boolean hasConstraints() {
        if (constraints != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the {@code Constraint} object of this type.
     * 
     * @return the {@code Constraint} object or {@code null}
     */
    public Constraint getConstraints() {
        return constraints;
    }

    /**
     * Adds or sets a {@code Constraint} object.
     * 
     * @param theConstraint the constraint to set or to add to the current constraint
     */
    public void addConstraint(Constraint theConstraint) {
        if (constraints == null) {
            constraints = theConstraint;
        } else {
            constraints.addConstraint(theConstraint);
        }
    }

    /**
     * Removes all constraints, sets it to {@code null}.
     */
    public void removeConstraints() {
        constraints = null;
    }

    /**
     * Returns whether this type is time-alignable.
     * 
     * @return true if this type is time alignable, false otherwise, 
     * should be consistent with the constraints
     */
    public boolean isTimeAlignable() {
        return timeAlignable;
    }

    /**
     * Sets the time alignable flag. This flag should in fact be derived
     * from the constraint object but doesn't currently.
     * 
     * @param isTimeAlignable the new time alignable flag
     */
    public void setTimeAlignable(boolean isTimeAlignable) {
        timeAlignable = isTimeAlignable;
    }

    /**
     * Returns whether or not annotation values on tiers using this LinguisticType 
     * are restricted by a ControlledVocabulary.<br>
     * Current implementation is very loose; the value returned only depends 
     * on the presence of a non-null reference string. Could be more strict 
     * by keeping a flag independent from a reference string or object reference.
     * 
     * @return true if there is a reference to a ControlledVocabulary
     */
    public boolean isUsingControlledVocabulary() {
    	return (controlledVocabularyName == null || 
    		controlledVocabularyName.length() == 0) ? false : true;
    }
    
    /**
     * Returns the name (identifier) of the ControlledVocabulary in use by 
     * this type.<br>
     * The actual CV objects are stored in and managed by the Transcription.
     * (Candidate for change: might store a reference to the CV object itself 
     * instead of using a reference to it.)
     * 
     * @return the name/identifier of the cv
     */
    public String getControlledVocabularyName() {
    	return controlledVocabularyName;
    }
    
    /**
     * Sets the name of the ControlledVocabulary to be used by this type.
     * 
     * @see #getControlledVocabularyName()
     * @param name the name/identifier of the cv
     */
    public void setControlledVocabularyName(String name) {
    	controlledVocabularyName = name;
    }
    
    /**
     * Returns the Data Category reference (the identifier).
     * 
     * @return the Data Category reference
     */
	public String getDataCategory() {
		return dataCategory;
	}
	
	/**
	 * Sets the Data Category reference.
	 * 
	 * @param dataCategory the identifier of the data category
	 */
	public void setDataCategory(String dataCategory) {
		this.dataCategory = dataCategory;
	}
	
	/**
	 * Returns whether this type has a connection to a lexicon service.
	 * <br>
	 * Added by: Micha Hulsbosch
	 * @return true if there is a reference to lexicon service 
	 */
	public boolean isUsingLexiconQueryBundle() {
		return (lexiconQueryBundle == null) ? false : true;
	}
	
	/**
	 * Returns an information structure with a connection to a lexicon service.
	 * <br>
	 * Added by: Micha Hulsbosch
	 * @return the lexicon query bundle
	 */
	public LexiconQueryBundle2 getLexiconQueryBundle() {
		return lexiconQueryBundle;
	}

	/**
	 * Sets a lexicon query bundle with a connection to a lexicon service.
	 * <br>
	 * Added by: Micha Hulsbosch
	 * @param lexiconQueryBundle the lexicon query bundle
	 */
	public void setLexiconQueryBundle(LexiconQueryBundle2 lexiconQueryBundle) {
		this.lexiconQueryBundle = lexiconQueryBundle;
	}
    
	
	/**
	 * Overrides <code>Object</code>'s equals method by checking all  fields of
	 * the other object to be equal to all fields in this  object.
	 *
	 * @param obj the reference object with which to compare
	 *
	 * @return true if this object is the same as the obj argument; false
	 *         otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof LinguisticType)) {
			// it should be a LinguisticType object
			return false;
		}
		LinguisticType other = (LinguisticType) obj;
		if (constraints == null) {
			if (other.constraints != null) {
				return false;
			}
		} else if (!constraints.equals(other.constraints)) {
			return false;
		}
		if (controlledVocabularyName == null) {
			if (other.controlledVocabularyName != null) {
				return false;
			}
		} else if (!controlledVocabularyName.equals(other.controlledVocabularyName)) {
			return false;
		}
		if (dataCategory == null) {
			if (other.dataCategory != null) {
				return false;
			}
		} else if (!dataCategory.equals(other.dataCategory)) {
			return false;
		}
		if (lexiconQueryBundle == null) {
			if (other.lexiconQueryBundle != null) {
				return false;
			}
		} else if (!lexiconQueryBundle.equals(other.lexiconQueryBundle)) {
			return false;
		}
		if (timeAlignable != other.timeAlignable) {
			return false;
		}
		if (typeName == null) {
			if (other.typeName != null) {
				return false;
			}
		} else if (!typeName.equals(other.typeName)) {
			return false;
		}
		return true;
	}

	/**
	 * Includes all members that are checked in equals() (which are all mutable)
	 * in the calculation of a hash code.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(typeName, constraints, controlledVocabularyName, dataCategory, 
				lexiconQueryBundle, timeAlignable);
	}
	
}
