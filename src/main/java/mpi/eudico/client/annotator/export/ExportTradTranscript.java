package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.TimeFormatter;
import mpi.eudico.util.TimeRelation;
import nl.mpi.util.FileExtension;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;

import static mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel.BY_TIER;
import static mpi.eudico.client.annotator.util.IOUtil.getOutputStreamWriter;


/**
 * An export dialog for exporting tiers in a 'traditional' transcription style. This class will probably be obsolete by the
 * time the full-featured text export  function is fully implemented.
 *
 * @author Han Sloetjes
 * @author Allan van Hulst
 */
@SuppressWarnings("serial")
public class ExportTradTranscript extends AbstractExtTierExportDialog implements ItemListener {

    private final String TIME_SEP = " - ";       /* Separation between time indicators */
    private final char NL_CHAR = '\n';           /* Newline character */
    private final char SPACE_CHAR = ' ';         /* White space */
    private final int LABEL_VALUE_MARGIN = 3;    /* Default margin */
    private final int NUM_CHARS = 80;            /* Default line width */
    private final int LABEL_WIDTH = 5;           /* Default tier label width */
    private final int MERGE_DUR = 50;            /* Default merge duration limit */
    private final int MIN_SILENCE = 20;          /* Default minimal silence */

    private JCheckBox tierLabelsCB;
    private JCheckBox participantLabelsCB;
    private JCheckBox suppressRepeatedLabelsCB;
    private JCheckBox labelWidthCB;
    private JCheckBox selectionCB;
    private JCheckBox timeCodeCB;
    private JCheckBox silenceCB;
    private JCheckBox wrapLinesCB;
    private JCheckBox emptyLineCB;
    private JCheckBox numberAnnotationsCB;
    private JCheckBox numberLinesCB;
    private JCheckBox mergeAnnCB;
    private JCheckBox useJeffersonCB;

    private JTextField minDurSilTF;
    private JTextField labelWidthTF;
    private JTextField numCharTF;
    private JTextField mergeDurTF;

    private JLabel minDurSilLabel;
    private JLabel silDecimalLabel;
    private JLabel charPerLineLabel;
    private JLabel mergeDurLabel;

    private JComboBox silenceDecimalComboBox;

    /**
     * Common variables for the formatting of the output
     */
    private boolean includeTimeCodes;
    private boolean includeLabels;
    private boolean suppressRepeatedLabels;

    /**
     * Two mappings: tier names to pre-formatted strings and tier names to non-truncated first mappings
     */
    private Map<String, String> marginStrings;
    private Map<String, String> untruncatedLabels;

    private boolean includeSilence;

    /**
     * Use unique key that won't clash with a tier name and maps to some spaces.
     */
    private static final String EMPTY_LABEL = "EMPTY_LABEL-Beic1quepeev7ov1uDi1ohbeeyaebein";

    /**
     * Use unique key that won't clash with a tier name. If lines are not numbered, this is equivalent to  EMPTY_LABEL.
     */
    private static final String NUMBERED_EMPTY_LABEL = "NUMBERED_EMPTY_LABEL-vaib2agaud9shaeg8Lai7ohvoht6ut8w";

    /**
     * Use unique key that won't clash with a tier name
     */
    private static final String TC_LABEL = "TC-ieh8Ku0saewi0Au0oash6eileij8Yeit";

    private boolean insertEmptyLine;
    private boolean wrapLines;
    private boolean numberAnnotations;

    /**
     * This value is only relevant if wrapLines &amp;&amp; numberAnnotations is true
     */
    private boolean numberAnnotationLines;
    private String numberFieldSpaces = "";
    private String numberFieldFormat = "%4d ";
    private int lineNumber;
    private boolean useParticipantLabel;
    private boolean useJefferson;

    /**
     * Constructor.
     *
     * @param parent parent frame
     * @param modal the modal/blocking attribute
     * @param transcription the transcription to export from
     * @param selection the current selection
     */
    public ExportTradTranscript(Frame parent, boolean modal, TranscriptionImpl transcription, Selection selection) {
        super(parent, modal, transcription, selection);

        makeLayout();
        postInit();
    }

