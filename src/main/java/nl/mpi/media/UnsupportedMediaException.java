package nl.mpi.media;

/**
 * A generic exception that indicates a media file or stream is not supported.
 * The context determines the kind of media it concerns and the kind of 
 * operation that is not supported. 
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class UnsupportedMediaException extends Exception {
	/*
	 * Constructors and their description inherit from the super class.
	 */
	/**
	 * Constructor.
	 */
	public UnsupportedMediaException() {
		super();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param message the exception message
	 * @param cause the cause of the exception
	 */
	public UnsupportedMediaException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message the exception message
	 */
	public UnsupportedMediaException(String message) {
		super(message);
	}

}
