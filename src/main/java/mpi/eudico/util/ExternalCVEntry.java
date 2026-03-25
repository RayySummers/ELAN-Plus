package mpi.eudico.util;

/**
 * This class no longer adds any fields to CVEntry.
 * Consider removing it if this is likely to remain so.
 * 
 * @author olasei
 */
@SuppressWarnings("serial")
public class ExternalCVEntry extends CVEntry {
	/**
	 * Creates a new entry instance.
	 * 
	 * @param parent the vocabulary this entry is part of
	 * @param content the content or value of the entry
	 * @param desc the description of the entry
	 * @param id the id of the entry
	 */
	public ExternalCVEntry(BasicControlledVocabulary parent, String content, String desc, String id) {
		super(parent, content, desc);
		setId(id);
	}
	
	/**
	 * Creates a new entry instance.
	 * 
	 * @param parent the vocabulary this entry is part of
	 */
	public ExternalCVEntry(BasicControlledVocabulary parent) {
		super(parent);
	}
	
	/**
	 * Creates a new entry instance, a deep copy the specified external entry.
	 * 
	 * @param parent the vocabulary this entry is part of
	 * @param orig the entry to copy
	 */
	public ExternalCVEntry(BasicControlledVocabulary parent, ExternalCVEntry orig) {
		super(parent, orig);
	}
	
	/**
	 * Creates a new entry instance, a deep copy the specified entry.
	 * @param parent the vocabulary this entry is part of
	 * @param orig the entry to copy
	 */
	public ExternalCVEntry(BasicControlledVocabulary parent, CVEntry orig) {
		super(parent, orig);
	}	
}
