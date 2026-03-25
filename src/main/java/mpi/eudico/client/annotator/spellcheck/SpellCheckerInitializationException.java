/**
 * 
 */
package mpi.eudico.client.annotator.spellcheck;

/**
 * Exception thrown if an attempt to initialize a spell checker failed.
 * 
 * @author micha
 */
@SuppressWarnings("serial")
public class SpellCheckerInitializationException extends Exception {

	/**
	 * No-arg constructor.
	 */
	public SpellCheckerInitializationException() {
	}

	/**
	 * Creates a new exception instance with a message.
	 * 
	 * @param message the exception message
	 */
	public SpellCheckerInitializationException(String message) {
		super(message);
	}

	/**
	 * Creates a new exception instance with a cause.
	 * 
	 * @param cause the cause of the exception
	 */
	public SpellCheckerInitializationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new exception instance with message and cause.
	 * 
	 * @param message the exception message
	 * @param cause the cause of the exception
	 */
	public SpellCheckerInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new exception instance.
	 * 
	 * @param message the exception message
	 * @param cause the cause of the exception
	 * @param enableSuppression enable or disable suppression
	 * @param writableStackTrace whether or not the stack trace is writable
	 */
	public SpellCheckerInitializationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
