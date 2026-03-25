package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.util.HhMmSsMssMaskFormatter;
import mpi.eudico.util.TimeFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A dialog for entering (typing) start and/or end time of an existing annotation. The current time format used is
 * hh:mm:ss:mss. This dialog could maybe also be used for new annotations?
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class TimeIntervalEditDialog extends ClosableDialog implements ActionListener {
    private JLabel messageLabel;
    private JLabel limitsLabel;
    private JButton okButton;
    private JButton cancelButton;
    private JFormattedTextField btTextField;
    private JFormattedTextField etTextField;

    private long beginTime = -1L;
    private long endTime = -1L;

    private long minBeginTime = 0L;
    private long maxEndTime = Long.MAX_VALUE;

    /**
     * Constructors of the super classes.
     *
     * @throws HeadlessException when run in a headless environment
     */
    public TimeIntervalEditDialog() throws
                                    HeadlessException {
        this((Frame) null, true);
    }

    /**
     * Constructor
     *
     * @param owner the containing frame
     *
     * @throws HeadlessException the exception thrown in an headless environment
     */
    public TimeIntervalEditDialog(Frame owner) throws
                                               HeadlessException {
        this(owner, null, true);
    }

    /**
     * Constructor
     *
     * @param owner the containing frame
     * @param modal the modal
     *
     * @throws HeadlessException thrown in an headless environment
     */
    public TimeIntervalEditDialog(Frame owner, boolean modal) throws
                                                              HeadlessException {
        this(owner, null, modal);
    }

    /**
     * Constructor
     *
     * @param owner the containing frame
     * @param title the title
     *
     * @throws HeadlessException thrown in an headless environment
     */
    public TimeIntervalEditDialog(Frame owner, String title) throws
                                                             HeadlessException {
        this(owner, title, true);
    }

    /**
     * Constructor
     *
     * @param owner the containing frame
     * @param title the title
     * @param modal the modal
     *
     * @throws HeadlessException thrown in an headless environment
     */
    public TimeIntervalEditDialog(Frame owner, String title, boolean modal) throws
                                                                            HeadlessException {
        this(owner, title, modal, null);
    }

    /**
     * Constructor
     *
     * @param owner the containing frame
     * @param title the title
     * @param modal the modal
     * @param gc the graphics configuration
     */
    public TimeIntervalEditDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
        initComponents();
    }

    /**
     * Constructor
     *
     * @param owner the containing frame
     *
     * @throws HeadlessException thrown in an headless environment
     */
    public TimeIntervalEditDialog(Dialog owner) throws
                                                HeadlessException {
        this(owner, null, true);
    }

    /**
     * Constructor
     *
     * @param owner the containing frame
     * @param modal the modal
     *
     * @throws HeadlessException thrown in an headless environment
     */
    public TimeIntervalEditDialog(Dialog owner, boolean modal) throws
                                                               HeadlessException {
        this(owner, null, modal);
    }

    /**
     * Constructor
     *
     * @param owner the containing frame
     * @param title the modal
     *
     * @throws HeadlessException thrown in an headless environment
     */
    public TimeIntervalEditDialog(Dialog owner, String title) throws
                                                              HeadlessException {
        this(owner, title, true, null);
    }

    /**
     * Constructor
     *
     * @param owner the containing frame
     * @param title the title
     * @param modal the modal
     *
     * @throws HeadlessException thrown in an headless environment
     */
    public TimeIntervalEditDialog(Dialog owner, String title, boolean modal) throws
                                                                             HeadlessException {
        this(owner, title, modal, null);
    }

    /**
     * Constructor
     *
     * @param owner the containing frame
     * @param title the title
     * @param modal the modal
     * @param gc the graphics configuration
     *
     * @throws HeadlessException thrown in an headless environment
     */
    public TimeIntervalEditDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) throws
                                                                                                       HeadlessException {
        super(owner, title, modal, gc);
        initComponents();
    }

    /**
     * Sets the initial values of start and end time in case of modifying an existing annotation, otherwise both fields are
     * initialized with 0's.
     *
     * @param begin the current begin time
     * @param end the current end time
     */
    public void setInterval(long begin, long end) {
        beginTime = begin;
        endTime = end;
        btTextField.setValue(TimeFormatter.toString(beginTime));
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
     * Returns the entered begin and end time.
     *
     * @return the entered begin and end time as an long array of size 2
     */
    public long[] getValue() {
        return new long[] {beginTime, endTime};
    }

    /**
     * Returns the begin time.
     *
     * @return the entered begin time, currently it is not prevented that {@code start >= end}
     */
    public long getBeginTime() {
        return beginTime;
    }

    /**
     * Returns the end time.
     *
     * @return the entered end time, currently it is not prevented that {@code end <= start}
     */
    public long getEndTime() {
        return endTime;
    }

    private void initComponents() {
        getContentPane().setLayout(new GridBagLayout());
        messageLabel = new JLabel(ElanLocale.getString("ModifyTimesDialog.EnterTimes"), SwingConstants.CENTER);
        limitsLabel = new JLabel("", SwingConstants.CENTER);
        JLabel btLabel = new JLabel(ElanLocale.getString("Frame.GridFrame.ColumnBeginTime"), SwingConstants.TRAILING);
        JLabel etLabel = new JLabel(ElanLocale.getString("Frame.GridFrame.ColumnEndTime"), SwingConstants.TRAILING);

        HhMmSsMssMaskFormatter timeFormatter = new HhMmSsMssMaskFormatter();
        btTextField = new JFormattedTextField(timeFormatter);
        etTextField = new JFormattedTextField(timeFormatter);

        okButton = new JButton(ElanLocale.getString("Button.OK"));
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        //buttonPanel.add(okButton);
        //buttonPanel.add(cancelButton);
        okButton.addActionListener(null);
        cancelButton.addActionListener(null);


        Container cp = getContentPane();
        ((JComponent) cp).setBorder(new EmptyBorder(6, 8, 2, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 4, 4, 4);
        cp.add(messageLabel, gbc);

        gbc.gridy = 1;
        //        gbc.insets = new Insets (4, 4, 4, 4);
        cp.add(limitsLabel, gbc);

        gbc.gridy = 2;
        //gbc.fill = GridBagConstraints.NONE;
        //gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        //        gbc.insets = new Insets (4, 4, 4, 4);
        cp.add(btLabel, gbc);

        gbc.gridy = 3;
        cp.add(etLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        cp.add(btTextField, gbc);
        gbc.gridy = 3;
        cp.add(etTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        //        gbc.fill = GridBagConstraints.NONE;
        //        gbc.weightx = 0;
        gbc.insets = new Insets(12, 4, 4, 4);
        gbc.anchor = GridBagConstraints.EAST;
        cp.add(okButton, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        cp.add(cancelButton, gbc);

        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        pack();
        setLocationRelativeTo(getParent());
        //getRootPane().setDefaultButton(okButton);
    }

    /**
     * The buttons action event handling. "Apply" converts the current time value to long values.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelButton) {
            // ignore values
            setVisible(false);
            dispose();
        } else if (e.getSource() == okButton) {
            beginTime = TimeFormatter.toMilliSeconds((String) btTextField.getValue());
            endTime = TimeFormatter.toMilliSeconds((String) etTextField.getValue());

            if (beginTime >= endTime) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info(String.format("The entered begin time (%d) is >= the end time (%d).", beginTime, endTime));
                }
            }
            setVisible(false);
            dispose();
        }
    }

}
