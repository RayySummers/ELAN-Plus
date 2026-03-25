package mpi.search.content.query.viewer;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mpi.search.gui.XNumericalJTextFieldFilter;


/**
 * A time field consists of two Textfields to type in seconds and milliseconds
 * separately; only numbers and the "infinity symbols" X and -X are possible
 * as inputs. Created on Jun 15, 2004
 *
 * @author Alexander Klassmann
 */
@SuppressWarnings("serial")
public class TimeField extends JPanel {
    /** if this is a {@code FROM} panel */
    public final boolean FROM = false;

    /** if this is a {@code TO} panel */
    public final boolean TO = true;

    /** the textField for milliseconds */
    protected final JTextField milliSecTextField = new JTextField(new XNumericalJTextFieldFilter(
                XNumericalJTextFieldFilter.POS_INTEGER), "", 3);

    /** the textField for seconds */
    protected final JTextField secTextField;
    /** positive flag */
    private final boolean positiv;

    /**
     * same as TimeField(true)
     *
     * @see java.lang.Object#Object()
     */
    public TimeField() {
        this(true);
    }

    /**
     * if true, positive numbers and infinity (letter 'X') are possible as
     * input, otherwise  negative numbers and negative infinity ('-X') are
     * allowed.
     *
     * @param positiv if true, only positive numbers are allowed 
     * @see javax.swing.JPanel#JPanel(boolean)
     */
    public TimeField(boolean positiv) {
        this.positiv = positiv;
        secTextField = new JTextField(new XNumericalJTextFieldFilter(positiv
                    ? XNumericalJTextFieldFilter.INTEGER_WITH_POS_INFINITY
                    : XNumericalJTextFieldFilter.INTEGER_WITH_NEG_INFINITY),
                "", 3);
        secTextField.setHorizontalAlignment(JTextField.RIGHT);
        milliSecTextField.setHorizontalAlignment(JTextField.RIGHT);
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        add(secTextField);
        add(new JLabel("."));
        add(milliSecTextField);
        add(new JLabel("s"));
    }

    /**
     * sets time in the two textFields
     *
     * @param milliSeconds time in milliseconds
     */
    public void setTime(long milliSeconds) {
        if ((positiv && (milliSeconds == Long.MAX_VALUE)) ||
                (!positiv && (milliSeconds == Long.MIN_VALUE))) {
            secTextField.setText("");
            milliSecTextField.setText("");
        } else {
            secTextField.setText("" + (milliSeconds / 1000));
            milliSecTextField.setText("" + (milliSeconds % 1000));
        }
    }

    /**
     * gets time in milliseconds out of the two textfields if both texts are
     * empty, Long.MAX_VALUE resp. Long.MIN_VALUE are returned!
     *
     * @return the time in milliseconds as long 
     */
    public long getTime() {
        long milliSeconds = 0;

        if ((secTextField.getText() + milliSecTextField.getText()).trim()
                 .equals("")) {
            milliSeconds = positiv ? Long.MAX_VALUE : Long.MIN_VALUE;
        } else {
            try {
                milliSeconds = 1000 * Long.parseLong(secTextField.getText());
            } catch (NumberFormatException e) {
            }

            try {
                milliSeconds += Long.parseLong(milliSecTextField.getText());
            } catch (NumberFormatException e) {
            }
        }

        return milliSeconds;
    }
}
