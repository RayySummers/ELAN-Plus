package mpi.eudico.client.annotator.search.query.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.TierSortAndSelectDialog2;
import mpi.eudico.client.annotator.search.model.ElanType;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.search.SearchLocale;
import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.Constraint;
import mpi.search.content.query.model.DependentConstraint;
import mpi.search.content.query.model.RestrictedAnchorConstraint;
import mpi.search.content.query.viewer.AbstractConstraintPanel;
import mpi.search.content.query.viewer.AttributeConstraintPanel;
import mpi.search.content.query.viewer.DependentConstraintPanel;


/**
 * ELAN specific subclass with extra font options.
 * 
 * @author HS
 * @version Aug 2008
  */
@SuppressWarnings("serial")
public class ElanDependentConstraintPanel extends DependentConstraintPanel implements MouseListener{
    /**
     * Creates a new ElanDependentConstraintPanel instance.
     *
     * @param constraint the dependent constraint
     * @param treeModel the tree model the constraint panel is part of
     * @param type the corpus type
     * @param startAction the action to start the search
     */
    public ElanDependentConstraintPanel(DependentConstraint constraint,
        DefaultTreeModel treeModel, CorpusType type, Action startAction) {
        super(constraint, treeModel, type, startAction);
    }

    /**
     * Creates the ui elements and layout.
     */
    @Override
	protected void makeLayout() {
        //setFont(Constants.DEFAULTFONT);
        //RegExPanel
        patternPanel = new ElanPatternPanel(type, tierComboBox,
                constraint, startAction, Constants.DEFAULTFONT);

        //RelationPanel
        relationPanel = new ElanRelationPanel(type, constraint, Constants.DEFAULTFONT);

        //OptionPanel
        JPanel checkBoxPanel = new JPanel(new GridBagLayout());
        //regExCheckBox.setFont(Constants.deriveSmallFont(regExCheckBox.getFont()));
        //caseCheckBox.setFont(Constants.deriveSmallFont(caseCheckBox.getFont()));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;       
        
        checkBoxPanel.add(regExCheckBox, gbc);
        gbc.gridy = 1;
        checkBoxPanel.add(caseCheckBox, gbc);
        optionPanel.add(checkBoxPanel, BorderLayout.NORTH);
        regExCheckBox.addChangeListener(checkBoxListen);
        
        //InputPanel
        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 0, 1));
        inputPanel.add(patternPanel);
        inputPanel.add(relationPanel);

        //AttributePanel
        if (type.hasAttributes()) {
            attributePanel = new AttributeConstraintPanel(type);
            optionPanel.add(attributePanel, BorderLayout.CENTER);
            attributePanel.setTier(getTierName());
        }

        //FramedPanel
        JPanel specificationPanel = new JPanel(new BorderLayout());
        specificationPanel.add(inputPanel, BorderLayout.CENTER);
        specificationPanel.add(optionPanel, BorderLayout.EAST);
        framedPanel.add(specificationPanel, "");
        framedPanel.setBorder(blueBorder);
        framedPanelLayout.show(framedPanel, "");

        //this
        setLayout(new BorderLayout());
        add(titleComponent, BorderLayout.NORTH);
        add(framedPanel, BorderLayout.CENTER);

        tierComboBox.addItemListener(this);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
        Action addConstraintAction = new AbstractAction(SearchLocale.getString(
                    "Search.Query.Add")) {
                @Override
				public void actionPerformed(ActionEvent e) {
                    addConstraint();
                }
            };

        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_A,
                ActionEvent.CTRL_MASK);
        addConstraintAction.putValue(Action.ACCELERATOR_KEY, ks);

        JButton addButton = new JButton(addConstraintAction);
        //addButton.setFont(getFont().deriveFont(11f));
        buttonPanel.add(addButton);

        if ((constraint.getParent() != null) &&
                !(constraint.getParent() instanceof RestrictedAnchorConstraint)) {
            Action deleteConstraintAction = new AbstractAction(SearchLocale.getString(
                        "Search.Query.Delete")) {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        deleteConstraint();
                    }
                };

            ks = KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK);
            deleteConstraintAction.putValue(Action.ACCELERATOR_KEY, ks);

            JButton deleteButton = new JButton(deleteConstraintAction);
            //deleteButton.setFont(getFont().deriveFont(11f));
            buttonPanel.add(deleteButton);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        try {
            Class<?> popupMenu = type.getInputMethodClass();
            popupMenu.getConstructor(new Class[] {
                    Component.class, AbstractConstraintPanel.class
                })
                     .newInstance(new Object[] {
                    patternPanel.getDefaultInputComponent(),
                    ElanDependentConstraintPanel.this
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
        // add mouse listener, to consume mouse clicks on any part other than combo boxes
        // checkboxes etc. in order to prevent the creation of a new editor component
        // when a click occurs in the editor panel.
        addMouseListener(this);
    }

    @Override
    /**
     * Transfers the focus to the textfield of the Pattern panel.
     */
	public void grabFocus() {
		patternPanel.grabFocus();
	}

	@Override
	protected void updateTierComboBox() {
        if (Constraint.TEMPORAL.equals(getMode())) {
        	String[] tierNames = type.getTierNames();
            if (type instanceof ElanType) {
            		String[] tnPlus = new String[tierNames.length + 1];
            		System.arraycopy(tierNames, 0, tnPlus, 1, tierNames.length);
            		tnPlus[0] = Constraint.CUSTOM_TIER_SET;
            		updateComboBox(tierComboBox, tnPlus);
            } else {
            	updateComboBox(tierComboBox, tierNames);
            }
        } else {
            final String tierName = getTierNamesOfParent()[0];
            if (tierName != null) {
				updateComboBox(tierComboBox,
	                type.getRelatedTiers(tierName));
            }
            updateUnitComboBox();
        }
    }
    
    /**
     * Shows an extended tier selection dialog that allows for tier
     * selection based on type, participant or annotator.
     */
	@Override
	protected void selectCustomTierSet() {
    	if ( !(type instanceof ElanType) ) {
    		return; // message??
    	}
    	Transcription trans = ((ElanType) type).getTranscription();
		// popup an extended tier selection window
    	Window w = SwingUtilities.getWindowAncestor(this);
    	List<String> allTiers = new ArrayList<String>();
    	String[] tierNames = type.getTierNames();// all tiers
    	for (String s : tierNames) {
    		allTiers.add(s);
    	}
    	List<String> sTiers = new ArrayList<String>();
    	if (selectedTiers != null) {
    		sTiers.addAll(selectedTiers);
    	} else {
    		String[] curTiers = getTierNames();
    		if (curTiers.length == 1 && curTiers[0] == Constraint.ALL_TIERS) {   			
    			List<String> oldSelTiers = Preferences.getListOfString("Search.Dependent.SelectedTiers", trans);
    	    	
    	    	if (oldSelTiers != null) {
    	    		sTiers.addAll(oldSelTiers);
    	    	} else {
    	    		sTiers.addAll(allTiers);
    	    	}

    		} else {
    			for (String s: curTiers) {
    				sTiers.add(s);
    			}
    		}
    	}
    	
    	TierSortAndSelectDialog2 dialog = null;
    	if (w instanceof Dialog) {
    		dialog = new TierSortAndSelectDialog2((Dialog) w, 
    				(TranscriptionImpl) ((ElanType) type).getTranscription(),
    				allTiers, sTiers);
    	} else if (w instanceof Frame) {
    		dialog = new TierSortAndSelectDialog2((Frame) w, 
    				(TranscriptionImpl) ((ElanType) type).getTranscription(),
    				allTiers, sTiers);
    	}
    	if (dialog == null) {
    		return;
    	}
    	String modePref = Preferences.getString("Search.Dependent.TierSelectionMode", trans);
    	List<String> itemObj = Preferences.getListOfString("Search.Dependent.HiddenItems", trans);
    	if (modePref != null) {
    		dialog.setSelectionMode(modePref, itemObj);
    	}
    	
    	dialog.setTitle(ElanLocale.getString("TranscriptionManager.SelectTierDlg.Title"));
    	dialog.setLocationRelativeTo(this);
    	dialog.setVisible(true);
    	
    	
    	List<String> selTiers = dialog.getSelectedTiers();
    	if (selTiers != null) {
   			setTierNames(selTiers);
    		
    		String mode = dialog.getSelectionMode();
    		List<String> items = dialog.getUnselectedItems();
    		
    		Preferences.set("Search.Dependent.SelectedTiers", selTiers, trans);
    		Preferences.set("Search.Dependent.TierSelectionMode", mode, trans);
    		Preferences.set("Search.Dependent.HiddenItems", items, trans);
    	} else {
    		// nothing changed
    	}
	}
	
	@Override
	protected void setRegEx(boolean regEx) {
		super.setRegEx(regEx);
		
		patternPanel.setUseDefaultInputComponent(isRegEx());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getSource() == tierComboBox) {
            	// copied from DependentConstraintPanel
            	updateUnitComboBox();
            	
            	if (e.getItem() != Constraint.CUSTOM_TIER_SET) {
            		selectedTiers = null;
            	}
                if (type.isClosedVoc((String) e.getItem())) {

                    if (type.getClosedVoc((String) e.getItem()).isEmpty()) {
                        startAction.setEnabled(false);
                    } else {
                        startAction.setEnabled(true);
                    }
                } else {
                    startAction.setEnabled(true);
                }

                if (type.strictCaseSensitive((String) e.getItem())) {
                    setCaseSensitive(true);
                }

                if (attributePanel != null) {
                    attributePanel.setTier((String) e.getItem());
                }
                // prevent super implementations
                return;
            }
        }
        // other actions in super
		super.itemStateChanged(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		e.consume();	
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		e.consume();		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
}
