package nl.mpi.jds;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

/**
 * A player that contains methods that mostly wrap a DirectShow 
 * function, accessed via JNI.
 * All methods accepting time values expect millisecond values and pass them 
 * to the native function in "reference time", units of 100 nanoseconds.
 * All methods returning time values receive values as "reference time" values 
 * from the native functions and return them as milliseconds.s  
 *  
 * @author Han Sloetjes
 */
public class JDSPlayer {
	/** the default time format in Direct Show is Reference Time, units of 100 nanoseconds */
	private static final int MS_TO_REF_TIME = 10000;
    /** The stopped state */
    public final static int STATE_STOP = 0;

    /** The pause state */
    public final static int STATE_PAUSE = 1;

    /** The running state */
    public final static int STATE_RUN = 2;
    
	static {
		System.loadLibrary("DSPlayer");
    	String debug = System.getProperty("JDSDebug");
    	if (debug != null && debug.toLowerCase().equals("true")) {
    		JDSPlayer.enableDebugMode(true);
    	}
	}
	
	private String mediaPath;
	//private URL mediaURL;
	private long id = -1;// not inited
	private Component visualComponent;
	// initialize as true in order to be sure it will tried at least once
	private boolean stopTimeSupported = true;
	private float videoScaleFactor = 1f;
	private int vx = 0, vy = 0, vw = 0, vh = 0;
	private int vdx = 0, vdy = 0;// for dragging
	
	/**
	 * Creates a new Java - DirectShow player for the specified media file.
	 * 
	 * @param mediaPath the path of the media file
	 * @throws JDSException any JDS exception that can occur
	 */
	public JDSPlayer(String mediaPath) throws JDSException {
		super();
		//check path
		this.mediaPath = mediaPath;
		 
		id = initWithFile(this.mediaPath);
		// 			
//		String codec = "Elecard MPEG Demultiplexer";
//		boolean b = isCodecInstalled(codec);
//		System.out.println("Codec installed: " + codec + " : " + b);
//		b = isCodecInstalled("boe");
//		System.out.println("Codec installed: boe" + " : " + b);
//		String type = getFileType(mediaPath);
//		System.out.println("File type: " + type);
	}
	
	/**
	 * Creates a new Java - DirectShow player for the specified media file, 
	 * with the specified preferred codec.
	 * 
	 * @param mediaPath the path of the media file
	 * @param preferredCodec the name or identifier of a codec that should be
	 * used when possible (if it is installed and supports the file format)
	 * 
	 * @throws JDSException any JDS exception that can occur
	 */
	public JDSPlayer(String mediaPath, String preferredCodec) throws JDSException {
		super();
		//check path
		this.mediaPath = mediaPath;
		 
		id = initWithFileAndCodec(this.mediaPath, preferredCodec);
	}
	
	/**
	 * Returns an array of the filters in the filter graph created for this
	 * media file.
	 * @return an array of filter names
	 */
	public String[] getFiltersInGraph () {
		return getFiltersInGraph(id);
	}
	
//	public JDSPlayer(URL mediaURL) throws JDSException {
//		super();
//		this.mediaURL = mediaURL;
//	}

	/**
	 * Starts the player.
	 */
	public void start() {
		start(id);
	}
	
	/**
	 * Stops the player.
	 */
	public void stop() {
		stop(id);
	}
	
	/**
	 * Pauses the player.
	 */
	public void pause() {
		pause(id);
	}
	
	/**
	 * Returns whether the player is playing.
	 * 
	 * @return {@code true} if the player is playing, {@code false} if the
	 * player is stopped/paused.
	 */
	public boolean isPlaying() {
		return isPlaying(id);
	}
	
	/**
	 * Updates the video after a seek operation. Transitions the player to the
	 * stopped state.
	 */
	public void stopWhenReady() {
		stopWhenReady(id);
	}
	
