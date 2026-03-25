package mpi.eudico.client.annotator;

/**
 * Defines methods to be implemented by a TimeScaleListener.
 */
public interface TimeScaleListener {
    /**
     * Notification of a change in the {@code TimeScale}.
     */
    public void updateTimeScale();
}
