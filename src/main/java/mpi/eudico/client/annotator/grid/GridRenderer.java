package mpi.eudico.client.annotator.grid;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.search.result.model.ElanMatch;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.util.TimeRelation;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.File;


/**
 * Renders a media time pointer and the 'active annotation'. NOTE: Selection rendering is done NOT via the selection
 * mechanism of the JTable; selections are set 'manually' with the setFirst/LastSelectedRow methods!
 */
public class GridRenderer extends AnnotationTableCellRenderer {
    /**
     * active border constant
     */
    protected static final Border activeBorder = new LineBorder(Constants.ACTIVEANNOTATIONCOLOR);

    /**
     * delivers annotation (value + time)
     */
    protected final AbstractTableModel tableModel;

    /**
     * delivers mediaTime and activeAnnotation
     */
    protected final AbstractViewer viewer;

    /**
     * Creates a new GridRenderer instance
     *
     * @param viewer the viewer containing the grid
     * @param tableModel the model of the table
     */
    public GridRenderer(AbstractViewer viewer, AbstractTableModel tableModel) {
        this.viewer = viewer;
        this.tableModel = tableModel;
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
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                                                   int column) {
        String columnName = table.getColumnName(column);
        setAlignment(label, columnName);

        int annotationColumn = tableModel.findColumn(GridViewerTableModel.ANNOTATION);

        boolean isInSelection = isSelected;

        if (viewer != null) {
            final AnnotationCore valueAt = (AnnotationCore) tableModel.getValueAt(row, annotationColumn);
            isInSelection = valueAt != null && TimeRelation.overlaps(valueAt,
                                                                     viewer.getSelectionBeginTime(),
                                                                     viewer.getSelectionEndTime());
        }

        boolean isActive = false;

        if (viewer != null) {
            isActive = value instanceof AnnotationCore && (viewer.getActiveAnnotation() == value);
        }

        setComponentLayout(label, table, value, isInSelection, isActive, column);

        if (GridViewerTableModel.TIMEPOINT.equals(columnName)) {
            label.setBorder(null);

            if (viewer != null) {
                //create icons only if cell size has changed
                int iconWidth = table.getCellRect(row, column, true).width - 1;
                int iconHeight = table.getCellRect(row, column, true).height - 1;
                label.setIcon(getTimePointerIcon(row, annotationColumn, iconWidth, iconHeight));
            }
        } else {
            label.setIcon(null);

            String renderedText = getRenderedText(value);

            if (GridViewerTableModel.FILENAME.equals(columnName) && !renderedText.equals("")) {
                String fileName = new File(renderedText).getName();
                int stopIndex = fileName.lastIndexOf('.');
                if (stopIndex > 0) {
                    label.setText(fileName.substring(0, stopIndex));
                } else {
                    label.setText(fileName);
                }

            } else {
                label.setText(renderedText);
            }

            if (!"".equals(renderedText)) {
                label.setToolTipText(renderedText);
            }
        }

        return label;
    }

    /**
     * Sets component layout
     *
     * @param component the component
     * @param table the JTable
     * @param value the value
     * @param isSelected is selected boolean
     * @param isActive is active boolean
     * @param column the column index
     */
    protected static void setComponentLayout(JComponent component, JTable table, Object value, boolean isSelected,
                                             boolean isActive, int column) {
        //setComponentLayout(component, table, isSelected);
        //component.setBorder(marginBorder);
        component.setBackground(isSelected ? Constants.SELECTIONCOLOR : table.getBackground());

        if (table instanceof AnnotationTable) {
            component.setFont(((AnnotationTable) table).getFontForColumn(column));

            if (!isSelected) {
                Annotation a = null;
                if (value instanceof Annotation) {
                    a = (Annotation) value;
                } else if (value instanceof ElanMatch) {
                    a = ((ElanMatch) value).getAnnotation();
                }
                if (a != null) {
                    Color c = ((AnnotationTable) table).getColorForAnnotation(a);
                    if (c != null) {
                        component.setBackground(c);
                    }
                }
            }
        } else {
            component.setFont(table.getFont());
        }

        if (isActive) {
            component.setBorder(activeBorder);
        } else {
            component.setBorder(marginBorder);
        }
    }

