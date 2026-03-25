package mpi.eudico.server.corpora.clomimpl.reflink;

import java.util.Map;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.RefLink;

/**
 * Abstract base class for the just-parsed forms of a ...RefLink.
 * Also has a base-implementation of a factory for the ...RefLink object.
 * <p>
 * Implementation note:
 * This class is mainly needed because its ExternalReference member cannot
 * be definitively initialised when parsing the EAF file.
 * It needs to be converted from an id to the proper object.
 * 
 * @author olasei
 */
public abstract class AbstractRefLinkRecord {
	/** the ID of the reference link */
	protected String id;
	/** the name of the reference link */
	protected String refName;
	/** the external resource reference of this link */
	protected String extRefID;
	/** the language identifier of this link */
	protected String langRef;
	/** the CV entry reference of this llink */
	protected String cveRef;
	/** the type of the reference link */
	protected String refType;
	/** the content of this reference link */
	protected String content;
	
	/**
	 * Creates a new record for reference link attributes.
	 */
	public AbstractRefLinkRecord() {
		super();
	}

	/**
	 * Returns the ID of the link.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the ID of the link.
	 * 
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Returns the name of the link.
	 * 
	 * @return the refName
	 */
	public String getRefName() {
		return refName;
	}
	
	/**
	 * Sets the name of the link.
	 * 
	 * @param refName the refName to set
	 */
	public void setRefName(String refName) {
		this.refName = refName;
	}
	
	/**
	 * Returns the external reference of the link.
	 * @return the extRefID
	 */
	public String getExtRefID() {
		return extRefID;
	}
	
	/**
	 * Sets the id of the external reference of the link.
	 * 
	 * @param extRefID the extRefID to set
	 */
	public void setExtRefID(String extRefID) {
		this.extRefID = extRefID;
	}
	
	/**
	 * Returns the language identifier of the link.
	 * 
	 * @return the langRef
	 */
	public String getLangRef() {
		return langRef;
	}
	
	/**
	 * Sets the language identifier of the link.
	 * 
	 * @param langRef the langRef to set
	 */
	public void setLangRef(String langRef) {
		this.langRef = langRef;
	}
	
	/**
	 * Returns the CV entry reference of the link.
	 * 
	 * @return the cveRef
	 */
	public String getCveRef() {
		return cveRef;
	}
	
	/**
	 * Sets the CV entry reference o the link.
	 * 
	 * @param cveRef the cveRef to set
	 */
	public void setCveRef(String cveRef) {
		this.cveRef = cveRef;
	}
	
	/**
	 * Returns the type of the link.
	 * 
	 * @return the refType
	 */
	public String getRefType() {
		return refType;
	}
	
	/**
	 * Sets the type of the link.
	 * 
	 * @param refType the refType to set
	 */
	public void setRefType(String refType) {
		this.refType = refType;
	}
	
	/**
	 * Returns the textual content o the link.
	 * 
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * Sets the content of the link.
	 * 
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * Creates a new {@code RefLink} instance based on the properties of this
	 * record and a {@code ExternalReference} from the specified map.
	 * 
	 * @param externalReferences map to get an external reference from if
	 * there is a {@code extRefID} in this record
	 * 
	 * @return a new {@code RefLink} instance
	 */
	public abstract RefLink fabricate(
			Map<String, ? extends ExternalReference> externalReferences);
	
	/**
	 * Populates the fields of the {@code AbstractRefLink} parameter with the
	 * value of the fields in this class and the external references.
	 * 
	 * @param rl the {@code AbstractRefLink} to update
	 * @param externalReferences map to get an external reference from if
	 * there is a {@code extRefID} in this record
	 */
	protected void fabricate(AbstractRefLink rl, Map<String, ? extends ExternalReference> externalReferences) {
		rl.setId(id);
		rl.setRefName(refName);
		if (extRefID != null) {
			rl.setExtRef(externalReferences.get(extRefID));
		}
		rl.setCveRef(cveRef);
		rl.setLangRef(langRef);
		rl.setRefType(refType);
		rl.setContent(content);
	}
}
