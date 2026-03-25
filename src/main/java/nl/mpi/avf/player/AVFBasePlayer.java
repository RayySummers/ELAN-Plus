package nl.mpi.avf.player;

import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A base class a media player based on the macOS native AudioVideo Foundation;
 * it creates an AVPlayer for the sound (the audio track) and the presentation clock. 
 * For video this class is extended by two classes, one for rendering of the video in Java,
 * one that adds a native player layer to a Java component.
 * 
 * This base player contains almost everything the actual players need, the "id" which is used to 
 * connect to the correct native player (more than one player instance can exist). 
 * Almost all methods in this class have a private native counterpart method which takes 
 * the "id" as an argument.   
 * 
 * @author Han Sloetjes
 * 
 * @see AVFNativePlayer
 * @see JAVFPlayer
 */
public class AVFBasePlayer {
	final static Logger LOG = Logger.getLogger("NativeLogger");
	/** the id  of the native player, the key for retrieval of the player from a map */
	long id;
	String mediaPath;
	static boolean nativeLibLoaded = false;
	static boolean nativeLogLoaded = false;
	// it would be better to use an enum for these status related constants although
	// that would make the JNI a bit more complicated
	final int STATUS_UNKNOWN = 0;
	final int STATUS_READY = 1;
	final int STATUS_FAILED = 2;
	final int MAX_LOAD_TIME = 15000;
	// load library block
	static {
		try {
			System.loadLibrary("JNIUtil");
			nativeLogLoaded = true;
		} catch (UnsatisfiedLinkError ule) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Could not load native utility library (libJNIUtil.dylib): " + ule.getMessage());
			}
		} catch (Throwable t) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Could not load native utility library (libJNIUtil.dylib): " + t.getMessage());
			}
		}
		try {
			System.loadLibrary("AVFPlayer");
			nativeLibLoaded = true;
		} catch (SecurityException se) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Could not load native library (libAVFPlayer.dylib): " + se.getMessage());
			}
		} catch (UnsatisfiedLinkError ule) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Could not load native library (libAVFPlayer.dylib): " + ule.getMessage());
			}
		} catch (Throwable t) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Could not load native library (libAVFPlayer.dylib): " + t.getMessage());
			}
		}
		// configure native logging
		if (nativeLogLoaded && nativeLibLoaded) {
			if (LOG.getLevel() != null) {
				AVFBasePlayer.setLogLevel(LOG.getLevel());
			} else if (LOG.getParent().getLevel() != null) {
				LOG.setLevel(LOG.getParent().getLevel());
				AVFBasePlayer.setLogLevel(LOG.getParent().getLevel());
			}
			
			AVFBasePlayer.initLog("nl/mpi/jni/NativeLogger", "nlog");
		}
	}
	
	// cache a few fields that are unlikely to change during the lifetime of this player
	private double encFrameRate = 0d;
	private double encTimePerFrame = 0d;
	private Dimension encVideoSize = null;
	// media duration
	// aspect ratio
	// has video, has audio
	// end cached fields
	
	/**
	 * Constructor with media path
	 * 
	 * @param mediaPath the path to a media source
	 * @throws JAVFPlayerException any exception that can occur when creating a native player
	 */
	public AVFBasePlayer(String mediaPath) throws JAVFPlayerException {
		super();
		this.mediaPath = mediaPath;
		if (mediaPath.startsWith("file:///")) {
			this.mediaPath = mediaPath.substring(5);
		}

		id = initPlayer(this.mediaPath);
		//System.out.println("Id for player: " + id);
		
		if (id <= 0) {
			// throw exception			
			throw new JAVFPlayerException("Failed to create a native AVPlayer");
		} else {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, String.format("New player created, ID: %d for path %s", id, this.mediaPath));
			}
		}
		
	}

	/**
	 * Deletes the native player, resets the id.
	 */
	public void deletePlayer() {
		deletePlayer(id);
		// make sure no more calls are made to this player
		id = -id;
	}
	
	/**
	 * Start the player.
	 */
	public void start() {
		if (id <= 0) {
			return;
		}
		start(id);
	}
	
	/**
	 * Stops the player; in practice this is equivalent to pausing the player.
	 */
	public void stop() {
		if (id <= 0) {
			return;
		}
		stop(id);
	}
	
	/**
	 * Pauses the player.
	 */
	public void pause() {
		if (id <= 0) {
			return;
		}
		pause(id);
	}
	
	/**
	 * Returns whether the player is playing.
	 *  
	 * @return true if the player is playing, false otherwise. 
	 * The (native) player is considered to be playing if the playback rate is &gt; 0
	 */
	public boolean isPlaying() {
		if (id <= 0) {
			return false;
		}
		return isPlaying(id);
	}
	
	/**
	 * Sets the playback rate.
	 * 
	 * @param rate the new playback rate
	 */
	public void setRate(float rate) {
		if (id <= 0) {
			return;
		}
		setRate(id, rate);
	}
	
	/**
	 * Returns the current play back rate.
	 * 
	 * @return the current play back rate (the rate as it was set)
	 */
	public float getRate() {
		if (id <= 0) {
			return 0.0f;
		}
		return getRate(id);
	}
	
	/**
	 * Sets the volume of the audio, a value between 0 and 1
	 * 
	 * @param volume the new volume
	 */
	public void setVolume(float volume) {
		if (id <= 0) {
			return;
		}
		setVolume(id, volume);
	}
	
	/**
	 * Returns the audio volume setting.
	 * 
	 * @return the current audio volume of the player, between 0 and 1
	 */
	public float getVolume() {
		if (id <= 0) {
			return 0.0f;
		}
		return getVolume(id);
	}
	
	/**
	 * Returns the current media time.
	 * 
	 * @return the current media time in milliseconds (rounded to a long value)
	 */
	public long getMediaTime() {
		if (id <= 0) {
			return 0L;
		}
		return getMediaTime(id);
	}
	
	/**
	 * Returns the current media time in seconds.
	 * 
	 * @return the current media time in seconds (as a floating point value)
	 */
	public double getMediaTimeSeconds() {
		if (id <= 0) {
			return 0.0d;
		}
		return getMediaTimeSeconds(id);
	}
	
	/**
	 * Requests the player to position the time pointer (the presentation clock) 
	 * to a new position.
	 * @param time the time to jump to, in milliseconds
	 */
	public void setMediaTime(long time) {
		if (id <= 0) {
			return;
		}
		setMediaTime(id, time);
	}
	
	/**
	 * Requests the player to position the time pointer (the presentation clock) 
	 * to a new position.
	 * @param time the time to jump to, in seconds
	 */
	public void setMediaTimeSeconds(double time) {
		if (id <= 0) {
			return;
		}
		setMediaTimeSeconds(id, time);
	}
	
	/**
	 * Sets the stop time for a play selection action. The native player 
	 * framework observes the media time and stops the player when the stop 
	 * time is reached. 
	 * (Experimental)
	 * 
	 * @param stopTime the end time of the interval to play in milliseconds
	 */
	public void setStopTime(long stopTime) {
		if (id <= 0) {
			return;
		}
		setStopTime(id, stopTime);
	}
	
	/**
	 * Sets the stop time for a play selection action. The native player 
	 * framework observes the media time and stops the player when the stop 
	 * time is reached.
	 * (Experimental)
	 * 
	 * @param stopTimeSec the end time of the interval to play in seconds
	 */
	public void setStopTimeSeconds(double stopTimeSec) {
		if (id <= 0) {
			return;
		}
		setStopTimeSeconds(id, stopTimeSec);
	}
	
	/**
	 * Removes the stop time from the native player.
	 */
	public void removeStopTime() {
		if (id <= 0) {
			return;
		}
		removeStopTime(id);
	}
	
	/**
	 * Returns whether the media source has a video track.
	 * 
	 * @return true if at least one video track was found in the media file, 
	 * false otherwise
	 */
	public boolean hasVideo() {
		if (id <= 0) {
			return false;
		}
		return hasVideo(id);
	}
	
	/**
	 * Returns whether the media source has an audio track.
	 * 
	 * @return true if at least one audio track was found in the media file,
	 * false otherwise
	 */
	public boolean hasAudio() {
		if (id <= 0) {
			return false;
		}
		return hasAudio(id);
	}
	
	/**
	 * Returns the media duration in milliseconds.
	 * 
	 * @return the media duration in milliseconds
	 */
	public long getDuration() {
		if (id <= 0) {
			return 0L;
		}
		return getDuration(id);
	}
	
	/**
	 * Returns the media duration in seconds.
	 * 
	 * @return the media duration in seconds
	 */
	public double getDurationSeconds() {
		if (id <= 0) {
			return 0.0d;
		}
		return getDurationSeconds(id);
	}
	
	/**
	 * Returns the encoded frames per second value.
	 * 
	 * @return the encoded frame rate (number of frames per second)
	 * @see #getTimePerFrame()
	 */
	public double getFrameRate() {
		if (id <= 0) {
			return 0.0d;
		}
		if (encFrameRate == 0) {
			encFrameRate = getFrameRate(id);
		}
		return encFrameRate;
	}
	
	/**
	 * Returns the frame duration in milliseconds.
	 * 
	 * @return the encoded duration of a single video frame in milliseconds
	 * @see #getFrameRate()
	 */
	public double getTimePerFrame() {
		if (id <= 0) {
			return 0.0d;
		}
		if (encTimePerFrame == 0) {
			encTimePerFrame = getTimePerFrame(id);
		}
		return encTimePerFrame;
	}
	
	// if has video, information about first video track, video frames
	/**
	 * Returns the encoded aspect ratio of the video.
	 * 
	 * @return the aspect ratio of the video, based on the encoded dimension
	 */
	public float getAspectRatio() {
		if (id <= 0) {
			return 0.0f;
		}
		return getAspectRatio(id);
	}
	
	/**
	 * Returns the encoded size of the video images.
	 * 
	 * @return the encoded dimension of the video images
	 */
	public Dimension getOriginalSize() {
		if (id < 0) {
			return new Dimension(0, 0);
		}
		if (encVideoSize == null) {
			encVideoSize = getOriginalSize(id);
		}
		return encVideoSize;
	}
	
	/**
	 * Informs the native player of a change in the video scale factor.
	 * If the value is 1, the video normally fills the entire area of 
	 * the video panel (respecting aspect ratio settings).
	 * 
	 * @param scale the video scaling factor, 1 of no scaling is applied
	 */
	public void setVideoScaleFactor(float scale) {
		// to be implemented by subclasses
	}

	/**
	 * If the video is scaled and only a sub-region of the video image can be 
	 * shown on/by the video panel, the x and y value determine the horizontal
	 * and vertical displacement of the image. w and h are calculated based 
	 * on panel size and video scale factor.
	 * (x, y) are the coordinates of the left top corner of the bounds/rectangle.
	 * In native code this might have too be translated/recalculated into the
	 * coordinates of the lower left corner.  
	 * 
	 * @param x the x coordinate of the top left corner of the bounds 
	 * @param y the y coordinate of the top left corner of the bounds
	 * @param w the width of the bounds (i.e. of the video image)
	 * @param h the height of the bounds (i.e of the video image)
	 */
	public void setVideoBounds(int x, int y, int w, int h) {
		// to be implemented by subclasses
	}
	
	/**
	 * Returns the current video bounds.
	 * 
	 * @return the current position and size of the scaled video image 
	 * relative to the video display panel
	 */
	public int[] getVideoBounds() {
		return null;
	}
	
	/**
	 * Try to force the video to repaint/display itself.
	 */
	public void repaintVideo() {
		// to be implemented by subclasses
	}
	
	/**
	 * A shorthand to set the logging level to {@code FINE} or below or 
	 * {@code INFO} or above.
	 * 
	 * @param enable if {@code true} the level will be set to {@code FINE},
	 * unless the level is already lower, if {@code false} the level will be
	 * set to {@code INFO}, unless the level is already higher
	 */
	public static void enableDebugMode(boolean enable) {
		int curLevel = AVFBasePlayer.getJAVFLogLevel();
		if (enable) {
			if (curLevel > JAVFLogLevel.FINE) {
				AVFBasePlayer.setJAVFLogLevel(JAVFLogLevel.FINE);
			}
		} else {
			if (curLevel < JAVFLogLevel.INFO) {
				AVFBasePlayer.setJAVFLogLevel(JAVFLogLevel.INFO);
			}
		}
	}
	
	/**
	 * Maps a {@link Level} to one of the <code>JAVFLogLevel</code> levels
	 * 
	 * @param level the new logging level
	 */
	public static void setLogLevel(Level level) {
		if (level == null) {
			return;
		}
		if (level == Level.ALL) {
			setJAVFLogLevel(JAVFLogLevel.ALL);
		} else if (level == Level.OFF) {
			setJAVFLogLevel(JAVFLogLevel.OFF);
		} else if (level.intValue() > Level.ALL.intValue() && level.intValue() <= Level.FINE.intValue()) {
			setJAVFLogLevel(JAVFLogLevel.FINE);
		} else if (level.intValue() > Level.FINE.intValue() && level.intValue() <= Level.INFO.intValue()) {
			setJAVFLogLevel(JAVFLogLevel.INFO);
		} else if (level.intValue() > Level.INFO.intValue() && level.intValue() <= Level.WARNING.intValue()) {
			setJAVFLogLevel(JAVFLogLevel.WARNING);
		} else if (level.intValue() > Level.WARNING.intValue()) {
			setJAVFLogLevel(JAVFLogLevel.OFF);
		}
	}
	
