package mpi.eudico.client.annotator;

/**
 * Defines methods for listeners for changes in the {@code ActiveAnnotation}.
 */
public interface ActiveAnnotationListener {
    /**
     * Notification of a change in the active annotation.
     */
    public void updateActiveAnnotation();
}
