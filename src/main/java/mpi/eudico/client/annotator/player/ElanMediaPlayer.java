package mpi.eudico.client.annotator.player;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.PreferencesListener;
import mpi.eudico.client.mediacontrol.Controller;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

/**
 * Interface for ELAN media players.
 */
public interface ElanMediaPlayer extends Controller, ElanLocaleListener, PreferencesListener {
	/**
	 * Returns the media descriptor of this player.
	 * 
	 * @return the media descriptor
	 */
	public MediaDescriptor getMediaDescriptor();
    /**
     * Starts the media player.
     */
    @Override
	public void start();

    /**
     * Stops/pauses the media player.
     * The {@code ElanMediaPlayer} interface does not distinguish between pause
     * and stop, though native media players often do. This {@code stop} method
     * is probably equivalent to the {@code pause} function of most players.
     */
    @Override
	public void stop();

    /**
     * Returns whether the media player is playing.
     *
     * @return {@code true} if the player is playing, {@code false} otherwise
     */
    public boolean isPlaying();

    /**
     * Plays back an interval, a selected segment of the media file. 
     *
     * @param startTime the start time of the interval, in milliseconds
     * @param stopTime the stop time of the interval, in milliseconds
     */
    public void playInterval(long startTime, long stopTime);
    
    /**
     * Returns whether the player is (still) playing an interval (a selection).
     * This can be used to test if playing a selection has completely finished. 
     *
     * @return true if the player is currently playing an interval
     */
    public boolean isPlayingInterval();

    /**
     * Sets the stop time for the player.
     * 
     * @param stopTime the media time where the player should stop
     */
    @Override
	public void setStopTime(long stopTime);
    
    /**
     * Sets the new {@code point zero} or starting point of the media, in 
     * milliseconds.
     * Should be a positive value, so that a part of the media is skipped.
     *
     * @param offset the new starting point
     */
    public void setOffset(long offset);

    /**
     * Returns the offset that has been determined after synchronisation with other media; 
     * the new virtual point zero.
     *
     * @return the value of the new starting point, in milliseconds
     */
    public long getOffset();

    /**
     * Moves the media player's position to the next frame, frame forward.
     * The behaviour is undefined for audio only files.
     */
    public void nextFrame();
    
    /**
     * Moves the media player to the previous frame, frame backward.
     * The behaviour is undefined for audio only files.
     */
    public void previousFrame();
    
    /**
     * Sets the flag that determines whether frame forward/backward jumps with 
     * the number of ms. of the frame duration or jumps to the beginning of
     * the next or previous frame.
     * 
     * @param stepsToFrameBegin if true frame forward/backward jumps to begin of 
     * next or previous frame, otherwise it jumps with a fixed number of ms., 
     * the same as the frame duration
     */
    public void setFrameStepsToFrameBegin(boolean stepsToFrameBegin);
    
    /**
     * Sets the media time for the player, requests the player to jump to that
     * position.
     *
     * @param time the new media time for the player
     */
    @Override
	public void setMediaTime(long time);

    /**
     * Returns the current media time.
     *
     * @return the media time
     */
    public long getMediaTime();

    /**
     * Sets the playback rate.
     * {@code 1.0} indicates normal speed, {@code < 1.0} is slow motion, 
     * {@code > 1.0} is fast playback.
     *
     * @param rate the playback rate
     */
    @Override
	public void setRate(float rate);

    /**
     * Returns the current playback rate.
     *
     * @return the current playback rate
     */
    public float getRate();
    
    /**
     * Returns whether or not the framework has successfully detected the encoded framerate and thus 
     * the duration per frame.
     * 
     * @return true if the framerate has been detected, false otherwise
     */
    public boolean isFrameRateAutoDetected();

    /**
     * Returns the media duration.
     * If an offset has been set, the media duration is the encoded 
     * duration - offset.
     *
     * @return the media duration
     */
    public long getMediaDuration();

    /**
     * Returns the volume level.
     *
     * @return the current volume
     */
    public float getVolume();

