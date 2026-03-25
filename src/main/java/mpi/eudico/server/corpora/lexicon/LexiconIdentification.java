package mpi.eudico.server.corpora.lexicon;

/**
 * Holds the ID, name and description of a Lexicon.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author Micha Hulsbosch
 *
 */
public class LexiconIdentification implements Comparable<LexiconIdentification> {
	private String id;
	private String name;
	private String description;
	
	/**
	 * Constructor.
	 */
	public LexiconIdentification() {
		super();
	}

	/**
	 * Constructor.
	 * @param id the id of a  lexicon
	 * @param name the name of a lexicon
	 */
	public LexiconIdentification(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Copy constructor, copies all fields of the original.
	 *  
	 * @param otherLexIdent the LexiconIdentification to copy
	 */
	public LexiconIdentification(LexiconIdentification otherLexIdent) {
		super();
		if (otherLexIdent != null) {
			this.id = otherLexIdent.getId();
			this.name = otherLexIdent.getName();
			this.description = otherLexIdent.getDescription();
		}// otherwise all fields will be null
	}

	/**
	 * Returns the id of the lexicon
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the id of the lexicon.
	 * 
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Returns the name of the lexicon.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of the lexicon.
	 * 
	 * @param name the name of the lexicon
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the description of the lexicon.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description of the lexicon.
	 * 
	 * @param description the description of the lexicon
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int compareTo(LexiconIdentification o) {
		return name.compareToIgnoreCase(o.getName());
	}
}
