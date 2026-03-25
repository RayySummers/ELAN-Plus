package mpi.eudico.client.annotator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class that manages properties of a time scale.
 */
public class TimeScale {
    private List<TimeScaleListener> listeners;
    private long timeScaleBeginTime;
    private long timeScaleEndTime;
    private float timeScaleMsPerPixel;
    private ReentrantLock timeScaleLock = new ReentrantLock();

    /**
     * Creates an empty TimeScale (begin time == end time).
     */
    public TimeScale() {
        listeners = new ArrayList<TimeScaleListener>();

        timeScaleBeginTime = 0;
        timeScaleEndTime = 0;
        timeScaleMsPerPixel = 10f;
    }

    /**
     * Returns the begin time of the time scale in milliseconds.
     *
     * @return the begin time
     */
    public long getBeginTime() {
        return timeScaleBeginTime;
    }

    /**
     * Sets the beginTime of the time scale in milliseconds.
     *
     * @param beginTime the new begin time
     */
    public void setBeginTime(long beginTime) {
        // Only update if needed.
        if (timeScaleBeginTime != beginTime) {
        	try {
        		if (timeScaleLock.tryLock() || timeScaleLock.tryLock(20, TimeUnit.MILLISECONDS)) {
        			try {
		            timeScaleBeginTime = beginTime;
		
		            // Tell all the interested TimeScalelisteners about the change
		            notifyListeners();
        			} finally {
						timeScaleLock.unlock();
					}
        		}
        	} catch (InterruptedException ie) {}
        }
    }

    /**
     * Returns the end time of the time scale in milliseconds.
     *
     * @return the end time
     */
    public long getEndTime() {
        return timeScaleEndTime;
    }

    /**
     * Sets the endTime of the time scale in milliseconds.
     *
     * @param endTime the end time
     */
    public void setEndTime(long endTime) {
        // Only update if needed.
        if (timeScaleEndTime != endTime) {
        	try {
        		if (timeScaleLock.tryLock() || timeScaleLock.tryLock(20, TimeUnit.MILLISECONDS)) {
        			try {
		            timeScaleEndTime = endTime;
		
		            // Tell all the interested TimeScalelisteners about the change
		            notifyListeners();
					} finally {
						timeScaleLock.unlock();
					}
        		}
        	} catch (InterruptedException ie) {}
        }
    }

    /**
     * Returns the duration of the visible interval in the time scale in 
     * milliseconds.
     *
     * @return the duration of the visible interval
     */
    public long getIntervalDuration() {
        return timeScaleEndTime - timeScaleBeginTime;
    }

    /**
     * Returns the number of milliseconds that correspond to one pixel in this
     * time scale.
     *
     * @return the number of milliseconds per pixel
     */
    public float getMsPerPixel() {
        return timeScaleMsPerPixel;
    }

    /**
     * Sets the number of milliseconds corresponding to one pixel in this time scale..
     *
     * @param msPerPixel the number of milliseconds per pixel
     */
    public void setMsPerPixel(float msPerPixel) {
        // Only update if needed.
        if (timeScaleMsPerPixel != msPerPixel) {
            timeScaleMsPerPixel = msPerPixel;

            // Tell all the interested TimeScalelisteners about the change
            notifyListeners();
        }
    }

    /**
     * Add a listener for {@code TimeScale} events.
     *
     * @param listener the listener that wants to be notified of {@code TimeScale}
     *        events.
     */
    public void addTimeScaleListener(TimeScaleListener listener) {
        listeners.add(listener);
        listener.updateTimeScale();
    }

    /**
     * Remove a listener for {@code TimeScale} events.
     *
     * @param listener the listener that no longer wants to be notified
     *        {@code TimeScale} events
     */
    public void removeTimeScaleListener(TimeScaleListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies listeners of a change in the {@code TimeScale}.
     */
    public void notifyListeners() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).updateTimeScale();
        }
    }
}
