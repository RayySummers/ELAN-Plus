package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;

import javax.swing.*;
import java.awt.*;


/**
 * A message panel that shows the user a message with an additional option (a CheckBox). The typical and default use is a
 * message with a "Don't show this again" checkbox, but it can also be used in other situations where the options that
 * {@link JOptionPane} provides are not so practical.
 *
 * @author Han Sloetjes, MPI
 */
@SuppressWarnings("serial")
public class TextExtraOptionPane extends JPanel {
    private final JLabel messageLabel;
    private final JCheckBox extraOptionCB; // was showAgainCB

    /**
     * Creates a new TextExtraOptionPane instance without message and default "don't show again" option.
     */
    public TextExtraOptionPane() {
        this("");
    }

    /**
     * Creates a new TextExtraOptionPane instance with the specified message and default "don't show again" option.
     *
     * @param message the message or question to show
     */
    public TextExtraOptionPane(String message) {
        this(message, ElanLocale.getString("Message.DontShow"), false);
    }

    /**
     * Creates a new TextExtraOptionPane instance with the specified message and option label.
     *
     * @param message the message or question to show
     * @param optionLabel the label for the option/checkbox
     */
    public TextExtraOptionPane(String message, String optionLabel) {
        this(message, optionLabel, false);
    }

    /**
     * Creates a new TextExtraOptionPane instance with the specified message and option label.
     *
     * @param message the message or question to show
     * @param optionLabel the label for the option/checkbox
     * @param selected if {@code true} the checkbox will be selected, it is unselected by default
     */
    public TextExtraOptionPane(String message, String optionLabel, boolean selected) {
        messageLabel = new JLabel(message);
        extraOptionCB = new JCheckBox(optionLabel, selected);
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 6, 10, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(messageLabel, gbc);

        gbc.gridy = 1;
        add(extraOptionCB, gbc);
    }

    /**
     * Sets the message text.
     *
     * @param text the main message to show
     */
    public void setMessage(String text) {
        messageLabel.setText(text);
    }

    /**
     * Returns whether the user wishes to be warned again or not.
     *
     * @return true if the "don't show again" checkbox is checked
     *
     * @see #getOption()
     */
    public boolean getDontShowAgain() {
        return extraOptionCB.isSelected();
    }

    /**
     * Returns whether the additional option CheckBox is or has been selected.
     *
     * @return {@code true} if the option checkbox is selected, {@code false} otherwise
     *
     * @see #getDontShowAgain()
     */
    public boolean getOption() {
        return extraOptionCB.isSelected();
    }
}
