package mpi.eudico.client.annotator.interannotator;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.export.multiplefiles.AbstractFilesAndTierSelectionStepPane;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * Only reuses and extends the file selection part of the super class. The extension is that, depending on choices in a
 * previous step, the user can specify how to match 2 files, based on prefix or suffix.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class FilesSelectionStep extends AbstractFilesAndTierSelectionStepPane implements ChangeListener {
    /**
     * matching options panel identifier
     */
    protected JPanel matchingOptionsPanel;
    /**
     * matching label identifier
     */
    protected JLabel matchLabel;
    /**
     * prefix match radio button
     */
    protected JRadioButton prefixMatchRB;
    /**
     * suffix match radio button
     */
    protected JRadioButton suffixMatchRB;
    /**
     * custom step check box
     */
    protected JCheckBox customSepCB;
    /**
     * custom step text field
     */
    protected JTextField customSepTF;
    /**
     * selected files list
     */
    protected ArrayList<File> selFiles;
    // items for tier name matching
    private JPanel tierMatchingOptionsPanel;
    private JRadioButton tierPrefixMatchRB;
    private JRadioButton tierSuffixMatchRB;
    private JCheckBox tierCustomSepCB;
    private JTextField tierCustomSepTF;
    // panel for option to combine matching tiers in a single (new) file
    private JPanel exportTierPanel;
    private JCheckBox exportCombineTiersCB;
    private JRadioButton exportFilePerSetOfTiersRB;
    private JRadioButton exportFilePerSetOfFilesRB;
    private JLabel exportOutLabel;
    private JTextField exportFolderTF;
    private JButton exportFolderBrowseBT;
    private String exportFolder;
    private JPanel contentPanel;
    private JScrollPane contentScrollPane;

    private final String prefFileSelection = "Compare.FileSelection";
    private final String prefTierAffixType = "Compare.Matching.TierAffix.Type";
    private final String prefFileAffixType = "Compare.Matching.FileAffix.Type";
    private final String prefFileCustomSep = "Compare.Matching.FileCustomSeparator";
    private final String prefTierCustomSep = "Compare.Matching.TierCustomSeparator";
    private final String prefExportMatchingTiers = "Compare.Export.MatchingTiers";
    private final String prefExportPerTierSet = "Compare.Export.TiersPerTierSet";
    private final String prefExportTiersFolder = "Compare.Export.TierFolder";

    /**
     * Constructor.
     *
     * @param mp the parent pane
     * @param transcription the transcription, can be {@code null}
     */
    public FilesSelectionStep(MultiStepPane mp, TranscriptionImpl transcription) {
        super(mp, transcription);
        initComponents2();
    }

    @Override
    protected void initComponents() {
        // method stub
    }

    /**
     * Initializes the components
     */
    protected void initComponents2() {
        initFileSelectionPanel();
        initTierSelectionPanel();
        initOptionsPanel();
        initExportPanel();

        contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = globalInset;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(fileSelectionPanel, gbc);

        gbc.gridy = 1;
        contentPanel.add(tierMatchingOptionsPanel, gbc);
        gbc.gridy = 2;
        contentPanel.add(exportTierPanel, gbc);
        contentScrollPane = new JScrollPane(contentPanel);
        setLayout(new BorderLayout());
        add(contentScrollPane);
        loadPreferences();
    }

    @Override
    protected void initFileSelectionPanel() {
        super.initFileSelectionPanel();
        fileSelectionPanel.remove(currentlyOpenedFileRB);

        // add additional options if appropriate
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        fileSelectionPanel.add(new JPanel(), gbc);
    }

    @Override
    protected void initTierSelectionPanel() {
        tierSelectionPanel = new JPanel();
        textArea = new JTextArea();

        tierMatchingOptionsPanel = new JPanel(new GridBagLayout());
        JPanel innerPanel = new JPanel(new GridBagLayout());

        tierSuffixMatchRB = new JRadioButton();
        tierSuffixMatchRB.setSelected(true);
        tierPrefixMatchRB = new JRadioButton();
        ButtonGroup matchGroup = new ButtonGroup();
        matchGroup.add(tierSuffixMatchRB);
        matchGroup.add(tierPrefixMatchRB);
        tierCustomSepCB = new JCheckBox();
        tierCustomSepTF = new JTextField(6);
        tierCustomSepTF.setEnabled(false);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        //gbc.insets = globalInset; // new Insets(2, 4, 2, 4)
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 30, 0, 10);
        innerPanel.add(tierSuffixMatchRB, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        innerPanel.add(tierPrefixMatchRB, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 3;
        innerPanel.add(tierCustomSepCB, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 60, 0, 10); // TODO revise, have some constants with indentation values
        gbc.fill = GridBagConstraints.NONE;
        innerPanel.add(tierCustomSepTF, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        tierMatchingOptionsPanel.add(innerPanel, gbc);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        tierMatchingOptionsPanel.add(new JPanel(), gbc);

        tierSuffixMatchRB.setText(ElanLocale.getString("CreateMultiEAFDialog.Label.Suffix"));
        tierPrefixMatchRB.setText(ElanLocale.getString("CreateMultiEAFDialog.Label.Prefix"));
        tierCustomSepCB.setText(ElanLocale.getString("CreateMultiEAFDialog.Button.Separator"));
        tierCustomSepCB.addChangeListener(this);
        tierMatchingOptionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
            "CompareAnnotatorsDialog.FilesSelectionStep.CombineTiers")));

    }

    /**
     * Initialize the options panel
     */
    protected void initOptionsPanel() {
        matchingOptionsPanel = new JPanel(new GridBagLayout());
        // give this panel a titled border, or just add it to the tier selection panel?
        matchLabel = new JLabel();
        //matchLabel.setAlignmentX(0f);
        suffixMatchRB = new JRadioButton();
        suffixMatchRB.setSelected(true);
        prefixMatchRB = new JRadioButton();
        ButtonGroup matchGroup = new ButtonGroup();
        matchGroup.add(suffixMatchRB);
        matchGroup.add(prefixMatchRB);
        customSepCB = new JCheckBox();
        customSepTF = new JTextField(6);
        customSepTF.setEnabled(false);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = globalInset;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        matchingOptionsPanel.add(matchLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 30, 0, 10);
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        matchingOptionsPanel.add(suffixMatchRB, gbc);

        gbc.gridx = 1;
        matchingOptionsPanel.add(prefixMatchRB, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        matchingOptionsPanel.add(customSepCB, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 60, 0, 10); // TODO revise, have some constants with indentation values
        gbc.fill = GridBagConstraints.NONE;
        matchingOptionsPanel.add(customSepTF, gbc);

        // constraints for the options panel
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(15, 10, 5, 10);
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // insert the panel after the last real component, "before" the filler panel
        GridBagConstraints fillConstraints = null;
        GridBagConstraints lastConstraints = null;
        try {
            Component lastRelComp = fileSelectionPanel.getComponent(fileSelectionPanel.getComponentCount() - 2);
            lastConstraints = ((GridBagLayout) fileSelectionPanel.getLayout()).getConstraints(lastRelComp);
            Component filler = fileSelectionPanel.getComponent(fileSelectionPanel.getComponentCount() - 1);
            fillConstraints = ((GridBagLayout) fileSelectionPanel.getLayout()).getConstraints(filler);

            if (lastConstraints != null) {
                gbc.gridy = lastConstraints.gridy + 1;
                if (fillConstraints != null) { // remove the filler panel and add it again
                    fileSelectionPanel.remove(filler);
                    fillConstraints.gridy = lastConstraints.gridy + 2;
                    fileSelectionPanel.add(filler, fillConstraints);
                }
            }
        } catch (Throwable t) {
            // just catch any possible exception
            // results in adding the options panel at the end
        }

        fileSelectionPanel.add(matchingOptionsPanel, gbc);

        matchLabel.setText(ElanLocale.getString("CompareAnnotatorsDialog.FilesSelectionStep.CombineFiles"));
        suffixMatchRB.setText(ElanLocale.getString("CreateMultiEAFDialog.Label.Suffix"));
        prefixMatchRB.setText(ElanLocale.getString("CreateMultiEAFDialog.Label.Prefix"));
        customSepCB.setText(ElanLocale.getString("CreateMultiEAFDialog.Button.Separator"));
        customSepCB.addChangeListener(this);
    }

    /**
     * Initialize the export panel
     */
    protected void initExportPanel() {
        exportTierPanel = new JPanel(new GridBagLayout());
        exportTierPanel.setBorder(new TitledBorder(ElanLocale.getString("")));
        exportCombineTiersCB =
            new JCheckBox(ElanLocale.getString("CompareAnnotatorsDialog.FilesSelectionStep.SaveMatchingTiers"));
        exportFilePerSetOfTiersRB =
            new JRadioButton(ElanLocale.getString("CompareAnnotatorsDialog.FilesSelectionStep.SaveFilePerTierGroup"), true);
        exportFilePerSetOfFilesRB =
            new JRadioButton(ElanLocale.getString("CompareAnnotatorsDialog.FilesSelectionStep.SaveFilePerFileGroup"));
        ButtonGroup expGroup = new ButtonGroup();
        expGroup.add(exportFilePerSetOfTiersRB);
        expGroup.add(exportFilePerSetOfFilesRB);
        exportFilePerSetOfFilesRB.setEnabled(false); // not supported yet
        exportOutLabel = new JLabel(ElanLocale.getString("CompareAnnotatorsDialog.FilesSelectionStep.SaveFolder"));
        exportFolderTF = new JTextField();
        exportFolderBrowseBT = new JButton(ElanLocale.getString("Button.Browse"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = globalInset;
        gbc.gridwidth = 2;
        exportTierPanel.add(exportCombineTiersCB, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(2, 15, 2, 0);
        exportTierPanel.add(exportFilePerSetOfTiersRB, gbc);
        gbc.gridy = 2;
        exportTierPanel.add(exportFilePerSetOfFilesRB, gbc);

        gbc.gridy = 3;
        exportTierPanel.add(exportOutLabel, gbc);
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        exportTierPanel.add(exportFolderTF, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.insets = globalInset;
        exportTierPanel.add(exportFolderBrowseBT, gbc);

        exportCombineTiersCB.addChangeListener(this);
        exportFolderBrowseBT.addActionListener(new BrowseHandler());

        exportFolder = System.getProperty("user.home");
        exportFolderTF.setText(exportFolder);
    }

    private void updateExportPanel() {
        boolean selected = exportCombineTiersCB.isSelected();
        exportFilePerSetOfTiersRB.setEnabled(selected);
        //exportFilePerSetOfFilesRB.setEnabled(selected); // activate when supported
        exportOutLabel.setEnabled(selected);
        exportFolderTF.setEnabled(selected);
        exportFolderBrowseBT.setEnabled(selected);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void updateButtonStates() {
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, selFiles != null && selFiles.size() != 0);
    }

    @Override
    protected void initializeTierSelectPanel(ArrayList<File> files) {
        if (files != null) {
            if (selFiles == null) {
                selFiles = new ArrayList<File>();
            }
            selFiles.clear();
            selFiles.addAll(files);
        }
        updateButtonStates();
    }

    /**
     * Overrides this method by doing nothing. Since the files are not going to be changed a warning concerning open files is
     * either not necessary or has to be different (unsaved changes in the files will not be part of the results).
     */
    @Override
    protected void checkForOpenedFiles(List<String> fileNames) {
        // method stub
    }

    @Override
    public void enterStepForward() {
        Object tierSource = multiPane.getStepProperty(CompareConstants.TIER_SOURCE_KEY);
        matchingOptionsPanel.setVisible(tierSource == CompareConstants.FILE_MATCHING.ACROSS_FILES);

        Object tierMatching = multiPane.getStepProperty(CompareConstants.TIER_MATCH_KEY);
        tierMatchingOptionsPanel.setVisible(tierMatching == CompareConstants.MATCHING.AFFIX);

        Object methodObj = multiPane.getStepProperty(CompareConstants.METHOD_KEY);
        if (methodObj != CompareConstants.METHOD.MOD_FLEISS && tierSource != CompareConstants.FILE_MATCHING.CURRENT_DOC) {
            contentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            contentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            exportTierPanel.setVisible(false);
        } else {
            contentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            contentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            exportTierPanel.setVisible(true);
        }

        multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
        boolean nextStep = (selFiles != null && selFiles.size() > 0) || transcription != null;
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, nextStep);

        if (transcription != null
            && tierSource == CompareConstants.FILE_MATCHING.CURRENT_DOC
            && tierMatching != CompareConstants.MATCHING.AFFIX) {
            // nothing to do in this step so move on
            leaveStepForward();
            multiPane.nextStep();
        }

        // check enabling/disabling of file selection components
        if (transcription != null && tierSource == CompareConstants.FILE_MATCHING.CURRENT_DOC) {
            // disable the file selection panel
            selectedFilesFromDiskRB.setEnabled(false);
            filesFromDomainRB.setEnabled(false);
            selectFilesBtn.setEnabled(false);
            selectDomainBtn.setEnabled(false);
        } else {
            selectedFilesFromDiskRB.setEnabled(true);
            filesFromDomainRB.setEnabled(true);
            if (selectedFilesFromDiskRB.isSelected()) {
                selectFilesBtn.setEnabled(true);
            } else {
                selectDomainBtn.setEnabled(true);
            }
        }

    }

    @Override
    public void enterStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);

        Object tierSource = multiPane.getStepProperty(CompareConstants.TIER_SOURCE_KEY);
        Object tierMatching = multiPane.getStepProperty(CompareConstants.TIER_MATCH_KEY);
        if (transcription != null
            && tierSource == CompareConstants.FILE_MATCHING.CURRENT_DOC
            && tierMatching != CompareConstants.MATCHING.SUFFIX
            && tierMatching != CompareConstants.MATCHING.PREFIX) {
            // nothing to do in this step so move on
            leaveStepBackward();
            multiPane.previousStep();
        }
    }


    @Override
    public boolean leaveStepForward() {
        if ((selFiles == null || selFiles.size() == 0) && transcription == null) {
            // warn the user? This shouldn't be possible actually
            LOG.warning("No files selected, cannot proceed to next step.");
            return false;
        }

        if (matchingOptionsPanel.isVisible() && customSepCB.isSelected()) {
            String separator = customSepTF.getText();
            if (separator == null || separator.length() == 0) {
                // warn the user
                LOG.warning("No custom separator specified for file matching, cannot proceed to next step.");
                showWarning(ElanLocale.getString("CompareAnnotatorsDialog.DocumentSelectionStep.Warning.NoSeparator"));
                customSepTF.requestFocus();
                return false;
            }
        }
        if (tierMatchingOptionsPanel.isVisible() && tierCustomSepCB.isSelected()) {
            String separator = tierCustomSepTF.getText();
            if (separator == null || separator.length() == 0) {
                // warn the user
                LOG.warning("No custom separator specified for tier matching, cannot proceed to next step.");
                showWarning(ElanLocale.getString("CompareAnnotatorsDialog.DocumentSelectionStep.Warning.NoTierSeparator"));
                tierCustomSepTF.requestFocus();
                return false;
            }
        }

        multiPane.putStepProperty(CompareConstants.SEL_FILES_KEY, selFiles);

        if (matchingOptionsPanel.isVisible()) {
            if (suffixMatchRB.isSelected()) {
                multiPane.putStepProperty(CompareConstants.FILE_MATCH_KEY, CompareConstants.MATCHING.SUFFIX);
            } else {
                multiPane.putStepProperty(CompareConstants.FILE_MATCH_KEY, CompareConstants.MATCHING.PREFIX);
            }
            if (customSepCB.isSelected()) {
                multiPane.putStepProperty(CompareConstants.FILE_SEPARATOR_KEY, customSepTF.getText());
            } else {
                multiPane.putStepProperty(CompareConstants.FILE_SEPARATOR_KEY, null); // reset to be sure
            }
        } else { //reset some properties?
            multiPane.putStepProperty(CompareConstants.FILE_MATCH_KEY, null); // or set suffix as default
            multiPane.putStepProperty(CompareConstants.FILE_SEPARATOR_KEY, null);
        }

        if (tierMatchingOptionsPanel.isVisible()) {
            if (tierSuffixMatchRB.isSelected()) {
                multiPane.putStepProperty(CompareConstants.TIER_MATCH_KEY,
                                          CompareConstants.MATCHING.SUFFIX); // replaces AFFIX
            } else {
                multiPane.putStepProperty(CompareConstants.TIER_MATCH_KEY,
                                          CompareConstants.MATCHING.PREFIX); // replaces AFFIX
            }
            if (tierCustomSepCB.isSelected()) {
                multiPane.putStepProperty(CompareConstants.TIER_SEPARATOR_KEY, tierCustomSepTF.getText());
            } else {
                multiPane.putStepProperty(CompareConstants.TIER_SEPARATOR_KEY, null); // reset
            }
        } else { //reset?
            multiPane.putStepProperty(CompareConstants.TIER_SEPARATOR_KEY, null);
        }

        if (exportTierPanel.isVisible()) {
            if (exportCombineTiersCB.isSelected()) {
                multiPane.putStepProperty(CompareConstants.EXPORT_MATCHING_TIERS_KEY, Boolean.TRUE);
                if (exportFilePerSetOfTiersRB.isSelected()) {
                    multiPane.putStepProperty(CompareConstants.EXPORT_TIERS_METHOD_KEY,
                                              CompareConstants.EXPORT_TIERS_PERTIERSET);
                } else {
                    multiPane.putStepProperty(CompareConstants.EXPORT_TIERS_METHOD_KEY,
                                              CompareConstants.EXPORT_TIERS_PERFILE);
                }
                multiPane.putStepProperty(CompareConstants.EXPORT_FOLDER_KEY, exportFolderTF.getText().strip());
            } else {
                multiPane.putStepProperty(CompareConstants.EXPORT_MATCHING_TIERS_KEY, Boolean.FALSE);
            }
        } else {
            multiPane.putStepProperty(CompareConstants.EXPORT_MATCHING_TIERS_KEY, Boolean.FALSE);
        }

        storePreferences();
        return true;
    }

    private void storePreferences() {
        if (filesFromDomainRB.isSelected()) {
            Preferences.set(prefFileSelection, "Domain", null);
        } else if (selectedFilesFromDiskRB.isSelected()) {
            Preferences.set(prefFileSelection, "Browse", null);
        }// current document dealt with separately

        if (matchingOptionsPanel.isVisible()) {
            CompareConstants.MATCHING curMatching =
                (CompareConstants.MATCHING) multiPane.getStepProperty(CompareConstants.FILE_MATCH_KEY);
            if (curMatching != null) {
                Preferences.set(prefFileAffixType, curMatching.getValue(), null);
            }
            Preferences.set(prefFileCustomSep, Boolean.valueOf(customSepCB.isSelected()), null);
            Preferences.set(CompareConstants.FILE_SEPARATOR_KEY, customSepTF.getText(), null);
        }

        if (tierMatchingOptionsPanel.isVisible()) {
            if (tierSuffixMatchRB.isSelected()) {
                Preferences.set(prefTierAffixType, CompareConstants.MATCHING.SUFFIX.getValue(), null);
            } else {
                Preferences.set(prefTierAffixType, CompareConstants.MATCHING.PREFIX.getValue(), null);
            }
            Preferences.set(prefTierCustomSep, Boolean.valueOf(tierCustomSepCB.isSelected()), null);
            Preferences.set(CompareConstants.TIER_SEPARATOR_KEY, tierCustomSepTF.getText(), null);
        }
        // tier export
        if (exportTierPanel.isVisible()) {
            Preferences.set(prefExportMatchingTiers, Boolean.valueOf(exportCombineTiersCB.isSelected()), null);
            Preferences.set(prefExportPerTierSet, exportFilePerSetOfTiersRB.isSelected(), null);
            Preferences.set(prefExportTiersFolder, exportFolderTF.getText().strip(), null);
        }

    }

    private void loadPreferences() {
        String fileSelPref = Preferences.getString(prefFileSelection, null);
        if ("Domain".equals(fileSelPref)) { // Browse is default
            filesFromDomainRB.setSelected(true);
            selectDomainBtn.setEnabled(true);
            selectFilesBtn.setEnabled(false);
        }
        String fileMatchType = Preferences.getString(prefFileAffixType, null);
        // suffix is default
        if (CompareConstants.MATCHING.PREFIX.getValue().equals(fileMatchType)) {
            prefixMatchRB.setSelected(true);
        }
        Boolean customFileSep = Preferences.getBool(prefFileCustomSep, null);
        if (customFileSep != null) {
            customSepCB.setSelected(customFileSep); // will this take care of enabling/disabling of the text field
        }
        String stringPref = Preferences.getString(CompareConstants.FILE_SEPARATOR_KEY, null);
        if (stringPref != null) {
            customSepTF.setText(stringPref);
        }
        // tier preferences
        String tierPref = Preferences.getString(prefTierAffixType, null);
        if (CompareConstants.MATCHING.PREFIX.getValue().equals(tierPref)) { // suffix default
            tierPrefixMatchRB.setSelected(true);
        }
        Boolean boolPref = Preferences.getBool(prefTierCustomSep, null);
        if (boolPref != null) {
            tierCustomSepCB.setSelected(boolPref);
        }
        stringPref = Preferences.getString(CompareConstants.TIER_SEPARATOR_KEY, null);
        if (stringPref != null) {
            tierCustomSepTF.setText(stringPref);
        }
        // tier export
        boolPref = Preferences.getBool(prefExportMatchingTiers, null);
        if (boolPref != null) {
            exportCombineTiersCB.setSelected(boolPref);
        }
        boolPref = Preferences.getBool(prefExportPerTierSet, null);
        if (boolPref != null) {
            if (boolPref) {
                exportFilePerSetOfTiersRB.setSelected(true); // default
            } else {
                exportFilePerSetOfFilesRB.setSelected(true);
            }
        }
        stringPref = Preferences.getString(prefExportTiersFolder, null);
        if (stringPref != null) {
            exportFolderTF.setText(stringPref);
        }

        updateExportPanel();
    }

    @Override
    public String getStepTitle() {
        return ElanLocale.getString("CompareAnnotatorsDialog.FilesSelectionStep.Title");
    }

    @Override
    public String getPreferredNextStep() {
        return "Tiers";
    }

    @Override
    public String getPreferredPreviousStep() {
        return "Document";
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == customSepCB) {
            customSepTF.setEnabled(customSepCB.isSelected());
        } else if (e.getSource() == tierCustomSepCB) {
            tierCustomSepTF.setEnabled(tierCustomSepCB.isSelected());
        } else if (e.getSource() == exportCombineTiersCB) {
            updateExportPanel();
        }
    }

    /**
     * Button action handler.
     */
    private class BrowseHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == exportFolderBrowseBT) {
                FileChooser chooser = new FileChooser(FilesSelectionStep.this);
                chooser.setCurrentDirectory(exportFolder);
                chooser.createAndShowFileDialog(ElanLocale.getString("Button.Browse"),
                                                FileChooser.OPEN_DIALOG,
                                                ElanLocale.getString("Button.Select"),
                                                null,
                                                null,
                                                false,
                                                null,
                                                FileChooser.DIRECTORIES_ONLY,
                                                null);
                File f = chooser.getSelectedFile();
                if (f != null) {
                    exportFolder = f.getAbsolutePath();
                    exportFolderTF.setText(exportFolder);
                }
            }
        }
    }
}
