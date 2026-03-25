package mpi.eudico.server.corpora.clomimpl.dobes;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information needed to construct a CVEntry object.
 * 
 * @see mpi.eudico.util.CVEntry
 * 
 * @author Han Sloetjes
 * @version jun 2004
 */
public class CVEntryRecord {
	private String description;
	private String value;
	private String extRefId;
	private String id;
	private List<CVEntryRecord> subEntries;
	private String subEntryLangRef;

	/**
	 * Constructor.
	 */
	public CVEntryRecord() {
		super();
	}

	/**
	 * Returns the description.
	 * @return the description, or {@code null}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the value.
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the description.
	 * @param description the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the value.
	 * @param value the value
	 */
	public void setValue(String value) {
		this.value = value;
	}
	

	/**
	 * Returns the id of an external reference object.
	 * 
	 * @return the extRefId the id of an external reference, e.g. a concept
	 * defined in ISO DCR
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
	 * Returns the ID of the CV entry.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the ID of the CV entry.
	 *  
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/*
	 * The same record is used for old-style one-language vocabularies
	 * and new-style multi-language vocabularies, so that the new parser
	 * can also parse the old files.
	 * This makes the data structure a bit messy.
	 */
	/**
	 * Returns whether this entry is a sub-entry in a multi-language
	 * vocabulary. 
	 * @return {@code true} if this is a sub-entry, {@code false} otherwise 
	 */
	public boolean isSubEntry() {
		return subEntryLangRef != null;
	}
	
	/**
	 * Returns whether this is an entry in a multi-language vocabulary.
	 * 
	 * @return {@code true} if this entry has multiple sub-entries, 
	 * {@code false} otherwise
	 */
	public boolean isMLEntry() {
		return subEntries != null;
	}

	/**
	 * Returns the language identifier of this sub-entry.
	 * 
	 * @return the language identifier or {@code null} if this is not a
	 * sub-entry
	 */
	public String getSubEntryLangRef() {
		return subEntryLangRef;
	}

	/**
	 * Sets the language identifier of this sub-entry.
	 * 
	 * @param subEntryLangRef the language identifier
	 */
	public void setSubEntryLangRef(String subEntryLangRef) {
		this.subEntryLangRef = subEntryLangRef;
	}

	/**
	 * Adds a sub-entry record to this record.
	 * 
	 * @param cve a record which has to represent a sub-entry
	 */
	public void addSubEntry(CVEntryRecord cve) {
		assert(!this.isSubEntry());
		assert(cve.isSubEntry());
		assert(this.description == null);
		assert(this.subEntryLangRef == null);
		assert(this.value == null);
		
		if (subEntries == null) {
			subEntries = new ArrayList<CVEntryRecord>(2);
		}
		subEntries.add(cve);
	}
	
	/**
	 * Returns the list of sub-entry records.
	 * 
	 * @return the list of sub-entry records
	 */
	public List<CVEntryRecord> getSubEntries() {
		return subEntries;
	}
}
