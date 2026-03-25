package mpi.eudico.client.annotator.multiplefilesedit;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.IncludedIn;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicSubdivision;
import mpi.eudico.server.corpora.clomimpl.type.TimeSubdivision;
import mpi.eudico.util.EmptyStringComparator;

/**
 * A panel for the tier type table and a add type button. 
 *
 */
public class TableByTypePanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 685888690974798752L;
	private MFEModel model;
	private MFEFrame parent;
	
	private MFETypeTable table;
	private JButton addRowButton;
	private TableRowSorter<TableModel> rowSorter;

	/**
	 * Constructor.
	 * 
	 * @param model the base data model
	 * @param parent the parent frame
	 */
	public TableByTypePanel(MFEModel model, MFEFrame parent) {
		super();
		this.model = model;
		this.parent = parent;
		initComponents();
	}

	private void initComponents() {
		GridBagLayout lm = new GridBagLayout();
		setLayout(lm);

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 0.9;
		c.fill = GridBagConstraints.BOTH;
		table = new MFETypeTable(model, parent);
		
		//Sorting
		rowSorter = new TableRowSorter<TableModel>(table.getModel());
        EmptyStringComparator emptyComp = new EmptyStringComparator();
        for (int i = 0; i < table.getColumnCount(); i++) {
    		if(i != MFEModel.TYPE_TIMEALIGNABLECOLUMN) {
    			rowSorter.setComparator(i, emptyComp);
        	} else {
        		rowSorter.setSortable(i, false);
        	}
        }
        table.setRowSorter(rowSorter);
		
		JScrollPane scroll_pane = new JScrollPane(table);
		//HS reordering false
		table.getTableHeader().setReorderingAllowed(false);
		add(scroll_pane, c);

		c.gridwidth = 1;
		c.gridy = 0;
		c.weighty = 0;
		addRowButton = new JButton(ElanLocale.getString("MFE.TierTab.AddType"));
		addRowButton.setActionCommand("addRow");
		addRowButton.addActionListener(this);
		add(addRowButton, c);
		
		enableUI(false);
	}

	/**
	 * Not implemented.
	 */
	public void updateLocale() {

	}

	/**
	 * Enables or disables the add type button.
	 * 
	 * @param b if {@code true} enables the button
	 */
	public void enableUI(boolean b) {
		addRowButton.setEnabled(b);
		
		if (!b) {
			table.setRowSorter(null);
		} else {
			if (table.getRowSorter() == null) {
				rowSorter = new TableRowSorter<TableModel>(table.getModel());
		        EmptyStringComparator emptyComp = new EmptyStringComparator();
		        for (int i = 0; i < table.getColumnCount(); i++) {
		    		if(i != MFEModel.TYPE_TIMEALIGNABLECOLUMN) {
		    			rowSorter.setComparator(i, emptyComp);
		        	} else {
		        		rowSorter.setSortable(i, false);
		        	}
		        }
		        table.setRowSorter(rowSorter);
		        rowSorter.sort();
			}
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			if (e.getActionCommand().equals("addRow")) {
				Constraint constraint = null;
				ArrayList<String> constraintList = new ArrayList<String>();
				constraintList.add("None");
				constraintList.addAll(Arrays.asList(Constraint.publicStereoTypes));
				String constraint_string = (String)JOptionPane.showInputDialog(
						this,
						"Pick a stereotype",
						"Add Linguistic Type",
						JOptionPane.PLAIN_MESSAGE,
						null,
						constraintList.toArray(),
						"ham");
				if (Objects.equals(constraint_string, "None")) {
					// None
				} else if (Objects.equals(constraint_string, Constraint.publicStereoTypes[0])) {
					//Time Subdivision
					constraint = new TimeSubdivision();
				} else if (Objects.equals(constraint_string, Constraint.publicStereoTypes[1])) {
					//Included In
					constraint = new IncludedIn();
				} else if (Objects.equals(constraint_string, Constraint.publicStereoTypes[2])) {
					//Symbolic Subdivision
					constraint = new SymbolicSubdivision();
				} else if (Objects.equals(constraint_string, Constraint.publicStereoTypes[3])) {
					//Symbolic Association
					constraint = new SymbolicAssociation();
				}
				((TableByTypeModel)table.getModel()).newRow(constraint);
				parent.initCombobox();
				table.showCell(table.getRowCount()-1, 0);
			} else if (e.getActionCommand().equals("removeRow")) {
			}
		}
	}

	/**
	 * Notification that a row for a type was added.
	 * 
	 * @param row_nr the row index
	 */
	public void rowAdded(int row_nr) {
		((TableByTypeModel) table.getModel()).fireTableRowsInserted(row_nr, row_nr);
	}
    
	/**
	 * Notifies the underlying table model that data was changed.
	 */
    public void dataChanged() {
        ((TableByTypeModel)table.getModel()).fireTableDataChanged();
    }
}
