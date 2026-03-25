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

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.prefs.PreferenceEditor;
import nl.mpi.util.FileUtility;

/**
 * A panel for changing the automatic backup preference settings
 *
 */
@SuppressWarnings("serial")
public class AutomaticBackupPanel extends AbstractEditPrefsPanel
		implements ActionListener, PreferenceEditor, ChangeListener {

	private JCheckBox backupOnCB;
	private JCheckBox backupOffCB;
	private Boolean autoBackupOn = false;
	private JLabel backUpFilesLabel;
	private JLabel backUpIntervalLabel;
	private JRadioButton backup1Minute;
	private JRadioButton backup5Minute;
	private JRadioButton backup10Minute;
	private JRadioButton backup20Minute;
	private JRadioButton backup30Minute;
	// private boolean automaticBackupFlag = false;
	private JComboBox<Integer> nrOfBuFilesCB;
	private Integer origNumBuFiles = 1;
	private JLabel backupFileLocationLabel;
	private JLabel setBackupDirLabel;
	private JLabel curBackupDirLabel;
	private String curBackupFilesLocation = "-";
	private JButton defaultDirButton;
	private JButton resetDirButton;
	
	private Integer backupDelay = 0;
	private Integer selectedBackupDelay = 0;
	
	
	

	/**
	 * Creates a new AutomaticBackupPanel instance.
	 */
	public AutomaticBackupPanel() {
		super(ElanLocale.getString("AutomaticBackupDialog.Label.AB"));

		readPrefs();
		initComponents();
	}

	/**
	 * initialize components
	 */
	private void initComponents() {
		GridBagConstraints gbc;

		backupOnCB = new JCheckBox(ElanLocale.getString("AutomaticBackupDialog.Edit.BackupOn"));
		backupOffCB = new JCheckBox(ElanLocale.getString("AutomaticBackupDialog.Edit.BackupOff"));
		ButtonGroup group = new ButtonGroup();
		group.add(backupOnCB);
		group.add(backupOffCB);
		backupOnCB.addChangeListener(this);
		backupOffCB.addChangeListener(this);

		JPanel automaticBPEditPanel = new JPanel(new GridBagLayout());

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		automaticBPEditPanel.add(backupOnCB, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		automaticBPEditPanel.add(backupOffCB, gbc);

		// backup panel
		Integer[] nrOfBuItemsList = { 1, 2, 3, 4, 5 };
		nrOfBuFilesCB = new JComboBox<Integer>(nrOfBuItemsList);
		nrOfBuFilesCB.setSelectedItem(origNumBuFiles);

		backUpFilesLabel = new JLabel(ElanLocale.getString("AutomaticBackupDialog.Edit.NumBackUp"));

		JPanel backupFilesPanel = new JPanel(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		backupFilesPanel.add(backUpFilesLabel, gbc);

		gbc.gridx = 1;
		gbc.insets = leftInset;
		backupFilesPanel.add(nrOfBuFilesCB, gbc);

		// interval panel
		// retrieve the stored value for backup interval, reuses the main back up
		// preference key

		backUpIntervalLabel = new JLabel(ElanLocale.getString("AutomaticBackupDialog.Edit.BackUpInterval"));

		Integer backUpDelay = Preferences.getInt("BackUpDelay", null);

		backup1Minute = new JRadioButton(ElanLocale.getString("AutomaticBackupDialog.Edit.Backup1Minute"));
		
		//default setting if elan.pfsx is empty or coming from previous version settings
		if (backUpDelay == null || (backupDelay.intValue() == 0)) {
			backup1Minute.setSelected(true);
			Preferences.set("BackUpDelay", Constants.BACKUP_1, null);
		}

		if ((backUpDelay != null) && (backUpDelay.compareTo(Constants.BACKUP_1) == 0)) {
			backup1Minute.setSelected(true);
		}

		backup5Minute = new JRadioButton(ElanLocale.getString("AutomaticBackupDialog.Edit.Backup5Minute"));

		if ((backUpDelay != null) && (backUpDelay.compareTo(Constants.BACKUP_5) == 0)) {
			backup5Minute.setSelected(true);
		}

		backup10Minute = new JRadioButton(ElanLocale.getString("AutomaticBackupDialog.Edit.Backup10Minute"));

		if ((backUpDelay != null) && (backUpDelay.compareTo(Constants.BACKUP_10) == 0)) {
			backup10Minute.setSelected(true);
		}

		backup20Minute = new JRadioButton(ElanLocale.getString("AutomaticBackupDialog.Edit.Backup20Minute"));

		if ((backUpDelay != null) && (backUpDelay.compareTo(Constants.BACKUP_20) == 0)) {
			backup20Minute.setSelected(true);
		}

		backup30Minute = new JRadioButton(ElanLocale.getString("AutomaticBackupDialog.Edit.Backup30Minute"));

		if ((backUpDelay != null) && (backUpDelay.compareTo(Constants.BACKUP_30) == 0)) {
			backup30Minute.setSelected(true);
		}
		ButtonGroup backupGroup = new ButtonGroup();
		backupGroup.add(backup1Minute);
		backupGroup.add(backup5Minute);
		backupGroup.add(backup10Minute);
		backupGroup.add(backup20Minute);
		backupGroup.add(backup30Minute);

		JPanel backupIntervalPanel = new JPanel(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		backupIntervalPanel.add(backup1Minute, gbc);

		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		gbc.insets = topInset;
		backupIntervalPanel.add(backup5Minute, gbc);

		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		gbc.insets = topInset;
		backupIntervalPanel.add(backup10Minute, gbc);

		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		gbc.insets = topInset;
		backupIntervalPanel.add(backup20Minute, gbc);

		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		gbc.insets = topInset;
		backupIntervalPanel.add(backup30Minute, gbc);

		backup1Minute.addActionListener(this);
		backup5Minute.addActionListener(this);
		backup10Minute.addActionListener(this);
		backup20Minute.addActionListener(this);
		backup30Minute.addActionListener(this);

		backupFileLocationLabel = new JLabel(ElanLocale.getString("AutomaticBackupDialog.Edit.FileLocation"));

		setBackupDirLabel = new JLabel(ElanLocale.getString("AutomaticBackupDialog.Edit.DefaultLoc"));
		Font plainFont;
		plainFont = setBackupDirLabel.getFont().deriveFont(Font.PLAIN);
		setBackupDirLabel.setFont(plainFont);

		JPanel backupDirPanel = new JPanel(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = topInset;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		backupDirPanel.add(setBackupDirLabel, gbc);

		curBackupDirLabel = new JLabel(curBackupFilesLocation);
		curBackupDirLabel.setFont(new Font(curBackupDirLabel.getFont().getFontName(), Font.PLAIN, 10));
		gbc.gridy = 1;
		backupDirPanel.add(curBackupDirLabel, gbc);

		defaultDirButton = new JButton(ElanLocale.getString("Button.Browse"));
		gbc.gridy = 0;
		gbc.gridx = 1;
		gbc.gridheight = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.insets = leftInset;
		backupDirPanel.add(defaultDirButton, gbc);
		defaultDirButton.addActionListener(this);

		resetDirButton = new JButton();
		ImageIcon resetIcon = null;
		// add reset icon
		try {
			resetIcon = new ImageIcon(AutomaticBackupPanel.class.getResource("/mpi/eudico/client/annotator/resources/Remove.gif"));
			resetDirButton.setIcon(resetIcon);
		} catch (Exception ex) {
			resetDirButton.setText("X");
		}

		resetDirButton.setToolTipText(ElanLocale.getString("PreferencesDialog.Reset"));
		resetDirButton.setPreferredSize(
				new Dimension(resetDirButton.getPreferredSize().width, defaultDirButton.getPreferredSize().height));
		gbc.gridx = 2;
		backupDirPanel.add(resetDirButton, gbc);
		resetDirButton.addActionListener(this);
		
		if (autoBackupOn) {
			backupOnCB.setSelected(autoBackupOn);
		} else {
			backupOffCB.setSelected(!autoBackupOn);
		}

		// editing options panel
		int gy = 0;

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = gy++;
		gbc.insets = catInset;
		outerPanel.add(new JLabel(ElanLocale.getString("AutomaticBackupDialog.Label.AB")), gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = gy++;
		gbc.insets = catPanelInset;
		outerPanel.add(automaticBPEditPanel, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = gy++;
		gbc.insets = catInset;
		outerPanel.add(backupFilesPanel, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = gy++;
		gbc.insets = catInset;
		outerPanel.add(backUpIntervalLabel, gbc);
		
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = gy++;
		gbc.insets = catPanelInset;
		outerPanel.add(backupIntervalPanel, gbc);

		gbc.gridy = gy++;
		gbc.insets = catInset;
		outerPanel.add(backupFileLocationLabel, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = gy++;
		gbc.insets = catPanelInset;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		outerPanel.add(backupDirPanel, gbc);

		gbc.gridy = gy++;
		gbc.weighty = 1.0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		outerPanel.add(new JPanel(), gbc);

	}

	/**
	 * Reads the preference settings
	 */
	private void readPrefs() {
		
		Integer integerPref = Preferences.getInt("BackUpDelay", null);
		if(integerPref != null) {
			backupDelay = integerPref;
		}
		
		Boolean boolPref = Preferences.getBool("AutomaticBackupOn" , null);
		
		if(boolPref != null) {
			if (backupDelay.intValue() == 0) {
				autoBackupOn = false;
			}else {
				autoBackupOn = boolPref.booleanValue();
			}
		}

		Integer intPref = Preferences.getInt("NumberOfBackUpFiles", null);

		if (intPref != null) {
			origNumBuFiles = intPref;
		}
		
		String stringPref = Preferences.getString("DefaultBackupFilesLocation", null);
		if (stringPref != null) {
			curBackupFilesLocation = stringPref;
		}		
		 

	}

	/**
	 * Listener event for the state change
	 */
	@Override
	public void stateChanged(ChangeEvent e) {

		nrOfBuFilesCB.setEnabled(backupOnCB.isSelected());
		backup1Minute.setEnabled(backupOnCB.isSelected());
		backup5Minute.setEnabled(backupOnCB.isSelected());
		backup10Minute.setEnabled(backupOnCB.isSelected());
		backup20Minute.setEnabled(backupOnCB.isSelected());
		backup30Minute.setEnabled(backupOnCB.isSelected());
		defaultDirButton.setEnabled(backupOnCB.isSelected());
		resetDirButton.setEnabled(backupOnCB.isSelected());
		backUpFilesLabel.setEnabled(backupOnCB.isSelected());
		backUpIntervalLabel.setEnabled(backupOnCB.isSelected());
		backupFileLocationLabel.setEnabled(backupOnCB.isSelected());
		setBackupDirLabel.setEnabled(backupOnCB.isSelected());
		curBackupDirLabel.setEnabled(backupOnCB.isSelected());

	}

	/**
     * Returns weather anything has changed or not
     *
     * @return true if anything changed
     */
	@Override
	public boolean isChanged() {
		
		if (backup1Minute.isSelected()) {
			selectedBackupDelay = Constants.BACKUP_1;
		} else if (backup5Minute.isSelected()) {
			selectedBackupDelay = Constants.BACKUP_5;
		} else if (backup10Minute.isSelected()) {
			selectedBackupDelay = Constants.BACKUP_10;
		} else if (backup20Minute.isSelected()) {
			selectedBackupDelay = Constants.BACKUP_20;
		} else if (backup30Minute.isSelected()) {
			selectedBackupDelay = Constants.BACKUP_30;
		}

		return (backupOnCB.isSelected() != autoBackupOn || !selectedBackupDelay.equals(backupDelay)
				|| !nrOfBuFilesCB.getSelectedItem().equals(origNumBuFiles)
				|| !curBackupFilesLocation.equals(curBackupDirLabel.getText()));

	}

	/**
	 * Returns a map of (changed) preferences
	 * 
	 * @return a map of (changed) preferences, or {@code}
	 */
	@Override
	public Map<String, Object> getChangedPreferences() {
		if (isChanged()) {
			Map<String, Object> chMap = new HashMap<String, Object>(10);

			if (!origNumBuFiles.equals(nrOfBuFilesCB.getSelectedItem())) {
				chMap.put("NumberOfBackUpFiles", nrOfBuFilesCB.getSelectedItem());
			}
			
			if (autoBackupOn != backupOnCB.isSelected()) {
				chMap.put("AutomaticBackupOn", backupOnCB.isSelected());
			}
			
			if (!selectedBackupDelay.equals(backupDelay)) {
				chMap.put("BackUpDelay", selectedBackupDelay);
			}
			
			if ((curBackupDirLabel.getText() != null) && !curBackupDirLabel.getText().equals("-")) {
                chMap.put("DefaultBackupFilesLocation", curBackupDirLabel.getText());
            } else {
                chMap.put("DefaultBackupFilesLocation", null);
            }

			return chMap;
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == backup1Minute) {
			Preferences.set("BackUpDelay", Constants.BACKUP_1, null);
		} else if (e.getSource() == backup5Minute) {
			Preferences.set("BackUpDelay", Constants.BACKUP_5, null);
		} else if (e.getSource() == backup10Minute) {
			Preferences.set("BackUpDelay", Constants.BACKUP_10, null);
		} else if (e.getSource() == backup20Minute) {
			Preferences.set("BackUpDelay", Constants.BACKUP_20, null);
		} else if (e.getSource() == backup30Minute) {
			Preferences.set("BackUpDelay", Constants.BACKUP_30, null);
		} else if (e.getSource() == defaultDirButton) {
			// show a folder file chooser, set the current def. location
			FileChooser chooser = new FileChooser(this);
			if (curBackupFilesLocation.length() > 1) {
				File dir = new File(FileUtility.urlToAbsPath(curBackupFilesLocation));

				if (dir.exists() && dir.isDirectory()) {
					chooser.setCurrentDirectory(dir.getAbsolutePath());
				}
			}
			chooser.createAndShowFileDialog(ElanLocale.getString("PreferencesDialog.Media.DefaultLoc"),
					FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), null, null, true, null,
					FileChooser.DIRECTORIES_ONLY, null);
			// chooser.setMultiSelectionEnabled(false);

			File selFile = chooser.getSelectedFile();
			if (selFile != null) {
				curBackupDirLabel.setText(selFile.getAbsolutePath());
				curBackupDirLabel.setText(FileUtility.pathToURLString(selFile.getAbsolutePath()));
			}

		} else if (e.getSource() == resetDirButton) {
			curBackupDirLabel.setText("-");
		}

	}

}
