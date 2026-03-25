package mpi.eudico.server.corpora.clomimpl.reflink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.RefLink;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Corresponds to the parse result of an RefLinkSet.
 * Also works as a factory.
 * 
 * @author olasei
 */
public class RefLinkSetRecord {
	// Attributes
	private String linksID;
	private String linksName;
	private String extRefID;
	private String langRef;
	private String cvRef;
	
	// Content
	private List<AbstractRefLinkRecord> refLinks;
	
	/**
	 * Construct the record, ready for use.
	 */
	public RefLinkSetRecord() {
		refLinks = new ArrayList<AbstractRefLinkRecord>();
	}

	/**
	 * Returns the ID of the links set.
	 * 
	 * @return the links ID
	 */
	public String getLinksID() {
		return linksID;
	}

	/**
	 * Sets the ID of the links set.
	 * 
	 * @param linksID the links set ID to set
	 */
	public void setLinksID(String linksID) {
		this.linksID = linksID;
	}

	/**
	 * Returns the name of the links set.
	 * 
	 * @return the linksName
	 */
	public String getLinksName() {
		return linksName;
	}

	/**
	 * Sets the name of the links set.
	 * 
	 * @param linksName the links name to set
	 */
	public void setLinksName(String linksName) {
		this.linksName = linksName;
	}

	/**
	 * Returns the reference to an external resource.
	 * 
	 * @return the external RefID
	 */
	public String getExtRefID() {
		return extRefID;
	}

	/**
	 * Sets the reference to an external resource.
	 * 
	 * @param extRefID the external reference ID to set
	 */
	public void setExtRefID(String extRefID) {
		this.extRefID = extRefID;
	}

	/**
	 * Returns the language identifier.
	 * 
	 * @return the language reference
	 */
	public String getLangRef() {
		return langRef;
	}

	/**
	 * Sets the language identifier.
	 * 
	 * @param langRef the language reference to set
	 */
	public void setLangRef(String langRef) {
		this.langRef = langRef;
	}

	/**
	 * Returns the reference to a CV entry.
	 * 
	 * @return the CV entry reference
	 */
	public String getCvRef() {
		return cvRef;
	}

	/**
	 * Sets the reference to a CV entry.
	 * 
	 * @param cvRef the CV entry reference to set
	 */
	public void setCvRef(String cvRef) {
		this.cvRef = cvRef;
	}

	/**
	 * Returns a list of records of links in the set.
	 * 
	 * @return the list of records
	 */
	public List<AbstractRefLinkRecord> getRefLinks() {
		return refLinks;
	}

	/**
	 * Sets the list of records in this set.
	 * 
	 * @param refs the list of records to set
	 */
	public void setRefLinks(List<AbstractRefLinkRecord> refs) {
		this.refLinks = refs;
	}

	/**
	 * Factory function.
	 * @param trans the transcription
	 * @param externalReferences to map external reference IDs to objects.

	 * @return a CrossRefLink or a GroupRefLink (or any other subtype of AbstractRefLink).
	 */
	public RefLinkSet fabricate(
			TranscriptionImpl trans,
			Map<String, ? extends ExternalReference> externalReferences) {
		RefLinkSet set = new RefLinkSet(trans);
		
		set.setLinksID(linksID);
		set.setLinksName(linksName);
		set.setCvRef(cvRef);
		set.setLangRef(langRef);
		if (extRefID != null) {
			set.setExtRef(externalReferences.get(extRefID));
		}
		
		List<RefLink> instlist = new ArrayList<RefLink>();
		set.setRefLinks(instlist);
		
		for (AbstractRefLinkRecord rec : refLinks) {
			instlist.add(rec.fabricate(externalReferences));
		}	
		
		return set;
	}

}
