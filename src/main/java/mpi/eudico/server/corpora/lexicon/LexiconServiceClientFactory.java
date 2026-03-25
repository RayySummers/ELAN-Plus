package mpi.eudico.server.corpora.lexicon;

import java.util.ArrayList;


/**
 * The abstract factory which Lexicon Service extension should extend.
 * 
 * @author Micha Hulsbosch
 */
public abstract class LexiconServiceClientFactory {
	/**
	 * Creates a new client factory.
	 */
	public LexiconServiceClientFactory() {
		super();
	}

	/**
	 * Sets the type of a service client.
	 * 
	 * @param type the type of a service
	 */
	public abstract void setType(String type);
	
	/**
	 * Returns the type of a service client.
	 * 
	 * @return the type of the client
	 */
	public abstract String getType();
	
	/**
	 * Sets the description of the service client.
	 * 
	 * @param description the description of the client
	 */
	public abstract void setDescription(String description);
	
	/**
	 * Returns the description of the client.
	 * 
	 * @return the description of the client
	 */
	public abstract String getDescription();
	
	/**
	 * Sets the default URL of the service.
	 * 
	 * @param url the default URL of the service
	 */
	public abstract void setDefaultUrl(String url);
	
	/**
	 * Returns the default URL of the service.
	 * 
	 * @return the default URL of the service
	 */
	public abstract String getDefaultUrl();
	
	/**
	 * Creates and returns a lexicon service client.
	 *  
	 * @return a new lexicon service client
	 */
	public abstract LexiconServiceClient createClient();
	
	/**
	 * Creates and returns a lexicon service client.
	 * 
	 * @param url the URL of the service client to create
	 * @return a new lexicon service client
	 */
	public abstract LexiconServiceClient createClient(String url);
	
	/**
	 * Creates and returns a lexicon service client.
	 * 
	 * @param username the user name
	 * @param password the password
	 * @return a new lexicon service client
	 */
	public abstract LexiconServiceClient createClient(String username, String password);
	
	/**
	 * Creates and returns a lexicon service client.
	 * 
	 * @param url the default URL of the service
	 * @param username the user name
	 * @param password the password
	 * @return a new lexicon service client
	 */
	public abstract LexiconServiceClient createClient(String url, String username, String password);
	
	/**
	 * Adds a search constraint.
	 * 
	 * @param content the constraint
	 */
	public abstract void addSearchConstraint(String content);
	
	/**
	 * Returns a list of search constraint
	 * 
	 * @return a list of search constraint
	 */
	public abstract ArrayList<String> getSearchConstraints();
}
