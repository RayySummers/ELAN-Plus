package mpi.search.content.query.viewer;

import java.awt.Font;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import mpi.search.SearchLocale;
import mpi.search.gui.XNumericalJTextFieldFilter;

/**
 * A panel for configuration of structural distance relations.
 *  
 * @author klasal
 */
@SuppressWarnings("serial")
public class StructuralDistancePanel extends AbstractDistancePanel {
    private static int unitComboBoxWidth = 160;

    /** box for the unit strings */
    protected final JComboBox<String> unitComboBox;

    /** a field for a {@code from} distance, a negative number or 0 */
    private final JTextField fromTextField = new JTextField(new XNumericalJTextFieldFilter(
                XNumericalJTextFieldFilter.INTEGER_WITH_NEG_INFINITY), "0", 3);

    /** a field for a {@code to} distance, a positive number or 0 */
    private final JTextField toTextField = new JTextField(new XNumericalJTextFieldFilter(
                XNumericalJTextFieldFilter.INTEGER_WITH_POS_INFINITY), "0", 3);

    /**
     * Creates a new StructuralDistancePanel object.
     */
    public StructuralDistancePanel() {
        fromTextField.setHorizontalAlignment(JTextField.CENTER);
        toTextField.setHorizontalAlignment(JTextField.CENTER);

        unitComboBox = new JComboBox<String>() {
                    @Override
					public Dimension getPreferredSize() {
                        return new Dimension(unitComboBoxWidth,
                            super.getPreferredSize().height);
                    }
                };

        JLabel label = new JLabel(SearchLocale.getString("Search.Constraint.Distance") +
                " ");
        label.setFont(getFont().deriveFont(Font.PLAIN));
        add(label);
        add(fromTextField);
        label = new JLabel(" " + SearchLocale.getString("Search.To") + " ");
        label.setFont(getFont().deriveFont(Font.PLAIN));
        add(label);
        add(toTextField);
        add(new JLabel(" "));
        add(unitComboBox);
    }

    /**
     * Sets the {@code from} field value 
     *
     * @param l a value {@code <= 0}
     */
    @Override
	public void setLowerBoundary(long l) {
        fromTextField.setText(getString(l));
    }

    /**
     * Returns the lower boundary, the {@code from} value.
     * 
     * @return the current {@code from} value
     */
    @Override
	public long getLowerBoundary() {
        return fromTextField.getText().trim().equals("") ? Long.MIN_VALUE
                                                         : getLong(fromTextField.getText());
    }

    /**
     * @param s the unit string
     */
    @Override
	public void setUnit(String s) {
        unitComboBox.setSelectedItem(s);
    }

    /**
     * @return the selected unit string
     */
    @Override
	public String getUnit() {
        return (unitComboBox.getSelectedIndex() != -1)
        ? (String) unitComboBox.getSelectedItem() : "";
    }

    /**
     * Returns the unit combo box.
     * 
     * @return the box with the possible unit strings
     */
    public JComboBox<String> getUnitComboBox() {
        return unitComboBox;
    }

    /**
     * Sets the renderer for the unit combo box.
     * 
     * @param renderer a renderer for the unit combobox
     */
    public void setUnitComboBoxRenderer(ListCellRenderer renderer) {
        unitComboBox.setRenderer(renderer);
    }

    /**
     * Sets the value for the {@code to} field
     *
     * @param l the {@code to} distance
     */
    @Override
	public void setUpperBoundary(long l) {
        toTextField.setText(getString(l));
    }

    /**
     * Returns the upper boundary, the {@code to} field.
     * 
     * @return the current value of the {@code to} field
     */
    @Override
	public long getUpperBoundary() {
        return toTextField.getText().trim().equals("") ? Long.MAX_VALUE
                                                       : getLong(toTextField.getText());
    }
}
