package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.ELAN;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.TranscriptionTierSortAndSelectPanel;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.json.JSONWAEncoder;
import mpi.eudico.server.corpora.clomimpl.json.JSONWAEncoderInfo;
import mpi.eudico.server.corpora.clomimpl.json.WebAnnotationJSONBuilder;
import nl.mpi.util.FileExtension;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A dialog window to enable preview and export functionality for a JSON-encoding of the data in the current transcript.
 *
 * @author Allan van Hulst
 */
@SuppressWarnings("serial")
public class ExportJSONDialog extends ClosableDialog implements ActionListener,
                                                                ProgressListener {
    private final Transcription transcription;
    private Selection selection;
    private JSONWAEncoderInfo encoderInfo;

    private JButton buttonClose;
    private JButton buttonExport;
    private JButton buttonUpdate;
    private JButton buttonExportToServer;
    private JTextArea textMain;

    private JCheckBox checkLimitSelection;
    private JCheckBox checkPurpose;
    private JCheckBox checkSingleTarget;
    private JRadioButton radioIncrementalID;
    private JRadioButton radioElanID;
    private JRadioButton radioFragment;
    private JRadioButton radioSelector;
    private JRadioButton radioTextPlain;
    private JRadioButton radioTextHtml;

    private JProgressBar progressBar;

    private JLabel authenticationLabel;

    private JTextField authenticationKeyTextField;

    private WebAnnotationJSONBuilder webAnnotationBuilder;

    private TranscriptionTierSortAndSelectPanel tiersPanel = null;

    private Boolean exportAnnotationsToServer = false;

    private Boolean isAuthenticationEnabled = false;

    private String authenticationKey = "";

    /**
     * Constructor.
     *
     * @param owner the parent window
     * @param transcription the transcript to export
     * @param selection the selected time interval
     *
     * @throws HeadlessException if created in a headless environment
     */
    public ExportJSONDialog(Frame owner, Transcription transcription, Selection selection) throws
                                                                                           HeadlessException {
        super(owner, true);

        this.transcription = transcription;
        this.selection = selection;

        initComponents();

        if (selection == null || Math.abs(selection.getBeginTime() - selection.getEndTime()) <= 10) {
            checkLimitSelection.setEnabled(false);
        }

        checkPurpose.setSelected(true);
        radioElanID.setSelected(true);
        radioFragment.setSelected(true);
        radioTextPlain.setSelected(true);
        loadPreferences();
        applySettings();
        encoderInfo.setEncodePreview(true);
        textMain.setText(new JSONWAEncoder().createJSONText(transcription, encoderInfo));
        setTitle(ElanLocale.getString("ExportJSONDialog.Title"));
        pack();
        setLocationRelativeTo(getParent());
    }

    /**
     * Creates a new dialog instance.
     *
     * @param owner the parent window
     * @param transcription the transcript to load
     * @param selection the selected interval
     * @param exportToServer boolean to determine if the dialog is for export to a file or to load to a server
     * @param isAuthenticationEnabled to determine if the authentication is required to export the data
     *
     * @throws HeadlessException if the dialog is used in a headless environment
     */
    public ExportJSONDialog(Frame owner, Transcription transcription, Selection selection, Boolean exportToServer,
                            Boolean isAuthenticationEnabled) throws
                                                             HeadlessException {
        super(owner, true);

        this.transcription = transcription;
        // this.selection = selection;
        this.exportAnnotationsToServer = exportToServer;
        this.isAuthenticationEnabled = isAuthenticationEnabled;
        initComponentsToLoadServer();

        setTitle(ElanLocale.getString("ExportJSONToServerDialog.Title"));
        pack();
        setLocationRelativeTo(getParent());


    }

    /**
     * Initializes UI elements.
     */
    protected void initComponents() {
        // layout
        getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 3;
        getContentPane().add(createTextArea(), gbc);

        gbc.gridx = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.0;
        getContentPane().add(createTiersPanel(), gbc);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        gbc.weightx = 0.0;
        getContentPane().add(createControlsPanel(), gbc);

        gbc.gridy = 3;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        getContentPane().add(createButtonPanel(), gbc);

    }


    /**
     * Initializes the UI components to load to server
     */
    protected void initComponentsToLoadServer() {

        getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(2, 4, 2, 4);

        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        getContentPane().add(createTiersPanel(), gbc);

        if (isAuthenticationEnabled) {

            GridBagConstraints lgbc = new GridBagConstraints();
            lgbc.anchor = GridBagConstraints.WEST;
            lgbc.insets = insets;
            lgbc.gridx = 0;
            GridBagConstraints rgbc = new GridBagConstraints();

            lgbc.gridy = 1;
            lgbc.weighty = 0.0;
            lgbc.fill = GridBagConstraints.NONE;
            authenticationLabel = new JLabel();
            authenticationLabel.setText(ElanLocale.getString("Frame.ElanFrame.AnnotationServer.AuthenticationKey"));
            getContentPane().add(authenticationLabel, lgbc);

            rgbc.gridx = 1;
            rgbc.gridy = 1;
            rgbc.fill = GridBagConstraints.HORIZONTAL;
            rgbc.anchor = GridBagConstraints.WEST;
            rgbc.insets = insets;
            rgbc.weightx = 1.0;
            authenticationKeyTextField = new JTextField();
            getContentPane().add(authenticationKeyTextField, rgbc);

        }


        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        getContentPane().add(progressBar, gbc);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        getContentPane().add(createButtonPanel(), gbc);


    }


    /**
     * Create a panel containing various controls to fine-tune the JSON-export.
     *
     * @return A JPanel
     */
    private JPanel createControlsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder(ElanLocale.getString("Menu.Options")));
        GridBagConstraints gc = new GridBagConstraints();
        Insets insets = new Insets(4, 6, 4, 6);
        Insets indent = new Insets(0, 22, 2, 6);

        /* Initialize grid bag constraints */
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = insets;

        /* Add checkbox to limit export to current selection */
        checkLimitSelection = new JCheckBox(ElanLocale.getString("ExportJSONDialog.Limit.Selection"));
        panel.add(checkLimitSelection, gc);

        /* Add checkbox for selection of whether to add purpose=transcribing */
        gc.gridy = gc.gridy + 1;
        checkPurpose = new JCheckBox(ElanLocale.getString("ExportJSONDialog.Add.Purpose"));
        panel.add(checkPurpose, gc);

        /* add checkbox for single or multiple target export */
        gc.gridy = gc.gridy + 1;
        checkSingleTarget = new JCheckBox(ElanLocale.getString("ExportJSONDialog.Single.Target"));
        panel.add(checkSingleTarget, gc);

        /*
         * Add radio buttons for selection of whether to add incremental ID's or ELAN
         * ID's
         */

        radioIncrementalID = new JRadioButton(ElanLocale.getString("ExportJSONDialog.Incremental.ID"));
        radioElanID = new JRadioButton(ElanLocale.getString("ExportJSONDialog.ELAN.ID"));
        ButtonGroup idGroup = new ButtonGroup();
        idGroup.add(radioIncrementalID);
        idGroup.add(radioElanID);

        // JPanel idPanel = new JPanel();
        // idPanel.setBorder(new
        // TitledBorder(ElanLocale.getString("ExportJSONDialog.Format.ID")));
        JLabel idLabel = new JLabel(ElanLocale.getString("ExportJSONDialog.Format.ID"));
        // idPanel.setLayout(new BoxLayout(idPanel, BoxLayout.Y_AXIS));
        // idPanel.add(radioIncrementalID);
        // idPanel.add(radioElanID);
        gc.gridy = gc.gridy + 1;
        panel.add(idLabel, gc);
        gc.gridy++;
        gc.insets = indent;
        panel.add(radioElanID, gc);
        gc.gridy++;
        panel.add(radioIncrementalID, gc);

        /* Add radio buttons to handle formatting of fragment-indicator */
        radioFragment = new JRadioButton(ElanLocale.getString("ExportJSONDialog.Begin.End"));
        radioSelector = new JRadioButton(ElanLocale.getString("ExportJSONDialog.Selector"));
        ButtonGroup timeGroup = new ButtonGroup();
        timeGroup.add(radioFragment);
        timeGroup.add(radioSelector);

        // JPanel timePanel = new JPanel();
        // timePanel.setBorder(new
        // TitledBorder(ElanLocale.getString("ExportJSONDialog.Format.Timespan")));
        JLabel timeLabel = new JLabel(ElanLocale.getString("ExportJSONDialog.Format.Timespan"));
        // timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
        // timePanel.add(radioFragment);
        // timePanel.add(radioSelector);

        gc.gridy = gc.gridy + 1;
        gc.insets = insets;
        panel.add(timeLabel, gc);
        gc.gridy++;
        gc.insets = indent;
        panel.add(radioFragment, gc);
        gc.gridy++;
        panel.add(radioSelector, gc);

        /* Add radio buttons to handle the annotation-type */
        radioTextPlain = new JRadioButton(ElanLocale.getString("ExportJSONDialog.Text.Plain"));
        radioTextHtml = new JRadioButton(ElanLocale.getString("ExportJSONDialog.Text.HTML"));
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(radioTextPlain);
        typeGroup.add(radioTextHtml);

        // JPanel typePanel = new JPanel();
        // typePanel.setBorder(new
        // TitledBorder(ElanLocale.getString("ExportJSONDialog.Encode.Type")));
        JLabel typeLabel = new JLabel(ElanLocale.getString("ExportJSONDialog.Encode.Type"));
        // typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));
        // typePanel.add(radioTextPlain);
        // typePanel.add(radioTextHtml);

        gc.gridy = gc.gridy + 1;
        gc.insets = insets;
        panel.add(typeLabel, gc);
        gc.gridy++;
        gc.insets = indent;
        panel.add(radioTextPlain, gc);
        gc.gridy++;
        panel.add(radioTextHtml, gc);

        return panel;
    }

    /**
     * Create a JPanel containing the tier selection JTable.
     *
     * <p>This has been updated to use the TranscriptionTierSortAndSelectPanel
     * which is derived from JPanel.
     *
     * @return A JPanel
     */
    private JPanel createTiersPanel() {
        tiersPanel = new TranscriptionTierSortAndSelectPanel((TranscriptionImpl) transcription);
        tiersPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportDialog.Label.SelectTiers")));

        return tiersPanel;
    }

    /**
     * Create a text area for preview of the generated JSON code.
     *
     * @return A JScrollPane containing the JTextArea
     */
    private JScrollPane createTextArea() {
        textMain = new JTextArea(20, 40);
        textMain.setEditable(false);

        JScrollPane scrollpane = new JScrollPane(textMain);
        scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        return scrollpane;
    }

    /**
     * Create panel for Update, Export, and Close buttons
     *
     * @return A JPanel
     */
    private JPanel createButtonPanel() {

        if (!exportAnnotationsToServer) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
            buttonUpdate = new JButton(ElanLocale.getString("ExportJSONDialog.Update"));
            buttonUpdate.addActionListener(this);

            buttonExport = new JButton(ElanLocale.getString("ExportJSONDialog.Export"));
            buttonExport.addActionListener(this);

            panel.add(buttonUpdate);
            panel.add(buttonExport);

            buttonClose = new JButton(ElanLocale.getString("ExportJSONDialog.Close"));
            buttonClose.addActionListener(this);


            panel.add(buttonClose);

            return panel;
        } else {
            JPanel panel = new JPanel();
            buttonExportToServer = new JButton(ElanLocale.getString("ExportJSONToServerDialog.Export"));
            buttonExportToServer.addActionListener(this);

            panel.add(buttonExportToServer);

            buttonClose = new JButton(ElanLocale.getString("ExportJSONDialog.Close"));
            buttonClose.addActionListener(this);


            panel.add(buttonClose);

            return panel;
        }


    }

    /**
     * Export the JSON data to a file chosen by the user.
     */
    private void exportToFile() {

        FileChooser chooser = new FileChooser(this);

        chooser.createAndShowFileDialog(ElanLocale.getString("ExportJSONDialog.Export.Location"),
                                        FileChooser.SAVE_DIALOG,
                                        FileExtension.JSON_EXT,
                                        "ExportJSONDialog.Current.Directory");

        if (chooser.getSelectedFile() != null) {
            applySettings();

            encoderInfo.setEncodePreview(false);

            try {
                new JSONWAEncoder().encodeAndSave(transcription,
                                                  encoderInfo,
                                                  null,
                                                  chooser.getSelectedFile().getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                                              (ElanLocale.getString("ExportJSONDialog.Error")
                                               + " "
                                               + chooser.getSelectedFile().getName()),
                                              ElanLocale.getString("Message.Warning"),
                                              JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void applySettings() {
        if (encoderInfo == null) {
            encoderInfo = new JSONWAEncoderInfo();
            encoderInfo.setGenerator("ELAN Multimedia Annotator " + ELAN.getVersionString());
            encoderInfo.setIndentationLevel(2);
        }
        encoderInfo.setSelectedTiers(tiersPanel.getSelectedTiers());

        if (!exportAnnotationsToServer) {
            encoderInfo.setIncrementalIDs(radioIncrementalID.isSelected());
            encoderInfo.setFragmentSelector(radioSelector.isSelected());
            encoderInfo.setBodyFormat(radioTextHtml.isSelected() ? "text/html" : "text/plain");
            if (checkPurpose.isSelected()) {
                encoderInfo.setPurpose("transcribing");
            } else {
                encoderInfo.setPurpose(null);
            }

            encoderInfo.setSingleTargetExport(checkSingleTarget.isSelected());

            if (checkLimitSelection.isEnabled()) {
                if (selection != null && checkLimitSelection.isSelected()) {
                    encoderInfo.setBeginTime(selection.getBeginTime());
                    encoderInfo.setEndTime(selection.getEndTime());
                } else {
                    encoderInfo.setBeginTime(0);
                    encoderInfo.setEndTime(Long.MAX_VALUE);
                }
            }
        }


    }

    /**
     * Handle button clicks.
     *
     * @param evt The ActionEvent
     */
    public void actionPerformed(ActionEvent evt) {

        if (evt.getSource() == buttonUpdate) {
            applySettings();
            encoderInfo.setEncodePreview(true);
            textMain.setText(new JSONWAEncoder().createJSONText(transcription, encoderInfo));
        } else if (evt.getSource() == buttonExportToServer) {
            exportToServer();
        } else if (evt.getSource() == buttonClose) {
            if (!exportAnnotationsToServer) {
                doClose();
            } else {
                setVisible(false);
            }
        } else if (evt.getSource() == buttonExport) {
            exportToFile();
        }
    }


    private void doClose() {
        savePreferences();
        setVisible(false);
    }

    private void savePreferences() {
        Preferences.set("ExportJSON.LimitSelection", Boolean.valueOf(checkLimitSelection.isSelected()), null, false, false);
        Preferences.set("ExportJSON.AddPurpose", Boolean.valueOf(checkPurpose.isSelected()), null, false, false);
        Preferences.set("ExportJSON.ELAN.ID", Boolean.valueOf(radioElanID.isSelected()), null, false, false);
        Preferences.set("ExportJSON.MediaFragment", Boolean.valueOf(radioFragment.isSelected()), null, false, false);
        Preferences.set("ExportJSON.TextPlain", Boolean.valueOf(radioTextPlain.isSelected()), null, false, false);

        List<String> selNames = tiersPanel.getSelectedItems();
        List<String> hidNames = tiersPanel.getHiddenTiers();
        String selTab = tiersPanel.getSelectionMode();
        if (selNames != null && !selNames.isEmpty()) {
            Preferences.set("ExportJSON.SelectedItems", selNames, transcription, false, false);
            Preferences.set("ExportJSON.HiddenTiers", hidNames, transcription, false, false);
            Preferences.set("ExportJSON.SelectedMode", selTab, transcription, false, false);
        }
        Preferences.set("ExportJSON.SingleTarget", Boolean.valueOf(checkSingleTarget.isSelected()), null, false, true);
    }


    @SuppressWarnings("unchecked")
    private void loadPreferences() {
        Boolean boolPref = Preferences.getBool("ExportJSON.LimitSelection", null);
        if (boolPref != null) {
            checkLimitSelection.setSelected(boolPref.booleanValue());
        }
        boolPref = Preferences.getBool("ExportJSON.AddPurpose", null);
        if (boolPref != null) {
            checkPurpose.setSelected(boolPref.booleanValue());
        }
        boolPref = Preferences.getBool("ExportJSON.ELAN.ID", null);
        if (boolPref != null) {
            if (boolPref.booleanValue()) {
                radioElanID.setSelected(true);
            } else {
                radioIncrementalID.setSelected(true);
            }
        }
        boolPref = Preferences.getBool("ExportJSON.MediaFragment", null);
        if (boolPref != null) {
            if (boolPref.booleanValue()) {
                radioFragment.setSelected(true);
            } else {
                radioSelector.setSelected(true);
            }
        }
        boolPref = Preferences.getBool("ExportJSON.TextPlain", null);
        if (boolPref != null) {
            if (boolPref.booleanValue()) {
                radioTextPlain.setSelected(true);
            } else {
                radioTextHtml.setSelected(true);
            }
        }
        Object selItemsObj = Preferences.get("ExportJSON.SelectedItems", transcription);
        String tabName = Preferences.getString("ExportJSON.SelectedMode", transcription);
        Object hidNamesObj = Preferences.get("ExportJSON.HiddenTiers", transcription);
        if (tabName != null) {
            tiersPanel.setSelectionMode(tabName, null);
        }
        if (selItemsObj != null && tabName != null) {
            List<String> nameList = (List<String>) selItemsObj;
            List<String> hidNames = null;
            if (hidNamesObj != null) {
                hidNames = (List<String>) hidNamesObj;
            } else {
                hidNames = new ArrayList<String>(0);
            }
            tiersPanel.setSelectionMode(tabName, hidNames);
            tiersPanel.setSelectedItems(nameList);
        }
        boolPref = Preferences.getBool("ExportJSON.SingleTarget", null);
        if (boolPref != null) {
            checkSingleTarget.setSelected(boolPref.booleanValue());
        }
    }


    /**
     * action to perform the exporting of tiers and annotations
     */
    private void exportToServer() {

        if (tiersPanel.getSelectedTiers().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                                          ElanLocale.getString("Frame.ElanFrame.AnnotationServer.NoTiers"),
                                          ElanLocale.getString("Message.Warning"),
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (isAuthenticationEnabled) {
            authenticationKey = authenticationKeyTextField.getText().trim();
            if (authenticationKey.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                                              ElanLocale.getString("Frame.ElanFrame.AnnotationServer.EmptyBearerKey"),
                                              ElanLocale.getString("Message.Warning"),
                                              JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        progressBar.setValue(0);

        applySettings();

        encoderInfo.setEncodePreview(false);

        webAnnotationBuilder = new WebAnnotationJSONBuilder(transcription, encoderInfo);
        webAnnotationBuilder.addProgressListener(this);


        exportToServerInThread();

    }

    /**
     * exporting the data in thread
     */
    public void exportToServerInThread() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Map<String, Integer> exportedData = webAnnotationBuilder.exportAnnotationsToServer(authenticationKey);

                    if (exportedData.isEmpty()) {
                        progressInterrupted(this, "");
                    } else {
                        ExportedResultPane resultPane = new ExportedResultPane(exportedData);
                        JOptionPane.showMessageDialog(getParent(),
                                                      resultPane,
                                                      ElanLocale.getString("ExportJSONToServerResult.Message.Title"),
                                                      JOptionPane.INFORMATION_MESSAGE);

                    }
                }
            }).start();
        } catch (Exception e) {
            progressInterrupted(this, "");
        }
    }


    @Override
    public void progressUpdated(Object source, int percent, String message) {
        progressBar.setValue(percent);
    }

    @Override
    public void progressCompleted(Object source, String message) {
        progressBar.setValue(100);
    }

    @Override
    public void progressInterrupted(Object source, String message) {
        if (message != null) {
            progressBar.setString(message);
        }

        // reset
        progressBar.setValue(0);

        JOptionPane.showMessageDialog(getParent(),
                                      ElanLocale.getString("ExportJSONToServerResult.Failure.Info"),
                                      ElanLocale.getString("ExportJSONToServerResult.Message.Title"),
                                      JOptionPane.INFORMATION_MESSAGE);

    }
}
