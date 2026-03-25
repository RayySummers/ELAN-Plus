package mpi.search.content.query.viewer;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;

import mpi.search.SearchLocale;

/**
 * A renderer for localized list values. 
 * 
 * @author Alexander Klassmann
 * @version Jun 23, 2004
 */
@SuppressWarnings("serial")
public class LocalizeListCellRenderer extends DefaultListCellRenderer {
	/**
	 * Creates a new renderer instance.
	 */
	public LocalizeListCellRenderer() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(
		JList list,
		Object value,
		int index,
		boolean isSelected,
		boolean cellHasFocus) {

		String valueString = null;

		if (value != null) {
			valueString = value.toString();
			String localizedValueString = SearchLocale.getString(valueString);
			if (localizedValueString != null && localizedValueString.length() > 0)
				valueString = localizedValueString;
		}

		return super.getListCellRendererComponent(
			list,
			valueString,
			index,
			isSelected,
			cellHasFocus);
	}

}
