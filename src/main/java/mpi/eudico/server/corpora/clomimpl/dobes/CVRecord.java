package mpi.eudico.server.corpora.clomimpl.dobes;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information needed to construct a ControlledVocabulary object
 * 
 * @author Micha Hulsbosch
 * @version jul 2010
 */
public class CVRecord {
	private String cv_id;
	private String description;		/** for old, single language CVs only.*/
	private String extRefId;
	private ArrayList<CVEntryRecord> entries;
	private List<CVDescriptionRecord> descriptions;
	
	/**
	 * Construct an empty CVRecord, sets cv_id
	 * 
	 * @param cv_id the id
	 */
	public CVRecord(String cv_id) {
		setCv_id(cv_id);
		description = null;
		extRefId = null;
		entries = new ArrayList<CVEntryRecord>();
		descriptions = new ArrayList<CVDescriptionRecord>();
	}
	
	/**
	 * Returns whether this record has either a description or more than one
	 * entry.
	 * 
	 * @return a boolean saying whether this record has content
	 */
	public boolean hasContents() {
		if(description != null && !description.isEmpty()) {
			return true;
		}
		if(entries.size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the CV ID.
	 * 
	 * @return the cv_id
	 */
	public String getCv_id() {
		return cv_id;
	}
	
	/**
	 * Sets the CV ID.
	 * 
	 * @param cvId the cv_id to set
	 */
	public void setCv_id(String cvId) {
		cv_id = cvId;
	}
	
	/**
	 * Returns the single CV description.
	 * 
	 * @return the description, for old, single language CVs only.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * Sets the CV description.
	 * 
	 * @param description the description to set.
	 * For old, single language CVs only.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Returns the external reference ID.
	 * 
	 * @return the external reference ID
	 */
	public String getExtRefId() {
		return extRefId;
	}
	
	/**
	 * Sets the external reference ID.
	 * 
	 * @param extRefId the external reference ID to set
	 */
	public void setExtRefId(String extRefId) {
		this.extRefId = extRefId;
	}
	
	/**
	 * Returns a list of CV entry records.
	 * 
	 * @return the entries
	 */
	public ArrayList<CVEntryRecord> getEntries() {
		return entries;
	}
	
	/**
	 * Sets the list of CV entry records.
	 * 
	 * @param entries the entries to set
	 */
	public void setEntries(ArrayList<CVEntryRecord> entries) {
		this.entries = entries;
	}
	
	/**
	 * Adds an entry record to the list.
	 * 
	 * @param cvEntryRecord the entry record to add
	 */
	public void addEntry(CVEntryRecord cvEntryRecord) {
		entries.add(cvEntryRecord);
	}
	
	/**
	 * Removes an entry record from the list.
	 * 
	 * @param cvEntryRecord the entry record to remove
	 */
	public void removeEntry(CVEntryRecord cvEntryRecord) {
		entries.remove(cvEntryRecord);
	}
	
	/**
	 * Returns a list of description records.
	 * 
	 * @return a list of description records, maybe empty if this is not a
	 * multiple language CV
	 */
	public List<CVDescriptionRecord> getDescriptions() {
		return descriptions;
	}
	
	/**
	 * Adds a description record to the list.
	 * 
	 * @param cvDescriptionRecord the description record to add
	 */
	public void addDescription(CVDescriptionRecord cvDescriptionRecord) {
		descriptions.add(cvDescriptionRecord);
	}
}