// #####  native methods, mostly package private #####
	/**
	 * @param level the <code>level</code> of one of the JAVFLogLevel constants
	 */
	static native void setJAVFLogLevel(int level);
	/**
	 * @return the current native logging level
	 */
	static native int getJAVFLogLevel();
	
	/**
	 * Informs the native counterpart of the class and the method to use for
	 * logging messages to the Java logger.
	 * 
	 * @param clDescriptor the class descriptor, 
	 * 		e.g. {@code nl/mpi/jni/NativeLog}
	 * 	   
	 * @param methodName the name of the {@code static void} method to call, 
	 * e.g. {@code nlog}, a method which accepts one {@code String}
	 */
	static native void initLog(String clDescriptor, String methodName);
	
	// initializes the native counter part player, returns the id for subsequent calls to the native player
	native long initPlayer(String mediaURL);
	// returns one of the STATUS constants
	native int getPlayerLoadStatus (long id);
	// the error that occurred while creating a native player
	native String getPlayerError(long id);
	// delete all native resources associated with this player
	native void deletePlayer(long id);
	
	native void start(long id);
	native void stop(long id);
	native void pause(long id);
	native boolean isPlaying(long id);
	native int getState(long id); // get player state?
	
	native void setRate(long id, float rate);
	native float getRate(long id);
	native void setVolume(long id, float volume);
	native float getVolume(long id);
	
	native long getMediaTime(long id);
	native double getMediaTimeSeconds(long id);
	native void setMediaTime(long id, long time);
	native void setMediaTimeSeconds(long id, double time);
	native void setStopTime(long id, long time);
	native void setStopTimeSeconds(long id, double stopTime);
	native void removeStopTime(long id);
	
	native boolean hasVideo(long id);
	native boolean hasAudio(long id);
	native long getDuration(long id);
	native double getDurationSeconds(long id);
	native double getFrameRate(long id);
	native double getTimePerFrame(long id);
	
	// if has video, information about first video track, video frames
	native float getAspectRatio(long id);
	native Dimension getOriginalSize(long id);


}
