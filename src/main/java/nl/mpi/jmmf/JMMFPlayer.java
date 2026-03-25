package nl.mpi.jmmf;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

/**
 * A player that contains methods that mostly wrap a Microsoft Media Foundation
 * function, accessed via JNI.
 * All methods accepting time values expect millisecond values and pass them 
 * to the native function in "reference time", units of 100 nanoseconds.
 * All methods returning time values receive values as "reference time" values 
 * from the native functions and return them as milliseconds.s  
 *  
 * @author Han Sloetjes
 */
public class JMMFPlayer {
	private final static System.Logger LOG = System.getLogger("NativeLogger");
	/** the default time format in Direct Show/MMF is Reference Time, units of 100 nanoseconds */
	private static final int MS_TO_REF_TIME = 10000;
    /** The stopped state */
    public final static int STATE_STOP = 0;

    /** The pause state */
    public final static int STATE_PAUSE = 1;

    /** The running state */
    public final static int STATE_RUN = 2;
    /**
     * An enumeration that corresponds to the player states in the native MMFPlayer.
     */
    public enum PlayerState {
    	/** no session error */
    	NO_SESSION (0),
    	/** ready for play back */
    	READY(1),
    	/** opening a file */
    	OPENING(2),
    	/** the player is started */
    	STARTED(3),
    	/** the player is paused */
    	PAUSED(4),
    	/** the player is seeking a position */
    	SEEKING(5),
    	/** the player is stopped */
    	STOPPED(6),
    	/** the player is closing */
    	CLOSING(7),
    	/** the player is closed */
    	CLOSED(8);
    	// index
    	/** the state index */
    	public final int value;

    	/**
    	 * Constructor with state value index.
    	 * 
    	 * @param value the index of the state
    	 */
    	PlayerState (int value) {
    		this.value = value;
    	}
    }
    
    private static String initError = null;
	private static boolean nativeLogLoaded = false;
	
