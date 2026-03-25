package mpi.eudico.client.annotator.player;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.ControllerManager;
import mpi.eudico.client.mediacontrol.PeriodicUpdateController;
import mpi.eudico.client.mediacontrol.TimeEvent;


/**
 * The Empty implementation of an ELAN media player i.e. a MediaPlayer without
 * media.
 */
public class EmptyMediaPlayer extends ControllerManager
    implements ElanMediaPlayer, ControllerListener {
	private ElanLayoutManager layoutManager;
    private long mediaTime;
    private long offset;
    private float rate;
    private float volume;
	private float curSubVolume;
	private boolean mute;
    private boolean playing;
    private double milliSecondsPerSample;
    private long duration;
    private long startTimeMillis;
    private boolean playingInterval;
    private PeriodicUpdateController periodicController;
    private long intervalStopTime;
    
	/** if true frame forward and frame backward always jump to the begin
	 * of the next/previous frame, otherwise it jumps with the frame duration */
	private boolean frameStepsToFrameBegin = false;
	/**
	 * Specifies a minimal duration for empty players.
	 */
	private final long MIN_DURATION = 5 * 60 * 1000L;

    /**
     * Creates an empty media player instance.
     *
     * @param duration the duration of the player
     */
    public EmptyMediaPlayer(long duration) {
        this.duration = Math.max(MIN_DURATION, duration);
        offset = 0;
        volume = 1;
        rate = 1;
        milliSecondsPerSample = 40d;
    }

    /**
     * Returns {@code null}, there is no media file to play.
     * 
     * @return {@code null}
     */
	public mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor getMediaDescriptor() {
    	return null;
    }
    
    /**
     * Returns the name of this player, "Empty Media Player".
     *
     * @return the string "Empty Media Player"
     */
    @Override
	public String getFrameworkDescription() {
        return "Empty Media Player";
    }

    /**
     * Notification of a controller update.
     * Here used to stop at the stop time in combination with
     * the playInterval method.
     *
     * @param event the controller event, only {@code TimeEvent}s have an
     * effect
     */
    @Override
	public synchronized void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent) {
            if (periodicController != null) {
                if (getMediaTime() >= intervalStopTime) {
                    // stop the player
                    stop();
                }
            }
        }
    }

    /**
     * Plays a segment between two media times. This method uses the 
     * {@link #controllerUpdate(ControllerEvent)} method to
     * detect if the stop time is passed.
     *
     * @param startTime the start time (the from time)
     * @param stopTime the stop time (the to time)
     */
    @Override
	public synchronized void playInterval(long startTime, long stopTime) {
        if (playingInterval || (stopTime <= startTime)) {
            return;
        }

        periodicController = new PeriodicUpdateController(10);
        periodicController.addControllerListener(this);
        addController(periodicController);
        intervalStopTime = stopTime;
        setMediaTime(startTime);
        playingInterval = true;
        start();
    }
    
    /**
     * Empty implementation.
     * Only useful for a player that correctly supports setting the stop time.
     */
    @Override
	public void setStopTime(long stopTime) {
    	
    }

    /**
     * Disable all code for interval playing
     */
    private void stopPlayingInterval() {
        if (periodicController != null) {
            periodicController.removeControllerListener(this);
            removeController(periodicController);
            periodicController = null;
        }

        playingInterval = false;
    }

    /**
     * This player has no display Component.
     *
     * @return {@code null}
     */
    @Override
	public java.awt.Component getVisualComponent() {
        return null; //visualComponent;
    }

    /**
     * 
     * @return 0
     */
    @Override
	public int getSourceHeight() {
        return 0;
    }
    
    /**
     * @return 0
     */
    @Override
	public int getSourceWidth() {
        return 0;
    }
    
    /**
     * Returns a virtual aspect ratio.  
     *
     * @return 1.0
     */
    @Override
	public float getAspectRatio() {
        return 1.0f;
    }

    /**
     * Stub, empty implementation.
     * 
     * @param aspectRatio the new aspect ratio
     */
    @Override
	public void setAspectRatio(float aspectRatio){
    	// stub
    }
    
    /**
     * Starts the player as soon as possible and starts the controllers.
     */
    @Override
	public synchronized void start() {
        playing = true;

        startTimeMillis = System.currentTimeMillis();

        // make sure all managed controllers are started
        startControllers();
    }

    /**
     * Stops the media player and stops the controllers.
     */
    @Override
	public synchronized void stop() {
        if (playing) {
        	if (rate == 1) {
        		mediaTime += (System.currentTimeMillis() - startTimeMillis);
        	} else {
        		float advance = (System.currentTimeMillis() - startTimeMillis) * rate;
        		mediaTime += (long) advance;
        	}
            
        }

        playing = false;

        // make sure all managed controllers are stopped
        stopControllers();

        // make sure that all interval playing is finished
        if (playingInterval) {
        	if (getMediaTime() > intervalStopTime) {
        		setMediaTime(intervalStopTime);
        	}
            stopPlayingInterval();
        }
    }

    /**
     * Returns whether this player is playing.
     *
     * @return {@code true} if the player is playing
     */
    @Override
	public synchronized boolean isPlaying() {
        return playing;
    }

    /**
     * @return {@code true} if the player is playing a selection
     */
    @Override
	public synchronized boolean isPlayingInterval() {
		return playingInterval;
	}

	/**
     * Returns the "frame duration", the number of milliseconds per sample.
     *
     * @return the step size for one frame, defaults to 40 ms. If a custom
     * value has been set, this is read and applied when the LayoutManager 
     * is set.
     */
    @Override
	public double getMilliSecondsPerSample() {
        return milliSecondsPerSample;
    }

    /**
     * Sets the frame duration, the number of milliseconds per sample.
     *
     * @param milliSeconds the step size for one frame
     */
    @Override
	public void setMilliSecondsPerSample(double milliSeconds) {
        milliSecondsPerSample = milliSeconds;
		if (layoutManager != null) {
			layoutManager.setPreference("MediaPlayer.CustomFrameDuration", 
					Double.valueOf(milliSeconds), 
					layoutManager.getViewerManager().getTranscription());
		}
    }

    /**
     * Set the offset to be used in get and set media time for this player.
     *
     * @param offset the offset in milliseconds
     */
    @Override
	public void setOffset(long offset) {
        this.offset = offset;
    }

    /**
     *
     * @return the offset used by this player
     */
    @Override
	public long getOffset() {
        return offset;
    }

    /**
     * Gets this Clock's current media time in milliseconds.
     *
     * @return the current media time
     */
    @Override
	public synchronized long getMediaTime() {       
        if (playing) {
        	if (rate == 1) {
        		//System.out.println("Eget mt play: " + ((mediaTime + System.currentTimeMillis()) - startTimeMillis));
        		return (mediaTime + System.currentTimeMillis()) - startTimeMillis;
        	} else {
        		float advance = (System.currentTimeMillis() - startTimeMillis) * rate;
        		return mediaTime + (long) advance;
        	}
        }
        //System.out.println("Eget mt : " + (mediaTime - offset) + "  " + playing);
        return mediaTime - offset;
    }

    /**
     * Sets the Clock's media time in milliseconds.
     *
     * @param time the media time
     */
    @Override
	public synchronized void setMediaTime(long time) {
        //System.out.println("Eset mt : " + (time + offset));	
        mediaTime = time + offset;
        setControllersMediaTime(time);
    }

    @Override
	public void nextFrame() {
    	if (frameStepsToFrameBegin) {
    		long curFrame = (long) (getMediaTime() / milliSecondsPerSample);
    		setMediaTime((long) Math.ceil(((curFrame + 1) * milliSecondsPerSample)));
    	} else {
    		setMediaTime((long) Math.ceil((getMediaTime() + milliSecondsPerSample)));
    	}
    }
    
    @Override
	public void previousFrame() {
    	if (frameStepsToFrameBegin) {
    		long curFrame = (long) (getMediaTime() / milliSecondsPerSample);
    		if (curFrame > 0) {
    			setMediaTime((long) Math.ceil(((curFrame - 1) * milliSecondsPerSample)));
    		} else {
    			setMediaTime(0);
    		}
    	} else {
    		setMediaTime((long) Math.ceil((getMediaTime() - milliSecondsPerSample)));
    	}
    }

    @Override
	public void setFrameStepsToFrameBegin(boolean stepsToFrameBegin) {
    	frameStepsToFrameBegin = stepsToFrameBegin;
    }
    
    /**
     * Gets the current playback rate.
     *
     * @return the playback rate
     */
    @Override
	public synchronized float getRate() {
        return rate;
    }

    /**
     * Sets the playback rate.
     *
     * @param rate the new playback rate
     */
    @Override
	public synchronized void setRate(float rate) {
        this.rate = rate;
    }

    /**
     * @return {@code false}
     */
    @Override
	public boolean isFrameRateAutoDetected() {
        return false;
    }
    
    /**
     * Gets the volume as a number between 0 and 1
     *
     * @return the volume level
     */
    @Override
	public float getVolume() {
        return volume;
    }

    /**
     * Sets the volume as a number between 0 and 1.
     *
     * @param level the new volume level
     */
    @Override
	public void setVolume(float level) {
        volume = level;
    }

    @Override
    public void setSubVolume(float level) {
    	curSubVolume = level;
    }
    
    @Override
    public float getSubVolume(){
    	return curSubVolume;
    }
    
    @Override
    public void setMute(boolean mute) {
    	this.mute = mute;
    }
    
    @Override
    public boolean getMute() {
    	return mute;
    }
    
   /**
     * Stub, empty implementation.
     *
     */
    @Override
	public void setLayoutManager(ElanLayoutManager layoutManager) {
    	this.layoutManager = layoutManager;
		if (layoutManager != null) {
			Double prefFrameDur = Preferences.getDouble(
				"MediaPlayer.CustomFrameDuration", 
				layoutManager.getViewerManager().getTranscription());
			if (prefFrameDur != null) {
				milliSecondsPerSample = prefFrameDur.doubleValue();
			}
		}
    }

    /**
     * Stub, empty implementation.
     */
    @Override
	public void updateLocale() {
    }

    /**
     * Get the duration of the media represented by this object in milliseconds.
     *
     * @return the current duration of the player
     */
    @Override
	public long getMediaDuration() {
        return duration;
    }
    
    /**
     * Set the duration of the media represented by this object in milliseconds.
     * The minimum duration is enforced.
     * 
     * @param dur the new duration
     */
    public void setMediaDuration(long dur) {
        duration = Math.max(MIN_DURATION, dur);
    }

	@Override
	public void cleanUpOnClose() {
		layoutManager = null;
	}

	@Override
	public void preferencesChanged() {
		// stub
	}
}
