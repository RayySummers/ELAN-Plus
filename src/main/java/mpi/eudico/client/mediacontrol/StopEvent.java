package mpi.eudico.client.mediacontrol;

/**
 * Event that is sent as a parameter to {@code controllerUpdate} by the
 * TimeLineController and the PeriodicUpdateController. The new type StopEvent
 * is created to be able to distinguish it from other ControllerEvents in the
 * {@code controllerUpdate} methods.
 */
public class StopEvent extends ControllerEvent {
    /**
     * Construct the StopEvent for a specific Controller.
     *
     * @param controller the controller, the source of the event
     */
    public StopEvent(Controller controller) {
        super(controller);
    }
}
