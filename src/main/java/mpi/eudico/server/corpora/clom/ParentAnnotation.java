package mpi.eudico.server.corpora.clom;

import mpi.eudico.server.corpora.event.ParentAnnotationListener;

/**
 * A ParentAnnotation is supposed to notify its listening child annotations
 * after some modification.
 */
public interface ParentAnnotation {
    /**
     * Adds a {@code ParentAnnotationListener} to the list of listeners.
     * 
     * @param l the listener to add
     */
    public void addParentAnnotationListener(ParentAnnotationListener l);

    /**
     * Removes a {@code ParentAnnotationListener} from the list of listeners.
     * 
     * @param l the listener to remove
     */
    public void removeParentAnnotationListener(ParentAnnotationListener l);

    /**
     * Notifies parent listeners.
     */
    public void notifyParentListeners();
}
