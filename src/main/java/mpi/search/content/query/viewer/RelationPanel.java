package mpi.search.content.query.viewer;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import mpi.search.content.query.model.AnchorConstraint;

import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.Constraint;


/**
 * A panel for distance relations. 
 *
 * @author klasal
 * @version Jul 14, 2004
 */
@SuppressWarnings("serial")
public class RelationPanel extends JPanel {
	/** panel for structural distance */
    protected AbstractDistancePanel structuralDistancePanel;
    /** panel for temporal distance */
    protected AbstractDistancePanel temporalDistancePanel;
    /** a quantifier combo box */
    protected final JComboBox<String> quantifierComboBox = new JComboBox<String>(Constraint.QUANTIFIERS);
    /** distance input layout */
    private final CardLayout distanceInputLayout = new CardLayout();
    /** distance panel place holder */
    private final JPanel distancePanelPlaceHolder = new JPanel(distanceInputLayout);

    /**
     * Creates a new relation panel.
     * 
     * @param type the corpus type
     * @param constraint the constraint object
     */
    public RelationPanel(CorpusType type, Constraint constraint) {
        setLayout(new FlowLayout(FlowLayout.LEFT,0,0));

        GridBagConstraints c = new GridBagConstraints();

        if (constraint instanceof AnchorConstraint) {
            temporalDistancePanel = new AnchorTemporalDistancePanel();
        } else {
            temporalDistancePanel = new DependentTemporalDistancePanel();
        }

        structuralDistancePanel = new StructuralDistancePanel();
        distancePanelPlaceHolder.add(structuralDistancePanel,
            Constraint.STRUCTURAL);
        distancePanelPlaceHolder.add(temporalDistancePanel, Constraint.TEMPORAL);

        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.NONE;

        add(distancePanelPlaceHolder);

        c.anchor = GridBagConstraints.WEST;

        if (constraint instanceof AnchorConstraint && !type.allowsTemporalConstraints()) {
            setVisible(false);
        }else{
        		setDistanceMode(constraint.getMode());
        }
    }

    /**
     * Sets the distance mode, temporal or structural.
     * 
     * @param mode the distance mode, temporal or structural, to show in the
     * panel
     */
    public void setDistanceMode(String mode) {
        distanceInputLayout.show(distancePanelPlaceHolder, mode);
   }

    /**
     * The lower distance boundary in milliseconds or number of annotations
     *
     * @param boundary the lower boundary
     */
    public void setLowerBoundary(long boundary) {
        (temporalDistancePanel.isVisible() ? temporalDistancePanel
                                           : structuralDistancePanel).setLowerBoundary(boundary);
    }

    /**
     * Returns the lower distance boundary of the currently visible distance
     * panel.
     * 
     * @return the lower distance boundary
     */
    public long getLowerBoundary() {
        return ((!isVisible() || temporalDistancePanel.isVisible())
        ? temporalDistancePanel : structuralDistancePanel).getLowerBoundary();
    }

    /**
     * Sets the unit of the visible distance panel.
     * 
     * @param unit the unit string
     */
    public void setUnit(String unit) {
        if (unit != null) {
            (temporalDistancePanel.isVisible() ? temporalDistancePanel
                                               : structuralDistancePanel).setUnit(unit);
        }
    }

    /**
     * Returns the unit of the currently visible distance panel.
     * 
     * @return the current unit string
     */
    public String getUnit() {
        return (temporalDistancePanel.isVisible() ? temporalDistancePanel
                                                  : structuralDistancePanel).getUnit();
    }

    /**
     * Fills the unitComboBox with all SearchUnits which share the tier on this
     * constraint and the tier of the constraint it refers to
     *
     * @return a configured combobox
     */
    public JComboBox<String> getUnitComboBox() {
        return ((StructuralDistancePanel) structuralDistancePanel).getUnitComboBox();
    }

    /**
     * Sets the upper distance boundary, in milliseconds or number of annotations
     *
     * @param boundary the new boundary
     */
    public void setUpperBoundary(long boundary) {
        (temporalDistancePanel.isVisible() ? temporalDistancePanel
                                           : structuralDistancePanel).setUpperBoundary(boundary);
    }

    /**
     * Returns the upper distance boundary of the currently visible distance
     * panel.
     * 
     * @return the upper distance boundary
     */
    public long getUpperBoundary() {
        return ((!isVisible() || temporalDistancePanel.isVisible())
        ? temporalDistancePanel : structuralDistancePanel).getUpperBoundary();
    }
}
