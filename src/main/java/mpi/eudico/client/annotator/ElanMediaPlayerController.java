package mpi.eudico.client.annotator;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.gui.ElanSlider;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.EmptyMediaPlayer;
import mpi.eudico.client.annotator.player.HasAsynchronousNativePlayer;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.annotator.viewer.AnnotationDensityViewer;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StartEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;
import nl.mpi.util.FileUtility;


/**
 * A collection of buttons, sliders, etc to controls
 * the media player, e.g. start/pause playing, set the current time,
 * change volume or playback rate etc.
 */
@SuppressWarnings("serial")
public class ElanMediaPlayerController extends AbstractViewer
                                       implements PreferencesListener {
	/** the default button size (should ideally depend on screen resolution) */
    //final private static Dimension BUTTON_SIZE = new Dimension(30, 20);
    private long userTimeBetweenLoops = 500; //used when playing selection in loop mode, default 0.5 seconds
    private ViewerManager2 vm;
    private ElanSlider rateslider;
    private ElanSlider volumeslider;
    private SelectionPanel selectionpanel;
    private VolumeIconPanel volumeIconPanel;
    private StepAndRepeatPanel stepAndRepeatPanel;
    private JPanel volumesPanel;
    
    //private MediaPlayerControlSlider mpcs;
    private DurationPanel durationPanel;
    private AnnotationDensityViewer annotationDensityViewer;
    
    //private TimePanel timePanel;
    private PlayButtonsPanel playButtonsPanel;
    private AnnotationNavigationPanel annotationPanel;
    private SelectionButtonsPanel selectionButtonsPanel;
    private ModePanel modePanel;
    private long stopTime = 0;
    private boolean playingSelection = false;
    private boolean bLoopMode = false;
    private boolean bSelectionMode = false;
    private boolean bBeginBoundaryActive = false;
    private boolean stepAndRepeatMode = false;
    private boolean prevHaveSliders = false;
    
    // loopthread moved from PlaySelectionCommand to here to be able to stop it 
    // actively (instead of passively with a boolean, which has all kind of side effects)
    private LoopThread loopThread;
    
    private StepAndRepeatThread stepThread;

    /**
     * Constructor, initializes sliders and button groups.
     *
     * @param theVM the viewer manager managing viewers and players
     */
    @SuppressWarnings("unchecked")
	public ElanMediaPlayerController(ViewerManager2 theVM) {
        vm = theVM;

        rateslider = new ElanSlider("ELANSLIDERRATE", 0, 200, 100, vm);
        volumeslider = new ElanSlider("ELANSLIDERVOLUME", 0, 100, 100, vm);
        selectionpanel = new SelectionPanel(vm);
        
        //	mpcs = new MediaPlayerControlSlider();
        //	timePanel = new TimePanel();
        durationPanel = new DurationPanel(vm.getMasterMediaPlayer()
                                            .getMediaDuration());
        playButtonsPanel = new PlayButtonsPanel(getButtonSize(), vm);
        annotationPanel = new AnnotationNavigationPanel(getButtonSize(), vm);
        selectionButtonsPanel = new SelectionButtonsPanel(getButtonSize(), vm);
        modePanel = new ModePanel(vm, this);        
        volumeIconPanel = new VolumeIconPanel(vm, SwingConstants.VERTICAL, getButtonSize());
        stepAndRepeatPanel = new StepAndRepeatPanel(vm);
        
		Map prefs = Preferences.getMap(INDIVIDUAL_VOLUMES_PREFS, vm.getTranscription());
		if (prefs == null) {
			playerVolumes = new HashMap<String, Float>();
		} else {
			playerVolumes = (Map<String, Float>) prefs;
		}
		prefs = Preferences.getMap(INDIVIDUAL_PLAYER_MUTE_SOLO_PREF, vm.getTranscription());
		if (prefs == null) {
			playerMutedStates = new HashMap<String, String>();
		} else {
			playerMutedStates = (Map<String, String>) prefs;
		}
    }

    /**
     * Returns the default size of buttons on this panel.
     * Note: should ideally depend on resolution. 
     *
     * @return the default button size
     */
    public Dimension getButtonSize() {
        return Constants.ICON_BUTTON_SIZE;
    }

    /**
     * Returns the time between successive loops in loop mode, 
     * as specified by the user.
     *
     * @return the pause time between loops
     */
    public long getUserTimeBetweenLoops() {
        return userTimeBetweenLoops;
    }

    /**
     * Sets the time between successive loops in loop mode.
     *
     * @param loopTime the new pause time between loops
     */
    public void setUserTimeBetweenLoops(long loopTime) {
        userTimeBetweenLoops = loopTime;
    }

    // getters for subpanels
    /**
     * Returns a panel representing the total media duration, showing a custom
     * media playhead slider and marking the current time selection. 
     *
     * @return the media playhead control slider panel
     */
    public MediaPlayerControlSlider getSliderPanel() {
        return vm.getMediaPlayerControlSlider();
    }

    /**
     * Returns a viewer that visualizes the density of annotations as they are
     * distributed over the entire media duration. 
     *
     * @return the annotation density viewer
     */
    public AnnotationDensityViewer getAnnotationDensityViewer() {
    	if (annotationDensityViewer == null) {
    		annotationDensityViewer = vm.createAnnotationDensityViewer();
    	}
        return annotationDensityViewer;
    }

    /**
     * Returns the panel with a slider for the playback rate.
     *
     * @return the playback rate control panel
     */
    public JComponent getRatePanel() {
        return rateslider; //.getSlider();
    }

    /**
     * Sets the play rate and updates the UI.
     *
     * @param rate the play rate
     */
    @Override
    public void setRate(float rate) {
        super.setRate(rate);
        // multiply by 100; the slider uses ints
        rateslider.setValue((int) (100 * rate));
    }

    /**
     * Returns the panel with a slider to change the volume of a player. 
     *
     * @return the volume panel
     */
    public JComponent getVolumePanel() {
        return volumeslider;
    }

    /**
     * Return the panel that can be used to adjust the individual volumes 
     * of each media player: the master and all connected players.
     *
     * @return a JPanel
     */
    public JComponent getPlayersVolumePanel() {
    	if (volumesPanel == null) {
            makePlayersVolumePanel();
    	}
    	
        return volumesPanel;
    }

    /**
     * Sets the master volume and updates the ui.
     * This overrides the superclass method, which changes the subvolume.
     *
     * @param volume the volume
     */
    @Override
    public void setVolume(float volume) {
        // Don't call super.setVolume(volume): the slider will propagate its change anyway.
        // multiply by 100; the slider uses ints.
    	// The slider will propagate its new value to setMasterVolume().
        volumeslider.setValue((int) (100 * volume));
    }
    
    /**
     * Returns the volume of the main media player.
     * In currently known cases the volume slider would exist already,
     * but there is a fallback to the Preferences just in case.
     * 
     * @return the master volume
     */
    @Override 
    public float getVolume() {
    	if (volumeslider != null) {
    		return (float)volumeslider.getValue() / 100.0f;
    	} else {
    		Float volume = Preferences.getFloat("MediaControlVolume", 
    				vm.getTranscription());
    		if (volume != null) {
    			return volume.floatValue();
    		}
    		return 1.0f;
    	}
    }

    /**
     * Returns the selection and loop mode panel.
     *
     * @return the selection and loop mode panel
     */
    public JPanel getModePanel() {
        return modePanel;
    }

    /**
     * Returns the panel with buttons for playing the selection and for
     * placing the cursor at the left or right boundary of the selection. 
     *
     * @return the selection button panel
     */
    public SelectionButtonsPanel getSelectionButtonsPanel() {
        return selectionButtonsPanel;
    }

    /**
     * Returns the button panel with the Play/Pause, frame forward and backward
     * etc. buttons. 
     *
     * @return the main player control panel, with Play/Pause buttons etc.
     */
    public PlayButtonsPanel getPlayButtonsPanel() {
        return playButtonsPanel;
    }

    /**
     * Returns a panel with buttons to navigate through annotations within a 
     * tier and between tiers.
     *
     * @return the annotation navigation panel
     */
    public AnnotationNavigationPanel getAnnotationNavigationPanel() {
        return annotationPanel;
    }
    
    /**
     * Returns a panel with a volume icon which produces a pull down volume slider.
     *
     * @return a volume icon panel
     */
    public VolumeIconPanel getVolumeIconPanel() {
        return volumeIconPanel;
    }


    /**
     * Returns a small panel with a label showing a media time duration.
     *
     * @return a panel showing a time duration 
     */
    public JComponent getDurationPanel() {
        return durationPanel;
    }

    /**
     * Returns the time panel, showing the current media time as text.
     *
     * @return the media time panel
     */
    public JComponent getTimePanel() {
        return vm.getTimePanel();
    }

    /**
     * Returns a panel showing the selected time interval as text.
     * It shows begin_time-end_time selection_duration.
     *
     * @return the selection text panel
     */
    public JPanel getSelectionPanel() {
        return selectionpanel;
    }
    
    /**
     * Returns the step-and-repeat mode panel.
     * 
     * @return the step-and-repeat control panel
     */
    public StepAndRepeatPanel getStepAndRepeatPanel() {
    	return stepAndRepeatPanel;
    }

    /**
     * Empty.
     */
    @Override
	public void updateActiveAnnotation() {
    }

    /**
     * Updates a few control labels.
     */
    @Override
	public void updateLocale() {
    	if (muteButtons != null) {
    		String muteStr = ElanLocale.getString(
                    "MediaPlayerControlPanel.VolumeSlider.Mute");
    		for (ECheckBox box : muteButtons) {
    			box.setText(muteStr);
    		}
    	}
    	if (soloButtons != null) {
    		String soloStr = ElanLocale.getString(
                    "MediaPlayerControlPanel.VolumeSlider.Solo");
    		for (ERadioButton box : soloButtons) {
    			box.setText(soloStr);
    		}
    	}
    }

    /**
     * Empty.
     */
    @Override
	public void updateSelection() {
    }

    private void adjustSelection() {
        // set active boundary to current media time
        long currTime = getMediaTime();
        long beginTime = getSelectionBeginTime();
        long endTime = getSelectionEndTime();

        if (bBeginBoundaryActive) {
            beginTime = currTime;
        } else {
            endTime = currTime;
        }

        if (beginTime > endTime) { // begin and end change place
            setSelection(endTime, beginTime);
            toggleActiveSelectionBoundary();
        } else {
            setSelection(beginTime, endTime);
        }
    }

    /**
     * Notification that a media related event happened.
     * Checks for the end of play selection to possible update/restore the
     * selection.
     * 
     * @param event the controller event
     */
    @Override
    public void controllerUpdate(ControllerEvent event) {
        if (event instanceof StartEvent) {
            return;
        }

        // ignore time events within a certain time span after a stop event
        // that happened while playing a selection. This is needed to keep the
        // current selection after play selection is done in selection mode
        if (event instanceof TimeEvent &&
                ((System.currentTimeMillis() - stopTime) < 700)) {
            return;
        }

        // remember the stop time if the stop happened while playing a selection
        // time events will be ignored for a certain period after this stop time
        if (event instanceof StopEvent) {
            if (!bLoopMode) {
                //playingSelection = false;
            }

            stopTime = System.currentTimeMillis();
            // change active annotation boundary if at end of selection and active edge was on the left
            // added for practical reasons, users got confused and inadvertently destroyed the selection
            //    		long halfTime = getSelectionBeginTime() + (getSelectionEndTime() - getSelectionBeginTime()) / 2;
            if (isBeginBoundaryActive() &&
                    (getMediaTime() == getSelectionEndTime())) {
                toggleActiveSelectionBoundary();
            }
            
            // HS Aug 2008: make sure that in selection mode the selection is updated
            // the selection is always a bit behind the media playhead 
            //return;
            
            // HS 2016/7 after a stop event in selection mode, check if the (native) media player
            // already finished playing a selection in case of an asynchronous native player
            // HS 2020 generalized to check the play selection flag of any player
            if (bSelectionMode) {
            	while (vm.getMasterMediaPlayer().isPlayingInterval()) {
            		try {
            			Thread.sleep(10);
            		} catch (InterruptedException ie){}
            	}
            	
                if (!bLoopMode && playingSelection) {
                	setPlaySelectionMode(false);
                }
                if (isBeginBoundaryActive() &&
                        (getMediaTime() == getSelectionEndTime())) {
                    toggleActiveSelectionBoundary();
                }
            }
            if (!bSelectionMode && playingSelection) {
            	setPlaySelectionMode(false);
            }
            return;
        }

        //in some cases set a new selection 
        if (!playingSelection && (bSelectionMode == true)) {
            adjustSelection();
        }
    }

    /**
     * Switches the controller to the playing-selection mode.
     *
     * @param b the mode, on or off
     */
    public void setPlaySelectionMode(boolean b) {
    	if (b && playingSelection) {
    		int count = 0;
    		while (playingSelection && count < 400){//~2sec
    			count++;
    			try {
    				Thread.sleep(5);
    			} catch (Throwable t) {}
    		}
    	}
        playingSelection = b;
    }

    /**
     * Returns whether the controller is in play selection mode.
     *
     * @return whether the controller is in play selection mode
     */
    public boolean isPlaySelectionMode() {
        return playingSelection;
    }

    /**
     * Activates the loop mode.
     *
     * @param b if {@code true} the loop mode is active
     */
    public void setLoopMode(boolean b) {
        bLoopMode = b;
        modePanel.updateLoopMode(bLoopMode);
    }

    /**
     * Returns whether the loop mode is active.
     *
     * @return {@code true} if the loop mode is on, {@code false} by default
     */
    public boolean getLoopMode() {
        return bLoopMode;
    }

    /**
     * Toggles the loop mode
     */
    public void doToggleLoopMode() {
        if (bLoopMode == true) {
            bLoopMode = false;
        } else {
            bLoopMode = true;
        }
    }

    /**
     * Returns whether the selection mode is active.
     *
     * @return {@code true} if the selection mode is active
     */
    public boolean getSelectionMode() {
        return bSelectionMode;
    }

    /**
     * Toggles the selection mode
     */
    public void doToggleSelectionMode() {
        // bSelectionMode = !bSelectionMode
        if (bSelectionMode == true) {
            bSelectionMode = false;
        } else {
            bSelectionMode = true;
        }

        // generate a time event to make sure the image on the button toggles
        // this sometimes sets the selection begin time to 0
        //setMediaTime(getMediaTime());
        modePanel.updateSelectionMode(bSelectionMode);//??
        getModePanel().revalidate();
    }

    /**
     * When main time is begin time, main time is set to end time (of selection)
     * When main time is end time, main time is set to begin time (of selection)
     */
    public void toggleActiveSelectionBoundary() {
        // bBeginBoundaryActive = !bBeginBoundaryActive
        if (bBeginBoundaryActive == true) {
            bBeginBoundaryActive = false;
        } else {
            bBeginBoundaryActive = true;
        }
        // otherwise the button image is not always updated immediately
//        if (!playerIsPlaying()) {
//            setMediaTime(getMediaTime());
//        }
        selectionButtonsPanel.updateBoundary();
    }

    /**
     * Returns whether the selection begin boundary is active.
     *
     * @return {@code true} if the selection begin boundary is active
     */
    public boolean isBeginBoundaryActive() {
        return bBeginBoundaryActive;
    }

    /**
     * Starts a new play selection in a loop thread, after stopping the current
     * one (if necessary).
     *
     * @param begin selection begin time
     * @param end selection end time
     */
    public void startLoop(long begin, long end) {
        // stop current loop if necessary
        if ((loopThread != null) && loopThread.isAlive()) {
            loopThread.stopLoop();
            //??
            /*
            try {
            	loopThread.join(500);
            } catch (InterruptedException ie) {
            	
            }*/
        }

        loopThread = new LoopThread(begin, end);
        loopThread.start();
    }

    /**
     * Stops the current loop thread, if active.
     */
    public void stopLoop() {
        setPlaySelectionMode(false);

        if ((loopThread != null) && loopThread.isAlive()) {
            loopThread.stopLoop();
        }
    }
    
    // step and repeat mode methods
    /**
     * Switches the step-and-repeat mode on or off.
     * In step-and-repeat mode the player plays a segment of {@code x} seconds
     * {@code n} times and then moves {@code t} seconds on to the next segment.
     * 
     * @param mode if {@code true} the mode is set active, if {@code false} the
     * mode is stopped
     */
    public void setStepAndRepeatMode(boolean mode) {
    	if (stepAndRepeatMode == mode) {
    		return;
    	} else if (stepAndRepeatMode) {
    		stepAndRepeatMode = false;
    		if (stepThread != null) {
    			try {
    				stepThread.interrupt();
    			} catch (Exception ie) {
    				ie.printStackTrace();
    			}
    		}
    		playButtonsPanel.setEnabled(true);
    		selectionButtonsPanel.setEnabled(true);
    		stepAndRepeatPanel.setPlayIcon(true);
    	} else {
    		playButtonsPanel.setEnabled(false);
    		selectionButtonsPanel.setEnabled(false);
    		stepAndRepeatPanel.setPlayIcon(false);
	    	this.stepAndRepeatMode = mode;

	    	// stop player, stop play selection, play selectionmode = false
	    	playingSelection = false;
	    	bLoopMode = false;
	    	stepThread = new StepAndRepeatThread();
	    	stepThread.start();
    	}
    }
    
    /**
     * Returns whether the step-and-repeat mode is on.
     * 
     * @return {@code true} if this mode is on
     */
    public boolean isStepAndRepeatMode() {
    	return stepAndRepeatMode;
    }

    /**
     * Adjust volume and rate.
     */
    @Override
	public void preferencesChanged() {
		Float volume = Preferences.getFloat("MediaControlVolume", 
				vm.getTranscription());
		if (volume != null) {
			setVolume(volume.floatValue());
		}
		Float rate = (Float) Preferences.getFloat("MediaControlRate", 
				vm.getTranscription());
		if (rate != null) {
			setRate(rate.floatValue());
		}
		if (volumesPanel != null) {
			// Prevent doing a lot of work if only some other preference changed...
			boolean newHaveSliders = haveSliders();
			if (newHaveSliders != prevHaveSliders) {
				if (!newHaveSliders) {
					/*
					 * If the user doesn't want sliders any more, having
					 * multiple sounds at once is weird. Make life simple again.
					 * These values are deliberately not stored in the
					 * preferences, so that later, when the sliders are back in
					 * view, they can be set to their previous values again.
					 */
					vm.getVolumeManager().setSimpleVolumes();
				}
				// Creating the sliders will also get the desired subvolumes from
				// the preferences and apply them to the players.
				updatePlayersVolumePanel();
			}
		}
	}
	
    /**
     * A thread for playback in loop mode.
     */
    private class LoopThread extends Thread {
        private long beginTime;
        private long endTime;
        private boolean stopLoop = false;

        /**
         * Creates a new LoopThread instance
         *
         * @param begin the interval begin time
         * @param end the interval end time
         */
        LoopThread(long begin, long end) {
            this.beginTime = begin;
            this.endTime = end;
        }

        /**
         * Sets the flag that indicates that the loop thread should stop to
         * true.
         */
        public void stopLoop() {
            stopLoop = true;
        }

        /**
         * Restarts the player to play the interval as long as the controller
         * is in loop mode and the loop is not explicitly stopped.
         */
        @Override
		public void run() {
            while (!stopLoop && getLoopMode()) {
                if (!playerIsPlaying()) {
                    playInterval(beginTime, endTime);
    				// wait until playing is started
    				while (!playerIsPlaying()) {
    					try {
    						Thread.sleep(10);
    					} catch (InterruptedException ie) {
    						return;
    					}
    				}
                }

                while (playerIsPlaying() == true) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {
                    }
                    if (stopLoop) {
                    	return;
                    }
                }

                try {
                    Thread.sleep(getUserTimeBetweenLoops());
                } catch (Exception ex) {
                }
            }
        }
    }
    
    /**
     * A private helper class: a JCheckBox that remembers sufficient information to
     * avoid feedback loops between components, if they are set up to influence
     * each other mutually.
     * This is done by overriding the setSelected() method: it will remember what
     * value was set, so that in an event handler it can be checked if the change
     * event was expected (from the program) or not (from the user).
     * @author olasei
     */
    private static class ECheckBox extends JCheckBox {
		private boolean expectedValue = false;
    	ECheckBox() {
    		super();
    	}
    	@Override
		public void setSelected(boolean val) {
    		expectedValue = val;
    		super.setSelected(val);
    	}
    	public boolean wasNotExpected(boolean selected) {
    		boolean was = selected != expectedValue;
    		expectedValue = selected;

    		return was;
    	}
    }
    
    /**
     * And the same for JRadioButton.
     * @author olasei
     */
    private static class ERadioButton extends JRadioButton {
		private boolean expectedValue = false;
		ERadioButton() {
    		super();
    	}
    	@Override
		public void setSelected(boolean val) {
    		expectedValue = val;
    		super.setSelected(val);
    	}
    	public boolean wasNotExpected(boolean selected) {
    		boolean was = selected != expectedValue;
    		expectedValue = selected;

    		return was;
    	}
    }
    
    static final String INDIVIDUAL_VOLUMES_PREFS = "IndividualPlayerVolumes";
    /** media volume controls preference constant */
    public static final String HAVE_INDIVIDUAL_VOLUME_CONTROLS_PREF = "Media.HaveIndividualVolumeControls";
    /** Individual player mute settings constant */
    public static final String INDIVIDUAL_PLAYER_MUTE_SOLO_PREF = "IndividualPlayerMuteSoloSettings";
    private static final String MUTE = "mute";
    private static final String SOLO = "solo";
    
    private Map<String, Float> playerVolumes;
    private Map<String, String> playerMutedStates;
    private int deferringUpdates;
    private boolean updateWasDeferred;
    private List<ECheckBox> muteButtons;
    private List<ERadioButton> soloButtons;
    
    /**
     * Make a panel with volume sliders to adjust the volume of the individual media players.
     */
    private void makePlayersVolumePanel()
    {
    	volumesPanel = new JPanel();
    	volumesPanel.setLayout(new GridBagLayout());
    	// Indent the subvolume sliders a bit.
        Border inner = BorderFactory.createEmptyBorder(0, 40, 0, 0);
        volumesPanel.setBorder(inner);

    	deferringUpdates = 0;
    	updateWasDeferred = false;

    	updatePlayersVolumePanel();
    }
    
    /**
     * If it is foreseeable that there will be many updates to the list of
     * connected players, then call this function with {@code true} before,
     * and with {@code false} after. The updates will then not be done until
     * the latter call.
     * 
     * Calls nest and should therefore be properly balanced.
     * 
     * @param defer if {@code true} increase the deferring updates count, 
	 * otherwise decrease that count.
     */
    public void deferUpdatePlayersVolumePanel(boolean defer) {
    	if (defer) {
    		++deferringUpdates;
    	} else {
    		--deferringUpdates;
    		if (deferringUpdates <= 0 && updateWasDeferred) {
    			updatePlayersVolumePanel();
    		}
    	}
    }
    
    /**
     * Call this method if the list of connected media players
     * (or the master player) has been changed.
     * New sliders will be placed on the volumes panel.
     * This is needed in an unfortunate number of locations...
     */
    public void updatePlayersVolumePanel()
    {
    	if (deferringUpdates > 0) {
    		updateWasDeferred = true;
    		return;
    	}
		updateWasDeferred = false;

    	if (volumesPanel == null) {
    		return;
    	}

    	// Start out with an empty panel.
		volumesPanel.removeAll();
		
		// If preferences say we don't want these volume controls,
		// we're done at this point.
		prevHaveSliders = haveSliders();
		if (!prevHaveSliders) {
			return;
		}
    	    	
		// Then add back controls for each media player.   	
		
		muteButtons = new ArrayList<ECheckBox>();
		soloButtons = new ArrayList<ERadioButton>();
		
    	// Create a vertical orientation, flexible in horizontal direction.
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.anchor = GridBagConstraints.FIRST_LINE_START;
    	
		// Make one slider for the master and each connected media player.
		addOneVolumeSlider(vm.getMasterMediaPlayer(), gbc);
    	for (ElanMediaPlayer mp : vm.getConnectedMediaPlayers()) {
    		addOneVolumeSlider(mp, gbc);
    	}   	
    	
    	updateLocale();
    }

    private void addOneVolumeSlider(final ElanMediaPlayer mp, GridBagConstraints gbc) {
		if (mp instanceof EmptyMediaPlayer) {
			return;
		}
		String name = mp.getMediaDescriptor().mediaURL;
		final String fileName = FileUtility.fileNameFromPath(name);
		//name = FileUtility.dropExtension(fileName);
		JLabel label = new JLabel(fileName);
		
		gbc.gridx = 0;
    	gbc.weightx = 0;
    	gbc.gridwidth = 2;
    	gbc.gridheight = 1;
		volumesPanel.add(label, gbc);
		
		ECheckBox muteButton = new ECheckBox();
		ERadioButton soloButton = new ERadioButton();

		muteButton.setSelected(mp.getMute());
		muteButton.setFont(Constants.deriveSmallFont(muteButton.getFont()));
		soloButton.setFont(muteButton.getFont());

    	gbc.gridy++;
    	gbc.gridwidth = 1;
		volumesPanel.add(muteButton, gbc);
		gbc.gridx = 1;
		volumesPanel.add(soloButton, gbc);
		
		muteButtons.add(muteButton);
		soloButtons.add(soloButton);

		// Find the previously set volume from Preferences, if any.
		// If not, make sure it gets set in Preferences, so that
		// Preferences, slider, and player all have the same volume.
		float volume;
		if (playerVolumes.containsKey(fileName)) {
			volume = playerVolumes.get(fileName);
			vm.getVolumeManager().setSubVolume(mp, volume);
		} else {
    		volume = mp.getSubVolume();
			playerVolumes.put(fileName, Float.valueOf(volume));
		}
		
		final JSlider slider  = new JSlider(0, 100, (int) (100 * volume));
		slider.putClientProperty("JComponent.sizeVariant", "mini"); //Mac Aqua look & feel only
		slider.setMajorTickSpacing(25);
		slider.setMinorTickSpacing(5);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {					
				JSlider s = (JSlider)event.getSource();					
				final float newVolume = (float) s.getValue() / 100;
		        vm.getVolumeManager().setSubVolume(mp, newVolume);

		        if (!s.getValueIsAdjusting()) {
					// store it in preferences, keyed to the name of the media.
					playerVolumes.put(fileName, Float.valueOf(newVolume));
					// There is no need to save immediately, or to broadcast the new value.
					// (which means that unless the preferences make a copy, setting the
					// same Map every time probably isn't even necessary.)
					Preferences.set(INDIVIDUAL_VOLUMES_PREFS, playerVolumes, vm.getTranscription(), false, false);
		        }
			}});
		
		gbc.gridx = 2;
    	gbc.weightx = 1;
    	gbc.gridheight = 2;
    	gbc.gridy--;
		volumesPanel.add(slider, gbc);
		
		gbc.gridy += 2;
		
		// apply mute / solo preferences
		if (playerMutedStates.containsKey(fileName)) {
			String value = playerMutedStates.get(fileName);
			if (SOLO.equals(value)) {
				soloButton.setSelected(true);
			} else if (MUTE.equals(value)) {
				muteButton.setSelected(true);
				vm.getVolumeManager().setMute(mp, true);
				slider.setEnabled(false);
			}
		}
		
		/*
		 * Checking the boxes makes other boxes change.
		 * Checking a "Solo" box sets all Mute buttons to mute all players except this one.
		 * There can be at most one "Solo" button checked so it also de-selects all other Solo boxes.
		 * De-selecting the Solo box will un-Mute all.
		 * If the user manipulates a Mute box, all Solo boxes get deselected.
		 * 
		 * HOWEVER, with standard checkboxes we can't see if the user manipulated a checkbox, or if we did.
		 * Hence the derived class, which remembers the expected value, so that propagation
		 * of changes can be avoided when the change was not user-initiated.
		 */
		muteButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				ECheckBox source = (ECheckBox)e.getSource();
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				vm.getVolumeManager().setMute(mp, selected);
				slider.setEnabled(!selected);
				if (selected) {
					playerMutedStates.put(fileName, MUTE);
				} else {
					if (!SOLO.equals(playerMutedStates.get(fileName))) {
						playerMutedStates.remove(fileName);
					}
				}
				// superfluous except for the first time 
				// we are working directly with the map in the preferences, not a copy, if it already existed
				Preferences.set(INDIVIDUAL_PLAYER_MUTE_SOLO_PREF, playerMutedStates, 
						vm.getTranscription(), false, false);
				
				if (source.wasNotExpected(selected)) {
					for (ERadioButton box: soloButtons) {
						box.setSelected(false);
					}
				}
			}
		});
		
		soloButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				ERadioButton source = (ERadioButton)e.getSource();
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				// store preferences
				if (selected) {
					playerMutedStates.put(fileName, SOLO);
				} else {
					if (!MUTE.equals(playerMutedStates.get(fileName))) {
						playerMutedStates.remove(fileName);
					}
				}
				// superfluous except for the first time 
				// we are working directly with the map in the preferences, not a copy, if it already existed
				Preferences.set(INDIVIDUAL_PLAYER_MUTE_SOLO_PREF, playerMutedStates, 
						vm.getTranscription(), false, false);
				
				if (source.wasNotExpected(selected)) {
					if (selected) {
						// Deselect other solo checkboxes, and check all mutes that are not "ours".
						for (int i = 0; i < soloButtons.size(); i++) {
							ERadioButton solobox = soloButtons.get(i);
							if (source == solobox) {
								muteButtons.get(i).setSelected(false);
							} else {
								muteButtons.get(i).setSelected(true);
								solobox.setSelected(false);
							}
						}
					} else {
						// un-Mute all
						for (ECheckBox box: muteButtons) {
							box.setSelected(false);
						}				
					}
				}
			}
		});
    }
    
    /**
     * Check the preferences to see if the user wants volume control sliders for individual media players.
     * Default to <code>true</code> if no preference is set.
     * See {@link mpi.eudico.client.annotator.prefs.gui.MediaNavPanel#origShowVolumeControls} for default value
     * in the preferences panel.
     */
	private boolean haveSliders() {
		Boolean boolPref = Preferences.getBool(HAVE_INDIVIDUAL_VOLUME_CONTROLS_PREF, null);
    	boolean haveVolumeControls = true;
    	if (boolPref != null) {
    		haveVolumeControls = boolPref;
    	}
		return haveVolumeControls;
	}
    
	/**
     * A thread that plays an interval of duration {@code t n} times and then 
     * shifts the interval forward with a step size {@code s}.
     * 
     * @author Han Sloetjes
     */
    class StepAndRepeatThread extends Thread {
    	private long interval = 2000;
    	private long repeats = 3;// number of repeat, or total number of times each interval is played
    	private long step = 1000;
    	private long pauseBetweenLoops = 500;
    	private long begin, end;
    	private long ultimateEnd;
    	private long count = 0;// count from 0 or 1?
    	
    	/**
		 * Constructor, initializes fields based on settings stored in the step-and-repeat panel.
		 */
		public StepAndRepeatThread() {
			super();
			
			if (stepAndRepeatPanel.getBeginTime() < 0) {
				begin = getMediaTime();
			} else {
				begin = stepAndRepeatPanel.getBeginTime();
			}
			if (begin == getMediaDuration()) {
				begin = 0;//?? restart from begin?
			}
			interval = stepAndRepeatPanel.getIntervalDuration();
			end = begin + interval;
			repeats = stepAndRepeatPanel.getNumRepeats();
			step = stepAndRepeatPanel.getStepSize();
			pauseBetweenLoops = stepAndRepeatPanel.getPauseDuration();
			
			if (stepAndRepeatPanel.getEndTime() <= 0) {
				ultimateEnd = getMediaDuration();
			} else {
				ultimateEnd = stepAndRepeatPanel.getEndTime();
			    if (ultimateEnd < begin + interval) {
			    	ultimateEnd = begin + interval;// or change the interval?
			    	if (ultimateEnd > getMediaDuration()) {
			    		ultimateEnd = getMediaDuration();
			    		interval = ultimateEnd - begin;
			    	}
			    }
			}
		}


		@Override
		public void run() {
			
			if (!playerIsPlaying()) {
				playInterval(begin, end);
				// wait until playing is started
				while (!playerIsPlaying()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ie) {
						return;
					}
				}
			}
			//System.out.println("Start playing at " + begin);
			
    		while (!isInterrupted()) {
    			if (!playerIsPlaying()) {
    				if (isInterrupted()) {
    					return;
    				}
    				//System.out.println("Playing interval at " + begin + " count: " + count);
    				playInterval(begin, end);
    				// wait until playing is started
    				while (!playerIsPlaying()) {
    					try {
    						Thread.sleep(10);
    					} catch (InterruptedException ie) {
    						return;
    					}
    				}
    			}
    			
                while (playerIsPlaying()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                    	try {
                    		vm.getMasterMediaPlayer().stop();
                    		return;
                    	} catch (Exception eex) {
                    		
                    	}
                    }
                    if (isInterrupted()) {
                    	try {
                    		vm.getMasterMediaPlayer().stop();
                    		return;
                    	} catch (Exception eex) {
                    		
                    	}
                    }
                }
                
                //System.out.println("Playing at end of interval " + end + " count: " + count);
                try {
                    Thread.sleep(pauseBetweenLoops);
                } catch (Exception ex) {
                	break;
                }
                
                count++;
                if (count == repeats) {
                	begin += step;// check media duration
                	if (begin >= ultimateEnd) {
                		break;
                	}
                	end += step;// check media duration
                	if (end > ultimateEnd) {
                		end = ultimateEnd;
                	} else if (ultimateEnd - end < step) {
                		end = ultimateEnd;
                	}
                	// if the remaining interval is too short, break
                	if (end - begin < 100) {
                		break;
                	}
                	count = 0;                	
                }
    		}
    		
    		ElanMediaPlayerController.this.setStepAndRepeatMode(false);
    	}// end run
    		
    }// end StepAndRepeatThread class
    
}
//end of ElanMediaPlayerController
