package mpi.eudico.client.annotator.prefs.gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.player.PlayerFactory;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;
import mpi.eudico.client.annotator.util.SystemReporting;


/**
 * A panel for OS specific preference settings.
 * Some of the options are
 * <ul>
 * <li>changing the preferred look-and-feel
 * <li>changing the preferred media framework
 * </ul> 
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class PlatformPanel extends AbstractEditPrefsPanel implements PreferenceEditor, ChangeListener {
    /** an enumeration of Look and Feel options */
    public enum LFOption {
    	/** cross platform look and feel (Java, Metal) */
    	CROSS_PLATFORM_LF,
    	/** the system's look and feel */
    	SYSTEM_LF,
    	/** the Nimbus look and feel */
    	NIMBUS_LF
    };
    // for non-Linux systems other names are used: "metal", "system", ...
    // cross platform
	private JRadioButton crossPlatformLaFRB;
    private JCheckBox crossPlatformDarkThemeCB;
    private JCheckBox crossPlatformBoldFontCB;
    private boolean origCrossPlatformDark = false;
    private boolean origCrossPlatformBold = true; 
    private ButtonGroup lafButtonGroup;
    
	// macOS
	private JCheckBox macScreenBarCB;
    private boolean origMacUseScreenBar = false;// HS May 2013 changed default to false because if nothing is set the menu is not in the screen menubar
    private JRadioButton macLaFRB;
    private String origMacLaFName;
    private JCheckBox macFileDialogCB;
    private boolean origMacFileDialog = true;
    private JRadioButton jfxRB;
    private JRadioButton javaSoundRB;
    private JRadioButton javfRB;
    private JCheckBox javfJavaCB;
    private String origMacPrefFramework = PlayerFactory.AVFN;
    private JCheckBox javfDebugModeCB;
    private boolean origJAVFDebugMode = false;
    private JCheckBox javfNativeStopCB;
    private boolean origJAVFNativeStopping = false;
    private boolean origJAVFAudioExtraction = false;
    private JRadioButton macNoAudioExtractRB;
    private JRadioButton macFFmpegAudioExtractRB;
    private JRadioButton macJAVFAudioExtractRB;
    private String origMacAudioExtractFramework = PlayerFactory.NONE;// Aug 2023: change of default to none

    // windows
    private JRadioButton jdsRB;
    private JCheckBox jmmfCB;
    private String origWinPrefFramework = PlayerFactory.JDS;
    //private JCheckBox winLAndFCB;
    private JRadioButton winLaFRB;
    //private boolean origWinLF = false;
    // use a string instead of boolean
    private String origWinLaFName;
    private boolean origJMMFEnabled = true;
    private JCheckBox correctAtPauseCB;
    private boolean origCorrectAtPause = true;
    private JCheckBox jmmfSynchronousModeCB;
    private boolean origJMMFSynchronousMode = false;// the default is asynchronous behavior
    private JCheckBox jmmfDebugModeCB;
    private boolean origJMMFDebugMode = false;
    private boolean origJMMFAudioExtraction = false;// Aug 2023: change of default to false
    private JRadioButton winNoAudioExtractRB;
    private JRadioButton winJMMFAudioExtractRB;
    private String origWinAudioExtractFramework = PlayerFactory.NONE;// Aug 2023: change of default to none
    // Linux
	private String origLinuxPrefFramework  = PlayerFactory.VLCJ;
	private String origLinuxLFPref = LFOption.CROSS_PLATFORM_LF.name();
	private JRadioButton vlcjB;
	private JRadioButton systemLAndFRB;
	private JRadioButton nimbusLAndFRB;
	private JRadioButton linuxNoAudioExtractRB;
    private JRadioButton linuxFFmpegAudioExtractRB;
    private String origLinuxAudioExtractFramework = PlayerFactory.NONE;// Aug 2023: change of default to none

    /**
     * Creates a new PlatformPanel instance.
     */
    public PlatformPanel() {
        super();
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
    	if (SystemReporting.isMacOS()) {
    		Boolean boolPref = Preferences.getBool("OS.Mac.useScreenMenuBar", null);

            if (boolPref != null) {
                origMacUseScreenBar = boolPref.booleanValue();
            }
            origMacLaFName = "system";// default on macOS
            // old preference
            boolPref = Preferences.getBool("UseMacLF", null);

            if (boolPref != null && !boolPref.booleanValue()) {
                origMacLaFName = "metal";
            }
            
            String stringPref = Preferences.getString("Mac.PrefLookAndFeel", null);
            if (stringPref != null) {
            	origMacLaFName = stringPref;
            }
            boolPref = Preferences.getBool("Mac.MetalDarkTheme", null);
            if (boolPref != null) {
            	origCrossPlatformDark = boolPref.booleanValue();
            }
            boolPref = Preferences.getBool("Mac.MetalBoldFont", null);
            if (boolPref != null) {
            	origCrossPlatformBold = boolPref.booleanValue();
            }
            
            boolPref = Preferences.getBool("UseMacFileDialog", null);

            if (boolPref != null) {
            	origMacFileDialog = boolPref.booleanValue();
            }

            stringPref = Preferences.getString("Mac.PrefMediaFramework", null);

            if (stringPref != null) {
            	if (!stringPref.equals(PlayerFactory.COCOA_QT) && 
            			!stringPref.equals(PlayerFactory.QT_MEDIA_FRAMEWORK) &&
            			!stringPref.equals(PlayerFactory.JAVF)) {
            		origMacPrefFramework = stringPref;
            	}
            }
            
            boolPref = Preferences.getBool("NativePlayer.DebugMode", null);
            
            if (boolPref != null) {
            	origJAVFDebugMode = boolPref.booleanValue();
            }
            
            boolPref = Preferences.getBool("JAVFPlayer.UseNativeStopTime", null);
            
            if (boolPref != null) {
            	origJAVFNativeStopping = boolPref.booleanValue();
            }
            // old AV Foundation boolean
            boolPref = Preferences.getBool("NativePlayer.AudioExtraction", null);
            
            if (boolPref != null) {
            	origJAVFAudioExtraction = boolPref.booleanValue();
            }
            
            stringPref = Preferences.getString("AudioExtractionFramework", null);
            
            if (stringPref != null) {
            	origMacAudioExtractFramework = stringPref;
            } else {
            	if (origJAVFAudioExtraction) {
            		origMacAudioExtractFramework = PlayerFactory.JAVF;
            	}
            }

    	} else if (SystemReporting.isWindows()) {
            String stringPref = Preferences.getString("Windows.PrefMediaFramework", null);

            if (stringPref != null) {
            	// ignore obsolete frameworks
            	if (!stringPref.equals(PlayerFactory.JMF_MEDIA_FRAMEWORK) &&
            			!stringPref.equals(PlayerFactory.QT_MEDIA_FRAMEWORK)) {
            		origWinPrefFramework = stringPref;
            	}
            }
            origWinLaFName = "metal";
            // "old" boolean preference 
            Boolean boolPref = Preferences.getBool("UseWinLF", null);

            if (boolPref != null && boolPref.booleanValue()) {
                //origWinLF = boolPref.booleanValue();
                origWinLaFName = "system";
            }
            
            stringPref = Preferences.getString("Windows.PrefLookAndFeel", null);
            if (stringPref != null) {
            	origWinLaFName = stringPref;
            }
            boolPref = Preferences.getBool("Windows.MetalDarkTheme", null);
            if (boolPref != null) {
            	origCrossPlatformDark = boolPref.booleanValue();
            }
            boolPref = Preferences.getBool("Windows.MetalBoldFont", null);
            if (boolPref != null) {
            	origCrossPlatformBold = boolPref.booleanValue();
            }
            
            boolPref = Preferences.getBool("Windows.JMMFEnabled", null);
            
            if (boolPref != null) {
            	origJMMFEnabled = boolPref.booleanValue();
            }
            
            boolPref = Preferences.getBool("Windows.JMMFPlayer.CorrectAtPause", null);
            
            if (boolPref != null) {
            	origCorrectAtPause = boolPref.booleanValue();
            }
            
            boolPref = Preferences.getBool("Windows.JMMFPlayer.SynchronousMode", null);
            
            if (boolPref != null) {
            	origJMMFSynchronousMode = boolPref.booleanValue();
            }
            
            boolPref = Preferences.getBool("NativePlayer.DebugMode", null);
            
            if (boolPref != null) {
            	origJMMFDebugMode = boolPref.booleanValue();
            }
            
            boolPref = Preferences.getBool("NativePlayer.AudioExtraction", null);
            
            if (boolPref != null) {
            	origJMMFAudioExtraction = boolPref.booleanValue();
            }
            
            stringPref = Preferences.getString("AudioExtractionFramework", null);
            
            if (stringPref != null) {
            	origWinAudioExtractFramework = stringPref;
            } else {
            	if (origJMMFAudioExtraction) {
            		origWinAudioExtractFramework = PlayerFactory.JMMF;
            	}
            }
            
    	} else if (SystemReporting.isLinux()) {
            String stringPref = Preferences.getString("Linux.PrefMediaFramework", null);

            if (stringPref != null) {
                origLinuxPrefFramework = stringPref;
            }
            stringPref = Preferences.getString("Linux.PrefLookAndFeel", null);
            if (stringPref == null) {
            	origLinuxLFPref = LFOption.CROSS_PLATFORM_LF.name();
            } else {
            	origLinuxLFPref = stringPref;
            }
            
            Boolean boolPref = Preferences.getBool("Linux.MetalDarkTheme", null);
            if (boolPref != null) {
            	origCrossPlatformDark = boolPref.booleanValue();
            }
            boolPref = Preferences.getBool("Linux.MetalBoldFont", null);
            if (boolPref != null) {
            	origCrossPlatformBold = boolPref.booleanValue();
            }
            
            stringPref = Preferences.getString("AudioExtractionFramework", null);
            
            if (stringPref != null) {
            	origLinuxAudioExtractFramework = stringPref;
            }
    	}
    }

    private void initComponents() {        
        GridBagConstraints gbc = new GridBagConstraints();
        Font plainFont = null;
        int gy = 0;
        
        if (SystemReporting.isMacOS()) {
        	super.setTitle(ElanLocale.getString("PreferencesDialog.OS.Mac"));    
        	
	        macScreenBarCB = new JCheckBox(ElanLocale.getString(
	                    "PreferencesDialog.OS.Mac.ScreenMenuBar"));
	        macScreenBarCB.setSelected(origMacUseScreenBar);
	
	        plainFont = macScreenBarCB.getFont().deriveFont(Font.PLAIN);
	        macScreenBarCB.setFont(plainFont);
	        
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.weightx = 1.0;
	        gbc.gridy = gy++;
	        gbc.gridwidth = 1;
	        gbc.insets = globalInset;	       
	        outerPanel.add(macScreenBarCB, gbc);
	
	        JLabel relaunchLabel = new JLabel();
	        ImageIcon relaunchIcon = null;
	
	        // add relaunch icon
	        try {
	            relaunchIcon = new ImageIcon(this.getClass()
	                                             .getResource("/toolbarButtonGraphics/general/Refresh16.gif"));
	            relaunchLabel.setIcon(relaunchIcon);
	        } catch (Exception ex) {
	            relaunchLabel.setText(ElanLocale.getString(
	                    "PreferencesDialog.Relaunch"));
	        }
	
	        relaunchLabel.setToolTipText(ElanLocale.getString(
	                "PreferencesDialog.Relaunch.Tooltip"));
	        macScreenBarCB.setToolTipText(ElanLocale.getString(
	                "PreferencesDialog.Relaunch.Tooltip"));
	
	        gbc.gridx = 1;
	        gbc.gridwidth = 1;
	        gbc.fill = GridBagConstraints.NONE;
	        gbc.anchor = GridBagConstraints.EAST;
	        gbc.weightx = 0.0;
	        outerPanel.add(relaunchLabel, gbc);
	        
	        macLaFRB = new JRadioButton(ElanLocale.getString(
                    "PreferencesDialog.OS.Mac.LF"));        
	        crossPlatformLaFRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.OS.LF.CrossPlatform"));
	        crossPlatformDarkThemeCB = new JCheckBox(ElanLocale.getString(
	        		"PreferencesDialog.OS.LF.DarkTheme"), origCrossPlatformDark);
	        crossPlatformBoldFontCB = new JCheckBox(ElanLocale.getString(
	        		"PreferencesDialog.OS.LF.CrossPlatform.BoldFont"), origCrossPlatformBold);
	        lafButtonGroup = new ButtonGroup();
	        lafButtonGroup.add(macLaFRB);
	        lafButtonGroup.add(crossPlatformLaFRB);
	        macLaFRB.setFont(plainFont);
	        crossPlatformLaFRB.setFont(plainFont);
	        crossPlatformDarkThemeCB.setFont(plainFont);
	        crossPlatformBoldFontCB.setFont(plainFont);
	        if (origMacLaFName.equals("system")) {
	        	macLaFRB.setSelected(true);
	        	crossPlatformDarkThemeCB.setEnabled(false);
	        	crossPlatformBoldFontCB.setEnabled(false);
	        } else {
	        	crossPlatformLaFRB.setSelected(true);
	        }
	        
	        gbc.gridy = gy++;
	        gbc.gridx = 0;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.weightx = 1.0;
	        outerPanel.add(macLaFRB, gbc);
	
	        JLabel relaunchLabel2 = new JLabel();
	
	        if (relaunchIcon != null) {
	            relaunchLabel2.setIcon(relaunchIcon);
	        } else {
	            relaunchLabel2.setText(ElanLocale.getString(
	                    "PreferencesDialog.Relaunch"));
	        }
	
	        relaunchLabel2.setToolTipText(ElanLocale.getString(
	                "PreferencesDialog.Relaunch.Tooltip"));
	        macLaFRB.setToolTipText(ElanLocale.getString(
	                "PreferencesDialog.Relaunch.Tooltip"));
	        crossPlatformLaFRB.setToolTipText(ElanLocale.getString(
	                "PreferencesDialog.Relaunch.Tooltip"));
	
	        gbc.gridx = 1;
	        gbc.gridwidth = 1;
	        gbc.fill = GridBagConstraints.NONE;
	        gbc.anchor = GridBagConstraints.EAST;
	        gbc.weightx = 0.0;
	        outerPanel.add(relaunchLabel2, gbc);
	        
	        gbc.gridx = 0;
	        gbc.gridy = gy++;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.weightx = 1.0;
	        outerPanel.add(crossPlatformLaFRB, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.insets = singleTabInset;
	        outerPanel.add(crossPlatformDarkThemeCB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(crossPlatformBoldFontCB, gbc);
	        crossPlatformLaFRB.addChangeListener(this);
	        
	        macFileDialogCB = new JCheckBox(ElanLocale.getString(
                    "PreferencesDialog.OS.Mac.FileDialog"));
	        macFileDialogCB.setSelected(origMacFileDialog);
	        macFileDialogCB.setFont(plainFont);
	        gbc.gridy = gy++;
	        gbc.gridx = 0;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.weightx = 1.0;
	        gbc.insets = globalInset;
	        outerPanel.add(macFileDialogCB, gbc);     
	
	        JLabel frameworkLabel = new JLabel(ElanLocale.getString(
	                    "Player.Framework"));
	        frameworkLabel.setFont(plainFont);
	        jfxRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.JFX"));
	        jfxRB.setFont(plainFont);
	        javaSoundRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.JavaSound"));
	        javaSoundRB.setFont(plainFont);
	        javfRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.JAVF"), true);
	        javfRB.setFont(plainFont);
	        javfJavaCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.JAVF") + " (Java Rendering)");
	        javfJavaCB.setFont(plainFont);
	        vlcjB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.VLCJ"));
	        vlcjB.setFont(plainFont);
	        javfDebugModeCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.Debug"), 
	        		origJAVFDebugMode);
	        javfDebugModeCB.setFont(plainFont);
	        javfNativeStopCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.NativeStopMechanism"),
	        		origJAVFNativeStopping);
	        javfNativeStopCB.setFont(plainFont);
	        
	        ButtonGroup gr = new ButtonGroup();
	        gr.add(javfRB);
	        gr.add(jfxRB);
	        gr.add(vlcjB);
	        gr.add(javaSoundRB);
	        
	        javfRB.addChangeListener(this);
	        
	        if (origMacPrefFramework.equals(PlayerFactory.COCOA_QT)) {
	            // leave the AVFN button selected
	        } else if (origMacPrefFramework.equals(PlayerFactory.QT_MEDIA_FRAMEWORK)){
	            // leave the AVFN button selected
	        } else if (origMacPrefFramework.equals(PlayerFactory.JAVF)) {
	        	//javfRB.setSelected(true);
	        	//javfJavaCB.setSelected(true);
	        	// leave the AVFN button selected (as of ELAN 6.2)
	        } else if (origMacPrefFramework.equals(PlayerFactory.JFX)) {
	        	jfxRB.setSelected(true);
	        } else if (origMacPrefFramework.equals(PlayerFactory.JAVA_SOUND)) {
	        	javaSoundRB.setSelected(true);
	        } else if (origMacPrefFramework.equals(PlayerFactory.VLCJ)) {
	        	vlcjB.setSelected(true);
	        }
	
	        JLabel extractionLabel = new JLabel(ElanLocale.getString(
	        		"PreferencesDialog.Media.AudioExtractionFramework"));
	        
	        macNoAudioExtractRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.AudioExtractionNone"));
	        macNoAudioExtractRB.setFont(plainFont);
	        
	        macJAVFAudioExtractRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.AudioExtractionJAVF"));
	        macJAVFAudioExtractRB.setFont(plainFont);
	        
	        macFFmpegAudioExtractRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.AudioExtractionFFmpeg"));
	        macFFmpegAudioExtractRB.setFont(plainFont);
	        
	        if (origMacAudioExtractFramework == null) {
	        	if (origJAVFAudioExtraction) {
	        		macJAVFAudioExtractRB.setSelected(true);
	        	} else {
	        		macFFmpegAudioExtractRB.setSelected(true);
	        	}
	        } else if (origMacAudioExtractFramework.equals(PlayerFactory.NONE)) {
	        	macNoAudioExtractRB.setSelected(true);
	        } else if (origMacAudioExtractFramework.equals(PlayerFactory.JAVF)) {
	        	macJAVFAudioExtractRB.setSelected(true);
	        } else {
	        	macFFmpegAudioExtractRB.setSelected(true);
	        }
	        
	        ButtonGroup aeGroup = new ButtonGroup();
	        aeGroup.add(macNoAudioExtractRB);
	        aeGroup.add(macJAVFAudioExtractRB);
	        aeGroup.add(macFFmpegAudioExtractRB);
	        
	        gbc.gridx = 0;
	        gbc.gridy = gy++;
	        gbc.gridwidth = 2;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.weightx = 1.0;
	        gbc.insets = catInset;
	        outerPanel.add(frameworkLabel, gbc);
	
	        gbc.gridy = gy++;
	        gbc.insets = globalInset;
		    outerPanel.add(javfRB, gbc);
		    
	        gbc.gridy = gy++;
	        gbc.insets = singleTabInset;
	        outerPanel.add(javfDebugModeCB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(javfNativeStopCB, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.insets = globalInset;
	        outerPanel.add(jfxRB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(vlcjB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(javaSoundRB, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.insets = catInset;
	        outerPanel.add(extractionLabel, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.insets = globalInset;
	        outerPanel.add(macNoAudioExtractRB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(macJAVFAudioExtractRB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(macFFmpegAudioExtractRB, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.gridx = 0;
	        gbc.fill = GridBagConstraints.BOTH;
	        gbc.weighty = 1.0;
	        outerPanel.add(new JPanel(), gbc); // filler
	        
	        enableMacLaFButtons(!macScreenBarCB.isSelected());
	        macScreenBarCB.addChangeListener(this);
        } else if (SystemReporting.isWindows()) {
        	 // add Windows stuff	
        	super.setTitle(ElanLocale.getString("PreferencesDialog.OS.Windows"));
	        // look and feel
	        // add relaunch icon
	        JLabel relaunchLabel = new JLabel();
	        ImageIcon relaunchIcon = null;
	        try {
	            relaunchIcon = new ImageIcon(this.getClass()
	                                             .getResource("/toolbarButtonGraphics/general/Refresh16.gif"));
	            relaunchLabel.setIcon(relaunchIcon);
	        } catch (Exception ex) {
	            relaunchLabel.setText(ElanLocale.getString(
	                    "PreferencesDialog.Relaunch"));
	        }
	
	        relaunchLabel.setToolTipText(ElanLocale.getString(
	                "PreferencesDialog.Relaunch.Tooltip"));

	        winLaFRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.OS.Windows.LF"));
	        plainFont = winLaFRB.getFont().deriveFont(Font.PLAIN);
	        crossPlatformLaFRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.OS.LF.CrossPlatform"));
	        crossPlatformDarkThemeCB = new JCheckBox(ElanLocale.getString(
	        		"PreferencesDialog.OS.LF.DarkTheme"), origCrossPlatformDark);
	        crossPlatformBoldFontCB = new JCheckBox(ElanLocale.getString(
	        		"PreferencesDialog.OS.LF.CrossPlatform.BoldFont"), origCrossPlatformBold);
	        lafButtonGroup = new ButtonGroup();
	        lafButtonGroup.add(crossPlatformLaFRB);
	        lafButtonGroup.add(winLaFRB);
	        if (origWinLaFName.equals("system")) {
	        	winLaFRB.setSelected(true);
	        	crossPlatformDarkThemeCB.setEnabled(false);
	        	crossPlatformBoldFontCB.setEnabled(false);
	        } else {
	        	crossPlatformLaFRB.setSelected(true);
	        }
	        winLaFRB.setFont(plainFont);
	        crossPlatformLaFRB.setFont(plainFont);
	        crossPlatformDarkThemeCB.setFont(plainFont);
	        crossPlatformBoldFontCB.setFont(plainFont);
	        
	        gbc = new GridBagConstraints();
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.weightx = 1.0;
	        gbc.gridy = gy++;    
	        gbc.gridwidth = 1;
	        gbc.insets = globalInset;	       	       
	        outerPanel.add(winLaFRB, gbc);
	        
	        gbc.gridx = 1;
	        gbc.fill = GridBagConstraints.NONE;
	        gbc.anchor = GridBagConstraints.EAST;
	        gbc.weightx = 0.0;
	        outerPanel.add(relaunchLabel, gbc);
	        
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.weightx = 1.0;
	        gbc.gridwidth = 2;
	        gbc.gridx = 0;
	        gbc.gridy = gy++;
	        outerPanel.add(crossPlatformLaFRB, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.insets = singleTabInset;
	        outerPanel.add(crossPlatformDarkThemeCB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(crossPlatformBoldFontCB, gbc);
	        crossPlatformLaFRB.addChangeListener(this);
	        
	       //media framework	
	        ButtonGroup winBG = new ButtonGroup();
	        jdsRB = new JRadioButton(ElanLocale.getString(
	        		"PreferencesDialog.Media.JDS"), true);
	        jmmfCB = new JCheckBox(ElanLocale.getString(
    				"PreferencesDialog.Media.JMMF"), origJMMFEnabled);
	        correctAtPauseCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.JMMF.CorrectAtPause"), 
	        		origCorrectAtPause);
	        jmmfSynchronousModeCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.JMMF.SynchronousMode"),
	        		origJMMFSynchronousMode);
	        jmmfDebugModeCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Media.Debug"), 
	        		origJMMFDebugMode);
        	jfxRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.JFX"));
	        javaSoundRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.JavaSound"));
	        vlcjB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.VLCJ"));
	        
	        winBG.add(jdsRB);
	        winBG.add(javaSoundRB);
	        winBG.add(jfxRB);
	        winBG.add(vlcjB);
	        
	        plainFont = jdsRB.getFont().deriveFont(Font.PLAIN);
	       
	        JLabel winMedia = new JLabel(ElanLocale.getString("Player.Framework"));	        	       
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.weightx = 1.0;
	        gbc.gridwidth = 2;
	        gbc.gridx = 0;
	        gbc.gridy = gy++;	        
	        gbc.insets = catInset;
	        outerPanel.add(winMedia, gbc);
	
	        if (origWinPrefFramework.equals(PlayerFactory.QT_MEDIA_FRAMEWORK)) {
	            // leave the jds radio button selected
	        } else if (origWinPrefFramework.equals(PlayerFactory.JMF_MEDIA_FRAMEWORK)) {
	            // leave the jds radio button selected
	        } else if (origWinPrefFramework.equals(PlayerFactory.JFX)) {
	            jfxRB.setSelected(true);
	        } else if (origWinPrefFramework.equals(PlayerFactory.JAVA_SOUND)) {
	            javaSoundRB.setSelected(true);
	        } else if (origWinPrefFramework.equals(PlayerFactory.VLCJ)) {
	            vlcjB.setSelected(true);
	        }
	
	        jdsRB.setFont(plainFont);
	        jmmfCB.setFont(plainFont);
	        jfxRB.setFont(plainFont);
	        javaSoundRB.setFont(plainFont);
	        vlcjB.setFont(plainFont);
	        correctAtPauseCB.setFont(plainFont);
	        jmmfSynchronousModeCB.setFont(plainFont);
	        jmmfDebugModeCB.setFont(plainFont);
	        // audio extraction from video
	        JLabel extractionLabel = new JLabel(ElanLocale.getString(
	        		"PreferencesDialog.Media.AudioExtractionFramework"));
	        
	        winNoAudioExtractRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.AudioExtractionNone"));
	        winNoAudioExtractRB.setFont(plainFont);
	        
	        winJMMFAudioExtractRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.AudioExtractionJMMF"));
	        winJMMFAudioExtractRB.setFont(plainFont);
	        
	        if (origWinAudioExtractFramework == null) {
	        	if (origJMMFAudioExtraction) {
	        		winJMMFAudioExtractRB.setSelected(true);
	        	} else {
	        		winNoAudioExtractRB.setSelected(true);
	        	}
	        } else if (origWinAudioExtractFramework.equals(PlayerFactory.NONE)) {
	        	winNoAudioExtractRB.setSelected(true);
	        } else if (origWinAudioExtractFramework.equals(PlayerFactory.JMMF)) {
	        	winJMMFAudioExtractRB.setSelected(true);
	        }
	        
	        ButtonGroup aeGroup = new ButtonGroup();
	        aeGroup.add(winNoAudioExtractRB);
	        aeGroup.add(winJMMFAudioExtractRB);	        
	
	        gbc.insets = globalInset;
	        gbc.gridy = gy++;
	        outerPanel.add(jdsRB, gbc);
	       
	        gbc.gridy = gy++;
	        gbc.insets = singleTabInset;
	        outerPanel.add(jmmfCB, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.insets = doubleTabInset;
	        outerPanel.add(correctAtPauseCB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(jmmfSynchronousModeCB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(jmmfDebugModeCB, gbc);
	       
	        gbc.insets = globalInset;
	        gbc.gridy = gy++;
	        outerPanel.add(jfxRB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(vlcjB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(javaSoundRB, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.insets = catInset;
	        outerPanel.add(extractionLabel, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.insets = globalInset;
	        outerPanel.add(winNoAudioExtractRB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(winJMMFAudioExtractRB, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.gridx = 0;
	        gbc.fill = GridBagConstraints.BOTH;
	        gbc.weighty = 1.0;
	        outerPanel.add(new JPanel(), gbc); // filler
	        
	        if (SystemReporting.isWindows7OrHigher() || SystemReporting.isWindowsVista()) {
	        	jdsRB.addChangeListener(this);
	        	jmmfCB.setEnabled(jdsRB.isSelected());
	        	correctAtPauseCB.setEnabled(jdsRB.isSelected());
	        } else {
	        	jmmfCB.setEnabled(false);//??
	        	jmmfCB.setVisible(false);
	        	correctAtPauseCB.setVisible(false);
	        }
        } else if (SystemReporting.isLinux()) {
        	super.setTitle(ElanLocale.getString("PreferencesDialog.OS.Linux"));
	        // look and feel
	        // add relaunch icon
	        JLabel relaunchLabel = new JLabel();
	        ImageIcon relaunchIcon = null;
	        try {
	            relaunchIcon = new ImageIcon(this.getClass()
	                                             .getResource("/toolbarButtonGraphics/general/Refresh16.gif"));
	            relaunchLabel.setIcon(relaunchIcon);
	        } catch (Exception ex) {
	            relaunchLabel.setText(ElanLocale.getString(
	                    "PreferencesDialog.Relaunch"));
	        }
	
	        relaunchLabel.setToolTipText(ElanLocale.getString(
	                "PreferencesDialog.Relaunch.Tooltip"));
	        
	        JLabel lAndFLabel = new JLabel(ElanLocale.getString("PreferencesDialog.OS.Linux.LFLabel"));
	        crossPlatformLaFRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.OS.Linux.LF.CrossPlatform"), true);
        	systemLAndFRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.OS.Linux.LF.System"));
        	nimbusLAndFRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.OS.Linux.LF.Nimbus"));
	        crossPlatformDarkThemeCB = new JCheckBox(ElanLocale.getString(
	        		"PreferencesDialog.OS.LF.DarkTheme"), origCrossPlatformDark);
	        crossPlatformBoldFontCB = new JCheckBox(ElanLocale.getString(
	        		"PreferencesDialog.OS.LF.CrossPlatform.BoldFont"), origCrossPlatformBold);
        	
        	ButtonGroup lAndFGroup = new ButtonGroup();
        	lAndFGroup.add(crossPlatformLaFRB);
        	lAndFGroup.add(systemLAndFRB);
        	lAndFGroup.add(nimbusLAndFRB);
        	
        	if (LFOption.SYSTEM_LF.name().equals(origLinuxLFPref)) {
        		systemLAndFRB.setSelected(true);
        		crossPlatformDarkThemeCB.setEnabled(false);
        		crossPlatformBoldFontCB.setEnabled(false);
        	} else if (LFOption.NIMBUS_LF.name().equals(origLinuxLFPref)) {
        		nimbusLAndFRB.setSelected(true);
        		crossPlatformDarkThemeCB.setEnabled(false);
        		crossPlatformBoldFontCB.setEnabled(false);
        	}
        	// font 
        	plainFont = crossPlatformLaFRB.getFont().deriveFont(Font.PLAIN);
        	crossPlatformLaFRB.setFont(plainFont);
        	systemLAndFRB.setFont(plainFont);
        	nimbusLAndFRB.setFont(plainFont);
        	crossPlatformDarkThemeCB.setFont(plainFont);
        	crossPlatformBoldFontCB.setFont(plainFont);
        	
        	crossPlatformLaFRB.addChangeListener(this);
	        
	        JLabel frameworkLabel = new JLabel(ElanLocale.getString(
	                    "Player.Framework"));
	        //frameworkLabel.setFont(plainFont);//??
	        vlcjB = new JRadioButton(ElanLocale.getString(
	                    "PreferencesDialog.Media.VLCJ"), true);
	        vlcjB.setFont(plainFont);
	        
	        jfxRB = new JRadioButton(ElanLocale.getString(
	        		"PreferencesDialog.Media.JFX"));
	        jfxRB.setFont(plainFont);
	        javaSoundRB = new JRadioButton(ElanLocale.getString(
	        		"PreferencesDialog.Media.JavaSound"));
	        javaSoundRB.setFont(plainFont);
	        ButtonGroup gr = new ButtonGroup();
	        gr.add(vlcjB);
	        gr.add(jfxRB);
	        gr.add(javaSoundRB);
	        
	        if (origLinuxPrefFramework.equals(PlayerFactory.JMF_MEDIA_FRAMEWORK)) {
	            // leave the VLCJ radio button selected 
	        } else if (origLinuxPrefFramework.equals(PlayerFactory.JFX)) {
	        		jfxRB.setSelected(true);
	        } else if (origLinuxPrefFramework.equals(PlayerFactory.JAVA_SOUND)) {
	        	javaSoundRB.setSelected(true);
	        } else {
	            vlcjB.setSelected(true);
	        }
	        
	        JLabel extractionLabel = new JLabel(ElanLocale.getString(
	        		"PreferencesDialog.Media.AudioExtractionFramework"));
	        // add two more radio buttons
	        linuxNoAudioExtractRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.AudioExtractionNone"));
	        linuxNoAudioExtractRB.setFont(plainFont);

	        linuxFFmpegAudioExtractRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.Media.AudioExtractionFFmpeg"));
	        linuxFFmpegAudioExtractRB.setFont(plainFont);
	        
	        if (origLinuxAudioExtractFramework == null || !origLinuxAudioExtractFramework.equals(PlayerFactory.NONE)) {
	        	linuxFFmpegAudioExtractRB.setSelected(true);
	        } else {
	        	linuxNoAudioExtractRB.setSelected(true);
	        }
	        
	        ButtonGroup aeGroup = new ButtonGroup();
	        aeGroup.add(linuxNoAudioExtractRB);
	        aeGroup.add(linuxFFmpegAudioExtractRB);
	        
	        // add L&F items first
	        gbc.gridx = 0;
	        gbc.gridy = gy++;
	        gbc.weightx = 1.0;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.insets = catInset;
	        outerPanel.add(lAndFLabel, gbc);
	        gbc.gridx = 1;
	        gbc.fill = GridBagConstraints.NONE;
	        gbc.weightx = 0.0;
	        outerPanel.add(relaunchLabel, gbc);
	        
	        gbc.gridx = 0;
	        gbc.gridy = gy++;
	        gbc.gridwidth = 2;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.weightx = 1.0;
	        gbc.insets = globalInset;
	        outerPanel.add(crossPlatformLaFRB, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.insets = singleTabInset;
	        outerPanel.add(crossPlatformDarkThemeCB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(crossPlatformBoldFontCB, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.insets = globalInset;
	        outerPanel.add(systemLAndFRB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(nimbusLAndFRB, gbc);
	        
	        gbc.gridx = 0;
	        gbc.gridy = gy++;
	        gbc.gridwidth = 2;
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.weightx = 1.0;
	        gbc.insets = catInset;
	        outerPanel.add(frameworkLabel, gbc);
	
	        gbc.gridy = gy++;
	        gbc.insets = globalInset;
	        outerPanel.add(vlcjB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(jfxRB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(javaSoundRB, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.insets = catInset;
	        outerPanel.add(extractionLabel, gbc);
	        
	        gbc.gridy = gy++;
	        gbc.insets = globalInset;
	        outerPanel.add(linuxNoAudioExtractRB, gbc);
	        
	        gbc.gridy = gy++;
	        outerPanel.add(linuxFFmpegAudioExtractRB, gbc);
	
	        gbc.gridy = gy++;
	        gbc.gridx = 0;
	        gbc.fill = GridBagConstraints.BOTH;
	        gbc.weighty = 1.0;
	        outerPanel.add(new JPanel(), gbc); // filler

        }
    }

    /**
     * 
     */
    @Override
	public Map<String, Object> getChangedPreferences() {
        if (isChanged()) {
            Map<String, Object> chMap = new HashMap<String, Object>(4);

        	if (SystemReporting.isMacOS()) {
        		if (macScreenBarCB.isSelected() != origMacUseScreenBar) {
                    chMap.put("OS.Mac.useScreenMenuBar",
                    	Boolean.valueOf(macScreenBarCB.isSelected()));
                }
        		String tmpLF = "system";
        		if (crossPlatformLaFRB.isSelected()) {
        			tmpLF = "metal";
        			
        			if (origCrossPlatformDark != crossPlatformDarkThemeCB.isSelected()) {
        				chMap.put("Mac.MetalDarkTheme", crossPlatformDarkThemeCB.isSelected());
        			}
        			if (origCrossPlatformBold != crossPlatformBoldFontCB.isSelected()) {
        				chMap.put("Mac.MetalBoldFont", crossPlatformBoldFontCB.isSelected());
        			}
        		}
        		if (!tmpLF.equals(origMacLaFName)) {
        			chMap.put("Mac.PrefLookAndFeel", tmpLF);
        		}
        		
        		/*
                if (macLAndFCB.isSelected() != origMacLF) {
                    chMap.put("UseMacLF", Boolean.valueOf(macLAndFCB.isSelected()));
                }*/
                
                if (macFileDialogCB.isSelected() != origMacFileDialog) {
                    chMap.put("UseMacFileDialog", Boolean.valueOf(macFileDialogCB.isSelected()));
                }

                String tmp = PlayerFactory.AVFN;
                if (javfRB.isSelected()) {
//                	if (javfJavaCB.isSelected()) {
//                		tmp = PlayerFactory.JAVF;
//                	}
                } else if (jfxRB.isSelected()) {
            		tmp = PlayerFactory.JFX;
            	} else if (javaSoundRB.isSelected()) {
                	tmp = PlayerFactory.JAVA_SOUND;
                } else if (vlcjB.isSelected()) {
                	tmp = PlayerFactory.VLCJ;
                }

                if (!origMacPrefFramework.equals(tmp)) {
                    chMap.put("Mac.PrefMediaFramework", tmp);
                    //apply immediately
                    System.setProperty("PreferredMediaFramework", tmp);
                }
                
                if (javfDebugModeCB.isSelected() != origJAVFDebugMode) {
                	chMap.put("NativePlayer.DebugMode", Boolean.valueOf(javfDebugModeCB.isSelected()));
                }
                
                if (javfNativeStopCB.isSelected() != origJAVFNativeStopping) {
                	chMap.put("JAVFPlayer.UseNativeStopTime", Boolean.valueOf(javfNativeStopCB.isSelected()));
                }
                
                String tmp2 = PlayerFactory.NONE;
                if (macJAVFAudioExtractRB.isSelected()) {
                	tmp2 = PlayerFactory.JAVF; 
                } else if (macFFmpegAudioExtractRB.isSelected()) {
                	tmp2 = PlayerFactory.FFMPEG;
                }
                
                if (origMacAudioExtractFramework == null) {// not yet set before
                	chMap.put("AudioExtractionFramework", tmp2);
                } else if (!origMacAudioExtractFramework.equals(tmp2)){
                	chMap.put("AudioExtractionFramework", tmp2);
                }
                
        	} else if (SystemReporting.isWindows()) {
        		// look and feel
        		String tmpLF = "metal";
        		if (winLaFRB.isSelected()) {
        			tmpLF = "system";
        		}
        		if (!origWinLaFName.equals(tmpLF)) {
        			chMap.put("Windows.PrefLookAndFeel", tmpLF);
        		}
        		if (crossPlatformLaFRB.isSelected()) {
        			if (origCrossPlatformDark != crossPlatformDarkThemeCB.isSelected()) {
        				chMap.put("Windows.MetalDarkTheme", crossPlatformDarkThemeCB.isSelected());
        			}
        			if (origCrossPlatformBold != crossPlatformBoldFontCB.isSelected()) {
        				chMap.put("Windows.MetalBoldFont", crossPlatformBoldFontCB.isSelected());
        			}
        		}
        		// media framework
                String winTmp = PlayerFactory.JDS;

                if (jfxRB.isSelected()) {
                	winTmp = PlayerFactory.JFX;
                } else if (javaSoundRB.isSelected()) {
                	winTmp = PlayerFactory.JAVA_SOUND;
                } else if (vlcjB.isSelected()) {
                	winTmp = PlayerFactory.VLCJ;
                }

                if (!origWinPrefFramework.equals(winTmp)) {
                    chMap.put("Windows.PrefMediaFramework", winTmp);
                    //apply immediately
                    System.setProperty("PreferredMediaFramework", winTmp);
                }

                if (origJMMFEnabled != jmmfCB.isSelected()) {
                	chMap.put("Windows.JMMFEnabled", jmmfCB.isSelected());
                }
                
                if (origCorrectAtPause != correctAtPauseCB.isSelected()) {
                	chMap.put("Windows.JMMFPlayer.CorrectAtPause", correctAtPauseCB.isSelected());
                }
                
                if (origJMMFSynchronousMode != jmmfSynchronousModeCB.isSelected()) {
                	chMap.put("Windows.JMMFPlayer.SynchronousMode", jmmfSynchronousModeCB.isSelected());
                }
                
                if (origJMMFDebugMode != jmmfDebugModeCB.isSelected()) {
                	chMap.put("NativePlayer.DebugMode", jmmfDebugModeCB.isSelected());
                }
                
                String tmp2 = PlayerFactory.NONE;
                if (winJMMFAudioExtractRB.isSelected()) {
                	tmp2 = PlayerFactory.JMMF; 
                }
                
                if (origWinAudioExtractFramework == null) {// not yet set before
                	chMap.put("AudioExtractionFramework", tmp2);
                } else if (!origWinAudioExtractFramework.equals(tmp2)){
                	chMap.put("AudioExtractionFramework", tmp2);
                }
        	} else if (SystemReporting.isLinux()) {
        		String tmpLF = LFOption.CROSS_PLATFORM_LF.name();
        		if (systemLAndFRB.isSelected()) {
        			tmpLF = LFOption.SYSTEM_LF.name();
        		} else if (nimbusLAndFRB.isSelected()) {
        			tmpLF = LFOption.NIMBUS_LF.name();
        		}
        		if (!tmpLF.equals(origLinuxLFPref)) {
        			chMap.put("Linux.PrefLookAndFeel", tmpLF);
        		}
        		
        		if (crossPlatformLaFRB.isSelected()) {
        			
        			if (origCrossPlatformDark != crossPlatformDarkThemeCB.isSelected()) {
        				chMap.put("Linux.MetalDarkTheme", crossPlatformDarkThemeCB.isSelected());
        			}
        			if (origCrossPlatformBold != crossPlatformBoldFontCB.isSelected()) {
        				chMap.put("Linux.MetalBoldFont", crossPlatformBoldFontCB.isSelected());
        			}
        		}
        		
                String tmp = PlayerFactory.VLCJ;

                if (jfxRB.isSelected()) {
                	tmp = PlayerFactory.JFX;
                } else if (javaSoundRB.isSelected()) {
                	tmp = PlayerFactory.JAVA_SOUND;
                }

                if (!origLinuxPrefFramework.equals(tmp)) {
                    chMap.put("Linux.PrefMediaFramework", tmp);
                    //apply immediately
                    System.setProperty("PreferredMediaFramework", tmp);
                }
                
                String tmp2 = PlayerFactory.NONE;
                if (linuxFFmpegAudioExtractRB.isSelected()) {
                	tmp2 = PlayerFactory.FFMPEG;
                }
                
                if (!tmp2.equals(origLinuxAudioExtractFramework)) {
                	chMap.put("AudioExtractionFramework", tmp2);
                }
        	}
        	
            return chMap;
        }

        return null;
    }

    /**
     * 
     */
    @Override
	public boolean isChanged() {
    	if (SystemReporting.isMacOS()) {
    		if ((macScreenBarCB.isSelected() != origMacUseScreenBar) ||
                    (macFileDialogCB.isSelected() != origMacFileDialog)) {
                return true;
            }
    		
    		String tmpLF = "system";
    		if (crossPlatformLaFRB.isSelected()) {
    			tmpLF = "metal";
    		}
    		if (!tmpLF.equals(origMacLaFName)) {
    			return true;
    		}
    		if (crossPlatformLaFRB.isSelected()) {
    			if (origCrossPlatformDark != crossPlatformDarkThemeCB.isSelected()) {
    				return true;
    			}
    			if (origCrossPlatformBold != crossPlatformBoldFontCB.isSelected()) {
    				return true;
    			}
    		}

    		if (origJAVFDebugMode != javfDebugModeCB.isSelected()) {
    			return true;
    		}
    		if (origJAVFNativeStopping != javfNativeStopCB.isSelected()) {
    			return true;
    		}
    		
            String tmp = PlayerFactory.AVFN;
            if (javfRB.isSelected()) {
//            	if (javfJavaCB.isSelected()) {
//            		tmp = PlayerFactory.JAVF;
//            	}
            } else if (jfxRB.isSelected()) {
	           	tmp = PlayerFactory.JFX;
	        } else if (javaSoundRB.isSelected()) {
	           	tmp = PlayerFactory.JAVA_SOUND;
	        } else if (vlcjB.isSelected()) {
	           	tmp = PlayerFactory.VLCJ;
	        }
            
            if (!origMacPrefFramework.equals(tmp)) {
                return true;
            }
            String tmp2 = PlayerFactory.NONE;
            if (macJAVFAudioExtractRB.isSelected()) {
            	tmp2 = PlayerFactory.JAVF; 
            } else if (macFFmpegAudioExtractRB.isSelected()) {
            	tmp2 = PlayerFactory.FFMPEG;
            }
            
            if (!tmp2.equals(origMacAudioExtractFramework)) {
            	return true;
            }
    	} else if (SystemReporting.isWindows()) {
    		// look and feel
    		String tmpLF = "metal";
    		if (winLaFRB.isSelected()) {
    			tmpLF = "system";
    		}
    		if (!tmpLF.equals(origWinLaFName)) {
    			return true;
    		}
    		if (crossPlatformLaFRB.isSelected()) {
    			if (origCrossPlatformDark != crossPlatformDarkThemeCB.isSelected()) {
    				return true;
    			}
    			if (origCrossPlatformBold != crossPlatformBoldFontCB.isSelected()) {
    				return true;
    			}
    		}
    		
    		// player framework
    		String winTmp = PlayerFactory.JDS;
            
            if (jfxRB.isSelected()) {
            	winTmp = PlayerFactory.JFX;
            } else if (javaSoundRB.isSelected()) {
            	winTmp = PlayerFactory.JAVA_SOUND;
            } else if (vlcjB.isSelected()) {
            	winTmp = PlayerFactory.VLCJ;
            }

            if (!origWinPrefFramework.equals(winTmp)) {
                return true;
            }

            if (origJMMFEnabled != jmmfCB.isSelected()) {
            	return true;
            }
            if (origCorrectAtPause != correctAtPauseCB.isSelected()) {
            	return true;
            }
            if (origJMMFSynchronousMode != jmmfSynchronousModeCB.isSelected()) {
            	return true;
            }
            if (origJMMFDebugMode != jmmfDebugModeCB.isSelected()) {
            	return true;
            }
            
            String tmp2 = PlayerFactory.NONE;
            if (winJMMFAudioExtractRB.isSelected()) {
            	tmp2 = PlayerFactory.JMMF; 
            }
            
            if (!tmp2.equals(origWinAudioExtractFramework)) {
            	return true;
            }
    	}  else if (SystemReporting.isLinux()) {
    		String tmpLF = LFOption.CROSS_PLATFORM_LF.name();
    		if (systemLAndFRB.isSelected()) {
    			tmpLF = LFOption.SYSTEM_LF.name();
    		} else if (nimbusLAndFRB.isSelected()) {
    			tmpLF = LFOption.NIMBUS_LF.name();
    		}
    		if (!tmpLF.equals(origLinuxLFPref)) {
    			return true;
    		}
    		
    		if (crossPlatformLaFRB.isSelected()) {
    			if (origCrossPlatformDark != crossPlatformDarkThemeCB.isSelected()) {
    				return true;
    			}
    			if (origCrossPlatformBold != crossPlatformBoldFontCB.isSelected()) {
    				return true;
    			}
    		}
    		
            String tmp = PlayerFactory.VLCJ;

            if (jfxRB.isSelected()) {
            	tmp = PlayerFactory.JFX;
            } else if (javaSoundRB.isSelected()) {
            	tmp = PlayerFactory.JAVA_SOUND;
            }

            if (!origLinuxPrefFramework.equals(tmp)) {
                return true;
            } 
            
            String tmp2 = PlayerFactory.NONE;
            if (linuxFFmpegAudioExtractRB.isSelected()) {
            	tmp2 = PlayerFactory.FFMPEG;
            }
            
            if (!tmp2.equals(origLinuxAudioExtractFramework)) {
            	return true;
            }
    	}
        
        return false;
    }

	@Override
	public void stateChanged(ChangeEvent ce) {
		if (SystemReporting.isWindows()) {
			if (ce.getSource() == winLaFRB || ce.getSource() == crossPlatformLaFRB) {
				crossPlatformDarkThemeCB.setEnabled(crossPlatformLaFRB.isSelected());
				crossPlatformBoldFontCB.setEnabled(crossPlatformLaFRB.isSelected());
			} else {
				jmmfCB.setEnabled(jdsRB.isSelected());
				correctAtPauseCB.setEnabled(jdsRB.isSelected());
				jmmfSynchronousModeCB.setEnabled(jdsRB.isSelected());
				jmmfDebugModeCB.setEnabled(jdsRB.isSelected());
			}
		} else if (SystemReporting.isMacOS()) {
			if (ce.getSource() == macScreenBarCB) {
				enableMacLaFButtons(!macScreenBarCB.isSelected());
			}
			else if (ce.getSource() == macLaFRB || ce.getSource() == crossPlatformLaFRB) {
				crossPlatformDarkThemeCB.setEnabled(crossPlatformLaFRB.isSelected());
				crossPlatformBoldFontCB.setEnabled(crossPlatformLaFRB.isSelected());
			} else {
				javfJavaCB.setEnabled(javfRB.isSelected());
				javfDebugModeCB.setEnabled(javfRB.isSelected());
				javfNativeStopCB.setEnabled(javfRB.isSelected());
			}
		} else if (SystemReporting.isLinux()) {
			if (ce.getSource() == systemLAndFRB || ce.getSource() == nimbusLAndFRB 
					|| ce.getSource() == crossPlatformLaFRB) {
				crossPlatformDarkThemeCB.setEnabled(crossPlatformLaFRB.isSelected());
				crossPlatformBoldFontCB.setEnabled(crossPlatformLaFRB.isSelected());
			}
		}
	}
	
	private void enableMacLaFButtons(boolean enable) {
		macLaFRB.setEnabled(enable);
		crossPlatformLaFRB.setEnabled(enable);
		crossPlatformDarkThemeCB.setEnabled(crossPlatformLaFRB.isEnabled() && 
				crossPlatformLaFRB.isSelected());
		crossPlatformBoldFontCB.setEnabled(crossPlatformLaFRB.isEnabled() && 
				crossPlatformLaFRB.isSelected());
		
	}
}
