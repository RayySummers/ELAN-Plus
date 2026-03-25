package mpi.eudico.client.annotator.tier;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import mpi.eudico.client.util.CheckBoxTableCellRenderer;

/**
 * Implement a common tier table that goes nicely with {@link TierExportTableModel5Columns}.
 *
 * @author Steffen Zimmermann
 */
@SuppressWarnings("serial")
public class TierExportTable5Columns extends TierExportTable {

    /**
     * Creates a new export table instance.
     * 
     * @param model the table model
     * @param showTableHeader by default, the table header is not shown.
     *                        Use this constructor with {@code true} to show it.
     */
    public TierExportTable5Columns(DefaultTableModel model, boolean showTableHeader) {
        this(model, ListSelectionModel.SINGLE_INTERVAL_SELECTION, showTableHeader);
    }

    /**
     * Creates a new export table instance.
     * 
     * @param model the table model
     * @param selectionMode one of the ListSelectionModel.*_SELECTION values
     * @param showTableHeader by default, the table header is not shown.
     *                        Use this constructor with {@code true} to show it.
     */
    public TierExportTable5Columns(DefaultTableModel model, int selectionMode,
                           boolean showTableHeader) {
        super(model, selectionMode, showTableHeader);
        init(selectionMode, showTableHeader);
    }

    @Override
    public void init(int selectionMode, boolean showTableHeader) {
        if (getModel().getColumnCount() < 2) {
			if (getModel() instanceof DefaultTableModel model) {
				model.setColumnCount(2);
			}
        }

        DefaultCellEditor cellEd = new DefaultCellEditor(new JCheckBox());
        final TableColumn column0 = this.getColumnModel().getColumn(TierExportTableModel5Columns.CHECK_COL);
        column0.setCellEditor(cellEd);
        column0.setCellRenderer(new CheckBoxTableCellRenderer());
        column0.setMaxWidth(30);
        this.setSelectionMode(selectionMode);
        this.getSelectionModel().setSelectionMode(selectionMode);
        this.setShowVerticalLines(false);
        if (!showTableHeader) {
            this.setTableHeader(null);
        }

        if (getModel().getColumnCount() == 5) {
            DefaultCellEditor cellEd2 = new DefaultCellEditor(new JCheckBox());
            final TableColumn column2 = this.getColumnModel().getColumn(TierExportTableModel5Columns.COL2);
            column2.setCellEditor(cellEd2);
            column2.setCellRenderer(new CheckBoxTableCellRenderer());
            column2.setMaxWidth(70);
            column2.setPreferredWidth(70);
            this.setSelectionMode(selectionMode);
            this.getSelectionModel().setSelectionMode(selectionMode);
            this.setShowVerticalLines(false);
            if (!showTableHeader) {
                this.setTableHeader(null);
            }

            DefaultCellEditor cellEd3 = new DefaultCellEditor(new JCheckBox());
            final TableColumn column3 = this.getColumnModel().getColumn(TierExportTableModel5Columns.COL3);
            column3.setCellEditor(cellEd3);
            column3.setCellRenderer(new CheckBoxTableCellRenderer());
            column3.setMaxWidth(70);
            column3.setPreferredWidth(70);
            this.setSelectionMode(selectionMode);
            this.getSelectionModel().setSelectionMode(selectionMode);
            this.setShowVerticalLines(false);
            if (!showTableHeader) {
                this.setTableHeader(null);
            }

            DefaultCellEditor cellEd4 = new DefaultCellEditor(new JCheckBox());
            final TableColumn column4 = this.getColumnModel().getColumn(TierExportTableModel5Columns.COL4);
            column4.setCellEditor(cellEd4);
            column4.setCellRenderer(new CheckBoxTableCellRenderer());
            column4.setMaxWidth(70);
            column4.setPreferredWidth(70);
            this.setSelectionMode(selectionMode);
            this.getSelectionModel().setSelectionMode(selectionMode);
            this.setShowVerticalLines(false);
            if (!showTableHeader) {
                this.setTableHeader(null);
            }
        }
    }
}
