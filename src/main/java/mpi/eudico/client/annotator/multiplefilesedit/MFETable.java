package mpi.eudico.client.annotator.multiplefilesedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import mpi.eudico.client.annotator.Constants;

/**
 * A editable table for multiple files.
 */
public class MFETable extends JTable {
	private static final long serialVersionUID = -5306413363969435914L;
	/** the data model */
	protected MFEModel model;
	/** background color for even rows */
	public final Color EVEN_ROW_BG = Constants.EVEN_ROW_BG;
	/** background color of a selected row */
	public final Color SELECTED_ROW_BG = Constants.SELECTED_ROW_BG;

	/**
	 * Constructor.
	 * 
	 * @param model the table model
	 */
	public MFETable(MFEModel model) {
		super();
		this.model = model;
		// setAutoCreateRowSorter(true);
	}

	/**
	 * Scrolls to make a particular cell visible.
	 * 
	 * @param row the row index of the cell
	 * @param column the column index of the cell
	 */
	public void showCell(int row, int column) {
		Rectangle rect = getCellRect(row, column, true);
		scrollRectToVisible(rect);
		clearSelection();
		setRowSelectionInterval(row, row);
//		getModel().fireTableDataChanged(); // notify the model
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
			int vColIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
		if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
			c.setBackground(EVEN_ROW_BG);
		} else {
			// If not shaded, match the table's background
			c.setBackground(getBackground());
		}
		c.setForeground(getForeground());
		
		int[] selectedRows = getSelectedRows();
		for (int i = 0; i < selectedRows.length; i++) {
			if (rowIndex == selectedRows[i]) {
				c.setBackground(SELECTED_ROW_BG);
			}
		}
		return c;
	}
}
