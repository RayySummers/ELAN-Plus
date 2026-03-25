package mpi.eudico.server.corpora.lexicon;



/**
 * A Lexicon Link contains a Lexicon Service Client and a Lexicon Identification
 * If a Lexicon Service Client does not have a loaded extension, Lexicon Link contains
 * data to store the necessary information when the transcription is saved.
 * 
 * @author Micha Hulsbosch
 */
public class LexiconLink {
	/** an enumeration of property keys */
	public enum PropKey {
		/** the name property */
		NAME,
		/** the lexicon client type */
		LEXICON_CLIENT_TYPE,
		/** the lexicon identifier */
		LEXICON_IDENTIFICATION,
		/** the URL of the service */
		URL
	}
	private String name;
	private String lexSrvcClntType;
	private LexiconServiceClient srvcClient;
	private LexiconIdentification lexId;
	private String url;
	
	/**
	 * Creates a new lexicon link.
	 * 
	 * @param name the name of the lexicon
	 * @param lexSrvcClntType the lexicon service client type
	 * @param url the URL of the service
	 * @param srvcClient the service client
	 * @param lexId lexicon identifier
	 */
	public LexiconLink(String name, String lexSrvcClntType, String url, 
			LexiconServiceClient srvcClient, LexiconIdentification lexId) {
		this.name = name;
		this.lexSrvcClntType = lexSrvcClntType;
		this.url = url;
		//this.setUrl(url);
		this.srvcClient = srvcClient;
		this.lexId = lexId;
	}
	
	/**
	 * Returns the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the name of the lexicon
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the lexicon service client type.
	 * 
	 * @param lexSrvcClntType the service client type
	 */
	public void setLexSrvcClntType(String lexSrvcClntType) {
		this.lexSrvcClntType = lexSrvcClntType;
	}

	/**
	 * Returns the lexicon service client type.
	 * 
	 * @return the service client type
	 */
	public String getLexSrvcClntType() {
		return lexSrvcClntType;
	}

	/**
	 * Sets the URL of the service.
	 * 
	 * @param url the URL of the service
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Returns the URL of the service.
	 * 
	 * @return the URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns the service client.
	 *  
	 * @return the service client
	 */
	public LexiconServiceClient getSrvcClient() {
		return srvcClient;
	}
	
	/**
	 * Sets the service client.
	 *  
	 * @param srvcClient the service client
	 */
	public void setSrvcClient(LexiconServiceClient srvcClient) {
		this.srvcClient = srvcClient;
	}

	/**
	 * Returns the lexicon id.
	 * 
	 * @return the lexicon id
	 */
	public LexiconIdentification getLexId() {
		return lexId;
	}
	
	/**
	 * Sets the lexicon identifier.
	 * 
	 * @param lexId the lexicon identifier
	 */
	public void setLexId(LexiconIdentification lexId) {
		this.lexId = lexId;
	}
	
	/**
	 * Compares two lexicon links, first based on the name and, 
	 * if these are equal, then based on the lexicon identifier 
	 * (which in turn is based on the name of the lexicon identifier).  
	 * 
	 * @param oLink the LexiconLink to compare to
	 * @return -1 if this link is less than the other, 0 if they are equal,
	 * 1 if this link is greater than the other, all in the String sense of less,
	 * equal and greater than
	 */
	public int compareNameAndLexId(LexiconLink oLink) {
		if (oLink == null) {
			return -1;
		}
		
		int retValue = -1;
		if (name != null) {
			if (oLink.getName() != null) {
				retValue = name.compareTo(oLink.getName());
			}// else it is -1
		} else {
			if (oLink.getName() == null) {
				retValue = 0;
			} else {
				retValue = 1;
			}
		}
		// if the names are equal compare the lexicon id
		if (retValue == 0) {// compare the lexId
			if (lexId != null) {
				retValue = lexId.compareTo(oLink.getLexId());
			} else {
				if (oLink.getLexId() != null) {
					retValue = 1;
				}
			}
		}
		
		return retValue;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
