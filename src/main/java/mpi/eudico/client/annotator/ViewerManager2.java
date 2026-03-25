/*
 * Created on Sep 22, 2003
 *
 *
 */
package mpi.eudico.client.annotator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.grid.GridViewer;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.EmptyMediaPlayer;
import mpi.eudico.client.annotator.player.NoPlayerException;
import mpi.eudico.client.annotator.player.PlayerFactory;
import mpi.eudico.client.annotator.player.WAVSamplerFactory;
import mpi.eudico.client.annotator.recognizer.gui.RecognizerPanel;
import mpi.eudico.client.annotator.search.result.viewer.ElanResultViewer;
import mpi.eudico.client.annotator.transcriptionMode.TranscriptionViewer;
import mpi.eudico.client.annotator.turnsandscenemode.TurnsAndSceneViewer;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.annotator.viewer.AnnotationDensityViewer;
import mpi.eudico.client.annotator.viewer.CommentViewer;
import mpi.eudico.client.annotator.viewer.InterlinearViewer;
import mpi.eudico.client.annotator.viewer.LexiconEntryViewer;
import mpi.eudico.client.annotator.viewer.MetadataViewer;
import mpi.eudico.client.annotator.viewer.MultiTierControlPanel;
import mpi.eudico.client.annotator.viewer.MultiTierViewer;
import mpi.eudico.client.annotator.viewer.SegmentationViewer2;
import mpi.eudico.client.annotator.viewer.SignalViewer;
import mpi.eudico.client.annotator.viewer.SignalViewerControlPanel;
import mpi.eudico.client.annotator.viewer.SingleTierViewer;
import mpi.eudico.client.annotator.viewer.SingleTierViewerPanel;
import mpi.eudico.client.annotator.viewer.SpectrogramViewer;
import mpi.eudico.client.annotator.viewer.SubtitleViewer;
import mpi.eudico.client.annotator.viewer.TextViewer;
import mpi.eudico.client.annotator.viewer.TimeLineViewer;
import mpi.eudico.client.annotator.viewer.TimeSeriesViewer;
import mpi.eudico.client.annotator.viewer.Viewer;
import mpi.eudico.client.mediacontrol.Controller;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.PeriodicUpdateController;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.client.mediacontrol.TimeLineController;
import mpi.eudico.client.util.WAVSamplesProvider;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.server.corpora.util.ACMEditableDocument;
import nl.mpi.util.FileUtility;

/**
 * A ViewerManager must manage the viewer world that is created around a
 * Transcription. It takes care of creating, destroying, enabling and
 * disabling viewers and media players ensuring that all connections between
 * controllers and listeners are as they should be.
 * 
 * Note: all create...Viewer, get...Viewer, destroy...Viewer methods should be
 * replaced by using generic methods. Ideally the manager wouldn't have local,
 * named fields for known single instance viewers and panels. (Viewers and 
 * panels could be stored in a map with unique names as keys.  
 * 
 * @version Aug 2005 Identity removed
 */
public class ViewerManager2 {
	/** The default time between controller updates sent to the SignalViewer,
	 *  50 milliseconds */
	private final static long SIGNAL_VIEWER_PERIOD = 50; // make these values setable?

	/** The default time between controller updates sent to the TimeLine Viewer,
	 *  50 milliseconds */
	private final static long TIME_LINE_VIEWER_PERIOD = 50;

	/** The default time between controller updates sent to the Interlinear Viewer,
	 *  100 milliseconds */
	private final static long INTERLINEAR_VIEWER_PERIOD = 100;

	/** The default time between controller updates sent to the Media Control Panel,
	 *  100 milliseconds */
	private final static long MEDIA_CONTROL_PANEL_PERIOD = 100;
	private ElanMediaPlayer masterMediaPlayer;
	private ElanMediaPlayer signalSourcePlayer;
	private SignalViewer signalViewer;
	private RecognizerPanel recognizerPanel;
	private TranscriptionImpl transcription;
	private Selection selection;
	private TimeScale timeScale;
	private ActiveAnnotation activeAnnotation;
	private TierOrder tierOrder;
	private ElanMediaPlayerController mediaPlayerController;
	private AnnotationDensityViewer annotationDensityViewer;
	private MediaPlayerControlSlider mediaPlayerControlSlider;
	private TimePanel timePanel;
	private MultiTierControlPanel multiTierControlPanel;
	private SignalViewerControlPanel signalViewerControlPanel;
	private List<ElanMediaPlayer> connectedMediaPlayers;
	private List<ElanMediaPlayer> disabledMediaPlayers;
	private Map<Object, Controller> controllers;
	private List<AbstractViewer> viewers;
	private List<AbstractViewer> enabledViewers;
	private List<AbstractViewer> disabledViewers;
	private MetadataViewer metadataViewer;
	
	private GridViewer gridViewer;
	private TimeLineViewer timeLineViewer;
	private TextViewer textViewer;
	private LexiconEntryViewer lexiconViewer;
	private CommentViewer commentViewer;
	private TurnsAndSceneViewer turnsAndSceneViewer;
	private List<SubtitleViewer> subtitleViewers;	
	private InterlinearViewer interlinearViewer;	
	private TranscriptionViewer transcriptionViewer;
	private SpectrogramViewer spectrogramViewer;
	private String signalMediaURL;
	private List<String> audioPaths;
	private List<String> videoPaths;
	private List<String> otherMediaPaths;

	private VolumeManager volumeManager;
	
	/** The maximal number of video players in ELAN */
	public static final int MAX_NUM_VIDEO_PLAYERS = 2; // will be 4
	/** The maximal number of audio players in ELAN */
	public static final int MAX_NUM_AUDIO_PLAYERS = 1;

	/**
	 * Create a ViewerManager for a specific Transcription.
	 *
	 * @param transcription the Transcription used in this ViewerManager's
	 *        universe
	 */
	public ViewerManager2(TranscriptionImpl transcription) {
		this.transcription = transcription;

		// as long as no real media player is set as master player use
		// an empty media player.
		masterMediaPlayer = new EmptyMediaPlayer(Integer.MAX_VALUE);

		// observables for this viewer universe
		selection = new Selection();
		timeScale = new TimeScale();
		activeAnnotation = new ActiveAnnotation();
		
		createTierOrderObject();
		//tierOrder = new TierOrder();

		// administration objects
		connectedMediaPlayers = new ArrayList<ElanMediaPlayer>();
		disabledMediaPlayers = new ArrayList<ElanMediaPlayer>();
		controllers = new HashMap<Object, Controller>();
		viewers = new ArrayList<AbstractViewer>();
		subtitleViewers = new ArrayList<SubtitleViewer>();
		enabledViewers = new ArrayList<AbstractViewer>();
		disabledViewers = new ArrayList<AbstractViewer>();
		
		audioPaths = new ArrayList<String>();
		videoPaths  = new ArrayList<String>();
		otherMediaPaths = new ArrayList<String>();
	}
	
	private void createTierOrderObject(){	
		tierOrder = new TierOrder(transcription);
		connectListener(tierOrder);
		List<TierImpl> tiers = transcription.getTiers();
		List<String> tierOrderList = Preferences.getListOfString("MultiTierViewer.TierOrder", 
			transcription);
		
		if (tierOrderList != null) {				
			// add (new) tiers, tiers that are not in the preferences
			for (int i = 0; i < tierOrderList.size(); i++) {
				Tier t = transcription.getTierWithId(tierOrderList.get(i));					
				if ( t == null ) {
					tierOrderList.remove(i);
					i--;
				}					
			}	
			
			for (Tier t : tiers) {
				if ( !tierOrderList.contains(t.getName()) ) {
					tierOrderList.add(t.getName()); 
				}					
			}				
		} else{
			tierOrderList = new ArrayList<String>();
			for (Tier t : tiers) {
				tierOrderList.add(t.getName());
			}				
		}
		
		if(tierOrderList instanceof ArrayList){
			tierOrder.setTierOrder(tierOrderList);
		} else {
			tierOrder.setTierOrder(new ArrayList<String>(tierOrderList));
		}			
	}

	/**
	 * Currently the returned {@code Transcription} is a {@code TranscriptionImpl}
	 * instance and it is often cast to that.
	 *
	 * @return the {@code Transcription} object for this viewer universe
	 */
	public Transcription getTranscription() {
		return transcription;
	}

	/**
	 * Returns the {@code Selection} object. 
	 *
	 * @return the {@code Selection} object for this viewer universe
	 */
	public Selection getSelection() {
		return selection;
	}

