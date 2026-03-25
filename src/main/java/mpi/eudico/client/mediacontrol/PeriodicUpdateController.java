package mpi.eudico.client.mediacontrol;

/**
 * Class that generates periodic TimeEvents. This class must implement the
 * Controller interface in order to be able to be coupled to a Player.
 */
public class PeriodicUpdateController extends EventPostingBase
    implements Controller, Runnable {
    /** The started state */
    private final int STARTED = 0;

    /** The stopped state */
    private final int STOPPED = 1;
    private long period;
    private float rate;
    private Thread thread;
    private volatile int state; // Thread docs advice to use volatile
    private TimeEvent timeEvent;
    private StartEvent startEvent;
    private StopEvent stopEvent;

    /**
     * Create a controller that must be connected to an ElanMediaPlayer and that
     * calls {@code controllerUpdate} on its connected listeners every {@code t} 
     * milliseconds.
     * 
     * @param period the number of milliseconds between posts
     */
    public PeriodicUpdateController(long period) {
        // the pulse period in milliseconds
        this.period = period;

        // initially the controller is not running
        state = STOPPED;

        // create the events
        timeEvent = new TimeEvent(this);
        startEvent = new StartEvent(this);
        stopEvent = new StopEvent(this);
    }

    /**
     * While in the started state send periodic ControlerEvents
     */
    @Override
	public void run() {
        long n = 0;

        // the run Thread started so set the state accordingly
        state = STARTED;

        while (state == STARTED) {
            // send a TimeEvent to the connected ControllerListeners
            postEvent(timeEvent);

            // sleep period milli seconds
            if (!Thread.currentThread().isInterrupted()) {
                // sleep until next event
                try {
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Not implemented by this controller.
     */
    @Override
	public void setStopTime(long time) {
    	
    }
    
    /**
     * Notify listeners of a time event.
     * Calls {@code #postEvent(ControllerEvent)} with a single {@code TimeEvent}
     * instance (the event does not contain the time).
     *
     * @param time the media time (ignored)
     */
    @Override
	public void setMediaTime(long time) {
        postEvent(timeEvent);
    }

    /**
     * The rate is ignored at the moment.
     *
     * @param rate the rate
     */
    @Override
	public void setRate(float rate) {
        this.rate = rate;
    }

    /**
     * Stop the periodic {@code controllerUpdate} calls.
     */
    @Override
	public void stop() {
        if (state == STOPPED) {
            return;
        }

        state = STOPPED;

        if (thread != null) {
            thread.interrupt();
        }

        postEvent(stopEvent);
    }

    /**
     * Start the periodic {@code controllerUpdate} calls.
     */
    @Override
	public void start() {
        if (state == STARTED) {
            return;
        }

        // Tell all the listeners that we start
        postEvent(startEvent);

        // start the run method
        thread = new Thread(this, "PeriodicUpdateController");
        thread.start();
    }
}
