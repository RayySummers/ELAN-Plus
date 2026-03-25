package mpi.eudico.client.annotator.viewer;

import mpi.eudico.client.annotator.ActiveAnnotation;
import mpi.eudico.client.annotator.ActiveAnnotationUser;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.MediaPlayerUser;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.PreferencesUser;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.SelectionUser;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.VolumeManager;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

import javax.swing.JComponent;


/**
 * Abstract base class for viewers that provide a view on and/or visualization
 * of (the contents of) a transcription (mostly of tiers and annotations), 
 * audio files, timeseries data, comments, metadata etc.  
 */
@SuppressWarnings("serial")
public abstract class AbstractViewer extends JComponent
    implements ControllerListener, MediaPlayerUser, SelectionUser,
        ActiveAnnotationUser, ElanLocaleListener, Viewer,
        PreferencesUser {
    private ViewerManager2 viewerManager;
    private ElanMediaPlayer player;
    private Selection selection;
    private ActiveAnnotation activeAnnotation;

    /**
     * Creates a new viewer instance.
     */
    public AbstractViewer() {
		super();
	}
    
    // ControllerListener methods

	/*
     * Notification for a ControllerListener that a media related event happened.
     * This method is called by a separate thread for each event. Therefore
     * the actual implementation of this method might need to take care of
     * problems caused by more than one thread being active in the
     * controllerUpdate method. There are 3 options:
     *
     *        1. do nothing
     *
     *        2. make the method synchronized:
     *                public synchronized void controllerUpdate(ControllerEvent e) {
     *
     *        3. discard events that come while another is being handled:
     *                public void controllerUpdate(ControllerEvent e) {
     *                    synchronized(this) {                	
     *                        if (handlingEvent) {
     *                            return;
     *                        }
     *                        handlingEvent = true;
     *                    }
     *
     *                    .... DO YOUR THING HERE
     *
     *
     *                    handlingEvent = false;
     *                }
     *                
     *           but note the race condition after testing handlingEvent but 
     *           before setting it, unless the check is also synchronized. 
     *
     */
    /**
     * Notification for a ControllerListener that a media related event happened.
     * This method is called on a separate thread for each event. Therefore
     * the actual implementation of this method might need to take care of
     * problems caused by more than one thread being active in the
     * controllerUpdate method.
     * 
     * @param event the controller event
     */
    @Override
	public abstract void controllerUpdate(ControllerEvent event);

    // viewer manager
    /**
     * Sets the {@code ViewerManager2} this viewer is connected to.
     * The viewer manager creates, connects and destroys viewers and media 
     * players, provides access to shared objects like the active annotation,
     * the time selection etc.
     * 
     *  @param viewerManager the viewer manager
     */
    @Override
	public void setViewerManager(ViewerManager2 viewerManager) {
        this.viewerManager = viewerManager;
    }

    /**
     * Returns the viewer manager this viewer is connected to.
     *
     * @return the {@code ViewerManager2}
     */
    @Override
	public ViewerManager2 getViewerManager() {
        return viewerManager;
    }

    /*
     * This is a wrapper around the controllerUpdate method to make sure that
     * all viewers implement it synchronized. Maybe for some viewers it might
     * be a better solution to discard events instead of synchronizing on them
     * but this is the preferred solution, a viewer must in principle be fast
     * enough to handle all the events it gets.
     */

    //    public synchronized void synchronizedControllerUpdate(ControllerEvent event) {
    //		controllerUpdate(event);
    //	}
    
    // MediaPlayerUser methods
    /**
     * Set the player that receives all the player commands from this viewer.
     *
     * @param player the main media player
     */
    @Override
	public void setPlayer(ElanMediaPlayer player) {
        this.player = player;
    }

    /**
     * Starts the master media player if not {@code null}.
     */
    @Override
	public void startPlayer() {
        if (player == null) {
            return;
        }

        player.start();
    }

    /**
     * Play between a start and a stop time. If the player's media position is
     * not equal to the start time, it should first jump to the start time.
     *
     * @param startTime the start time of the interval
     * @param stopTime the stop time of the interval
     */
    @Override
	public void playInterval(long startTime, long stopTime) {
        if (player == null) {
            return;
        }

        player.playInterval(startTime, stopTime);
    }

    /**
     * Stops the master media player.
     * This is equivalent to the {@code pause} command of many native media
     * frameworks. The intent is that the player pauses at the current position
     * and stays ready to be restarted.
     */
    @Override
	public void stopPlayer() {
        if (player == null) {
            return;
        }

        player.stop();
        viewerManager.getMediaPlayerController().stopLoop();
    }

    /**
     * Returns a boolean that tells if the player is playing.
     *
     * @return {@code true} if the player is in the playing state, 
     * {@code false} otherwise
     */
    @Override
	public boolean playerIsPlaying() {
        if (player == null) {
            return false;
        }

        return player.isPlaying();
    }

    /**
     * Sets the player's media time in milliseconds.
     *
     * @param milliSeconds the media time to jump to
     */
    @Override
	public void setMediaTime(long milliSeconds) {
        if (player == null) {
            return;
        }

        player.setMediaTime(milliSeconds);
        viewerManager.getMediaPlayerController().stopLoop();
    }

    /**
     * Returns the player's media time in milliseconds.
     *
     * @return the current media time in milliseconds
     */
    @Override
	public long getMediaTime() {
        if (player == null) {
            return 0;
        }

        return player.getMediaTime();
    }

    /**
     * Sets the media player's playback rate.
     *
     * @param rate the new rate, 1 means normal speed, &gt;1 means fast playback,
     * &lt;1 indicates slow motion
     */
    @Override
	public void setRate(float rate) {
        if (player == null) {
            return;
        }

        player.setRate(rate);
    }

    /**
     * Returns the current playback rate of the media player.
     *
     * @return the current rate
     */
    @Override
	public float getRate() {
        if (player == null) {
            return 0;
        }

        return player.getRate();
    }

    /**
     * Returns the media duration in milliseconds.
     *
     * @return the duration of the media as known to or interpreted by the player
     */
    @Override
	public long getMediaDuration() {
        if (player == null) {
            return 0;
        }

        return player.getMediaDuration();
    }
    
    /**
     * Notification of the fact that the offset (and therefore the duration) 
     * of the media player changed. Does nothing by default.
     */
    @Override
	public void mediaOffsetChanged() {
    	repaint();
    }

    /**
     * Returns the sound volume as a number between {@code 0} and {@code 1}.
     *
     * @return the current volume
     */
    @Override
	public float getVolume() {
        if (player == null) {
            return 0;
        }

        return viewerManager.getVolumeManager().getSubVolume(player);
    }

    /**
     * Sets the sub volume as a number between {@code 0} and {@code 1}.
     * The sub volume can be used to mix the audio from multiple streams.
     * The default implementation delegates the call to the {@code VolumeManager}.
     *  
     * @param level the sub volume
     */
    @Override
	public void setVolume(float level) {
        if (player == null) {
            return;
        }

        viewerManager.getVolumeManager().setSubVolume(player, level);
    }

    // SelectionUser methods

    /**
     * Sets the {@code Selection} object that contains the selection for this
     * Viewer. That object holds the selection begin time and selection end
     * time, if these are equal this means there is no selection.
     * 
     * @param selection the {@code Selection} object
     */
    @Override
	public void setSelectionObject(Selection selection) {
        this.selection = selection;
    }

    /**
     * Sets the selection begin and end time in milliseconds.
     * Forwards the information to {@link Selection#setSelection(long, long)}.
     * 
     * @param begin the selection begin time
     * @param end the selection end time
     */
    @Override
	public void setSelection(long begin, long end) {
        if (selection == null) {
            return;
        }

        if (begin == end) {
        	selection.setSelection(begin, end);
        	return;
        }

    	if (getViewerManager().getMultiTierControlPanel() != null) {
    		TierImpl constrainingTier = (TierImpl) getViewerManager()
        		.getMultiTierControlPanel().getActiveTier();
    	
	        if (constrainingTier != null) {
	            if (constrainingTier.getLinguisticType()
	                     .hasConstraints()) {
	                Constraint c = constrainingTier.getLinguisticType()
	                                .getConstraints();
	                long[] segment = { begin, end };
					
					c.forceTimes(segment, constrainingTier.getParentTier());

					begin = segment[0];
					end = segment[1];
	            }
	        }
    	}
    	
        selection.setSelection(begin, end);
    }
    
    /**
     * Empty implementation. A viewer can commit or discard pending edits and
     * clean up resources here.
     */
    public void isClosing(){
    	
    }

    /**
     * Sets the selection to the boundaries of the annotation.
     * 
     * @param annotation the annotation providing the new begin and end time 
     * for the selection
     */
    public void setSelection(Annotation annotation) {
        if (selection == null) {
            return;
        }

        selection.setSelection(annotation);
    }
    
    /**
     * Returns the selection begin time in milliseconds.
     * 
     * @return the begin time of the selection
     */
    @Override
	public long getSelectionBeginTime() {
        if (selection == null) {
            return 0;
        }

        return selection.getBeginTime();
    }

    /**
     * Returns the selection end time in milliseconds.
     * 
     * @return the end time of the selection
     */
    @Override
	public long getSelectionEndTime() {
        if (selection == null) {
            return 0;
        }

        return selection.getEndTime();
    }

    /**
     * Called when the selection has been changed.
     * The viewer must implement this method and take action to
     * update the selection in its view.
     */
    @Override
	public abstract void updateSelection();

    // ActiveAnnotationUser related methods
    /**
     * Sets the active annotation object.
     * This object wraps an annotation and notifies listeners if this annotation
     * is replaced by another annotation or by {@code null}.
     * 
     * @param activeAnnotation the {@code ActiveAnnotation} object
     */
    @Override
	public void setActiveAnnotationObject(ActiveAnnotation activeAnnotation) {
        this.activeAnnotation = activeAnnotation;
    }

    /**
     * Returns the {@code Annotation} that is currently wrapped by the 
     * {@code ActiveAnnotation} object.
     *
     * @return the active {@code Annotation} or {@code null}
     */
    @Override
	public Annotation getActiveAnnotation() {
        if (activeAnnotation == null) {
            return null;
        }

        return activeAnnotation.getAnnotation();
    }

    /**
     * Sets the active {@code Annotation}.
     *
     * @param annotation the new active annotation
     */
    @Override
	public void setActiveAnnotation(final Annotation annotation) {
        if (activeAnnotation == null) {
            return;
        }

        Command c = ELANCommandFactory.createCommand(getViewerManager()
                                                         .getTranscription(),
                ELANCommandFactory.ACTIVE_ANNOTATION);
        c.execute(getViewerManager(), new Object[] { annotation });
    }

    /**
     * Abstract method to be implemented by all abstract viewers, notification
     * that the active annotation changed. 
     * This is not nice because not all viewers are meant to render Annotation
     * related data, for example SignalViewer, and therefore should not be 
     * bothered with this update call. Maybe we can make for this (and other 
     * abstract methods) an empty implementation. Thereby we no longer force 
     * the viewer that extends AbstractViewer to implement the method.
     */
    @Override
	public abstract void updateActiveAnnotation();

    /**
     * Called when the {@code Locale} changed.
     * The viewer must implement this method and take action to
     * update the locale.
     */
    @Override
	public abstract void updateLocale();

	/**
	 * Sets a preference value for a specific key and possibly for a specific
	 * document. If {@code document} is {@code null}, it is a global preference
	 * setting.
	 * 
	 * @param key the preference key
	 * @param value the value to store
	 * @param document the document this preference is for, {@code null} indicates
	 * a global setting
	 * 
	 * @see mpi.eudico.client.annotator.PreferencesUser#setPreference(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setPreference(String key, Object value, Object document) {
		if (document instanceof Transcription) {
			Preferences.set(key, value, (Transcription)document, false, false);
		} else {
			Preferences.set(key, value, null, false, false);
		}		
	}
	
	/**
	 * Returns the stored preference for the specified key, or {@code null}.
	 * 
	 * @deprecated because Preferences now has some typed methods for
	 * retrieving preferences settings.
	 * 
	 * @param key the key
	 * @param document the transcription
	 * @return the preference object or {@code null}
	 */
	@Deprecated
	protected Object getPreference(String key, Transcription document) {
		return Preferences.get(key, document);
	}

	/**
	 * Notification of a change in preferences.
	 * Viewers may need to update their view based on new settings.
	 * 
	 * @see mpi.eudico.client.annotator.PreferencesListener#preferencesChanged()
	 */
	@Override
	public abstract void preferencesChanged();
}