    /**
     * The item state changed handling.
     *
     * @param ie the ItemEvent
     */
    @Override
    public void itemStateChanged(ItemEvent ie) {
        final Object source = ie.getSource();
        if (source == wrapLinesCB) {
            if (wrapLinesCB.isSelected()) {
                numCharTF.setEnabled(true);
                numCharTF.setBackground(Constants.SHAREDCOLOR4);

                if ((numCharTF.getText() != null) || (numCharTF.getText().length() == 0)) {
                    numCharTF.setText("" + NUM_CHARS);
                }

                numCharTF.requestFocus();
            } else {
                numCharTF.setEnabled(false);
                numCharTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            }
        } else if (source == silenceCB) {
            if (silenceCB.isSelected()) {
                minDurSilTF.setEnabled(true);
                minDurSilTF.setBackground(Constants.SHAREDCOLOR4);
                silenceDecimalComboBox.setEnabled(true);
                if (minDurSilTF.getText() == null || minDurSilTF.getText().isEmpty()) {
                    minDurSilTF.setText("" + MIN_SILENCE);
                }

                minDurSilTF.requestFocus();
            } else {
                minDurSilTF.setEnabled(false);
                silenceDecimalComboBox.setEnabled(false);
                minDurSilTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            }
        } else if (source == tierLabelsCB || source == participantLabelsCB) {
            final boolean tierSelected = tierLabelsCB.isSelected();
            final boolean participantSelected = participantLabelsCB.isSelected();

            /* Do mutual exclusion; a ButtonGroup won't let us de-select both of them. */
            if (((JCheckBox) source).isSelected()) {
                JCheckBox other = (source == tierLabelsCB ? participantLabelsCB : tierLabelsCB);
                other.setSelected(false);
            }
            final boolean enableSubOptions = (tierSelected || participantSelected);
            labelWidthCB.setEnabled(enableSubOptions);
            suppressRepeatedLabelsCB.setEnabled(enableSubOptions);
            if (enableSubOptions) {
                if (labelWidthCB.isSelected()) {
                    labelWidthTF.setEnabled(true);
                    labelWidthTF.setBackground(Constants.SHAREDCOLOR4);
                    if (labelWidthTF.getText() == null || labelWidthTF.getText().isEmpty()) {
                        labelWidthTF.setText("" + LABEL_WIDTH);
                    }
                    labelWidthTF.requestFocus();
                } else {
                    labelWidthTF.setEnabled(false);
                    labelWidthTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
                }
            } else {
                labelWidthTF.setEnabled(false);
                labelWidthTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            }
        } else if (source == labelWidthCB) {
            if (labelWidthCB.isSelected() && labelWidthCB.isEnabled()) {
                labelWidthTF.setEnabled(true);
                labelWidthTF.setBackground(Constants.SHAREDCOLOR4);

                if (labelWidthTF.getText() == null || labelWidthTF.getText().isEmpty()) {
                    labelWidthTF.setText("" + LABEL_WIDTH);
                }

                labelWidthTF.requestFocus();
            } else {
                labelWidthTF.setEnabled(false);
                labelWidthTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            }
        } else if (source == mergeAnnCB) {
            if (mergeAnnCB.isSelected()) {
                mergeDurTF.setEnabled(true);
                mergeDurTF.setBackground(Constants.SHAREDCOLOR4);
                if (mergeDurTF.getText() == null || mergeDurTF.getText().isEmpty()) {
                    mergeDurTF.setText("" + MERGE_DUR);
                }

                mergeDurTF.requestFocus();
            } else {
                mergeDurTF.setEnabled(false);
                mergeDurTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            }
        }

        if (source == wrapLinesCB || source == numberAnnotationsCB) {
            numberLinesCB.setEnabled(wrapLinesCB.isSelected() && numberAnnotationsCB.isSelected());
        }
    }

    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
        List<String> stringsPref = Preferences.getListOfString("ExportTradTranscript.TierOrder", transcription);

        if (stringsPref != null) {
            setTierOrder(stringsPref);
        } else {
            super.extractTiers(true);
        }

        setSelectedTiers(Preferences.getListOfString("ExportTradTranscript.selectedTiers", transcription));

