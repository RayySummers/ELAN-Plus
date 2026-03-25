package mpi.eudico.client.annotator.prefs.gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.JFontChooser;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;


/**
 * A panel for changing UI related settings.
 * 
 * @author Mark Blokpoel
  */
@SuppressWarnings("serial")
public class UIPrefsPanel extends AbstractEditPrefsPanel implements PreferenceEditor {
    private JComboBox<Integer> nrOfRecentItemsCBox;
    private Integer origNrRecentItems = 5;
    private JCheckBox tooltipCB;
    private boolean origToolTipEnabled = true;
    private JCheckBox showAnnotationCountCB;
    private boolean origShowAnnotationCount = false; 
    private JRadioButton useBufferedPaintingRB;
    private JRadioButton useDirectPaintingRB;
    /**
     * 03-2013 user interface for buffered painting setting that used to be only a 
     * command line parameter. Default is now false.
     */
    private boolean origUseBufferedPainting = false;// default false
    private JSlider fontScaleSlider;
    //private float origFontScale = 1.0f; // CC
    private int origFontScaleInt = 100;
    private float minFontScale = 0.5f;
    private float maxFontScale = 2.0f;
    // default preferred tier font and size
    private JTextField defTierFontField;
    private String origDefTierFontName = "";
    private JButton fontSelectButton;
    private JButton fontResetButton;
    private JTextField defTierFontSizeField;
    private int origDefTierFontSize = 0;
    private int modDefTierFontSize = 0;
    private JButton fontSizeResetButton;
    private Font curTierFont = null;
    
    /**
     * Creates a new PlatformPanel instance.
     */
    public UIPrefsPanel() {
        super(ElanLocale.getString("PreferencesDialog.Category.UI"));
        readPrefs();
        initComponents();
    }

    private void readPrefs() {
        Integer intPref = Preferences.getInt("UI.RecentItems", null);

        if (intPref != null) {
            origNrRecentItems = intPref;
        }
        
        Boolean boolPref = Preferences.getBool("UI.ToolTips.Enabled", null);
        
        if (boolPref != null) {
        	origToolTipEnabled = boolPref;
        }
        
        boolPref = Preferences.getBool("UI.MenuItems.ShowAnnotationCount", null);
        if (boolPref != null) {
    		origShowAnnotationCount = boolPref;
        }
    	
    	boolPref = Preferences.getBool("UI.UseBufferedPainting", null);
        if (boolPref != null) {
    		origUseBufferedPainting = boolPref;
    	}
        
        Float scalePref = Preferences.getFloat("UI.FontScaleFactor", null);
        if (scalePref != null) {
        	float origFontScale = scalePref.floatValue();
        	if (origFontScale < minFontScale) {
        		origFontScale = minFontScale;
        	} else if (origFontScale > maxFontScale) {
        		origFontScale = maxFontScale;
        	}
        	origFontScaleInt = (int) (100 * origFontScale);
        }
        // corresponds to property ELAN.Tiers.DefaultFontName
        String stringPref = Preferences.getString("Tiers.DefaultFontName", null);
        if (stringPref != null) {
        	origDefTierFontName = stringPref;
        }
        
        intPref = Preferences.getInt("Tiers.DefaultFontSize", null);
        if (intPref != null) {
        	origDefTierFontSize = intPref.intValue();
        }
    }

