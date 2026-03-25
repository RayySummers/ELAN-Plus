package nl.mpi.jds;

/**
 * A class for Java Direct Show exceptions. Doesn't add anything to the 
 * {@code Exception} implementation.
 * 
 * @author Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class JDSException extends Exception {

	/**
	 * Constructor.
	 */
	public JDSException() {
		super();
	}

	/**
	 * Constructor with a message and a cause.
	 * 
	 * @param arg0 the message
	 * @param arg1 the cause
	 */
	public JDSException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructor with a message.
	 * 
	 * @param arg0 the message
	 */
	public JDSException(String arg0) {
		super(arg0);
	}

	/**
	 * Constructor with a cause.
	 * 
	 * @param arg0 the cause
	 */
	public JDSException(Throwable arg0) {
		super(arg0);
	}

}
