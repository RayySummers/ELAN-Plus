package nl.mpi.avf.player;
/**
 * A class for JAVFPLayer exceptions. Does not add anything to
 * the {@link Exception} implementation.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class JAVFPlayerException extends Exception {

	/**
	 * Constructor.
	 */
	public JAVFPlayerException() {
		super();
	}

	/**
	 * Constructor with a message.
	 *  
	 * @param arg0 the message
	 */
	public JAVFPlayerException(String arg0) {
		super(arg0);
	}

	/**
	 * Constructor with a cause.
	 * 
	 * @param arg0 the cause
	 */
	public JAVFPlayerException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Constructor with a method and a cause.
	 *  
	 * @param arg0 the message
	 * @param arg1 the cause
	 */
	public JAVFPlayerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
