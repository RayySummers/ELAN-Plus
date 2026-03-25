package mpi.eudico.client.util;

/**
 * Wrapper for exceptions that can occur during export.
 */
@SuppressWarnings("serial")
public class ExportException extends Exception {
    /**
     * Creates a new ExportException instance.
     * 
     * @param message the exception message
     */
    public ExportException(String message){
        super(message);
    }
}