    private void initComponents() {  
    	GridBagConstraints gbc;
    	Font plainFont;

    	//recent items panel
    	Integer[] nrOfRecentItemsList = { 5, 10, 15, 20, 25, 30 };
        nrOfRecentItemsCBox = new JComboBox<Integer>(nrOfRecentItemsList);
        nrOfRecentItemsCBox.setSelectedItem(origNrRecentItems);
        plainFont = nrOfRecentItemsCBox.getFont().deriveFont(Font.PLAIN);
        nrOfRecentItemsCBox.setFont(plainFont);
        nrOfRecentItemsCBox.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Relaunch.Tooltip"));
        
        JLabel recentItemsLabel = new JLabel(ElanLocale.getString("PreferencesDialog.UI.RecentItems"));
        recentItemsLabel.setFont(plainFont);

        JPanel recentItemsPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;        
        gbc.insets = topInset;
        recentItemsPanel.add(recentItemsLabel,gbc);

        gbc.gridx = 1;
        gbc.insets = leftInset;
        recentItemsPanel.add(nrOfRecentItemsCBox, gbc);
    	
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;  
        gbc.weightx = 1.0;
        recentItemsPanel.add(new JPanel(), gbc); // filler
        
        // main panel    	
    	int gy=0;
    	       
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.insets = catInset;
        gbc.gridy = gy++;
        outerPanel.add(recentItemsPanel, gbc);
        
        
        JLabel uiFontScaleLabel = new JLabel(ElanLocale.getString("PreferencesDialog.UI.FontScaling"));
        fontScaleSlider = new JSlider(SwingConstants.HORIZONTAL, (int)(100 * minFontScale), 
        		(int)(100 * maxFontScale), origFontScaleInt);
        fontScaleSlider.setMajorTickSpacing(25);
        fontScaleSlider.setPaintLabels(true);
        fontScaleSlider.setPaintTicks(true);
		fontScaleSlider.setToolTipText(String.valueOf(fontScaleSlider.getValue())+ "%");
 
        JPanel fontScalePanel = new JPanel(new GridBagLayout());
        GridBagConstraints fbc = new GridBagConstraints();
        fbc.anchor = GridBagConstraints.NORTHWEST;        
        fbc.insets = topInset;
        fbc.fill = GridBagConstraints.HORIZONTAL;
        fbc.weightx = 0.1;
        fbc.gridwidth = 1;
        fbc.gridheight = 1;
        fontScalePanel.add(uiFontScaleLabel, fbc);
        fbc.gridy = 1;
        fbc.insets = globalInset;
        fontScalePanel.add(fontScaleSlider, fbc);
        
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

        fbc.gridx = 1;
        fbc.gridy = 0;
        fbc.gridheight = 2;
        fbc.fill = GridBagConstraints.NONE;
        fbc.anchor = GridBagConstraints.CENTER;      
        fbc.weightx = 0.0;
        fontScalePanel.add(relaunchLabel, fbc);
        
        gbc.gridy = gy++;
        outerPanel.add(fontScalePanel, gbc);
        
        // preferred tier font panel
        JPanel tierFontPanel = new JPanel(new GridBagLayout());
        defTierFontField = new JTextField(origDefTierFontName);
        if (origDefTierFontName.isEmpty()) {
        	defTierFontField.setText(ElanLocale.getString("Button.Default"));
        	defTierFontField.setForeground(Constants.SHAREDCOLOR3);
        }
        defTierFontField.setEditable(false);
        fontSelectButton = new JButton(ElanLocale.getString("Button.Browse"));
        fontResetButton = new JButton();
        fontResetButton.setToolTipText(ElanLocale.getString("Button.Default"));
        defTierFontSizeField = new JTextField(String.valueOf(origDefTierFontSize));
        if (origDefTierFontSize == 0) {
        	defTierFontSizeField.setText("");
        	defTierFontSizeField.setForeground(Constants.SHAREDCOLOR3);
        }
        fontSizeResetButton = new JButton();
        fontSizeResetButton.setToolTipText(ElanLocale.getString("Button.Default"));
        // add reset icon
        try {
        	ImageIcon resetIcon = new ImageIcon(this.getClass()
                                          .getResource("/mpi/eudico/client/annotator/resources/Remove.gif"));
        	fontResetButton.setIcon(resetIcon);
        	fontSizeResetButton.setIcon(resetIcon);
        } catch (Exception ex) {
        	fontResetButton.setText("X");
        	fontSizeResetButton.setText("X");
        }
        JLabel relaunch2Label = new JLabel(relaunchIcon);
        relaunch2Label.setToolTipText(ElanLocale.getString(
                "PreferencesDialog.Relaunch.Tooltip"));
        
        fbc = new GridBagConstraints();
        fbc.anchor = GridBagConstraints.NORTHWEST;        
        fbc.insets = topInset;
        fbc.fill = GridBagConstraints.HORIZONTAL;
        fbc.weightx = 1;
        fbc.gridwidth = 5;
        fbc.gridheight = 1;
        tierFontPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.UI.DefaultContentFont")), fbc);
        
        fbc.gridy = 1;
        fbc.gridwidth = 1;
        fbc.fill = GridBagConstraints.NONE;
        fbc.weightx = 0;
        fbc.insets = globalPanelInset;
        JLabel fontLabel = new JLabel(ElanLocale.getString("DisplaySettingsPane.Label.Font"));
        fontLabel.setFont(plainFont);
        tierFontPanel.add(fontLabel, fbc);
        
        fbc.gridx = 1;
        fbc.fill = GridBagConstraints.HORIZONTAL;
        fbc.weightx = 0.1;
        fbc.insets = globalInset;
        tierFontPanel.add(defTierFontField, fbc);
        
        fbc.gridx = 2;
        fbc.fill = GridBagConstraints.NONE;
        fbc.weightx = 0;
        tierFontPanel.add(fontSelectButton, fbc);
        
        fbc.gridx = 3;
        tierFontPanel.add(fontResetButton, fbc);
        // next row
        fbc.gridx = 0;
        fbc.gridy = 2;
        fbc.insets = globalPanelInset;
        JLabel fontSizeLabel = new JLabel(ElanLocale.getString("DisplaySettingsPane.Label.FontSize"));
        fontSizeLabel.setFont(plainFont);
        tierFontPanel.add(fontSizeLabel, fbc);
        
        fbc.gridx = 1;
        defTierFontSizeField.setColumns(10);
        fbc.insets = globalInset;
        tierFontPanel.add(defTierFontSizeField, fbc);
        
        fbc.gridx = 3;
        fbc.fill = GridBagConstraints.NONE;
        fbc.weightx = 0.0;
        tierFontPanel.add(fontSizeResetButton, fbc);
        
        fbc.gridx = 4;
        fbc.gridy = 1;
        fbc.gridheight = 2;
        tierFontPanel.add(relaunch2Label, fbc);
        
        gbc.gridy = gy++;
        outerPanel.add(tierFontPanel, gbc);
        ActListener acl = new ActListener();
        fontSelectButton.addActionListener(acl);
        fontResetButton.addActionListener(acl);
        fontSizeResetButton.addActionListener(acl);
//        defTierFontSizeField.addActionListener(acl);
        defTierFontSizeField.addCaretListener(acl);
        fontScaleSlider.addChangeListener(acl);
        
        // UI tooltip
        gbc.gridy = gy++;
        gbc.insets = catInset;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.UI.ToolTip")), gbc);
        
        tooltipCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.UI.ToolTip.Enabled"));
        tooltipCB.setSelected(origToolTipEnabled);
        tooltipCB.setFont(tooltipCB.getFont().deriveFont(Font.PLAIN));
       
        gbc.gridy = gy++;
        gbc.insets = globalInset;
        outerPanel.add(tooltipCB, gbc);
        
        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.UI.MenuOptions")), gbc);
        
        showAnnotationCountCB = new JCheckBox(ElanLocale.getString("PreferencesDialog.UI.MenuOptions.ShowAnnotationCount"));
        showAnnotationCountCB.setSelected(origShowAnnotationCount);
        showAnnotationCountCB.setFont(showAnnotationCountCB.getFont().deriveFont(Font.PLAIN));
        gbc.gridy = gy++;
        gbc.insets = globalInset;
        outerPanel.add(showAnnotationCountCB, gbc);
        
        // hier add label?
        gbc.gridy = gy++;
        gbc.insets = catInset;
        outerPanel.add(new JLabel(ElanLocale.getString("PreferencesDialog.UI.PaintingStrategy")), gbc);
        
        ButtonGroup bGroup = new ButtonGroup();
        useBufferedPaintingRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.UI.UseBufferedPainting"));
        useBufferedPaintingRB.setSelected(origUseBufferedPainting);
        useBufferedPaintingRB.setFont(tooltipCB.getFont().deriveFont(Font.PLAIN));
        useDirectPaintingRB = new JRadioButton(ElanLocale.getString("PreferencesDialog.UI.UseDirectPainting"));
        useDirectPaintingRB.setSelected(!origUseBufferedPainting);
        useDirectPaintingRB.setFont(tooltipCB.getFont().deriveFont(Font.PLAIN));
        bGroup.add(useBufferedPaintingRB);
        bGroup.add(useDirectPaintingRB);
       
        // add buttons        
        gbc.gridy = gy++;
        gbc.insets = globalInset;
        outerPanel.add(useBufferedPaintingRB, gbc);
      
        gbc.gridy = gy++;
        outerPanel.add(useDirectPaintingRB, gbc);
        
        gbc.gridy = gy++;       
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;        
        outerPanel.add(new JPanel(), gbc); // filler
    }
    
