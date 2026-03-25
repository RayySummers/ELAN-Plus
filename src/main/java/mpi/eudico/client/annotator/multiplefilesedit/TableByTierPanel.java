package mpi.eudico.client.annotator.multiplefilesedit;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.util.EmptyStringComparator;

/**
 * A panel for a tier table and buttons for adding and removing tiers. 
 */
@SuppressWarnings("serial")
public class TableByTierPanel extends JPanel implements ActionListener {
	private MFEModel model;
	
	private MFETierTable table;
	private JButton addRowButton;
	private JButton addChildTierButton;
	private JButton removeRowButton;
	
	/**
	 * Constructor.
	 * 
	 * @param model the base model for the tier table
	 */
	public TableByTierPanel(MFEModel model) {
		super();
		this.model = model;
		initComponents();
		initCombobox();
	}

	/**
	 * Delegates initialization of combo boxes to the tier table.
	 */
	public void initCombobox() {
		table.initCombobox();
	}
	
	private void initComponents() {
		GridBagLayout lm = new GridBagLayout();
		setLayout(lm);
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		c.gridheight=1;
        c.weightx=1;
        c.weighty=0.9;
		c.fill = GridBagConstraints.BOTH;
		table = new MFETierTable(model);
		
		//Sorting
		TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(table.getModel());
        EmptyStringComparator emptyComp = new EmptyStringComparator();
        for (int i = 1; i < table.getColumnCount(); i++) {
        	rowSorter.setComparator(i, emptyComp);
        }
        table.setRowSorter(rowSorter);
		
		JScrollPane scroll_pane = new JScrollPane(table);
		table.setRowHeight(24);
		//HS reordering false
		table.getTableHeader().setReorderingAllowed(false);
		add(scroll_pane, c);
		
		c.gridwidth = 1;
		c.gridy = 0;
		c.weighty=0;
		addRowButton = new JButton(ElanLocale.getString("MFE.TierTab.AddTier"));
		addRowButton.setActionCommand("addRow");
		addRowButton.addActionListener(this);
		add(addRowButton, c);
		
		c.gridx = 1;
		addChildTierButton = new JButton(ElanLocale.getString("MFE.TierTab.AddDependentTier"));
		addChildTierButton.setActionCommand("addDependentTier");
		addChildTierButton.addActionListener(this);
		add(addChildTierButton, c);
		
		c.gridx = 2;
		removeRowButton = new JButton(ElanLocale.getString("MFE.TierTab.RemoveTier"));
		removeRowButton.setActionCommand("removeRow");
		removeRowButton.addActionListener(this);
		add(removeRowButton, c);
		
		enableUI(false);
	}
	
	
	/**
	 * Not implemented.
	 */
	public void updateLocale() {
		
	}

