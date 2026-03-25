package mpi.eudico.client.annotator.export.multiplefiles;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

/**
 * An abstract step pane to set the 'save as' settings for the output files.
 *
 * @author aarsom
 */
@SuppressWarnings("serial")
public abstract class AbstractMultiFileExportSaveSettingsStepPane extends StepPane implements ActionListener {

    //
    /**
     * declaration of save with original names
     */
    protected String saveWithOriginalNames;
    /**
     * declaration of save in original folder
     */
    protected String saveInOriginalFolder;
    /**
     * declaration of save in relative folder
     */
    protected String saveInRelativeFolder;
    /**
     * declaration of save in relative folder name
     */
    protected String saveInRelativeFolderName;
    /**
     * declaration of save in same folder name
     */
    protected String saveInSameFolderName;
    /**
     * declaration of do not create empty files variable
     */
    protected String dontCreateEmptyFiles;

    //components
    /**
     * declaration of browser button
     */
    protected JButton browseBtn;

    /**
     * declaration of do not export files without tiers check box
     */
    protected JCheckBox dontExportFilesWithoutTiersCB;

    /**
     * declaration of original directory, same directory and new directory radio buttons
     */
    protected JRadioButton originalDirRB;
    protected JRadioButton togetherInSameDirRB;
    protected JRadioButton newDirectoryRB;
    /**
     * declaration of original file name and add suffix radio buttons
     */
    protected JRadioButton originalFileNameRB;
    protected JRadioButton addSuffixRB;

    /**
     * declaration of directory text fields
     */
    protected JTextField sameDirectoryTextField;
    protected JTextField localDirectoryTextField;

    /**
     * declaration of file name , directory options and other options panels
     */
    protected JPanel fileNameOptionsPanel;
    protected JPanel directoryOptionsPanel;
    protected JPanel otherOptionsPanel;

    /**
     * declaration of insets
     */
    protected Insets insets = new Insets(2, 4, 2, 4);

    /**
     * file extension combo box
     */
    protected JComboBox fileExtComboBox;

    /**
     * declaration of scroll pane
     */
    protected JScrollPane outerScrollPane;

    /**
     * export tiers dialog directory name field
     */
    protected Object browseDirText = ElanLocale.getString("ExportTiersDialog.TextField.DirectoryNameField");

    /**
     * extensions array
     */
    private final String[] extensions;

    /**
     * Constructor.
     *
     * @param multiStepPane the parent container holding the step panes
     */
    public AbstractMultiFileExportSaveSettingsStepPane(MultiStepPane multiStepPane) {
        super(multiStepPane);
        setPreferenceStrings();
        extensions = getExportExtensions();
        initComponents();
    }

    /**
     * Method to set the preference string which will be used to restore the last used setting.
     */
    protected abstract void setPreferenceStrings();

    /**
     * Initializes the ui components
     */
    @Override
    protected void initComponents() {
        initFileNameOptionsPanel();
        initDirectoryOptionsPanel();
        initOtherOptionsPanel();

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new GridBagLayout());

        outerScrollPane = new JScrollPane(outerPanel);
        outerScrollPane.setBorder(null);

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        outerPanel.add(fileNameOptionsPanel, gbc);

        gbc.gridy = 1;
        outerPanel.add(directoryOptionsPanel, gbc);

        gbc.gridy = 2;
        outerPanel.add(otherOptionsPanel, gbc);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weighty = 1.0;
        outerPanel.add(new JPanel(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(outerScrollPane, gbc);

        //event handlers
        //ActionListener radioBtnListener = new RadioButtonHandler();
        KeyListener keyListener = new TextFieldHandler();

        //event handlers for the radio buttons
        originalDirRB.addActionListener(this);
        newDirectoryRB.addActionListener(this);
        togetherInSameDirRB.addActionListener(this);
        originalFileNameRB.addActionListener(this);
        addSuffixRB.addActionListener(this);

        //event handlers for the buttons
        browseBtn.addActionListener(this);

        //event handlers for the text fields
        localDirectoryTextField.addKeyListener(keyListener);

        loadPreferences();
    }

    @Override
    public abstract String getStepTitle();

    /**
     * Gets the extensions in an array
     *
     * @return array of strings
     */
    protected abstract String[] getExportExtensions();

    /**
     * Calls the next step
     *
     * @see StepPane#doFinish()
     */
    @Override
    public boolean doFinish() {
        savePreferences();
        multiPane.nextStep();
        return false;
    }

    @Override
    public void enterStepForward() {
        updateButtonStates();
    }

    @Override
    public void enterStepBackward() {
        updateButtonStates();
    }

    /**
     * Check and store properties, if all conditions are met.
     *
     * @see StepPane#leaveStepForward()
     */
    @Override
    public boolean leaveStepForward() {
        // in fact all buttons and check boxes would need to be checked for null
        multiPane.putStepProperty("UseOriginalDir", originalDirRB.isSelected());
        multiPane.putStepProperty("NewDirectory", newDirectoryRB.isSelected());
        multiPane.putStepProperty("NewDirName", localDirectoryTextField.getText().trim());
        multiPane.putStepProperty("TogetherInSameDir", togetherInSameDirRB.isSelected());
        multiPane.putStepProperty("SameDirectoryName", sameDirectoryTextField.getText());

        if (dontExportFilesWithoutTiersCB != null) {
            multiPane.putStepProperty("DontExportFilesWithoutTiers", dontExportFilesWithoutTiersCB.isSelected());
        }

        multiPane.putStepProperty("UseOriginalFileName", originalFileNameRB.isSelected());
        multiPane.putStepProperty("UseOriginalFileNameWithSuffix", addSuffixRB.isSelected());

        if (fileExtComboBox != null) {
            multiPane.putStepProperty("ExportExtension", fileExtComboBox.getSelectedItem().toString());
        }
        return true;

    }

    /**
     * Set the button states appropriately, according to constraints
     */
    public void updateButtonStates() {
        multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);

        if (togetherInSameDirRB.isSelected() && sameDirectoryTextField.getText().equals(browseDirText)) {
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
            return;
        }

        if (newDirectoryRB.isSelected() && localDirectoryTextField.getText().length() <= 0) {
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
        }
    }

