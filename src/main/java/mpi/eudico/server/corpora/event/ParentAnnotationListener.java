package mpi.eudico.server.corpora.event;

import java.util.EventObject;


/**
 * A ParentAnnotationListener reacts on changes of the ParentAnnotation.
 *
 * @author Hennie Brugman version 1-Jul-2002
 */
public interface ParentAnnotationListener {
    /**
     * Notification of a change of/in the parent annotation
     *
     * @param e the event
     */
    public void parentAnnotationChanged(EventObject e);
}
