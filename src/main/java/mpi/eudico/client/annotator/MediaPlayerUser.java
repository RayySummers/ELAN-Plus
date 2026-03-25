package mpi.eudico.client.annotator;

import mpi.eudico.client.annotator.player.*;

/**
 * Interface that defines MediaPlayerUser methods.
 * Allows implementers, e.g viewers, to interact with the media player.
 */
public interface MediaPlayerUser {
    /**
     * Sets the player for the user to interact with.
     *
     * @param player the player
     */
    public void setPlayer(ElanMediaPlayer player);

    /**
     * A request to start the player.
     */
    public void startPlayer();

    /**
     * A request to stop the player.
     */
    public void stopPlayer();

    /**
     * Returns whether the player is currently playing.
     *
     * @return {@code true} if the player is playing
     */
    public boolean playerIsPlaying();

    /**
     * Starts playing an interval, a time selection.
     *
     * @param startTime the interval start time
     * @param stopTime the interval end time
     */
    public void playInterval(long startTime, long stopTime);

    /**
     * Sets the media time of the player.
     *
     * @param milliSeconds the time in millisecond
     */
    public void setMediaTime(long milliSeconds);

    /**
     * Returns the current media time.
     *
     * @return the current media time in milliseconds
     */
    public long getMediaTime();

    /**
     * Sets the playback rate of the player.
     * The value of {@code 1} represents the normal speed, a value {@code <1}
     * means slow motion and a value {@code >1} means fast forward.
     *
     * @param rate the playback rate
     */
    public void setRate(float rate);

    /**
     * Returns the current playback rate.
     *
     * @return the playback rate
     */
    public float getRate();

    /**
     * Returns the current volume setting of the player.
     *
     * @return the current volume
     */
    public float getVolume();

    /**
     * Sets the audio volume of the player.
     *
     * @param level the volume level
     */
    public void setVolume(float level);

    /**
     * Returns the duration of the media file. 
     *
     * @return the media duration
     */
    public long getMediaDuration();
    
    /**
     * Notification of the fact that the offset (and therefore the duration)
     * of the media player changed.
     */
    public void mediaOffsetChanged();
}