    /**
     * Initializes the File name options panel
     */
    protected void initFileNameOptionsPanel() {
        fileNameOptionsPanel = new JPanel(new GridBagLayout());
        fileNameOptionsPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportTiersDialog.Label.FileNameOptions")));

        originalFileNameRB =
            new JRadioButton(ElanLocale.getString("MultiFileExport.SaveSettingsPane.RB.OriginalFileName"), true);
        addSuffixRB =
            new JRadioButton(ElanLocale.getString("MultiFileExport.SaveSettingsPane.RB.OriginalFileNameWithSuffix"));

        ButtonGroup fileNameBtnGroup = new ButtonGroup();
        fileNameBtnGroup.add(originalFileNameRB);
        fileNameBtnGroup.add(addSuffixRB);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        fileNameOptionsPanel.add(originalFileNameRB, gbc);

        gbc.gridy = 1;
        fileNameOptionsPanel.add(addSuffixRB, gbc);

        if (this.extensions != null) {
            gbc.gridy = 2;
            fileNameOptionsPanel.add(getFileExtensionPanel(), gbc);
        }
    }

    /**
     * File Extension option panel
     *
     * @return returns file extension panel
     */
    protected JPanel getFileExtensionPanel() {
        fileExtComboBox = new JComboBox();
        for (String ext : extensions) {
            fileExtComboBox.addItem(ext);
        }

        //fileExtComboBox.setSelectedIndex(0);

        JPanel fileExtPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = insets;
        fileExtPanel.add(new JLabel(ElanLocale.getString("MultiFileExport.SaveSettingsPane.Label.FileExtension")), gbc);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 1;
        fileExtPanel.add(fileExtComboBox, gbc);
        return fileExtPanel;
    }

