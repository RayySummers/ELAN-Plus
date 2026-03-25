package mpi.eudico.client.util;

import java.awt.Color;
import java.awt.Component;

import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * A list cell renderer for {@code Locale} objects.
 * A JLabel is the rendering component. 
 */
@SuppressWarnings("serial")
public class LangCellRenderer extends JLabel implements ListCellRenderer<Locale> {
    /**
     * Creates a new LangCellRenderer instance.
     */
    public LangCellRenderer() {
        setOpaque(true);
    }

    /**
     * Sets the text of the label to the display name of the {@code Locale}.
     *
     * @return the configured JLabel
     */
    @Override
	public Component getListCellRendererComponent(JList<? extends Locale> list, Locale value,
        int index, boolean isSelected, boolean cellHasFocus) {
        setText(value.getDisplayName());
        setBackground(isSelected ? Color.lightGray : Color.white);
        setForeground(isSelected ? Color.white : Color.black);

        return this;
    }
}