	/**
	 * Returns the global {@code TimeScale} object.
	 *
	 * @return the {@code TimeScale} object for this viewer universe
	 */
	public TimeScale getTimeScale() {
		return timeScale;
	}

	/**
	 * Returns the {@code ActiveAnnotation} object.
	 *
	 * @return the {@code ActiveAnnotation} object for this viewer universe
	 */
	public ActiveAnnotation getActiveAnnotation() {
		return activeAnnotation;
	}
	
	/**
	 * Returns the global {@code TierOrder} object.
	 *
	 * @return the {@code TierOrder} object for this viewer universe
	 */
	public TierOrder getTierOrder() {
		return tierOrder;
	}


	/**
	 * Makes an ElanMediaPlayer master media player. The current master media
	 * player becomes connected to the new master. The old master media player
	 * should be destroyed separately if it is no longer needed.
	 *
	 * @param player the ElanMediaPlayer that must become master player
	 */
	public void setMasterMediaPlayer(ElanMediaPlayer player) {
		if (player == masterMediaPlayer) {
			return;
		}

		// remember the rate of the current master
		float rate = masterMediaPlayer.getRate();

		// make sure all current master media player connections are removed
		// disconnect connected players
		for (ElanMediaPlayer mp : connectedMediaPlayers) {
			masterMediaPlayer.removeController(mp);
		}

		// disconnect the non-player controllers, TimeLine and PeriodicUpdate
		for (Controller c : controllers.values()) {
			masterMediaPlayer.removeController(c);
			
			// check TimeLineControllers
			if (c instanceof TimeLineController) {
				((TimeLineController) c).setControllongPlayer(null);
			}
		}

		// remove the new master player from the connected or disabled list
		// and add the current master player to the connected list
		connectedMediaPlayers.remove(player);
		disabledMediaPlayers.remove(player);
		connectedMediaPlayers.add(masterMediaPlayer);

		// set the master
		masterMediaPlayer = player;

		// connect the new master media player to the viewer universe
		// reconnect disconnected players
		for (ElanMediaPlayer mp : connectedMediaPlayers) {
			masterMediaPlayer.addController(mp);
		}

		// reconnect the non-player controllers, TimeLine and PeriodicUpdate
		for (Controller c : controllers.values()) {
			masterMediaPlayer.addController(c);
			
			if (c instanceof TimeLineController) {
				((TimeLineController) c).setControllongPlayer(masterMediaPlayer);
			}
		}

		// set the player in all existing viewers
		for (AbstractViewer v : viewers) {
			v.setPlayer(masterMediaPlayer);
		}

		// set the rate
		masterMediaPlayer.setRate(rate);

		// Make sure the user hears the new master media.
		// This may be overridden again in the next paragraph
		// (but this is set to get something sensible in any case).
		getVolumeManager().setSimpleVolumes();
		
		if (mediaPlayerController != null) {
			mediaPlayerController.updatePlayersVolumePanel();
		}
	}

	/**
	 * Creates an ElanMediaPlayer and connects it to the master media player
	 *
	 * @param mediaDescriptor a string representation of the media URL
	 *
	 * @return an ElanMediaPlayer that is connected to the master media player
	 *
	 * @throws NoPlayerException if creation of a media player failed
	 */
	public ElanMediaPlayer createMediaPlayer(MediaDescriptor mediaDescriptor)
		throws NoPlayerException {
		// ask the player factory to create a player
		ElanMediaPlayer player = PlayerFactory.createElanMediaPlayer(mediaDescriptor);

		if (player == null) {
			return null;
		}

		addMediaPlayer(player);
		return player;
	}
	
	/**
	 * Creates an ElanMediaPlayer and connects it to the master media player.
	 * It first tries to create a player of the preferred type; if this fails 
	 * it will try to create a player the default way. 
	 *
	 * @param mediaDescriptor a string representation of the media URL
	 * @param preferredMediaFramework the preferred media framework
	 *
	 * @return an ElanMediaPlayer that is connected to the master media player
	 *
	 * @throws NoPlayerException if creation of a media player failed
	 */
	public ElanMediaPlayer createMediaPlayer(MediaDescriptor mediaDescriptor, 
		String preferredMediaFramework) throws NoPlayerException {
		if (preferredMediaFramework == null) {
			return createMediaPlayer(mediaDescriptor);
		}
		// ask the player factory to create a player
		ElanMediaPlayer player = null;
		StringBuilder errors = new StringBuilder();
		try {
			player = PlayerFactory.createElanMediaPlayer(mediaDescriptor, preferredMediaFramework);
			if (player == null) {
				errors.append(String.format(
					"A player of the requested framework \"%s\" could not be created on this platform\n", 
					preferredMediaFramework));
				return createMediaPlayer(mediaDescriptor);
			}
		} catch (NoPlayerException npe) {
			errors.append(npe.getMessage() + "\n");
			try {
				return createMediaPlayer(mediaDescriptor);
			} catch (NoPlayerException np) {
				errors.append(np.getMessage() + "\n");
			}
		}
		

		if (player == null) {
			throw new NoPlayerException(errors.toString());
			//return null;
		}

		addMediaPlayer(player);
		return player;
	}

	/**
	 * Adds a custom made ELAN media player to the list of connected media
	 * players and connects it to the master media player.
	 * 
	 * @see #destroyMediaPlayer(ElanMediaPlayer)
	 * @param player the player to add
	 */
	public void addMediaPlayer(ElanMediaPlayer player) {
		if (player == null || connectedMediaPlayers.contains(player) ||
				player == masterMediaPlayer) {
			return;
		}

		ElanLocale.addElanLocaleListener(transcription, player);
		connectListener(player);
		player.setRate(masterMediaPlayer.getRate());
		getVolumeManager().setSubVolume(player, 0);

		// connect it to the master media player
		masterMediaPlayer.addController(player);

		// update the administration
		connectedMediaPlayers.add(player);	

		if (mediaPlayerController != null) {
			mediaPlayerController.updatePlayersVolumePanel();
		}
	}
	
	/**
	 * Removes an ElanMediaPlayer from this viewer universe. Nothing will be
	 * done if an attempt is made to remove the master media player
	 *
	 * @param player the ElanMediaPlayer that must be destroyed
	 */
	public void destroyMediaPlayer(ElanMediaPlayer player) {
		if (player == masterMediaPlayer) {
			return;
		}

		ElanLocale.removeElanLocaleListener(player);
		disconnectListener(player);
		// disconnect the player from the master player
		masterMediaPlayer.removeController(player);

		// update the administration, the player is in one of two vectors
		connectedMediaPlayers.remove(player);
		disabledMediaPlayers.remove(player);
		
		player.cleanUpOnClose(); // sometimes crashed with NullPointerException
		player = null;

		if (mediaPlayerController != null) {
			mediaPlayerController.updatePlayersVolumePanel();
		}
	}

	/**
	 * Enables an ElanMediaPlayer that was previously disabled.
	 *
	 * @param player the ElanMediaPlayer that must be enabled.
	 */
	public void enableMediaPlayer(ElanMediaPlayer player) {
		// only enable a player that is a disabled player
		if (disabledMediaPlayers.contains(player)) {
			// reconnect the player to the master player
			masterMediaPlayer.addController(player);

			// update the administration
			connectedMediaPlayers.add(player);
			disabledMediaPlayers.remove(player);

			if (mediaPlayerController != null) {
				mediaPlayerController.updatePlayersVolumePanel();
			}
		}
	}

	/**
	 * Temporarily disconnects the player from the master media player. It can
	 * be reconnected by calling {@code enableElanMediaPlayer} The master media player
	 * will not be disabled.
	 *
	 * @param player the ElanMediaPlayer that must be disabled.
	 * @see #enableMediaPlayer(ElanMediaPlayer)
	 */
	public void disableMediaPlayer(ElanMediaPlayer player) {
		// only disable a player that is a connected player
		if (connectedMediaPlayers.contains(player)) {
			// disconnect the player from the master player
			masterMediaPlayer.removeController(player);

			// update the administration
			connectedMediaPlayers.remove(player);
			disabledMediaPlayers.add(player);

			if (mediaPlayerController != null) {
				mediaPlayerController.updatePlayersVolumePanel();
			}
		}
	}

	/**
	 * Enables all disabled players and connects them to the master player.
	 */
	public void enableDisabledMediaPlayers() {
		for (ElanMediaPlayer mp : disabledMediaPlayers) {
			// reconnect the player to the master player
			masterMediaPlayer.addController(mp);
		}

		// update the administration
		connectedMediaPlayers.addAll(disabledMediaPlayers);
		disabledMediaPlayers.clear();

		if (mediaPlayerController != null) {
			mediaPlayerController.updatePlayersVolumePanel();
		}
	}