    /**
     * Sets the volume level.
     *
     * @param level the volume, a value between 0 and 1
     */
    public void setVolume(float level);
    
    /**
     * Sets the subvolume as selected by the user
     * through the appropriate slider. The VolumeManager will use the
     * remembered value to set an effective volume.
     * 
     * @param level the subvolume level
     */
    public void setSubVolume(float level);
    
    /**
     * Returns the subvolume, a value set by the user which is used to 
     * determine the effective volume.
     * 
     * @return the subvolume
     */
    public float getSubVolume();
    
    /**
     * Sets the player's muted state.
     * 
     * @param mute if {@code true} the volume level will be set to 0, otherwise
     * the previously set volume will be restored 
     */
    public void setMute(boolean mute);
    
    /**
     * Returns the muted state.
     * 
     * @return the current muted state
     */
    public boolean getMute();

    /**
     * Sets the reference to the/a {@code ElanLayoutManager}.
     *
     * @param layoutManager the {@code ElanLayoutManager}, or {@code null} to
     * remove the reference to the layoutmanager
     */
    public void setLayoutManager(ElanLayoutManager layoutManager);

    /**
     * Returns the visual component of the player.
     * This can be called e.g. to add it to the layout of the application 
     * window.<p>
     * In case of an audio only file usually/currently {@code null}
     * is returned. This could be changed in either returning an icon or
     * something else that provides information on the file.
     *
     * @return the visual component or {@code null}
     */
    public java.awt.Component getVisualComponent();
    
    /**
     * Returns the width in pixels of the media, if it has a visual component.
     * 
     * @return the (video) image width, as encoded in the media file, or as interpreted by the 
     * framework
     */
    public int getSourceWidth();
    
    /**
     * Returns the height in pixels of the media, if it has a visual component.
     * 
     * @return the (video) image height, as encoded in the media file, or as interpreted by the 
     * framework
     */
    public int getSourceHeight();

    /**
     * Returns the aspect ratio (width / height) of the video (if the media file has a video 
     * track).
     * In case of an audio only file the return value is undefined, probably 0 or 1.
     *
     * @return the aspect ratio of the video / visual component
     */
    public float getAspectRatio();
    
    /**
     * Enforces an aspect ratio for the media component.
     * 
     * @param aspectRatio the new aspect ratio
     */
    public void setAspectRatio(float aspectRatio);

    /**
     * Returns the frame or sample duration as encoded in the media file (if possible).
     * HS April 2014 the value is now returned as a double.
     * 
     * @return the sample duration in milliseconds
     */
    public double getMilliSecondsPerSample();

    /**
     * Sets the frame duration in case the framework has not detected the sample duration. 
     * This value determines the number of ms the playhead moves forward or backward when 
     * the frame forward/backward command has been issued.
     *
     * @param milliSeconds the new frame duration
     */
    public void setMilliSecondsPerSample(double milliSeconds); // temporary, player should do this 

    /**
     * Notification of a change in user interface language.
     */
    @Override
	public void updateLocale();

    /**
     * Returns a description of this media framework.
     *
     * @return a framework description
     */
    public String getFrameworkDescription();

    /**
     * Adds a controller.
     *
     * @param controller the controller to adf
     */
    public void addController(Controller controller);

    /**
     * Removes a controller.
     *
     * @param controller the controller to remove
     */
    public void removeController(Controller controller);

    /**
     * Starts the registered controllers.
     */
    public void startControllers();

    /**
     * Stops the registered controllers.
     */
    public void stopControllers();

    /**
     * Notifies registered controllers of the current media time.
     *
     * @param time the (new) current media time
     */
    public void setControllersMediaTime(long time);

    /**
     * Notifies the registered controllers of the current play back rate.
     *
     * @param rate the (new) current rate
     */
    public void setControllersRate(float rate);
    
    /**
     * Opportunity to dispose of objects, close streams etc. to ensure proper
     * garbage collection.
     */
    public void cleanUpOnClose();
}
