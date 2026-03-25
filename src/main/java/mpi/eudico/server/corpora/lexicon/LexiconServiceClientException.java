package mpi.eudico.server.corpora.lexicon;

/**
 * Exception thrown by LexiconServiceClient methods.
 * 
 * @author Micha Hulsbosch
 */
@SuppressWarnings("serial")
public class LexiconServiceClientException extends Exception {
	/** message for absence of user name or password */
	public static final String NO_USERNAME_OR_PASSWORD = "No username or password";
	/** malformed URL message */
	public static final String MALFORMED_URL = "Malformed Url";
	/** message for a client malfunction */
	public static final String CLIENT_MALFUNCTION = "Client malfunction";
	/** message for an incorrect user name or password */
	public static final String INCORRECT_USERNAME_OR_PASSWORD = "Incorrect username or password";
	/** message for a connection malfunction */
	public static final String CONNECTION_MALFUNCTION = "CONNECTION_MALFUNCTION";

	/**
	 * No argument constructor.
	 */
	public LexiconServiceClientException() {
	}

	/**
	 * Creates a new client exception instance.
	 * 
	 * @param message the exception message
	 */
	public LexiconServiceClientException(String message) {
		super(message);
	}

	/**
	 * Creates a new client exception instance.
	 * 
	 * @param cause the exception cause
	 */
	public LexiconServiceClientException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new client exception instance.
	 * 
	 * @param message the exception message
	 * @param cause the exception cause
	 */
	public LexiconServiceClientException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Returns the key for a localized version of a message.
	 * 
	 * @return the key to use for looking up a localized message in a 
	 * resource bundle, or null 
	 */
	public String getMessageLocaleKey() {
		if(getMessage().equals(MALFORMED_URL)) {
			return "LexiconServiceClientException.MalformedUrl";
		} else if(getMessage().equals(CLIENT_MALFUNCTION)) {
			return "LexiconServiceClientException.ClientMalfunction";
		} else if(getMessage().equals(CONNECTION_MALFUNCTION)) {
			return "LexiconServiceClientException.ConnectionMalfunction";
		} else if(getMessage().equals(LexiconServiceClientException.INCORRECT_USERNAME_OR_PASSWORD)) {
			return "LexiconServiceClientException.IncorrectUsernameOrPassword";
		}	
		return null;
	}
}