    /**
     * Determines some values needed for drawing the red triangle indicating the media time pointer.
     *
     * @param row the row to create a (partial) arrow for
     * @param annotationColumn the column containing the annotation
     * @param iconWidth width of the icon
     * @param iconHeight height of the icon
     *
     * @return the created icon or {@code null} if the arrow doesn't need to be drawn in this row
     */
    private Icon getTimePointerIcon(int row, int annotationColumn, int iconWidth, int iconHeight) {
        long beginTime;
        long endTime;
        long previousEndTime = 0;
        long nextBeginTime = Long.MAX_VALUE;
        label.setText("");

        AnnotationCore aa = (AnnotationCore) tableModel.getValueAt(row, annotationColumn);

        if (aa == null) {
            return null;
        }

        beginTime = aa.getBeginTimeBoundary();
        endTime = aa.getEndTimeBoundary();

        if (row > 0) {
            aa = (AnnotationCore) tableModel.getValueAt(row - 1, annotationColumn);
            previousEndTime = aa.getEndTimeBoundary();
        }

        if (row < (tableModel.getRowCount() - 1)) {
            aa = (AnnotationCore) tableModel.getValueAt(row + 1, annotationColumn);
            nextBeginTime = aa.getBeginTimeBoundary();
        }

        long mediaTime = viewer.getMediaTime();

        if ((mediaTime >= beginTime) && (mediaTime < endTime)) { //complete triangle

            return new GridViewerIcon(GridViewerIcon.COMPLETE, iconWidth, iconHeight);
        } else if ((mediaTime > 0) && (mediaTime >= previousEndTime) && (mediaTime < beginTime)) { //upper half triangle

            return new GridViewerIcon(GridViewerIcon.UPPER_HALF, iconWidth, iconHeight);
        } else if ((mediaTime < (viewer.getMediaDuration() - 1000)) //allow for inaccuracy of player of 1 ms
                   && (mediaTime < nextBeginTime) && (mediaTime >= endTime)) { //lower half triangle

            return new GridViewerIcon(GridViewerIcon.LOWER_HALF, iconWidth, iconHeight);
        }

        return null;
    }

    /**
     * Class which represents a red triangle
     */
    private class GridViewerIcon implements Icon {
        static final int COMPLETE = 0;
        static final int UPPER_HALF = -1;
        static final int LOWER_HALF = 1;

        /**
         * array of x coordinates
         */
        private final int[] x_arr;

        /**
         * array of y coordinates
         */
        private final int[] y_arr;
        private final int iconHeight;
        private final int iconWidth;

        /**
         * Creates a new GridViewerIcon instance. Calculates the x an y coordinates of the icon.
         *
         * @param type the type, one of {@code GridViewerIcon#COMPLETE}, {@code GridViewerIcon#UPPER_HALF} and
         *     {@code GridViewerIcon#LOWER_HALF}
         * @param iconWidth the width
         * @param iconHeight the height
         */
        GridViewerIcon(int type, int iconWidth, int iconHeight) {
            if (type == LOWER_HALF) {
                x_arr = new int[] {0, iconWidth, 0};
                y_arr = new int[] {iconHeight / 2, iconHeight, iconHeight};
            } else if (type == UPPER_HALF) {
                x_arr = new int[] {0, 0, iconWidth};
                y_arr = new int[] {0, iconHeight / 2, 0};
            } else {
                x_arr = new int[] {0, iconWidth, 0};
                y_arr = new int[] {0, iconHeight / 2, iconHeight};
            }

            this.iconWidth = iconWidth;
            this.iconHeight = iconHeight;
        }

        /**
         * @return the icon height
         */
        @Override
        public int getIconHeight() {
            return iconHeight;
        }

        /**
         * @return the icon width
         */
        @Override
        public int getIconWidth() {
            return iconWidth;
        }

        /**
         * Renders the icon.
         *
         * @param c not used
         * @param g the graphics context to render to
         * @param x not used
         * @param y not used
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Constants.CROSSHAIRCOLOR);
            g.fillPolygon(x_arr, y_arr, x_arr.length);
        }
    }

    //end GridViewerIcon
}
