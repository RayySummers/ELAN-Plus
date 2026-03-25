package mpi.eudico.client.annotator;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;

/**
 * Administers the current ActiveAnnotation.
 */
public class ActiveAnnotation {
    private List<ActiveAnnotationListener> listeners;
    private Annotation annotation;

    /**
     * Creates a new ActiveAnnotation instance.
     * Creates a list for listeners, the annotation is set to {@code null}. 
     */
    public ActiveAnnotation() {
        listeners = new ArrayList<ActiveAnnotationListener>();
        annotation = null;
    }

    /**
     * Sets the {@code Annotation} that is active.
     * If {@code null} is passed the active annotation is reset (no active
     * annotation).
     *
     * @param annotation the new active annotation or {@code null}
     */
    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;

        // Tell all the interested ActiveAnnotation about the change
        notifyListeners();
    }

    /**
     * Returns the active {@code Annotation}.
     *
     * @return the active {@code Annotation} or {@code null}
     */
    public Annotation getAnnotation() {
        return annotation;
    }

    /**
     * Notifies ActiveAnnotationListeners of a change in the ActiveAnnotation.
     */
    public void notifyListeners() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).updateActiveAnnotation();
        }
    }

    /**
     * Add a listener for ActiveAnnotation events.
     *
     * @param listener the listener to register for notification of
     *        ActiveAnnotation events.
     */
    public void addActiveAnnotationListener(ActiveAnnotationListener listener) {
        listeners.add(listener);
        listener.updateActiveAnnotation();
    }

    /**
     * Remove a listener for ActiveAnnotation events.
     *
     * @param listener the listener to be removed from the listener list
     *        
     */
    public void removeActiveAnnotationListener(
        ActiveAnnotationListener listener) {
        listeners.remove(listener);
    }
}
