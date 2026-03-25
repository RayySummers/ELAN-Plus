package mpi.eudico.client.annotator;

/**
 * Defines methods to be implemented by a SelectionListener.
 */
public interface SelectionListener {
    /**
     * Notification that the selection changed. 
     * Listeners have to check the new values separately.
     */
    public void updateSelection();
}
