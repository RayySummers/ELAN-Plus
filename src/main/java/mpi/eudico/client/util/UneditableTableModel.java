package mpi.eudico.client.util;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * Extends the default implementation of a table model by overriding 
 * {@link #isCellEditable(int, int)} to always return false. 
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class UneditableTableModel extends DefaultTableModel {

	/**
	 * Creates a default table model which can not be edited.
	 */
	public UneditableTableModel() {
		super();
	}

	/**
	 * Creates an uneditable table model with the specified number of rows and
	 * columns.
	 * 
	 * @param rowCount the number of rows
	 * @param columnCount the number of columns
	 */
	public UneditableTableModel(int rowCount, int columnCount) {
		super(rowCount, columnCount);
	}

	/**
	 * Creates an uneditable table model with the specified number of rows and 
	 * the specified column names.
	 * 
	 * @param columnNames the column names
	 * @param rowCount the number of rows
	 */
	public UneditableTableModel(Object[] columnNames, int rowCount) {
		super(columnNames, rowCount);
	}

	/**
	 * Creates an uneditable table model with the specified column names 
	 * filling it with the specified data.
	 * 
	 * @param data the data
	 * @param columnNames the column names
	 */
	public UneditableTableModel(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
	}

	/**
	 * Creates an uneditable table model with the specified column names and 
	 * the specified number of rows.
	 * 
	 * @param columnNames the column names
	 * @param rowCount the row count
	 */
	public UneditableTableModel(Vector columnNames, int rowCount) {
		super(columnNames, rowCount);
	}

	/**
	 * Creates an uneditable table model with the specified column names, 
	 * filling it with the specified data.
	 * 
	 * @param data the data for the model
	 * @param columnNames the column names
	 */
	public UneditableTableModel(Vector data, Vector columnNames) {
		super(data, columnNames);
	}

	/**
	 * @return {@code false}, regardless of row and column parameters, making
	 * the model uneditable
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
