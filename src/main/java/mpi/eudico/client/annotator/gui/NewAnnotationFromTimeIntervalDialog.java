package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.tier.TierExportTable;
import mpi.eudico.client.annotator.tier.TierExportTableModel;
import mpi.eudico.client.annotator.util.HhMmSsMssMaskFormatter;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.TimeFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;


/**
 * A Dialog for entering the begin and end time of an annotation and also the annotation value. The dialog also lists all the
 * tiers from which one or more tiers can be selected . The annotation is created on the selected tiers.
 */
@SuppressWarnings("serial")
public class NewAnnotationFromTimeIntervalDialog extends ClosableDialog implements ActionListener {

    private final TranscriptionImpl transcription;
    private final ViewerManager2 vm;

    private long beginTime = -1L;
    private long endTime = -1L;
    private String annotationValue;
    private List<String> returnedTiers = new ArrayList<String>();

    private JLabel limitsLabel;
    private JButton okButton;

    private JButton cancelButton;
    private JFormattedTextField btTextField;
    private JFormattedTextField etTextField;
    private JTextField annoTextField;

    private TierExportTable tierTable;
    private TierExportTableModel model;

    private long minBeginTime = 0L;
    private long maxEndTime = Long.MAX_VALUE;


    private Boolean actionApplied = false;


    /**
     * Constructor.
     *
     * @param viewerManager the viewer manager
     * @param owner the containing frame
     * @param modal the modal true or false identifier
     *
     * @throws HeadlessException when run in a headless environment
     */

    public NewAnnotationFromTimeIntervalDialog(ViewerManager2 viewerManager, Frame owner, boolean modal) throws
                                                                                                         HeadlessException {
        super(owner, modal);
        this.vm = viewerManager;
        this.transcription = (TranscriptionImpl) viewerManager.getTranscription();
        initComponentsForNewAnnotation();
        extractTiers();
    }

    /**
     * Returns the begin time.
     *
     * @return the entered begin time
     */
    public long getBeginTime() {
        return beginTime;
    }

    /**
     * Returns the end time.
     *
     * @return the entered end time
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Returns the value provided by the user for the annotation.
     *
     * @return the typed annotation value
     */
    public String getAnnotationValue() {
        return annotationValue;
    }

    /**
     * Returns the list of selected tiers.
     *
     * @return the selected tier(s)
     */
    public List<String> getTiers() {
        return returnedTiers;
    }

    /**
     * Returns whether the dialog is applied or cancelled.
     *
     * @return boolean value which indicates if OK button is pressed or not
     */
    public Boolean isActionApplied() {
        return actionApplied;
    }


    /**
     * Sets the initial values of start and end time in case of modifying an existing annotation, otherwise both fields are
     * initialized with 0's.
     *
     * @param begin the current begin time
     * @param end the current end time
     */
    public void setInterval(long begin, long end) {
        this.beginTime = begin;
        this.endTime = end;
        btTextField.setValue(TimeFormatter.toString(beginTime));
        etTextField.setValue(TimeFormatter.toString(endTime));
    }

    /**
     * Sets the begin time.
     *
     * @param begin begin time of an annotation
     */
    public void setBeginTime(long begin) {
        this.beginTime = begin;
        btTextField.setValue(TimeFormatter.toString(beginTime));
    }


    /**
     * Sets the end time.
     *
     * @param end end time of an annotation
     */
    public void setEndTime(long end) {
        this.endTime = end;
        etTextField.setValue(TimeFormatter.toString(endTime));

    }

    /**
     * The limits are provided as a visible suggestion but are not enforced. In case of an annotation on a top level tier the
     * limits will be 0 - media duration, in case of a dependent annotation, the parent's boundaries will serve as limits.
     *
     * @param minBT the minimal value for the begin time
     * @param maxET the maximal value for the end time
     */
    public void setLimits(long minBT, long maxET) {
        minBeginTime = minBT;
        maxEndTime = maxET;
        // update label
        limitsLabel.setText(String.format(ElanLocale.getString("ModifyTimesDialog.EnterTimes.Range"),
                                          TimeFormatter.toString(minBeginTime),
                                          TimeFormatter.toString(maxEndTime)));
        pack();
    }


