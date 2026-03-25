package mpi.eudico.client.annotator.tier;

import java.util.ArrayList;
import java.util.List;

/**
 * An extended table model for 5 Columns.
 *
 * A good subclass of JTable to use is {@link TierExportTable5Columns}.
 *
 * @author Steffen Zimmermann
 */
@SuppressWarnings("serial")
public class TierExportTableModel5Columns extends TierExportTableModel {
	/** column index 2 */
    public static final int COL2 = 2;
    /** column index 3 */
    public static final int COL3 = 3;
    /** column index 4 */
    public static final int COL4 = 4;

    /**
     * Creates a new export table model instance.
     */
    public TierExportTableModel5Columns() {
		super();
	}

	/**
     * Convenience method to add a tier name and three extra columns to the model.
     * 
     * @param selected the initial selected state
     * @param name the tier name to add
     * @param col2 the value for the column at index 2
     * @param col3 the value for the column at index 3
     * @param col4 the value for the column at index 4
     */
    public void addRow(Boolean selected, String name, Object col2, Object col3, Object col4) {
        addRow(new Object[]{selected, name, col2, col3, col4});
    }

    /**
     * Returns the tiers that have been selected in the table with associated settings.
     *
     * @return a list of the selected tiers
     */
    public List<TierExportSetting> getSelectedTiersWithSettings() {
        List<TierExportSetting> tierSettings = new ArrayList<TierExportSetting>();
        Object selObj = null;
        Object nameObj = null;
        Object col2 = null;
        Object col3 = null;
        Object col4 = null;

        final int rowCount = this.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            selObj = this.getValueAt(i, CHECK_COL);

            if (Boolean.TRUE.equals(selObj)) {
                nameObj = this.getValueAt(i, NAME_COL);

                if (nameObj instanceof String) {

                    col2 = this.getValueAt(i, COL2);
                    col3 = this.getValueAt(i, COL3);
                    col4 = this.getValueAt(i, COL4);

                    TierExportSetting tierSetting = new TierExportSetting(
                            (String) nameObj, true, (Boolean) col2, (Boolean) col3, (Boolean) col4);
                    tierSettings.add(tierSetting);
                }
            }
        }
        return tierSettings;
    }
}