	/**
	 * Disable all players except the master player.
	 * 
	 * @see #enableDisabledMediaPlayers()
	 */
	public void disableConnectedMediaPlayers() {
		for (ElanMediaPlayer mp : connectedMediaPlayers) {
			// disconnect the player from the master player
			masterMediaPlayer.removeController(mp);
		}

		// update the administration
		disabledMediaPlayers.addAll(connectedMediaPlayers);
		connectedMediaPlayers.clear();

		if (mediaPlayerController != null) {
			mediaPlayerController.updatePlayersVolumePanel();
		}
	}

	// this must be called from the outside, maybe viewer manager can derive
	// the signalSourcePlayer implicitly. The signal source player is an mpeg
	// or wav player that renders the audio for the wav data that is used in the signal viewer
	/**
	 * Sets the media player that plays the file which is the source (video or 
	 * audio) for the wave file in the {@code SignalViewer}.
	 * 
	 * @param player the media player
	 */
	public void setSignalSourcePlayer(ElanMediaPlayer player) {
		signalSourcePlayer = player;
	}

	/**
	 * Returns the offset of the file in the {@code SignalViewer}.
	 *
	 * @return the offset of the file in the {@code SignalViewer}
	 */
	public long getSignalViewerOffset() {
		long offset = 0;

		if (signalSourcePlayer != null) {
			offset = signalSourcePlayer.getOffset();
		}

		return offset;
	}

	/**
	 * Returns the {@code SignalViewer}, if there is one.
	 * 
	 * @return the {@code SignalViewer} or {@code null}
	 */
	public SignalViewer getSignalViewer() {
		return signalViewer;
	}
	
	/**
	 * Sets the media offset of a specific player.
	 * The offset marks the new starting point of the media, everything before
	 * that point will be skipped.
	 *
	 * @param player the media player
	 * @param offset the new offset 
	 */
	public void setOffset(ElanMediaPlayer player, long offset) {
		player.setOffset(offset);

		if ((player == signalSourcePlayer) && (signalViewer != null)) {
			signalViewer.setOffset(offset);
		}
		transcription.setChanged();
	}

	/**
	 * Returns the main media player.
	 *
	 * @return the ElanMediaPlayer that is the current master media player.
	 */
	public ElanMediaPlayer getMasterMediaPlayer() {
		return masterMediaPlayer;
	}
	
	/**
	 * Returns the collection of connected media players.
	 * 
	 * @return the collection of connected media players
	 */
	public List<ElanMediaPlayer> getConnectedMediaPlayers() {
		return connectedMediaPlayers;
	}

	/**
	 * Returns the media player controller, it is created when it doesn't
	 * exist yet.
	 *
	 * @return the control panel for the master media player
	 */
	public ElanMediaPlayerController getMediaPlayerController() {
		if (mediaPlayerController == null) {
			mediaPlayerController = new ElanMediaPlayerController(this);

			PeriodicUpdateController controller =
				getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD);
			controllers.put(mediaPlayerController, controller);
			connect(mediaPlayerController);
			viewers.add(mediaPlayerController);
			enabledViewers.add(mediaPlayerController);
		}

