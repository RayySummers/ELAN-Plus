package mpi.eudico.client.annotator.grid;

import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationCore;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A list selection listener for the annotation table.
 *
 * @author Alexander Klassmann
 * @version Oct 22, 2004
 */
public class AnnotationTableListSelectionListener implements ListSelectionListener {
    /**
     * abstract viewer declaration
     */
    protected final AbstractViewer viewer;
    /**
     * JTable declaration
     */
    protected final JTable table;

    /**
     * Constructor.
     *
     * @param viewer the viewer containing the annotation table
     * @param table the annotation table
     */
    public AnnotationTableListSelectionListener(AbstractViewer viewer, JTable table) {
        this.viewer = viewer;
        this.table = table;
    }

    /**
     * Update the active annotation and the selection by calling methods in AbstractViewer.
     *
     * @param e the list selection event
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {

        if (!e.getValueIsAdjusting()) {
            return;
        }

        if (table.getSelectedRowCount() <= 0) {
            return;
        }

        GridViewerTableModel tableModel = (GridViewerTableModel) table.getModel();

        //if value in selected column is instance of Annotation
        //(e.g. children Annotation in MultiTierViewer), take this,
        //else take 'main' annotationCore
        int row = table.getSelectedRow();
        if (table.getRowSorter() != null) {
            row = table.convertRowIndexToModel(row);
        }
        Object object = tableModel.getValueAt(row, table.getSelectedColumn());
        AnnotationCore ann = object instanceof Annotation ? (Annotation) object : tableModel.getAnnotationCore(row);

        if (ann instanceof Annotation) {
            viewer.setActiveAnnotation((Annotation) ann);
        }

        // HS 4 dec 03: setActiveAnnotation should handle setSelection; only in case of more selected rows
        // (mouse drag) the selection may be set here
        if (table.getSelectedRowCount() > 1) {
            int[] rows = table.getSelectedRows();
            long selectedBeginTime = tableModel.getAnnotationCore(rows[0]).getBeginTimeBoundary();
            long selectedEndTime = tableModel.getAnnotationCore(rows[rows.length - 1]).getEndTimeBoundary();

            viewer.setSelection(selectedBeginTime, selectedEndTime);
        }
    }
}
