package mpi.eudico.client.mediacontrol;

/**
 * Interface that defines the methods for a ControllerListener.
 * A ControllerListener will be called from a Controller at specific times and
 * will receive a ControllerEvent.
 */
public interface ControllerListener {
    /**
     * Notification of a {@code ControllerEvent}.
     *
     * @param event the event
     */
    public void controllerUpdate(ControllerEvent event);

    //	public void synchronizedControllerUpdate(ControllerEvent event);
}
