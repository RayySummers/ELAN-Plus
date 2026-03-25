package mpi.eudico.client.util;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTable;

/**
 * A table cell editor based on a JRadioButton.
 * 
 * @see RadioButtonTableCellRenderer
 */
@SuppressWarnings("serial")
public class RadioButtonCellEditor  extends  DefaultCellEditor
		implements ItemListener {

	private JRadioButton button;

	/**
	 * Creates a new RadioButtonCellEditor instance.
	 * 
	 * @param checkBox passed to a {@code super} constructor, but not used
	 */
	public RadioButtonCellEditor(JCheckBox checkBox) {
		super(checkBox);
	}

	/**
	 * Applies the text and the enabled and selected properties to the radio 
	 * button, based on the value in the cell.
	 * 
	 * @return the configured radio button
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		if (value instanceof SelectEnableObject) {
			if (button == null) {
				button = new JRadioButton(value.toString());
				button.addItemListener(this);
			} else {
				button.setText(value.toString());
			}
			button.setSelected(((SelectEnableObject<?>) value).isSelected());
			button.setEnabled(((SelectEnableObject<?>) value).isEnabled());
		}

		return button;
	}

	/**
	 * Returns a configured {@code SelectEnableObject<String>} object.
	 * 
	 * @return a {@code SelectEnableObject<String>} based on the properties of
	 * the radio button
	 */
	@Override
	public Object getCellEditorValue() {
		// button.removeItemListener(this);
		SelectEnableObject<String> seo = new SelectEnableObject<String>(button.getText(), button.isSelected(),
				button.isEnabled());
		return seo;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		super.fireEditingStopped();
	}
	}