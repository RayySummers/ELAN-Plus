package mpi.eudico.client.annotator.multiplefilesedit;

import java.util.HashMap;
import java.util.Map;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * A model that maintains a {@code TableCellEditor} and a {@code TableCellRenderer}
 * for each row in a table.
 */
public class RowModel {
	private Map<Integer, TableCellEditor> editor_data;
	private Map<Integer, TableCellRenderer> renderer_data;

	/**
	 * Constructor, creates maps for editors and renderers.
	 */
	public RowModel() {
		editor_data = new HashMap<Integer, TableCellEditor>();
		renderer_data = new HashMap<Integer, TableCellRenderer>();
	}

	/**
	 * Adds or sets a renderer for a row.
	 * 
	 * @param row the row index
	 * @param e the renderer
	 */
	public void addRendererForRow(int row, TableCellRenderer e) {
		renderer_data.put(Integer.valueOf(row), e);
	}

	/**
	 * Removes a renderer for a row.
	 * 
	 * @param row the row index
	 */
	public void removeRendererForRow(int row) {
		renderer_data.remove(Integer.valueOf(row));
	}

	/**
	 * Returns the renderer for a row.
	 * 
	 * @param row the row index
	 * @return the renderer or {@code null}
	 */
	public TableCellRenderer getRenderer(int row) {
		return renderer_data.get(Integer.valueOf(row));
	}
	
	/**
	 * Adds or sets an editor for a row.
	 * 
	 * @param row the row index
	 * @param e the editor for the row
	 */
	public void addEditorForRow(int row, TableCellEditor e) {
		editor_data.put(Integer.valueOf(row), e);
	}

	/**
	 * Removes an editor for a row.
	 * 
	 * @param row the row index
	 */
	public void removeEditorForRow(int row) {
		editor_data.remove(Integer.valueOf(row));
	}

	/**
	 * Returns the editor for arow.
	 * 
	 * @param row the row index
	 * @return the editor or {@code null}
	 */
	public TableCellEditor getEditor(int row) {
		return editor_data.get(Integer.valueOf(row));
	}
}
