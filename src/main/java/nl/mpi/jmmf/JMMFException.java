package nl.mpi.jmmf;
/**
 * Class for Java- Microsoft Media Foundation related exceptions. 
 */
@SuppressWarnings("serial")
public class JMMFException extends Exception {
	/**
	 * Constructor.
	 */
	public JMMFException() {
		super();
	}

	/**
	 * Constructor with a message and a cause.
	 * 
	 * @param arg0 the message
	 * @param arg1 the cause
	 */
	public JMMFException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructor with a message.
	 * 
	 * @param arg0 the message
	 */
	public JMMFException(String arg0) {
		super(arg0);
	}

	/**
	 * Constructor with a cause.
	 * 
	 * @param arg0 the cause
	 */
	public JMMFException(Throwable arg0) {
		super(arg0);
	}

}
