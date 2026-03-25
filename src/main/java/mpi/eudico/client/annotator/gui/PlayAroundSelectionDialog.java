package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


/**
 * A dialog to change the "play around selection" value. This sets the amount of milliseconds to add to both sides of the
 * selection in order to play a wider segment than the selection.
 *
 * @author Han Sloetjes
 * @version Jul 2009 removed the references to a PlayAroundSelectionCA and a ViewerManager, the mode and value are now stored
 *     as general preferences.
 */
@SuppressWarnings("serial")
public class PlayAroundSelectionDialog extends ClosableDialog implements ItemListener,
                                                                         ActionListener {
    private JPanel content;
    private JLabel unitLabel;
    private JRadioButton framesRB;
    private JRadioButton msRB;
    private JButton okButton;
    private JButton cancelButton;
    private ElanSlider slider;

    /**
     * the maximum for the ms mode, for frames mode is max/40
     */
    private final int maxMsOffset = 5000;
    private int curValue = 500; // 500 ms
    private String mode = "ms"; // or frames

    /**
     * Creates a new PlayAroundSelectionDialog instance
     *
     * @param owner the parent frame
     */
    public PlayAroundSelectionDialog(Frame owner) {
        super(owner, true);

        String stringPref = Preferences.getString("PlayAroundSelection.Mode", null);
        if (stringPref != null) {
            mode = stringPref;
        }
        Integer intPref = Preferences.getInt("PlayAroundSelection.Value", null);
        if (intPref != null) {
            curValue = intPref.intValue();
        }

        initComponents();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setTitle(ElanLocale.getString("CommandActions.PlayAroundSelection"));
        setSize(350, 240);

        content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(6, 6, 6, 6));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;

        Insets inset = new Insets(2, 2, 12, 2);
        gbc.insets = inset;

        JPanel unitsPanel = new JPanel(new GridBagLayout());
        unitLabel = new JLabel(ElanLocale.getString("PlayAroundSelDialog.UnitsLabel"));

        ButtonGroup unitsGroup = new ButtonGroup();
        msRB = new JRadioButton(ElanLocale.getString("PlayAroundSelDialog.Ms"));
        msRB.setSelected(true);
        framesRB = new JRadioButton(ElanLocale.getString("PlayAroundSelDialog.Frames"));
        unitsGroup.add(msRB);
        unitsGroup.add(framesRB);
        if (!mode.equals("ms")) {
            framesRB.setSelected(true);
        }
        msRB.addItemListener(this);
        framesRB.addItemListener(this);

        GridBagConstraints gbcon = new GridBagConstraints();
        gbcon.anchor = GridBagConstraints.WEST;
        unitsPanel.add(unitLabel, gbcon);
        gbcon.gridy = 1;
        unitsPanel.add(msRB, gbcon);
        gbcon.gridy = 2;
        unitsPanel.add(framesRB, gbcon);

        gbc.fill = GridBagConstraints.NONE;
        content.add(unitsPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;

        slider = new ElanSlider(msRB.getText(), 0, maxMsOffset, curValue, null);
        content.add(slider, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        okButton = new JButton(ElanLocale.getString("Button.OK"));
        okButton.addActionListener(this);
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        content.add(buttonPanel, gbc);
        setContentPane(content);
    }

    /**
     * <b>Note: </b>to avoid unnecessary GUI updates this method reacts on a
     * DESELECTED event. This prevents updates when an already selected item is clicked again. This implementation should be
     * changed when more then two radio buttons are in this ButtonGroup.
     *
     * @param e the change event
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        if ((e.getSource() == msRB) && (e.getStateChange() == ItemEvent.DESELECTED)) {
            // add the frames related ElanSlider
            // ElanSlider should have setMinimum and setMaximum methods ?
            curValue = slider.getValue();
            content.remove(slider);

            curValue /= 40; // use a default of 40 ms per frame

            int max = maxMsOffset / 40;
            if (curValue > max) {
                curValue = max;
            }

            slider = new ElanSlider(framesRB.getText(), 0, max, curValue, null);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            gbc.weightx = 1.0;
            content.add(slider, gbc);
            content.revalidate();
        } else if ((e.getSource() == framesRB) && (e.getStateChange() == ItemEvent.DESELECTED)) {
            // add the ms related ElanSlider
            // ElanSlider should have setMinimum and setMaximum methods    ?
            //            curValue = (int) (slider.getValue() * vm.getMasterMediaPlayer()
            //                                                    .getMilliSecondsPerSample());
            curValue = slider.getValue() * 40; // use a default of 40 ms per frame

            if (curValue > maxMsOffset) {
                curValue = maxMsOffset;
            }

            content.remove(slider);
            slider = new ElanSlider(msRB.getText(), 0, maxMsOffset, curValue, null);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            gbc.weightx = 1.0;
            content.add(slider, gbc);
            content.revalidate();
        }
    }

    /**
     * The action event handling.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okButton) {
            if (slider != null) {
                int value = slider.getTextFieldValue();

                if (framesRB.isSelected()) {
                    // store mode as preference
                    Preferences.set("PlayAroundSelection.Mode", "frames", null, false);
                } else {
                    Preferences.set("PlayAroundSelection.Mode", "ms", null, false);
                }
                // store mode as preference
                Preferences.set("PlayAroundSelection.Value", Integer.valueOf(value), null, true);

            }
        }

        dispose();
    }
}
