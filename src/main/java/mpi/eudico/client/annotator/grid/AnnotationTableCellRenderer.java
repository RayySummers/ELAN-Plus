package mpi.eudico.client.annotator.grid;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.server.corpora.clom.AnnotationCore;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;


/**
 * Renders annotations in a Table (number, value, times, etc)  Created on Oct 5, 2004
 *
 * @author Alexander Klassmann
 * @version Oct 5, 2004
 */
public class AnnotationTableCellRenderer implements TableCellRenderer {
    /**
     * margin border initilaization
     */
    protected static final Border marginBorder = BorderFactory.createEmptyBorder(0, 1, 0, 3);
    /**
     * label
     */
    protected final JLabel label;

    /**
     * Creates a new AnnotationTableCellRenderer object.
     */
    @SuppressWarnings("serial")
    public AnnotationTableCellRenderer() {
        // override the defaults for performance reasons
        label = new JLabel() {
            @Override
            public void validate() {
            }

            @Override
            public void revalidate() {
            }

            @Override
            protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
                // Strings get interned...
                if ("text".equals(propertyName)) {
                    super.firePropertyChange(propertyName, oldValue, newValue);
                }
            }

            @Override
            public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
            }

            /* only for J 1.4, new solutions for 1.5 and 1.6
            public void paintComponent(Graphics g) {
                if (g instanceof Graphics2D) {
                    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(g);
            }
            */
        };

        label.setOpaque(true);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object,
     * boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                                                   int column) {
        setComponentLayout(label, table, isSelected);
        setAlignment(label, table.getColumnName(column));

        String renderedText = getRenderedText(value);
        label.setText(renderedText);

        if (!"".equals(renderedText)) {
            label.setToolTipText(renderedText);
        }

        return label;
    }

    /**
     * Sets the alignment to the label based on column name
     *
     * @param label the label to which the alignment is set to
     * @param columnName the column name
     */
    protected static void setAlignment(JLabel label, String columnName) {
        if (GridViewerTableModel.COUNT.equals(columnName)
            || GridViewerTableModel.LEFTCONTEXT.equals(columnName)
            || GridViewerTableModel.BEGINTIME.equals(columnName)
            || GridViewerTableModel.ENDTIME.equals(columnName)
            || GridViewerTableModel.DURATION.equals(columnName)) {
            label.setHorizontalAlignment(SwingConstants.RIGHT);
        } else {
            label.setHorizontalAlignment(SwingConstants.LEFT);
        }
    }

    /**
     * Sets the component layout
     *
     * @param component the JComponent to which the layout is set to
     * @param table the JTable
     * @param isSelected boolean parameter to see if selected or not
     */
    protected static void setComponentLayout(JComponent component, JTable table, boolean isSelected) {
        component.setFont(table.getFont());
        component.setBorder(marginBorder);
        component.setBackground(isSelected ? Constants.SELECTIONCOLOR : table.getBackground());
    }

    /**
     * Handles the drawing of the cells with text values.
     *
     * @param value the value of the table cell. May be of type AnnotationCore or String.
     *
     * @return the text, the value of an annotation
     */
    protected static String getRenderedText(Object value) {
        return (value instanceof AnnotationCore)
               ? ((AnnotationCore) value).getValue()
               : (value instanceof String) ? (String) value : "";
    }
}
