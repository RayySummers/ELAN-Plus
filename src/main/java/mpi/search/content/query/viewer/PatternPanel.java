package mpi.search.content.query.viewer;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.TreeNode;

import mpi.eudico.util.CVEntry;
import mpi.search.SearchLocale;
import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.AnchorConstraint;


/**
 * A class for the user interface for defining a query pattern.
 *  
 * @author klasal
 */
@SuppressWarnings("serial")
public class PatternPanel extends JPanel implements ItemListener {
    /** Holds cardLayout */
    protected final CardLayout inputLayout = new CardLayout();

    /** Holds value of quantifier input component */
    protected final JComboBox<String> quantifierComboBox = new JComboBox<String>(AnchorConstraint.QUANTIFIERS);

    /** Holds container for pattern input component (TextField or ComboBox) */
    protected final JPanel inputPanel = new JPanel(inputLayout);

    /** Holds value of regular expression or string */
    protected JTextField textField = new JTextField(12);
    /** a flag to override the selection of input component based on selected tier */
    protected boolean enforceDefaultInputComponent = false;
    /** store a reference to the tier selection combo box */
    protected JComboBox<String> tierComboBox;
    /** the corpus type */
    protected final CorpusType type;

    /* 
     * if LightwightPopup is enabled, one gets (at least for linux/java 1.5)
     * weird behavior of the tooltips for closed vocabularies:
     * the tooltips are BEHIND the jcomboBox-menu and the items in the menu are not 
     * always changed back from selected to unselected
     * This has most probably to do with the fact, that the ConstraintPanel itself is already
     * part of the CellRenderer of the constraint-JTable (comboBoxes elsewhere work fine).
     * Setting this property at the specific JComboBox has no effect. 
     */
    static{
    	//has unwanted side effects
        //ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    }
    
    /**
     * Creates a new PatternPanel object.
     *
     * @param type the corpus type, representing one or more files with attached constraints
     * @param tierComboBox a list of tiers to select as input source
     * @param node the tree node this panel is part of
     * @param startAction the action to perform when search starts
     */
    public PatternPanel(CorpusType type, JComboBox<String> tierComboBox, TreeNode node,
        final Action startAction) {
        this.type = type;
        this.tierComboBox = tierComboBox;
        textField.setFont(getFont().deriveFont(Font.BOLD, (getFont().getSize2D() * 1.2f)));
        inputPanel.add(textField, "");

        for (int j = 0; j < type.getTierNames().length; j++) {
            String tierName = type.getTierNames()[j];
            List<CVEntry> closedVoc = type.getClosedVoc(tierName);

            if (closedVoc != null) {
                final JComboBox<Object> comboBox = new JComboBox<Object>(closedVoc.toArray()) {
                        @Override
						public Dimension getPreferredSize() {
                            return new Dimension(textField.getPreferredSize().width,
                                super.getPreferredSize().height);
                        }
                    };

                comboBox.setMaximumRowCount(15); // larger than swing default
                //renderer doesn't work properly with lightweight tooltips
                comboBox.setEditable(type.isClosedVocEditable(closedVoc));
                // the following is mainly relevant in case the box is editable
                /*
                comboBox.addItemListener(new ItemListener(){
                	@Override
					public void itemStateChanged(ItemEvent e){
                		if(e.getStateChange() == ItemEvent.SELECTED)
                			//regular expression if edited and different to all entries
                			regExCheckBox.setSelected(comboBox.getSelectedItem() instanceof String 
                					&& getMatchingIndex(comboBox, (String) comboBox.getSelectedItem()) == -1);
                	}
                });
				*/
                inputPanel.add(comboBox, tierName);
            }
        }

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel label;
        int gx = 0;
        gbc.gridx = gx;

        if ((node.getParent() == null) || !type.allowsQuantifierNO()) {
            label = new JLabel(SearchLocale.getString(AnchorConstraint.ANY));
            label.setFont(getFont().deriveFont(Font.PLAIN));
            add(label, gbc);
        } else {
        	gbc.fill = GridBagConstraints.HORIZONTAL;
        	gbc.weightx = 0.1;
            add(quantifierComboBox, gbc);
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
        }

        gbc.gridx = ++gx;
        label = new JLabel(String.join(" ", SearchLocale.getString("Search.Annotation_SG"), 
        		SearchLocale.getString("Search.Constraint.OnTier") ));
        label.setFont(getFont().deriveFont(Font.PLAIN));
        add(label, gbc);
        gbc.gridx = ++gx;
        add(tierComboBox, gbc);
        gbc.gridx = ++gx;
        if (node.getParent() == null) {
            label = new JLabel(" " +
                    SearchLocale.getString("Search.Constraint.That"));
            label.setFont(getFont().deriveFont(Font.PLAIN));
            add(label, gbc);
            gbc.gridx = ++gx;
        }

        label = new JLabel(" " +
                SearchLocale.getString("Search.Constraint.Matches") + " ");
        label.setFont(getFont().deriveFont(Font.PLAIN));
        add(label, gbc);
        gbc.gridx = ++gx;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(inputPanel, gbc);

        inputLayout.show(inputPanel, "");
        quantifierComboBox.setRenderer(new LocalizeListCellRenderer());
        quantifierComboBox.setSelectedItem(AnchorConstraint.ANY);
        tierComboBox.addItemListener(this);
        tierComboBox.setRenderer(new TierListCellRenderer(type));
        textField.requestFocus();

        if (startAction != null) {
            KeyListener l = new KeyAdapter() {
                    @Override
					public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            startAction.actionPerformed(new ActionEvent(
                                    e.getSource(), e.getID(),
                                    KeyEvent.getKeyText(e.getKeyCode())));
                        }
                    }
                };

            textField.addKeyListener(l);
        }
    }

    /**
     * Returns the default input component, the query text field.
     *  
     * @return the search query text field
     */
    public Component getDefaultInputComponent() {
        return textField;
    }

    /**
     * Sets the pattern for the text field or combo box.
     * 
     * @param pattern the pattern to set in the text field or select the 
     * current CV entry combo box
     */
    public void setPattern(String pattern) {
        Component c = getVisibleInputComponent();

        if (c instanceof JTextField) {
            ((JTextField) c).setText(pattern);
        } else {
        	JComboBox<?> comboBox = (JComboBox<?>) c;
        	int matchingIndex = getMatchingIndex(comboBox, pattern);
        	if(matchingIndex >=0 ){
        		comboBox.setSelectedIndex(matchingIndex);
        	}
        	else{ 
        		comboBox.setSelectedItem(pattern);
        	}
        	
        }

        c.requestFocus();
    }

    /*
     * test if pattern is contained in closed vocabulary 
     * returns the index of that item whose .toString() method equals pattern; -1 if no item matches
     */
    private int getMatchingIndex(JComboBox<?> comboBox, String pattern){
    	int selectedIndex = -1;
    	for(int i=0; i<comboBox.getItemCount(); i++){
    		if(comboBox.getItemAt(i).toString().equals(pattern)){
    			comboBox.setSelectedIndex(i);
    			selectedIndex = i;
    			break;
    		}
    	}
    	return selectedIndex;
    }
    
    /** 
     * Returns the current pattern from the user interface.
     * 
     * @return string from input field (text or selected item from closed vocabulary combo box)
     */
    public String getPattern() {
        Component c = getVisibleInputComponent();
        return (c instanceof JTextField) ? ((JTextField) c).getText()
                                         : ((JComboBox<?>) c).getSelectedItem()
                                            .toString();
    }

    /**
     * Sets the quantifier to select.
     * 
     * @param quantifier the value to select in the quantifier combo box
     */
    public void setQuantifier(String quantifier) {
        quantifierComboBox.setSelectedItem(quantifier);
    }

    /**
     * Returns the current quantifier.
     * 
     * @return the selected quantifier string
     */
    public String getQuantifier() {
        return (String) quantifierComboBox.getSelectedItem();
    }

    /**
     * Returns the input component.
     * 
     * @return either the query textfield or a combobox with CV entry values
     */
    public Component getVisibleInputComponent() {
    	if (enforceDefaultInputComponent) {
    		return textField;
    	}
        Component[] comps = inputPanel.getComponents();

        for (int i = 0; i < comps.length; i++) {
            if (comps[i].isVisible()) {
                return comps[i];
            }
        }

        return textField;
    }
    
    /**
     * A method to override standard behavior by requesting a specific type 
     * of input component.
     * 
     * @param enforceDefault if true the input textfield will be shown, regardless
     * of selected tier and possible controlled vocabulary
     */	
    public void setUseDefaultInputComponent(boolean enforceDefault) {
    	enforceDefaultInputComponent = enforceDefault; 
    	// force a ui update
    	updateInputPanel((String) tierComboBox.getSelectedItem());
    }
    
    /**
     * Switches between the query input textfield and a closed vocabulary 
     * combobox in case the selected tier is linked to a CV and the default
     * textfield is not enforced (e.g. for regular expression search).
     * 
     * @param tierName the selected tier name or null if the textfield is required
     */
    protected void updateInputPanel(String tierName) {
    	if (tierName == null || tierName.isEmpty()) {
    		inputLayout.show(inputPanel, "");
    	} else {
    		if (enforceDefaultInputComponent) {
    			inputLayout.show(inputPanel, "");
    		} else {
                if (type.isClosedVoc(tierName)) {
                    inputLayout.show(inputPanel, tierName);
                } else {
                    inputLayout.show(inputPanel, "");
                }
    		}
    	}
    	
        inputPanel.setLocale(type.getDefaultLocale(tierName));
        validate();
        repaint();
    }

    /**
     * Tier name selection listener. If the selected tier has a controlled 
     * vocabulary linked to it, the search query textfield is replaced by 
     * the CV entry pull down list
     *
     * @param e the event
     */
    @Override
	public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
        	updateInputPanel((String) e.getItem());
        	/*
        	if(enforceDefaultInputComponent) {
        		inputLayout.show(inputPanel, "");
        	} else
            if (type.isClosedVoc((String) e.getItem())) {
                inputLayout.show(inputPanel, (String) e.getItem());
            } else {
                inputLayout.show(inputPanel, "");
            }

            inputPanel.setLocale(type.getDefaultLocale((String) e.getItem()));
            validate();
            repaint();
            */
        }
    }
}
