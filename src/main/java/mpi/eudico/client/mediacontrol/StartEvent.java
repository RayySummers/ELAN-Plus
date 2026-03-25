package mpi.eudico.client.mediacontrol;

/**
 * Event that is sent as a parameter to {@code controllerUpdate} by the
 * TimeLineController and the PeriodicUpdateController. The new type
 * StartEvent is created to be able to distinguish it from other
 * ControllerEvents in the {@code controllerUpdate} methods.
 */
public class StartEvent extends ControllerEvent {
    /**
     * Construct the {@code StartEvent} for a specific Controller.
     *
     * @param controller the controller, the source of the event
     */
    public StartEvent(Controller controller) {
        super(controller);
    }
}
