package mpi.eudico.client.annotator.gui;

import mpi.eudico.util.CVEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * A cell renderer for CVEntry objects, uses an icon to show preferred color and shortcut key.
 *
 * @author Olaf Seibert; Han Sloetjes
 */
@SuppressWarnings("serial")
public class CVEntryTableCellRenderer extends DefaultTableCellRenderer {
    private final CVEIcon icon;

    /**
     * Constructor.
     */
    public CVEntryTableCellRenderer() {
        super();
        icon = new CVEIcon();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean cellHasFocus,
                                                   int row, int col) {

        icon.color = null;
        icon.text = null;

        if (value instanceof CVEntry entry) {

            icon.color = entry.getPrefColor();
            int code = entry.getShortcutKeyCode();
            if (code != -1) {
                icon.text = KeyEvent.getKeyText(code);
            }
            setIcon(icon);
            col = table.convertColumnIndexToModel(col);
            super.getTableCellRendererComponent(table, entry.getValue(col), isSelected, cellHasFocus, row, col);
        } else {
            super.getTableCellRendererComponent(table, value, isSelected, cellHasFocus, row, col);
        }
        setIcon(icon);
        setHorizontalTextPosition(SwingConstants.RIGHT);

        return this;
    }

    /**
     * Overrides paintIcon, getWidth and getHeight.
     *
     * @author Han Sloetjes
     */
    private class CVEIcon extends ImageIcon {
        int width = 20;
        Color color;
        String text;

        /**
         * Constructor
         */
        public CVEIcon() {
            super();
        }

        @Override
        public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
            super.paintIcon(c, g, x, y);
            if (color != null) {
                g.setColor(color);
                g.fillRect(x, y, width, c.getHeight());
            }
            if (text != null) {
                g.setColor(c.getForeground());
                g.setFont(c.getFont());
                int sw = c.getFontMetrics(c.getFont()).stringWidth(text);

                g.drawString(text, x + (width - sw) / 2, c.getHeight() - ((c.getHeight() - c.getFont().getSize()) / 2) - 1);
            }
        }

        @Override
        public int getIconHeight() {
            return getHeight() - 4;
        }

        @Override
        public int getIconWidth() {
            return width;
        }
    }
}