    /**
     * Initialize components for the new dialog
     */
    private void initComponentsForNewAnnotation() {

        getContentPane().setLayout(new GridBagLayout());
        limitsLabel = new JLabel("", SwingConstants.CENTER);
        JLabel btLabel = new JLabel(ElanLocale.getString("Frame.GridFrame.ColumnBeginTime"), SwingConstants.TRAILING);
        JLabel etLabel = new JLabel(ElanLocale.getString("Frame.GridFrame.ColumnEndTime"), SwingConstants.TRAILING);
        JLabel annotationLabel =
            new JLabel(ElanLocale.getString("Frame.GridFrame.ColumnAnnotationValue"), SwingConstants.TRAILING);


        JPanel timeFramePanel = new JPanel(new GridBagLayout());
        timeFramePanel.setBorder(new TitledBorder(ElanLocale.getString("Frame.GridFrame.EnterTime.Text")));
        HhMmSsMssMaskFormatter timeFormatter = new HhMmSsMssMaskFormatter();
        btTextField = new JFormattedTextField(timeFormatter);
        etTextField = new JFormattedTextField(timeFormatter);
        annoTextField = new JTextField();

        model = new TierExportTableModel();
        tierTable = new TierExportTable(model, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JPanel tierPanel = new JPanel();
        tierPanel.setLayout(new GridBagLayout());
        tierPanel.setBorder(new TitledBorder(ElanLocale.getString("Frame.GridFrame.ColumnSelectTiers")));
        JScrollPane tierScroll = new JScrollPane(tierTable);
        tierScroll.setPreferredSize(new Dimension(100, 100));


        okButton = new JButton(ElanLocale.getString("Button.OK"));
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));

        Insets insets = new Insets(2, 6, 2, 6);

        Container contentPane = getContentPane();
        ((JComponent) contentPane).setBorder(new EmptyBorder(6, 8, 2, 8));


        GridBagConstraints lgbc = new GridBagConstraints();
        lgbc.anchor = GridBagConstraints.WEST;
        lgbc.insets = insets;


        GridBagConstraints rgbc = new GridBagConstraints();
        rgbc.gridx = 1;
        rgbc.anchor = GridBagConstraints.WEST;
        rgbc.insets = insets;
        rgbc.weightx = 1.0;

        rgbc.gridy = 0;
        rgbc.fill = GridBagConstraints.EAST;
        timeFramePanel.add(limitsLabel, rgbc);

        rgbc.fill = GridBagConstraints.HORIZONTAL;

        lgbc.gridy = 1;
        timeFramePanel.add(btLabel, lgbc);

        rgbc.gridy = 1;
        timeFramePanel.add(btTextField, rgbc);

        lgbc.gridy = 2;
        timeFramePanel.add(etLabel, lgbc);

        rgbc.gridy = 2;
        timeFramePanel.add(etTextField, rgbc);

        lgbc.gridy = 3;
        timeFramePanel.add(annotationLabel, lgbc);

        rgbc.gridy = 3;
        timeFramePanel.add(annoTextField, rgbc);


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        contentPane.add(timeFramePanel, gbc);


        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        tierPanel.add(tierScroll, gbc);


        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        contentPane.add(tierPanel, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = insets;
        contentPane.add(buttonPanel, gbc);


        okButton.addActionListener(this);
        cancelButton.addActionListener(this);

    }


    /**
     * Extract all tiers and fill the table.
     */
    private void extractTiers() {
        String activeTierName = null;

        if (vm.getMultiTierControlPanel() != null && vm.getMultiTierControlPanel().getActiveTier() != null) {
            TierImpl activeTier = (TierImpl) vm.getMultiTierControlPanel().getActiveTier();
            activeTierName = activeTier.getName();
        }

        if (transcription != null) {
            List<TierImpl> tiers = transcription.getTiers();

            model.extractTierNames(tiers, activeTierName);
        }
    }


    /**
     * Returns the tiers that have been selected in the table.
     *
     * @return a list of the selected tiers
     */
    private List<String> getSelectedTiers() {
        return model.getSelectedTiers();
    }


    /**
     * The buttons action event handling.
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == cancelButton) {
            actionApplied = false;
            setVisible(false);
            dispose();
        } else if (e.getSource() == okButton) {

            annotationValue = annoTextField.getText();
            beginTime = TimeFormatter.toMilliSeconds((String) btTextField.getValue());
            endTime = TimeFormatter.toMilliSeconds((String) etTextField.getValue());
            returnedTiers = getSelectedTiers();

            if (beginTime >= endTime) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info(String.format("The entered begin time (%d) is >= the end time (%d).", beginTime, endTime));
                }
                JOptionPane.showMessageDialog(this,
                                              ElanLocale.getString("Message.InvalidBeginEndTime"),
                                              ElanLocale.getString("Message.Warning"),
                                              JOptionPane.WARNING_MESSAGE);
                actionApplied = false;
            } else {
                setVisible(false);
                dispose();
                actionApplied = true;
            }
        }

    }

}
