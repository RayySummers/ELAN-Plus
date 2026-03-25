package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;
import nl.mpi.media.spectrogram.SpectrogramSettings;
import nl.mpi.media.spectrogram.WindowFunction;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * A dialog for configuring various settings for the Spectrogram viewer, including settings for the Fourier transform. The
 * settings are in a tab pane with two tabs.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class SpectrogramSettingsDialog extends ClosableDialog implements ActionListener,
                                                                         ItemListener {
    private final SpectrogramSettings curSettings;
    private JTabbedPane tabPane;
    private JPanel transformPanel;
    private JPanel displayPanel;
    private JButton applyButton;
    private JButton cancelButton;
    private JButton defaultsButton;
    // display tab
    private JFormattedTextField minFreqField;
    private JFormattedTextField maxFreqField;
    private JCheckBox adaptiveCB;
    private JRadioButton grayRB;
    private JRadioButton revGrayRB;
    private JRadioButton colorRB;
    private JPanel fgColorPanel;
    private JPanel bgColorPanel;
    private JPanel fgColorPreview;
    private JPanel bgColorPreview;
    private JButton fgColorButton;
    private JButton bgColorButton;
    private JLabel brightnessLabel;
    private JLabel fgBrightLabel;
    private JLabel bgBrightLabel;
    private JSpinner fgBrightSpinner;
    private JSpinner bgBrightSpinner;
    // transform tab
    private JComboBox<String> functionsCombo;
    private JFormattedTextField windowDurationTF; // or spinner?
    private JFormattedTextField strideDurationTF; // or spinner?

    /**
     * Constructor, initializing the UI with the provided settings.
     *
     * @param owner the owner frame
     * @param curSettings the current settings, used to populate the user interface
     *
     * @throws HeadlessException in a headless environment
     */
    public SpectrogramSettingsDialog(Frame owner, SpectrogramSettings curSettings) throws
                                                                                   HeadlessException {
        super(owner, true);
        this.curSettings = curSettings;
        initComponents();
        applySettingsToGui(curSettings);
        WindowLocationAndSizeManager.postInit(this, "SpectrogramSettingsDialog");
    }

    /**
     * Build the user interface.
     */
    private void initComponents() {
        setTitle(ElanLocale.getString("SpectrogramSettingsDialog.Title"));
        tabPane = new JTabbedPane(JTabbedPane.TOP);
        createDisplayPanel();
        tabPane.addTab(ElanLocale.getString("SpectrogramSettingsDialog.Tab.Display"), displayPanel);
        createTransformPanel();
        tabPane.addTab(ElanLocale.getString("SpectrogramSettingsDialog.Tab.Transform"), transformPanel);
        getContentPane().setLayout(new GridBagLayout());
        Insets insets = new Insets(2, 6, 2, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        getContentPane().add(tabPane, gbc);

        JPanel buttonPanel = createButtonPanel();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0d;
        gbc.weighty = 0.0d;
        getContentPane().add(buttonPanel, gbc);

        Integer tabIndex = Preferences.getInt("SpectrogramSettingsDialog.ActiveTab", null);
        if (tabIndex != null && tabIndex.intValue() < tabPane.getTabCount()) {
            tabPane.setSelectedIndex(tabIndex.intValue());
        }
        pack();
        setSize(Math.max(getSize().width, 400), Math.max(getSize().height, 400));
    }

    /**
     * Creates the tab pane for display and visualization related settings.
     */
    private void createDisplayPanel() {
        displayPanel = new JPanel(new GridBagLayout());
        JLabel minFrLabel = new JLabel(ElanLocale.getString("SpectrogramSettingsDialog.MinDisplayFrequency"));
        JLabel maxFrLabel = new JLabel(ElanLocale.getString("SpectrogramSettingsDialog.MaxDisplayFrequency"));
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setMaximumFractionDigits(1);
        nf.setMinimumFractionDigits(1);
        minFreqField = new JFormattedTextField(nf);
        minFreqField.setHorizontalAlignment(JTextField.RIGHT);
        //maxFreqField = new JTextField(10);
        maxFreqField = new JFormattedTextField(nf);
        maxFreqField.setHorizontalAlignment(JTextField.RIGHT);
        adaptiveCB = new JCheckBox(ElanLocale.getString("SpectrogramSettingsDialog.AdaptiveContrast"));
        adaptiveCB.addItemListener(this);
        JLabel colorLabel = new JLabel(ElanLocale.getString("SpectrogramSettingsDialog.ColorScheme"));
        grayRB = new JRadioButton(ElanLocale.getString("SpectrogramSettingsDialog.Color.Gray"), true);
        revGrayRB = new JRadioButton(ElanLocale.getString("SpectrogramSettingsDialog.Color.ReversedGray"));
        colorRB = new JRadioButton(ElanLocale.getString("SpectrogramSettingsDialog.Color.Gradient"));
        fgColorPanel = createColorPanel(true);
        bgColorPanel = createColorPanel(false);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(grayRB);
        buttonGroup.add(revGrayRB);
        buttonGroup.add(colorRB);
        grayRB.addItemListener(this);
        revGrayRB.addItemListener(this);
        colorRB.addItemListener(this);
        brightnessLabel = new JLabel(ElanLocale.getString("SpectrogramSettingsDialog.Brightness.Label"));
        fgBrightLabel = new JLabel(ElanLocale.getString("SpectrogramSettingsDialog.Brightness.FG"));
        bgBrightLabel = new JLabel(ElanLocale.getString("SpectrogramSettingsDialog.Brightness.BG"));
        fgBrightSpinner = new JSpinner(new SpinnerNumberModel(0, -30, 30, 5));
        bgBrightSpinner = new JSpinner(new SpinnerNumberModel(0, -30, 30, 5));


        Insets insets = new Insets(2, 6, 2, 6);
        Insets topInsets = new Insets(15, 6, 2, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        displayPanel.add(minFrLabel, gbc);

        gbc.gridx = 1;
        displayPanel.add(minFreqField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        displayPanel.add(maxFrLabel, gbc);

        gbc.gridx = 1;
        displayPanel.add(maxFreqField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = topInsets;
        displayPanel.add(colorLabel, gbc);

        Insets tabInsets = new Insets(2, 15, 2, 6);
        gbc.gridy++;
        gbc.insets = tabInsets;
        displayPanel.add(grayRB, gbc);

        gbc.gridy++;
        displayPanel.add(revGrayRB, gbc);

        gbc.gridy++;
        displayPanel.add(colorRB, gbc);

        Insets tab2Insets = new Insets(2, 30, 2, 6);
        gbc.gridy++;
        gbc.insets = tab2Insets;
        displayPanel.add(fgColorPanel, gbc);

        gbc.gridy++;
        displayPanel.add(bgColorPanel, gbc);

        gbc.gridy++;
        gbc.insets = topInsets;
        displayPanel.add(brightnessLabel, gbc);

        gbc.gridy++;
        gbc.insets = tabInsets;
        displayPanel.add(adaptiveCB, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        displayPanel.add(fgBrightLabel, gbc);

        gbc.gridx = 1;
        displayPanel.add(fgBrightSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        displayPanel.add(bgBrightLabel, gbc);

        gbc.gridx = 1;
        displayPanel.add(bgBrightSpinner, gbc);

        JPanel filler = new JPanel();
        filler.setPreferredSize(new Dimension(10, 10));
        gbc.gridy++;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1.0;
        displayPanel.add(filler, gbc);
    }

    /**
     * Creates a tab pane for Fourier transform related settings.
     */
    private void createTransformPanel() {
        transformPanel = new JPanel(new GridBagLayout());
        JLabel analysisLabel = new JLabel(ElanLocale.getString("SpectrogramSettingsDialog.SpectralAnalysis"));
        JLabel winFunctionLabel = new JLabel(ElanLocale.getString("SpectrogramSettingsDialog.WindowFunction"));
        JLabel winDurLabel = new JLabel(ElanLocale.getString("SpectrogramSettingsDialog.WindowDuration"));
        JLabel strideDurLabel = new JLabel(ElanLocale.getString("SpectrogramSettingsDialog.StrideDuration"));
        functionsCombo = new JComboBox<String>();
        functionsCombo.addItem(WindowFunction.WF_NAME.HANN.getDisplayName());
        functionsCombo.addItem(WindowFunction.WF_NAME.GAUSSIAN.getDisplayName());
        functionsCombo.addItem(WindowFunction.WF_NAME.GAUSSIAN_TUKEY.getDisplayName());
        functionsCombo.addItem(WindowFunction.WF_NAME.HAMMING.getDisplayName());
        functionsCombo.addItem(WindowFunction.WF_NAME.BARTLETT.getDisplayName());
        functionsCombo.addItem(WindowFunction.WF_NAME.WELCH.getDisplayName());
        functionsCombo.addItem(WindowFunction.WF_NAME.BLACKMAN_HARRIS.getDisplayName());
        functionsCombo.addItem(WindowFunction.WF_NAME.TUKEY.getDisplayName());
        functionsCombo.addItem(WindowFunction.WF_NAME.KAISER_BESSEL.getDisplayName());
        functionsCombo.addItem(WindowFunction.WF_NAME.TRIANGULAR.getDisplayName());
        functionsCombo.addItem(WindowFunction.WF_NAME.RECTANGULAR.getDisplayName());
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setMaximumFractionDigits(4);
        nf.setMinimumFractionDigits(3);
        windowDurationTF = new JFormattedTextField();
        strideDurationTF = new JFormattedTextField();
        windowDurationTF.setHorizontalAlignment(JTextField.RIGHT);
        strideDurationTF.setHorizontalAlignment(JTextField.RIGHT);

        Insets insets = new Insets(2, 6, 2, 6);
        Insets topInsets = new Insets(15, 6, 2, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridwidth = 2;
        transformPanel.add(analysisLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = topInsets;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        transformPanel.add(winFunctionLabel, gbc);

        gbc.gridx = 1;
        transformPanel.add(functionsCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        transformPanel.add(winDurLabel, gbc);

        gbc.gridx = 1;
        transformPanel.add(windowDurationTF, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = insets;
        transformPanel.add(strideDurLabel, gbc);

        gbc.gridx = 1;
        transformPanel.add(strideDurationTF, gbc);

        JPanel filler = new JPanel();
        filler.setPreferredSize(new Dimension(10, 10));
        gbc.gridy++;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1.0;
        transformPanel.add(filler, gbc);
    }

    /**
     * Creates a small panel for selecting and showing a color.
     *
     * @param foreground if {@code true} it represents the foreground color, the background color otherwise
     *
     * @return the color panel
     */
    private JPanel createColorPanel(boolean foreground) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 6, 0, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        if (foreground) {
            panel.add(new JLabel(ElanLocale.getString("SpectrogramSettingsDialog.Color.Gradient.FG")), gbc);
        } else {
            panel.add(new JLabel(ElanLocale.getString("SpectrogramSettingsDialog.Color.Gradient.BG")), gbc);
        }

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JPanel colPanel = new JPanel();
        colPanel.setPreferredSize(new Dimension(20, 20));
        colPanel.setBackground(Color.WHITE);
        colPanel.setBorder(new LineBorder(Color.DARK_GRAY, 1));
        panel.add(colPanel, gbc);

        JButton browseButton = new JButton(ElanLocale.getString("Button.Select"));
        browseButton.addActionListener(this);
        gbc.gridx = 2;
        panel.add(browseButton, gbc);

        if (foreground) {
            fgColorPreview = colPanel;
            fgColorButton = browseButton;
        } else {
            bgColorPreview = colPanel;
            bgColorButton = browseButton;
        }

        return panel;
    }

    /*
     * Enables/disables the components of a panel.
     */
    private void enablePanel(JPanel panel, boolean enable) {
        for (Component cp : panel.getComponents()) {
            cp.setEnabled(enable);
        }
    }

    /*
     * Enables/disables the color panels
     */
    private void updatePanels() {
        enablePanel(fgColorPanel, colorRB.isSelected());
        enablePanel(bgColorPanel, colorRB.isSelected());
    }

    /**
     * Creates the button panel.
     *
     * @return the main button panel.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 6, 2));
        applyButton = new JButton(ElanLocale.getString("Button.Apply"));
        applyButton.addActionListener(this);
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        defaultsButton = new JButton(ElanLocale.getString("SpectrogramSettingsDialog.ResetDefaults"));
        defaultsButton.addActionListener(this);
        panel.add(applyButton);
        panel.add(cancelButton);
        panel.add(defaultsButton);

        return panel;
    }

    /**
     * After initialization of the dialog, the UI elements are updated based on the specified settings.
     *
     * @param settings the settings to apply
     */
    private void applySettingsToGui(SpectrogramSettings settings) {
        // display settings
        minFreqField.setValue(settings.getMinDisplayFrequency());
        maxFreqField.setValue(settings.getMaxDisplayFrequency());
        adaptiveCB.setSelected(settings.isAdaptiveContrast());
        if (settings.getColorScheme() == SpectrogramSettings.COLOR_SCHEME.GRAY) {
            grayRB.setSelected(true);
        } else if (settings.getColorScheme() == SpectrogramSettings.COLOR_SCHEME.REVERSED_GRAY) {
            revGrayRB.setSelected(true);
        } else if (settings.getColorScheme() == SpectrogramSettings.COLOR_SCHEME.BI_COLOR) {
            colorRB.setSelected(true);
            fgColorPreview.setBackground(settings.getColor1());
            bgColorPreview.setBackground(settings.getColor2());
        }
        fgBrightSpinner.setValue(settings.getUpperValueAdjustment());
        bgBrightSpinner.setValue(settings.getLowerValueAdjustment());
        // transform settings
        functionsCombo.setSelectedItem(settings.getWindowFunction());
        windowDurationTF.setValue(settings.getWindowDurationSec());
        strideDurationTF.setValue(settings.getStrideDurationSec());
        updatePanels();
    }

    private void closeDialog() {
        Preferences.set("SpectrogramSettingsDialog.ActiveTab", tabPane.getSelectedIndex(), null);
        WindowLocationAndSizeManager.storeLocationAndSizePreferences(this, "SpectrogramSettingsDialog");
        this.setVisible(false);
        dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == applyButton) {
            // apply GUI settings to current SpectrogramSettings and close the dialog
            try {
                minFreqField.commitEdit();
                maxFreqField.commitEdit();

                curSettings.setMinMaxDisplayFrequency(((Number) minFreqField.getValue()).doubleValue(),
                                                      ((Number) maxFreqField.getValue()).doubleValue());

            } catch (ParseException pe) {
                //continue or return and give the field the focus
            }
            try {
                windowDurationTF.commitEdit();
                strideDurationTF.commitEdit();

                curSettings.setWindowAndStride(((Number) windowDurationTF.getValue()).doubleValue(),
                                               ((Number) strideDurationTF.getValue()).doubleValue());

            } catch (ParseException pe) {
                //continue or return and give the field the focus
            }

            curSettings.setAdaptiveContrast(adaptiveCB.isSelected());
            if (grayRB.isSelected()) {
                curSettings.setColorScheme(SpectrogramSettings.COLOR_SCHEME.GRAY);
            } else if (revGrayRB.isSelected()) {
                curSettings.setColorScheme(SpectrogramSettings.COLOR_SCHEME.REVERSED_GRAY);
            } else if (colorRB.isSelected()) {
                curSettings.setColorScheme(SpectrogramSettings.COLOR_SCHEME.BI_COLOR);
                curSettings.setColor1(fgColorPreview.getBackground());
                curSettings.setColor2(bgColorPreview.getBackground());
            }
            curSettings.setLowerValueAdjustment(((Number) bgBrightSpinner.getValue()).doubleValue());
            curSettings.setUpperValueAdjustment(((Number) fgBrightSpinner.getValue()).doubleValue());
            curSettings.setWindowFunction((String) functionsCombo.getSelectedItem());

            closeDialog();
        } else if (e.getSource() == cancelButton) {
            closeDialog();
        } else if (e.getSource() == defaultsButton) {
            // update the user interface but wait for "apply" before actually changing the settings
            applySettingsToGui(new SpectrogramSettings()); // will be initialized to default settings
        } else if (e.getSource() == fgColorButton) {
            ColorDialog colDiag = new ColorDialog(this, fgColorPreview.getBackground());
            Color nextColor = colDiag.chooseColor();
            if (nextColor != null) {
                fgColorPreview.setBackground(nextColor);
            }
        } else if (e.getSource() == bgColorButton) {
            ColorDialog colDiag = new ColorDialog(this, bgColorPreview.getBackground());
            Color nextColor = colDiag.chooseColor();
            if (nextColor != null) {
                bgColorPreview.setBackground(nextColor);
            }
        }

    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == adaptiveCB) {
            fgBrightLabel.setEnabled(!adaptiveCB.isSelected());
            bgBrightLabel.setEnabled(!adaptiveCB.isSelected());
            fgBrightSpinner.setEnabled(!adaptiveCB.isSelected());
            bgBrightSpinner.setEnabled(!adaptiveCB.isSelected());
        } else {
            updatePanels();
        }
    }

}