		return mediaPlayerController;
	}
	
	/**
	 * Removes the media player controller, sets the reference to {@code null}.
	 */
	public void destroyElanMediaPlayerController() {
		if(mediaPlayerController != null){
			destroyViewer(mediaPlayerController);
			mediaPlayerController = null;
		}
	}

	/**
	 * Returns the volume manager, it is created if it doesn't exist yet.
	 * 
	 * @return the volume manager
	 */
	public VolumeManager getVolumeManager() {
		if (volumeManager == null) {
			volumeManager = new VolumeManager(this);
		}
		
		return volumeManager;
	}
	
	/**
	 * Destroys the volume manager, sets the reference to {@code null}.
	 */
	public void destroyVolumeManager() {
		if (volumeManager != null) {
			volumeManager = null;
		}
	}
	
	/**
	 * Returns the media player control slider, it is created if it doesn't
	 * exist yet.
	 *
	 * @return the media control slider
	 */
	public MediaPlayerControlSlider getMediaPlayerControlSlider() {
		if (mediaPlayerControlSlider == null) {
			mediaPlayerControlSlider = new MediaPlayerControlSlider();

			PeriodicUpdateController controller =
				getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD);
			controllers.put(mediaPlayerControlSlider, controller);
			connect(mediaPlayerControlSlider);
			viewers.add(mediaPlayerControlSlider);
			enabledViewers.add(mediaPlayerControlSlider);
		}

		return mediaPlayerControlSlider;
	}

	/**
	 * This creates a annotation density viewer if it does not exist.
	 *
	 * @return the {@code AnnotationDensityViewer}
	 */
	public AnnotationDensityViewer createAnnotationDensityViewer() {
		if (annotationDensityViewer == null) {
			annotationDensityViewer = new AnnotationDensityViewer(transcription);
			
			annotationDensityViewer.setTierOrderObject(tierOrder);
			PeriodicUpdateController controller =
				getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD);
			controllers.put(annotationDensityViewer, controller);
			connect(annotationDensityViewer);
			viewers.add(annotationDensityViewer);
			enabledViewers.add(annotationDensityViewer);
		}

		return annotationDensityViewer;
	}
	
	/**
	 * Returns the {@code AnnotationDensityViewer} or {@code null} if it does
	 * not exist.
	 * <p>
	 * HS Sep 2013: the getter doesn't create the viewer anymore, there's a
	 * separate create method for that (like for most other viewers).
	 *
	 * @return the density viewer or {@code null}
	 */
	public AnnotationDensityViewer getAnnotationDensityViewer() {
		return annotationDensityViewer;
	}

	/**
	 * Returns the {@code TimePanel}, it is created if it doesn't exist yet.
	 *
	 * @return the {@code TimePanel}
	 */
	public TimePanel getTimePanel() {
		if (timePanel == null) {
			timePanel = new TimePanel();

			PeriodicUpdateController controller =
				getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD);
			controllers.put(timePanel, controller);
			connect(timePanel);
			viewers.add(timePanel);
			enabledViewers.add(timePanel);
		}

		return timePanel;
	}
	
	/**
	 * Creates a Viewer for the specified fully qualified class name.
	 * 
	 * @param className the class name
	 * @param controllerPeriod the requested period for controller updates
	 * 
	 * @return the viewer
	 */
	public Viewer createViewer(String className, long controllerPeriod) {
	    Viewer viewer = null; 
	    
	    try {
	        viewer = (Viewer) Class.forName(className).getDeclaredConstructor().newInstance();
	        viewer.setViewerManager(this);
	        if (viewer instanceof AbstractViewer) {
		    		PeriodicUpdateController controller = getControllerForPeriod(controllerPeriod);
		    		controllers.put(viewer, controller);
		    		connect((AbstractViewer) viewer);
	
		    		viewers.add((AbstractViewer)viewer);
		    		enabledViewers.add((AbstractViewer)viewer);
	        } else if (viewer instanceof ControllerListener) {
	            getControllerForPeriod(controllerPeriod).addControllerListener(
	                    (ControllerListener) viewer);
	        } else {
	            // special case for syntax viewer?
	            /*
	            try {
	                Method method = viewer.getClass().getDeclaredMethod("getControllerListener", null);
	                ControllerListener listener = (ControllerListener) method.invoke(viewer, null);
	                getControllerForPeriod(controllerPeriod).addControllerListener(
	                        listener);	                
	            } catch (Exception e){
	                System.out.println("Could not connect controller: " + e.getMessage());
	            }
	            */
	        }
	    } catch (Exception e) {
	        System.out.println("Could not create viewer: " + className + ": " + e.getMessage());
	    }
	    
	    return viewer;
	}
	
	/**
	 * Connection method to be used by external objects that want to connect a
	 * listener.
	 *
	 * @param listener known listener types are added to the relevant object, 
	 * e.g. a {@code SelectionListener} is added to the {@code Selection}
	 */
	public void connectListener(Object listener) {
		if (listener instanceof ControllerListener) {
			getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD).addControllerListener(
				(ControllerListener) listener);
		}

		if (listener instanceof SelectionListener) {
			selection.addSelectionListener((SelectionListener) listener);
		}

		if (listener instanceof ActiveAnnotationListener) {
			activeAnnotation.addActiveAnnotationListener((ActiveAnnotationListener) listener);
		}

		if (listener instanceof TimeScaleListener) {
			timeScale.addTimeScaleListener((TimeScaleListener) listener);
		}

		if (listener instanceof ACMEditListener) {
			try {
				((ACMEditableDocument) transcription).addACMEditListener(
					(ACMEditListener) listener);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (listener instanceof PreferencesListener) {
			Preferences.addPreferencesListener(transcription, (PreferencesListener) listener);
		}
	}

	/**
	 * Method to be used by external objects that want to disconnect a
	 * listener.
	 *
	 * @param listener the listener to disconnect
	 */
	public void disconnectListener(Object listener) {
		if (listener instanceof ControllerListener) {
			getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD).removeControllerListener(
				(ControllerListener) listener);
		}

		if (listener instanceof SelectionListener) {
			selection.removeSelectionListener((SelectionListener) listener);
		}

		if (listener instanceof ActiveAnnotationListener) {
			activeAnnotation.removeActiveAnnotationListener((ActiveAnnotationListener) listener);
		}

		if (listener instanceof TimeScaleListener) {
			timeScale.removeTimeScaleListener((TimeScaleListener) listener);
		}

		if (listener instanceof ACMEditListener) {
			try {
				((ACMEditableDocument) transcription).removeACMEditListener(
					(ACMEditListener) listener);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (listener instanceof PreferencesListener) {
			Preferences.removePreferencesListener(transcription, (PreferencesListener) listener);
		}
	}
	
	/**
	 * Returns the control panel for the {@code SognalViewer}, it is created if
	 * it does not exist yet.
	 *
	 * @return the control panel for the wav files in the {@code SignalViewer}
	 */
	public SignalViewerControlPanel getSignalViewerControlPanel() {
		if(signalViewerControlPanel == null){
			return createSignalViewerControlPanel();
		}
		return signalViewerControlPanel;
	}
	
	/**
	 * Creates and returns the control panel for the {@code SognalViewer}.
	 * If it hadn't been created yet, it is created now.
	 * 
	 * @return the control panel for the wav files in the {@code SignalViewer}
	 */
	public SignalViewerControlPanel createSignalViewerControlPanel(){
		if(signalViewerControlPanel == null){
			signalViewerControlPanel = new SignalViewerControlPanel(
				((ElanFrame2)ELANCommandFactory.getRootFrame(transcription)).getWaveFormViewerMenuManager());
		}
		
		return signalViewerControlPanel;
	}
	
	
	/**
	 * Destroys the control panel for the {@code SognalViewer}.
	 * The reference is set to {@code null}.
	 */
	public void destroySignalViewerControlPanel(){
		if(signalViewerControlPanel != null){
			signalViewerControlPanel = null;
		}
	}

	/**
	 * Returns the {@code MultiTierControlPanel}, it is created when it doesn't
	 * exist yet.
	 *
	 * @return the control panel for multiple tiers viewers
	 */
	public MultiTierControlPanel getMultiTierControlPanel() {
		if(multiTierControlPanel == null){
			return createMultiTierControlPanel();
		}
		return multiTierControlPanel;
	}
	
	/**
	 * Creates and returns the {@code MultiTierControlPanel}.
	 * If it hadn't been created before, it is created now.
	 * 
	 * @return the {@code MultiTierControlPanel} for multiple tiers viewers
	 */
	public MultiTierControlPanel createMultiTierControlPanel(){
		if(multiTierControlPanel == null){
			multiTierControlPanel = new MultiTierControlPanel(transcription, tierOrder);
			//multiTierControlPanel.setTierOrderObject(tierOrder);
			ElanLocale.addElanLocaleListener(transcription, multiTierControlPanel);
			Preferences.addPreferencesListener(transcription, multiTierControlPanel);
		}
		
		return multiTierControlPanel;
	}
	
	/**
	 * Destroys the {@code MultiTierControlPanel}. It is unregistered as listener
	 * and the reference is set to {@code null}.
	 */
	public void destroyMultiTierControlPanel(){
		if(multiTierControlPanel != null){
			Preferences.removePreferencesListener(transcription,multiTierControlPanel);
			ElanLocale.removeElanLocaleListener(multiTierControlPanel);
			multiTierControlPanel = null;
		}
	}

	/**
	 * Creates a {@code TimeLineViewer} that is connected to the Viewer universe.
	 *
	 * @return the {@code TimeLineViewer} for the Transcription in this ViewerManager
	 */
	public TimeLineViewer createTimeLineViewer() {
		timeLineViewer = new TimeLineViewer(transcription);
		PeriodicUpdateController controller = getControllerForPeriod(TIME_LINE_VIEWER_PERIOD);
		controllers.put(timeLineViewer, controller);
		connect(timeLineViewer);

		viewers.add(timeLineViewer);
		enabledViewers.add(timeLineViewer);

		return timeLineViewer;
	}
	
	/**
	 * Returns the {@code TimeLineViewer}.
	 *
	 * @return the {@code TimeLineViewer} for the Transcription in this 
	 * ViewerManager or {@code null}
	 */
	public TimeLineViewer getTimeLineViewer() {
		return timeLineViewer;
	}

	/**
	 * Creates a {@code InterlinearViewer} that is connected to the Viewer universe.
	 *
	 * @return the {@code InterlinearViewer} for the Transcription in this 
	 * ViewerManager, can be {@code null}
	 */
	public InterlinearViewer createInterlinearViewer() {
		Boolean val = Preferences.getBool(ELANCommandFactory.INTERLINEAR_VIEWER, null);
	    if (val == null || val) {
	    	if(interlinearViewer == null){				
	    		interlinearViewer = new InterlinearViewer(transcription);
	    		PeriodicUpdateController controller = getControllerForPeriod(INTERLINEAR_VIEWER_PERIOD);
	    		controllers.put(interlinearViewer, controller);
	    		connect(interlinearViewer);

	    		viewers.add(interlinearViewer);
	    		enabledViewers.add(interlinearViewer);
			}
	    	return interlinearViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the {@code InterlinearViewer}, can be {@code null}
	 *
	 * @return the {@code InterlinearViewer}, can be {@code null}
	 */
	public InterlinearViewer getInterlinearViewer() {		
		return interlinearViewer;
	}	
	
	/**
	 * Returns the URL of the file loaded in the {@code SignalViewer}.
	 * 
	 * @return the URL or path to the file in the {@code SignalViewer}  
	 */
	public String getSignalMediaURL(){
		return signalMediaURL;
	}
	
	/**
	 * Sets the paths of the linked audio files.
	 * 
	 * @param audioPath the list of audio paths
	 */
	public void setAudioPaths(List<String> audioPath) {
		audioPaths.clear();
		if (audioPath != null) {
			for (String path : audioPath) {
				path = FileUtility.urlToAbsPath(path);
				if(!audioPaths.contains(path)){
					audioPaths.add(path);
				}				
			}
		}
	}
	
	/**
	 * Returns the paths of the linked audio files, can be {@code null}.
	 * 
	 * @return audioPaths a list of audio file paths, can be {@code null}
	 */
	public List<String> getAudioPaths(){
		return audioPaths;
	}
	
	/**
	 * Sets the paths of the linked videos.
	 * 
	 * @param videoPath a list of video paths
	 */	 
	public void setVideoPaths(List<String> videoPath) {
		videoPaths.clear();
		if (videoPath != null) {
			for (String path : videoPath) {
				path = FileUtility.urlToAbsPath(path);
				if (!videoPaths.contains(path)) {
					videoPaths.add(path);
				}				
			}
		}
	}
	
	/**
	 * Returns the paths of the linked video files, can be {@code null}.
	 * 
	 * @return a list of paths of the linked video files, can be {@code null}
	 */
	public List<String> getVideoPaths(){
		return videoPaths;
	}

	/**
	 * Set the paths of secondary linked media files.
	 * 
	 * @param otherPath the list of paths of other types of media files
	 */	 
	public void setOtherMediaPaths(List<String> otherPath) {
		otherMediaPaths.clear();
		if (otherPath != null) {
			for (String path : otherPath) {
				path = FileUtility.urlToAbsPath(path);
				if (!otherMediaPaths.contains(path)) {
					otherMediaPaths.add(path);
				}				
			}
		}
	}
	
	/**
	 * Returns the paths of secondary linked media files, can be {@code null}.
	 * 
	 * @return the list of paths of secondary linked media files, can be
	 * {@code null} 
	 */
	public List<String> getOtherMediaPaths(){
		return otherMediaPaths;
	}
	
	/**
	 * Creates and returns the {@code SignalViewer}. If it hadn't been created
	 * yet, it is created now.
	 * 
	 * @return the {@code SignalViewer} or {@code null}
	 */
	public SignalViewer createSignalViewer() {
		Boolean val = Preferences.getBool(ELANCommandFactory.SIGNAL_VIEWER, null);
	    if (val == null || val) {
	    	if(signalViewer == null){
	    		if(signalMediaURL != null){
	    			createSignalViewer(signalMediaURL);
	    			if (signalViewer != null) {
	    				signalViewer.setOffset(getSignalViewerOffset());
	    				signalViewer.preferencesChanged();
	    			}
	    		}
	    	}	    	
	    	return signalViewer;
	    } else {
	    	return null;
	    }
	}
	
	/**
	 * Creates a {@code SignalViewer} that is connected to the Viewer universe.
	 *
	 * @param mediaURL String that represents the signal media URL
	 *
	 * @return the {@code SignalViewer} for the media URL
	 */
	public SignalViewer createSignalViewer(String mediaURL) { // throw exception ?
		SignalViewer viewer = null;
		if(mediaURL != null){
			signalMediaURL = mediaURL;
		}
		// remove test, maybe the native framework can deal with remote content?
		// URL or String  problem to be solved, 
		// the SignalViewer does not work with remote files
		/*
		if (FileUtility.isRemoteFile(mediaURL)) {
			return viewer; // == null
		}
		*/
		Boolean val = Preferences.getBool(ELANCommandFactory.SIGNAL_VIEWER, null);
		if (val != null && !val) {
			 return viewer;
		}
		
		Boolean boolPref = Preferences.getBool("NativePlayer.AudioExtraction", null);
		
		if (boolPref != null) {
			WAVSamplerFactory.setUseNativeExtractor(boolPref.booleanValue());
		}
		// use the factory create method
		WAVSamplesProvider sampler = WAVSamplerFactory.createWAVSamplesProvider(mediaURL);
		if (sampler != null) {
			viewer = new SignalViewer(sampler);	
		
			PeriodicUpdateController controller = getControllerForPeriod(SIGNAL_VIEWER_PERIOD);
			controllers.put(viewer, controller);
			connect(viewer);
	
			// something to set the offset
			viewers.add(viewer);
			enabledViewers.add(viewer);
			signalViewer = viewer; // a problem when there is more than one signal viewer		
		}
		return viewer;
	}	
	
	/**
	 * Updates the {@code SignalViewer} with a new media file path.
	 * 
	 * @param mediaURL the new media path
	 */
	public void updateSignalViewerMedia(String mediaURL){
		if(mediaURL != null){
			signalMediaURL = mediaURL;
		}
	}
	
	/**
	 * Creates the spectrogram viewer
	 * @return the spectrogram viewer instance
	 */
	public SpectrogramViewer createSpectrogramViewer() {
		Boolean val = Preferences.getBool(ELANCommandFactory.SPECTROGRAM_VIEWER, null);
		if (val != null && val.booleanValue()) {// Aug 2023: change of default to hidden
			if (spectrogramViewer == null) {
	    		if(signalMediaURL != null){// use signal viewer url for now
	    			createSpectrogramViewer(signalMediaURL);
	    			if (spectrogramViewer != null) {
	    				//spectrogramViewer.setOffset(getSignalViewerOffset());
	    				spectrogramViewer.preferencesChanged();
	    			}
	    		}
			}
			
			return spectrogramViewer;
		}
		
		return null;
	}
	
	/**
	 * Creates a spectrogram viewer with a media url
	 * @param mediaURL the media url
	 * @return the spectrogram instance
	 */
	public SpectrogramViewer createSpectrogramViewer(String mediaURL) {		
		if (mediaURL == null) {
			return null;
		}
		Boolean val = Preferences.getBool(ELANCommandFactory.SPECTROGRAM_VIEWER, null);
		if (val != null && !val) {
			 return null;
		}
		
		Boolean boolPref = Preferences.getBool("NativePlayer.AudioExtraction", null);
		
		if (boolPref != null) {
			WAVSamplerFactory.setUseNativeExtractor(boolPref.booleanValue());
		}
		
		SpectrogramViewer spViewer = null;
		// use the factory create method
		WAVSamplesProvider sampler = WAVSamplerFactory.createWAVSamplesProvider(mediaURL);
		if (sampler != null) {
			spViewer = new SpectrogramViewer(sampler);	
		
			PeriodicUpdateController controller = getControllerForPeriod(SIGNAL_VIEWER_PERIOD);
			controllers.put(spViewer, controller);
			connect(spViewer);
	
			// something to set the offset
			viewers.add(spViewer);
			enabledViewers.add(spViewer);
			spectrogramViewer = spViewer; 		
		}
		
		return spViewer;
	}
	
	/**
	 * Returns the spectrogram viewer instance
	 * @return the spectrogram viewer instance
	 */
	public SpectrogramViewer getSpectrogramViewer() {
		return spectrogramViewer;
	}
	
	/**
	 * Creates and returns the {@code TranscriptionViewer}.
	 * 
	 * @return the {@code TranscriptionViewer}.
	 */
	public TranscriptionViewer createTranscriptionViewer() {
		transcriptionViewer = new TranscriptionViewer(this);
		PeriodicUpdateController controller = getControllerForPeriod(ViewerManager2.MEDIA_CONTROL_PANEL_PERIOD);
		controllers.put(transcriptionViewer, controller);
		connect(transcriptionViewer);

		viewers.add(transcriptionViewer);
		enabledViewers.add(transcriptionViewer);

		return transcriptionViewer;
	}
	
	/**
	 * Returns the {@code TranscriptionViewer}, can be {@code null}.
	 *
	 * @return the {@code TranscriptionViewer}, can be {@code null}
	 */
	public TranscriptionViewer getTranscriptionViewer() {		
		return transcriptionViewer;
	}	

	/**
	 * Creates and returns a {@code GridViewer} that is connected to the Viewer
	 * universe but not yet connected to a certain Tier.
	 * If it hadn't been created yet, it is created now.
	 *
	 * @return the {@code GridViewer}, can return {@code null}
	 */
	public GridViewer createGridViewer() {
		Boolean val = Preferences.getBool(ELANCommandFactory.GRID_VIEWER, null);
	    if (val == null || val) {
	    	if(gridViewer == null){				
	    		gridViewer = new GridViewer();
				connect(gridViewer);
				viewers.add(gridViewer);
				enabledViewers.add(gridViewer);
			}
	    	return gridViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the {@code GridViewer}, can be {@code null}.
	 *
	 * @return the {@code GridViewer}, can be {@code null}
	 */
	public GridViewer getGridViewer() {		
		return gridViewer;
	}	
	
	/**
	 * Creates a {@code ElanResultViewer} that is connected to the Viewer
	 * universe but not yet connected to a certain Tier.
	 *
	 * @return a new {@code ElanResultViewer}
	 */
	public ElanResultViewer createSearchResultViewer() {
		ElanResultViewer viewer = new ElanResultViewer();
		connect(viewer);

		viewers.add(viewer);
		enabledViewers.add(viewer);

		return viewer;
	}

	/**
	    * Creates a {@code SubtitleViewer} that is connected to the Viewer
	    * universe but not yet connected to a certain Tier.
	    *
	    * @return a new {@code SubtitleViewer}, can be {@code null}
	    */
	public SubtitleViewer createSubtitleViewer() {
		Boolean val = Preferences.getBool(ELANCommandFactory.SUBTITLE_VIEWER, null);
	    if (val == null || val) {
	    	SubtitleViewer subtitleViewer = new SubtitleViewer();
			connect(subtitleViewer);

			viewers.add(subtitleViewer);
			enabledViewers.add(subtitleViewer);
			
			subtitleViewers.add(subtitleViewer);
			
	    	return subtitleViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the list of {@code SubtitleViewer}s, can be {@code null}
	 *
	 * @return the list of {@code SubtitleViewer}s, or {@code null}
	 */
	public List<SubtitleViewer> getSubtitleViewers() {		
		return subtitleViewers;
	}

	/**
	 * Creates a {@code TextViewer} that is connected to the Viewer universe but not
	 * yet connected to a certain Tier.
	 * If it hadn't been created yet, it is created now.
	 *
	 * @return the {@code TextViewer}, can be {@code null}
	 */
	public TextViewer createTextViewer() {
		Boolean val = Preferences.getBool(ELANCommandFactory.TEXT_VIEWER, null);
	    if (val == null || val) {
	    	if(textViewer == null){				
	    		textViewer =  new TextViewer();
				connect(textViewer);
				viewers.add(textViewer);
				enabledViewers.add(textViewer);
			}
	    	return textViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the {@code TextViewer}, can be {@code null}
	 * 
	 * @return the {@code TextViewer}, can be {@code null}
	 */
	public TextViewer getTextViewer() {		
		return textViewer;
	}

	/**
	 * Creates a {@code SegmentationViewer} that is connected to the Viewer universe.
	 *
	 * @return a {@code SegmentationViewer} for the Transcription in this ViewerManager
	 */
	public SegmentationViewer2 createSegmentationViewer() {
		SegmentationViewer2 viewer = new SegmentationViewer2(transcription);
		PeriodicUpdateController controller = getControllerForPeriod(TIME_LINE_VIEWER_PERIOD);
		controllers.put(viewer, controller);
		connect(viewer);

		viewers.add(viewer);
		enabledViewers.add(viewer);

		return viewer;
	}

	/**
	 * Creates a {@code TimeSeriesViewer} that is connected to the Viewer universe.
	 * 
	 * @return a {@code TimeSeriesViewer} for the Transcription in this ViewerManager
	 */
	public TimeSeriesViewer createTimeSeriesViewer() {
		TimeSeriesViewer viewer = new TimeSeriesViewer(transcription);
		PeriodicUpdateController controller = getControllerForPeriod(TIME_LINE_VIEWER_PERIOD);
		controllers.put(viewer, controller);
		connect(viewer);

		viewers.add(viewer);
		enabledViewers.add(viewer);
		return viewer;
	}	
	
	/**
	 * Connects or disconnects a viewer to or from the viewer universe.
	 * 
	 * @param viewer the viewer to connect or disconnect
	 * @param connect if {@code true} the viewer will be connected, otherwise
	 * it will be disconnected
	 */
	public void connectViewer(AbstractViewer viewer, boolean connect){
		if(viewer == null){
			return;
		}
		if(connect){
			if(viewer instanceof TimeSeriesViewer){
				controllers.put(viewer, getControllerForPeriod(TIME_LINE_VIEWER_PERIOD));
			}else if(viewer instanceof SignalViewer){
				controllers.put(viewer, getControllerForPeriod(SIGNAL_VIEWER_PERIOD));
			}
			connect(viewer);
			viewers.add(viewer);
			enabledViewers.add(viewer);
			disabledViewers.remove(viewer);
		} else {
			disconnect(viewer , false);
			enabledViewers.remove(viewer);
			disabledViewers.add(viewer);
		}
	}
	
	/**
	 * Creates and returns a {@code MetadataViewer}.
	 * If it hadn't been created yet, it is created now.
	 * 
	 * @return the {@code MetadataViewer} or {@code null}
	 */
	public MetadataViewer createMetadataViewer() {
		Boolean val = Preferences.getBool(ELANCommandFactory.METADATA_VIEWER, null);
	    if (val == null || val) {
	    	if(metadataViewer == null){				
	    		metadataViewer = new MetadataViewer(this);
	    		ElanLocale.addElanLocaleListener(transcription, metadataViewer);
	    		Preferences.addPreferencesListener(transcription, metadataViewer);
				}
	    	return metadataViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the {@code MetadataViewer}, can be {@code null}
	 * 
	 * @return the {@code MetadataViewer}, can be {@code null}
	 */
	public MetadataViewer getMetadataViewer() {
		return metadataViewer;
	}
	
	/**
	 * Creates and returns a {@code RecognizerPanel}, connected to the viewer universe.
	 * If it hadn't been created yet, it is created now.
	 * 
	 * Note: use media descriptor instead of path?
	 * Note: if there is nothing to connect this could also be done in e.g. ElanFrame
	 * 
	 * @return a {@code RecognizerPanel} for selection and configuration of an
	 * audio or video (or other) based recognizer, can be {@code null} 
	 */
	public RecognizerPanel createRecognizerPanel() {		
		Boolean val = Preferences.getBool(ELANCommandFactory.RECOGNIZER, null);
	    if (val == null || val) {
	    	if(recognizerPanel == null){
	    		boolean a = audioPaths != null && !audioPaths.isEmpty();
	    		boolean v = videoPaths != null && !videoPaths.isEmpty();
	    		boolean o = otherMediaPaths != null && !otherMediaPaths.isEmpty();
				if (a || v || o) {
					recognizerPanel = new RecognizerPanel(this);
					if (a) {
						recognizerPanel.setAudioFilePaths(audioPaths);
					}
					if (v) {
						recognizerPanel.setVideoFilePaths(videoPaths);
					}
					if (o) {
						recognizerPanel.setOtherFilePaths(otherMediaPaths);
					}
					
					// connect to anything??
					ElanLocale.addElanLocaleListener(transcription, recognizerPanel);
				}
	    	}
	    	return recognizerPanel;
	    } else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the {@code RecognizerPanel}, can be {@code null}.
	 * 
	 * @return the {@code RecognizerPanel}, can be {@code null}
	 */
	public RecognizerPanel getRecognizerPanel() {
		return recognizerPanel;
	}	
	
	/**
	 * Creates and connects a {@code LexiconEntryViewer}.
	 * 
	 * @return a {@code LexiconEntryViewer}
	 */
	public LexiconEntryViewer createLexiconEntryViewer() {
		lexiconViewer = new LexiconEntryViewer();
		
		connect(lexiconViewer);
		
		viewers.add(lexiconViewer);
		enabledViewers.add(lexiconViewer);

		return lexiconViewer;
	}
	
	/**
	 * Returns the {@code LexiconEntryViewer}, can be {@code null}.
	 * 
	 * @return the {@code LexiconEntryViewer}, can be {@code null}
	 */
	public LexiconEntryViewer getLexiconViewer() {
		return lexiconViewer;
	}
	
	/**
	 * Creates and connects a {@code CommentViewer}.
	 * 
	 * @param transcription the transcription or the viewer (should be the same
	 *  as the one central to this viewer universe?)
	 * @return a {@code CommentViewer}
	 */
	public CommentViewer createCommentViewer(Transcription transcription) {
		commentViewer = new CommentViewer((TranscriptionImpl) transcription);
		// We want to know when the crosshair changes its location
		PeriodicUpdateController controller = getControllerForPeriod(TIME_LINE_VIEWER_PERIOD);
		controllers.put(commentViewer, controller);
		connect(commentViewer);
		
		viewers.add(commentViewer);
		enabledViewers.add(commentViewer);
		
		// We want to know when the active tier changes
		if (multiTierControlPanel != null) {
			multiTierControlPanel.addViewer(commentViewer);
		}

		return commentViewer;
	}
	
	/**
	 * Returns the {@code CommentViewer}, can be {@code null}.
	 * 
	 * @return the {@code CommentViewer}, can be {@code null}
	 */
	public CommentViewer getCommentViewer() {
		return commentViewer;
	}
	
	
	/**
	 * Creates and connects the {@code TurnsAndSceneViewer}.
	 *  
	 * @return the {@code TurnsAndSceneViewer}
	 */
	public TurnsAndSceneViewer createTurnsAndSceneViewer() {
		turnsAndSceneViewer = new TurnsAndSceneViewer(transcription);
		
		PeriodicUpdateController controller = getControllerForPeriod(INTERLINEAR_VIEWER_PERIOD);
		controllers.put(turnsAndSceneViewer, controller);
		connect(turnsAndSceneViewer);
		viewers.add(turnsAndSceneViewer);
		enabledViewers.add(turnsAndSceneViewer);
		
		return turnsAndSceneViewer;
	}
	
	/**
	 * Returns the {@code TurnsAndSceneViewer}, can be {@code null}.
	 * 
	 * @return the {@code TurnsAndSceneViewer}, can be {@code null}
	 */
	public TurnsAndSceneViewer getTurnsAndSceneViewer() {
		return turnsAndSceneViewer;
	}
	
	/**
	 * Destroys a panel identified by its name.
	 * 
	 * @param panelName the name of the panel
	 */
	public void destroyPanel(String panelName){
		if(panelName == null){
			return;
		}
		if (panelName.equals(ELANCommandFactory.RECOGNIZER)) {
			if(recognizerPanel != null){
				ElanLocale.removeElanLocaleListener(recognizerPanel);
				recognizerPanel = null;
			}
        }
	}
	
	/**
	 * Registers a viewer with time line controllers for specific tiers.
	 *  
	 * @param viewer the viewer for which the controllers must be set
	 * @param tierNames array of Tier names 
	 * 
	 * @see #setControllersForViewer(AbstractViewer, Tier[])
	 */
	public void setControllersForViewer(AbstractViewer viewer, String[] tierNames){
	    try{
	        Tier[] tiers = new Tier[tierNames.length];
	        for(int i=0; i<tierNames.length; i++){
	            tiers[i] = transcription.getTierWithId(tierNames[i]);
	        }
	        setControllersForViewer(viewer, tiers);
	    }
	    catch(Exception e){}
	}
	
	/**
	 * Sets the {@code Tier} for a {@code SingleTierViewer}.
	 * 
	 * @param viewer the viewer
	 * @param tier the tier for the viewer
	 */
	public void setTierForViewer(SingleTierViewer viewer, Tier tier){
	    if(viewer instanceof AbstractViewer) {
			setControllersForViewer((AbstractViewer) viewer, tier == null ? new Tier[0] : new Tier[]{tier});
		}
	    viewer.setTier(tier);
	}
	
	/**
	 * Registers the viewer with time line controllers for selected tiers.
	 * Note: currently only the first tier of the array is actually used.
	 * 
	 * @param viewer the viewer for which the controllers must be set
	 * @param tiers array of tiers that must be set
	 */
	public void setControllersForViewer(AbstractViewer viewer, Tier[] tiers) {
		if (viewer == null) {
			return;
		}

	    // disconnect an old controller if it exists
        disconnectController(viewer, true);

	    //TODO: connect to all tiers, not just first (-> change storage of controllers)
	    // connect the viewer to the right controller
	    if (tiers != null && tiers.length > 0) {
	        TimeLineController controller = getControllerForTier(tiers[0]);
	        controller.addControllerListener(viewer);
	        controllers.put(viewer, controller);

            // set the controller in the started state if player is playing
            if (masterMediaPlayer.isPlaying()) {
                controller.start();
            }
        }
	}

	/**
	 * Creates a {@code SingleTierViewerPanel} and connects it to the
	 * {@code Transcription} as an {@code ACMEditListener}.
	 *
	 * @return the new {@code SingleTierViewerPanel}
	 */
	public SingleTierViewerPanel createSingleTierViewerPanel() {
		SingleTierViewerPanel panel = new SingleTierViewerPanel(this);
		tierOrder.addTierOrderListener(panel);

		try {
			((ACMEditableDocument) transcription).addACMEditListener(panel);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		ElanLocale.addElanLocaleListener(transcription, panel);

		return panel;
	}

	/**
	 * Destroys (disconnects) the {@code SingleTierViewerPanel} as {@code ACMEditListener} from
	 * the {@code Transcription}.
	 *
	 * @param panel the {@code SingleTierViewerPanel} to be destroyed (disconnected).
	 */
	public void destroySingleTierViewerPanel(SingleTierViewerPanel panel) {
		if(panel == null){
			return;
		}
		tierOrder.removeTierorderListener(panel);
		try {
			((ACMEditableDocument) transcription).removeACMEditListener(panel);
			
			Preferences.removePreferencesListener(transcription, panel);			
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		ElanLocale.removeElanLocaleListener(panel);
	}

	/**
	 * Removes an {@code AbstractViewer} completely from the viewer universe.
	 *
	 * @param viewer the {@code AbstractViewer} that must be destroyed
	 */
	public void destroyViewer(AbstractViewer viewer) {
		if (enabledViewers.contains(viewer)) {
			enabledViewers.remove(viewer);
		}
		if (disabledViewers.contains(viewer)){
			disabledViewers.remove(viewer);
		}
	
		disconnect(viewer, true);
		viewers.remove(viewer);	
	}
	
	/**
	 * Removes the viewer identified by the specified name (usually a constant
	 * defined in the command factory).
	 * The reference of the viewer is set to {@code null}.
	 * @param viewerName the name or identifier of the viewer
	 */
	public void destroyViewerByName(String viewerName) {
		if(viewerName == null){
			return;
		}
		if (viewerName.equals(ELANCommandFactory.COMMENT_VIEWER)) {
			if (commentViewer != null) {
				destroyViewer(commentViewer);
				commentViewer = null;
			}
		} else if (viewerName.equals(ELANCommandFactory.GRID_VIEWER)) {
			if(gridViewer != null){
				destroyViewer(gridViewer);
				gridViewer = null;
			}
		} else if (viewerName.equals(ELANCommandFactory.INTERLINEAR_VIEWER)) {
			if(interlinearViewer != null){
				destroyViewer(interlinearViewer);
				interlinearViewer = null;
			}
		} else if (viewerName.equals(ELANCommandFactory.LEXICON_VIEWER)) {
			if(lexiconViewer != null){
				destroyViewer(lexiconViewer);
				lexiconViewer = null;
			}
		} else if (viewerName.equals(ELANCommandFactory.METADATA_VIEWER)) {
			if(metadataViewer != null){
				ElanLocale.removeElanLocaleListener(metadataViewer);
				Preferences.removePreferencesListener(transcription, metadataViewer);
				metadataViewer = null;
			}
		} else if (viewerName.equals(ELANCommandFactory.SIGNAL_VIEWER)) {
			if(signalViewer != null){
				destroyViewer(signalViewer);
				signalViewer = null;			
				destroySignalViewerControlPanel();
			}
		} else if (viewerName.equals(ELANCommandFactory.SPECTROGRAM_VIEWER)) {
			if(spectrogramViewer != null){
				destroyViewer(spectrogramViewer);
				spectrogramViewer = null;			
				//destroySpectrogramViewerControlPanel();
			}
		} else if (viewerName.equals(ELANCommandFactory.TEXT_VIEWER)) {
			if(textViewer != null){
				destroyViewer(textViewer);
				textViewer = null;
			}
		} else if (viewerName.equals(ELANCommandFactory.TIMELINE_VIEWER)) {
			if(timeLineViewer != null){
				destroyViewer(timeLineViewer);
				timeLineViewer = null;
			}
		} else if (viewerName.equals(ELANCommandFactory.TRANSCRIPTION_VIEWER)) {
			if(transcriptionViewer != null){
				destroyViewer(transcriptionViewer);
				transcriptionViewer = null;
			}
		} else if (viewerName.equals(ELANCommandFactory.TRANSCRIPTION_VIEWER)) {
			if(annotationDensityViewer != null){
				destroyViewer(annotationDensityViewer);
				annotationDensityViewer = null;
			}	
		} else if (viewerName.equals(ELANCommandFactory.SUBTITLE_VIEWER)) {
			// Removes all registered SubtitleViewers.	
			if(subtitleViewers != null){
				for (int i = 0; i < subtitleViewers.size(); i++) {
					destroyViewer(subtitleViewers.get(i));
				}
				subtitleViewers.clear();
				//subtitleViewers = null;
			}
		}
	}	

	/**
	 * Removes the {@code MediaPlayerControlSlider}.
	 * The reference is set to {@code null}. 
	 */
	public void destroyMediaPlayerControlSlider() {
		if(mediaPlayerControlSlider != null){
			destroyViewer(mediaPlayerControlSlider);
			mediaPlayerControlSlider = null;
		}		
	}
	
	/**
	 * Removes the {@code TimePanel}.
	 * The reference is set to {@code null}.
	 */
	public void destroyTimePanel() {
		if(timePanel != null){
			destroyViewer(timePanel);
			timePanel = null;
		}		
	}
	/**
	 * Disconnects an {@code AbstractViewer} from the viewer universe in such a
	 * manner that it can be reconnected.
	 *
	 * @param viewer the {@code AbstractViewer} that must be disabled
	 */
	public void disableViewer(AbstractViewer viewer) {
		if (enabledViewers.contains(viewer)) {
			enabledViewers.remove(viewer);
			disconnect(viewer, false);
			disabledViewers.add(viewer);
		}
	}

	/**
	 * Reconnects an {@code AbstractViewer} to the viewer universe from which
	 * it was temporarily disconnected.
	 *
	 * @param viewer the {@code AbstractViewer} that must be enabled
	 */
	public void enableViewer(AbstractViewer viewer) {
		if (disabledViewers.contains(viewer)) {
			disabledViewers.remove(viewer);
			connect(viewer);
			enabledViewers.add(viewer);
		}
	}
	
	/**
	 * Sets a flag on existing players whether frame forward/backward always jumps
	 * to the beginning of the next/previous frame or jumps with the ms per frame value.
	 * 
	 * @param stepsToBegin if {@code true} frame forward/backward jumps to the 
	 * begin of next/previous frame
	 */
	public void setFrameStepsToBeginOfFrame(boolean stepsToBegin) {
		if (masterMediaPlayer != null) {
			masterMediaPlayer.setFrameStepsToFrameBegin(stepsToBegin);
		}
		for (ElanMediaPlayer player : connectedMediaPlayers) {
			player.setFrameStepsToFrameBegin(stepsToBegin);
		}
		for (ElanMediaPlayer player : disabledMediaPlayers) {
			player.setFrameStepsToFrameBegin(stepsToBegin);
		}
	}

	/**
	 * Tries to make sure resources are freed, especially the created 
	 * media players might have to release resources.
	 * Preliminary implementation.
	 */
	public void cleanUpOnClose() {
		if(masterMediaPlayer != null && 
				masterMediaPlayer.isPlaying()){
			masterMediaPlayer.stop();
	    }
		
		for (ElanMediaPlayer player : connectedMediaPlayers) {
			player.cleanUpOnClose();
		}
		for (ElanMediaPlayer player : disabledMediaPlayers) {
			player.cleanUpOnClose();
		}
		if (masterMediaPlayer != null) {
			masterMediaPlayer.cleanUpOnClose();
		}
		for (int i = 0; i < viewers.size(); i++) {
			AbstractViewer viewer = viewers.get(i);
			disconnect(viewer, true);
			if (viewer instanceof ACMEditListener && transcription != null) {
				((ACMEditableDocument) transcription).removeACMEditListener((ACMEditListener) viewer);
			}
			if (viewer instanceof TimeLineViewer) {
				((TimeLineViewer)viewer).setTranscription(null);
			}
			if (viewer instanceof InterlinearViewer) {
				((InterlinearViewer)viewer).setTranscription(null);
			}
		}
		if (recognizerPanel != null) {
			// it will be removed as locale listener when the transcription is removed
		}
		enabledViewers.clear();
		viewers.clear();
		disabledViewers.clear();
	}
	
	/**
	 * Connect an {@code AbstractViewer} to the viewer universe.
	 *
	 * @param viewer the viewer that must be connected
	 */
	private void connect(AbstractViewer viewer) {
		// observables for all viewers
		viewer.setPlayer(masterMediaPlayer);
		viewer.setSelectionObject(selection);
		selection.addSelectionListener(viewer);
		viewer.setActiveAnnotationObject(activeAnnotation);
		activeAnnotation.addActiveAnnotationListener(viewer);
		ElanLocale.addElanLocaleListener(transcription, viewer);

		viewer.setViewerManager(this);

		// only for viewers that show trancription data
		if (viewer instanceof ACMEditListener) {
			try {
				((ACMEditableDocument) transcription).addACMEditListener((ACMEditListener) viewer);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		// only for viewers that share the time scale
		if (viewer instanceof TimeScaleUser) {
			((TimeScaleUser) viewer).setGlobalTimeScale(timeScale);
			timeScale.addTimeScaleListener((TimeScaleListener) viewer);
		}

		// only multi tier viewers are connected to the multi tier control panel
		if (viewer instanceof MultiTierViewer && multiTierControlPanel != null) {
			multiTierControlPanel.addViewer((MultiTierViewer) viewer);
		}

		if (viewer instanceof PreferencesListener) {
			Preferences.addPreferencesListener(transcription, viewer);
		}
		// if there is a controller associated with this viewer connect them
		Controller controller = controllers.get(viewer);

		if (controller != null) {
			controller.addControllerListener(viewer);

			// make sure the viewer is in sync
			viewer.controllerUpdate(new TimeEvent(controller));
		}		
	}

	/**
	 * Disconnect an {@code AbstractViewer} from the viewer universe.
	 *
	 * @param viewer the viewer that must be disconnected
	 * @param finalDisconnection flag that tells if the viewer might need to be
	 *        reconnected
	 */
	private void disconnect(AbstractViewer viewer, boolean finalDisconnection) {
		// observables for all viewers
		viewer.setPlayer(null);
		viewer.setSelectionObject(null);
		selection.removeSelectionListener(viewer);
		viewer.setActiveAnnotationObject(null);
		activeAnnotation.removeActiveAnnotationListener(viewer);
		ElanLocale.removeElanLocaleListener(viewer);
		
		viewer.setViewerManager(null);
		
		// only for viewers that show transcription data
		// TEMPRORARY? disabled because disconnected viewers are not aware of changes in the edited
		// document after they wake up. Keeping them connected looks like the easiest solution.
		// DO NOT disable the same block in the connect method because that takes care of the
		// first time connection.

		if (viewer instanceof ACMEditListener) {
			try {
		       ((ACMEditableDocument) transcription).removeACMEditListener((ACMEditListener) viewer);
			} catch (Exception e) {
		       e.printStackTrace();
			}
		}		

		// only for viewers that share the time scale
		if (viewer instanceof TimeScaleUser) {
			timeScale.removeTimeScaleListener((TimeScaleUser) viewer);
		}
		
		// only multi tier viewers are disconnected to the multi tier control panel
		if (viewer instanceof MultiTierViewer && multiTierControlPanel != null) {
			multiTierControlPanel.removeViewer((MultiTierViewer) viewer);
		}

		if (viewer instanceof PreferencesListener) {
			Preferences.removePreferencesListener(transcription, viewer);
		}
		
		disconnectController(viewer, finalDisconnection);
	}

	/**
	 * Break the connection between a viewer and its controller. Removes the
	 * associated controller if it has no connected Viewers left after this
	 * operation.
	 *
	 * @param viewer the viewer that must be disconnected from its controller
	 * @param finalDisconnection flag that tells if the viewer might need to be
	 *        reconnected
	 */
	private void disconnectController(AbstractViewer viewer, boolean finalDisconnection) {
		//	get the controller for this viewer and remove the viewer as listener
		Controller controller = controllers.get(viewer);

		//searchResultViewer might be created yet not connected -> controller == null
		if (controller != null) {
			controller.removeControllerListener(viewer);

			// remove the viewer key from the controllers hashtable if the disconnection is final
			if (finalDisconnection) {
				controllers.remove(viewer);

				// if there are no more listeners for the controller clean it up
				if (controller.getNrOfConnectedListeners() == 0) {
					removeFromHashTable(controller, controllers);
					masterMediaPlayer.removeController(controller);
					if (controller instanceof TimeLineController) {
						((TimeLineController) controller).setControllongPlayer(null);
					}
					controller = null;
				}
			}
		}
	}

	/**
	 * Gets a {@code TimeLineController} for a {@code Tier}. If the controller
	 * already exists it is reused otherwise it is created.
	 *
	 * @param tier the {@code Tier} for which the {@code TimeLineController} must be created
	 *
	 * @return the {@code TimeLineController} for the tier
	 */
	private TimeLineController getControllerForTier(Tier tier) {
		if (tier == null) {
			return null;
		}

		TimeLineController controller = null;

		// first see if the controller already exists
		if (controllers.containsKey(tier)) {
			controller = (TimeLineController) controllers.get(tier);
		}
		else {
			// The controller does not exist, create it
			controller = new TimeLineController(tier, masterMediaPlayer);

			// connect the controller to the master media player
			masterMediaPlayer.addController(controller);

			// add the controller to the existing controller list
			controllers.put(tier, controller);
		}

		return controller;
	}

	/**
	 * Gets a {@code PeriodicUpdateController} for a period. If the controller
	 * already exists it is reused otherwise it is created.
	 *
	 * @param period the period in milliseconds for which the
	 *        {@code PeriodicUpdateController} must be created
	 *
	 * @return the {@code PeriodicUpdateController} for the period
	 */
	private PeriodicUpdateController getControllerForPeriod(long period) {
		PeriodicUpdateController controller = null;
		Long periodKey = Long.valueOf(period);

		// first see if the controller already exists
		if (controllers.containsKey(periodKey)) {
			controller = (PeriodicUpdateController) controllers.get(periodKey);
		}
		else {
			// The controller does not exist, create it
			controller = new PeriodicUpdateController(period);

			// connect the controller to the master media player
			masterMediaPlayer.addController(controller);

			// add the controller to the existing controller list
			controllers.put(periodKey, controller);
		}

		return controller;
	}

	/**
	 * Utility to remove all occurrences of an object from a {@code Map}. 
	 * There is no direct method for this in the Java API if you do not know
	 * the key.
	 *
	 * @param object the {@code Object} to be removed
	 * @param hashtable the (now) {@code Map} that contains the object
	 *
	 * @return boolean {@code true} if the {@code Object} was in the map and
	 * was actually removed, {@code false} otherwise
	 */
	private boolean removeFromHashTable(Object object, Map<Object, Controller> hashtable) {
		boolean objectRemoved = false;

		Iterator<Map.Entry<Object, Controller>> it = hashtable.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Object, Controller> e = it.next();
			if (e.getValue() == object) {
				it.remove();
				objectRemoved = true;
			}
		}

		return objectRemoved;
	}
}
