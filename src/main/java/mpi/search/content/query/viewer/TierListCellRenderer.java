package mpi.search.content.query.viewer;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import mpi.search.SearchLocale;
import mpi.search.content.model.CorpusType;

/**
 * A specialized tier list renderer.
 * 
 * @author Alexander Klassmann
 * @version Apr 14, 2004
 */
@SuppressWarnings("serial")
public class TierListCellRenderer extends DefaultListCellRenderer {
	/** the corpus type */
	private final CorpusType type;

	/**
	 * Creates a new renderer.
	 *  
	 * @param type the corpus type
	 */
	public TierListCellRenderer(CorpusType type) {
		this.type = type;
	}

	/**
	 * If TYPE contains long name of tier, use that one.
	 * @return configured list cell renderer
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(JList, Object, int, boolean, boolean)
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

			if (type != null) {
				String longName = type.getUnabbreviatedTierName(valueString);
				if (longName != null) {
					valueString = longName;
				}
				else {
					String localizedValueString = SearchLocale.getString(valueString);
					if (localizedValueString != null && localizedValueString.length() > 0)
						valueString = localizedValueString;
				}
			}
		}

		return super.getListCellRendererComponent(
			list,
			valueString,
			index,
			isSelected,
			cellHasFocus);
	}

}
