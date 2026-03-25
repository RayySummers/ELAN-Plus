package mpi.eudico.client.annotator.player;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import javax.swing.JMenuItem;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.PreferencesListener;
import mpi.eudico.client.annotator.export.ImageExporter;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.ControllerManager;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import nl.mpi.jmmf.DIBInfoHeader;
import nl.mpi.jmmf.JMMFCanvas;
import nl.mpi.jmmf.JMMFException;
import nl.mpi.jmmf.JMMFFrameGrabber;
import nl.mpi.jmmf.JMMFPanel;
import nl.mpi.jmmf.JMMFPlayer;

/**
 * An ELAN player that wraps a JMMFPlayer, a Java based player that uses
 * the Microsoft Media Foundation for media playback. Only available on
 * Vista and Windows 7, mp4 support on Windows 7 and newer.
 * 
 * @author Han Sloetjes
 */
public class JMMFMediaPlayer extends ControllerManager implements
		ElanMediaPlayer, ControllerListener, VideoFrameGrabber,
		VideoScaleAndMove, ActionListener, PreferencesListener {
	private JMMFPlayer jmmfPlayer;
	private JMMFPanel jmmfPanel = null;
	private JMMFCanvas jmmfCanvas = null;
	private VideoMouseAdapter mouseAdapter;
	private JMMFFrameGrabber frameGrabber;
	private MediaDescriptor mediaDescriptor;
	private String mediaUrlString;
	private long offset = 0L;
	private long stopTime;
	private long duration;// media duration minus offset
	private long origDuration;// the original media duration
	// end of media buffer, don't set stop time or media time to 
	// the end of the media because then the media jumps to 0
	private long eomBuffer = 0;
	private int numFramesEOMBuffer = 5;
	private float origAspectRatio = 0;
	private float aspectRatio = 0;
	private double millisPerSample;
	//private boolean playing;
	private PlayerStateWatcher stopThread = null;
	private EndOfTimeWatcher endTimeWatcher = null;
	
	private boolean isInited = false;
	private boolean initWaitThreadStarted = false;
	private float cachedVolume = 1.0f;
	private float cachedRate = 1.0f;
	private float curSubVolume;
	private boolean mute;
	// after a pause() the native player may return 0.0 as rate (scrubbing)
	// return the latest user or program specified rate instead in that case (should be handled natively)
	private float programSetRate = 1.0f;
	
    private boolean frameRateAutoDetected = true;
	/** if true frame forward and frame backward always jump to the begin
	 * of the next/previous frame, otherwise it jumps with the frame duration */
	private boolean frameStepsToFrameBegin = false;
	private boolean pre47FrameStepping = false;
	// gui
	private ElanLayoutManager layoutManager;
	private JMenuItem saveNatSizeItem;
	private int SET_MT_TIMEOUT = 1000;
	
	private final ReentrantLock syncLock = new ReentrantLock();
	/** a flag for recording and checking if the player is currently playing
	 *  an interval. The asynchronous native player does not reach the paused
	 *  state immediately after it was paused. */
	private boolean isPlayingInterval = false;
	private static boolean useCanvas = false;
	
	// make sure debug mode is set, if enabled, before the first player is created
	static {
		Boolean val = Preferences.getBool("NativePlayer.DebugMode", null);
    	
    	if (val != null) {
    		JMMFPlayer.enableDebugMode(val.booleanValue());
    		JMMFFrameGrabber.setDebugMode(val.booleanValue());
    	}
    	String canvasProp = System.getProperty("JMMFPlayer.UseCanvas");
    	if ("true".equalsIgnoreCase(canvasProp)) {
    		useCanvas = true;
    	}
	}
	
	/**
	 * Constructor.
	 * 
	 * @param mediaDescriptor the media descriptor containing the URL of the 
	 * media file
	 * @throws NoPlayerException if the file cannot be found or is not supported
	 */
	public JMMFMediaPlayer(MediaDescriptor mediaDescriptor) throws NoPlayerException {
		this.mediaDescriptor = mediaDescriptor;
		offset = mediaDescriptor.timeOrigin;
		
        String urlString = mediaDescriptor.mediaURL;
        if (urlString.startsWith("file:") &&
                !urlString.startsWith("file:///")) {
            urlString = urlString.substring(5);
        }
        mediaUrlString = urlString;
        // check the EndOfMedia buffer property
        String eomFramesProp = System.getProperty("JMMFPlayer.EndOfMedia.NumberOfFramesBuffer");
        if (eomFramesProp != null) {
        	try {
        		numFramesEOMBuffer = Integer.parseInt(eomFramesProp);
        	} catch (NumberFormatException nfe) {
        		LOG.warning("There is a 'EndOfMedia.NumberOfFramesBuffer' property but its value can not be parsed");
        	}
        }
        try {
        	boolean synchronousMode = false;
        	Boolean synVal = Preferences.getBool("Windows.JMMFPlayer.SynchronousMode", null);
        	if (synVal != null) {
        		synchronousMode = synVal;
        	}
        	
        	jmmfPlayer = new JMMFPlayer(urlString, synchronousMode);
        	if (jmmfPlayer.isVisualMedia()) {
        		if (useCanvas) {
        			jmmfCanvas = new JMMFCanvas(jmmfPlayer);
        		} else {
        			jmmfPanel = new JMMFPanel(jmmfPlayer);
        		}
        		
        		Boolean boolPref = Preferences.getBool("Windows.JMMFPlayer.CorrectAtPause", null);
        		
        		if (boolPref != null) {
        			JMMFPlayer.correctAtPause(boolPref);
        		}
        	}        	
        	// cannot get info from the player yet
        } catch (JMMFException je) {
        	throw new NoPlayerException("JMMFPlayer cannot handle the file: " + je.getMessage());
        } catch (Throwable tr) {
        	throw new NoPlayerException("JMMFPlayer cannot handle the file: " + tr.getMessage());
        }
	}
	
	@Override
	public void cleanUpOnClose() {
		if (jmmfPlayer != null) {
			if (endTimeWatcher != null) {
				endTimeWatcher.close();
			}
			if (jmmfPanel != null) {
				jmmfPanel.setPlayer(null);
			} else if (jmmfCanvas != null) {
				jmmfCanvas.setPlayer(null);
			}
			
			jmmfPlayer.cleanUpOnClose();
			jmmfPlayer = null;//make sure no more calls are made to this player
			if (mouseAdapter != null) {
				mouseAdapter.release();
			}
		}
		layoutManager = null;
	}

	/**
	 * Returns the aspect ratio.
	 */
	@Override
	public float getAspectRatio() {
		if (aspectRatio != 0) {
			return aspectRatio;
		}
		if (jmmfPlayer != null) {
			if (origAspectRatio == 0) {
				origAspectRatio = jmmfPlayer.getAspectRatio();
			}
			aspectRatio = origAspectRatio;
			
			// initialization problem?
			if (jmmfPlayer.isVisualMedia() && jmmfPlayer.getAspectRatio() == 0) {
				LOG.warning("The aspect ratio is not initialized yet: 0.0");
				return 1;
			}
		}
		return aspectRatio;
	}

	/**
	 * @return the string "JMMF - Java with Microsoft Media Foundation Player"
	 */
	@Override
	public String getFrameworkDescription() {
		String compType = jmmfPanel != null ? "Panel" : "Canvas";
		String sMode = (jmmfPlayer != null && jmmfPlayer.isSynchronousMode()) ?
				"Synchronous Mode, " : "";
		
		return String.format("JMMF - Java with Microsoft Media Foundation Player (%swith %s)", 
				sMode, compType);
	}

	@Override
	public MediaDescriptor getMediaDescriptor() {
		return mediaDescriptor;
	}

	/**
	 * Gets the duration from the player (and stores it locally).
	 * @return the media duration in ms
	 */
	@Override
	public long getMediaDuration() {
		if (duration <= 0) {
			if (jmmfPlayer != null) {
				if (origDuration == 0) {
					origDuration = jmmfPlayer.getDuration();					
				}
				duration = origDuration - offset;
			}
		}
		return duration;
	}

	/**
	 * Returns the current media time, in ms and corrected for the offset.
	 */
	@Override
	public long getMediaTime() {
		if (jmmfPlayer != null) {
			return jmmfPlayer.getMediaTime() - offset;
		}
		return 0;
	}

	/**
	 * Retrieves the duration per sample (and caches it locally).
	 */
	@Override
	public double getMilliSecondsPerSample() {
		if (millisPerSample == 0.0) {
			if (jmmfPlayer != null) {
				millisPerSample = jmmfPlayer.getTimePerFrame();
				if (millisPerSample == 0.0) {
					millisPerSample = 40.0;
					frameRateAutoDetected = false;
					if (layoutManager != null) {
						Double prefFrameDur = Preferences.getDouble(
							"MediaPlayer.CustomFrameDuration", 
							layoutManager.getViewerManager().getTranscription());
						if (prefFrameDur != null) {
							millisPerSample = prefFrameDur.doubleValue();
						}
					}
				}
			}
		}
		return millisPerSample;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public float getRate() {
		if (jmmfPlayer != null) {
			float curRate = jmmfPlayer.getRate();
			if (curRate == 0) {
				return programSetRate;
			} else {
				return curRate;
			}
			//return jmmfPlayer.getRate();
		}
		return 1;
	}

	@Override
	public int getSourceHeight() {
		if (jmmfPlayer != null) {
			if (jmmfPlayer.isVisualMedia() && jmmfPlayer.getSourceHeight() <= 1) {
				LOG.warning(String.format("Height not initialized yet: %d", jmmfPlayer.getSourceHeight()));
				return 100;
			}
			return jmmfPlayer.getSourceHeight();
		}
		return 0;
	}

	@Override
	public int getSourceWidth() {
		if (jmmfPlayer != null) {
			if (jmmfPlayer.isVisualMedia() && jmmfPlayer.getSourceWidth() <= 1) {
				LOG.warning(String.format("Width not initialized yet: %d", jmmfPlayer.getSourceWidth()));
				return 100;
			}
			return jmmfPlayer.getSourceWidth();
		}
		return 0;
	}

	/**
	 * After the first time this is called the panel will be added to a window,
	 * upon which the player will be initialized fully.
	 */
	@Override
	public Component getVisualComponent() {
		if (!isInited && !initWaitThreadStarted) {
			initWaitThreadStarted = true;
			new InitWaitThread().start();
		}
		return jmmfPanel != null ? jmmfPanel : jmmfCanvas;
	}

	@Override
	public float getVolume() {
		if (jmmfPlayer != null) {
			return jmmfPlayer.getVolume();
		}
		return 0f;
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
    
    @Override
	public boolean isFrameRateAutoDetected() {
		return frameRateAutoDetected;
	}

	@Override
	public boolean isPlaying() {
		if (jmmfPlayer != null) {
			return jmmfPlayer.isPlaying();
			//return playing;
			//return jmmfPlayer.getState() == JMMFPlayer.PlayerState.STARTED.value;
		}
		return false;
	}

	@Override
	public void nextFrame() {
		if (jmmfPlayer != null) {
			if (jmmfPlayer.isPlaying()) {
				stop();
			}
			
	        if (pre47FrameStepping) {
	        	nextFramePre47();
	        	return;
	        }
	        
			double nextTime = jmmfPlayer.nextFrame(frameStepsToFrameBegin);
			
			if (!jmmfPlayer.isSynchronousMode()) {
	        	long sysTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException ie){}

					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
			}
			
			setControllersMediaTime((long) Math.ceil(nextTime) - offset);
			/*
	        if (frameStepsToFrameBegin) {
	        	long curFrame = (long)(getMediaTime() / millisPerSample);
	    		setMediaTime((long) Math.ceil((curFrame + 1) * millisPerSample));
	        } else {
	        	long curTime = jmmfPlayer.getMediaTime();
	        	//System.out.println("Current time: " + curTime);
	        	//curTime += millisPerSample;
	        	curTime = (long) Math.ceil(curTime + millisPerSample);
	        	//System.out.println("Current time 2: " + curTime);
	        	jmmfPlayer.setMediaTime(curTime);
	        	if (!jmmfPlayer.isSynchronousMode()) {
		        	long sysTime = System.currentTimeMillis();
					while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
						try {
							Thread.sleep(5);
						} catch (InterruptedException ie){}
	
						if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
							break;
						}
					}
	        	}
				//jmmfPlayer.repaintVideo();
	        	setControllersMediaTime(curTime - offset);
	        }
	        */
		}
	}
	
    /**
     * The pre 4.7 implementation of next frame.
     */
    private void nextFramePre47() {
    	// assumes a check for jmmfPlayer != null and player == paused has been performed 
        if (frameStepsToFrameBegin) {
        	long curFrame = (long)(getMediaTime() / millisPerSample);
    		setMediaTime((long)((curFrame + 1) * millisPerSample));
        } else {
        	long curTime = jmmfPlayer.getMediaTime();
        	//System.out.println("Current time: " + curTime);
        	curTime += millisPerSample;
        	//System.out.println("Current time 2: " + curTime);
        	jmmfPlayer.setMediaTime(curTime);
        	if (!jmmfPlayer.isSynchronousMode()) {
	        	long sysTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException ie){}
	
					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
        	}
			//jmmfPlayer.repaintVideo();
        	setControllersMediaTime(curTime - offset);
        }
    }

	@Override
	public void playInterval(long startTime, long stopTime) {
		if (jmmfPlayer != null) {
			if (jmmfPlayer.isPlaying()) {
				stop();
			}
			syncLock.lock();
			try {
				setStopTime(stopTime);
				if (getMediaTime() != startTime + offset) {
					setMediaTimeAndWait(startTime);
				}
				
				startInterval();
			} finally {
				syncLock.unlock();
			}
		}

	}
	
	void startInterval() {
		if (jmmfPlayer != null) {
			if (jmmfPlayer.isPlaying()) {
				return;
			}

	        // create a PlayerEndWatcher thread
	        if (stopThread != null && stopThread.isAlive()) {
	        	stopThread.setStopped();
	        }
	        int sleepTime = 10;	        
	        if (jmmfPlayer.isSynchronousMode()) {
	        	sleepTime = 5;
	        }
	        
	        stopThread = new PlayerStateWatcher(sleepTime);
	        isPlayingInterval = true;
	        if (jmmfPlayer.isSynchronousMode()) {
	        	jmmfPlayer.start();
	        	startControllers();
	        	stopThread.start();
	        } else {
		        stopThread.start();		        
		        jmmfPlayer.start();
		        startControllers();
	        }
		}
	}
	
	/**
	 * Returns the value of the isPlayingInterval flag, which is set to false at the 
	 * very end of a play interval action, taking into account the asynchronous behavior
	 * of the native media player.
	 * 
	 * @return {@code true} if an interval is being played, {@code false} by default
	 * 
	 */
	@Override
	public boolean isPlayingInterval() {
		return isPlayingInterval;
	}

	@Override
	public void previousFrame() {
		if (jmmfPlayer != null) {
			if (jmmfPlayer.isPlaying()) {
				stop();
			}
			
			if (pre47FrameStepping) {
				previousFramePre47();
				return;
			}
			
			double prevTime = jmmfPlayer.previousFrame(frameStepsToFrameBegin);
			
			if (!jmmfPlayer.isSynchronousMode()) {
	        	long sysTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException ie){}

					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
			}
			
			setControllersMediaTime((long) Math.ceil(prevTime) - offset);
		}

	}
	
    /**
     * The previous implementation of previous frame, with (more) rounding effects.
     */
    private void previousFramePre47() {
    	// assumes this is checked: player != null and is paused
        if (frameStepsToFrameBegin) {
        	long curFrame = (long)(getMediaTime() / millisPerSample);
        	if (curFrame > 0) {
        		setMediaTime((long)((curFrame - 1) * millisPerSample));
        	} else {
        		setMediaTime(0);
        	}
        } else {
        	long curTime = jmmfPlayer.getMediaTime();
        	curTime -= millisPerSample;
        	
	        if (curTime < 0) {
	        	curTime = 0;
	        }
	
	        jmmfPlayer.setMediaTime(curTime);
	        if (!jmmfPlayer.isSynchronousMode()) {
		        long sysTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException ie){}
					
					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
	        }
        	setControllersMediaTime(curTime - offset);
        }	
    }

	@Override
	public void setAspectRatio(float aspectRatio) {
		this.aspectRatio = aspectRatio;
		// hier update the visual component
	}

	@Override
	public void setFrameStepsToFrameBegin(boolean stepsToFrameBegin) {
		this.frameStepsToFrameBegin = stepsToFrameBegin;
	}

	@Override
	public void setLayoutManager(ElanLayoutManager layoutManager) {
		this.layoutManager = layoutManager;
		if (this.layoutManager != null) {
			//detached = !(this.layoutManager.isAttached(this));
			if (jmmfPanel != null) {
				if (mouseAdapter != null) {
					mouseAdapter.release();
				}
				mouseAdapter = new VideoMouseAdapter(this, layoutManager, jmmfPanel);
		        saveNatSizeItem = new JMenuItem(ElanLocale.getString("Player.SaveFrame.NaturalSize"));
		        saveNatSizeItem.addActionListener(this);
		        mouseAdapter.addCustomMenuItem(saveNatSizeItem);
			} else if (jmmfCanvas != null) {
				if (mouseAdapter != null) {
					mouseAdapter.release();
				}
				mouseAdapter = new VideoMouseAdapter(this, layoutManager, jmmfCanvas);
		        saveNatSizeItem = new JMenuItem(ElanLocale.getString("Player.SaveFrame.NaturalSize"));
		        saveNatSizeItem.addActionListener(this);
		        mouseAdapter.addCustomMenuItem(saveNatSizeItem);
			}
		}
	}

	@Override
	public void setMediaTime(long time) {
		if (jmmfPlayer != null) {
			// works a bit better than just setting the position
//			if (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value){
//				return;
//			}
			if (jmmfPlayer.isPlaying()) {
				stop();
			}
			if (time < 0) {
				time = 0;
			}
			if (time > duration - eomBuffer) {
				time = duration - eomBuffer;
			}

			// blocking
			jmmfPlayer.setMediaTime(time + offset);
			if (!jmmfPlayer.isSynchronousMode()) {
				long curTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException ie){}
					if (System.currentTimeMillis() - curTime > SET_MT_TIMEOUT) {
	//					System.out.println("Set MT: time out");
						break;
					}
				}
			}
			//System.out.println("Set MT: " + (System.currentTimeMillis() - curTime));
			setControllersMediaTime(time);

		}
	}

	private void setMediaTimeAndWait(long time) {
		//System.out.println("T: " + time);
		if (jmmfPlayer != null) {
			// works a bit better than just setting the position
//			if (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value){
//				return;
//			}
			if (jmmfPlayer.isPlaying()) {
				stop();
			}
			if (time < 0) {
				time = 0;
			}
			// don't check for the margin at the end of media
			if (time > duration /* - eomBuffer*/) {
				time = duration /* - eomBuffer*/;
			}
			
			jmmfPlayer.setMediaTime(time + offset);
			if (!jmmfPlayer.isSynchronousMode()) {
				long sysTime = System.currentTimeMillis();
				
				while (jmmfPlayer.getState() == JMMFPlayer.PlayerState.SEEKING.value) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException ie){}
					
					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
			}
			setControllersMediaTime(time);
		}
	}

	@Override
	public void setMilliSecondsPerSample(double milliSeconds) {
		if (!frameRateAutoDetected) {
			this.millisPerSample = milliSeconds;
			if (layoutManager != null) {
				layoutManager.setPreference("MediaPlayer.CustomFrameDuration", 
						Double.valueOf(milliSeconds), 
						layoutManager.getViewerManager().getTranscription());
			}
		}
	}

	@Override
	public void setOffset(long offset) {
		long diff = this.offset - offset;
        this.offset = offset;
        mediaDescriptor.timeOrigin = offset;
        if (jmmfPlayer != null) {
			if (origDuration == 0) {
				origDuration = jmmfPlayer.getDuration();
			}
        	duration = origDuration - offset;
        }
        stopTime += diff;
        setStopTime(stopTime);//??
	}

	@Override
	public void setRate(float rate) {
		if (!isInited) {
			cachedRate = rate;
			programSetRate = rate;
			return;
		}
		programSetRate = rate;
		if (jmmfPlayer != null) {
			jmmfPlayer.setRate(rate);
		}
		setControllersRate(rate);
	}

	@Override
	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
        // see if the stop time must be increased to ensure correct frame rendering at a frame boundary
		double msps = getMilliSecondsPerSample();
		if (msps != 0.0) {
	        long nFrames = (long) ((stopTime + offset) / msps);
	
	        if ((long) Math.ceil(nFrames * msps) == (stopTime + offset)) { // on a frame boundary
	            this.stopTime += 1;
	        }
		}
		if (jmmfPlayer != null) {
			jmmfPlayer.setStopTime(this.stopTime + offset);
		}
        setControllersStopTime(this.stopTime);
	}

	@Override
	public void setVolume(float level) {
		//System.out.println("Set volume: " + level);
		if (!isInited) {
			cachedVolume = level;
			return;
		}
		if (jmmfPlayer != null) {
			jmmfPlayer.setVolume(level);
		}
	}

	@Override
	public void start() {
		//System.out.println("start");
		if (jmmfPlayer != null) {
//			if (playing) {
//				return;
//			}
	        if (jmmfPlayer.isPlaying()) {
	        	return;
	        }
	        // play at start of media if at end of media
	        if ((getMediaDuration() - getMediaTime()) < 40) {
	            setMediaTime(0);
	        }

	        //playing = true;
	        jmmfPlayer.start();
	        if (!jmmfPlayer.isSynchronousMode()) {
				long sysTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() != JMMFPlayer.PlayerState.STARTED.value) {
					//System.out.println("Poll: " + count + " " + getMediaTime());
					try {
						Thread.sleep(4);
					} catch (InterruptedException ie) {
						
					}
					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
	        }
	        
	        startControllers();
	        
	        if (endTimeWatcher == null) {
	        	endTimeWatcher = new EndOfTimeWatcher(250);
	        	endTimeWatcher.setNormalPlayback(true);
	        	endTimeWatcher.setPlaying(true);
	        	endTimeWatcher.start();
	        } else {
	        	endTimeWatcher.setNormalPlayback(true);
	        	endTimeWatcher.setPlaying(true);
	        }

		}

	}

	@Override
	public void stop() {
		//System.out.println("stop");
		if (jmmfPlayer != null) {
//			if (!playing) {
//				return;
//			}
	        if (!jmmfPlayer.isPlaying()) {
	        	return;
	        }

			// stop a stop listening thread
			if (stopThread != null) {
				stopThread.setStopped();
			}
			
			//playing = false;
			jmmfPlayer.pause();
			// stop controller immediately without waiting until the player is actually stopped
			stopControllers();
			if (endTimeWatcher != null) {
				endTimeWatcher.setPlaying(false);
			}
			
			if (!jmmfPlayer.isSynchronousMode()) {
				// wait until the player is in the paused state, but not indefinitely
				long sysTime = System.currentTimeMillis();
				while (jmmfPlayer.getState() != JMMFPlayer.PlayerState.PAUSED.value) {
					//System.out.println("Poll: " + count + " " + getMediaTime());
					try {
						Thread.sleep(4);
					} catch (InterruptedException ie) {
						
					}
					if (System.currentTimeMillis() - sysTime > SET_MT_TIMEOUT) {
						break;
					}
				}
			}
			// to late to stop the controllers here?
//				stopControllers();
			//System.out.println("Paused at " + getMediaTime());
			setControllersMediaTime(getMediaTime());
			jmmfPlayer.repaintVideo();
			// stop a bit before the end of media because the player jumps to 0 when reaching the end
			// canceling the stop timer would be better
			if (jmmfPlayer.getStopTime() != duration - eomBuffer) {
				setStopTime(duration - eomBuffer);
			}

		}

	}

	@Override
	public void updateLocale() {
		if (saveNatSizeItem != null) {
			saveNatSizeItem.setText(ElanLocale.getString("Player.SaveFrame.NaturalSize")); 
		}
		if (mouseAdapter != null) {
			mouseAdapter.updateLocale();
		}
	}

	@Override
	public void controllerUpdate(ControllerEvent event) {
	}

	/**
	 * Returns the current image; it is retrieved from the renderer, 
	 * so the size might not be the original size.
	 */
	@Override
	public Image getCurrentFrameImage() {
		return getFrameImageForTime(getMediaTime());
	}

	/**
	 * Currently returns the current image.
	 */
	@Override
	public Image getFrameImageForTime(long time) {
		if (jmmfPlayer == null) {
			return null;
		}

		if (jmmfPlayer.isPlaying()) {
			stop();
		}
		
        if (time != getMediaTime()) {
            setMediaTime(time);
        }

        // pass a header object as argument, it will be filled by the JNI code.
        // the image data array, without header, is returned
        BufferedImage image = null;
        DIBInfoHeader dih = new DIBInfoHeader();
        byte[] data = jmmfPlayer.getCurrentImageData(dih);
        image = DIBToImage.DIBDataToBufferedImage(dih, data);
		return image;
	}
	
	/**
	 * Extracts the video frame corresponding to the specified media time using the 
	 * encoded width and height. Uses a separate Source Reader/ decoder, independent
	 * of the media player.
	 *  
	 * @param time the media time to get the frame image from
	 * @return the frame image or null if anything goes wrong in the process
	 */
	private Image getFrameImageNaturalSizeForTime(long time) {
		if (frameGrabber == null) {
			frameGrabber = new JMMFFrameGrabber(mediaUrlString);
		}
		
		return frameGrabber.getVideoFrameImage(getMediaTime());
	}

    /**
     * Check the setting for correct-at-pause behavior.
     */
	@Override
	public void preferencesChanged() {
		Boolean val = Preferences.getBool("Windows.JMMFPlayer.CorrectAtPause", null);
		
		if (val != null) {
			JMMFPlayer.correctAtPause(val.booleanValue());
		}
    	
    	val = Preferences.getBool("MediaNavigation.Pre47FrameStepping", null);
    	
    	if (val != null) {
    		pre47FrameStepping = val;
    	} 
    	
    	val = Preferences.getBool("NativePlayer.DebugMode", null);
    	
    	if (val != null) {
    		JMMFPlayer.enableDebugMode(val.booleanValue());
    		JMMFFrameGrabber.setDebugMode(val.booleanValue());
    	} 
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == saveNatSizeItem) {
        	ImageExporter export = new ImageExporter();
        	export.exportImage(getFrameImageNaturalSizeForTime(getMediaTime() + offset), 
        			mediaDescriptor.mediaURL, getMediaTime() + offset);
        }
	}
