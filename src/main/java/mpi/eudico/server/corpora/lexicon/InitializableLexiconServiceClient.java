package mpi.eudico.server.corpora.lexicon;

/**
 * Interface for an initializable lexicon service client.
 * 
 * @author michahulsbosch
 *
 */
public interface InitializableLexiconServiceClient extends LexiconServiceClient {
	/**
	 * Initializes the service client.
	 * 
	 * @param doInBackground whether or not the work is performed in a 
	 * background thread
	 * @param lexID an identifier of the lexicon
	 * @throws LexiconServiceClientException if the client can not be initialized
	 */
	public void initialize(Boolean doInBackground, LexiconIdentification lexID) throws LexiconServiceClientException;
}
