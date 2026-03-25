package mpi.search.content.query.viewer;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.tree.DefaultTreeModel;

import mpi.search.SearchLocale;
import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.Constraint;
import mpi.search.content.query.model.DependentConstraint;


/**
 * Panel for dependent constraints. 
 *
 * @author Alexander Klassmann
 * @version Aug 18, 2004
 */
@SuppressWarnings("serial")
public class DependentConstraintPanel extends AbstractConstraintPanel {
	/** combo box for constraint modes */
    protected final JComboBox<String> modeComboBox = new JComboBox<String>(Constraint.MODES);

    /**
     * Creates a new DependentConstraintPanel object.
     *
     * @param constraint the dependent constraint
     * @param treeModel the tree model
     * @param type the corpus type
     * @param startAction the action to execute
     */
    public DependentConstraintPanel(DependentConstraint constraint,
        DefaultTreeModel treeModel, CorpusType type, Action startAction) {
        super(constraint, treeModel, type, startAction);

        titleComponent.add(new JLabel(SearchLocale.getString(
                    "Search.Query.With").toUpperCase()));

        if (type.allowsTemporalConstraints()) {
            titleComponent.add(modeComboBox);
        }

        titleComponent.add(new JLabel(SearchLocale.getString(
                    "Search.Query.Constraint")));
        modeComboBox.setRenderer(new LocalizeListCellRenderer());
        modeComboBox.addItemListener(new ItemListener() {
                @Override
				public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        updateMode();
                    }
                }
            });
        tierComboBox = new JComboBox<String>() {
                    @Override
					public Dimension getPreferredSize() {
                        return new Dimension(tierComboBoxWidth,
                            super.getPreferredSize().height);
                    }
                };
                
//		selectedTiers = new ArrayList<String>(6);
//		if (type.allowsSearchOverMultipleTiers() && type.getTierNames().length > 1){
//			if (type instanceof ElanType) {
//				tierComboBox.insertItemAt(Constraint.CUSTOM_TIER_SET, 0);
//			}
//		}

        makeLayout();
        setConstraint(constraint);
        updateMode();
        tierComboBox.addPopupMenuListener(this);
    }

    /**
     * Sets the constraint.
     *
     * @param c the dependent constraint
     */
    public void setConstraint(DependentConstraint c) {
        try {
            setMode(c.getMode());
            patternPanel.setQuantifier(c.getQuantifier());
            //setTierName(c.getTierName());
            setTierNames(c.getTierNames());
            super.setConstraint(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //implementation of ItemListener
    @Override
	public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getSource() == tierComboBox) {
                updateUnitComboBox();
            }
        }

        super.itemStateChanged(e);
    }

    /**
     * Sets the constraint mode.
     * 
     * @param mode the constraint mode
     */
    protected void setMode(String mode) {
        modeComboBox.setSelectedItem(mode);

        //just to be sure; if item didn't change in combobox, nevertheless update!
        updateMode();
    }

    /**
     * Returns the constraints mode.
     * 
     * @return the selected constraint mode
     */
    protected String getMode() {
        return (String) modeComboBox.getSelectedItem();
    }

    /**
     * Updates the user interface.
     */
    protected void update() {
    	String[] tierNamesOfParent = getTierNamesOfParent();
        if (Constraint.ALL_TIERS.equals(tierNamesOfParent[0]) ||
                (tierNamesOfParent.length > 1)) {
            modeComboBox.setSelectedItem(Constraint.TEMPORAL);
            modeComboBox.setEnabled(false);
        } else {
            modeComboBox.setEnabled(true);
        }

        updateTierComboBox();
    }

    /**
     * Updates the user interface and mode.
     */
    protected void updateMode() {
        update();
        relationPanel.setDistanceMode((String) modeComboBox.getSelectedItem());
    }

    @Override
	protected void updateNode() {
        super.updateNode();
        ((DependentConstraint) constraint).setMode(getMode());
        ((DependentConstraint) constraint).setQuantifier(patternPanel.getQuantifier());
    }

    /**
     * Updates the tier combo box.
     */
    protected void updateTierComboBox() {
        if (Constraint.TEMPORAL.equals(getMode())) {
            updateComboBox(tierComboBox, type.getTierNames());
        } else {
            updateComboBox(tierComboBox,
                type.getRelatedTiers(getTierNamesOfParent()[0]));
            updateUnitComboBox();
        }
    }

    /**
     * Fills the unitComboBox with all SearchUnits which share the tier on this
     * constraint and the tier of the constraint it refers to
     */
    protected void updateUnitComboBox() {
        if (Constraint.STRUCTURAL.equals(getMode())) {
            final String tierName1 = (String) tierComboBox.getSelectedItem();
			final String tierName2 = getTierNamesOfParent()[0];
			if (tierName1 != null && tierName2 != null) {
				String[] possibleUnits = (type.getPossibleUnitsFor(tierName1,
	                    tierName2));
	            updateComboBox(relationPanel.getUnitComboBox(), possibleUnits);
			}
        }
    }

    /**
     * Returns the selected tier names of the parent constraint.
     * 
     * @return tier names of the parent constraint
     */
    protected String[] getTierNamesOfParent() {
        return ((Constraint) constraint.getParent()).getTierNames();
    }
    
    /**
     * Sets the selected tier names.
     * <p>
     * Nov 2011: added the option to have more than one tier selected.
     * 
     * @param tierNames a list of selected tier names
     */
	protected void setTierNames(List<String> tierNames) {
		if (tierNames.size() > 0) {
			if (tierNames.size()== 1) {
				setTierName(tierNames.get(0));
			} else {
				if (tierComboBox.getItemCount() > 0 && tierComboBox.getItemAt(0) != Constraint.CUSTOM_TIER_SET) {
					tierComboBox.insertItemAt(Constraint.CUSTOM_TIER_SET, 0);
				}
				tierComboBox.setSelectedItem(Constraint.CUSTOM_TIER_SET);
				selectedTiers = new ArrayList<String>(tierNames);
			}
		} else {
			tierComboBox.setSelectedIndex(0);
		}
	}

	/**
	 * Sets the selected tier names.
	 * 
	 * @param tierNames an array of tier names
	 */
	protected void setTierNames(String[] tierNames) {
		setTierNames(Arrays.asList(tierNames));
	}
}