//##############
	
	// hier test thread for setting media time ?
	/**
	 * Sets the media position of the player, waits till the operation is finished 
	 * and then updates the controllers.
	 */
	/*
	private class SetMediaPositionThread extends Thread {
		long time;
		long offset = 0;
		
		public SetMediaPositionThread(long time, long offset) {
			super();
			this.time = time;
			this.offset = offset;
		}

		public void run() {
			//jmmfPlayer.setMediaTime(time + offset);
			while (jmmfPlayer.getMediaTime() != time + offset) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException ie){
					return;
				}
			}
			setControllersMediaTime(time);
			//jmmfPlayer.repaintVideo();
		}
	}
	*/
	/*
	private class SetMediaPosQueued extends Thread {
		private ArrayDeque<long[]> queue;
		private final Object LOCK = new Object();
		
		public SetMediaPosQueued() {
			queue = new ArrayDeque<long[]>(10);
		}
		
		public void add(long[] timepair) {
			synchronized (LOCK) {
				queue.add(timepair);
			}
		}
		
		public void run () {
			while (true) {
				while (queue.isEmpty()) {
					try {
					    sleep(40);
					} catch (InterruptedException ie) {
						System.out.println("Interrupted while waiting...");
					}
				}
				
				long[] next;
				synchronized (LOCK) {
					next = queue.poll();
				}
				
				if (next != null) {
					System.out.println("Setting pos: " + next[0]);
					jmmfPlayer.setMediaTime(next[0] + next[1]);
					while (jmmfPlayer.getMediaTime() != next[0] + next[1]) {
						try {
							Thread.sleep(5);
						} catch (InterruptedException ie){
							
						}
					}
					jmmfPlayer.repaintVideo();
					setControllersMediaTime(next[0]);
				}
			}
		}
	}
	*/
	/**
	 * TODO Revise, add a simple isInited to JMMFPlayer?
	 * Waits until the player is initiated and then stores, caches 
	 * some properties of the media.
	 */
	private class InitWaitThread extends Thread {
		final  int MAX_TRIES = 30;
		final int SLEEP = 200;
		int count = 0;
		
		@Override
		public void run() {
			int state = 0;
			do {
				state = jmmfPlayer.getState();
				count++;
				if (state >= JMMFPlayer.PlayerState.STARTED.value && 
						state < JMMFPlayer.PlayerState.CLOSING.value) {
					isInited = true;
					StringBuilder sb = new StringBuilder("JMMFMediaPlayer: Init Session\n");
					sb.append("\tAspect Ratio: ").append(jmmfPlayer.getAspectRatio() + "\n");
					sb.append("\tDuration: " ).append(jmmfPlayer.getDuration() + "\n");
					sb.append("\tTime Per Frame: ").append(jmmfPlayer.getTimePerFrame() + "\n");

					origDuration = jmmfPlayer.getDuration();
					//origAspectRatio = jmmfPlayer.getAspectRatio();
					int [] ar = jmmfPlayer.getPreferredAspectRatio();
					if (ar != null && ar.length == 2) {
						origAspectRatio = ar[0] / (float) ar[1];
						if (Math.abs(origAspectRatio - jmmfPlayer.getAspectRatio()) > 0.0000001) {
							sb.append("\tPreferred Aspect Ratio: ").append(String.valueOf(origAspectRatio));
						}
					}
					
					if (LOG.isLoggable(Level.INFO)) {
						LOG.log(Level.INFO, sb.toString());
					}
					millisPerSample = jmmfPlayer.getTimePerFrame();
					eomBuffer = (long) (numFramesEOMBuffer * millisPerSample);

					setVolume(cachedVolume);
					setRate(cachedRate);
					if (layoutManager != null) {
						layoutManager.doLayout();
					} else {
						LOG.info("No LayoutManager set yet");
					}
					
					if (LOG.isLoggable(Level.INFO)) {
						LOG.log(Level.INFO, String.format(
								"JMMFMediaPlayer initialized in %d times %d ms", 
								count, SLEEP));
					}
					break;
				}
				try {
					Thread.sleep(SLEEP);
				} catch (InterruptedException ie) {
					
				}
				if (count > MAX_TRIES) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, String.format(
								"JMMFMediaPlayer waited for initialization for %d times %d ms without success", 
								count, SLEEP));
					}
					// conditionally retry native player creation in certain cases? 
					
					break;
				}
			} while (true);
		}
	}
	
	 /**
     * Class to take care of state changes after the player finished
     * playing an interval or reached end of media  Active
     * callback does not seem possible due to threading issues in JNI and MMF?
     */
    private class PlayerStateWatcher extends Thread {
    	// default sleep time of 250 ms
    	private int sleepInterval = 250;
		private boolean stopped = false;
		private final int MAX_SLEEP = 3; // break the waiting if sleep time is less than this
		
    	/**
    	 * Constructor.
    	 * 
    	 * @param sleepInterval the number of ms to sleep in between tests
    	 */
    	public PlayerStateWatcher(int sleepInterval) {
			super();
			if (sleepInterval > 0) {
				this.sleepInterval = sleepInterval;
			}
		}
    	
    	public void setStopped() {
    		stopped = true;
    	}
    	
        /**
         * Check the player state.
         */
        @Override
		public void run() {
        	long refTime = stopTime;
        	long curTime;
        	
            while (!stopped /*&& (getMediaTime() < refTime)*/) {
            	curTime = getMediaTime();
            	if (curTime >= refTime) {
            		break;
            	} else if (refTime - curTime <= sleepInterval) {
            		sleepInterval = (int) Math.max((refTime - curTime) / 2 - MAX_SLEEP, MAX_SLEEP);
            	}
//            	System.out.println("M time: " + getMediaTime() + " (" + refTime + ")");
//            	System.out.println("Sleep: " + sleepInterval);
                try {
                    Thread.sleep(sleepInterval);
                } catch (InterruptedException ie) {
                    //ie.printStackTrace();
                	return;
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                if (!jmmfPlayer.isPlaying()) {
                	break;
                }
            }
            
            if (stopped) {
            	isPlayingInterval = false;
            	return;
            }
            syncLock.lock();
            try {
	            if (jmmfPlayer.isPlaying()) {// in case pausing in the native player didn't succeed
	            	JMMFMediaPlayer.this.stop();
	                stopControllers();
	                jmmfPlayer.setMediaTime(refTime + offset);
	                setControllersMediaTime(getMediaTime());
	                setStopTime(duration - eomBuffer);
	                //playing = false;
	            } else
	            /*if (playing)*/ { //if at stop time (i.e. not stopped by hand) do some extra stuff
	            	//System.out.println("Player at stop time");
	                stopControllers();
	                jmmfPlayer.setMediaTime(refTime + offset);	                
	                setControllersMediaTime(getMediaTime());
	                setStopTime(duration - eomBuffer);
	                //playing = false;
	            }
	            // or check the paused state here
	            isPlayingInterval = false;
            } finally {
            	syncLock.unlock();
            }
        }
    }
    
    /**
     * A thread that tries to detect whether the media player already reached the end of media
     * and stops connected controllers if so. 
     * The native media player tries to stop playback a few hundred ms before the end of
     * media because when the media foundation player reaches the end the player is stopped 
     * (and as a result jumps back to 0 without "scrubbing" the first frame).
     * 
     * @author Han Sloetjes
     *
     */
    private class EndOfTimeWatcher extends Thread {
    	// default sleep time of 250 ms
    	private int sleepInterval = 250;
    	/** only detect end of file in case of normal playback */
    	private volatile boolean normalPlayback = true;
    	private volatile boolean isPlaying = false;
    	private boolean closed = false;
		/**
		 * Constructor that sets the sleep duration.
		 * 
		 * @param sleepInterval the sleep interval
		 */
		EndOfTimeWatcher(int sleepInterval) {
			super();
			if (sleepInterval > 0) {
				this.sleepInterval = sleepInterval;
			}
		}
    	
		/**
		 * Sets whether the player is in normal playback mode, i.e. whether it 
		 * is not playing a selection but plays until the end of the file.
		 * 
		 * @param normalPlayback a flag to indicate whether the player is in 
		 * normal playback mode
		 */
		public synchronized void setNormalPlayback(boolean normalPlayback) {
			this.normalPlayback = normalPlayback;
		}
		
		/**
		 * Sets the playing state.
		 * 
		 * @param playing
		 */
		public synchronized void setPlaying(boolean isPlaying) {
			this.isPlaying = isPlaying;
			if (isPlaying) {
				notify();
			}
		}
		
		/**
		 * Closes this thread, stops execution.
		 */
		public void close() {
			closed = true;
		}
		
		/**
		 * When active check if the player is at (or close to) the end of the media.
		 */
		@Override
		public void run() {
			while (!closed) {
				try {
					Thread.sleep(sleepInterval);
					
					synchronized(this) {
						while (!isPlaying || !normalPlayback) {
							//System.out.println("Waiting...");
							wait();
						}
					}
				} catch (InterruptedException ie) {
					
				}
				// test for end of media, stop controllers etc.
				long curMediaTime = getMediaTime();
				if (curMediaTime >= getMediaDuration() - eomBuffer) {
					//System.out.println("At end: " + curMediaTime);
					if (jmmfPlayer.isPlaying()) {
						//System.out.println("At end: " + curMediaTime + " player still playing");
		            	JMMFMediaPlayer.this.stop();
		                stopControllers();

		                isPlaying = false;
		            } else {
		            	//System.out.println("At end: " + curMediaTime + " player already stopped.");
		            	// the player reached end of media and rewinded back to the beginning
		                stopControllers();

		                isPlaying = false;
		            }
				} else if (curMediaTime == 0){// maybe the media player isn't playing anymore, time = 0??
					if (jmmfPlayer.isPlaying()) {
						//System.out.println("Rewinded to: " + curMediaTime + " player is playing.");
		            	JMMFMediaPlayer.this.stop();
		                stopControllers();

		                isPlaying = false;
					} else {
						//System.out.println("Rewinded to: " + curMediaTime + " player stopped.");
		                stopControllers();

		                isPlaying = false;
					}
				} else if (jmmfPlayer.getState() == JMMFPlayer.PlayerState.PAUSED.value ||
						jmmfPlayer.getState() == JMMFPlayer.PlayerState.STOPPED.value) {
					if (isPlaying) {
		            	JMMFMediaPlayer.this.stop();
		                stopControllers();
		                
						isPlaying = false;
					}
				}
			}
		}
    }
	
	//## VideoScaleAndMove interface ##
	// implementation interface for scaling and panning of the video
	@Override
	public float getVideoScaleFactor() {
		return jmmfPlayer.getVideoScaleFactor();
	}

	@Override
	public void setVideoScaleFactor(float scaleFactor) {
		jmmfPlayer.setVideoScaleFactor(scaleFactor);
		jmmfPlayer.repaintVideo();	
	}

	@Override
	public void repaintVideo() {	
		jmmfPlayer.repaintVideo();
	}

	@Override
	public int[] getVideoBounds() {
		int[] xy = jmmfPlayer.getVideoTranslation();
		int[] wh = jmmfPlayer.getScaledVideoRect();
		return new int[]{xy[0], xy[1], wh[0], wh[1]};
	}

	@Override
	public void setVideoBounds(int x, int y, int w, int h) {
		jmmfPlayer.setVideoDestinationPos(x, y, w, h);
		jmmfPlayer.repaintVideo();
	}

	@Override
	public void moveVideoPos(int dx, int dy) {
		jmmfPlayer.moveVideoPos(dx, dy);
		jmmfPlayer.repaintVideo();
	}

}