    /**
     * Panel with directory options
     */
    protected void initDirectoryOptionsPanel() {

        directoryOptionsPanel = new JPanel(new GridBagLayout());
        directoryOptionsPanel.setBorder(new TitledBorder(ElanLocale.getString("MultiFileExportToolbox.Label"
                                                                              + ".SaveDirOptions")));

        originalDirRB = new JRadioButton(ElanLocale.getString("ExportTiersDialog.RadioButton.OriginalDirectory"), true);
        newDirectoryRB = new JRadioButton(ElanLocale.getString("ExportTiersDialog.RadioButton.NewDirectory"));
        togetherInSameDirRB =
            new JRadioButton(ElanLocale.getString("ExportTiersDialog.RadioButton.TogetherInSameDirectory"));

        ButtonGroup saveTierBtnGroup = new ButtonGroup();
        saveTierBtnGroup.add(originalDirRB);
        saveTierBtnGroup.add(togetherInSameDirRB);
        saveTierBtnGroup.add(newDirectoryRB);

        localDirectoryTextField =
            new JTextField(ElanLocale.getString("ExportTiersDialog.TextField.DefaultLocalDirectoryName"));
        localDirectoryTextField.setEnabled(false);

        sameDirectoryTextField = new JTextField(ElanLocale.getString("ExportTiersDialog.TextField.DirectoryNameField"));
        sameDirectoryTextField.setEnabled(false);
        sameDirectoryTextField.setEditable(false);
        sameDirectoryTextField.setMinimumSize(new Dimension(194, sameDirectoryTextField.getMinimumSize().height));

        browseBtn = new JButton(ElanLocale.getString("ExportTiersDialog.Button.Browse"));
        browseBtn.setEnabled(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = insets;
        directoryOptionsPanel.add(originalDirRB, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        directoryOptionsPanel.add(newDirectoryRB, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        directoryOptionsPanel.add(localDirectoryTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        directoryOptionsPanel.add(togetherInSameDirRB, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        directoryOptionsPanel.add(sameDirectoryTextField, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        directoryOptionsPanel.add(browseBtn, gbc);
    }

    /**
     * Initializes Other options panel
     */
    protected void initOtherOptionsPanel() {
        otherOptionsPanel = new JPanel(new GridBagLayout());
        otherOptionsPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportTiersDialog.Label.OtherOptions")));

        dontExportFilesWithoutTiersCB =
            new JCheckBox(ElanLocale.getString("ExportTiersDialog.CheckBox.ExportFilesWithoutTiers"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        otherOptionsPanel.add(dontExportFilesWithoutTiersCB, gbc);
    }

    /**
     * Updates the state of the buttons depending on the selection
     */
    protected void updateButtonsAndFields() {
        sameDirectoryTextField.setEnabled(togetherInSameDirRB.isSelected());
        browseBtn.setEnabled(togetherInSameDirRB.isSelected());
        localDirectoryTextField.setEnabled(newDirectoryRB.isSelected());
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();
        if (source == browseBtn) {
            String directoryStr = showDirectoryChooser();
            if (directoryStr != null) {
                sameDirectoryTextField.setText(directoryStr);
            }
        }

        updateButtonsAndFields();
        updateButtonStates();

    }

    /**
     * Shows a dialog that lets the user choose a directory
     *
     * @return string representing the selected directory name or null if no directory has been chosen
     */
    protected String showDirectoryChooser() {
        //create a directory chooser and set relevant attributes
        FileChooser dirChooser = new FileChooser(this);
        dirChooser.createAndShowFileDialog(ElanLocale.getString("Frame.ElanFrame.OpenDialog.Title"),
                                           FileChooser.OPEN_DIALOG,
                                           ElanLocale.getString("Button.OK"),
                                           null,
                                           null,
                                           false,
                                           "LastUsedExportDir",
                                           FileChooser.DIRECTORIES_ONLY,
                                           null);

        File selectedDir = dirChooser.getSelectedFile();
        if (selectedDir != null) {
            return selectedDir.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * saves preferences
     */
    protected void savePreferences() {
        if (saveWithOriginalNames != null) {
            Preferences.set(saveWithOriginalNames, originalFileNameRB.isSelected(), null);
        }

        if (saveInOriginalFolder != null) {
            Preferences.set(saveInOriginalFolder, originalDirRB.isSelected(), null);
        }

        if (saveInRelativeFolder != null) {
            Preferences.set(saveInRelativeFolder, newDirectoryRB.isSelected(), null);
        }

        if (saveInRelativeFolderName != null) {
            String relFolderName = localDirectoryTextField.getText();
            if (!ElanLocale.getString("ExportTiersDialog.TextField.DefaultLocalDirectoryName").equals(relFolderName)) {
                Preferences.set(saveInRelativeFolderName, relFolderName, null);
            }
        }

        if (saveInSameFolderName != null) {
            String sameFolder = sameDirectoryTextField.getText();
            if (!ElanLocale.getString("ExportTiersDialog.TextField.DirectoryNameField").equals(sameFolder)) {
                Preferences.set(saveInSameFolderName, sameFolder, null);
            }
        }

        if (dontCreateEmptyFiles != null && dontExportFilesWithoutTiersCB != null) {
            Preferences.set(dontCreateEmptyFiles, dontExportFilesWithoutTiersCB.isSelected(), null);
        }
    }

    /**
     * Loads preferences
     */
    protected void loadPreferences() {
        String stringPref;
        Boolean boolPref;

        if (saveWithOriginalNames != null) {
            boolPref = Preferences.getBool(saveWithOriginalNames, null);
            if (boolPref != null) {
                originalFileNameRB.setSelected(boolPref);
                addSuffixRB.setSelected(!boolPref);
            }
        }

        if (saveInOriginalFolder != null) {
            boolPref = Preferences.getBool(saveInOriginalFolder, null);
            if (boolPref != null) {
                boolean origFolder = boolPref;
                if (!origFolder) {
                    boolPref = Preferences.getBool(saveInRelativeFolder, null);
                    if (boolPref != null) {
                        boolean relFolder = boolPref;
                        if (relFolder) {
                            newDirectoryRB.setSelected(true);
                        } else {
                            togetherInSameDirRB.setSelected(true);
                        }
                    }
                }
            }
        }

        if (saveInRelativeFolderName != null) {
            stringPref = Preferences.getString(saveInRelativeFolderName, null);
            if (stringPref != null) {
                localDirectoryTextField.setText(stringPref);
            }
        }

        if (saveInSameFolderName != null) {
            stringPref = Preferences.getString(saveInSameFolderName, null);
            if (stringPref != null) {
                sameDirectoryTextField.setText(stringPref);
            }
        }

        if (dontCreateEmptyFiles != null) {
            boolPref = Preferences.getBool(dontCreateEmptyFiles, null);
            if (boolPref != null && dontExportFilesWithoutTiersCB != null) {
                dontExportFilesWithoutTiersCB.setSelected(boolPref);
            }
        }

        updateButtonsAndFields();
    }

    /**
     * Class to handle key events like key press, key release and key type
     */
    protected class TextFieldHandler implements KeyListener {
        /**
         * Creates a new handler instance.
         */
        public TextFieldHandler() {
            super();
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            updateButtonStates();
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

    }
}
