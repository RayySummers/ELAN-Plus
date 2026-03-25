package mpi.search.content.query.viewer;

import java.awt.FlowLayout;
import javax.swing.JPanel;

import mpi.search.SearchLocale;

/**
 * Base class for distance relation panels.
 * 
 * @author Alexander Klassmann
 * @version May 26, 2004
 */
@SuppressWarnings("serial")
public abstract class AbstractDistancePanel extends JPanel {
	/**
	 * Creates a new distance panel.
	 */
	AbstractDistancePanel(){
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	}

	/**
	 * Method to get the unit.
	 * 
	 * @return the current unit string
	 */
    abstract public String getUnit();

    
    /**
     * Method to return the lower distance boundary.
     * 
     * @return the lower distance boundary
     */
    abstract public long getLowerBoundary();

    
    /**
     * Method to return the upper distance boundary.
     * 
     * @return the upper distance boundary
     */
    abstract public long getUpperBoundary();

    /**
     * Method to set the unit.
     * 
     * @param s the unit string
     */
    abstract public void setUnit(String s);

    /**
     * Method to set the lower distance boundary.
     * 
     * @param lowerBoundary the lower boundary
     */
    abstract public void setLowerBoundary(long lowerBoundary);

    /**
     * Method to set the upper distance boundary.
     * 
     * @param upperBoundary the upper boundary
     */
    abstract public void setUpperBoundary(long upperBoundary);

    
    /**
     * Custom conversion of a string to a {@code long} value.
     * 
     * @param s the text value
     * @return the distance value
     */
    protected long getLong(String s) {
        if (s.toUpperCase().equals("-X")) {
            return Long.MIN_VALUE;
        }
        if (s.toUpperCase().equals("X")) {
            return Long.MAX_VALUE;
        }
        long l = 0;
        try {
            l = Long.parseLong(s);
        } catch (NumberFormatException e) {
            System.out.println(SearchLocale
                    .getString("Search.Exception.WrongNumberFormat")
                    + ": " + e.getMessage());
        }
        return l;
    }

    /**
     * Custom conversion of a {@code long} value to a string.
     * 
     * @param l the distance value
     * @return the string value 
     */
    protected String getString(long l) {
        return (l != Long.MIN_VALUE && l != Long.MAX_VALUE) ? "" + l : "";
    }

}
