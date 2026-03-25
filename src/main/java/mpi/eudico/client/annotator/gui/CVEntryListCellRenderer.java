package mpi.eudico.client.annotator.gui;

import mpi.eudico.util.CVEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * A cell renderer for CVEntry objects, uses an icon to show preferred color and shortcut key.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class CVEntryListCellRenderer extends DefaultListCellRenderer {
    private final CVEIcon icon;

    /**
     * Constructor.
     */
    public CVEntryListCellRenderer() {
        super();
        icon = new CVEIcon();
    }


    /**
     * Calls super and adds the configured icon.
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof CVEntry cvEntry) {
            icon.color = cvEntry.getPrefColor();
            int code = cvEntry.getShortcutKeyCode();
            if (code == -1) {
                icon.text = null;
            } else {
                icon.text = KeyEvent.getKeyText(code);
            }
        } else {
            icon.color = null;
            icon.text = null;
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
            return CVEntryListCellRenderer.this.getHeight() - 4;
        }

        @Override
        public int getIconWidth() {
            return width;
        }

    }

}
