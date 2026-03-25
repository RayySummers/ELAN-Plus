package mpi.eudico.client.util;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * A table cell renderer based on a JComboBox.
 */
@SuppressWarnings("serial")
public class ComboBoxTableCellRenderer extends JComboBox<Object> implements
		TableCellRenderer {

	/**
	 * Creates a new ComboBoxTableCellRenderer instance.
	 */
	public ComboBoxTableCellRenderer() {
		super();
		setOpaque(true);
	}

	/**
	 * Creates a new ComboBoxTableCellRenderer instance.
	 * 
	 * @param values the values to be added to the JComboBox
	 */
	public ComboBoxTableCellRenderer(Object[] values) {
		super(values);
		setOpaque(true);
	}

	/**
	 * Sets colors and selects the cell value in the JComboBox, if possible.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            super.setBackground(table.getSelectionBackground());
        } else {
            super.setBackground(table.getBackground());
        }
        // assume that the value is in the list of values of the combobox
		setSelectedItem(value);
		return this;
	}

}
