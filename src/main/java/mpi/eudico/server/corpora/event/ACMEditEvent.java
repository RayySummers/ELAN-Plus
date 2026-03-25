package mpi.eudico.server.corpora.event;

import java.util.EventObject;


/**
 * Class for a range of ACM edit events. Listeners can check which object was
 * modified and what the nature of the modification was. There are 3 fields 
 * that can be examined to determine what happened:
 * <ul>
 * <li>the {@code invalidatedObject} or {@code source} of the event. E.g. if a 
 * tier was added to the transcription, the transcription is the 
 * {@code invalidatedObject}
 * <li>the {@code operation}, one of the operation constants of this class, 
 * e.g. {@link #ADD_TIER}, an indication of the type of edit that occurred
 * <li> the {@code modification}, the object that is the change, e.g. if a tier
 * was added to the transcription, the tier is the modification, if an 
 * annotation is added to a tier, the annotation is the modification, the tier
 * the invalidated object. Sometimes source and modification are the same object.
 * </ul>
 */
@SuppressWarnings("serial")
public class ACMEditEvent extends EventObject {
    // operation constants

    /** Constant for the Add Tier operation */
    public static final int ADD_TIER = 0;

    /** Constant for the Remove Tier operation */
    public static final int REMOVE_TIER = 1;

    /** Constant for the Change Tier operation. This can indicate a number
     * of changes, e.g. the value of one or more properties were modified */
    public static final int CHANGE_TIER = 2;

    /** Constant for the Add Annotation operation. "Here" depends e.g. on
     * selected tier and selected time interval */
    public static final int ADD_ANNOTATION_HERE = 3;

    /** Constant for the Add Annotation Before operation. "Before" depends on
     * and refers to a selected annotation */
    public static final int ADD_ANNOTATION_BEFORE = 4;

    /** Constant for the Add Annotation After operation */
    public static final int ADD_ANNOTATION_AFTER = 5;

    /** Constant for the Remove Annotation operation */
    public static final int REMOVE_ANNOTATION = 6;

    /** Constant for the Change Annotation Time operation */
    public static final int CHANGE_ANNOTATION_TIME = 7;

    /** Constant for the Change Annotation Value operation */
    public static final int CHANGE_ANNOTATION_VALUE = 8;

    /** Constant for the Add Linguistic Type operation (also referred to as 
     * Tier Type) */
    public static final int ADD_LINGUISTIC_TYPE = 9;

    /** Constant for the Remove Linguistic Type (Tier Type) operation */
    public static final int REMOVE_LINGUISTIC_TYPE = 10;

    /** Constant for the Change Linguistic Type (Tier Type) operation. 
     * This can indicate a number of changes e.g. changes of property values.*/
    public static final int CHANGE_LINGUISTIC_TYPE = 11;

    /** Constant for the Change Annotation Graphics operation. 
     * Currently not in use (but might become relevant again). */
    public static final int CHANGE_ANNOTATION_GRAPHICS = 12;
    
	/** Constant for the Change Controlled Vocabulary operation. 
	 * Indicates any change in a ControlledVocabulary. */
	public static final int CHANGE_CONTROLLED_VOCABULARY = 13;
	
	/** Constant for the Change Annotations operation. Can indicate any number 
	 * of changes in an unspecified number of annotations on an unspecified
	 * number of tiers. */
	public static final int CHANGE_ANNOTATIONS = 14;
	
	/** Constant for the Change External Reference operation. Indicates any 
	 * change in the external reference(s) from an annotation. */
	public static final int CHANGE_ANNOTATION_EXTERNAL_REFERENCE = 15;

	/** Constant for the Add Lexicon Query Bundle operation.  */
	public static final int ADD_LEXICON_QUERY_BUNDLE = 16;

	/** Constant for the Change Lexicon Query Bundle operation */
	public static final int CHANGE_LEXICON_QUERY_BUNDLE = 17;

	/** Constant for the Delete Lexicon Link operation. */
	public static final int DELETE_LEXICON_LINK = 18;

	/** Constant for the Add Link to Lexicon operation. */
	public static final int ADD_LEXICON_LINK = 19;
	
	/** Constant for the Add Comment operation. */
	public static final int ADD_COMMENT = 20;
	
	/** Constant for the Remove Comment operation. */
	public static final int REMOVE_COMMENT = 21;
	
	/** Constant for the Change Comment operation. */
	public static final int CHANGE_COMMENT = 22;
	
	/** Constant for the Add Reference Link operation. */
	public static final int ADD_REFERENCE_LINK = 23;
	
	/** Constant for the Remove Reference Link operation. */
	public static final int REMOVE_REFERENCE_LINK = 24;
	
	/** Constant for the Change Reference Link operation. */
	public static final int CHANGE_REFERENCE_LINK = 25;
	
	/** Constant for the Add Reference Link Set operation. */
	public static final int ADD_REFERENCE_LINK_SET = 26;
	
	/** Constant for the Remove Reference Link Set operation. */
	public static final int REMOVE_REFERENCE_LINK_SET = 27;
	
    // members
	/** the edit operation */
    private int operation;
    /** the modification */
    private Object modification;

    // constructor
    /**
     * Constructor.
     * 
     * @param invalidatedObject the source of the event
     * @param theOperation one of the {@code ACMEditEvent} operation constants
     * @param theModification the object that is the modification or is the 
     * modified object
     */
    public ACMEditEvent(Object invalidatedObject, int theOperation,
        Object theModification) {
        super(invalidatedObject);

        operation = theOperation;
        modification = theModification;
    }

    // methods

    /**
     * Returns the Object that is invalidated by the ACM edit operation.
     *
     * @return the {@code source} of the event, the object that was invalidated
     * by the ACM edit operation
     */
    public Object getInvalidatedObject() {
        return getSource();
    }

    /**
     * Return an integer constant indicating the nature of the ACM edit
     * operation. These constants are defined by this class itself.
     *
     * @return the operation of the event
     */
    public int getOperation() {
        return operation;
    }

    /**
     * Returns the object that modifies the invalidated Object. Example: in
     * case of adding an annotation to a tier, the tier is invalidated, and
     * the modification is the added annotation.
     *
     * @return the modification itself, maybe null in some cases
     */
    public Object getModification() {
        return modification;
    }
}
