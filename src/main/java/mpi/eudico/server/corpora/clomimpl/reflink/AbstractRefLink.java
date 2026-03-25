package mpi.eudico.server.corpora.clomimpl.reflink;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.RefLink;

/**
 * AbstractRefLink is the abstract superclass for the GROUP_REF_LINK and CROSS_REF_LINK
 * elements in the EAF file. It implements common attributes, but leaves abstract which
 * other elements are actually linked together.
 * 
 * @author olasei
 */
public abstract class AbstractRefLink implements RefLink, Comparable<AbstractRefLink> {
	/** the ID of the link, not {@code null} */
	protected String id;
	/** the name of the link, can be {@code null} */
	protected String refName;
	/** the external resource reference of this link, can be {@code null} */
	protected ExternalReference extRef;
	/** the language identifier of this link, can be {@code null} */
	protected String langRef;
	/** the controlled vocabulary entry reference of this link, can be 
	 * {@code null} */
	protected String cveRef;
	/** the type of this reference link, not {@code null} */
	protected String refType;
	/** the textual content of this link, not {@code null} */
	protected String content;
	
	/**
	 * Constructor, initializes all fields to {@code null} or empty string
	 */
	public AbstractRefLink() {
		super();
		this.id = "";
		this.refName = null;
		this.extRef = null;
		this.langRef = null;
		this.cveRef = null;
		this.refType = "";
		this.content = "";
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the ID of this reference link.
	 * 
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getRefName() {
		return refName;
	}
	
	/**
	 * Sets the name of this reference link.
	 * 
	 * @param refName the refName to set
	 */
	public void setRefName(String refName) {
		this.refName = refName;
	}

	@Override
	public ExternalReference getExtRef() {
		return extRef;
	}
	
	/**
	 * Sets the external reference of this link.
	 *  
	 * @param extRef the extRef to set
	 */
	public void setExtRef(ExternalReference extRef) {
		this.extRef = extRef;
	}

	@Override
	public String getLangRef() {
		return langRef;
	}
	
	/**
	 * Sets the language identifier of this link.
	 * 
	 * @param langRef the langRef to set
	 */
	public void setLangRef(String langRef) {
		this.langRef = langRef;
	}

	@Override
	public String getCveRef() {
		return cveRef;
	}
	
	/**
	 * Sets the reference to a controlled vocabulary entry.
	 * 
	 * @param cveRef the cveRef to set
	 */
	public void setCveRef(String cveRef) {
		this.cveRef = cveRef;
	}

	@Override
	public String getRefType() {
		return refType;
	}
	
	/**
	 * Sets the type of this reference link.
	 * 
	 * @param refType the refType to set
	 */
	public void setRefType(String refType) {
		this.refType = refType;
	}

	@Override
	public String getContent() {
		return content;
	}
	
	/**
	 * Sets the textual content or value of this reference link.
	 * 
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * Compares this reference link to another link by comparing their ID's.
	 * 
	 * @param l the other reference link
	 * @return the result of comparing this link's ID to the other link's ID
	 * 
	 * @see String#compareTo(String)
	 */
	@Override // Comparable<AbstractRefLink>
	public int compareTo(AbstractRefLink l) {
		return this.id.compareTo(l.getId());		
	}
	
}
