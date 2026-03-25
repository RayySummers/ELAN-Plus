package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.annotator.player.JavaSoundPlayer;
import mpi.eudico.client.annotator.spectrogram.SpectrogramSettingsIO;
import mpi.eudico.client.annotator.viewer.TimeRuler;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.PeriodicUpdateController;
import mpi.eudico.client.mediacontrol.StartEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.client.util.WAVEncoder;
import mpi.eudico.util.TimeFormatter;
import nl.mpi.media.spectrogram.Frequency;
import nl.mpi.media.spectrogram.ImageCreator;
import nl.mpi.media.spectrogram.SpectrogramSettings;
import nl.mpi.media.spectrogram.SpectrogramSettings.FREQ_CHANNEL;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * A JPanel for rendering the audio spectrogram of a selection of an audio
 * source. It is intended for showing a selection with some extra samples to
 * the left and right of that selection. The selection boundaries are editable
 * in this viewer.
 *
 * @author Allan van Hulst
 * @version 1.0
 */
@SuppressWarnings("serial")
public class AudioSpectrogramPanel extends JPanel implements ControllerListener, ActionListener, ItemListener {
	// keep a copy of the passed audio samples
	private int[][] origAudioSamples;
    private int[][] audioSamples;
    private double[][][] freqData;
    private SpectrogramSettings settings;
    private Selection selection;
    private long intervalStart;
    private long intervalEnd;
    private int selectionBeginPos;
    private int selectionEndPos;
    /** the 'current' time (in 'panel' space) */
    private long crossHairTime;
    /** the pixel (x-)position representing the current time */
    private int crossHairPos;
    private AlphaComposite alpha02 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f);

    // border around the actual image, for frequency labels (left),
    // selection knobs (bottom), time scale (top), ? (right)
    // all painted in paintComponent, or on separate panels in a
    // border layout?
    private ClosableDialog dialog;
    private double msPerPixel = 10d;
    private PanelComponentListener resizeListener;
    
    private JButton closeButton;
    private JButton playPauseButton;
    private JButton playSelectionButton;
    private JButton configureButton;
    private Icon playIcon;
    private Icon pauseIcon;
    private JavaSoundPlayer player;
    private PeriodicUpdateController updateController;
    
    //private JPanel mainPanel;
    private SpectrogramPanel spectrogramPanel;
    private ImageCreator imgCreator;
    private BufferedImage spectrogramImage;
    private BasicStroke dashLine = new BasicStroke(0.75f, BasicStroke.CAP_BUTT, 
    		BasicStroke.JOIN_BEVEL, 1f, new float[] {8, 10}, 0f);
    private TimeRuler timeRuler;
    private JPanel rulerPanel;
    private JPanel buttonPanel;
    private JPanel leftLabelPanel;
    private JPanel rightPanel;
	private JLabel highFreqLabel;
	private JLabel lowFreqLabel;
	private final int labelMargin = 4;
	private MouseHandler mouseHandler;
	private int knobSize = 10;
	// channel menu
	private boolean multiChannelMode = false;
	private JPopupMenu popupMenu;
    private JMenu channelMenu;
    private JRadioButtonMenuItem channel1MI;
    private JRadioButtonMenuItem channel2MI;
    private JRadioButtonMenuItem channel12MI;
    private JRadioButtonMenuItem channel12SeparateMI;

    /**
     * No argument constructor
     */
    public AudioSpectrogramPanel() {
        super(null);
        settings = loadSettings();
        initComponents();
    }


    /**
     * Constructor to  audio samples, frequency data, settings, selection, interval start and interval end variables
     *
     * @param audioSamples the audio samples array
     * @param settings spectrogram settings, if {@code null} default settings will be used
     * @param selection selection object, the currently selected interval
     * @param intervalStart interval start time
     * @param intervalEnd interval end time
     */
    public AudioSpectrogramPanel(int[][] audioSamples,
    							 SpectrogramSettings settings, Selection selection,
                                 long intervalStart, long intervalEnd) {
        super(null); // null layout
        if (settings != null) {
        	this.settings = settings;
        } else {
        	this.settings = loadSettings();
        }
        if (audioSamples.length > 1 && audioSamples[1] != null) {
        	multiChannelMode = true;
        	initPreferences();
        }
        initComponents();
        // store/set local references
        // wait for setVisible and/or setSize before generating the image
        showSpectrogram(audioSamples, settings, selection, intervalStart, intervalEnd);
    }
    
    private void initComponents() {
    	setLayout(new GridBagLayout());
    	spectrogramPanel = new SpectrogramPanel();
    	leftLabelPanel = createLeftLabelPanel();
    	rightPanel = new JPanel();
    	buttonPanel = createButtonPanel();
    	
        if (Constants.DEFAULT_LF_LABEL_FONT != null) {
            timeRuler = new TimeRuler(Constants.deriveSmallFont(Constants.DEFAULT_LF_LABEL_FONT), 
            		TimeFormatter.toString(0), 5);
        } else {
        	timeRuler = new TimeRuler(Constants.DEFAULTFONT, TimeFormatter.toString(0), 5);
        }
        rulerPanel = new TimeRulerPanel(timeRuler);
        rulerPanel.setPreferredSize(new Dimension(100, timeRuler.getHeight()));
        //leftLabelPanel.setPreferredSize(new Dimension(100, 20));
        rightPanel.setPreferredSize(leftLabelPanel.getPreferredSize());
        
    	//Insets marginInsets = new Insets(2, 2, 2, 2);
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	
    	// filler left top corner
    	add(new JPanel(), gbc);
    	
    	gbc.gridx = 1;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 1.0d;
    	add(rulerPanel, gbc);
    	
    	// filler right top corner
    	gbc.gridx = 2;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.weightx = 0.0d;
    	add(new JPanel(), gbc);
    	
    	gbc.gridx = 0;
    	gbc.gridy = 1;
    	gbc.fill = GridBagConstraints.VERTICAL;
    	gbc.weightx = 0.0d;
    	gbc.weighty = 1.0d;
    	add(leftLabelPanel, gbc);
    	
    	gbc.gridx = 1;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = 1.0d;
    	add(spectrogramPanel, gbc);
    	
    	gbc.gridx = 2;
    	gbc.fill = GridBagConstraints.VERTICAL;
    	gbc.weightx = 0.0d;
    	add(rightPanel, gbc);
    	
    	// filler left bottom corner
    	gbc.gridx = 0;
    	gbc.gridy = 2;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.weightx = 0.0d;
    	gbc.weighty = 0.0d;
    	add(new JPanel(), gbc);
    	
    	gbc.gridx = 1;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 1.0d;
    	add(buttonPanel, gbc);
    	
    	// filler right bottom corner
    	gbc.gridx = 2;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.weightx = 0.0d;
    	add(new JPanel(), gbc);
    	
        intervalStart = 1000;
        intervalEnd = 11000;
        resizeListener = new PanelComponentListener();
        addComponentListener(resizeListener);
        mouseHandler = new MouseHandler();
    	spectrogramPanel.addMouseListener(mouseHandler);
    	spectrogramPanel.addMouseMotionListener(mouseHandler);
		setFocusable(true);	// to receive key strokes
		//setFocusTraversalKeysEnabled(false); // to be able to use the TAB key for Play/Pause	
    	addKeyBoardActions();
    }
    
    /*
     * Adds keyboard actions to the action map and input map.
     */
	private void addKeyBoardActions() {
		InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		
		getActionMap().put(ELANCommandFactory.PLAY_PAUSE, new PlayPauseAction());
		im.put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
					ELANCommandFactory.PLAY_PAUSE, ELANCommandFactory.ANNOTATION_MODE), 
				ELANCommandFactory.PLAY_PAUSE);
		// add the TAB key, this only works until a button or the panel has been clicked
		// otherwise maybe all buttons need to be set to be not focusable ?
		// im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), 
		//		ELANCommandFactory.PLAY_PAUSE);
    	
    	getActionMap().put(ELANCommandFactory.PLAY_SELECTION, new PlaySelectionAction());
    	im.put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
				ELANCommandFactory.PLAY_SELECTION, ELANCommandFactory.ANNOTATION_MODE), 
    			ELANCommandFactory.PLAY_SELECTION);
    	
    	// begin boundary left
    	getActionMap().put("BL", new KeyBoardMoveActions("BL"));
    	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "BL");
    	// begin boundary right
    	getActionMap().put("BR", new KeyBoardMoveActions("BR"));
    	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "BR");
    	// end boundary left
    	getActionMap().put("EL", new KeyBoardMoveActions("EL"));
    	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 
    			InputEvent.SHIFT_DOWN_MASK), "EL");
    	// end boundary right
    	getActionMap().put("ER", new KeyBoardMoveActions("ER"));
    	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 
    			InputEvent.SHIFT_DOWN_MASK), "ER");
	}
    
    /*
     * Only needs to be loaded if the audio contains more than one channel.
     */
    private void initPreferences() {
    	String chMode = Preferences.getString("AudioSpectrogramPanel.ChannelMode", null);
        if (chMode != null) {
        	if (chMode.equals(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_1.toString())) {
        		settings.setChannelMode(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_1);
        	} else if (chMode.equals(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_2.toString())) {
        		settings.setChannelMode(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_2);
        	} else if (chMode.equals(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_SEPARATE.toString())) {
        		settings.setChannelMode(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_SEPARATE);
        	} else {
        		settings.setChannelMode(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_ALL);
        	}
        }
    }

    /**
     * Updates the panel with sample data for a new spectrogram image and an
     * in-memory media player.<br>
     * If the panel is to be reused for consecutive actions to show the 
     * spectrogram of a selected time interval, this method could be made 
     * public (again). Maybe cleanup of existing objects (player, listeners)
     * would be required then.
     *
     * @param audioSamples the audio samples arrays, one or two separate channels
     * @param settings spectrogram settings
     * @param selection selection object, the currently selected interval
     * @param intervalStart interval start time
     * @param intervalEnd interval end time
     */
    private void showSpectrogram(int[][] audioSamples, 
    							SpectrogramSettings settings, Selection selection,
                                long intervalStart, long intervalEnd) {
    	origAudioSamples = audioSamples;
        this.audioSamples = new int[2][];
        if (settings != null) {
        	this.settings = settings;
        }
        this.selection = selection;
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
        applyChannelMode();
        // create frequency bins
		createFrequencies();		
        // create image
		createImage();
        repaint();
        createPlayer();
    }

    
    /*
     * Create a panel containing buttons for playing/pausing, configuring and
     * for closing the window.
     * 
     * @return a JPanel with buttons
     */
    private JPanel createButtonPanel() {
    	SpringLayout buttonLayout = new SpringLayout();
        JPanel panel = new JPanel(buttonLayout);
        int margin = 4;
        panel.setBorder(new EmptyBorder(margin, margin, margin, margin));

		playIcon = new ImageIcon(this.getClass().getResource(
				Constants.ICON_LOCATION + "PlayButton.gif"));
		pauseIcon = new ImageIcon(this.getClass().getResource(
				Constants.ICON_LOCATION + "PauseButton.gif"));
        Icon playSelectionIcon = new ImageIcon(this.getClass().getResource(
				Constants.ICON_LOCATION + "PlaySelectionButton.gif"));
        Icon configureIcon = new ImageIcon(this.getClass().getResource(
				Constants.ICON_LOCATION + "Configure16.gif"));
        

        playPauseButton = new JButton(playIcon);
        playPauseButton.addActionListener(this);
        
        playSelectionButton = new JButton(playSelectionIcon);
        playSelectionButton.addActionListener(this);
        
        configureButton = new JButton(configureIcon);
        configureButton.addActionListener(this);
        
        closeButton = new JButton(ElanLocale.getString("Button.Close"));
        closeButton.addActionListener(this);

        panel.add(playPauseButton);
        panel.add(playSelectionButton);
        panel.add(configureButton);
        panel.add(closeButton);
        
        buttonLayout.putConstraint(SpringLayout.EAST, playSelectionButton, 2 * -margin, 
        		SpringLayout.HORIZONTAL_CENTER, panel);
        buttonLayout.putConstraint(SpringLayout.EAST, playPauseButton, -margin, 
        		SpringLayout.WEST, playSelectionButton);
        buttonLayout.putConstraint(SpringLayout.WEST, configureButton, 2 * margin, 
        		SpringLayout.HORIZONTAL_CENTER, panel);
        buttonLayout.putConstraint(SpringLayout.WEST, closeButton, margin, 
        		SpringLayout.EAST, configureButton);
        buttonLayout.putConstraint(SpringLayout.VERTICAL_CENTER, playSelectionButton, 0, 
        		SpringLayout.VERTICAL_CENTER, panel);
        buttonLayout.putConstraint(SpringLayout.VERTICAL_CENTER, playPauseButton, 0, 
        		SpringLayout.VERTICAL_CENTER, panel);
        buttonLayout.putConstraint(SpringLayout.VERTICAL_CENTER, configureButton, 0, 
        		SpringLayout.VERTICAL_CENTER, panel);
        buttonLayout.putConstraint(SpringLayout.VERTICAL_CENTER, closeButton, 0, 
        		SpringLayout.VERTICAL_CENTER, panel);
        // random preferred width; could calculate the sum of preferred widths of all buttons etc.
        panel.setPreferredSize(new Dimension(100, closeButton.getPreferredSize().height + 2 * margin));

        return panel;
    }
    
    /*
     * Creates a panel for frequency labels to the left of the spectrogram.
     * 
     * @return a panel with frequency labels (in Hz.)
     */
    private JPanel createLeftLabelPanel() {
		SpringLayout labelLayout = new SpringLayout();
		JPanel panel = new JPanel(labelLayout);
		
		highFreqLabel = new JLabel(String.valueOf((int) settings.getMaxDisplayFrequency()) + " Hz");
		lowFreqLabel = new JLabel(String.valueOf((int) settings.getMinDisplayFrequency()) + " Hz");
		panel.add(highFreqLabel);
		panel.add(lowFreqLabel);
		
		labelLayout.putConstraint(SpringLayout.NORTH, highFreqLabel, labelMargin, 
				SpringLayout.NORTH, panel);
		labelLayout.putConstraint(SpringLayout.EAST, highFreqLabel, -labelMargin,
				SpringLayout.EAST, panel);
		labelLayout.putConstraint(SpringLayout.SOUTH, lowFreqLabel, -labelMargin, 
				SpringLayout.SOUTH, panel);
		labelLayout.putConstraint(SpringLayout.EAST, lowFreqLabel, -labelMargin,
				SpringLayout.EAST, panel);
		
		int prefWidth =  Math.max(highFreqLabel.getPreferredSize().width, 
				lowFreqLabel.getPreferredSize().width) + 2 * labelMargin;
		panel.setPreferredSize(new Dimension(prefWidth, 40));
		
		return panel;
	}
    
    /*
     * Creates a popup menu for changing which channel(s) should be shown.
     */
    private void createPopupMenu() {
        popupMenu = new JPopupMenu("AudioSpectrogramPanel");
		channelMenu = new JMenu(ElanLocale.getString("SpectrogramViewer.AudioChannel"));
		ButtonGroup channelGroup = new ButtonGroup();
		channel1MI = new JRadioButtonMenuItem(ElanLocale.getString("SpectrogramViewer.Channel1"), 
				settings.getChannelMode() == SpectrogramSettings.FREQ_CHANNEL.CHANNEL_1);
		channel1MI.addItemListener(this);
		channelGroup.add(channel1MI);
		channelMenu.add(channel1MI);
		channel2MI = new JRadioButtonMenuItem(ElanLocale.getString("SpectrogramViewer.Channel2"), 
				settings.getChannelMode() == SpectrogramSettings.FREQ_CHANNEL.CHANNEL_2);
		channel2MI.addItemListener(this);
		channelGroup.add(channel2MI);
		channelMenu.add(channel2MI);
		channel12MI = new JRadioButtonMenuItem(ElanLocale.getString("SpectrogramViewer.Channel12"),
				settings.getChannelMode() == SpectrogramSettings.FREQ_CHANNEL.CHANNEL_ALL);
		channel12MI.addItemListener(this);
		channelGroup.add(channel12MI);
		channelMenu.add(channel12MI);
		channel12SeparateMI = new JRadioButtonMenuItem(ElanLocale.getString("SpectrogramViewer.Channel12Separate"),
				settings.getChannelMode() == SpectrogramSettings.FREQ_CHANNEL.CHANNEL_SEPARATE);
		channel12SeparateMI.addItemListener(this);
		channelGroup.add(channel12SeparateMI);
		channelMenu.add(channel12SeparateMI);
		popupMenu.add(channelMenu);
    }
    
    /*
     * Loads settings that have been saved before or creates a settings object
     * with default settings.
     *  
     * @return a spectrogram settings instance
     */
    private SpectrogramSettings loadSettings() {
    	return SpectrogramSettingsIO.loadSpectrogramSettings();
    }
    
    /*
     * Based on the selected channel mode, the audio samples array(s) are 
     * filled by copying from or merging of the original, cached arrays that
     * have been passed to the constructor.  
     */
    private void applyChannelMode() {
    	switch (settings.getChannelMode()) {
    	case CHANNEL_1:
    		audioSamples[0] = Arrays.copyOf(origAudioSamples[0], origAudioSamples[0].length);
    		audioSamples[1] = null;
    		break;
    	case CHANNEL_2:
    		if (origAudioSamples.length > 1) {
    			audioSamples[0] = Arrays.copyOf(origAudioSamples[1], origAudioSamples[1].length);
    			audioSamples[1] = null;
    		} else {
        		audioSamples[0] = Arrays.copyOf(origAudioSamples[0], origAudioSamples[0].length);
        		audioSamples[1] = null;
    		}
    		break;
    	case CHANNEL_SEPARATE:
    		audioSamples[0] = Arrays.copyOf(origAudioSamples[0], origAudioSamples[0].length);
    		if (origAudioSamples.length > 1) {
    			audioSamples[1] = Arrays.copyOf(origAudioSamples[1], origAudioSamples[1].length);
    		} else {
    			audioSamples[1] = null;
    		}
    		break;
    	case CHANNEL_ALL:
    		// merge channel 1 and 2 if they both exist
    		if (origAudioSamples.length > 1 && origAudioSamples[1] != null) {
    			int[] mergedChannel = new int[origAudioSamples[0].length];
    			for (int i = 0; i < origAudioSamples[0].length; i++) {
    				mergedChannel[i] = (origAudioSamples[0][i] + origAudioSamples[1][i]) / 2;
    			}
    			audioSamples[0] = mergedChannel;
    			audioSamples[1] = null;
    		}
    		break;
    		default:
    			break;
    	}
    	
    }
    
    /*
     * Creates arrays of frequencies based on the provided audio samples.
     * Assumes the sample array(s) is/are not {@c0de null}. The actual creation
     * is delegated to the {@link Frequency} class. 
     */
    private void createFrequencies() {
        freqData = new double[2][][];
        freqData[0] = Frequency.getFrequencies(audioSamples[0], audioSamples[0].length, 
        		settings);
		if (audioSamples[1] != null) {
			freqData[1] = Frequency.getFrequencies(audioSamples[1], audioSamples[1].length, 
					settings);
		}
    }
    
    /*
     * Creates the spectrogram image based on the current settings and 
     * frequency array(s). The creation is delegated to the {@link ImageCreator}
     * class.
     */
    private void createImage() {
    	if (spectrogramPanel.getWidth() == 0 || spectrogramPanel.getHeight() == 0) {
    		return;
    	}
        if (imgCreator == null) {
        	imgCreator = new ImageCreator(settings);
        }
    	if (freqData != null) {
			if (freqData[1] == null || settings.getChannelMode() != SpectrogramSettings.FREQ_CHANNEL.CHANNEL_SEPARATE) {
				spectrogramImage = imgCreator.createSpecImage(freqData[0], 
						spectrogramPanel.getWidth(), spectrogramPanel.getHeight(), 
						SpectrogramSettings.PERFORMANCE.QUALITY);
			} else {
				spectrogramImage = imgCreator.createStereoSpecImage(freqData, 
						spectrogramPanel.getWidth(), spectrogramPanel.getHeight(), 
						SpectrogramSettings.PERFORMANCE.QUALITY);
			}
    	}
    }

    /*
     * The resolution (the number of milliseconds each horizontal pixel
     * represents and the number of frequency bins each vertical pixel
     * represents) depends on the size of the image panel. When the window
     * is resized, these values have to be recalculated. 
     */
	private void recalculateResolution() {
		int w = spectrogramPanel.getWidth();
		
		long totalInterval = intervalEnd - intervalStart;
		msPerPixel = totalInterval / (double) w;
		selectionBeginPos = mediaTimeToPixel(selection.getBeginTime());
		selectionEndPos = mediaTimeToPixel(selection.getEndTime());
		crossHairPos = panelTimeToPixel(crossHairTime);
		settings.setPixelDurationSec(msPerPixel / 1000d);
		createImage();
		repaint();
	}
	
	private int mediaTimeToPixel(long time) {
		return (int) ((time - intervalStart) / msPerPixel);
	}

	private int panelTimeToPixel(long time) {
		return (int) (time / msPerPixel);
	}
	
	private long pixelToPanelTime(int pixel) {
		return (long) (pixel * msPerPixel);
	}
	
	private long pixelToMediaTime(int pixel) {
		return (long) (pixel * msPerPixel) + intervalStart;
	}
	
	/*
	 * The provided audio samples are used to create a {@code JavaSound} media
	 * player. This means that the main media player of the calling context is
	 * not used, but the audio samples that are passed for the display of the
	 * spectrogram (which may have been taken from a different linked file). 
	 */
	private void createPlayer() {
		// hard-coded conversion of 32-bit to 16-bit to suit JavaSound
		int playerBitsPerSample = settings.getBitsPerSample() == 32 ? 16 : settings.getBitsPerSample();
		AudioFormat format = new AudioFormat((float) settings.getSampleFrequency(), 
				playerBitsPerSample, (audioSamples[1] == null ? 1 : 2), settings.getBitsPerSample() > 8,
				false);
		byte[] waveArray = settings.getBitsPerSample() == 32 ? 
				WAVEncoder.toWaveArray32To16Bit(audioSamples, (int) settings.getSampleFrequency()) :
					WAVEncoder.toWaveArray(audioSamples, settings.getBitsPerSample(), 
							(int) settings.getSampleFrequency());
		player = new JavaSoundPlayer(waveArray, format);
		
		updateController = new PeriodicUpdateController(20);
		updateController.addControllerListener(this);
		player.addController(updateController);
	}
	
	/**
	 * If the host dialog or frame is closed, it can call this method to 
	 * trigger clean up of resources.
	 */
	void destroyPlayer() {
		if (player != null) {
			player.removeController(updateController);
			player.cleanUpOnClose();
			audioSamples = null;
			player = null;
		}
	}

	/**
     * Show the current panel in a dialog.
     *
     * @param owner the owner frame
     */
    public void show(Frame owner) {
        dialog = new AudioSpectrogramDialog(owner, this);
        dialog.setVisible(true);
    }

	@Override
	public void controllerUpdate(ControllerEvent event) {
		if (event instanceof StartEvent) {
			playPauseButton.setIcon(pauseIcon);
		} else if (event instanceof StopEvent) {
			playPauseButton.setIcon(playIcon);
		}
		if (player == null) {
			return;
		}
		crossHairTime = player.getMediaTime();
		crossHairPos = panelTimeToPixel(crossHairTime);
		repaint();

		if (event instanceof TimeEvent) {
			if (crossHairTime >= player.getMediaDuration() && player.isPlaying()) {
				player.stop();
				playPauseButton.setIcon(playIcon);
			}
		}
	}
	
    /**
     * Handle button clicks
     *
     * @param evt The action event
     */
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == playPauseButton) {
        	if (player == null) {
        		return;
        	}
        	if (player.isPlaying()) {
        		player.stop();
        	} else {
        		player.start();
        	}
        } else if (evt.getSource() == playSelectionButton) {
        	if (player == null) {
        		return;
        	}
        	if (!player.isPlaying()) {
        		player.playInterval(pixelToPanelTime(selectionBeginPos), 
        				pixelToPanelTime(selectionEndPos));
        	} else {
        		player.stop();
        	}
        	
        } else if (evt.getSource() == closeButton) {
        	if (dialog != null) {
        		// remove listeners?
        		dialog.setVisible(false);
        		dialog.dispose();
        	}
        } else if (evt.getSource() == configureButton) {
        	SpectrogramSettingsDialog dialog = new SpectrogramSettingsDialog(null, settings);

        	dialog.setVisible(true);
        	
        	if (settings.isNewWindowDataRequired() || settings.isNewTransformRequired() || 
        			settings.isNewImageRequired()) {
        		highFreqLabel.setText(String.valueOf((int) settings.getMaxDisplayFrequency()) + " Hz");
        		lowFreqLabel.setText(String.valueOf((int) settings.getMinDisplayFrequency()) + " Hz");
        		
        		int prefWidth =  Math.max(highFreqLabel.getPreferredSize().width, 
        				lowFreqLabel.getPreferredSize().width) + 2 * labelMargin;
        		leftLabelPanel.setPreferredSize(new Dimension(prefWidth, 40));
        		if (settings.isNewWindowDataRequired() || settings.isNewTransformRequired()) {
        			createFrequencies();
        		}
        		createImage();
        		repaint();
        		SpectrogramSettingsIO.storeSpectrogramSettings(settings);
        	}
        }
    }
    
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getSource() == channel1MI) {
				settings.setChannelMode(FREQ_CHANNEL.CHANNEL_1);			
			} else if (e.getSource() == channel2MI) {
				settings.setChannelMode(FREQ_CHANNEL.CHANNEL_2);	
			} else if (e.getSource() == channel12MI) {
				settings.setChannelMode(FREQ_CHANNEL.CHANNEL_ALL);
			} else if (e.getSource() == channel12SeparateMI) {
				settings.setChannelMode(FREQ_CHANNEL.CHANNEL_SEPARATE);
			}
			applyChannelMode();
		}

        // recreate frequency bins and image
		createFrequencies();
		createImage();
        repaint();
		Preferences.set("AudioSpectrogramPanel.ChannelMode", settings.getChannelMode().toString(), 
				null, false, false);		
	}

	/**
	 * The panel for displaying the spectrogram image, the selection boundaries
	 * and the media time crosshair.
	 */
	class SpectrogramPanel extends JComponent {
		KnobIcon topKnob = new KnobIcon(SwingConstants.TOP, knobSize);
		KnobIcon bottomKnob = new KnobIcon(SwingConstants.BOTTOM, knobSize);
		
        /**
         * Re-draw the spectrogram.
         *
         * @param g The graphics context
         */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            synchronized (getTreeLock()) {
                Graphics2D g2d = (Graphics2D) g;
                // paint background
                g2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // paint image, creating a border around it
				if (spectrogramImage != null) {
					g2d.drawImage(spectrogramImage, 0, 0, this);
				}
                // draw selection markers and knobs
				int h = getHeight();
				Stroke basicStr = g2d.getStroke();
	            g2d.setColor(Constants.SELECTIONCOLOR);
	            g2d.setComposite(alpha02);
	            g2d.fillRect(selectionBeginPos, 0, selectionEndPos - selectionBeginPos, h - 1);
	            g2d.setComposite(AlphaComposite.Src);
	            g2d.setColor(Constants.ACTIVEANNOTATIONCOLOR);
	            g2d.setStroke(dashLine);
	            g2d.drawLine(selectionBeginPos, 0, selectionBeginPos, h - 1);
	            g2d.drawLine(selectionEndPos, 0, selectionEndPos, h - 1);
	            
	            // draw crosshair
		        g2d.setStroke(basicStr);
		        g2d.setColor(Constants.CROSSHAIRCOLOR);
		        g2d.drawLine(crossHairPos, 0, crossHairPos, h);
		        
		        // draw knobs
		        topKnob.paintIcon(this, g2d, selectionBeginPos, 0);
		        topKnob.paintIcon(this, g2d, selectionEndPos, 0);
		        bottomKnob.paintIcon(this, g2d, selectionBeginPos, h - 1);
		        bottomKnob.paintIcon(this, g2d, selectionEndPos, h - 1);
                // draw frame round image
                g2d.setColor(getForeground());
                g2d.drawRect(0, 0, getWidth(), getHeight() - 1);
                
                if (crossHairPos == 0 || crossHairPos >= getWidth()) {
                	int x = crossHairPos >= getWidth() ? getWidth() - 1 : crossHairPos;
    		        g2d.setColor(Constants.CROSSHAIRCOLOR);
    		        g2d.drawLine(x, 0, x, h);
                }
            }

        }
        
        /**
         * A small icon for dragging a boundary with the mouse. 
         */
        class KnobIcon implements Icon {
        	int position;
        	int size = 10;
        	int[] xc = new int[3];
        	int[] yc = new int[3];
        	
        	/**
        	 * Constructor.
        	 * 
        	 * @param position top or bottom as a {@link SwingConstants}
        	 * @param size the size of the knob in pixels
        	 */
        	KnobIcon(int position, int size) {
        		this.position = position;
        		this.size = size;
        	}

			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				Graphics2D g2d = (Graphics2D) g;
				
				int[] xc = new int[3];
				if (position == SwingConstants.TOP) {
					xc[0] = x - size;
					xc[1] = x + size;
					xc[2] = x;
					yc[0] = y;
					yc[1] = y;
					yc[2] = y + size;
				} else {
					xc[0] = x - size;
					xc[1] = x + size;
					xc[2] = x;
					yc[0] = y;
					yc[1] = y;
					yc[2] = y - size;
				}
				g2d.setColor(c.getBackground());
				g2d.fillPolygon(xc, yc, xc.length);
				g2d.setColor(Constants.ACTIVEANNOTATIONCOLOR);
				g2d.drawPolygon(xc, yc, xc.length);
			}

			@Override
			public int getIconWidth() {
				return 2 * size;
			}

			@Override
			public int getIconHeight() {
				return size;
			}
        	
        }
    }
    
	/**
	 * A panel for displaying a time ruler next to the spectrogram.
	 */
	class TimeRulerPanel extends JPanel {
    	TimeRuler ruler;
    	
    	/**
    	 * Constructor.
    	 * 
    	 * @param ruler the {@link TimeRuler} to display
    	 */
    	TimeRulerPanel(TimeRuler ruler) {
    		this.ruler = ruler;
    	}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
	        synchronized (getTreeLock()) {
	            Graphics2D g2d = (Graphics2D) g;
	            double dx = intervalStart / msPerPixel;
	            g2d.translate(-dx, 0); 
	            ruler.paint(g2d, intervalStart, getWidth(), (float) msPerPixel, SwingConstants.BOTTOM);
	            g2d.translate(dx, 0);
	        }
		}
    }
	
	/**
	 * Implementation of a component listener, initiates (re)calculation of
	 * the spectrogram after a resize of the window.
	 */
	class PanelComponentListener implements ComponentListener {

		@Override
		public void componentResized(ComponentEvent e) {
			recalculateResolution();
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			// stub
		}

		@Override
		public void componentShown(ComponentEvent e) {
			createImage();
			repaint();
		}

		@Override
		public void componentHidden(ComponentEvent e) {
			// stub	
		}
		
	}
	
	/**
	 * Mouse listener for the spectrogram panel only. 
	 * The selection boundaries can be dragged with the mouse or moved with
	 * with the keyboard (e.g. with arrow left/right).
	 */
	class MouseHandler extends MouseAdapter {
		boolean dragLeftBoundary = false;
		boolean dragRightBoundary = false;
		int dragStartPos = 0;
		
		public MouseHandler() {
			super();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			crossHairPos = e.getX();
			crossHairTime = pixelToPanelTime(e.getX());
			if (player != null) {
				player.setMediaTime(crossHairTime);
			}
			repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
	        if (multiChannelMode && SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
	            if (popupMenu == null) {
	                createPopupMenu();
	            }
	            popupMenu.show(e.getComponent(), e.getX(), e.getY());
	            return;
	        }
	        
			int x = e.getX();
			if (Math.abs(x - selectionBeginPos) <= knobSize) {
				dragLeftBoundary = true;
			} else if (Math.abs(x - selectionEndPos) <= knobSize) {
				dragRightBoundary = true;
			}
			dragStartPos = x;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (dragLeftBoundary || dragRightBoundary) {
				dragLeftBoundary = false;
				dragRightBoundary = false;
				selection.setSelection(
						pixelToMediaTime(selectionBeginPos), pixelToMediaTime(selectionEndPos));
			}
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int x = e.getX();
			if (dragLeftBoundary) {
				x = Math.max(x, 1);
				if (selectionEndPos - x < 2 * knobSize) {
					selectionBeginPos = selectionEndPos - 2 * knobSize;
				} else {
					selectionBeginPos = x;
				}
				repaint();
			} else if (dragRightBoundary) {
				x = Math.min(x, spectrogramImage.getWidth() - 1);
				if (x - selectionBeginPos < 2 * knobSize) {
					selectionEndPos = selectionBeginPos + 2 * knobSize;
				} else {
					selectionEndPos = x;
				}
				repaint();
			}
		}

	}
	
	/*
	 * Play selection keyboard action
	 */
	private class PlaySelectionAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			playSelectionButton.doClick();
			
		}
	}
	
	/*
	 * Play/pause keyboard action.
	 */
	private class PlayPauseAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			playPauseButton.doClick();
			
		}
	}
	
	/*
	 * Keyboard actions for moving the left and right selection boundaries.
	 */
	private class KeyBoardMoveActions extends AbstractAction {
		
		/*
		 * Constructor, the name is also used as the command action key.
		 */
		KeyBoardMoveActions(String name) {
			super(name);
			putValue(Action.ACTION_COMMAND_KEY, name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("BL")) {
				moveBoundary(SwingConstants.LEFT, -1);
			} else if (e.getActionCommand().equals("BR")) {
				moveBoundary(SwingConstants.LEFT, 1);
			} else if (e.getActionCommand().equals("EL")) {
				moveBoundary(SwingConstants.RIGHT, -1);
			} else if (e.getActionCommand().equals("ER")) {
				moveBoundary(SwingConstants.RIGHT, 1);
			}			
		}
		
		/*
		 * Moves the selection begin or end boundary to the left or to the
		 * right, while ensuring that they remain within the interval (the 
		 * image's width) and that the begin boundary remains on the left
		 * side of the end boundary.
		 */
		private void moveBoundary(int side, int distance) {
			switch (side) {
			case SwingConstants.LEFT:
				int nextBeginPos = selectionBeginPos + distance;
				if (nextBeginPos > 0 && nextBeginPos < selectionEndPos - 2 * knobSize) {
					selectionBeginPos = nextBeginPos;
					selection.setSelection(
							pixelToMediaTime(selectionBeginPos), pixelToMediaTime(selectionEndPos));
					repaint();
				}
				break;
			case SwingConstants.RIGHT:
				int nextEndPos = selectionEndPos + distance;
				if (nextEndPos > selectionBeginPos + 2 * knobSize && nextEndPos < spectrogramImage.getWidth() - 1) {
					selectionEndPos = nextEndPos;
					selection.setSelection(
							pixelToMediaTime(selectionBeginPos), pixelToMediaTime(selectionEndPos));
					repaint();
				}
				break;
				default:
					
			}
		}
	}
	
}
