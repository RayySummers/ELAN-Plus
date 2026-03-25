package mpi.eudico.client.annotator;

/**
 * The interface that defines methods for an ELAN Locale listener.
 */
public interface ElanLocaleListener {
    /**
     * Notification that the language for the user interface changed.
     */
    public void updateLocale();
}