    /**
     * Returns a map with the changes.
     *
     * @return a map containing the changes or {@code null}
     */
    @Override
	public Map<String, Object> getChangedPreferences() {
        if (isChanged()) {
            Map<String, Object> chMap = new HashMap<String, Object>(6);

            if (!nrOfRecentItemsCBox.getSelectedItem().equals(origNrRecentItems)) {
                chMap.put("UI.RecentItems",
                    nrOfRecentItemsCBox.getSelectedItem());
            }
            if (fontScaleSlider.getValue() != origFontScaleInt) {
            	if (fontScaleSlider.getValue() == 100) {
            		// remove the preference
            		chMap.put("UI.FontScaleFactor", null);
            	} else {
            		chMap.put("UI.FontScaleFactor", fontScaleSlider.getValue() / 100f);
            	}
            }
            // will be handled by ElanLayoutManager (arbitrary choice)
            if (tooltipCB.isSelected() != origToolTipEnabled) {
            	chMap.put("UI.ToolTips.Enabled",
            			Boolean.valueOf(tooltipCB.isSelected()));
            }
            if(showAnnotationCountCB.isSelected() != origShowAnnotationCount){
        		chMap.put( "UI.MenuItems.ShowAnnotationCount", Boolean.valueOf(showAnnotationCountCB.isSelected()) );
        	}
            
            if (useBufferedPaintingRB.isSelected() != origUseBufferedPainting) {
            	chMap.put("UI.UseBufferedPainting", Boolean.valueOf(useBufferedPaintingRB.isSelected()));
            }
            
            if (!origDefTierFontName.equals(defTierFontField.getText())) {
            	if (!ElanLocale.getString("Button.Default").equals(defTierFontField.getText())) {
            		chMap.put("Tiers.DefaultFontName", defTierFontField.getText());
            	} else {
            		// remove the preference
            		chMap.put("Tiers.DefaultFontName", null);
            	}
            }
            
            if (origDefTierFontSize != modDefTierFontSize) {
            	if (modDefTierFontSize == 0) {
            		chMap.put("Tiers.DefaultFontSize", null);
            	} else {
            		chMap.put("Tiers.DefaultFontSize", Integer.valueOf(modDefTierFontSize));
            	}
            }
            
            return chMap;
        }

        return null;
    }

