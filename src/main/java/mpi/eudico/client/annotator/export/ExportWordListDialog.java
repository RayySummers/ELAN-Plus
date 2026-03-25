package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.ReportDialog;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.wordlist.Transcription2WordList;
import mpi.eudico.server.corpora.clomimpl.wordlist.WordListEncoderInfo;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.SimpleReport;
import nl.mpi.util.FileExtension;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel.BY_TIER;


/**
 * Tier selection dialog for export of a word list, either from a single file or from multiple files.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ExportWordListDialog extends AbstractExtTierExportDialog implements ChangeListener,
                                                                                 ItemListener {

    private JRadioButton customDelimRB;
    private JLabel tokenDelimLabel;
    private JRadioButton defaultDelimRB;
    private JTextField customDelimField;
    private ButtonGroup delimButtonGroup;
    private JCheckBox countTokensCB;
    private JCheckBox includeTotalsCB;
    private JCheckBox includeFreqPercentCB;

    private int mode = WordListEncoderInfo.WORDS;

    /**
     * Constructor for single file.
     *
     * @param parent the parent frame
     * @param modal the modal flag
     * @param transcription the (single) transcription
     */
    public ExportWordListDialog(Frame parent, boolean modal, TranscriptionImpl transcription) {
        super(parent, modal, transcription, null);
        makeLayout();
        extractTiers();
        postInit();
    }

    /**
     * Constructor for multiple files.
     *
     * @param parent the parent frame
     * @param modal the modal flag
     * @param files a list of eaf files
     */
    public ExportWordListDialog(Frame parent, boolean modal, List<File> files) {
        this(parent, modal, files, WordListEncoderInfo.WORDS);
    }

    /**
     * Constructor for multiple files.
     *
     * @param parent the parent frame
     * @param modal the modal flag
     * @param files a list of eaf files
     * @param mode the mode, WORDS or ANNOTATION export
     */
    public ExportWordListDialog(Frame parent, boolean modal, List<File> files, int mode) {
        super(parent, modal, files);
        this.files = files;
        if (mode == WordListEncoderInfo.ANNOTATIONS || mode == WordListEncoderInfo.WORDS) {
            this.mode = mode;
        }
        makeLayout();
        extractTiersFromFiles();
        postInit();
    }

    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {

        List<String> stringsPref = Preferences.getListOfString("ExportWordListDialog.TierOrder", transcription);
        if (stringsPref != null) {
            setTierOrder(stringsPref);
        } else {
            super.extractTiers(false);
        }

        stringsPref = Preferences.getListOfString("ExportWordListDialog.selectedTiers", transcription);
        if (stringsPref != null) {
            //loadTierPreferences(useTyp);
            setSelectedTiers(stringsPref);
        }

        String stringPref = Preferences.getString("ExportWordListDialog.SelectTiersMode", transcription);
        if (stringPref != null) {
            //List list = (List) Preferences.get("ExportWordListDialog.HiddenTiers", transcription);
            //setSelectedMode(string, list);
            setSelectionMode(stringPref);

            if (!BY_TIER.equals(stringPref)) {
                // call this after! the mode has been set
                List<String> selItems = Preferences.getListOfString("ExportWordListDialog.LastSelectedItems", transcription);

                if (selItems != null) {
                    setSelectedItems(selItems);
                }
            }
        }

        // use previous preference settings, if available
        if (stringPref == null) {
            Boolean boolPref = Preferences.getBool("ExportWordListDialog.tierRB", null);
            if (boolPref != null) {
                if (boolPref) {
                    setSelectionMode(BY_TIER);
                } else {
                    setSelectionMode(AbstractTierSortAndSelectPanel.BY_TYPE);
                }
            } else {
                boolPref = Preferences.getBool("ExportWordListDialog.typeRB", null);
                if (boolPref != null) {
                    if (boolPref) {
                        setSelectionMode(AbstractTierSortAndSelectPanel.BY_TYPE);
                    } else {
                        setSelectionMode(BY_TIER);
                    }
                }
            }
        }
    }

    /**
     * Restore some multiple file preferences.
     */
    @Override
    protected void extractTiersFromFiles() {
        super.extractTiersFromFiles();
        // in multiple file mode transcription is null and global setting will be used
        String stringPref = Preferences.getString("ExportWordListDialog.SelectTiersMode", null);
        if (stringPref != null) {
            //List list = (List) Preferences.get("ExportWordListDialog.HiddenTiers", transcription);
            //setSelectedMode((String)useTyp, list);
            setSelectionMode(stringPref);
        }

        // use previous preference settings, if available
        if (stringPref == null) {
            Boolean boolPref = Preferences.getBool("ExportWordListDialog.tierRB", null);
            if (boolPref != null) {
                if (boolPref) {
                    setSelectionMode(BY_TIER);
                } else {
                    setSelectionMode(AbstractTierSortAndSelectPanel.BY_TYPE);
                }
            } else {
                boolPref = Preferences.getBool("ExportWordListDialog.typeRB", null);
                if (boolPref != null) {
                    if (boolPref) {
                        setSelectionMode(AbstractTierSortAndSelectPanel.BY_TYPE);
                    } else {
                        setSelectionMode(BY_TIER);
                    }
                }
            }
        }
    }

    /**
     * Calls the super implementation and sets some properties of the tier table.
     */
    @Override
    protected void makeLayout() {
        super.makeLayout();

        countTokensCB = new JCheckBox();
        includeTotalsCB = new JCheckBox();
        includeFreqPercentCB = new JCheckBox();
        int county;

        // options
        if (mode == WordListEncoderInfo.WORDS) {
            delimButtonGroup = new ButtonGroup();
            tokenDelimLabel = new JLabel();
            defaultDelimRB = new JRadioButton();
            customDelimRB = new JRadioButton();
            customDelimField = new JTextField();

            GridBagConstraints gridBagConstraints;
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = insets;
            optionsPanel.add(tokenDelimLabel, gridBagConstraints);

            defaultDelimRB.setSelected(true);
            defaultDelimRB.addChangeListener(this);
            delimButtonGroup.add(defaultDelimRB);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(defaultDelimRB, gridBagConstraints);

            customDelimRB.addChangeListener(this);
            delimButtonGroup.add(customDelimRB);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(customDelimRB, gridBagConstraints);

            customDelimField.setEnabled(false);
            customDelimField.setColumns(6);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(customDelimField, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(12, 6, 4, 6);
            optionsPanel.add(countTokensCB, gridBagConstraints);
            county = gridBagConstraints.gridy;

        } else {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = insets;
            optionsPanel.add(countTokensCB, gridBagConstraints);
            county = gridBagConstraints.gridy;
            //getContentPane().remove(optionsPanel);
        }

        int indent = 26;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(4, indent, 4, 6);
        gridBagConstraints.gridy = county + 1;
        optionsPanel.add(includeTotalsCB, gridBagConstraints);

        gridBagConstraints.gridy++;
        optionsPanel.add(includeFreqPercentCB, gridBagConstraints);
        countTokensCB.addItemListener(this);

        setPreferredSetting();
        updateLocale();

    }

    /**
     * Applies localized text values ui elements.
     */
    @Override
    protected void updateLocale() {
        super.updateLocale();
        titleLabel.setText(ElanLocale.getString("ExportDialog.WordList.Title"));

        if (mode == WordListEncoderInfo.WORDS) {
            titleLabel.setText(ElanLocale.getString("ExportDialog.WordList.Title"));
            tokenDelimLabel.setText(ElanLocale.getString("TokenizeDialog.Label.TokenDelimiter"));
            defaultDelimRB.setText(ElanLocale.getString("Button.Default") + "( . , ! ? \" ' )");
            customDelimRB.setText(ElanLocale.getString("TokenizeDialog.RadioButton.Custom"));
        } else if (mode == WordListEncoderInfo.ANNOTATIONS) {
            titleLabel.setText(ElanLocale.getString("ExportDialog.AnnotationList.Title"));
        }
        countTokensCB.setText(ElanLocale.getString("ExportDialog.WordList.CountOccur"));
        includeTotalsCB.setText(ElanLocale.getString("ExportDialog.WordList.IncludeCounts"));
        includeFreqPercentCB.setText(ElanLocale.getString("ExportDialog.WordList.IncludeFreqs"));
    }

    /**
     * @see mpi.eudico.client.annotator.export.AbstractBasicExportDialog#startExport()
     */
    @Override
    protected boolean startExport() throws
                                    IOException {
        List<String> selectedTiers = getSelectedTiers();
        savePreferences();

        if (selectedTiers.size() == 0) {
            JOptionPane.showMessageDialog(this,
                                          ElanLocale.getString("ExportTradTranscript.Message.NoTiers"),
                                          ElanLocale.getString("Message.Warning"),
                                          JOptionPane.WARNING_MESSAGE);

            return false;
        }

        // prompt for file name and location
        File exportFile = null;
        if (mode == WordListEncoderInfo.WORDS) {
            exportFile =
                promptForFile(ElanLocale.getString("ExportDialog.WordList.Title"), null, FileExtension.TEXT_EXT, true);
        } else if (mode == WordListEncoderInfo.ANNOTATIONS) {
            exportFile =
                promptForFile(ElanLocale.getString("ExportDialog.AnnotationList.Title"), null, FileExtension.TEXT_EXT, true);
        }

        if (exportFile == null) {
            return false;
        }

        String delimiters = null;
        if (mode == WordListEncoderInfo.WORDS && customDelimRB.isSelected()) {
            delimiters = customDelimField.getText();
        }
        boolean countOccurrences = countTokensCB.isSelected();

        WordListEncoderInfo encoderInfo = new WordListEncoderInfo();
        encoderInfo.setCountOccurrences(countOccurrences);
        if (countOccurrences) {
            encoderInfo.setIncludeCounts(includeTotalsCB.isSelected());
            encoderInfo.setIncludeFreqPercent(includeFreqPercentCB.isSelected());
        }
        encoderInfo.setDelimiters(delimiters);
        encoderInfo.setExportFile(exportFile);
        encoderInfo.setSelectedTiers(selectedTiers);
        if (mode == WordListEncoderInfo.ANNOTATIONS) {
            encoderInfo.setDelimiters("");
        }
        encoderInfo.setExportMode(mode);
        encoderInfo.setEncoding(encoding);

        Transcription2WordList twl = new Transcription2WordList();
        twl.setProcessReport(new SimpleReport());

        try {
            if (transcription != null) {
                twl.encodeAndSave(transcription, encoderInfo, null, null);
            } else {
                twl.encodeAndSave(files, encoderInfo);
            }
            ProcessReport report = twl.getProcessReport();
            if (report != null) {
                ReportDialog rd = new ReportDialog(this, report);
                rd.setModal(true);
                rd.setVisible(true);
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this,
                                          ElanLocale.getString("ExportDialog.Message.Error"),
                                          ElanLocale.getString("Message.Error"),
                                          JOptionPane.WARNING_MESSAGE);

            return false;
        }

        return true;
    }

    /**
     * The state changed event handling.
     *
     * @param ce the change event
     */
    @Override
    public void stateChanged(ChangeEvent ce) {
        if (ce.getSource() == defaultDelimRB || ce.getSource() == customDelimField) {
            if (defaultDelimRB.isSelected()) {
                customDelimField.setEnabled(false);
            } else {
                customDelimField.setEnabled(true);
                customDelimField.requestFocus();
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == countTokensCB) {
            updateBoxes();
        }
    }

    private void updateBoxes() {
        includeTotalsCB.setEnabled(countTokensCB.isSelected());
        includeFreqPercentCB.setEnabled(countTokensCB.isSelected());
    }

    /**
     * Initializes the dialogBox with the last preferred/used settings
     */
    private void setPreferredSetting() {
        if (mode == WordListEncoderInfo.WORDS) {
            Boolean boolPref = Preferences.getBool("ExportWordListDialog.customDelimRB", null);
            if (boolPref != null) {
                customDelimRB.setSelected(boolPref);
            }

            boolPref = Preferences.getBool("ExportWordListDialog.defaultDelimRB", null);
            if (boolPref != null) {
                defaultDelimRB.setSelected(boolPref);
            }

            boolPref = Preferences.getBool("ExportWordListDialog.customDelimField", null);
            if (boolPref != null) {
                customDelimField.setText(boolPref.toString());
            }

            boolPref = Preferences.getBool("ExportWordListDialog.includeTotalsCB", null);
            if (boolPref != null) {
                includeTotalsCB.setSelected(boolPref);
            }

            boolPref = Preferences.getBool("ExportWordListDialog.includeFreqPercCB", null);
            if (boolPref != null) {
                includeFreqPercentCB.setSelected(boolPref);
            }

            boolPref = Preferences.getBool("ExportWordListDialog.countTokensCB", null);
            if (boolPref != null) {
                countTokensCB.setSelected(boolPref);
            }

        } else {
            Boolean boolPref = Preferences.getBool("ExportAnnotationListDialog.includeTotalsCB", null);
            if (boolPref != null) {
                includeTotalsCB.setSelected(boolPref);
            }

            boolPref = Preferences.getBool("ExportAnnotationListDialog.includeFreqPercCB", null);
            if (boolPref != null) {
                includeFreqPercentCB.setSelected(boolPref);
            }

            boolPref = Preferences.getBool("ExportAnnotationListDialog.countTokensCB", null);
            if (boolPref != null) {
                countTokensCB.setSelected(boolPref);
            }
        }
        updateBoxes();
    }

    /**
     * Saves the preferred/used settings.
     */
    private void savePreferences() {
        if (mode == WordListEncoderInfo.WORDS) {
            Preferences.set("ExportWordListDialog.customDelimRB", customDelimRB.isSelected(), null);
            Preferences.set("ExportWordListDialog.defaultDelimRB", defaultDelimRB.isSelected(), null);
            Preferences.set("ExportWordListDialog.countTokensCB", countTokensCB.isSelected(), null);
            Preferences.set("ExportWordListDialog.includeTotalsCB", includeTotalsCB.isSelected(), null);
            Preferences.set("ExportWordListDialog.includeFreqPercCB", includeFreqPercentCB.isSelected(), null);

            if (customDelimField.getText() != null) {
                Preferences.set("ExportWordListDialog.customDelimField", customDelimField.getText(), null);
            }

            if (!multipleFileExport) {
                Preferences.set("ExportWordListDialog.selectedTiers", getSelectedTiers(), transcription);
                Preferences.set("ExportWordListDialog.SelectTiersMode", getSelectionMode(), transcription);
                Preferences.set("ExportWordListDialog.HiddenTiers", getHiddenTiers(), transcription);
                // save the selected list in case on non-tier tab
                if (!BY_TIER.equals(getSelectionMode())) {
                    Preferences.set("ExportWordListDialog.LastSelectedItems", getSelectedItems(), transcription);
                }
                List<String> tierOrder = getTierOrder();
                Preferences.set("ExportWordListDialog.TierOrder", tierOrder, transcription);
                /*
                List currentTierOrder = getCurrentTierOrder();
                for(int i=0; i< currentTierOrder.size(); i++){
                    if(currentTierOrder.get(i) != tierOrder.get(i)){
                        Preferences.set("ExportWordListDialog.TierOrder", currentTierOrder, transcription);
                        break;
                    }
                }
                */
            } else {
                Preferences.set("ExportWordListDialog.SelectTiersMode", getSelectionMode(), null);
            }
        } else {
            Preferences.set("ExportAnnotationListDialog.countTokensCB", countTokensCB.isSelected(), null);
            Preferences.set("ExportAnnotationListDialog.includeTotalsCB", includeTotalsCB.isSelected(), null);
            Preferences.set("ExportAnnotationListDialog.includeFreqPercCB", includeFreqPercentCB.isSelected(), null);
        }
    }
}