	/**
	 * Enables or disables the buttons.
	 * 
	 * @param b if {@code true} the buttons are enabled
	 */
	public void enableUI(boolean b) {
		addRowButton.setEnabled(b);
		addChildTierButton.setEnabled(b);
		removeRowButton.setEnabled(b && model.areTiersRemovable());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JButton)
		{
			if(e.getActionCommand().equals("addRow")) {
				int new_row = ((TableByTierModel)table.getModel()).newRow();
				table.newRow(new_row);
				table.showCell(table.convertRowIndexToView(table.getRowCount()-1), 0);
			}else if(e.getActionCommand().equals("addDependentTier")) {
				String[] parentTiers = model.getConsistentTierNames();
				if(parentTiers != null){
					AddChildTierDialog dlg = new AddChildTierDialog(parentTiers);
					dlg.setVisible(true);
					String parentName = dlg.parentName;
					int stereotype = dlg.stereotype;
					if(parentName != null && stereotype > -1){
						int new_row = ((TableByTierModel)table.getModel()).newRowForChildTier(parentName, stereotype);
						table.newRow(new_row);
						table.showCell(table.convertRowIndexToView(table.getRowCount()-1), 0);
					}
				}
			} else if (e.getActionCommand().equals("removeRow")) {
				int[] selectedRows=table.getSelectedRows();
				int[] convertedSelectedRows=new int[selectedRows.length];
				for(int i=0;i<selectedRows.length;i++) {
					convertedSelectedRows[i]=table.convertRowIndexToModel(selectedRows[i]);
				}
				((TableByTierModel)table.getModel()).removeRows(convertedSelectedRows);
			}
		}
	}

	/**
	 * Notification that a row was added.
	 * 
	 * @param row_nr the index of the added row
	 */
	public void rowAdded(int row_nr) {
		((TableByTierModel)table.getModel()).fireTableRowsInserted(row_nr, row_nr);
	}
	
	/**
	 * A dialog that allows to select a target parent tier and a type for the
	 * new dependent tier.
	 * 
	 * @author aarsom
	 */
	private class AddChildTierDialog extends ClosableDialog implements ActionListener{
		private JComboBox<String> parentComboBox;
		private JComboBox<String> stereotypeComboBox;
		private JButton addButton;
		private JButton cancelButton;
		
		protected String parentName;
		protected int stereotype;
		
		private final String SELECT = ElanLocale.getString("InterlinearAnalyzerConfigDlg.ComboBoxDefaultString");
		
		/**
		 * Constructor.
		 * 
		 * @param parentTiers an array of potential parent tiers
		 */
		public AddChildTierDialog(String[] parentTiers){
			super((MFEFrame)TableByTierPanel.this.getTopLevelAncestor(), ElanLocale.getString("MFE.TierTab.AddDependentTier"), true);
			initComponents(parentTiers);
			postInit();
		}
		
		/**
	     * Pack, size and set location.
	     */
	    private void postInit() {
	    	pack();       
	        setResizable(true);
	        setLocationRelativeTo(getParent());
	    }
	    
	    /**
	     * Closes this dialog
	     */
	    private void doClose() {
			setVisible(false);
			dispose();
		}
		
		private void initComponents(String[] parentTiers){
			setLayout(new GridBagLayout());
			
			parentComboBox =  new JComboBox<String>();
			parentComboBox.addItem(SELECT);
			for (String parentTier : parentTiers) {
				parentComboBox.addItem(parentTier);
			}
			parentComboBox.addActionListener(this);
			
			stereotypeComboBox =  new JComboBox<String>();

			stereotypeComboBox.addItem(SELECT);
			stereotypeComboBox.setEnabled(false);
			stereotypeComboBox.addActionListener(this);
			
			addButton = new JButton(ElanLocale.getString("Button.Add"));
			addButton.addActionListener(this);
			
			cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
			cancelButton.addActionListener(this);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(addButton);
			buttonPanel.add(cancelButton);
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.insets = new Insets(10,10,10,10);
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.gridwidth = 2;
			add(new JLabel(ElanLocale.getString("MFE.TierTab.Select")), gbc);
			
			gbc.insets = new Insets(10,30,4,6);
			
			gbc.gridy = 1;
			gbc.gridwidth = 1;
			add(new JLabel(ElanLocale.getString("MFE.TierTab.SelectParent")), gbc);
			
			gbc.gridx = 1;
			add(parentComboBox, gbc);
			
			gbc.gridx = 0;
			gbc.gridy = 2;
			add(new JLabel(ElanLocale.getString("OverlapsDialog.Column.Stereotype")), gbc);
		
			gbc.gridx = 1;
			add(stereotypeComboBox, gbc);
			
			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.gridwidth = 2;
			gbc.weightx = 1.0;
			gbc.insets = new Insets(4,6,4,6);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.SOUTH;
			add(buttonPanel, gbc);
			
			addButton.setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(!parentComboBox.getSelectedItem().equals(SELECT) &&
					!stereotypeComboBox.getSelectedItem().equals(SELECT)){
				addButton.setEnabled(true);
			} else {
				addButton.setEnabled(false);
			}
			
			if(e.getSource() ==  parentComboBox){
				Object s = parentComboBox.getSelectedItem();
				
				if(s != null && !s.equals(SELECT)){
					stereotypeComboBox.removeActionListener(this);
					stereotypeComboBox.removeAllItems();
					stereotypeComboBox.addItem(SELECT);
					stereotypeComboBox.setEnabled(true);
					
					Constraint parentConstraint = model.getTierByName((String) s).getLinguisticType().getConstraints();
					if (parentConstraint != null && 
							(parentConstraint.getStereoType() == Constraint.SYMBOLIC_SUBDIVISION ||
		                     parentConstraint.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)) {
						for(String cons : model.getConsistentStereoTypeNames() /*Constraint.publicStereoTypes*/){
							if(cons.toLowerCase().startsWith("symbolic")){
								stereotypeComboBox.addItem(cons);
							}
						}
					} else {
						for(String cons : model.getConsistentStereoTypeNames() /*Constraint.publicStereoTypes*/){
							stereotypeComboBox.addItem(cons);
						}
		            }
					
					stereotypeComboBox.addActionListener(this);
				} else {
					stereotypeComboBox.setEnabled(false);
					stereotypeComboBox.removeActionListener(this);
					stereotypeComboBox.removeAllItems();
					stereotypeComboBox.addActionListener(this);
					stereotypeComboBox.addItem(SELECT);
				}
			} else if( e.getSource() == addButton){
				parentName = (String) parentComboBox.getSelectedItem();
				String constraint_string = (String) stereotypeComboBox.getSelectedItem();
				stereotype = -1;
				
				if (Objects.equals(constraint_string, Constraint.publicStereoTypes[0])) {
					//Time Subdivision
					stereotype = Constraint.TIME_SUBDIVISION;
				} else if (Objects.equals(constraint_string, Constraint.publicStereoTypes[1])) {
					//Included In
					stereotype = Constraint.INCLUDED_IN;
				} else if (Objects.equals(constraint_string, Constraint.publicStereoTypes[2])) {
					//Symbolic Subdivision
					stereotype = Constraint.SYMBOLIC_SUBDIVISION;
				} else if (Objects.equals(constraint_string, Constraint.publicStereoTypes[3])) {
					//Symbolic Association
					stereotype = Constraint.SYMBOLIC_ASSOCIATION;
				}
				doClose();
			} else if(e.getSource() == cancelButton){
				parentName = null;
				stereotype = -1;
				doClose();
			}	
		}
	}
}