        String stringPref = Preferences.getString("ExportTradTranscript.SelectTiersMode", transcription);
        if (stringPref != null) {
            setSelectionMode(stringPref);

            if (!BY_TIER.equals(stringPref)) {
                List<String> selItems = Preferences.getListOfString("ExportTradTranscript.LastSelectedItems", transcription);

                if (selItems != null) {
                    setSelectedItems(selItems);
                }
            }
        }
    }

    /**
     * Initializes UI elements. The text values for the various controls created at the initial part of this method will be
     * initialized and updated using the updateLocale method.
     */
    @Override
    protected void makeLayout() {
        super.makeLayout();

        /* Construct labels */
        charPerLineLabel = new JLabel();
        minDurSilLabel = new JLabel();
        silDecimalLabel = new JLabel();
        mergeDurLabel = new JLabel();

        /* Construct checkboxes */
        wrapLinesCB = new JCheckBox();
        timeCodeCB = new JCheckBox();
        silenceCB = new JCheckBox();
        tierLabelsCB = new JCheckBox();
        labelWidthCB = new JCheckBox();
        participantLabelsCB = new JCheckBox();
        suppressRepeatedLabelsCB = new JCheckBox();
        selectionCB = new JCheckBox();
        emptyLineCB = new JCheckBox();
        mergeAnnCB = new JCheckBox();
        numberAnnotationsCB = new JCheckBox();
        numberLinesCB = new JCheckBox();
        useJeffersonCB = new JCheckBox();

        /* Construct textfields */
        numCharTF = new JTextField(4);
        minDurSilTF = new JTextField(4);
        labelWidthTF = new JTextField(4);
        mergeDurTF = new JTextField(4);

        /* Combobox for {1,2,3}: number of decimal points in silence duration */
        silenceDecimalComboBox = new JComboBox();
        silenceDecimalComboBox.addItem(Constants.ONE_DIGIT);
        silenceDecimalComboBox.addItem(Constants.TWO_DIGIT);
        silenceDecimalComboBox.addItem(Constants.THREE_DIGIT);
        silenceDecimalComboBox.setSelectedItem(Constants.TWO_DIGIT);

        GridBagConstraints gbc;

        optionsPanel.setLayout(new GridBagLayout());

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;

        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        optionsPanel.add(selectionCB, gbc);

        wrapLinesCB.addItemListener(this);
        gbc.gridy = gbc.gridy + 1;
        optionsPanel.add(wrapLinesCB, gbc);

        /* Fill one cell in column 0 with a spacer; that is sufficient. */
        JPanel fill = new JPanel();
        Dimension fillDim = new Dimension(30, 10);
        fill.setPreferredSize(fillDim);
        gbc.gridy = gbc.gridy + 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        optionsPanel.add(fill, gbc);

        numCharTF.setEnabled(false);
        numCharTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        gbc.gridx = 1;
        optionsPanel.add(numCharTF, gbc);

        gbc.gridx = 2;
        optionsPanel.add(charPerLineLabel, gbc);

        mergeAnnCB.addItemListener(this);
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridy = gbc.gridy + 1;
        optionsPanel.add(mergeAnnCB, gbc);


        mergeDurTF.setEnabled(false);
        mergeDurTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        gbc.gridy = gbc.gridy + 1;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        optionsPanel.add(mergeDurTF, gbc);

        gbc.gridx = 2;
        optionsPanel.add(mergeDurLabel, gbc);

        numberAnnotationsCB.addItemListener(this);
        gbc.gridy = gbc.gridy + 1;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        optionsPanel.add(horizontal(numberAnnotationsCB, numberLinesCB), gbc);

        tierLabelsCB.addItemListener(this);
        participantLabelsCB.addItemListener(this);

        gbc.gridy = gbc.gridy + 1;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        optionsPanel.add(horizontal(tierLabelsCB, participantLabelsCB), gbc);

        labelWidthCB.addItemListener(this);
        labelWidthCB.setEnabled(false);

        gbc.gridy = gbc.gridy + 1;
        gbc.gridx = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        optionsPanel.add(horizontal(labelWidthCB, labelWidthTF), gbc);

        gbc.gridy = gbc.gridy + 1;
        optionsPanel.add(suppressRepeatedLabelsCB, gbc);

        gbc.gridy = gbc.gridy + 1;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        optionsPanel.add(timeCodeCB, gbc);

        silenceCB.addItemListener(this);
        gbc.gridy = gbc.gridy + 1;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        optionsPanel.add(silenceCB, gbc);

        minDurSilTF.setEnabled(false);
        minDurSilTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);

        gbc.gridy = gbc.gridy + 1;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        optionsPanel.add(minDurSilTF, gbc);

        gbc.gridx = 2;
        optionsPanel.add(minDurSilLabel, gbc);

        gbc.gridy = gbc.gridy + 1;
        gbc.gridx = 1;
        optionsPanel.add(silenceDecimalComboBox, gbc);

        gbc.gridx = 2;
        optionsPanel.add(silDecimalLabel, gbc);

        gbc.gridy = gbc.gridy + 1;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        optionsPanel.add(emptyLineCB, gbc);

        gbc.gridy = gbc.gridy + 1;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        optionsPanel.add(useJeffersonCB, gbc);

        setPreferredSetting();
        updateLocale();
    }

    /**
     * Put multiple JComponents horizontally on a JPanel
     *
     * @param components The components
     *
     * @return A JPanel
     */
    private JPanel horizontal(JComponent... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;

        for (JComponent comp : components) {
            panel.add(comp, gbc);
        }

        return panel;
    }

    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    protected void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message, ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Starts the actual export after performing some checks.
     *
     * @return true if export succeeded, false otherwise
     *
     * @throws IOException if writing the file did not succeed
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

        /* check the chars per line value */
        int charsPerLine = Integer.MAX_VALUE;

        if (wrapLinesCB.isSelected()) {
            String textValue = numCharTF.getText().trim();

            try {
                charsPerLine = Integer.parseInt(textValue);
                if (charsPerLine <= 10) {
                    showWarningDialog(ElanLocale.getString("ExportTradTranscript.Message.InvalidNumber"));
                    numCharTF.selectAll();
                    numCharTF.requestFocus();
                    return false;
                }
            } catch (NumberFormatException nfe) {
                showWarningDialog(ElanLocale.getString("ExportTradTranscript.Message.InvalidNumber"));
                numCharTF.selectAll();
                numCharTF.requestFocus();

                return false;
            }
        }

        /* check the minimal silence duration */
        int labelWidth = LABEL_WIDTH;
        if ((tierLabelsCB.isSelected() || participantLabelsCB.isSelected()) && labelWidthCB.isSelected()) {
            String textValue = labelWidthTF.getText().trim();

            try {
                labelWidth = Integer.parseInt(textValue);
                if (labelWidth <= 0) {
                    showWarningDialog(ElanLocale.getString("ExportTradTranscript.Message.InvalidNumber3"));
                    labelWidthTF.selectAll();
                    labelWidthTF.requestFocus();
                    return false;
                }
            } catch (NumberFormatException nfe) {
                showWarningDialog(ElanLocale.getString("ExportTradTranscript.Message.InvalidNumber3"));
                labelWidthTF.selectAll();
                labelWidthTF.requestFocus();

                return false;
            }
        }

        /* check the merge duration */
        int mergeValue = MERGE_DUR;
        if (mergeAnnCB.isSelected()) {
            String textValue = mergeDurTF.getText().trim();

            try {
                mergeValue = Integer.parseInt(textValue);
                if (mergeValue <= 0) {
                    showWarningDialog(ElanLocale.getString("ExportTradTranscript.Message.InvalidNumber4"));
                    mergeDurTF.selectAll();
                    mergeDurTF.requestFocus();
                    return false;
                }
            } catch (NumberFormatException nfe) {
                showWarningDialog(ElanLocale.getString("ExportTradTranscript.Message.InvalidNumber4"));
                mergeDurTF.selectAll();
                mergeDurTF.requestFocus();

                return false;
            }
        }

        /* check the minimal silence duration */
        int minSilence = MIN_SILENCE;
        if (silenceCB.isSelected()) {
            String textValue = minDurSilTF.getText().trim();

            try {
                minSilence = Integer.parseInt(textValue);
                if (minSilence < 0) {
                    showWarningDialog(ElanLocale.getString("ExportTradTranscript.Message.InvalidNumber2"));
                    minDurSilTF.selectAll();
                    minDurSilTF.requestFocus();
                    return false;
                }
            } catch (NumberFormatException nfe) {
                showWarningDialog(ElanLocale.getString("ExportTradTranscript.Message.InvalidNumber2"));
                minDurSilTF.selectAll();
                minDurSilTF.requestFocus();

                return false;
            }
        }

        File exportFile =
            promptForFile(ElanLocale.getString("ExportTradTranscript.Title"), null, FileExtension.TEXT_EXT, true);
        if (exportFile == null) {
            return false;
        }

        return doExport(exportFile, selectedTiers, charsPerLine, minSilence, mergeValue, labelWidth);
    }

    /**
     * Applies localized strings to the ui elements.
     */
    @Override
    protected void updateLocale() {
        super.updateLocale();

        setTitle(ElanLocale.getString("ExportTradTranscript.Title"));
        titleLabel.setText(ElanLocale.getString("ExportTradTranscript.Title"));
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportDialog.Label.Options")));
        wrapLinesCB.setText(ElanLocale.getString("ExportTradTranscript.Label.WrapLines"));
        charPerLineLabel.setText(ElanLocale.getString("ExportTradTranscript.Label.NumberChars"));
        timeCodeCB.setText(ElanLocale.getString("ExportTradTranscript.Label.IncludeTimeCode"));
        tierLabelsCB.setText(ElanLocale.getString("ExportTradTranscript.Label.IncludeTierLabels"));
        participantLabelsCB.setText(ElanLocale.getString("ExportTradTranscript.Label.IncludeParticipantLabels"));
        suppressRepeatedLabelsCB.setText(ElanLocale.getString("ExportTradTranscript.Label.SuppressRepeatedLabels"));
        numberAnnotationsCB.setText(ElanLocale.getString("ExportTradTranscript.Label.NumberAnnos"));
        numberLinesCB.setText(ElanLocale.getString("ExportTradTranscript.Label.NumberLines"));
        silenceCB.setText(ElanLocale.getString("ExportTradTranscript.Label.IncludeSilence"));
        minDurSilLabel.setText(ElanLocale.getString("ExportTradTranscript.Label.MinSilenceDuration"));
        selectionCB.setText(ElanLocale.getString("ExportDialog.Restrict"));
        silDecimalLabel.setText(ElanLocale.getString("InterlinearizerOptionsDlg.NumberofDigits"));
        emptyLineCB.setText(ElanLocale.getString("ExportTradTranscript.Label.IncludeEmptyLines"));
        labelWidthCB.setText(ElanLocale.getString("ExportTradTranscript.Label.MaxLabelWidth"));
        mergeAnnCB.setText(ElanLocale.getString("ExportTradTranscript.Label.MergeAnnotations"));
        mergeDurLabel.setText(ElanLocale.getString("ExportTradTranscript.Label.MergeDuration"));
        useJeffersonCB.setText(ElanLocale.getString("ExportTradTranscript.Label.UseJefferson"));

        validate();
    }

    /**
     * Creates a label string of length numchars. A number of space characters will be added to the input string  to make it
     * the right length.
     *
     * <p>If the string is longer than fixedLabelWidth (which should be smaller than
     * numchars), then it is first truncated before being padded with spaces.
     *
     * @param name the input string
     * @param fixedLabelWidth the length of which that will be used for the name
     * @param numchars the new length (at least fixedLabelWidth)
     *
     * @return the input string with the right number of space characters added to it
     */
    private String getMarginString(String name, final int fixedLabelWidth, int numchars) {
        int nameLength = 0;

        if (name.length() > fixedLabelWidth) {
            name = name.substring(0, fixedLabelWidth);
        }

        nameLength = name.length();

        StringBuilder bf = new StringBuilder(name);

        for (int i = numchars - nameLength; i > 0; i--) {
            bf.append(SPACE_CHAR);
        }

        return bf.toString();
    }

    /**
     * Split a string into an array of substrings, each not longer than  the max annotation length.
     *
     * @param val the string to split
     * @param maxAnnotationLength the maximum length of the substrings
     *
     * @return an array of substrings
     */
    private String[] breakValue(String val, int maxAnnotationLength) {
        if (val == null) {
            return new String[] {};
        }

        if ((val.indexOf(SPACE_CHAR) < 0) || (val.length() < maxAnnotationLength)) {
            return new String[] {val};
        }

        List<String> vals = new ArrayList<String>();
        String sub = null;

        while (val.length() > maxAnnotationLength) {
            sub = val.substring(0, maxAnnotationLength);

            int breakSpace = sub.lastIndexOf(SPACE_CHAR);

            if (breakSpace < 0) {
                breakSpace = val.indexOf(SPACE_CHAR);

                if (breakSpace < 0) {
                    vals.add(val);

                    break;
                } else {
                    vals.add(val.substring(0, breakSpace)); /* Omit the one space at the end of the line. */
                    val = val.substring(breakSpace + 1);    /* If multiple spaces present, some remain.   */
                }
            } else {
                vals.add(sub.substring(0, breakSpace));     /* Omit the one space at the end of the line. */
                val = val.substring(breakSpace + 1);        /* If multiple spaces present, some remain.   */
            }

            if (val.length() <= maxAnnotationLength) {
                vals.add(val);

                break;
            }
        }

        return vals.toArray(new String[] {});
    }

    /**
     * The actual writing. If this class is to survive alot of the export stuff should go to another  class.
     *
     * @param fileName path to the file, not null
     * @param orderedTiers tier names, ordered by the user, min size 1
     * @param charsPerLine num of chars per line if linewrap is selected
     *
     * @return true if all went well, false otherwise
     */
    private boolean doExport(final File exportFile, final List<String> orderedTiers, final int charsPerLine,
                             final int minSilence, final int mergeValue, final int fixedLabelWidth) {

        boolean selectionOnly = selectionCB.isSelected();
        boolean mergeAnn = mergeAnnCB.isSelected();

        useJefferson = useJeffersonCB.isSelected();
        ArrayList<IndexedExportRecord> annotList = null;

        wrapLines = wrapLinesCB.isSelected();
        includeTimeCodes = timeCodeCB.isSelected();
        includeLabels = tierLabelsCB.isSelected() || participantLabelsCB.isSelected();
        useParticipantLabel = participantLabelsCB.isSelected();
        suppressRepeatedLabels = includeLabels && suppressRepeatedLabelsCB.isSelected();
        includeSilence = silenceCB.isSelected();
        insertEmptyLine = emptyLineCB.isSelected();
        numberAnnotations = numberAnnotationsCB.isSelected();
        numberAnnotationLines = wrapLines && numberAnnotations && numberLinesCB.isSelected();

        int labelMargin = 0;
        int labelWidth = 0;

        untruncatedLabels = null;
        marginStrings = null;
        lineNumber = 1;

        if (includeLabels) {
            untruncatedLabels = new HashMap<String, String>();
            marginStrings = new HashMap<String, String>();
            if (labelWidthCB.isSelected()) {
                labelWidth = fixedLabelWidth;
                labelMargin = labelWidth + LABEL_VALUE_MARGIN;
            } else {
                for (int i = 0; i < orderedTiers.size(); i++) {
                    String name = orderedTiers.get(i);

                    if (name.length() > labelMargin) {
                        labelMargin = name.length();
                    }
                }
                labelWidth = labelMargin;
                labelMargin += LABEL_VALUE_MARGIN;
            }

            for (int i = 0; i < orderedTiers.size(); i++) {
                String name = orderedTiers.get(i);
                String value;
                if (useParticipantLabel) {

                    /* Directly map the tier name to the participant */
                    Tier tier = transcription.getTierWithId(name);
                    value = tier.getParticipant();

                    /* If the participant is unknown, use the tier name anyway */
                    if (value == null || value.isEmpty()) {
                        value = name;
                    }
                } else {
                    value = name;
                }

                untruncatedLabels.put(name, value);
                marginStrings.put(name, getMarginString(value, labelWidth, labelMargin));
            }

            /* Add timecode label with alignment */
            if (includeTimeCodes) {
                marginStrings.put(TC_LABEL, getMarginString("", labelWidth, labelMargin));
            }

            /* Add empty labels with proper alignment */
            marginStrings.put(EMPTY_LABEL, getMarginString("", labelWidth, labelMargin));
            marginStrings.put(NUMBERED_EMPTY_LABEL, getMarginString("", labelWidth, labelMargin));
        }

        /* Begin boundary and end boundary times. */
        long bb = 0;
        long eb = Long.MAX_VALUE;

        if (selectionOnly && (selection != null)) {
            bb = selection.getBeginTime();
            eb = selection.getEndTime();
        }

        /* The parameters are now set, create an ordered set of Annotation records */
        TreeSet<IndexedExportRecord> records = new TreeSet<IndexedExportRecord>();

        for (int i = 0; i < orderedTiers.size(); i++) {
            String tierName = orderedTiers.get(i);

            Tier t = transcription.getTierWithId(tierName);

            if (t == null) {
                continue;
            }

            for (Annotation ann : t.getAnnotations()) {
                if (TimeRelation.overlaps(ann, bb, eb)) {
                    records.add(new IndexedExportRecord(this, ann, i));
                }

                if (ann.getBeginTimeBoundary() > eb) {
                    break;
                }
            }
        }

        /*
         *  If silence indicators should be part of the output, calculate them here.
         *
         *  If not, they stay all at the default value: -1.
         */
        if (includeSilence) {
            IndexedExportRecord prev = null;
            long prevEndTime = 0;

            /*
             *  Keep the maximum end time that we have seen so far and never let
             * it decrease. This corresponds with the idea that as long as we're
             * still overlapping any annotation, it is not silent.
             * So there will be adjacent annotations that have "no silence"
             * between them, even if there is some time between them.
             */
            for (IndexedExportRecord rec : records) {
                if (prev != null) {

                    /* set the "silence after" of record 1, note that the subtraction can be negative */
                    long dur = rec.getBeginTime() - prevEndTime;
                    if (dur >= minSilence) {
                        prev.setSilenceAfter(dur);
                    }
                }

                if (prevEndTime < rec.getEndTime()) {
                    prevEndTime = rec.getEndTime();
                }
                prev = rec;
            }
        }

        /*
         * Estimate how many line numbers we are going to need,
         * and from that the line number field width.
         */
        int numberFieldWidth;

        if (numberAnnotations) {
            int estimatedLineNumbers = 0;

            if (numberAnnotationLines) {
                int estimatedLineLength = charsPerLine - labelMargin - 10;
                for (IndexedExportRecord rec : records) {
                    int len = rec.getValue().length();

                    /* This is probably a slight over-estimation, which is good. */
                    estimatedLineNumbers += 1 + len / estimatedLineLength;
                }
            } else {
                estimatedLineNumbers = records.size();
            }
            int numDigits = (estimatedLineNumbers == 0) ? 1 : (1 + (int) Math.floor(Math.log10(estimatedLineNumbers)));

            /* Calculate format strings based on numDigits */
            numberFieldWidth = numDigits + 1;
            char[] spaces = new char[numberFieldWidth];
            Arrays.fill(spaces, ' ');
            numberFieldSpaces = new String(spaces);
            numberFieldFormat = "%0" + numDigits + "d ";
        } else {
            numberFieldWidth = 0;
        }

        /* Calculate more precisely how much space is left over for the actual annotations */
        int maxAnnotationLength = charsPerLine - labelMargin - numberFieldWidth;

        if (useJefferson) {
            indentList = new ArrayList<String>();
            indentBegin = new ArrayList<Long>();
            indentEnd = new ArrayList<Long>();
            indentApplied = new ArrayList<Integer>();
            indentRefers = new ArrayList<Integer>();

            for (int i = 0; i < records.size(); i = i + 1) {
                indentApplied.add(0);
                indentRefers.add(0);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(getOutputStreamWriter(encoding, new FileOutputStream(exportFile)))) {
            if (mergeAnn) {
                Iterator<IndexedExportRecord> recIter = records.iterator();

                if (recIter.hasNext()) {

                    /* Treat the first one separately */
                    IndexedExportRecord indexedExportRecord = recIter.next();
                    String val = indexedExportRecord.getValue();
                    IndexedExportRecord beginRecord = indexedExportRecord;

                    int index = 0;

                    /* Then loop over the rest */
                    while (recIter.hasNext()) {

                        IndexedExportRecord nextRec = recIter.next();
                        long silDur = indexedExportRecord.getSilenceAfter();

                        /*
                         *  Note that silDur is either -1 (no silence due to overlap, or too short, or ! includeSilence)
                         *
                         *  or at least minSilence.
                         */
                        if (indexedExportRecord.getTierName().equals(nextRec.getTierName())
                            && silDur >= 0
                            && silDur <= mergeValue) {

                            /*
                             *  Check for silence duration.
                             *
                             * Note that the constraints on silDur imply that if we get here,
                             * includeSilence is true!
                             */
                            if (includeSilence) {
                                val = val + " (" + formatSilenceString(silDur) + ")";
                            }

                            val = val + " " + nextRec.getValue();
                            indexedExportRecord = nextRec;
                        } else {

                            /* write the indexedExportRecord */
                            maybeWriteTierLabel(writer, indexedExportRecord);
                            writeAnnotationBlock(writer,
                                                 val,
                                                 maxAnnotationLength,
                                                 beginRecord.getBeginTime(),
                                                 indexedExportRecord.getEndTime());
                            writeTimeCodeSilenceBlock(writer, beginRecord, indexedExportRecord);

                            indexedExportRecord = nextRec;
                            val = indexedExportRecord.getValue();
                            beginRecord = indexedExportRecord;
                        }

                        index = index + 1;
                    }

                    maybeWriteTierLabel(writer, indexedExportRecord);
                    writeAnnotationBlock(writer,
                                         val,
                                         maxAnnotationLength,
                                         beginRecord.getBeginTime(),
                                         indexedExportRecord.getEndTime());
                    writeTimeCodeSilenceBlock(writer, beginRecord, indexedExportRecord);
                }
            } else {
                int index = 0;

                for (IndexedExportRecord indexedExportRecord : records) {
                    String val = indexedExportRecord.getValue();

                    maybeWriteTierLabel(writer, indexedExportRecord);
                    writeAnnotationBlock(writer,
                                         val,
                                         maxAnnotationLength,
                                         indexedExportRecord.getBeginTime(),
                                         indexedExportRecord.getEndTime());
                    writeTimeCodeSilenceBlock(writer, indexedExportRecord, indexedExportRecord);

                    index = index + 1;
                }
            }

            writer.flush();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                                          ElanLocale.getString("ExportDialog.Message.Error"),
                                          ElanLocale.getString("Message.Warning"),
                                          JOptionPane.WARNING_MESSAGE);
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Very simply method, return a String containing n spaces.
     *
     * @param n The number of spaces.
     *
     * @return A string containing n spaces.
     */
    private String addSpaces(int n) {
        String s = "";

        for (int i = 0; i < n; i = i + 1) {
            s = s + " ";
        }

        return s;
    }

    /**
     * Merge an array of strings by adding a newline at the end of each string. This allows us to store a result of the
     * breakValue-method used to compute word-wrapping exactly as it was written to the file.
     *
     * @param a An array of strings.
     *
     * @return A single string, the folded-back array.
     */
    private String mergeString(String[] a) {
        String s = "";

        for (int i = 0; i < a.length; i = i + 1) {
            s = s + a[i] + NL_CHAR;
        }

        return s;
    }

    ArrayList<String> indentList = null;
    ArrayList<Long> indentBegin = null;
    ArrayList<Long> indentEnd = null;
    ArrayList<Integer> indentApplied = null;
    ArrayList<Integer> indentRefers = null;

    /**
     * Find the nth index of the character '[' in string s, but reset the index every time we encounter a newline character.
     *
     * @param s The string to look into.
     * @param n Find the n'th character.
     *
     * @return The corresponding index, or zero otherwise.
     */
    private int findNthIndexOf(String s, int n) {
        int index = 0;

        for (int i = 0; i < s.length(); i = i + 1) {
            if (s.charAt(i) == '[') {
                if (n == 0) {
                    return index;
                } else {
                    n = n - 1;
                }
            }

            if (s.charAt(i) == NL_CHAR) {
                index = 0;
            } else {
                index++;
            }
        }

        return (-1);
    }

    /**
     * Compute the amount of indentation for Jefferson-style indentation.
     *
     * @param s The string value for which the amount of indentation should be computed.
     *
     * @return The amount of indentation that should be applied.
     */
    private int computeIndent(String s, long b, long e) {

        int indent = 0;

        /*
         * First check whether this string should be indented at all, this is
         * only the case if the first character is an opening square bracket.
         */
        if (s.length() == 0 || s.charAt(0) != '[') {
            return 0;
        }

        /* Check if this annotation overlaps with a previous annotation*/
        for (int i = 0; i < indentList.size(); i = i + 1) {
            if (indentBegin.get(i) <= b && b <= indentEnd.get(i)) {

                indent = findNthIndexOf(indentList.get(i), indentRefers.get(i));

                if (indent != -1) {
                    int refers = indentRefers.get(i);

                    indentRefers.set(i, refers + 1);

                    break;
                } else {
                    indent = 0;
                }
            }
        }

        /* No need to store anything if no match was found */
        if (indent == 0) {
            return 0;
        }

        /* Store the indent that was applied to this annotation */
        indentApplied.set(indentList.size(), indent);

        /* Set the number of refers for this annotation to 1 */
        indentRefers.set(indentList.size(), 1);

        return indent;
    }

    /**
     * Writes a block of text from an annotation. Newlines are printed as spaces. It wraps the text to multiple lines if
     * requested and needed. The optional label before it has already been written, but this method takes care of empty
     * labels on continuation lines, and the final newline.
     *
     * @param writer the output writer
     * @param val the value
     * @param maxAnnotationLength the maximum length
     * @param beginTime the begin time
     * @param endTime the end time
     *
     * @throws IOException any IO exception
     */
    private void writeAnnotationBlock(BufferedWriter writer, String val, int maxAnnotationLength, long beginTime,
                                      long endTime) throws
                                                    IOException {
        val = val.replace(NL_CHAR, SPACE_CHAR);

        if (useJefferson) {
            val = addSpaces(computeIndent(val, beginTime, endTime)) + val;
        }

        if (!wrapLines || val.length() <= maxAnnotationLength) {

            writer.write(val);
            writer.newLine();

            /* Add the string value as it was written to the file */
            if (useJefferson) {
                indentList.add(val);
                indentBegin.add(beginTime);
                indentEnd.add(endTime);
            }

        } else {

            String[] valLines = breakValue(val, maxAnnotationLength);
            String wrappedLineLabel = numberAnnotationLines ? NUMBERED_EMPTY_LABEL : EMPTY_LABEL;

            for (int i = 0; i < valLines.length; i++) {
                if (i != 0) {
                    maybeWriteLabel(writer, wrappedLineLabel);
                }

                writer.write(valLines[i]);
                writer.newLine();
            }

            /* Re-merge the string and store it as it was written to the file */
            if (useJefferson) {
                indentList.add(mergeString(valLines));
                indentBegin.add(beginTime);
                indentEnd.add(endTime);
            }
        }
    }

    /**
     * Write the block with TC (time code), silence duration and separating empty line. All are optional.
     *
     * @param writer the output writer
     * @param beginRecord the begin record
     * @param endRecord the end record
     *
     * @throws IOException any IO exception
     */
    private void writeTimeCodeSilenceBlock(BufferedWriter writer, IndexedExportRecord beginRecord,
                                           IndexedExportRecord endRecord) throws
                                                                          IOException {

        if (includeTimeCodes) {
            maybeWriteLabel(writer, TC_LABEL);
            writer.write(TimeFormatter.toString(beginRecord.getBeginTime()));
            writer.write(TIME_SEP);
            writer.write(TimeFormatter.toString(endRecord.getEndTime()));
            writer.newLine();
        }

        if (includeSilence) {
            final long silenceAfter = endRecord.getSilenceAfter();
            if (silenceAfter >= 0) {
                maybeWriteLabel(writer, EMPTY_LABEL);
                writer.write("(" + formatSilenceString(silenceAfter) + ")");
                writer.newLine();
            }
        }

        if (insertEmptyLine) {
            writer.newLine();
        }
    }

    private String prevTierLabel;

    /**
     * Write tier label or participant name.
     *
     * <p>Optionally checks if the label is the same as the previous one.
     * If so, print NUMBERED_EMPTY_LABEL instead.
     *
     * <p>For the comparison it uses the untruncated labels, so that
     * Paul and Paula don't get confused if there are only 4 letters for the label.
     *
     * @param writer the output writer
     * @param indexedExportRecord the indexedExportRecord to write
     *
     * @throws IOException any IO exception
     */
    private void maybeWriteTierLabel(BufferedWriter writer, IndexedExportRecord indexedExportRecord) throws
                                                                                                     IOException {
        String tierName = indexedExportRecord.getTierName();

        if (suppressRepeatedLabels) {
            String untruncatedLabel = untruncatedLabels.get(tierName);

            if (untruncatedLabel.equals(prevTierLabel)) {
                tierName = NUMBERED_EMPTY_LABEL;
            } else {
                prevTierLabel = untruncatedLabel;
            }
        }

        maybeWriteLabel(writer, tierName);
    }

    /**
     * If writing of labels is requested, looks up the padded/truncated label for the given tier and prints it.
     *
     * <p>Also accepts the special pseudo-tier names EMPTY_LABEL and TC_LABEL.
     * Those are for lines that are never numbered.
     *
     * <p>If line numbering is requested, prints (and increments) the number
     * on lines that are not one of the pseudo-tiers.
     *
     * @param writer the output writer
     * @param tierName the tier label to write
     *
     * @throws IOException any IO exception
     */
    private void maybeWriteLabel(BufferedWriter writer, String tierName) throws
                                                                         IOException {
        if (numberAnnotations) {
            if (EMPTY_LABEL.equals(tierName) || TC_LABEL.equals(tierName)) {
                writer.write(numberFieldSpaces);
            } else {
                writer.write(String.format(numberFieldFormat, lineNumber));
                lineNumber++;
            }
        }

        if (includeLabels) {
            writer.write(marginStrings.get(tierName));
        }
    }

    /**
     * A formatter for the silence string, initialized on first usage.
     */
    private NumberFormat formatter;

    /**
     * Formats a long value in ms as a string in seconds with 2 decimals.
     *
     * @param dur the duration in ms
     *
     * @return a string in seconds
     */
    private String formatSilenceString(long dur) {
        if (dur < 0) {
            return "";
        }

        if (formatter == null) {
            int decimal = ((Integer) silenceDecimalComboBox.getSelectedItem()).intValue();

            /* Note: this depends on the current locale */
            formatter = NumberFormat.getInstance();
            formatter.setMinimumFractionDigits(decimal);
            formatter.setMaximumFractionDigits(decimal);
        }

        return formatter.format(dur / 1000f);
    }

    /**
     * Initializes the dialogBox with the last preferred/used settings
     */
    private void setPreferredSetting() {
        extractTiers();

        Boolean boolPref = Preferences.getBool("ExportTradTranscript.rootTiersCB", null);

        if (boolPref != null) {
            setRootTiersOnly(boolPref);
        }

        boolPref = Preferences.getBool("ExportTradTranscript.wrapLinesCB", null);
        if (boolPref != null) {
            wrapLinesCB.setSelected(boolPref);
        }

        boolPref = Preferences.getBool("ExportTradTranscript.minimalDurTF", null);

        if (boolPref != null) {
            Preferences.set("ExportTradTranscript.numCharPerLine", boolPref.toString(), null);
            Preferences.set("ExportTradTranscript.minimalDurTF", null, null);
        }

        String stringPref = Preferences.getString("ExportTradTranscript.numCharPerLine", null);
        if (stringPref != null) {
            numCharTF.setText(stringPref);
        }

        boolPref = Preferences.getBool("ExportTradTranscript.timeCodeCB", null);
        if (boolPref != null) {
            timeCodeCB.setSelected(boolPref);
        }

        boolPref = Preferences.getBool("ExportTradTranscript.MergeAnnotations", null);
        if (boolPref != null) {
            mergeAnnCB.setSelected(boolPref);
        }

        stringPref = Preferences.getString("ExportTradTranscript.MergeDurationValue", null);
        if (stringPref != null) {
            mergeDurTF.setText(stringPref);
        }

        boolPref = Preferences.getBool("ExportTradTranscript.tierLabelsCB", null);
        if (boolPref != null) {
            tierLabelsCB.setSelected(boolPref);
        }

        boolPref = Preferences.getBool("ExportTradTranscript.participantLabelsCB", null);
        if (boolPref != null) {
            participantLabelsCB.setSelected(boolPref);
        }

        boolPref = Preferences.getBool("ExportTradTranscript.suppressRepeatedLabelsCB", null);
        if (boolPref != null) {
            suppressRepeatedLabelsCB.setSelected(boolPref);
        }

        boolPref = Preferences.getBool("ExportTradTranscript.FixedLabelWidth", null);
        if (boolPref != null) {
            labelWidthCB.setSelected(boolPref);
        }

        stringPref = Preferences.getString("ExportTradTranscript.FixedLabelWidthValue", null);
        if (stringPref != null) {
            labelWidthTF.setText(stringPref);
        }

        boolPref = Preferences.getBool("ExportTradTranscript.numberAnnosCB", null);
        if (boolPref != null) {
            numberAnnotationsCB.setSelected(boolPref);
        }

        boolPref = Preferences.getBool("ExportTradTranscript.numberLinesCB", null);
        if (boolPref != null) {
            numberLinesCB.setSelected(boolPref);
        }

        boolPref = Preferences.getBool("ExportTradTranscript.IncludeEmptyLines", null);
        if (boolPref != null) {
            emptyLineCB.setSelected(boolPref);
        }

        boolPref = Preferences.getBool("ExportTradTranscript.silenceCB", null);
        if (boolPref != null) {
            silenceCB.setSelected(boolPref);
        }

        stringPref = Preferences.getString("ExportTradTranscript.minDurSilTF", null);
        if (stringPref != null) {
            minDurSilTF.setText(stringPref);
        }

        Integer intPref = Preferences.getInt("NumberOfDecimalDigits", null);
        if (intPref != null) {
            silenceDecimalComboBox.setSelectedItem(intPref.intValue());
        }

        boolPref = Preferences.getBool("ExportTradTranscript.selectionCB", null);
        if (boolPref != null) {
            selectionCB.setSelected(boolPref);
        }

        boolPref = Preferences.getBool("ExportTradTranscript.useJeffersonCB", null);
        if (boolPref != null) {
            useJeffersonCB.setSelected(boolPref);
        }
    }

    /**
     * Saves the preferred settings Used.
     */
    private void savePreferences() {
        Preferences.set("ExportTradTranscript.rootTiersCB", isRootTiersOnly(), null, false, false);
        Preferences.set("ExportTradTranscript.wrapLinesCB", wrapLinesCB.isSelected(), null, false, false);

        if (numCharTF.getText() != null) {
            Preferences.set("ExportTradTranscript.numCharPerLine", numCharTF.getText().trim(), null, false, false);
        }

        Preferences.set("ExportTradTranscript.timeCodeCB", timeCodeCB.isSelected(), null, false, false);
        Preferences.set("ExportTradTranscript.MergeAnnotations", mergeAnnCB.isSelected(), null, false, false);

        if (mergeDurTF.getText() != null) {
            Preferences.set("ExportTradTranscript.MergeDurationValue", mergeDurTF.getText(), null, false, false);
        }

        Preferences.set("ExportTradTranscript.tierLabelsCB", tierLabelsCB.isSelected(), null, false, false);
        Preferences.set("ExportTradTranscript.participantLabelsCB", participantLabelsCB.isSelected(), null, false, false);
        Preferences.set("ExportTradTranscript.FixedLabelWidth", labelWidthCB.isSelected(), null, false, false);
        Preferences.set("ExportTradTranscript.suppressRepeatedLabelsCB",
                        suppressRepeatedLabelsCB.isSelected(),
                        null,
                        false,
                        false);

        if (labelWidthTF.getText() != null) {
            Preferences.set("ExportTradTranscript.FixedLabelWidthValue", labelWidthTF.getText().trim(), null, false, false);
        }

        Preferences.set("ExportTradTranscript.numberAnnosCB", numberAnnotationsCB.isSelected(), null, false, false);
        Preferences.set("ExportTradTranscript.numberLinesCB", numberLinesCB.isSelected(), null, false, false);
        Preferences.set("ExportTradTranscript.IncludeEmptyLines", emptyLineCB.isSelected(), null, false, false);
        Preferences.set("ExportTradTranscript.silenceCB", silenceCB.isSelected(), null, false, false);

        if (minDurSilTF.getText() != null) {
            Preferences.set("ExportTradTranscript.minDurSilTF", minDurSilTF.getText().trim(), null, false, false);
        }

        Preferences.set("NumberOfDecimalDigits", silenceDecimalComboBox.getSelectedItem(), null, false, false);
        Preferences.set("ExportTradTranscript.selectionCB", selectionCB.isSelected(), null, false, false);
        Preferences.set("ExportTradTranscript.selectedTiers", getSelectedTiers(), transcription, false, false);
        Preferences.set("ExportTradTranscript.SelectTiersMode", getSelectionMode(), transcription, false, false);

        if (!BY_TIER.equals(getSelectionMode())) {
            Preferences.set("ExportTradTranscript.LastSelectedItems", getSelectedItems(), transcription, false, false);
        }

        Preferences.set("ExportTradTranscript.HiddenTiers", getHiddenTiers(), transcription, false, false);
        List<String> tierOrder = getTierOrder();
        Preferences.set("ExportTradTranscript.TierOrder", tierOrder, transcription, false, true);
        Preferences.set("ExportTradTranscript.useJeffersonCB", useJeffersonCB.isSelected(), null, false, false);
    }
}
