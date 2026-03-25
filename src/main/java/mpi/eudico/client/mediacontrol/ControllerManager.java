package mpi.eudico.client.mediacontrol;

import java.util.ArrayList;
import java.util.List;

/**
 * A ControllerManager takes care of informing interested Controllers about
 * media related events.
 */
public class ControllerManager extends EventPostingBase {
    private List<Controller> controllers;
    private boolean controllersAreStarted;

    /**
     * Creates a new ControllerManager instance.
     */
    public ControllerManager() {
        controllers = new ArrayList<Controller>();
        controllersAreStarted = false;
    }

    /**
     * Adds a {@code Controller} to this manager.
     *
     * @param controller the {@code Controller} to add
     */
    public synchronized void addController(Controller controller) {
        if (!controllers.contains(controller)) {
            controllers.add(controller);
        }
    }

    /**
     * Removes a {@code Controller} from this manager.
     *
     * @param controller the {@code Controller} to remove
     */
    public synchronized void removeController(Controller controller) {
        controllers.remove(controller);
    }

    /**
     * Start all managed Controllers.
     */
    public void startControllers() {
        if (!controllersAreStarted) {
            final int size = controllers.size();
			for (int i = 0; i < size; i++) {
                controllers.get(i).start();
            }

            controllersAreStarted = true;
        }
    }

    /**
     * Stop all managed Controllers.
     */
    public void stopControllers() {
        if (controllersAreStarted) {
            final int size = controllers.size();
			for (int i = 0; i < size; i++) {
                controllers.get(i).stop();
            }

            controllersAreStarted = false;
        }
    }

    /**
     * Set the stop time for all managed Controllers.
     *
     * @param time the stop time for the controllers
     */
    public void setControllersStopTime(long time) {
        final int size = controllers.size();
		for (int i = 0; i < size; i++) {
            controllers.get(i).setStopTime(time); 
        }
    }
    
    /**
     * Set the media time for all managed Controllers.
     *
     * @param time the media time for the controllers
     */
    public void setControllersMediaTime(long time) {
        final int size = controllers.size();
		for (int i = 0; i < size; i++) {
            controllers.get(i).setMediaTime(time);
        }
    }

    /**
     * Set the rate for all managed Controllers.
     *
     * @param rate the rate for the controllers
     */
    public void setControllersRate(float rate) {
        final int size = controllers.size();
		for (int i = 0; i < size; i++) {
            controllers.get(i).setRate(rate);
        }
    }
}
