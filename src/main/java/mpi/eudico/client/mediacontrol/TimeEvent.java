package mpi.eudico.client.mediacontrol;

/**
 * Event that is sent as a parameter to {@code controllerUpdate} by the
 * TimeLineController and the PeriodicUpdateController. The new type TimeEvent
 * is created to be able to distinguish it from other controller events in the
 * {@code controllerUpdate} methods.
 */
public class TimeEvent extends ControllerEvent {
    /**
     * Construct the TimeEVent for a specific Controller.
     *
     * @param controller the controller, the source of event
     */
    public TimeEvent(Controller controller) {
        super(controller);
    }
}