	static {
		try {
			System.loadLibrary("JNIUtil");
			nativeLogLoaded = true;
		} catch (UnsatisfiedLinkError ule) {
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Could not load native utility library (JNIUtil.dll): " + ule.getMessage());
			}
		} catch (Throwable t) {
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Could not load native utility library (JNIUtil.dll): " + t.getMessage());
			}
		}
		
		try {
			// don't use SystemReporting here
			if (System.getProperty("os.name").indexOf("Vista") > -1) {
				System.loadLibrary("MMFPlayerVista");// somehow on my machine WIn7 reports as Vista
			} else {
				System.loadLibrary("MMFPlayer");// assume win 7 or >
			}
			String debug = System.getProperty("JMMFDebug");
			if (debug != null && debug.toLowerCase().equals("true")) {
				JMMFPlayer.enableDebugMode(true);
			}
			String correctAtPause = System.getProperty("JMMFCorrectAtPause");
			if (correctAtPause != null) {
				if (correctAtPause.toLowerCase().equals("true")) {
					JMMFPlayer.correctAtPause(true);
				} else if (correctAtPause.toLowerCase().equals("false")) {
					JMMFPlayer.correctAtPause(false);	
				}
				
			}
			// changing the native stdout to the log file only works or another file than the ELAN log file
//			String logPath = System.getProperty("nl.mpi.elan.logfile.path");
//			if (logPath != null) {
//				JMMFPlayer.setLogFile(logPath); // logPath + "n"
//			}
		} catch (UnsatisfiedLinkError ue) {
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Could not load native player library (MMFPlayer.dll): " + ue.getMessage());
			}
			initError = ue.getMessage();
		} catch (Throwable th) {
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Could not load native player library (MMFPlayer.dll): " + th.getMessage());
			}
			initError = th.getMessage();
		}
		
		if (nativeLogLoaded) {
			try {
				JMMFPlayer.initLog("nl/mpi/jni/NativeLogger", "nlog");
			} catch (Throwable t) {
				if (LOG.isLoggable(System.Logger.Level.WARNING)) {
					LOG.log(System.Logger.Level.WARNING, "Error while configuring native logging: " + t.getMessage());
				}
			}
		}
	}
	
	private String mediaPath;
	//private URL mediaURL;
	private long id = -1;// not initialized
	private Component visualComponent;
	// initialize as true in order to be sure it will be tried at least once
	private boolean stopTimeSupported = true;
	private boolean allowVideoScaling = true;
	private float videoScaleFactor = 1f;
	private int vx = 0, vy = 0, vw = 0, vh = 0;
	private int vdx = 0, vdy = 0;
	// in synchronous mode all calls that "normally" are performed asynchronous
	// are performed synchronous. These methods only return after the action has 
	// fully been performed
	private boolean synchronousMode = false;// temporarily true
	
	/**
	 * Creates a new player instance.
	 * 
	 * @throws JMMFException if an exception occurs while creating the player
	 */
	public JMMFPlayer() throws JMMFException {
		super();
		
		if (initError != null) {
			throw new JMMFException(initError);
		}
		//id = initWithFileAndOwner(this.mediaPath, this.visualComponent);
		id = initPlayer(synchronousMode);
		
		if (id > 0) {
			if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
				LOG.log(System.Logger.Level.DEBUG, "The native JMMFPlayer was initialized successfully");
			}
		} else {
			// failure to initialize
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "The native JMMFPlayer could not be initialized");
			}
			throw new JMMFException("The native JMMFPlayer could not be initialized.");
		}
	}
	
	/**
	 * Constructor with a flag for synchronous or asynchronous mode of operation.
	 * 
	 * @param synchronous if {@code true} the player runs in synchronous mode, 
	 * in asynchronous otherwise 
	 * @throws JMMFException any exception that can occur when creating a native
	 * media player
	 */
	public JMMFPlayer(boolean synchronous) throws JMMFException {
		super();
		
		if (initError != null) {
			throw new JMMFException(initError);
		}
		synchronousMode = synchronous;
		//id = initWithFileAndOwner(this.mediaPath, this.visualComponent);
		id = initPlayer(synchronousMode);
		
		if (id > 0) {
			if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
				LOG.log(System.Logger.Level.DEBUG, String.format("The native JMMFPlayer was initialized in %s mode successfully", 
						synchronous ? "synchronous" : "asynchronous"));
			}
		} else {
			// failure to initialize
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, String.format("The native JMMFPlayer could not be initialized (in %s mode)", 
						synchronous ? "synchronous" : "asynchronous"));
			}
			throw new JMMFException(String.format("The native JMMFPlayer could not be initialized (in %s mode)", 
					synchronous ? "synchronous" : "asynchronous"));
		}
	}

	/**
	 * Constructor with the media location as parameter.
	 * 
	 * @param mediaPath the location of the media to load
	 * @throws JMMFException any exception that can occur when creating a native media player
	 */
	public JMMFPlayer(String mediaPath) throws JMMFException {
		super();
		if (initError != null) {
			throw new JMMFException(initError);
		}
		//check path
		this.mediaPath = mediaPath;
		
		id = initWithFile(this.mediaPath, synchronousMode);
		
		if (id > 0) {
			if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
				LOG.log(System.Logger.Level.DEBUG, String.format("The native JMMFPlayer was initialized successfully for %s", mediaPath));
			}
		} else {
			// failure to initialize
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, String.format("The native JMMFPlayer could not be initialized for %s", mediaPath));
			}
			throw new JMMFException(String.format("The native JMMFPlayer could not be initialized for %s", mediaPath));
		}
	}

	/**
	 * Constructor with media path and flag for player mode, synchronous or asynchronous.
	 * 
	 * @param mediaPath the location of the media file
	 * @param synchronous if true the player will operate in synchronous mode, 
	 * asynchronous otherwise
	 * @throws JMMFException any exception that can occur when creating a native media player
	 */
	public JMMFPlayer(String mediaPath, boolean synchronous) throws JMMFException {
		super();
		if (initError != null) {
			throw new JMMFException(initError);
		}
		//check path
		this.mediaPath = mediaPath;
		synchronousMode = synchronous;
		
		id = initWithFile(this.mediaPath, synchronousMode);

		if (id > 0) {
			if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
				LOG.log(System.Logger.Level.DEBUG, String.format("The native JMMFPlayer was initialized successfully for %s in %s mode", 
						mediaPath, synchronous ? "synchronous" : "asynchronous"));
			}
		} else {
			// failure to initialize
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, String.format("The native JMMFPlayer could not be initialized for %s (in %s mode)", 
						mediaPath, synchronous ? "synchronous" : "asynchronous"));
			}
			throw new JMMFException(String.format("The native JMMFPlayer could not be initialized for %s (in %s mode)", 
					mediaPath, synchronous ? "synchronous" : "asynchronous"));
		}
	}
	
