package mpi.eudico.server.corpora.clom;

import java.util.Set;

/**
 * RefLink is the common interface for the {@code GROUP_REF_LINK} and 
 * {@code CROSS_REF_LINK} elements in the EAF file.
 * <p>
 * These elements link together Annotations, either one-on-one or in groups.
 * <p>
 * Implementation note:
 * don't override hash() or equals(), since we want RefLinks to be in
 * HashSets based on their reference value only.
 * 
 * @author olasei
 */

public interface RefLink {

	/**
	 * Returns the ID of this reference link.
	 * 
	 * @return the ID of the reference link itself.
	 */
	public String getId();
	
	/**
	 * Returns the name of this reference link.
	 * 
	 * @return a name of the reference link
	 */
	public String getRefName();

	/**
	 * Returns the external reference of this link, if any.
	 * 
	 * @return any External Reference that there may be, can be {@code null}
	 */
	public ExternalReference getExtRef();

	/**
	 * Returns the language identifier of this reference link.
	 * 
	 * @return the language of this reference link, can be {@code null}
	 */
	public String getLangRef();

	/**
	 * Returns the reference to a controlled vocabulary entry.
	 * 
	 * @return a reference to a Controlled Vocabulary Entry or {@code null}
	 */
	public String getCveRef();

	/**
	 * Returns the type of this reference link.
	 * 
	 * @return what sort of reference is this, the type of reference
	 */
	public String getRefType();

	/**
	 * Returns the textual contents of this reference link.
	 * 
	 * @return the text inside the element (may be removed from the schema)
	 */
	public String getContent();
	
	/**
	 * Checks if this reference link refers to any id in the specified collection.
	 * 
	 * @param ids a collection of id's to check
	 * @return {@code true} if this RefLink refers in some way to any of the given ids.
	 */
	public boolean references(Set<String> ids);
	
	/**
	 * Returns a string representation of this link.
	 * 
	 * @return converts the RefLink to some readable representation.
	 */
	@Override
	public String toString();
}
