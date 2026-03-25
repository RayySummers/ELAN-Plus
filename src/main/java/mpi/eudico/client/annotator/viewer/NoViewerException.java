package mpi.eudico.client.annotator.viewer;

/**
 * Exception that is thrown when it was not possible to create an ELAN Viewer.
 */
@SuppressWarnings("serial")
public class NoViewerException extends Exception {
    /**
     * Creates a new NoViewerException instance.
     *
     * @param message the description of the exception
     */
    public NoViewerException(String message) {
        super(message);
    }
}
