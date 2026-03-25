package mpi.eudico.server.corpora.event;

/**
 * An ACMEditListener reacts on changes of Transcriptions, Tiers, Annotations
 * and other elements that can be part of annotation document.
 *
 * @author Alexander Klassmann version 21-Jun-2001
 */
public interface ACMEditListener {
    /**
     * Notification of an ACM edit event. 
     *
     * @param e the event
     */
    public void ACMEdited(ACMEditEvent e);
}
