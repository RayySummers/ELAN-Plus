package mpi.eudico.client.annotator.prefs.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mpi.eudico.client.annotator.CachedDataManager;
import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;
import nl.mpi.util.FileUtility;


/**
 * A panel for changing settings concerning preferences files and
 * general preferences.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class GeneralPrefsPanel extends AbstractEditPrefsPanel implements PreferenceEditor,
    ActionListener {
    private String curGenPrefsLocation = "-";
    private JLabel setDirLabel;
    private JLabel curDirLabel;
    private JButton defaultDirButton;
    private JButton resetDirButton;
    
    // Cache directory 
    private String curCacheDirectory = "-";
    private JLabel cacheDirLabel;
    private JLabel curCacheDirLabel;
    private JButton cacheDirButton;
    private JButton resetCacheDirButton;
    
    // General search path for non-A/V linked files 
    private String curGenLinkedFilesLocation = "-";
    private JLabel genLinkedFilesLocLabel;
    private JLabel curGenLinkedFilesLocLabel;
    private JButton genLinkedFilesLocButton;
    private JButton resetLinkedFilesLocButton;
    
    private JCheckBox tierSetCB;
    private String curTSPath = "-";
    private JLabel setTSFileLabel;
    private JLabel curTSFileLabel;
    private JButton defaultTSFileButton;
    private JButton resetTSFileButton;
    
    private JCheckBox checkForUpdatesCB;
    private JCheckBox saveAsOldFormatCB;
    private JCheckBox createLockFilesCB;
    private boolean origCheckUpdates = true;    
    private boolean origSaveAsOldFormatFlag = false;
    private boolean origCreateLockFilesFlag = false;
    private boolean oriWorkWithTierSetFlag = false;

    /**
     * Constructor. 
     * Reads the current preferences and creates the UI.
     */
    public GeneralPrefsPanel() {
        super(ElanLocale.getString("PreferencesDialog.Category.Preferences"));
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
        String stringPref = Preferences.getString("DefaultPreferencesLocation", null);

        if (stringPref != null) {
            curGenPrefsLocation = stringPref;
        }
        
        stringPref = Preferences.getString("DefaultTierSetFilePath", null);
        if (stringPref != null) {
        	curTSPath = stringPref;
        }
        
        Boolean boolPref1 = Preferences.getBool("WorkwithTierSets", null);
        if (boolPref1 != null) {
        	oriWorkWithTierSetFlag = boolPref1.booleanValue();
        }
        
        Boolean boolPref = Preferences.getBool("AutomaticUpdate", null);
        
        if (boolPref != null) {
        	origCheckUpdates = boolPref;
		}
        
        boolPref = Preferences.getBool("SaveAsOldEAF2_7", null);
        
        if (boolPref != null) {
        	origSaveAsOldFormatFlag = boolPref.booleanValue();
        }
        
        boolPref = Preferences.getBool("CreateLockFiles", null);
        
        if (boolPref != null) {
        	origCreateLockFilesFlag = boolPref.booleanValue();
        }
        
        stringPref = Preferences.getString("CacheLocation", null);
        if (stringPref != null) {
        	curCacheDirectory = stringPref;
        }
        
        stringPref = Preferences.getString("DefaultGeneralLinkedFilesLocation", null);
        if (stringPref != null) {
        	curGenLinkedFilesLocation = stringPref;
        }
    }

    private void initComponents() {
    	
    	Font plainFont;
    	// create panel for set directory
        setDirLabel = new JLabel(ElanLocale.getString(
                "PreferencesDialog.Prefs.DefaultLoc"));
        plainFont = setDirLabel.getFont().deriveFont(Font.PLAIN);
        setDirLabel.setFont(plainFont);        
        
        JPanel dirPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 0;        
        gbc.insets = topInset;      
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;        
        dirPanel.add(setDirLabel, gbc);
        
        curDirLabel = new JLabel(curGenPrefsLocation);
        curDirLabel.setFont(plainFont.deriveFont(plainFont.getSize2D() * 
        		Constants.SMALLFONT_SCALE_FACTOR));
        //curDirLabel.setForeground(Constants.ACTIVEANNOTATIONCOLOR);
        gbc.gridy = 1;
        dirPanel.add(curDirLabel, gbc);
        
        defaultDirButton = new JButton(ElanLocale.getString("Button.Browse"));
        gbc.gridy = 0;
        gbc.gridx = 1; 
        gbc.gridheight = 2;
        gbc.insets = leftInset;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        dirPanel.add(defaultDirButton, gbc);
        defaultDirButton.addActionListener(this);
        
        resetDirButton = new JButton();
        ImageIcon resetIcon = null;
        // add reset icon
        try {
            resetIcon = new ImageIcon(this.getClass()
                                          .getResource("/mpi/eudico/client/annotator/resources/Remove.gif"));
            resetDirButton.setIcon(resetIcon);
        } catch (Exception ex) {
            resetDirButton.setText("X");
        }

        resetDirButton.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Reset"));
        resetDirButton.setPreferredSize(new Dimension(
                resetDirButton.getPreferredSize().width,
                defaultDirButton.getPreferredSize().height));        
        gbc.gridx = 2;
        dirPanel.add(resetDirButton, gbc);       
        resetDirButton.addActionListener(this);
        
        // create panel for cache directory
        cacheDirLabel = new JLabel(ElanLocale.getString(
                "PreferencesDialog.Prefs.CacheDirectory"));
        cacheDirLabel.setFont(plainFont);
        JPanel cacheDirPanel = new JPanel(new GridBagLayout());
        GridBagConstraints cachePanelGbc = new GridBagConstraints();
        cachePanelGbc.anchor = GridBagConstraints.NORTHWEST;
        cachePanelGbc.gridx = 0;
        cachePanelGbc.gridy = 0;        
        cachePanelGbc.insets = topInset;      
        cachePanelGbc.fill = GridBagConstraints.HORIZONTAL;
        cachePanelGbc.weightx = 1.0;        
        cacheDirPanel.add(cacheDirLabel, cachePanelGbc);
        
        curCacheDirLabel = new JLabel(curCacheDirectory);
        curCacheDirLabel.setFont(plainFont.deriveFont(plainFont.getSize2D() * 
        		Constants.SMALLFONT_SCALE_FACTOR));
        //curCacheDirLabel.setForeground(Constants.ACTIVEANNOTATIONCOLOR);
        cachePanelGbc.gridy = 1;
        cacheDirPanel.add(curCacheDirLabel, cachePanelGbc);
        
        cacheDirButton = new JButton(ElanLocale.getString("Button.Browse"));
        cachePanelGbc.gridy = 0;
        cachePanelGbc.gridx = 1; 
        cachePanelGbc.gridheight = 2;
        cachePanelGbc.insets = leftInset;
        cachePanelGbc.fill = GridBagConstraints.NONE;
        cachePanelGbc.weightx = 0.0;
        cacheDirPanel.add(cacheDirButton, cachePanelGbc);
        cacheDirButton.addActionListener(this);
        
        resetCacheDirButton = new JButton();
        if(resetIcon != null) {
        	resetCacheDirButton.setIcon(resetIcon);
        } else {
        	resetCacheDirButton.setText("X");
        }
        resetCacheDirButton.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Reset"));
        resetCacheDirButton.setPreferredSize(new Dimension(
        		resetCacheDirButton.getPreferredSize().width,
                cacheDirButton.getPreferredSize().height));        
        cachePanelGbc.gridx = 2;
        cacheDirPanel.add(resetCacheDirButton, cachePanelGbc);       
        resetCacheDirButton.addActionListener(this);
        
        // create panel for general linked files search location
        JPanel genLinkedFilesLocPanel = new JPanel(new GridBagLayout());
        genLinkedFilesLocLabel = new JLabel(ElanLocale.getString("PreferencesDialog.Prefs.OtherLFLocation"));
        genLinkedFilesLocLabel.setFont(plainFont);
        curGenLinkedFilesLocLabel = new JLabel(curGenLinkedFilesLocation);
        curGenLinkedFilesLocLabel.setFont(plainFont.deriveFont(plainFont.getSize2D() * 
        		Constants.SMALLFONT_SCALE_FACTOR));
        genLinkedFilesLocButton = new JButton(ElanLocale.getString("Button.Browse"));
        resetLinkedFilesLocButton = new JButton(resetIcon);
        resetLinkedFilesLocButton.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Reset"));
        resetLinkedFilesLocButton.setPreferredSize(new Dimension(
        		resetLinkedFilesLocButton.getPreferredSize().width,
                genLinkedFilesLocButton.getPreferredSize().height));
        
        GridBagConstraints lfGbc = new GridBagConstraints();
        lfGbc.anchor = GridBagConstraints.NORTHWEST;
        lfGbc.gridx = 0;
        lfGbc.gridy = 0;        
        lfGbc.insets = topInset;      
        lfGbc.fill = GridBagConstraints.HORIZONTAL;
        lfGbc.weightx = 1.0;        
        genLinkedFilesLocPanel.add(genLinkedFilesLocLabel, lfGbc);
        
        lfGbc.gridy = 1;
        genLinkedFilesLocPanel.add(curGenLinkedFilesLocLabel, lfGbc);
        lfGbc.gridy = 0;
        lfGbc.gridx = 1;
        lfGbc.gridheight = 2;
        lfGbc.insets = leftInset;
        lfGbc.fill = GridBagConstraints.NONE;
        lfGbc.weightx = 0.0;
        genLinkedFilesLocPanel.add(genLinkedFilesLocButton, lfGbc);
        genLinkedFilesLocButton.addActionListener(this);
        
        lfGbc.gridx = 2;
        genLinkedFilesLocPanel.add(resetLinkedFilesLocButton, lfGbc);
        resetLinkedFilesLocButton.addActionListener(this);
        
        // create panel for set tier set        
        tierSetCB = new JCheckBox(ElanLocale.getString(
				"PreferencesDialog.Edit.TierSet"),oriWorkWithTierSetFlag);
        
        tierSetCB.setFont(plainFont);
        
        setTSFileLabel = new JLabel(ElanLocale.getString(
                "PreferencesDialog.Prefs.DefaultTSFilePath"));
        setTSFileLabel.setFont(plainFont);   
        
        curTSFileLabel = new JLabel(curTSPath);
        curTSFileLabel.setFont(plainFont.deriveFont(plainFont.getSize2D() * 
        		Constants.SMALLFONT_SCALE_FACTOR));
        
        defaultTSFileButton = new JButton(ElanLocale.getString("Button.Browse"));
        defaultTSFileButton.addActionListener(this);
        
        resetTSFileButton = new JButton();
        resetTSFileButton.addActionListener(this);
        if(resetIcon != null){
        	resetTSFileButton.setIcon(resetIcon);
        } else {
        	resetTSFileButton.setText("X");
        }
        
        resetTSFileButton.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Reset"));
        resetTSFileButton.setPreferredSize(resetDirButton.getPreferredSize());       
        
        JPanel tsFilePathPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 0;        
        gbc.insets = topInset;      
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; 
        tsFilePathPanel.add(tierSetCB, gbc);   

        gbc.gridy = 1;
        //gbc.insets = globalInset;
        tsFilePathPanel.add(setTSFileLabel, gbc);
        
        gbc.gridy = 2;
        tsFilePathPanel.add(curTSFileLabel, gbc);
        
        gbc.gridy = 1;
        gbc.gridx = 1; 
        gbc.gridheight = 2;
        gbc.insets = leftInset;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        tsFilePathPanel.add(defaultTSFileButton, gbc);  
        
        gbc.gridx = 2;
        tsFilePathPanel.add(resetTSFileButton, gbc);  
        
    	// main preferences panel 
    	
    	int gy = 0;
    	
    	gbc = new GridBagConstraints();
    	gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = gy++;
        gbc.insets = globalInset;        
        checkForUpdatesCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.Prefs.AutoUpdate"), origCheckUpdates);
        checkForUpdatesCB.setFont(plainFont);
        outerPanel.add(checkForUpdatesCB, gbc);
        
        saveAsOldFormatCB = new JCheckBox(ElanLocale.getString(
                "PreferencesDialog.Prefs.SaveAsOld"), origSaveAsOldFormatFlag);
        saveAsOldFormatCB.setFont(plainFont);
        gbc.gridy = gy++;
        gbc.insets = globalInset;
        outerPanel.add(saveAsOldFormatCB, gbc);
        
        createLockFilesCB = new JCheckBox(ElanLocale.getString(
        		"PreferencesDialog.Prefs.CreateLockFiles"), origCreateLockFilesFlag);
        createLockFilesCB.setFont(plainFont);
        gbc.gridy = gy++;
        outerPanel.add(createLockFilesCB, gbc);

        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Prefs.Location")),
            gbc);

        gbc.gridy = gy++;
        gbc.insets = catPanelInset;
        outerPanel.add(dirPanel, gbc);   
        
        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Prefs.OtherLFLocationHead")),
            gbc);

        gbc.gridy = gy++;
        gbc.insets = catPanelInset;
        outerPanel.add(genLinkedFilesLocPanel, gbc);
        
        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("MultiTierControlPanel.Menu.TierSet")),
            gbc);

        gbc.gridy = gy++;
        gbc.insets = catPanelInset;
        outerPanel.add(tsFilePathPanel, gbc);   
        
        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.Prefs.CacheLocation")), gbc);
        
        gbc.gridy = gy++;
        gbc.insets = catPanelInset;
        outerPanel.add(cacheDirPanel, gbc);
        
        gbc.gridy = gy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        outerPanel.add(new JPanel(), gbc); // filler
    }

    /**
     * Returns the changed preferences.
     * Calls {@link #isChanged()} first.
     *
     * @return a map with changed preferences, key-value pairs, 
     * or {@code null} if nothing changed
     */
    @Override
	public Map<String, Object> getChangedPreferences() {
        if (isChanged()) {
            Map<String, Object> chMap = new HashMap<String, Object>(2);

            if (curDirLabel.getText() != null) {
            	if (!curDirLabel.getText().equals(curGenPrefsLocation)) {
            		if (curDirLabel.getText().equals("-")) {
            			chMap.put("DefaultPreferencesLocation", null);
            		} else {
            			chMap.put("DefaultPreferencesLocation", curDirLabel.getText());
            		}
            	}              
            } else {
            	chMap.put("DefaultPreferencesLocation", null);
            }          
            
            if (curCacheDirLabel.getText() != null) {
            	if (!curCacheDirLabel.getText().equals(curCacheDirectory)) {          	
	        		if(curCacheDirLabel.getText().equals("-")) {
	        			chMap.put("CacheLocation", null);
	        		} else {
	        			chMap.put("CacheLocation", curCacheDirLabel.getText());
	        		}
            	}
            } else {
            	chMap.put("CacheLocation", null);
            }
            
            if (curTSFileLabel.getText() != null) {
            	if (!curTSFileLabel.getText().equals(curTSPath)) {
            		if (curTSFileLabel.getText().equals("-")) {
            			chMap.put("DefaultTierSetFilePath", null);
            		} else {
            			chMap.put("DefaultTierSetFilePath", curTSFileLabel.getText());
            		}
            	}  
            } else {
            	chMap.put("DefaultTierSetFilePath", null);
            }
            
            if (tierSetCB.isSelected() != oriWorkWithTierSetFlag) {
            	chMap.put("WorkwithTierSets", 
            		Boolean.valueOf(tierSetCB.isSelected()));
            }
            
            if (origCheckUpdates != checkForUpdatesCB.isSelected()) {
            	chMap.put("AutomaticUpdate", checkForUpdatesCB.isSelected());
            }

            if (saveAsOldFormatCB.isSelected() != origSaveAsOldFormatFlag) {
            	chMap.put("SaveAsOldEAF2_7", 
            		Boolean.valueOf(saveAsOldFormatCB.isSelected()));
            }
            
            if (origCreateLockFilesFlag != createLockFilesCB.isSelected()) {
            	chMap.put("CreateLockFiles", Boolean.valueOf(createLockFilesCB.isSelected()));
            }
            
            if (curGenLinkedFilesLocLabel.getText() != null) {// superfluous
            	if (!curGenLinkedFilesLocLabel.getText().equals(curGenLinkedFilesLocation)) {
	            	if (curGenLinkedFilesLocLabel.getText().equals("-")) {
	            		chMap.put("DefaultGeneralLinkedFilesLocation", null);
	            	} else {
	            		chMap.put("DefaultGeneralLinkedFilesLocation", curGenLinkedFilesLocLabel.getText());
	            	}
            	}
            } else {
            	chMap.put("DefaultGeneralLinkedFilesLocation", null);
            }
            
            return chMap;
        }

        return null;
    }

    /**
     * Returns whether or not anything changed.
     *
     * @return {@code true} if anything changed, {@code false} otherwise
     */
    @Override
	public boolean isChanged() {
        return !curGenPrefsLocation.equals(curDirLabel.getText()) ||
        		!curTSPath.equals(curTSFileLabel.getText()) ||
        		origCheckUpdates != checkForUpdatesCB.isSelected() ||
        		tierSetCB.isSelected() != oriWorkWithTierSetFlag ||
        		origSaveAsOldFormatFlag != saveAsOldFormatCB.isSelected() ||
        		!curCacheDirectory.equals(curCacheDirLabel.getText()) ||
        		origCreateLockFilesFlag != createLockFilesCB.isSelected() ||
        		!curGenLinkedFilesLocation.equals(curGenLinkedFilesLocLabel.getText());
    }

    /**
     * Action event handling.
     *
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == defaultDirButton) {
            File selFile = showDirectoryChooser(ElanLocale.getString("PreferencesDialog.Media.DefaultLoc"), curGenPrefsLocation);
            if (selFile != null) {
                curDirLabel.setText(selFile.getAbsolutePath());
                curDirLabel.setText(FileUtility.pathToURLString(
                        selFile.getAbsolutePath()));
            }
        } else if (e.getSource() == resetDirButton) {
        	curDirLabel.setText("-");
        } else if (e.getSource() == cacheDirButton) {
        	File selFile = showDirectoryChooser(ElanLocale.getString("PreferencesDialog.Prefs.CacheDirectory"), curCacheDirectory);
            if (selFile != null && selFile.isDirectory()) {
                if (!CachedDataManager.containsCacheSubdirs(selFile)) {
					curCacheDirLabel.setText(selFile.getAbsolutePath());
					curCacheDirLabel.setText(FileUtility.pathToURLString(selFile.getAbsolutePath()));
				} else {
					JOptionPane.showMessageDialog(this,
							ElanLocale.getString("PreferencesDialog.Prefs.NewCacheDirContainsSubdirs"),
							ElanLocale.getString("Message.Error"),
						    JOptionPane.ERROR_MESSAGE);
				}
            }
        } else if (e.getSource() == resetCacheDirButton) {
        	curCacheDirLabel.setText("-");
        } else if (e.getSource() == resetTSFileButton) {
        	curTSFileLabel.setText("-");
        } else if (e.getSource() == defaultTSFileButton) {
            // show a folder file chooser, set the current def tier set file path
            FileChooser chooser = new FileChooser(this);

            File startDir = new File(System.getProperty("user.home"));
            String selectedFile = null;
            if (curTSPath.length() > 1) {
            	 File file = new File(FileUtility.urlToAbsPath(curTSPath));
                 if (file.exists() && file.isFile()) {
                	 selectedFile = file.getAbsolutePath();
                 }
            	
                File dir = new File(FileUtility.urlToAbsPath(
                		FileUtility.directoryFromPath(curTSPath)));
                if (dir.exists() && dir.isDirectory()) {
                    startDir = dir;
                }
            }

            chooser.setCurrentDirectory(startDir.getAbsolutePath());            
            chooser.createAndShowFileDialog(ElanLocale.getString("PreferencesDialog.Prefs.TierSetFilePath"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
            		null, null, true, null, FileChooser.FILES_ONLY, selectedFile);
            File selFile = chooser.getSelectedFile();
            if (selFile != null) {
                curTSFileLabel.setText(selFile.getAbsolutePath());
                curTSFileLabel.setText(FileUtility.pathToURLString(
                        selFile.getAbsolutePath()));
            }
        } else if (e.getSource() == genLinkedFilesLocButton) {
        	// show file chooser for general linked files folder
            File selFile = showDirectoryChooser(ElanLocale.getString("PreferencesDialog.Prefs.OtherLFLocationHead"), curGenLinkedFilesLocation);
            if (selFile != null) {
            	curGenLinkedFilesLocLabel.setText(selFile.getAbsolutePath());
            	curGenLinkedFilesLocLabel.setText(FileUtility.pathToURLString(
                        selFile.getAbsolutePath()));
            }
        } else if (e.getSource() == resetLinkedFilesLocButton) {
        	curGenLinkedFilesLocLabel.setText("-");
        }
    }
    
    private File showDirectoryChooser(String title, String currentDirectory) {
    	// show a folder file chooser, set the current def. location
        FileChooser chooser = new FileChooser(this);

        File startDir = new File(System.getProperty("user.home"));
        if (currentDirectory.length() > 1) {
            File dir = new File(FileUtility.urlToAbsPath(
            		currentDirectory));

            if (dir.exists() && dir.isDirectory()) {
                startDir = dir;
            }
        }

        chooser.setCurrentDirectory(startDir.getAbsolutePath());            
        chooser.createAndShowFileDialog(title, FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
        		null, null, true, null, FileChooser.DIRECTORIES_ONLY, null);
        return chooser.getSelectedFile();
    }
}
