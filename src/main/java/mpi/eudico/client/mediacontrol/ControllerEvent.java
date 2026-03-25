package mpi.eudico.client.mediacontrol;

/**
 * Event that is sent as a parameter to {@code controllerUpdate} by the
 * {@code TimeLineController} and the {@code PeriodicUpdateController}. 
 * The generic type {@code ControllerEvent} is extended by more specific event
 * types like {@code StartEvent}, {@code StopEvent} and {@code TimeEvent}.
 */
public class ControllerEvent {
    private Controller controller;

    /**
     * Construct the event for a {@code Controller}.
     *
     * @param controller the source of the event
     */
    public ControllerEvent(Controller controller) {
        this.controller = controller;
    }

    /**
     * Returns the source of the event.
     *
     * @return the controller as source of the event
     */
    public Controller getSource() {
        return controller;
    }
}
