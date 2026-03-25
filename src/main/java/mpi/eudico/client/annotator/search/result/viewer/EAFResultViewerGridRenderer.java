package mpi.eudico.client.annotator.search.result.viewer;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import mpi.eudico.client.annotator.grid.GridRenderer;
import mpi.eudico.client.annotator.search.result.model.ElanMatch;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.util.TimeRelation;

import mpi.search.content.result.model.ContentMatch;


/**
 * Renders textPanes instead of labels (for highlighting of found matches).
 *
 * @author Alexander Klassmann
 * @version Oct 22, 2004
 */
public class EAFResultViewerGridRenderer extends GridRenderer {
    /**
     * Creates a new EAFResultViewerGridRenderer object.
     *
     * @param tableModel the table model the data
     */
    public EAFResultViewerGridRenderer(AbstractTableModel tableModel) {
        this(null, tableModel);
    }

    /**
     * Creates a new EAFResultViewerGridRenderer object.
     *
     * @param viewer the viewer
     * @param tableModel the table model containing the data
     */
    public EAFResultViewerGridRenderer(AbstractViewer viewer,
        AbstractTableModel tableModel) {
        super(viewer, tableModel);
    }

    /**
     * Returns a configured JLabel for every cell in the table.
     *
     * @param table the table
     * @param value the cell value
     * @param isSelected selected state of the cell
     * @param hasFocus whether or not the cell has focus
     * @param row the row index
     * @param column the column index
     *
     * @return this JLabel
     */
    @Override
	public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
    	boolean isInSelection = isSelected;
    	
        if (value instanceof ContentMatch) {
            final ContentMatch contentMatch = (ContentMatch) value;
            
			if (viewer != null) {
                isInSelection = TimeRelation.overlaps(contentMatch,
                        viewer.getSelectionBeginTime(),
                        viewer.getSelectionEndTime());
            }

            boolean isActive = false;

            if ((viewer != null) && value instanceof ElanMatch) {
                isActive = viewer.getActiveAnnotation() == ((ElanMatch) value).getAnnotation();
            }

            setComponentLayout(label, table, value, isInSelection, isActive, column);
            setAlignment(label, table.getColumnName(column));

            label.setText(ElanResult2HTML.translate(contentMatch, false));

            label.setToolTipText(ElanResult2HTML.translate(
                    contentMatch, true));

            return label;
        } else {
        	int modelRow = table.convertRowIndexToModel(row);
        	if (this.tableModel instanceof EAFResultViewerTableModel && viewer != null) {
        		AnnotationCore ac = ((EAFResultViewerTableModel) tableModel).getAnnotationCore(modelRow);
        		isInSelection = TimeRelation.overlaps(ac, viewer.getSelectionBeginTime(), 
        				viewer.getSelectionEndTime());
        	}
            return super.getTableCellRendererComponent(table, value, isInSelection,
                    hasFocus, modelRow, column);
        }

//        return super.getTableCellRendererComponent(table, value, isInSelection,
//            hasFocus, row, column);
    }
}
