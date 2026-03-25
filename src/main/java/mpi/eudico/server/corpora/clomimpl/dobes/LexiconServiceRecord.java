package mpi.eudico.server.corpora.clomimpl.dobes;

/**
 * Record of a Lexicon Query Bundle element or Lexicon Link element found by
 * the EAF parser.
 * 
 * @author Micha Hulsbosch
 */
public class LexiconServiceRecord {
	private String name;
	private String lexiconId;
	private String lexiconName;
	private String type;
	private String datcatId;
	private String datcatName;
	private String url;
	
	/**
	 * Creates a new lexicon service record.
	 */
	public LexiconServiceRecord() {
		super();
	}
	
	/**
	 * Returns the name of the lexicon service.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of the lexicon service.
	 * 
	 * @param name the name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the lexicon id.
	 * 
	 * @return the lexicon id
	 */
	public String getLexiconId() {
		return lexiconId;
	}
	
	/**
	 * Sets the lexicon id.
	 * 
	 * @param lexiconId the lexicon id
	 */
	public void setLexiconId(String lexiconId) {
		this.lexiconId = lexiconId;
	}
	
	/**
	 * Returns the lexicon name.
	 * 
	 * @return the lexicon name
	 */
	public String getLexiconName() {
		return lexiconName;
	}
	
	/**
	 * Sets the lexicon name.
	 * 
	 * @param lexiconName the lexicon name
	 */
	public void setLexiconName(String lexiconName) {
		this.lexiconName = lexiconName;
	}
	
	/**
	 * Return the type.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Sets the type.
	 * 
	 * @param type the type
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Returns the data category id.
	 * 
	 * @return the data category id
	 */
	public String getDatcatId() {
		return datcatId;
	}
	
	/**
	 * Sets the data category id.
	 * 
	 * @param datcatId the data category id
	 */
	public void setDatcatId(String datcatId) {
		this.datcatId = datcatId;
	}
	
	/**
	 * Returns the name of the data category.
	 * 
	 * @return the data category name
	 */
	public String getDatcatName() {
		return datcatName;
	}
	
	/**
	 * Sets the name of the data category.
	 * 
	 * @param datcatName the data category name
	 */
	public void setDatcatName(String datcatName) {
		this.datcatName = datcatName;
	}
	
	/**
	 * Returns the URL of the lexicon service.
	 * 
	 * @return the URL of the service
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * Sets the URL of the service.
	 * 
	 * @param url the URL of the lexicon service
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
}
