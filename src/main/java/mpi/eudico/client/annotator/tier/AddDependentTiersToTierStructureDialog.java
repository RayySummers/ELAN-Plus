package mpi.eudico.client.annotator.tier;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.prefs.gui.RecentLanguagesBox;
import mpi.eudico.client.im.ImUtil;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * A dialog to create the new dependent tiers to the tier structure
 * based on the participant, and prefix/suffix value 
 *
 */
@SuppressWarnings("serial")
public class AddDependentTiersToTierStructureDialog extends ClosableDialog implements ActionListener ,
				FocusListener, ItemListener, ChangeListener ,TableModelListener {

	private TranscriptionImpl transcription;
	private TierExportTableModel model;
	private JTable participantTable;
	private JLabel prefixSuffixLabel;
	private JRadioButton prefixRB;
	private JRadioButton suffixRB;
	private JPanel optionsPanel;
	private JLabel prefixSuffixValueLabel;    
	private JTextField prefixOrSuffixText;
	private JLabel separatorLabel;
	private JComboBox<String> separatorComboBox;
	private JPanel buttonPanel;
	private JLabel annotatorLabel;
	private JTextField annotatorTextField;
	private JLabel lingTypeLabel;
	private JComboBox<String> lingTypeComboBox;
	private JLabel parentLabel;
	private JTextField parentTextField;
	private JLabel languageLabel;
	private JComboBox<String> languageComboBox;
	private JLabel mlLanguageLabel;
	private RecentLanguagesBox mlLanguageBox;
	private JButton closeButton;
    private JButton okButton;
    private JLabel exampleLabel;
    
    private Locale[] langs;
	
	private boolean close = false;
	
	private String[] specialCharacters = { "_", "@", "#", "-", "$","&", "!" };


	/**
	 * Creates the AddDependentTiersToTierStructureDialog instance
	 * @param transcription the transcription that hold the tiers
	 * @param frame the parent frame
	 */
	public AddDependentTiersToTierStructureDialog(TranscriptionImpl transcription, Frame frame) {
		super(frame);
		this.transcription = transcription;
		initComponents();
		extractParticipants();
		fillUIComponents();
		if (close) {
			closeDialog(null);
		} else {
			postInit();
			setVisible(true);
		}
	}

	private void initComponents() {
		langs = ImUtil.getLanguages(this);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeDialog(evt);
			}
		});

		prefixSuffixLabel = new JLabel();
		prefixRB = new JRadioButton();
		suffixRB = new JRadioButton();

		prefixSuffixValueLabel = new JLabel();
		prefixOrSuffixText = new JTextField();
		
		separatorLabel = new JLabel();
		separatorComboBox = new JComboBox<String>(specialCharacters);
		String none = ElanLocale.getString("EditTierDialog.Label.None");
		separatorComboBox.insertItemAt(none, 0);
		separatorComboBox.setSelectedIndex(0);
		
		String custom = ElanLocale.getString("AddDependentTierToTierStructureDlg.CustomSeparator");
		separatorComboBox.insertItemAt(custom, separatorComboBox.getItemCount());
		

		okButton = new JButton();
		closeButton = new JButton();

		optionsPanel = new JPanel();
		optionsPanel.setLayout(new GridBagLayout());

		buttonPanel = new JPanel();

		annotatorLabel = new JLabel();
		annotatorTextField = new JTextField();
		lingTypeLabel = new JLabel();
		lingTypeComboBox = new JComboBox<String>();
		
		try {
			ImageIcon icon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/TipOfTheDay16.gif"));
			parentLabel = new JLabel(icon);
			parentLabel.setHorizontalTextPosition(JLabel.LEFT);
		} catch(Exception ex) {
			parentLabel = new JLabel();
		}
		parentTextField = new JTextField();
		languageLabel = new JLabel();
		languageComboBox = new JComboBox<String>();
		mlLanguageLabel = new JLabel();
		mlLanguageBox = new RecentLanguagesBox(null);
		mlLanguageBox.addNoLanguageItem();
		
		exampleLabel = new JLabel();
        exampleLabel.setFont(exampleLabel.getFont().deriveFont(Font.ITALIC));
        exampleLabel.setForeground(Constants.ACTIVEANNOTATIONCOLOR);
        exampleLabel.setBorder(new LineBorder(Constants.ACTIVEANNOTATIONCOLOR));

		ButtonGroup group = new ButtonGroup();
		group.add(prefixRB);
		group.add(suffixRB);

		getContentPane().setLayout(new GridBagLayout());
		getContentPane().setPreferredSize(new Dimension(700, 500));
		Insets insets = new Insets(2, 6, 2, 6);
		GridBagConstraints gridBagConstraints;

		model = new TierExportTableModel();
		participantTable = new TierExportTable(model, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JScrollPane participantScroll = new JScrollPane(participantTable);
		Dimension size = new Dimension(350, 120);
		participantScroll.setPreferredSize(size);
		participantScroll.setMinimumSize(size);

		setModal(true);

		JPanel participantPanel = new JPanel();
		participantPanel.setLayout(new GridBagLayout());
		participantPanel.setBorder(new TitledBorder(ElanLocale.getString("DeleteParticipantDialog.SelectParticipants")));

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = insets;
		getContentPane().add(participantPanel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		insets.bottom = 3;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = insets;
		getContentPane().add(optionsPanel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 3;
		gridBagConstraints.insets = insets;
		getContentPane().add(buttonPanel, gridBagConstraints);

		Dimension tableDim = new Dimension(100, 100);
		JScrollPane participantScrollPane = new JScrollPane(participantTable);
		participantScroll.setPreferredSize(tableDim);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		participantPanel.add(participantScrollPane, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		optionsPanel.add(prefixSuffixLabel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(4, 10, 4, 6);
		optionsPanel.add(prefixRB, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(4, 5, 4, 6);
		optionsPanel.add(suffixRB, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		optionsPanel.add(prefixSuffixValueLabel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		optionsPanel.add(prefixOrSuffixText, gridBagConstraints);
		
		//for separator
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		optionsPanel.add(separatorLabel, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		optionsPanel.add(separatorComboBox, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		optionsPanel.add(parentLabel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		optionsPanel.add(parentTextField, gridBagConstraints);
		

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 6;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		optionsPanel.add(annotatorLabel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		optionsPanel.add(annotatorTextField, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 7;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		optionsPanel.add(lingTypeLabel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 7;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		optionsPanel.add(lingTypeComboBox, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 8;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		optionsPanel.add(languageLabel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 8;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		optionsPanel.add(languageComboBox, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 9;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		optionsPanel.add(mlLanguageLabel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 9;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		optionsPanel.add(mlLanguageBox, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.insets = insets;
        optionsPanel.add(exampleLabel, gridBagConstraints);

		buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));
		okButton.addActionListener(this);
		buttonPanel.add(okButton);
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);

		parentTextField.addFocusListener(this);
		prefixRB.setSelected(true); 
		
		prefixRB.addChangeListener(this);
		suffixRB.addChangeListener(this);
		
		KeyAdapter keyList = new KeyAdapter() {
            @Override
			public void keyReleased(KeyEvent ke) {
                updateExample();
            }
        };
		prefixOrSuffixText.addKeyListener(keyList);
		parentTextField.addKeyListener(keyList);
		
		separatorComboBox.addItemListener(this);
		
		model.addTableModelListener(this);
		
		updateLocale();
	}
	
	private void fillUIComponents() {
		updateLanguageComboBox();
	}

	/**
	 * Closes the dialog
	 *
	 * @param evt the window closing event
	 */
	private void closeDialog(WindowEvent evt) {
		setVisible(false);
		dispose();
	}

	/**
     * Extract all participants and fill the table.
     */
    private void extractParticipants() {
    	 if (transcription != null) {
         	while(participantTable.getRowCount() > 0){
         		model.removeRow(0);
         	} 
         	
         	List<TierImpl> tiers = transcription.getTiers();
         	List<String> participants = new ArrayList<String>();
         	
         	TierImpl tier;

    		for (int i = 0; i < tiers.size(); i++) {
    			tier = tiers.get(i);
    			String participant = tier.getParticipant();
    			if(participant != null && participant.trim().length() != 0 && !participants.contains(participant)){
    				participants.add(participant);
    			}
    		}
    		
    		if(participants.size() > 0){
    			for(int i=0; i< participants.size(); i++){
    				model.addRow(Boolean.TRUE, participants.get(i)); 				
    			} 
    			return;
    		} 
    	}
  
    }
    
    
	private void updateLocale() {
		setTitle(ElanLocale.getString("AddDependentTierToTierStructureDlg.Title"));
		optionsPanel.setBorder(new TitledBorder(ElanLocale.getString("AddDependentTierToTierStructureDlg.Options"))); 
		prefixSuffixLabel.setText(ElanLocale.getString("AddDependentTierToTierStructureDlg.Label.PrefixSuffix"));
		prefixRB.setText(ElanLocale.getString("AddParticipantDlg.RB.Prefix"));
		suffixRB.setText(ElanLocale.getString("AddParticipantDlg.RB.Suffix"));
		prefixSuffixValueLabel.setText((ElanLocale.getString("AddDependentTierToTierStructureDlg.PrefixSuffixValueLabel")));
		separatorLabel.setText(ElanLocale.getString("AddDependentTierToTierStructureDlg.SeparatorLabel"));

		annotatorLabel.setText(ElanLocale.getString("EditTierDialog.Label.Annotator"));
		lingTypeLabel.setText(ElanLocale.getString("EditTierDialog.Label.LinguisticType"));
		parentLabel.setText(ElanLocale.getString("AddDependentTierToTierStructureDlg.Label.Parent"));
		parentLabel.setToolTipText(ElanLocale.getString("AddDependentTierToTierStructureDlg.ToolTipText"));
		languageLabel.setText(ElanLocale.getString("EditTierDialog.Label.Language"));
		mlLanguageLabel.setText(ElanLocale.getString("EditTierDialog.Label.ContentLanguage"));
		
		okButton.setText(ElanLocale.getString("Button.OK"));
	    closeButton.setText(ElanLocale.getString("Button.Close"));   
	}

	/**
	 * Pack, size and set location.
	 */
	private void postInit() {
		pack();
		setLocationRelativeTo(getParent());		
	}
	
	private void updateLanguageComboBox() {
		if (languageComboBox.getItemCount() == 0) {
			if (langs != null) {
				for (int i = 0; i < langs.length; i++) {
					if (i == 0 && langs[i] == Locale.getDefault()) {
						languageComboBox.addItem(langs[i].getDisplayName() + " (System default)");
					} else {
						languageComboBox.addItem(langs[i].getDisplayName());
					}
				}
			}

			String none = ElanLocale.getString("EditTierDialog.Label.None");
			languageComboBox.insertItemAt(none, 0);
			languageComboBox.setSelectedIndex(0);
		}

	}
	
	
	/**
     * Returns the tiers that have been selected in the table.
     *
     * @return a list of the selected tiers
     */
    private List<String> getSelectedParticipants() {
    	return model.getSelectedTiers();
    }
	
    
    /**
     * The action performed event handling.
     *
     * @param e the action event
     */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			startOperation();
		} else if (e.getSource() == closeButton) {
			setVisible(false);
			dispose();
		}
	}
	
	/**
	 * shows the example tier name in the dialog
	 */
	private void updateExample() {
		
		String exampleText = " ";
		
		List<String> selectedParticipants = getSelectedParticipants();

		String newValue = prefixOrSuffixText.getText();
		String separator = (String) separatorComboBox.getSelectedItem();

		if (separatorComboBox.getSelectedIndex() == 0) {
			separator = "";
		}

		String parentTierName = parentTextField.getText();
		String participantName = "" ;
		for(String participant : selectedParticipants) {
			if(parentTierName.contains(participant)) {
				participantName = participant;
			}
		}
		
		if(prefixRB.isSelected()) {
			exampleText = newValue + separator + participantName;
		}else {
			exampleText = participantName + separator + newValue;
		}
		exampleLabel.setText(exampleText);
	}
	
	/**
	 * Validates the input data and initiates the start process 
	 */
	private void startOperation() {
		 List<String> selectedParticipants = getSelectedParticipants();
		 
		 if (selectedParticipants.size() == 0 ) {
			 JOptionPane.showMessageDialog(this, ElanLocale.getString("AddDependentTierToTierStructureDlg.Warning.NoParticipant"),
                     ElanLocale.getString("Message.Error"), JOptionPane.WARNING_MESSAGE);
			 
			 return;
		 }
		 
		 String newValue = prefixOrSuffixText.getText();
	        if(newValue == null || newValue.trim().length() <= 0){
	        	JOptionPane.showMessageDialog(this,
	                    ElanLocale.getString("AddDependentTierToTierStructureDlg.Warning.NewValue"),
	                    ElanLocale.getString("Message.Error"),
	                    JOptionPane.WARNING_MESSAGE);
	            return;
	        }
	        
	     
		String separator = (String) separatorComboBox.getSelectedItem();
		
		if( separatorComboBox.getSelectedIndex() == 0) {
			separator = "";
		}
	    
	       
	    String parentTierName = parentTextField.getText();
	    if(parentTierName == null || parentTierName.trim().length() <=0 ) {
	    	JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("AddDependentTierToTierStructureDlg.Warning.EmptyParent"),
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.WARNING_MESSAGE);
            return;
	    }
	    Tier parentTier = transcription.getTierWithId(parentTierName);
	    if(parentTier == null) {
	    	JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("AddDependentTierToTierStructureDlg.Warning.ParentTier"),
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.WARNING_MESSAGE);
            return;
	    }
	    
	    String annotator = annotatorTextField.getText();
	    String lingType = (String) lingTypeComboBox.getSelectedItem();
	    
	    if(lingType == null || lingType.isEmpty()) {
	    	JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("AddDependentTierToTierStructureDlg.Warning.LingType"),
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.WARNING_MESSAGE);
            return;
	    }
	    
	    String localeName = (String) languageComboBox.getSelectedItem();
        Locale locale = null;
        
        
        if (languageComboBox.getSelectedIndex() == 0) {
        	// locale = null
        } else if (languageComboBox.getSelectedIndex() == 1 && localeName.indexOf("(System default)") > -1) {
        	locale = Locale.getDefault();
        } else {               
            if (langs != null) {
                for (Locale lang : langs) {
                    if (lang.getDisplayName().equals(localeName)) {
                        locale = lang;

                        break;
                    }
                }
            }
        }
        
        String mlLanguage = mlLanguageBox.getId();
        
        Object[] args = new Object[] { selectedParticipants, prefixRB.isSelected(), newValue, parentTier, annotator, lingType, locale, mlLanguage,separator };
      	 	Command command = ELANCommandFactory.createCommand(transcription,
      			 ELANCommandFactory.ADD_DEPENDENT_TIERS_TO_TIER_STRUCTURE_CMD);
      	 	command.execute(transcription, args);
		 
	}

	@Override
	public void focusGained(FocusEvent e) {
		
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (e.getSource() == parentTextField) {
			String parentTierName = parentTextField.getText();
			TierImpl parentTier = (transcription.getTierWithId(parentTierName));

			if (parentTier == null) {
				JOptionPane.showMessageDialog(this,
						ElanLocale.getString("AddDependentTierToTierStructureDlg.Warning.ParentTier"),
						ElanLocale.getString("Message.Error"), JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			fillLingTypeMenu(parentTier);

		}
	}
	
	 private void fillLingTypeMenu(TierImpl parentTier) {
		 lingTypeComboBox.removeAllItems();
		 boolean excludeTimeAlignable = false;
		 
		 Constraint parentConstraint = null;
		 
		 if (parentTier != null) {
	            parentConstraint = parentTier.getLinguisticType().getConstraints();

	            if (parentConstraint != null) {
	                if ((parentConstraint.getStereoType() == Constraint.SYMBOLIC_SUBDIVISION) ||
	                        (parentConstraint.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)) {
	                    excludeTimeAlignable = true;
	                }
	            }
	        }
		 
		 for (LinguisticType lt : transcription.getLinguisticTypes()) {
	            String ltName = lt.getLinguisticTypeName();

	            if (parentTier == null) { // only unconstrained types

	                if (lt.getConstraints() != null) {
	                    continue;
	                }
	            }
	            
	            if (excludeTimeAlignable && (lt.getConstraints() != null) &&
	                    ((lt.getConstraints().getStereoType() == Constraint.TIME_SUBDIVISION) || 
	                            lt.getConstraints().getStereoType() == Constraint.INCLUDED_IN)) {
	                continue;
	            }

	            if (parentTier != null) { // only constrained types

	                if (lt.getConstraints() == null) {
	                    continue;
	                }
	            } 
	            
	            lingTypeComboBox.addItem(ltName);
	        }
		 
			if (lingTypeComboBox.getModel().getSize() <= 0) {
				okButton.setEnabled(false);
			} else {
				okButton.setEnabled(true);
			}

	 }

	 
	@Override
	public void itemStateChanged(ItemEvent e) {
		if(separatorComboBox.getSelectedItem().equals(ElanLocale.getString("AddDependentTierToTierStructureDlg.CustomSeparator"))){
			separatorComboBox.setEditable(true);
			separatorComboBox.setSelectedItem("");
		}
		updateExample();
	}

	
	@Override
	public void stateChanged(ChangeEvent e) {
		updateExample();
	}

	
	@Override
	public void tableChanged(TableModelEvent e) {
		updateExample();
	}
	

}
