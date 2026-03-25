package mpi.search.content.query.viewer;

import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import mpi.search.SearchLocale;

import mpi.search.content.query.model.*;


/**
 * A panel for time based relations for an anchor constraint.
 * 
 * @author klasal
 */
@SuppressWarnings("serial")
public class AnchorTemporalDistancePanel extends TemporalDistancePanel {
    /**
     * Creates a new AnchorTemporalDistancePanel object.
     */
    public AnchorTemporalDistancePanel() {
        timeRelationComboBox = new JComboBox<String>(AnchorConstraint.ANCHOR_CONSTRAINT_TIME_RELATIONS);
        timeRelationComboBox.setRenderer(new LocalizeListCellRenderer());

        JLabel label = new JLabel(SearchLocale.getString("Search.And") + " ");
        label.setFont(getFont().deriveFont(Font.PLAIN));
        add(label);
        add(timeRelationComboBox);
        label = new JLabel(" " + SearchLocale.getString("Search.Interval") + " ");
        label.setFont(getFont().deriveFont(Font.PLAIN));
        add(label);

        JLabel open = new JLabel("[");
        open.setFont(getFont().deriveFont(20f));
        open.setVerticalAlignment(JLabel.TOP);
        add(open);
        add(fromTimeField);
        add(new JLabel(" ; "));
        add(toTimeField);

        JLabel close = new JLabel("]");
        close.setFont(getFont().deriveFont(20f));
        close.setVerticalAlignment(JLabel.TOP);
        add(close);
        timeRelationComboBox.setSelectedIndex(0);
    }
}
