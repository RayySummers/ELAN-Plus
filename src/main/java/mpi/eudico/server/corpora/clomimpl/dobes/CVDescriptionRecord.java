package mpi.eudico.server.corpora.clomimpl.dobes;

/**
 * A record for the description object of a Controlled Vocabulary.
 */
public class CVDescriptionRecord {
	private String langRef;
	private String description;

	/**
	 * Constructor.
	 */
	public CVDescriptionRecord() {
		super();
	}
	
	/**
	 * Returns the language identifier.
	 * 
	 * @return the language identifier
	 */
	public String getLangRef() {
		return langRef;
	}
	/**
	 * Sets the language identifier.
	 * 
	 * @param langRef the new language identifier
	 */
	public void setLangRef(String langRef) {
		this.langRef = langRef;
	}
	
	/**
	 * Returns the description in this language.
	 * 
	 * @return the description in this language
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description in this language.
	 * 
	 * @param description the new description in this language
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	

}
