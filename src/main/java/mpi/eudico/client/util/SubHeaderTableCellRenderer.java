package mpi.eudico.client.util;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A table cell renderer for {@code sub-table headers}, headers that are inside
 * a table but are not actual part of the data in the data model. 
 * 
 * @see TableSubHeaderObject
 */
@SuppressWarnings("serial")
public class SubHeaderTableCellRenderer extends DefaultTableCellRenderer {
    /**
     * Creates a new renderer instance.
     */
    public SubHeaderTableCellRenderer() {
		super();
	}

	/**
     * Calls the super implementation unless the value is of type 
     * {@code TableSubHeaderObject}. In that case it uses a {@code bold}
     * font slightly bigger than the table's default font.
     * 
     */
    @Override
	public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        if (value instanceof TableSubHeaderObject) {

            if (isSelected) {
                super.setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            	} else {
            	    super.setForeground(table.getForeground());
            	    super.setBackground(table.getBackground());
            	}
             setFont(table.getFont().deriveFont(Font.BOLD, table.getFont().getSize() + 2));

            setValue(value);
            
            return this;
        }
        
        return super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
    }
}
