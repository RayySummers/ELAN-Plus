package mpi.eudico.client.util;

import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * A button as table cell renderer.
 */
@SuppressWarnings("serial")
public class ButtonTableCellRenderer extends JButton implements TableCellRenderer {
	/**
	 * Creates a new ButtonTableCellRenderer instance.
	 */
	public ButtonTableCellRenderer() {
		super();
		setOpaque(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		if (value == null) {
			return null;
		}
		return (Component) value;
	}
}