//	public JMMFPlayer(URL mediaURL) throws JMMFException {
//		super();
//		this.mediaURL = mediaURL;
//	}
	/**
	 * Returns whether the player is in synchronous interaction mode.
	 * 
	 * @return {@code true} if the player is in synchronous mode, {@code false}
	 * if it runs in asynchronous mode 
	 */
	public boolean isSynchronousMode() {
		return synchronousMode;
	}

	/**
	 * Starts the player.
	 */
	public void start() {
		start(id);
	}
	
	/**
	 * In most cases stop means pause.
	 */
	public void stop() {
		pause(id);
	}
	
	/**
	 * Pauses the player.
	 */
	public void pause() {
		pause(id);
	}
	
	/**
	 * Returns whether the player is playing, in the {@code STARTED} state.
	 * 
	 * @return {@code true} if the player is playing, {@code false} otherwise
	 */
	public boolean isPlaying() {
		return isPlaying(id);
	}
	
	/**
	 * Returns the current state of the player.
	 * 
	 * @return the current state of the player
	 * @see PlayerState
	 */
	public int getState() {
		return getState(id);
	}
	
	/**
	 * Sets the play back rate.
	 * 
	 * @param rate the new play back rate, {@code <1} is slow motion, {@code >1}
	 * is fast forward
	 */
	public void setRate(float rate) {
		setRate(id, rate);
	}
	
	/**
	 * Returns the current play back rate.
	 * 
	 * @return the current play back rate
	 */
	public float getRate() {
		return getRate(id);
	}
	
	/**
	 * Sets the audio volume.
	 * 
	 * @param volume the volume level, between {@code 0} and {@code 1}
	 */
	public void setVolume(float volume) {
		setVolume(id, volume);
	}
	
	/**
	 * Returns the audio volume level.
	 * 
	 * @return the volume level
	 */
	public float getVolume() {
		return getVolume(id);
	}
	
	/**
	 * Sets the media time of the player.
	 * 
	 * @param time the media time in milliseconds
	 */
	public void setMediaTime(long time) {
		setMediaTime(id, MS_TO_REF_TIME * time);
	}
	
	/**
	 * Returns the current media time
	 * 
	 * @return the current media time in milliseconds
	 */
	public long getMediaTime() {
		return getMediaTime(id) / MS_TO_REF_TIME;
	}
	
	/**
	 * Returns the media duration in milliseconds.
	 * 
	 * @return the duration in milliseconds
	 */
	public long getDuration() {
		return getDuration(id) / MS_TO_REF_TIME;
	}
	
	/**
	 * Move a frame forward, implemented on a lower level.
	 * 
	 * @param atFrameBegin if true try to position the media at the frame start boundary
	 * @return the (natively) calculated media time where the player should jump to,
	 * in milliseconds
	 */
	public double nextFrame(boolean atFrameBegin) {
		return nextFrameInternal(id, atFrameBegin) / MS_TO_REF_TIME;
	}

	/**
	 * Move a frame backward, implemented on a lower level.
	 * 
	 * @param atFrameBegin if true try to position the media at the frame start boundary
	 * @return the (natively) calculated media time where the player should jump to,
	 * in milliseconds
	 */
	public double previousFrame(boolean atFrameBegin) {
		return previousFrameInternal(id, atFrameBegin) / MS_TO_REF_TIME;
	}
	
	/**
	 * Returns whether a native stop time (at the end of a selection) is 
	 * supported.
	 * 
	 * @return {@code true} if native stop time is supported
	 */
	public boolean isStopTimeSupported() {
		return stopTimeSupported;
	}
	
	/**
	 * Sets the stop time, if it is supported.
	 * 
	 * @param time the time in milliseconds
	 */
	public void setStopTime(long time) {
		if (stopTimeSupported) {
			try {
				setStopTime(id, MS_TO_REF_TIME * time);
			} catch (JMMFException jds) {
				stopTimeSupported = false;
				if (LOG.isLoggable(System.Logger.Level.WARNING)) {
					LOG.log(System.Logger.Level.WARNING, jds.getMessage());
				}
			}
		}
	}
	
	/**
	 * Returns the current stop time, if supported.
	 * 
	 * @return the current stop time
	 */
	public long getStopTime() {
		if (stopTimeSupported) {
			return getStopTime(id) / MS_TO_REF_TIME;
		}
		return 0L;
	}
	
	/**
	 * Returns the encoded frame rate, the number of frames per second.
	 *  
	 * @return the encoded frame rate
	 */
	public double getFrameRate() {
		return getFrameRate(id);
	}
	
	/**
	 * Retrieves the player's average time per frame, which is in seconds,
	 * and returns the value in milliseconds.
	 * 
	 * @return the average time per frame in ms.
	 */
	public double getTimePerFrame() {
		return getTimePerFrame(id) * 1000;
	}
	
	/**
	 * Returns the aspect ratio of a video.
	 * 
	 * @return the aspect ratio
	 */
	public float getAspectRatio() {
		return getAspectRatio(id);
	}
	
	/**
	 * Returns the original size of the video.
	 * 
	 * @return the original, encoded size
	 */
	public Dimension getOriginalSize() {
		return getOriginalSize(id);
	}
	
	/**
	 * Returns whether the media file has a visual part, usually a video track
	 * or an image.
	 *  
	 * @return {@code true} if the media has a visual track
	 */
	public boolean isVisualMedia() {
		return isVisualMedia(id);
	}
	
	/**
	 * Returns the preferred aspect ratio of the media, an interpretation by
	 * the framework of encoded ratios.
	 * 
	 * @return the preferred aspect ratio
	 */
	public int[] getPreferredAspectRatio() {
		return getPreferredAspectRatio(id);
	}
	
	/**
	 * Returns the source height.
	 * 
	 * @return the height of the source
	 */
	public int getSourceHeight() {
		return getSourceHeight(id);
	}
	
	/**
	 * Returns the width of the source.
	 * 
	 * @return the width of the source
	 */
	public int getSourceWidth() {
		return getSourceWidth(id);
	}
	
	/**
	 * Passes a Java component to the native player so that it can get the 
	 * native window handle for the video renderer.
	 * 
	 * @param component the visual host component
	 */
	public void setVisualComponent(Component component) {
		if (this.visualComponent == null) {
			this.visualComponent = component;
			setVisualComponent(id, component);
			// Note: start checking player state?
			// if initialization (partially) fails e.g. no VideoDisplayControl,
			// it is maybe best to create a new native player. Updating already
			// created objects (topology) seems error prone.
		} else {
			this.visualComponent = component;
			setVisualComponent(id, component);
		}
	}
	
	/**
	 * Returns the current visual component.
	 * 
	 * @return the visual component
	 */
	public Component getVisualComponent() {
		return visualComponent;
	}
	
	/**
	 * Changes the size of the video component, the native part should update
	 * its rendering.
	 * 
	 * @param w the width of the component
	 * @param h the height of the component
	 */
	public void setVisualComponentSize(int w, int h) {
		//setVisualComponentPos(id, x, y, w, h);
		repositionVideoRect();
		//int dx = Math.max(w, getSourceWidth());
		//int dy = Math.max(h, getSourceHeight());
		//setVideoDestinationPos(id, x, y, w, h);
		//setVideoDestinationPos(id, x, y, dx, dy);
		//float sx = Math.min(1, w / (float) getSourceWidth());
		//float sy = Math.min(1, h / (float) getSourceHeight());
		//setVideoSourcePos(id, 0f, 0f, 1f, 1f);
		//setVideoSourcePos(id, 0f, 0f, sx, sy);
		//setVideoSourceAndDestPos(0f, 0f, 1f, 1f, 0, 0, w, h);
	}
	
	/**
	 * Sets the visibility of the player (the visual component).
	 * 
	 * @param visible if {@code true} the player is visible, hidden otherwise
	 */
	public void setVisible(boolean visible) {
		setVisible(id, visible);
	}
	
	/**
	 * The source rectangle is the part of the video that is displayed on the 
	 * "window", expressed in values between 0 and 1 ({0,0} is the left top
	 * corner, {1,1} the right bottom corner).
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @param w the width
	 * @param h the height
	 */
	public void setVideoSourcePos(float x, float y, float w, float h) {
		setVideoSourcePos(id, x, y, w, h);
	}

	/**
	 * Sets the destination of the video rectangle relative to the visual 
	 * component's location.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param w the width
	 * @param h the height
	 */
	public void setVideoDestinationPos(int x, int y, int w, int h) {
		setVideoDestinationPos(id, x, y, w, h);
	}
	
	/**
	 * Sets both the source and destination coordinates of the video image
	 * relative to the visual component's size and location.
	 * 
	 * @param sx the source x coordinate
	 * @param sy the source y coordinate
	 * @param sw the source width
	 * @param sh the source height
	 * @param x the destination x coordinate
	 * @param y the destination y coordinate
	 * @param w the destination width
	 * @param h the destination height
	 */
	public void setVideoSourceAndDestPos(float sx, float sy, float sw, float sh, 
			int x, int y, int w, int h) {
		setVideoSourceAndDestPos(id, sx, sy, sw, sh, x, y, w, h);
	}
	
	/**
	 * Returns the video destination bounds.
	 * 
	 * @return the destination the video destination bounds
	 */
	public int[] getVideoDestinationPos() {
		return getVideoDestinationPos(id);
	}
	
	/**
	 * Returns the current scale factor of the video (relative to the host
	 * visual component).
	 * 
	 * @return the video scale factor
	 */
	public float getVideoScaleFactor() {
		return videoScaleFactor;
	}

	/**
	 * Sets the video scale factor, for zooming in.
	 * 
	 * @param videoScaleFactor the new scale factor {@code >=1}
	 */
	public void setVideoScaleFactor(float videoScaleFactor) {
		this.videoScaleFactor = videoScaleFactor;
		repositionVideoRect();
	}
	
	/**
	 * Moves the video (inside the component, or relative to the component).
	 * 
	 * @param dx distance to move along the x axis
	 * @param dy distance to move along the y axis
	 */
	public void moveVideoPos(int dx, int dy) {
		if (videoScaleFactor == 1 && allowVideoScaling) {
			return;// the video always fills the whole component
		}
		vdx += dx;
		//vdx = vdx > 0 ? 0 : vdx;
		vdy += dy;
		//vdy = vdy > 0 ? 0 : vdy;
		repositionVideoRect();
//		int[] currentDestPos = getVideoDestinationPos();
//		if (currentDestPos != null) {
//			setVideoDestinationPos(currentDestPos[0] + dx, currentDestPos[1] + dy, 
//					currentDestPos[2], currentDestPos[3]);
//			repaintVideo();
//		}
	}
	
	/**
	 * Returns the current video translation.
	 * @return array of size 2, the translation along the x and y axis 
	 */
	public int[] getVideoTranslation() {
		if (!isVisualMedia() || videoScaleFactor == 1) {
			return new int[] {0, 0};
		} else {
			return new int[] {vx, vy};
		}
	}

	/**
	 * Returns the current, scaled video size. This can be different from the component size
	 * on which the video is displayed.
	 * 
	 * @return array of size 2, the scaled video width and scaled video height 
	 */
	public int[] getScaledVideoRect() {
		if (!isVisualMedia()) {
			return new int[] {0, 0};
		} else if(videoScaleFactor == 1) {
			return new int[] {visualComponent.getWidth(), visualComponent.getHeight()};
		} else {
			return new int[] {vw, vh};
		}
	}
	
	/**
	 * Returns whether video scaling is allowed.
	 * 
	 * @return {@code true} if video scaling is allowed
	 */
	public boolean isAllowVideoScaling() {
		return allowVideoScaling;
	}

	/**
	 * Sets whether video scaling is allowed.
	 * 
	 * @param allowVideoScaling if {@code true} scaling is allowed
	 */
	public void setAllowVideoScaling(boolean allowVideoScaling) {
		this.allowVideoScaling = allowVideoScaling;
	}
	
	/**
	 * Returns the current image; it is retrieved from the renderer, 
	 * so the size might not be the original size.
	 * 
	 * @param dih the image info header
	 * @return the image as an array of bytes
	 */
	public byte[] getCurrentImageData(DIBInfoHeader dih) {
		return getCurrentImage(id, dih);
	}
	
	/**
	 * Currently returns the current image.
	 * 
	 * @param time the media time for which to get the image
	 * @param dih the image info header
	 * 
	 * @return the image as an array of bytes
	 */
	public byte[] getImageDataAtTime(long time, DIBInfoHeader dih) {
		return getImageAtTime(id, dih, MS_TO_REF_TIME * time);
	}
	
	/**
	 * First make sure the player is stopped, then close the session
	 * then delete the player.
	 */
	public void cleanUpOnClose() {
		//clean(id);
		final int STOP_TO = 7000;
		final int CLOSE_TO = 10000;
		boolean stopped = false;
		boolean closed = false;
		if (getState(id) != PlayerState.STOPPED.value) {
			stop(id);
			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime < STOP_TO) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException ie) {
					//System.out.println("Interrupted while waiting for player to stop.");
					if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
						LOG.log(System.Logger.Level.DEBUG, "Interrupted while waiting for player to stop");
					}
				}
				if (getState(id) == PlayerState.STOPPED.value) {
					//System.out.println("Player succesfully stopped.");
					if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
						LOG.log(System.Logger.Level.DEBUG, "Player stopped successfully");
					}
					stopped = true;
					break;
				}
			}
		}
		if (!stopped) {
			// is this fatal?
		}
		
		if (getState(id) != PlayerState.CLOSING.value) {
			closeSession(id);
			
			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime < CLOSE_TO) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException ie) {
					//System.out.println("Interrupted while waiting for player to close session.");
					if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
						LOG.log(System.Logger.Level.DEBUG, "Interrupted while waiting for player to close the session");
					}
				}
				if (getState(id) == PlayerState.CLOSED.value) {
					//System.out.println("Player session succesfully closed.");
					if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
						LOG.log(System.Logger.Level.DEBUG, "Player session successfully closed");
					}
					closed = true;
					break;
				}
			}
		}
		
		if (!closed) {
			// if the session is not closed don't delete the player in order to avoid a crash
			//System.out.println("Error: failed to close the player gracefully.");
			if (LOG.isLoggable(System.Logger.Level.INFO)) {
				LOG.log(System.Logger.Level.INFO, "Error: failed to close the player gracefully");
			}
		} else {
			deletePlayer(id);
		}
	}
	
	/**
	 * Returns the internal {@code id}.
	 * 
	 * @return the internal player id, necessary to communicate with the 
	 * proper native media player (in case multiple native media players have
	 * been instantiated  
	 */
	public long getID() {
		return id;
	}
	
	private void repositionVideoRect() {
		if (visualComponent != null) {
			if (!allowVideoScaling) {
//				int compW = visualComponent.getWidth();
//				int compH = visualComponent.getHeight();
//				int origW = getSourceWidth();
//				int origH = getSourceHeight();
//				float sx = 0f;
//				float sy = 0f;
//				float sw = 1f;
//				float sh = 1f;
//				float factW = compW / (float) origW;
//				float ar = origW / (float) origH;
//				if (factW > 1) {// component bigger than video
//					
//				} else {
//					
//				}
			} else {
				int compW = visualComponent.getWidth();
				int compH = visualComponent.getHeight();
				// correct for "resolution aware" default transform
				AffineTransform defTrans = visualComponent.getGraphicsConfiguration().getDefaultTransform();
				if (defTrans != null && !defTrans.isIdentity()) {
					compW = (int) Math.round(compW * defTrans.getScaleX());
					compH = (int) Math.round(compH * defTrans.getScaleY());
				}

				if (videoScaleFactor == 1) {
					setVideoSourceAndDestPos(0, 0, 1, 1, 0, 0, compW, compH);
				} else {
					vw = (int) (compW * videoScaleFactor);
					vh = (int) (compH * videoScaleFactor);
					vx = vdx;
					vy = vdy;
					if (vx + vw < compW) {
						vx = compW - vw;
					}
					if (vx > 0) {
						vx = 0;
					}
					if (vy + vh < compH) {
						vy = compH - vh;
					}
					if (vy > 0) {
						vy = 0;
					}
					//vx = -vx;
					//vy = -vy;
					float sx1 = (float) -vx / vw;
					float sy1 = (float) -vy / vh;
					float sx2 = sx1 + ((float) compW / vw);
					float sy2 = sy1 + ((float) compH / vh);
					//setVideoDestinationPos(id, vx, vy, vw, vh);
					setVideoSourceAndDestPos(sx1, sy1, sx2, sy2, 0, 0, compW, compH);
				}
			}
		}
	}
	
	/**
	 * Requests a repaint of the video.
	 */
	public void repaintVideo() {
		if (visualComponent != null) {
			repaintVideo(id);
		}
	}
	// internal 	
	/**
	 * Calculates and sets the new media position with the highest possible accuracy.
	 * Calculations are performed using the native time units of the Media Foundation, 
	 * units of 100 nanoseconds.
	 * 
	 * @param id the id of the native player
	 * @param atFrameBegin if true the player cursor should be placed at the beginning 
	 * of the next frame
	 * 
	 * @return the next calculated media position as a double, in units of 100 nanoseconds
	 */
	private double nextFrameInternal(long id, boolean atFrameBegin) {
		long curTime = getMediaTime(id); // in units of 100 nanoseconds
		double perFrame = getTimePerFrame(id); // in seconds
		double perFrameNano = perFrame * 1000 * MS_TO_REF_TIME;
		double nextMediaPosition;
		
		if (atFrameBegin) {
			long curFrame = (long) (curTime / perFrameNano);
			nextMediaPosition = (curFrame + 1) * perFrameNano;
		} else {
			nextMediaPosition = curTime + perFrameNano;
		}
		//System.out.println("Next Frame in Nano: " + nextMediaPosition);
		setMediaTime(id, (long) Math.ceil(nextMediaPosition));
		
		return nextMediaPosition;
	}
	
	/**
	 * Calculates and sets the new media position with the highest possible accuracy.
	 * Calculations are performed using the native time units of the Media Foundation, 
	 * units of 100 nanoseconds.
	 * 
	 * @param id the id of the native player
	 * @param atFrameBegin if true the player cursor should be placed at the beginning 
	 * of the previous frame
	 * 
	 * @return the calculated media position for the previous frame as a double, 
	 * in units of 100 nanoseconds
	 */
	private double previousFrameInternal(long id, boolean atFrameBegin) {
		long curTime = getMediaTime(id); // in units of 100 nanoseconds
		double perFrame = getTimePerFrame(id); // in seconds
		double perFrameNano = perFrame * 1000 * MS_TO_REF_TIME;
		double nextMediaPosition;
		
		if (atFrameBegin) {
			long curFrame = (long) (curTime / perFrameNano);
			if (curFrame == 0) {
				nextMediaPosition = 0;
			} else {
				nextMediaPosition = (curFrame - 1) * perFrameNano;
			}
		} else {
			nextMediaPosition = curTime - perFrameNano;
			if (nextMediaPosition < 0) {
				nextMediaPosition = 0;
			}
		}
		setMediaTime(id, (long) Math.ceil(nextMediaPosition));
		
		return nextMediaPosition;
	}
	
	//// native methods  /////
	/**
	 * Tells the native counterpart which class and method to use for
	 * logging messages to the Java logger.
	 * 
	 * @param clDescriptor the class descriptor, 
	 * 		e.g. {@code nl/mpi/jni/NativeLog}
	 * 	   
	 * @param methodName the name of the {@code static void} method to call, 
	 * e.g. {@code nlog}, a method which accepts one {@code String}
	 */
	static native void initLog(String clDescriptor, String methodName);
	
	private native long initPlayer(boolean synchronous);
	private native void start(long id);;
	private native void stop(long id);
	private native void pause(long id);
	private native boolean isPlaying(long id);
	private native int getState(long id); // get player state
	private native void setRate(long id, float rate);
	private native float getRate(long id);
	private native void setVolume(long id, float volume);
	private native float getVolume(long id);
	private native void setMediaTime(long id, long time);
	private native long getMediaTime(long id);
	private native long getDuration(long id);
	private native double getFrameRate(long id);
	private native double getTimePerFrame(long id);
	private native float getAspectRatio(long id);
	private native Dimension getOriginalSize(long id);
	private native void setVisualComponent(long id, Component component);
	private native void setVisible(long id, boolean visible);
	private native void setVideoSourcePos(long id, float x, float y, float w, float h);
	private native void setVideoDestinationPos(long id, int x, int y, int w, int h);
	private native void setVideoSourceAndDestPos(long id, float sx, float sy, float sw, float sh, 
			int x, int y, int w, int h);
	private native int[] getVideoDestinationPos(long id);
	
	private native long initWithFile(String mediaPath, boolean synchronous) throws JMMFException;
	/** 
	 * Returns the file type {@code GUID} of the specified media file.
	 * 
	 * @param mediaPath the media location
	 * 
	 * @return the GUID of the media subtype  
	 */
	public native String getFileType(String mediaPath);// can do without id
	private native boolean isVisualMedia(long id);
	private native void setStopTime(long id, long time) throws JMMFException;
	private native long getStopTime(long id);
	private native int getSourceHeight(long id);
	private native int getSourceWidth(long id);
	private native int[] getPreferredAspectRatio(long id);
	private native byte[] getCurrentImage(long id, DIBInfoHeader dih);
	private native byte[] getImageAtTime(long id, DIBInfoHeader dih, long time);
	private native void repaintVideo(long ids);
	/** 
	 * Enables or disables debugging messages in the native player.
	 * 
	 *  @param enable the debug mode flag
	 */
	public static native void enableDebugMode(boolean enable);
	/**
	 * Sets whether the native player should correct its media position after
	 * it has been paused (correct an overshoot).
	 * 
	 * @param correct if {@code true} the player should try to correct its 
	 * media position after a pause event
	 */
	public static native void correctAtPause(boolean correct);
	//private static native void setLogFile(String logPath);
	private native void clean(long id);
	private native void closeSession(long id);
	private native void deletePlayer(long id);
}
