package mpi.eudico.server.corpora.util;

/**
 * An interface for editable objects in the ACM model. When such an object is
 * modified it will arrange handling of the modification, either by delegating
 * it to another ACMEditableObject, or by notifying listeners.
 * Examples of implementers of ACMEditableObject are Transcription, Tier, Tag,
 * and Annotation. 
 */
public interface ACMEditableObject {
    /**
     * After a change in an editable object this method can be called.
     *
     * @param operation one of the constants in {@code ACMEditEvent}
     * @param modification the object which constitutes the modification,
     * can be {@code null}
     */
    public void modified(int operation, Object modification);

    /**
     * Handles the modification by creating an event and notifying listeners.
     *
     * @param source the source of the event, the invalidated object
     * @param operation one of the constants in {@code ACMEditEvent}
     * @param modification the object which constitutes the modification,
     * can be {@code null}
     */
    public void handleModification(ACMEditableObject source, int operation,
        Object modification);
}
