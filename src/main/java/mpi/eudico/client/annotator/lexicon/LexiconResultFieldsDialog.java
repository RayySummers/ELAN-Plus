package mpi.eudico.client.annotator.lexicon;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.tier.TierExportTable;
import mpi.eudico.client.annotator.tier.TierExportTableModel;
import mpi.eudico.server.corpora.lexicon.LexicalEntryFieldIdentification;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClientException;

/**
 * Dialog that lists all the result fields from the selected lexicon service
 * and allows the user to select and save the result fields. 
 * Only the selected fields will be visible in the result fields.
 */
@SuppressWarnings("serial")
public class LexiconResultFieldsDialog extends ClosableDialog implements ActionListener{

	private LexiconLink link;
	private TierExportTableModel model;
	private JTable fieldsTable;
	private JLabel titleLabel;
	private JPanel buttonPanel;
	private JButton closeButton;
	private JButton saveButton;
	
	
	/**
	 * Creates a new LexiconResultFieldsDialog instance
	 * @param owner the owner frame 
	 * @param link the lexicon link
	 * @throws HeadlessException throws headlessException
	 */
	public LexiconResultFieldsDialog(Frame owner, LexiconLink link) throws HeadlessException {
		super(owner);
		this.link = link;
		initComponents();
		getAllFieldNames();
        postInit();
        setVisible(true);
	}
	
	/**
	 * Initializes the components
	 */
    private void initComponents() {
    	 setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    	 
    	 addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent evt) {
 				closeDialog();
 			}
 		 });
    	 
    	 titleLabel = new JLabel();
         titleLabel.setFont(titleLabel.getFont().deriveFont((float) 12));
         titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

         getContentPane().setLayout(new GridBagLayout());
         getContentPane().setPreferredSize(new Dimension(400, 300));
         Insets insets = new Insets(2, 6, 2, 6);
         GridBagConstraints gridBagConstraints;

         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = GridBagConstraints.NORTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = insets;
         getContentPane().add(titleLabel, gridBagConstraints);
    	 
    	 model = new TierExportTableModel();
    	 fieldsTable = new TierExportTable(model, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

         JScrollPane fieldsScroll = new JScrollPane(fieldsTable);
         Dimension size = new Dimension(350, 120);
         fieldsScroll.setPreferredSize(size);
         fieldsScroll.setMinimumSize(size);
         
         JPanel fieldsPanel = new JPanel();
         fieldsPanel.setLayout(new GridBagLayout());
         fieldsPanel.setBorder(new TitledBorder(ElanLocale.getString("LexiconResultFields.Title")));

         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = insets;
         getContentPane().add(fieldsPanel, gridBagConstraints);

         buttonPanel = new JPanel();
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridy = 2;
         gridBagConstraints.insets = insets;
         getContentPane().add(buttonPanel, gridBagConstraints);

         Dimension tableDim = new Dimension(100, 100);
         JScrollPane fieldsScrollPane = new JScrollPane(fieldsTable);
         fieldsScroll.setPreferredSize(tableDim);
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = insets;
         gridBagConstraints.fill = GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         fieldsPanel.add(fieldsScrollPane, gridBagConstraints);

         saveButton = new JButton();
         closeButton = new JButton();

         buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));

         buttonPanel.add(saveButton);
         buttonPanel.add(closeButton);

         saveButton.addActionListener(this);
         closeButton.addActionListener(this);
         updateLocale();

    }
    
    
    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();
        setLocationRelativeTo(getParent());
    }

    /**
     * update the locale
     */
    private void updateLocale() {
        saveButton.setText(ElanLocale.getString("Button.Save"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
    }
    
    /**
     * Closes the dialog
     */
    private void closeDialog() {
    	setVisible(false);
		dispose();
	}
    
    /**
     * Fetches all the field names and fills in the model.
     */
    private void getAllFieldNames() {
		if (link == null || link.getSrvcClient() == null) {
			JOptionPane.showMessageDialog(this, ElanLocale.getString("LexiconLink.NoClient"), "Warning",
					JOptionPane.WARNING_MESSAGE);
		} else {
			ArrayList<LexicalEntryFieldIdentification> fldIds;
			try {
				fldIds = link.getSrvcClient().getLexicalEntryFieldIdentifications(link.getLexId());
				Collections.sort(fldIds);
				List<String> stringsPref = Preferences.getListOfString("LexiconResultFields.SelectedFields." + link.getLexId(), null);
				
				for (LexicalEntryFieldIdentification fldId : fldIds) {
					if (stringsPref != null && stringsPref.contains(fldId.getName())) {
						model.addRow(Boolean.TRUE, fldId.getName());
					} else {
						model.addRow(Boolean.FALSE, fldId.getName());
					}
				}
			} catch (LexiconServiceClientException e) {

			}

		}
    }
    
    /**
     * Returns the fields that have been selected in the table.
     *
     * @return a list of the selected fields
     */
    private List<String> getSelectedFields() {
        return model.getSelectedTiers();
    }

    /**
     * invoked when action occurs
     */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == saveButton) {
			saveSelectedFields();
			setVisible(false);
		} else if (e.getSource() == closeButton) {
			closeDialog();
		}
		
	}
	
	/**
	 * saves the selected fields in preference files
	 */
	private void saveSelectedFields() {
		 List<String> selectedFields = getSelectedFields();
		 if (link != null && link.getSrvcClient() != null) {
			 Preferences.set("LexiconResultFields.SelectedFields." + link.getLexId(), selectedFields, null);
		 }
		
	}

}