    /**
     * Returns whether any of the settings has changed.
     *
     * @return {@code true} if anything changed
     */
    @Override
	public boolean isChanged() {    	
    	boolean fontChanged = false;
    	if (origDefTierFontName.isEmpty()) {
    		if (!ElanLocale.getString("Button.Default").equals(defTierFontField.getText())) {
    			fontChanged = true;
    		}
    	} else if (!origDefTierFontName.equals(defTierFontField.getText())) {
    		fontChanged = true;
    	}
        if (!nrOfRecentItemsCBox.getSelectedItem().equals(origNrRecentItems) || 
        		tooltipCB.isSelected() != origToolTipEnabled || 
        		showAnnotationCountCB.isSelected() != origShowAnnotationCount ||
        		origUseBufferedPainting != useBufferedPaintingRB.isSelected() ||
        		origFontScaleInt != fontScaleSlider.getValue() ||
        		fontChanged ||
        		origDefTierFontSize != modDefTierFontSize) {
            return true;
        }

        return false;
    }
    
    /**
     * Button and TextField action and caret listener.
     *
     */
    private class ActListener implements ActionListener, CaretListener, ChangeListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == fontSelectButton) {
				JFontChooser fontChooser = new JFontChooser();
				Font f = fontChooser.showDialog((JDialog) SwingUtilities.getWindowAncestor(
						UIPrefsPanel.this), true, curTierFont);
				if (f != null) {
					defTierFontField.setText(f.getFontName());
					defTierFontField.setForeground(Constants.DEFAULTFOREGROUNDCOLOR);
					curTierFont = f;
				}
			} else if (e.getSource() == fontResetButton) {
				defTierFontField.setText(ElanLocale.getString("Button.Default"));
				defTierFontField.setForeground(Constants.SHAREDCOLOR3);
				curTierFont = null;
			} else if (e.getSource() == fontSizeResetButton) {
				defTierFontSizeField.setText("");
				defTierFontSizeField.setForeground(Constants.SHAREDCOLOR3);
			}		
		}

		@Override
		public void caretUpdate(CaretEvent e) {
			if (e.getSource() == defTierFontSizeField) {
				if (!defTierFontSizeField.getText().isEmpty()) {
					try {
						modDefTierFontSize = Integer.parseInt(defTierFontSizeField.getText());
						defTierFontSizeField.setForeground(Constants.DEFAULTFOREGROUNDCOLOR);
					} catch (Throwable t) {
						defTierFontSizeField.setForeground(Constants.SHAREDCOLOR3);
						modDefTierFontSize = 0;
					}
				} else {
					defTierFontSizeField.setForeground(Constants.SHAREDCOLOR3);
					modDefTierFontSize = 0;
				}
			}
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == fontScaleSlider) {
				fontScaleSlider.setToolTipText(
						String.valueOf(fontScaleSlider.getValue())+ "%");
			}
			
		}
    	
    }
}
