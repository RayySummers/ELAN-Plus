package mpi.eudico.client.mediacontrol;


/**
 * Interface that defines methods for a Controller that is connected to an
 * ElanMediaPlayer.
 */
public interface Controller {
    /**
     * Starts the controller.
     */
    public void start();

    /**
     * Stops the controller.
     */
    public void stop();

    /**
     * Sets the media time of the controller.
     *
     * @param time the media time in milliseconds
     */
    public void setMediaTime(long time);
    
    /**
     * Sets the stop time of the controller.
     * Usually in the context of playing a selection.
     * 
     * @param time the stop time of the controller
     */
    public void setStopTime(long time);

    /**
     * Sets the rate of the controller.
     *
     * @param rate the new rate
     */
    public void setRate(float rate);

    /**
     * Adds a controller listener to this controller.
     *
     * @param listener the listener to add
     */
    public void addControllerListener(ControllerListener listener);

    /**
     * Removes a controller listener.
     *
     * @param listener the listener to remove
     */
    public void removeControllerListener(ControllerListener listener);

    /**
     * Returns the number of connected listeners.
     *
     * @return the number of connected listeners
     */
    public int getNrOfConnectedListeners();

    /**
     * Posts a controller event to the listeners.
     *
     * @param event the controller event
     */
    public void postEvent(ControllerEvent event);
}
