package mpi.search.content.query.viewer;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeModel;

import mpi.search.SearchLocale;
import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.Constraint;

/**
 * Class for the user interface of the anchor or first constraint of a search
 * query.
 * 
 * @version 2006
 * @version 2020 removed ElanType dependency
 * @author Alex Klassmann
 */
@SuppressWarnings("serial")
public class AnchorConstraintPanel extends AbstractConstraintPanel {
	/** locked constraint label */
	final private JLabel lockedConstraintLabel = new JLabel();
	/** locked tier name */
	private String lockedTierName = null;
	/** locked panel */
	private String lockedPattern = null;

	/**
	 * Creates a new constraint panel.
	 * 
	 * @param constraint the anchor constraint
	 * @param treeModel the tree model for the hierarchical constraint
	 * @param type the corpus type
	 * @param startAction the start action
	 */
	public AnchorConstraintPanel(AnchorConstraint constraint, DefaultTreeModel treeModel, CorpusType type, Action startAction) {
		super(constraint, treeModel, type, startAction);
		// 'head line' of constraint
		titleComponent.add(
			new JLabel(
				SearchLocale.getString("Search.Query.Find").toUpperCase()));
		titleComponent.setBorder(new EmptyBorder(0, 0, 5, 0));

		tierComboBox = new JComboBox<String>(type.getTierNames()) {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				tierComboBoxWidth = this.getPreferredSize().width;
			}
		};

		if (type.allowsSearchOverMultipleTiers() && type.getTierNames().length > 1){
			tierComboBox.insertItemAt(Constraint.ALL_TIERS, 0);
		}
		
		lockedConstraintLabel.setHorizontalAlignment(JLabel.CENTER);
		framedPanel.add(lockedConstraintLabel, "locked");
		
		makeLayout();
		setConstraint(constraint);
		tierComboBox.addPopupMenuListener(this);
	}

	@Override
	protected void setTierName(String tierName) {
		for (int i = 0; i < type.getIndexTierNames().length; i++) {
			if (type.getIndexTierNames()[i].equals(tierName.toUpperCase())) {
				lockedTierName = tierName.toUpperCase();
				framedPanelLayout.show(framedPanel, "locked");
				return;
			}
		}
		//if lockedField was set, reset.
		if (lockedTierName != null) {
			lockedTierName = null;
			lockedPattern = null;
			framedPanelLayout.show(framedPanel, "");
		}
		super.setTierName(tierName);
	}

	/**
	 * Sets the tier names to be selected in the combo box.
	 * 
	 * @param tierNames a list of tier names
	 */
	protected void setTierNames(List<String> tierNames) {
		if (tierNames.size() > 0) {
			if (tierNames.size() == 1) {
				setTierName(tierNames.get(0));
			} else {
				tierComboBox.setSelectedItem(Constraint.CUSTOM_TIER_SET);
				selectedTiers = new ArrayList<String>(tierNames);
			}
		} else {
			tierComboBox.setSelectedIndex(0);
		}
	
	}
	
	/**
	 * Sets the tier names to be selected in the combo box.
	 * 
	 * @param tierNames an array of tier names
	 */
	protected void setTierNames(String[] tierNames) {
		setTierNames(Arrays.asList(tierNames));
	}
	
	@Override
	public String getTierName(){
	    return lockedTierName == null ? super.getTierName() : lockedTierName;
	}

	@Override
	protected void setPattern(String pattern) {
		if (lockedTierName != null) {
			lockedPattern = pattern;
			lockedConstraintLabel.setText(type.getUnabbreviatedTierName(lockedTierName) + " " + lockedPattern);
		} else {
			super.setPattern(pattern);
		}
	}

	@Override
	protected String getPattern(){
	    return lockedPattern == null ? super.getPattern() : lockedPattern;
	}
	
	/**
	 * Sets the {@code AnchorConstraint} for this panel.
	 * 
	 * @param c the constraint object
	 */
	public void setConstraint(AnchorConstraint c){
		setTierNames(c.getTierNames());
		super.setConstraint(c);
	}
}
