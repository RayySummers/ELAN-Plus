package mpi.eudico.client.annotator.interlinear;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.tier.TierExportSetting;
import mpi.eudico.client.annotator.tier.TierExportTable5Columns;
import mpi.eudico.client.annotator.tier.TierExportTableModel;
import mpi.eudico.client.annotator.tier.TierExportTableModel5Columns;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.html.HTMLRendererTimeAlignedInterlinear;
import mpi.eudico.server.corpora.clomimpl.html.TAIEncoderInfo;
import mpi.eudico.server.corpora.clomimpl.html.TAITierSetting;
import mpi.eudico.util.TimeFormatter;
import nl.mpi.util.FileExtension;


/**
 * An export dialog for exporting tiers to time-aligned interlinear gloss.
 *
 * @author Steffen Zimmermann
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ExportTimeAlignedInterlinear extends ClosableDialog
        implements  ItemListener, ActionListener {

    /** table model for tier table */
    private TranscriptionImpl transcription;
    /** selection */
    private Selection selection;
    /** selected tiers and their settings */
    private List<TAITierSetting> tierSettings;
    /** time of one character */
    private int timeUnit;
    /** width of one block */
    private int blockSpace;
    /** space for tier names */
    private int leftMargin;
    /** HTML font size */
    private int fontSize;
    /** reference tier name */
    private String refTier;
    /** print only the selected section */
    private boolean selectionOnly;
    /** wrap lines within one block */
    private boolean wrapWithinBlock;
    private TAIEncoderInfo encoderInfo;

    /** default value for time unit */
    private final int DEFAULT_TIME_UNIT = 30;
    /** default value for block space (in time units) */
    private final int DEFAULT_BLOCK_SPACE = 55;
    /** default value for time unit */
    private final int DEFAULT_LEFT_MARGIN = 15;
    /** default value for font size in HTML */
    private final int DEFAULT_FONT_SIZE = 8;

    // ui
    private PreviewPanelTimeAlignedInterlinear previewPanel;
    private JPanel optionsPanel;
    private JPanel tierSelectionPanel;
    private JPanel howPanel;
    private JPanel buttonPanel;
    private JTable tierTable;
    private TierExportTableModel5Columns model;

    // tier panel
    private JButton upButton;
    private JButton downButton;

    // howPanel
    private JLabel timeUnitLabel;
    private JTextField timeUnitTextField;
    private JLabel blockSpaceLabel;
    private JTextField blockSpaceTextField;
    private JLabel leftMarginLabel;
    private JTextField leftMarginTextField;
    private JLabel fontSizeLabel;
    private JTextField fontSizeTextField;
    private JCheckBox selectionCheckBox;
    private JCheckBox useReferenceTierCheckBox;
    private JCheckBox wrapWithinBlockCheckBox;
    private JComboBox<String> refTierComboBox;
    private JCheckBox showTimeLineCheckBox;
    private JComboBox<String> timeFormatComboBox;
    private JCheckBox showAnnotationBoundsCheckBox;
    private JCheckBox alignLeftCheckBox;
    // localized time code strings
    private String tcStyleHhMmSsMs;
    private String tcStyleSsMs;
    private String tcStyleMs;

    // button panel
    private JButton applyChangesButton;
    private JButton printButton;
    private JButton restoreDefaultsButton;
    private JButton closeButton;

    // localized option strings
    /** column id for the include in export checkbox column, invisible */
    private final String PRINT_COLUMN = "Export";
    /** column id for the tier name column, invisible */
    private final String TIER_NAME_COLUMN = "Tier";
    /** column id for the font size column, invisible */
    private final String UNDERLINED_COLUMN = "Underlined";
    /** column id for the font size column, invisible */
    private final String BOLD_COLUMN = "Bold";
    /** column id for the font size column, invisible */
    private final String ITALIC_COLUMN = "Italic";

    /**
     * Constructor.
     *
     * @param parent parent frame
     * @param modal the modal/blocking attribute
     * @param transcription the transcription to export from
     * @param selection the current selection
     */
    public ExportTimeAlignedInterlinear(Frame parent, boolean modal,
                                       TranscriptionImpl transcription, Selection selection) {
        super(parent, modal);
        this.transcription = transcription;
        this.selection = selection;
        encoderInfo = new TAIEncoderInfo();
        initComponents();
        extractTiers();
        updateLocale();
        setDefaultValues();
        loadPreferences();
        doApplyChanges();
        postInit();
    }

    /**
     * Initializes UI elements.
     */
    protected void initComponents() {
        optionsPanel = new JPanel();
        tierSelectionPanel = new JPanel();
        howPanel = new JPanel();
        buttonPanel = new JPanel();
        upButton = new JButton();
        downButton = new JButton();
        previewPanel = new PreviewPanelTimeAlignedInterlinear();

        // components of howPanel
        timeUnitLabel = new JLabel();
        timeUnitTextField = new JTextField(4);
        blockSpaceLabel = new JLabel();
        blockSpaceTextField = new JTextField(4);
        leftMarginLabel = new JLabel();
        leftMarginTextField = new JTextField(4);
        fontSizeLabel = new JLabel();
        fontSizeTextField = new JTextField(4);
        selectionCheckBox = new JCheckBox();
        useReferenceTierCheckBox = new JCheckBox();
        useReferenceTierCheckBox.addItemListener(this);
        wrapWithinBlockCheckBox = new JCheckBox();
        refTierComboBox = new JComboBox<String>();
        showTimeLineCheckBox = new JCheckBox();
        showTimeLineCheckBox.addItemListener(this);
        showAnnotationBoundsCheckBox = new JCheckBox();
        timeFormatComboBox = new JComboBox<String>();
        alignLeftCheckBox = new JCheckBox();

        // components of buttonPanel
        applyChangesButton = new JButton();
        printButton = new JButton();
        restoreDefaultsButton = new JButton();
        closeButton = new JButton();

        // init ref tier ComboBox (enabled/disabled)
        if (useReferenceTierCheckBox.isSelected()) {
            wrapWithinBlockCheckBox.setEnabled(true);
            refTierComboBox.setEnabled(true);
        } else {
            wrapWithinBlockCheckBox.setEnabled(false);
            refTierComboBox.setEnabled(false);
        }

        // tier table and scrollpane, panel
        try {
            ImageIcon upIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/" +
                    "navigation/Up16.gif"));
            ImageIcon downIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/" +
                    "navigation/Down16.gif"));
            upButton.setIcon(upIcon);
            downButton.setIcon(downIcon);
        } catch (Exception ex) {
            upButton.setText("Up");
            downButton.setText("Down");
        }

        model = new TierExportTableModel5Columns();
        model.setColumnIdentifiers(new String[] {
                PRINT_COLUMN, TIER_NAME_COLUMN, UNDERLINED_COLUMN, BOLD_COLUMN, ITALIC_COLUMN});
        tierTable = new TierExportTable5Columns(model, true);


        Dimension tableDim = new Dimension(50, 100);

        tierTable.getColumn(PRINT_COLUMN).setMaxWidth(70);
        tierTable.getColumn(PRINT_COLUMN).setPreferredWidth(50);

        JScrollPane tierScrollPane = new JScrollPane(tierTable);
        tierScrollPane.setPreferredSize(tableDim);

        // layout
        getContentPane().setLayout(new GridBagLayout());
        optionsPanel.setLayout(new GridBagLayout());
        tierSelectionPanel.setLayout(new GridBagLayout());
        howPanel.setLayout(new GridBagLayout());
        buttonPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc;
        Insets insets = new Insets(2, 2, 2, 2);

        // add the preview panel
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        getContentPane().add(previewPanel, gbc);

        // fill and add the tier selection panel
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        tierSelectionPanel.add(tierScrollPane, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        tierSelectionPanel.add(upButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        tierSelectionPanel.add(downButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        optionsPanel.add(tierSelectionPanel, gbc);

        // fill and add "how" panel
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        howPanel.add(timeUnitLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        howPanel.add(timeUnitTextField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        howPanel.add(blockSpaceLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        howPanel.add(blockSpaceTextField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        howPanel.add(leftMarginLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        howPanel.add(leftMarginTextField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        howPanel.add(fontSizeLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        howPanel.add(fontSizeTextField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20,2,2,2);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        howPanel.add(selectionCheckBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        howPanel.add(useReferenceTierCheckBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        howPanel.add(refTierComboBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 15, 2, 2);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        howPanel.add(wrapWithinBlockCheckBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        howPanel.add(alignLeftCheckBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        howPanel.add(showAnnotationBoundsCheckBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        howPanel.add(showTimeLineCheckBox, gbc);

        gbc.gridx = 1;
        howPanel.add(timeFormatComboBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        optionsPanel.add(howPanel, gbc);

        JScrollPane optionsScroll = new JScrollPane(optionsPanel);
        optionsScroll.setBorder(null);

        // button panel
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        buttonPanel.add(applyChangesButton, gbc);


        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        buttonPanel.add(printButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        buttonPanel.add(restoreDefaultsButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        buttonPanel.add(closeButton, gbc);

        // add the options panel
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.05;
        gbc.weighty = 1.0;
        getContentPane().add(optionsScroll, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        getContentPane().add(buttonPanel, gbc);

        // add listeners
        upButton.addActionListener(this);
        downButton.addActionListener(this);
        applyChangesButton.addActionListener(this);
        restoreDefaultsButton.addActionListener(this);
        closeButton.addActionListener(this);
        printButton.addActionListener(this);
    }

    /**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {

        if (model != null) {
            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                model.removeRow(i);
            }

            if (transcription != null) {
                List<TierImpl> tiers = transcription.getTiers();

                for (TierImpl t : tiers) {
                    String tierName = t.getName();

                    model.addRow(true, tierName, false, false, false);
                }
            }


            if (model.getRowCount() > 1) {
                upButton.setEnabled(true);
                downButton.setEnabled(true);
            } else {
                upButton.setEnabled(false);
                downButton.setEnabled(false);
            }

        }  else {
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }

        // initialize tierSettings
        tierSettings = convertSettings(model.getSelectedTiersWithSettings());
    }

    /**
     * Applies localized strings to the ui elements.
     */
    protected void updateLocale() {
        setTitle(ElanLocale.getString("InterlinearizerOptionsDlg.Title"));

        tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString(
                "InterlinearizerOptionsDlg.Tiers")));
        howPanel.setBorder(new TitledBorder(ElanLocale.getString(
                "InterlinearizerOptionsDlg.How")));

        // "how" panel
        timeUnitLabel.setText(ElanLocale.getString(
                "ExportTimeAlignedInterlinear.Label.TimeUnit"));
        blockSpaceLabel.setText(ElanLocale.getString(
                "ExportTimeAlignedInterlinear.Label.BlockSpace"));
        wrapWithinBlockCheckBox.setText(ElanLocale.getString(
                "ExportTimeAlignedInterlinear.CheckBox.WrapWithinBlock"));
        leftMarginLabel.setText(ElanLocale.getString(
                "ExportTimeAlignedInterlinear.Label.LeftMargin"));
        fontSizeLabel.setText(ElanLocale.getString(
                "ExportTimeAlignedInterlinear.Label.FontSize"));
        selectionCheckBox.setText(ElanLocale.getString(
                "ExportTimeAlignedInterlinear.CheckBox.Selection"));
        useReferenceTierCheckBox.setText(ElanLocale.getString(
                "ExportTimeAlignedInterlinear.CheckBox.UseReferenceTier"));

        showTimeLineCheckBox.setText(ElanLocale.getString(
        		"ExportTimeAlignedInterlinear.CheckBox.ShowTimeline"));
        showAnnotationBoundsCheckBox.setText(ElanLocale.getString(
        		"ExportTimeAlignedInterlinear.CheckBox.ShowBounds"));
        alignLeftCheckBox.setText(ElanLocale.getString("ExportTimeAlignedInterlinear.CheckBox.TextAlignment"));

        tcStyleHhMmSsMs = ElanLocale.getString(
                "TimeCodeFormat.TimeCode");
        tcStyleSsMs = ElanLocale.getString(
                "TimeCodeFormat.Seconds");
        tcStyleMs = ElanLocale.getString(
                "TimeCodeFormat.MilliSec");
        timeFormatComboBox.removeAllItems();
        timeFormatComboBox.addItem(tcStyleHhMmSsMs);
        timeFormatComboBox.addItem(tcStyleSsMs);
        timeFormatComboBox.addItem(tcStyleMs);

        for (TAITierSetting ts : tierSettings) {
            refTierComboBox.addItem(ts.getTierName());
        }

        // components of buttonPanel
        printButton.setText(ElanLocale.getString("Menu.File.SaveAs"));
        applyChangesButton.setText(ElanLocale.getString(
                "InterlinearizerOptionsDlg.ApplyChanges"));
        restoreDefaultsButton.setText(ElanLocale.getString("ExportTimeAlignedInterlinear.Button.RestoreDefaults"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
    }

    /**
     * Set the default Values of this export.
     */
    protected void setDefaultValues() {
        refTierComboBox.setSelectedIndex(0);
        timeUnitTextField.setText(Integer.toString(DEFAULT_TIME_UNIT));
        blockSpaceTextField.setText(Integer.toString(DEFAULT_BLOCK_SPACE));
        leftMarginTextField.setText(Integer.toString(DEFAULT_LEFT_MARGIN));
        fontSizeTextField.setText(Integer.toString(DEFAULT_FONT_SIZE));

        wrapWithinBlockCheckBox.setSelected(true);
        selectionCheckBox.setSelected(false);
        selectionCheckBox.setEnabled(selection != null &&
        		selection.getBeginTime() != selection.getEndTime());
        useReferenceTierCheckBox.setSelected(false);
        showTimeLineCheckBox.setSelected(false);
        showAnnotationBoundsCheckBox.setSelected(false);
        alignLeftCheckBox.setSelected(false);
        timeFormatComboBox.setSelectedIndex(0);

        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(true, i, 0);
            model.setValueAt(true, i, 2);
            model.setValueAt(false, i, 3);
            model.setValueAt(false, i, 4);
        }
    }

    /**
     * Updates the interlinear layout and the preview after (possible) changes
     * in parameters.
     */
    private void doApplyChanges() {
        tierSettings = convertSettings(model.getSelectedTiersWithSettings());
        selectionOnly = selectionCheckBox.isSelected() && selectionCheckBox.isEnabled();
        wrapWithinBlock = wrapWithinBlockCheckBox.isSelected();

        refTier = (String) refTierComboBox.getSelectedItem();
        TAITierSetting ts = getTierSettingById(refTier);
        if (! useReferenceTierCheckBox.isSelected()) {
            setReference(null);
            refTier = null;
        } else if (ts != null) {
        	TierImpl t = transcription.getTierWithId(refTier);
            if (t == null || t.getAnnotations().isEmpty()) {
            	JOptionPane.showMessageDialog(null,
                    ElanLocale.getString("ExportTimeAlignedInterlinear.Message.NoRefAnns"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
            }
            setReference(refTier);
        } else {
            JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ExportTimeAlignedInterlinear.Message.WrongTier"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);

            String defaultTier = tierSettings.get(0).getTierName();
            setReference(defaultTier);
            refTierComboBox.setSelectedItem(defaultTier);
            refTier = defaultTier;
        }

        try {
            timeUnit = Integer.parseUnsignedInt(timeUnitTextField.getText());
        }
        catch (NumberFormatException e)
        {
            timeUnitTextField.setText(Integer.toString(DEFAULT_TIME_UNIT));
            timeUnit = DEFAULT_TIME_UNIT;

            JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ExportTimeAlignedInterlinear.Message.InvalidNumber"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
            timeUnitTextField.requestFocus();
            return ;
        }

        try {
            blockSpace = Integer.parseUnsignedInt(blockSpaceTextField.getText());
        }
        catch (NumberFormatException e)
        {
            blockSpaceTextField.setText(Integer.toString(DEFAULT_BLOCK_SPACE));
            blockSpace = DEFAULT_BLOCK_SPACE;

            JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ExportTimeAlignedInterlinear.Message.InvalidNumber"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
            blockSpaceTextField.requestFocus();
            return ;
        }

        try {
            leftMargin = Integer.parseUnsignedInt(leftMarginTextField.getText());
        }
        catch (NumberFormatException e)
        {
            leftMarginTextField.setText(Integer.toString(DEFAULT_LEFT_MARGIN));
            leftMargin = DEFAULT_LEFT_MARGIN;

            JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ExportTimeAlignedInterlinear.Message.InvalidNumber"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
            leftMarginTextField.requestFocus();
            return ;
        }

        try {
            fontSize = Integer.parseUnsignedInt(fontSizeTextField.getText());
        }
        catch (NumberFormatException e)
        {
            fontSizeTextField.setText(Integer.toString(DEFAULT_FONT_SIZE));
            fontSize = DEFAULT_LEFT_MARGIN;

            JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ExportTimeAlignedInterlinear.Message.InvalidNumber"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
            fontSizeTextField.requestFocus();
            return ;
        }

        // store everything in the encoder info
        encoderInfo.setTimeUnit(timeUnit);
        encoderInfo.setBlockSpace(blockSpace);
        encoderInfo.setLeftMargin(leftMargin);
        encoderInfo.setFontSize(fontSize);

        encoderInfo.setTierSettings(tierSettings);
        if (selectionOnly) {
        	encoderInfo.setBeginTime(selection.getBeginTime());
        	encoderInfo.setEndTime(selection.getEndTime());
        } else {
        	encoderInfo.setBeginTime(0);
        	encoderInfo.setEndTime(Long.MAX_VALUE);
        }

        encoderInfo.setWrapWithinBlock(wrapWithinBlock);
        encoderInfo.setShowAnnotationBoundaries(showAnnotationBoundsCheckBox.isSelected());
        encoderInfo.setTextAlignment(alignLeftCheckBox.isSelected() ? SwingConstants.LEFT : SwingConstants.RIGHT);
        encoderInfo.setShowTimeLine(showTimeLineCheckBox.isSelected());
        if (showTimeLineCheckBox.isSelected()) {
        	if (timeFormatComboBox.getSelectedIndex() == 0) {
        		encoderInfo.setTimeFormat(TimeFormatter.TIME_FORMAT.HHMMSSMS);
        	} else if (timeFormatComboBox.getSelectedIndex() == 1) {
        		encoderInfo.setTimeFormat(TimeFormatter.TIME_FORMAT.SSMS);
        	} else if (timeFormatComboBox.getSelectedIndex() == 2) {
        		encoderInfo.setTimeFormat(TimeFormatter.TIME_FORMAT.MS);
        	}
        }
        encoderInfo.setRefTierName(refTier);

        HTMLRendererTimeAlignedInterlinear render = new HTMLRendererTimeAlignedInterlinear(transcription,
        encoderInfo);
        previewPanel.setHtmlText(render.renderToText());
        previewPanel.updateView();
    }

    /**
     * Pack, size and set location.
     */
    protected void postInit() {
        pack();

        int w = 550;
        int h = 400;
        setSize((getSize().width < w) ? w : getSize().width,
                (getSize().height < h) ? h : getSize().height);

        Container parent = getParent();
        if (parent != null) {
            GraphicsConfiguration gc = parent.getGraphicsConfiguration();
            if (gc != null) {
                Rectangle rect = gc.getBounds();
                Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
                w = getSize().width;
                h = getSize().height;
                int nw = rect.width - insets.left - insets.right;
                int nh = rect.height - insets.left - insets.right;
                if (w > nw || h > nh) {
                    setSize((w > nw ? nw : w), (h > nh ? nh : h));
                }
            }
        }
        setLocationRelativeTo(parent);
    }

    /**
     * Starts the actual HTML export
     */
    protected void startExport(){
        doApplyChanges();
        savePreferences();
        doSaveHTML(tierSettings);
    }

    /**
     * The item state changed handling.
     *
     * @param tierSettings list of settings of the selected tiers
     */
    private void doSaveHTML(final List<TAITierSetting> tierSettings) {
        String fileName = promptForHTMLFileName();

        if (fileName != null) {
            try {
                File exportFile = new File(fileName);

                validateTierSettings();
                // apply changes that may not have been committed yet 
                doApplyChanges();
                encoderInfo.setTierSettings(tierSettings);
                HTMLRendererTimeAlignedInterlinear render = new HTMLRendererTimeAlignedInterlinear(transcription,
                        encoderInfo);
                render.renderToFile(exportFile);
            } catch (Exception e) {
                // FileNotFound, IO, Security, Null etc
                JOptionPane.showMessageDialog(this,
                        ElanLocale.getString(
                                "InterlinearizerOptionsDlg.Error.TextOut") + " \n" +
                                "(" + e.getMessage() + ")",
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    protected void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message,
                ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }
    /**
     * Initializes the dialogBox with the last preferred/used settings
     */
    private void loadPreferences() {
        Boolean boolPref;
        String stringPref;
        Integer intPref;
        Object objPref;

        boolPref = Preferences.getBool("ExportTimeAlignedInterlinear.selection", null);
        if(boolPref != null){
            selectionCheckBox.setSelected(boolPref);
        }
        boolPref = Preferences.getBool("ExportTimeAlignedInterlinear.useReferenceTier", null);
        if(boolPref != null){
            useReferenceTierCheckBox.setSelected(boolPref);
        }
        boolPref = Preferences.getBool("ExportTimeAlignedInterlinear.wrapWithinBlock", null);
        if(boolPref != null){
            wrapWithinBlockCheckBox.setSelected(boolPref);
        }
        boolPref = Preferences.getBool("ExportTimeAlignedInterlinear.showTimeLine", null);
        if(boolPref != null){
            showTimeLineCheckBox.setSelected(boolPref);
        }
        stringPref = Preferences.getString("ExportTimeAlignedInterlinear.timeFormat", null);
        if (stringPref != null) {
        	if (TimeFormatter.TIME_FORMAT.SSMS.toString().equals(stringPref)) {
        		timeFormatComboBox.setSelectedIndex(1);
        	} else if (TimeFormatter.TIME_FORMAT.MS.toString().equals(stringPref)) {
        		timeFormatComboBox.setSelectedIndex(0);
        	} // by default the first item is selected
        }


        boolPref = Preferences.getBool("ExportTimeAlignedInterlinear.alignLeft", null);
        if(boolPref != null){
            alignLeftCheckBox.setSelected(boolPref);
        }
        boolPref = Preferences.getBool("ExportTimeAlignedInterlinear.showAnnotationBounds", null);
        if(boolPref != null){
            showAnnotationBoundsCheckBox.setSelected(boolPref);
        }

        intPref = Preferences.getInt("ExportTimeAlignedInterlinear.timeUnit", null);
        if(intPref != null){
            timeUnitTextField.setText(String.valueOf(intPref));
        }
        intPref = Preferences.getInt("ExportTimeAlignedInterlinear.blockSpace", null);
        if(intPref != null){
            blockSpaceTextField.setText(String.valueOf(intPref));
        }
        intPref = Preferences.getInt("ExportTimeAlignedInterlinear.leftMargin", null);
        if(intPref != null){
            leftMarginTextField.setText(String.valueOf(intPref));
        }
        intPref = Preferences.getInt("ExportTimeAlignedInterlinear.fontSize", null);
        if(intPref != null){
            fontSizeTextField.setText(String.valueOf(intPref));
        }

        objPref = Preferences.get("ExportTimeAlignedInterlinear.refTier", null);
        if(objPref != null){
            refTierComboBox.setSelectedItem(objPref);
        }

        objPref = Preferences.get("ExportTimeAlignedInterlinear.tierSettings", transcription);
        if (objPref != null) {
        	// expected: Map<String, List<String>>
        	@SuppressWarnings("unchecked")
			Map<String, List<String>> modelMap = (Map<String, List<String>>) objPref;
        	List<String> tierOrder = modelMap.get("TierOrder");
        	if (tierOrder != null) {
	        	for (String tName : tierOrder) {
	        		List<String> tierSet = modelMap.get(tName);
	        		int index = tierOrder.indexOf(tName);

    				for (int i = 0; i < model.getRowCount(); i++) {
    					if (tName.equals(model.getValueAt(i, TierExportTableModel.NAME_COL))) {
    						model.setValueAt(tierSet.contains("check"), i, TierExportTableModel.CHECK_COL);
    						model.setValueAt(tierSet.contains("ul"), i, TierExportTableModel5Columns.COL2);
    						model.setValueAt(tierSet.contains("b"), i, TierExportTableModel5Columns.COL3);
    						model.setValueAt(tierSet.contains("i"), i, TierExportTableModel5Columns.COL4);

    						if (i > index) {
    							model.moveRow(i, i, index);
    						}
    						break;
    					}
    				}
	        	}
        	}
        	tierSettings = convertSettings(model.getSelectedTiersWithSettings());
        }

    }

    /**
     * Saves the preferred settings used.
     */
    private void savePreferences(){

        Preferences.set("ExportTimeAlignedInterlinear.selection", selectionCheckBox.isSelected(),
                null, false, false);
        Preferences.set("ExportTimeAlignedInterlinear.useReferenceTier", useReferenceTierCheckBox.isSelected(),
                null, false, false);
        Preferences.set("ExportTimeAlignedInterlinear.wrapWithinBlock", wrapWithinBlockCheckBox.isSelected(),
                null, false, false);
        Preferences.set("ExportTimeAlignedInterlinear.showTimeLine", showTimeLineCheckBox.isSelected(),
        		null, false, false);
        // store time format locale/language independent
        String format = TimeFormatter.TIME_FORMAT.HHMMSSMS.toString();
        if (timeFormatComboBox.getSelectedIndex() == 1) {
        	format = TimeFormatter.TIME_FORMAT.SSMS.toString();
        } else if (timeFormatComboBox.getSelectedIndex() == 2) {
        	format = TimeFormatter.TIME_FORMAT.MS.toString();
        }
        Preferences.set("ExportTimeAlignedInterlinear.timeFormat", format,
        		null, false, false);
        Preferences.set("ExportTimeAlignedInterlinear.alignLeft", alignLeftCheckBox.isSelected(),
        		null, false, false);
        Preferences.set("ExportTimeAlignedInterlinear.showAnnotationBounds", showAnnotationBoundsCheckBox.isSelected(),
        		null, false, false);

        // doApplyChanges has to be called before this, so that timeUnit is updated
        Preferences.set("ExportTimeAlignedInterlinear.timeUnit", timeUnit, null, false,
                false);
        Preferences.set("ExportTimeAlignedInterlinear.blockSpace", blockSpace, null, false,
                false);
        Preferences.set("ExportTimeAlignedInterlinear.leftMargin", leftMargin, null, false,
                false);
        Preferences.set("ExportTimeAlignedInterlinear.fontSize", fontSize, null, false,
                false);

        Preferences.set("ExportTimeAlignedInterlinear.refTier", refTierComboBox.getSelectedItem(),
                null, false, false);

        if (model.getRowCount() > 0) {
        	Map<String, List<String>> modelMap = new HashMap<String, List<String>>(model.getRowCount() + 1);
        	List<String> tierOrder = new ArrayList<String>(model.getRowCount());
        	modelMap.put("TierOrder", tierOrder);
        	for (int i = 0; i < model.getRowCount(); i++) {
        		tierOrder.add((String) model.getValueAt(i, TierExportTableModel.NAME_COL));
        		List<String> tsList = new ArrayList<String>(4);

        		if ((Boolean) model.getValueAt(i, TierExportTableModel.CHECK_COL)) {
        			tsList.add("check");
        		}
        		if ((Boolean) model.getValueAt(i, TierExportTableModel5Columns.COL2)) {
        			tsList.add("ul");
        		}
        		if ((Boolean) model.getValueAt(i, TierExportTableModel5Columns.COL3)) {
        			tsList.add("b");
        		}
        		if ((Boolean) model.getValueAt(i, TierExportTableModel5Columns.COL4)) {
        			tsList.add("i");
        		}

        		modelMap.put((String) model.getValueAt(i, TierExportTableModel.NAME_COL), tsList);
        	}

            Preferences.set("ExportTimeAlignedInterlinear.tierSettings", modelMap,
                    transcription, false, false);
        }
    }

    /**
     * Sets reference true, while making sure that every other tier is not set as reference.
     */
    private void setReference(String refTier) {
        for (TAITierSetting ts : tierSettings) {
            if (ts.getTierName().equals(refTier)) {
                    ts.setReference(true);
            } else {
                ts.setReference(false);
            }
        }
    }

    /** Gets a tierSetting by Id
     *
     * @return the wanted tierSetting, otherwise null
     * */
    private TAITierSetting getTierSettingById(String tierName) {
        for (TAITierSetting ts : tierSettings) {
            if (ts.getTierName().equals(tierName)) {
                return ts;
            }
        }
        return null;
    }

    /**
     * Validates that tierSettings has either zero or one reference tier.
     * If this is not the case, the first tier is used as default.
     */
    private void validateTierSettings() {
        int numberOfRefTiers = 0;
        for (TAITierSetting ts : tierSettings) {
            if (ts.isReference()) {
                numberOfRefTiers++;
            }
        }
        if (numberOfRefTiers > 1) {
            JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("ExportTimeAlignedInterlinear.Message.refError"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);

            String defaultTier = tierSettings.get(0).getTierName();
            setReference(defaultTier);
            refTierComboBox.setSelectedItem(defaultTier);
        }
    }

    /**
     * Moves selected tiers up in the list of tiers.
     */
    private void moveUp() {
        if ((tierTable == null) || (model == null) ||
                (model.getRowCount() < 2)) {
            return;
        }

        int[] selected = tierTable.getSelectedRows();

        for (int element : selected) {
            int row = element;

            if ((row > 0) && !tierTable.isRowSelected(row - 1)) {
                model.moveRow(row, row, row - 1);
                tierTable.changeSelection(row, 0, true, false);
                tierTable.changeSelection(row - 1, 0, true, false);
            }
        }
    }

    /**
     * Moves selected tiers up in the list of tiers.
     */
    private void moveDown() {
        if ((tierTable == null) || (model == null) ||
                (model.getRowCount() < 2)) {
            return;
        }

        int[] selected = tierTable.getSelectedRows();

        for (int i = selected.length - 1; i >= 0; i--) {
            int row = selected[i];

            if ((row < (model.getRowCount() - 1)) &&
                    !tierTable.isRowSelected(row + 1)) {
                model.moveRow(row, row, row + 1);
                tierTable.changeSelection(row, 0, true, false);
                tierTable.changeSelection(row + 1, 0, true, false);
            }
        }
    }

    /**
     * prompt for HTML file name and location
     * */
    private String promptForHTMLFileName() {
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("ExportDialog.ExportToFile"), FileChooser.SAVE_DIALOG,
                FileExtension.HTML_ONLY_EXT, "LastUsedExportDir");

        File exportFile = chooser.getSelectedFile();
        if (exportFile != null) {
            return exportFile.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Converts generic tier export settings to time aligned export specific
     * settings.
     * @param settings list of export settings
     * @return a list of specific export settings
     */
    private List<TAITierSetting> convertSettings(List<TierExportSetting> settings) {
    	List<TAITierSetting> tsList = new ArrayList<TAITierSetting>(settings.size());
    	for (TierExportSetting tes : settings) {
    		tsList.add(new TAITierSetting(tes.tierName, tes.c2, tes.c3, tes.c4));
    	}
    	return tsList;
    }

    /**
     * The action performed event handling.
     *
     * @param event the action event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == applyChangesButton) {
            doApplyChanges();
            savePreferences();
        } else if (source == restoreDefaultsButton) {
            setDefaultValues();
            doApplyChanges();
            savePreferences();
        } else if (source == printButton) {
            startExport();
        } else if (source == upButton) {
            moveUp();
        } else if (source == downButton) {
            moveDown();
        } else if (source == closeButton) {
            doApplyChanges();
            setVisible(false);
            dispose();
        }
    }

    /**
     * The item state changed handling.
     *
     * @param ie the ItemEvent
     */
    @Override
    public void itemStateChanged(ItemEvent ie) {
        final Object source = ie.getSource();

        if (source == useReferenceTierCheckBox) {
            if (useReferenceTierCheckBox.isSelected()) {
                wrapWithinBlockCheckBox.setEnabled(true);
                refTierComboBox.setEnabled(true);
            } else {
                wrapWithinBlockCheckBox.setEnabled(false);
                refTierComboBox.setEnabled(false);
            }
        } else if (source == showTimeLineCheckBox) {
        	timeFormatComboBox.setEnabled(showTimeLineCheckBox.isSelected());
        }
    }
}