	/**
	 * Returns the current player state.
	 * 
	 * @return the current player state
	 */
	public int getState() {
		return getState(id);
	}
	
	/**
	 * Sets the play back rate of the player.
	 * 
	 * @param rate the playback rate, {@code <1} is slow motion, {@code >1} is
	 * fast forward.
	 */
	public void setRate(float rate) {
		setRate(id, rate);
	}
	
	/**
	 * Returns the play back rate.
	 * 
	 * @return the play back rate
	 */
	public float getRate() {
		return getRate(id);
	}
	
	/**
	 * Sets the audio output volume.
	 * 
	 * @param volume the audio volume, between {@code 0} and {@code 1}
	 */
	public void setVolume(float volume) {
		setVolume(id, volume);
	}
	
	/**
	 * Returns the audio volume.
	 * 
	 * @return the current volume
	 */
	public float getVolume() {
		return getVolume(id);
	}
	
	/**
	 * Sets the media time the player should jump to.
	 * 
	 * @param time the new media time
	 */
	public void setMediaTime(long time) {
		setMediaTime(id, MS_TO_REF_TIME * time);
	}
	
	/**
	 * Returns the current media time.
	 * 
	 * @return the current media time
	 */
	public long getMediaTime() {
		return getMediaTime(id) / MS_TO_REF_TIME;
	}
	
	/**
	 * Returns the media duration.
	 * 
	 * @return the duration of the media 
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
	 * Returns whether the native frameworks supports setting of a stop time,
	 * e.g. for stopping at the end of playing a selection.
	 * 
	 * @return whether a stop time is supported
	 */
	public boolean isStopTimeSupported() {
		return stopTimeSupported;
	}
	
	/**
	 * Sets the stop time, if this is supported.
	 * 
	 * @param time the media stop time
	 */
	public void setStopTime(long time) {
		if (stopTimeSupported) {
			try {
				setStopTime(id, MS_TO_REF_TIME * time);
			} catch (JDSException jds) {
				stopTimeSupported = false;
				System.out.println(jds.getMessage());
			}
		}
	}
	
	/**
	 * Returns the stop time.
	 * 
	 * @return the stop time or {@code 0}
	 */
	public long getStopTime() {
		if (stopTimeSupported) {
			return getStopTime(id) / MS_TO_REF_TIME;
		}
		return 0L;
	}
	
	/**
	 * Returns the current frame rate.
	 * 
	 * @return the frame rate
	 */
	public double getFrameRate() {
		return getFrameRate(id);
	}
	
	/**
	 * Retrieves the Basic Video's average time per frame, which is in seconds,
	 * and returns the value in milliseconds.
	 * 
	 * @return the average time per frame in ms.
	 */
	public double getTimePerFrame() {
		return getTimePerFrame(id) * 1000;
	}
	
	/**
	 * Returns the aspect ratio of the video.
	 * 
	 * @return the aspect ratio
	 */
	public float getAspectRatio() {
		return getAspectRatio(id);
	}
	
	/**
	 * Returns the original, encoded size of the video.
	 * 
	 * @return the original size of the video
	 */
	public Dimension getOriginalSize() {
		return getOriginalSize(id);
	}
	
	/**
	 * Returns whether the media file has at least one visual component or
	 * track, video or image.
	 * 
	 * @return {@code true} if the media file has visual content (video), 
	 * {@code false} if it is audio only
	 */
	public boolean isVisualMedia() {
		return isVisualMedia(id);
	}
	
	/**
	 * Returns the preferred aspect ratio.
	 * 
	 * @return the aspect ratio, an array with aspect X and aspect Y
	 */
	public int[] getPreferredAspectRatio() {
		return getPreferredAspectRatio(id);
	}
	
	/**
	 * Returns the source height of the video.
	 * 
	 * @return the height of the video
	 */
	public int getSourceHeight() {
		return getSourceHeight(id);
	}
	
