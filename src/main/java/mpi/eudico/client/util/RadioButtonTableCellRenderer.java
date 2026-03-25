package mpi.eudico.client.util;

import java.awt.Component;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * A table cell renderer based on a JRadioButton.
 * 
 * @see RadioButtonCellEditor
 */
@SuppressWarnings("serial")
public class RadioButtonTableCellRenderer extends JRadioButton implements TableCellRenderer {

	/**
	 * Constructor.
	 */
	public RadioButtonTableCellRenderer() {
		super();
		 setOpaque(true);
	}
	
	/**
	 * Applies the text and the enabled and selected properties to the radio 
	 * button, based on the value in the cell.
	 * 
	 * @return the configured radio button
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (value instanceof SelectEnableObject) {
			SelectEnableObject<?> seo = (SelectEnableObject<?>) value;
			setText(seo.getValue().toString());
			setSelected(seo.isSelected());
			setEnabled(seo.isEnabled());
		}
		
		return this;
	}
}
	
	
	

