package mpi.eudico.util.multilangcv;

import java.util.Comparator;

/**
 * A small collection of information about a language.
 * 
 * @author olasei
 */
public class LangInfo {
	private String id;
	private String longId;
	private String label;
	
	/**
	 * Construct a LangInfo. 
	 * {@code null} strings are not allowed.
	 * 
	 * @param id the short id of the language
	 * @param longId the long id of the language
	 * @param label the human friendly name of the language
	 */
	public LangInfo(String id, String longId, String label) {
		this.id = id;
		this.longId = longId;
		this.label = label;
	}
	
	/**
	 * Creates a copy of another {@code LangInfo} instance.
	 * 
	 * @param other the {@code LangInfo} to copy
	 */
	public LangInfo(LangInfo other) {
		this.id = other.id;
		this.longId = other.longId;
		this.label = other.label;   		
	}

	/**
	 * Id of the language, for instance "nld".
	 * 
	 * @return the short id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Persistent Identifier of the language, URL form,
	 * for instance "http://cdb.iso.org/lg/CDB-00138580-001".
	 * 
	 * @return the long id of the language
	 */
	public String getLongId() {
		return longId;
	}

	/**
	 * A name or description of the language, for instance "Dutch (nld)".
	 * 
	 * @return the descriptive name or label of the language
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the label. null is not allowed.
	 * 
	 * @param label the label of the language
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * For use in comboboxes and the like.
	 * 
	 * @return a parameterized string representation
	 */
	@Override
	public String toString() {
		return getLabel() + " - " + getId() + " - " + getLongId();	
		
	}
	
	/**
	 * Returns a comparator for the {@code id} member.
	 * 
	 * @return a comparator for the {@code id} member
	 */
	public static Comparator<LangInfo> getIdComparator() {
		return new Comparator<LangInfo>() {
			@Override
			public int compare(LangInfo o1, LangInfo o2) {
				return o1.getId().compareTo(o2.getId());
			}
		};
	}
	
	/**
	 * Returns a comparator for the {@code label} member.
	 * 
	 * @return a comparator for the {@code label} member
	 */
	public static Comparator<LangInfo> getLabelComparator() {
		return new Comparator<LangInfo>() {
			@Override
			public int compare(LangInfo o1, LangInfo o2) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
		};
	}

	/**
	 * Behaves like an equals() method would, but without calling it that.
	 * Which saves us from having to override hashCode() and changing behaviour in collections.
	 * 
	 * @param li the {@code LangInfo} to compare with
	 * @return {@code true} if the three fields of the {@code LangInfo} objects
	 * are equal
	 */
	public boolean valueEquals(LangInfo li) {
		return id.equals(li.id) && 
				longId.equals(li.longId) && 
				label.equals(li.label);
	}

}