	/**
	 * Returns the source width of the video.
	 * 
	 * @return the width of the video
	 */
	public int getSourceWidth() {
		return getSourceWidth(id);
	}
	
	/**
	 * Sets the visual component to be used by the player for the 
	 * visual output.
	 * 
	 * @param component the visual component
	 */
	public void setVisualComponent(Component component) {
		this.visualComponent = component;
		setVisualComponent(id, component);
		repositionVideoRect();
	}
	
	/**
	 * Returns the visual component.
	 * 
	 * @return the visual component.
	 */
	public Component getVisualComponent() {
		return visualComponent;
	}
	
	/**
	 * Updates the position of the visual component.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param w the width of the component
	 * @param h the height of the component
	 */
	public void setVisualComponentPos(int x, int y, int w, int h) {
		if (visualComponent != null) {
			// correct for "resolution aware" default transform
			AffineTransform defTrans = visualComponent.getGraphicsConfiguration().getDefaultTransform();
			if (defTrans != null && !defTrans.isIdentity()) {
				w = (int) Math.round(w * defTrans.getScaleX());
				h = (int) Math.round(h * defTrans.getScaleY());
			}
			setVisualComponentPos(id, x, y, w, h);
			repositionVideoRect();
		}
	}
	
	/**
	 * Sets the visible state of the player.
	 * 
	 * @param visible if {@code true} the video is visible, if {@code false}
	 * the video is hidden
	 */
	public void setVisible(boolean visible) {
		setVisible(id, visible);
	}
	
	/**
	 * Updates the size and location of the video host panel.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param w the width
	 * @param h the height
	 */
	public void setVideoSourcePos(int x, int y, int w, int h) {
		setVideoSourcePos(id, x, y, w, h);
	}

	/**	
	 * Updates the size and location of the video relative to the host panel.
	 * 
	 * @param x the x coordinate of the video destination
	 * @param y the y coordinate of the video destination
	 * @param w the width of the video destination
	 * @param h the height of the video destination
	 */
	public void setVideoDestinationPos(int x, int y, int w, int h) {
		vx = x;
		vy = y;
		setVideoDestinationPos(id, x, y, w, h);
	}
	
	/**
	 * Returns the current bounds of the (possibly scaled) video.
	 * 
	 * @return the bounds of the video destination
	 */
	public int[] getVideoDestinationPos() {
		return getVideoDestinationPos(id);
	}
	
	/**
	 * Returns the video scale factor.
	 * 
	 * @return the video scale factor
	 */
	public float getVideoScaleFactor() {
		return videoScaleFactor;
	}

	/**
	 * Sets the video scale factor.
	 * 
	 * @param videoScaleFactor the new scale factor for the video
	 */
	public void setVideoScaleFactor(float videoScaleFactor) {
		this.videoScaleFactor = videoScaleFactor;
		repositionVideoRect();
	}

	/**
	 * Returns the bytes of the current video image.
	 * 
	 * @return the image {@code byte} array or {@code null}
	 */
	public byte[] getCurrentImageData() {
		return getCurrentImage(id);
	}
	
	/**
	 * Returns the image data of the image at the specified time.
	 * 
	 * @param time the time to get the image for
	 * 
	 * @return  the image {@code byte} array or {@code null}
	 */
	public byte[] getImageDataAtTime(long time) {
		return getImageAtTime(id, MS_TO_REF_TIME * time);
	}
	
	/**
	 * Cleans up the player when the document closes.
	 */
	public void cleanUpOnClose() {
		clean(id);
	}
	
	/**
	 * Returns the ID of this player. 
	 * @return the ID of this player
	 */
	public long getID() {
		return id;
	}
	
