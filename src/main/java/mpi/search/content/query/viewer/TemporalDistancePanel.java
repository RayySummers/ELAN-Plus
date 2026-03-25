package mpi.search.content.query.viewer;

import java.awt.*;
import javax.swing.JComboBox;

/**
 * A panel for the configuration of temporal distance relations.
 * 
 * @author Alexander Klassmann
 * @version May 19, 2004
 */
@SuppressWarnings("serial")
public class TemporalDistancePanel extends AbstractDistancePanel {
	/** a temporal relation combo box */
    protected JComboBox<String> timeRelationComboBox;

    /** the {@code from time} field */
    final protected TimeField fromTimeField = new TimeField(false);

    /** the {@code to time} field */
    final protected TimeField toTimeField = new TimeField(true);

    /**
     * Creates a new distance panel.
     */
    public TemporalDistancePanel() {
        setLayout(new GridBagLayout());
    }
    
    /**
     * @return the {@code from} time value
     */
    @Override
	public long getLowerBoundary() {
        return fromTimeField.getTime();
    }

    /**
     * @return the {@code to} time value
     */
    @Override
	public long getUpperBoundary() {
        return toTimeField.getTime();
    }

    @Override
	public String getUnit() {
        return timeRelationComboBox.getSelectedIndex() != -1 ? (String) timeRelationComboBox
                .getSelectedItem()
                : "";
    }

    /**
     * @param milliSeconds the temporal {@code from} value in milliseconds
     */
    @Override
	public void setLowerBoundary(long milliSeconds) {
        fromTimeField.setTime(milliSeconds);
    }

    /**
     * @param milliSeconds the temporal {@code to} value in milliseconds
     */
    @Override
	public void setUpperBoundary(long milliSeconds) {
        toTimeField.setTime(milliSeconds);
    }

    @Override
	public void setUnit(String unit) {
        timeRelationComboBox.setSelectedItem(unit);
    }
}
