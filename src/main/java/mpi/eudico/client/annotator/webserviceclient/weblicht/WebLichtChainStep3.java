package mpi.eudico.client.annotator.webserviceclient.weblicht;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.UrlOpener;
import nl.mpi.util.FileExtension;

/**
 * A step that allows to select a downloaded tool chain file (.xml) and
 * to paste an access key obtained from and required for {@code WaaS 
 * (WebLicht as a Service)}.
 * 
 * @version Mar 2022
 */
@SuppressWarnings("serial")
public class WebLichtChainStep3 extends StepPane {
	private JLabel sentenceDurationLabel;
	private JTextField sentenceDurationTF;
	
	private JLabel chainFileLabel;
	private JTextField chainFileTF;
	private JButton chainBrowseButton;
	private JLabel accKeyLabel;
	private JTextField accKeyTF;
	private JButton waasHelpButton;
	private JLabel inputLabel;
	private JRadioButton plainTextRB;
	private JRadioButton tcfRB;
	private boolean tcfFormatPref = false;
	
	/**
	 * Constructor.
	 * 
	 * @param multiPane the host panel
	 */
	public WebLichtChainStep3(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
	}

	@Override
	protected void initComponents() {		
		super.initComponents();
		setLayout(new GridBagLayout());
		setBorder(new EmptyBorder(5, 10, 5, 10));
		
		chainFileLabel = new JLabel(ElanLocale.getString("WebServicesDialog.WebLicht.SelectChain"));
		chainFileTF = new JTextField();
		chainBrowseButton = new JButton(ElanLocale.getString("Button.Select"));
		chainBrowseButton.addActionListener(null);
		inputLabel = new JLabel(ElanLocale.getString("WebServicesDialog.WebLicht.InputFormat"));
		plainTextRB = new JRadioButton("text/plain", true);
		tcfRB = new JRadioButton("TCF (text/tcf+xml)");
		ButtonGroup bg = new ButtonGroup();
		bg.add(plainTextRB);
		bg.add(tcfRB);
		accKeyLabel = new JLabel(ElanLocale.getString("WebServicesDialog.WebLicht.AccessKey"));
		accKeyTF = new JTextField();
		waasHelpButton = new JButton();
		waasHelpButton.setToolTipText(ElanLocale.getString("Message.Web.OpenInfo"));
		waasHelpButton.setBorder(null);
				
		try {
			ImageIcon icon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Information16.gif"));
			waasHelpButton.setIcon(icon);
		} catch (Exception ex) {
			// catch any image loading exception
			waasHelpButton.setText("\u2139");
		}
		
		sentenceDurationLabel = new JLabel (ElanLocale.getString("WebServicesDialog.WebLicht.Duration"));
		sentenceDurationTF = new JTextField(12);
		sentenceDurationTF.setText("3000");
		AllHandler ah = new AllHandler();
		
    	Insets insets = new Insets(2, 0, 2, 0);
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.insets = insets;
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 1.0;
    	gbc.gridwidth = 2;
    	add(chainFileLabel, gbc);
    	
    	gbc.gridy = 1;
    	gbc.gridwidth = 1;  	
    	gbc.anchor = GridBagConstraints.WEST;
    	add(chainFileTF, gbc);
    	
    	gbc.gridx = 1;
    	//gbc.fill = GridBagConstraints.NONE;
    	gbc.insets = new Insets(2, 4, 2, 0);
    	gbc.weightx = 0.0;
    	add(chainBrowseButton, gbc);
    	
    	gbc.insets = new Insets(20, 0, 2, 0);
    	gbc.gridx = 0;
    	gbc.gridy = 2;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 1.0;
    	add(inputLabel, gbc);
    	
    	JPanel buttonPanel = new JPanel(new GridBagLayout());
    	GridBagConstraints rbc = new GridBagConstraints();
    	rbc.anchor = GridBagConstraints.WEST;
    	buttonPanel.add(plainTextRB, rbc);
    	rbc.fill = GridBagConstraints.HORIZONTAL;
    	rbc.weightx = 1.0;
    	rbc.insets = new Insets(0, 4, 0, 0);
    	buttonPanel.add(tcfRB, rbc);
    	
    	gbc.insets = new Insets(2, 15, 2, 0);
    	gbc.gridy = 3;
    	add(buttonPanel, gbc);
    	
    	gbc.insets = new Insets(20, 0, 2, 0);
    	gbc.gridx = 0;
    	gbc.gridy = 4;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 1.0;
    	add(accKeyLabel, gbc);
    	  	
    	gbc.insets = insets;
    	gbc.gridy = 5;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.anchor = GridBagConstraints.WEST;
    	gbc.weightx = 1.0;
    	add(accKeyTF, gbc);
    	
    	gbc.gridx = 1;
    	gbc.anchor = GridBagConstraints.CENTER;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.weightx = 0.0;
    	add(waasHelpButton, gbc);
    	
    	gbc.gridx = 0;
    	gbc.gridy = 6;
    	gbc.insets = new Insets(20, 0, 2, 0);
    	gbc.anchor = GridBagConstraints.WEST;
    	gbc.weightx = 0.0;
    	gbc.weighty = 0.0;
    	gbc.fill = GridBagConstraints.NONE;
    	add(sentenceDurationLabel, gbc);
    	
    	gbc.gridx = 1;
    	gbc.insets = new Insets(20, 4, 2, 0);
    	gbc.weighty = 1.0;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	add(sentenceDurationTF, gbc);
    	
    	// force items to the top
    	gbc.gridy++;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weighty = 1.0;
    	add(new JPanel(), gbc);
    	// check stored preferences	
    	Integer val = Preferences.getInt("WebLicht.SentenceDuration", null);
    	
    	if (val != null) {
    		sentenceDurationTF.setText(String.valueOf(val));
    	}
    	String path = Preferences.getString("WebLicht.ToolchainFile", null);
    	if (path != null) {
    		chainFileTF.setText(path);
    	}
    	String formatPref = Preferences.getString("WebLicht.Toolchain.InputFormat", null);
    	if (formatPref != null) {
    		tcfFormatPref = formatPref.equals("TCF");
    	}
    	//
    	chainBrowseButton.addActionListener(ah);
    	chainFileTF.addKeyListener(ah);
    	chainFileTF.getDocument().addDocumentListener(ah);
    	accKeyTF.addKeyListener(ah);
    	accKeyTF.getDocument().addDocumentListener(ah);
    	waasHelpButton.addActionListener(ah);
    	
	}

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("WebServicesDialog.WebLicht.StepTitle3c");
	}

	@Override
	public void enterStepForward() {
		String uploadType = (String) multiPane.getStepProperty("UploadContents");
		boolean tierMode = "Tiers".equals(uploadType);
		sentenceDurationLabel.setVisible(!tierMode);
		sentenceDurationTF.setVisible(!tierMode);
		
		plainTextRB.setEnabled(tierMode);
		tcfRB.setEnabled(tierMode);
		if (tierMode && tcfFormatPref) {
			tcfRB.setSelected(true);
		} else {
			plainTextRB.setSelected(true);
		}

		if (!chainFileTF.getText().isBlank() && !accKeyTF.getText().isBlank()) {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
		} else {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
		}
	}

	@Override
	public boolean leaveStepForward() {
		int duration = 3000;
		String durInput = sentenceDurationTF.getText();
		if (durInput != null) {
			try {
				duration = Integer.parseInt(durInput);
			} catch (NumberFormatException nfe) {
				
			}
		}
		multiPane.putStepProperty("SentenceDuration", duration);
		// store preferences
		if (duration != 3000 || Preferences.getInt("WebLicht.SentenceDuration", null) != null) {
			Preferences.set("WebLicht.SentenceDuration", duration, null);
		}
		
		// there should be an existing file selected and an access key needs to be provided
		String filePath = chainFileTF.getText().trim();
		try {
			File f = new File(filePath);
			if (!f.exists() || !f.canRead() || f.isDirectory()) {
				chainFileTF.setForeground(Color.RED);
				chainFileTF.requestFocus();
				return false;
			}
		} catch (Throwable t) {
			return false;
		}
		multiPane.putStepProperty("ToolchainFile", filePath);
		Preferences.set("WebLicht.ToolchainFile", filePath, null);
		
		if (tcfRB.isEnabled() && tcfRB.isSelected()) {
			multiPane.putStepProperty("ToolchainInput", "TCF");
			Preferences.set("WebLicht.Toolchain.InputFormat", "TCF", null);
		} else {
			multiPane.putStepProperty("ToolchainInput", "text/plain");
			Preferences.set("WebLicht.Toolchain.InputFormat", null, null);
		}
		
		String accKey = accKeyTF.getText().trim();
		if (accKey.isBlank()) {
			accKeyTF.requestFocus();
			return false;
		}
		multiPane.putStepProperty("ToolchainKey", accKey);
		// better not store key in the preferences? Or encrypted?
		// explicitly set the single service to null
		multiPane.putStepProperty("WLServiceDescriptor", null);
		return true;
	}

	@Override
	public String getPreferredNextStep() {
		String uploadType = (String) multiPane.getStepProperty("UploadContents");
		if ("Tiers".equals(uploadType)) {
			return "TierStep4";
		} else {
			return "TextStep4";
		}
	}

	@Override
	public String getPreferredPreviousStep() {
		String uploadType = (String) multiPane.getStepProperty("UploadContents");
		if ("Tiers".equals(uploadType)) {
			return "TierStep2";
		} else {
			return "TextStep2";
		}
	}
	
	/**
     * Delegates to {@link #leaveStepForward()}; Next and Finish both move to the 
     * Progress monitoring step in which the real work is done and monitored.  
     * 
     * @return {@link #leaveStepForward()}
     */
    @Override
	public boolean doFinish() {
    	
    	if (leaveStepForward()) {
    		multiPane.nextStep();
    		return false;
    	}
    	return false;
    }

    /**
     * Event handler class.
     *
     */
	class AllHandler implements ActionListener, KeyListener, DocumentListener {

		@Override
		public void keyTyped(KeyEvent e) {
			checkTextFields();
		}

		@Override
		public void keyPressed(KeyEvent e) {
			checkTextFields();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			checkTextFields();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == chainBrowseButton) {
				// browse to a chain file
				FileChooser chooser = new FileChooser(multiPane);
				chooser.createAndShowFileDialog(ElanLocale.getString(
						"WebServicesDialog.WebLicht.SelectChain"), 
						FileChooser.OPEN_DIALOG, 
						ElanLocale.getString("Button.Select"), null, FileExtension.XML_EXT,  
						false, "WebLichtChainFile", FileChooser.FILES_ONLY, null);
				File f = chooser.getSelectedFile();
				if (f != null) {
					chainFileTF.setText(f.getAbsolutePath());
					chainFileTF.setForeground(accKeyTF.getForeground());
				}
			} else if (e.getSource() == waasHelpButton) {
				String uriString = "https://weblicht.sfs.uni-tuebingen.de/WaaS/";
				try {
					URI r = new URI(uriString);
					//Desktop.getDesktop().browse(r);
					UrlOpener.openUrl(r, true);
				} catch (Throwable t) {
					JOptionPane.showMessageDialog(multiPane, 
							ElanLocale.getString("Message.Web.NoConnection") + ": \n" + 
								t.getMessage(), ElanLocale.getString("Message.Warning"), 
								JOptionPane.WARNING_MESSAGE);		
				}
			}
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			checkTextFields();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			checkTextFields();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			checkTextFields();
		}
		
		private void checkTextFields() {
			String chFile = chainFileTF.getText().trim();
			String key = accKeyTF.getText().trim();
			if (!chFile.isBlank() && !key.isBlank()) {
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
				multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
			} else {
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
				multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
			}
		}
	}
}