	/**
	 * Returns the current video translation.
	 * 
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
		if (!isVisualMedia() || videoScaleFactor == 1) {
			return new int[] {0, 0};
		} else {
			return new int[] {vw, vh};
		}
	}
	
	/**
	 * Moves the video (inside the component, or relative to the component).
	 * 
	 * @param dx distance to move along the x axis
	 * @param dy distance to move along the y axis
	 */
	public void moveVideoPos(int dx, int dy) {
		if (videoScaleFactor == 1) {
			return;// the video always fills the whole component
		}
		vdx += dx;
		//vdx = vdx > 0 ? 0 : vdx;
		vdy += dy;
		//vdy = vdy > 0 ? 0 : vdy;
		repositionVideoRect();
	}
	
	private void repositionVideoRect() {
		if (visualComponent != null) {
			int compW = visualComponent.getWidth();
			int compH = visualComponent.getHeight();
			// correct for "resolution aware" default transform
			AffineTransform defTrans = visualComponent.getGraphicsConfiguration().getDefaultTransform();
			if (defTrans != null && !defTrans.isIdentity()) {
				compW = (int) Math.round(compW * defTrans.getScaleX());
				compH = (int) Math.round(compH * defTrans.getScaleY());
			}

			if (videoScaleFactor == 1) {
				setVideoDestinationPos(id, 0, 0, compW, compH);
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
				setVideoDestinationPos(id, vx, vy, vw, vh);
			}		
		}
	}
	// internal 	
	/**
	 * Calculates and sets the new media position with the highest possible accuracy.
	 * Calculations are performed using the native time units of Direct Show, 
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
		nextMediaPosition += 100;
		//System.out.println("Next Frame in Nano: " + nextMediaPosition);
		setMediaTime(id, (long) Math.ceil(nextMediaPosition));
		
		return nextMediaPosition;
	}
	
	/**
	 * Calculates and sets the new media position with the highest possible accuracy.
	 * Calculations are performed using the native time units of Direct Show, 
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
	private native void start(long id);
	private native void stop(long id);
	private native void pause(long id);
	private native boolean isPlaying(long id);
	private native void stopWhenReady(long id);
	private native int getState(long id);
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
	private native void setVisualComponentPos(long id, int x, int y, int w, int h);
	private native void setVisible(long id, boolean visible);
	private native void setVideoSourcePos(long id, int x, int y, int w, int h);
	private native void setVideoDestinationPos(long id, int x, int y, int w, int h);
	private native int[] getVideoDestinationPos(long id);
	
	private native long initWithFile(String mediaPath) throws JDSException;
	/** 
	 * Use friendly name of the filter here, not the GUID 
	 * @param mediaPath the media location
	 * @param preferredCodec the codec to use (instantiate) if possible
	 * 
	 * @return result code
	 */
	private native long initWithFileAndCodec(String mediaPath, String preferredCodec) throws JDSException;
	/** 
	 * Use friendly name of the filter here, not the GUID 
	 * 
	 * @param codec the codec name to test
	 * @return {@code true} if the codec was detected, is there, {@code false} otherwise
	 */
	public native boolean isCodecInstalled(String codec);// can do without id
	/**
	 * Returns an array of registered filter names.
	 * 
	 * @return the registered filtes
	 */
	public native String[] getRegisteredFilters();// can do without id
	private native String[] getFiltersInGraph(long id);
	/** 
	 * Returns the GUID of the media subtype.
	 * 
	 * @param mediaPath the media file path
	 * @return the file type as a string
	 */
	public native String getFileType(String mediaPath);// can do without id
	private native boolean isVisualMedia(long id);
	private native void setStopTime(long id, long time) throws JDSException;
	private native long getStopTime(long id);
	private native int getSourceHeight(long id);
	private native int getSourceWidth(long id);
	private native int[] getPreferredAspectRatio(long id);
	private native byte[] getCurrentImage(long id);
	private native byte[] getImageAtTime(long id, long time);
	/**
	 * Enables or disables debugging messages in the native player 
	 * 
	 * @param enable the debug flag
	 */
	public static native void enableDebugMode(boolean enable);
	private native void clean(long id);
}
