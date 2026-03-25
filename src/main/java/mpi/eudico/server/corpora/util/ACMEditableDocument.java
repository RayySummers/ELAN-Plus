package mpi.eudico.server.corpora.util;

import mpi.eudico.server.corpora.event.ACMEditListener;

/**
 * An ACMEditableDocument is supposed to be implemented by a Transcription. 
 * It represents the Observable for edit modifications.
 */
public interface ACMEditableDocument {
    /**
     * Adds an {@code ACMEditListener} to the list of listeners.
     * 
     * @param l the listener to add to the list of listeners
     */
    public void addACMEditListener(ACMEditListener l);

    /**
     * Removes an {@code ACMEditListener} from the list of listeners.
     * 
     * @param l the listener to remove from the list of listeners
     */
    public void removeACMEditListener(ACMEditListener l);

    /**
     * Notifies listeners by creating an event object based on the parameters.
     *
     * @param source the source object on which the edit occurred
     * @param operation an identifier for the type of event
     * @param modification the modification, the object that was added or
     * removed etc.
     */
    public void notifyListeners(ACMEditableObject source, int operation,
        Object modification);
}
