package mpi.eudico.server.corpora.lexicon;

/**
 * Class to hold the identification of a Lexical Entry Field: ID and name 
 * (and description).
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author Micha Hulsbosch
 */
public class LexicalEntryFieldIdentification implements Comparable<LexicalEntryFieldIdentification> {
	private String id;
	private String name;
	private String description;
	
	/**
	 * Creates a new identification instance.
	 * 
	 * @param id the entry field id
	 * @param name the entry field name
	 */
	public LexicalEntryFieldIdentification(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Returns the entry field id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the entry field id.
	 * 
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Returns the field name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Sets the field name.
	 * 
	 * @param name the field name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the description of the field.
	 * 
	 * @return the description of the field
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description of the field.
	 * 
	 * @param description the description of the field
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Compares two field identification objects by comparing their names.
	 */
	@Override
	public int compareTo(LexicalEntryFieldIdentification o) {
		if(o == null) {
			return -1;
		}
		if (name == null && o.getName() == null) {
			return 0;
		}
		if (name == null) {
			return 1;
		}
		if (o.getName() == null) {
			return -1;
		}
		return name.compareToIgnoreCase(o.getName());
	}
}
