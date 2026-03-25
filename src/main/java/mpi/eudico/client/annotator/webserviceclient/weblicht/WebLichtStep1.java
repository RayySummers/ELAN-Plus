package mpi.eudico.client.annotator.webserviceclient.weblicht;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.UrlOpener;

/**
 * The first step in contacting a WebLicht service. 
 * Choice to upload plain text or tiers and a choice to upload with a tool
 * chain file to the tool chain web service (for registered users).  
 */
@SuppressWarnings("serial")
public class WebLichtStep1 extends StepPane implements ActionListener {
	private JRadioButton fromScratchRB;
	private JRadioButton uploadTiersRB;
	private JRadioButton toolChainRB;
	private JRadioButton singleToolRB;
	private JButton chainHelpButton;
	
	/**
	 * Creates a new step pane instance.
	 * 
	 * @param multiPane the containing pane
	 */
	public WebLichtStep1(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
	}

    /**
     * Initialize the panel.
     * 
     * @see mpi.eudico.client.annotator.gui.multistep.StepPane#initComponents()
     */
    @Override
	public void initComponents() {
    	setLayout(new GridBagLayout());
    	fromScratchRB = new JRadioButton(ElanLocale.getString("WebServicesDialog.WebLicht.PlainTextInput"), true);
    	uploadTiersRB = new JRadioButton(ElanLocale.getString("WebServicesDialog.WebLicht.TierInput"));
    	ButtonGroup buttonGroup = new ButtonGroup();
    	buttonGroup.add(fromScratchRB);
    	buttonGroup.add(uploadTiersRB);
    	chainHelpButton = new JButton();
    	chainHelpButton.setToolTipText(ElanLocale.getString("Message.Web.OpenInfo"));
		try {
			ImageIcon icon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Information16.gif"));
			chainHelpButton.setIcon(icon);
		} catch (Exception ex) {
			// catch any image loading exception
			chainHelpButton.setText("\u2139");
		}
    	
    	setBorder(new EmptyBorder(5, 10, 5, 10));
    	JLabel label = new JLabel(ElanLocale.getString("WebServicesDialog.WebLicht.Start"));
    	GridBagConstraints gbc = new GridBagConstraints();
    	Insets insets = new Insets(2, 0, 2, 0);
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	gbc.insets = insets;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 1.0;
    	gbc.gridwidth = 2;
    	add(label, gbc);
    	
    	gbc.gridy = 1;
    	gbc.insets = new Insets(2, 20, 2, 0);
    	add(fromScratchRB, gbc);
//    	add(fromScratchB, gbc);
    	gbc.gridy = 2;
    	add(uploadTiersRB, gbc);
//    	add(uploadTiersB, gbc);
    
    	JLabel toolLabel = new JLabel(ElanLocale.getString("WebServicesDialog.WebLicht.UploadInput"));
    	toolChainRB = new JRadioButton(ElanLocale.getString("WebServicesDialog.WebLicht.UploadToToolChain"), true);
    	singleToolRB = new JRadioButton(ElanLocale.getString("WebServicesDialog.WebLicht.UploadToService"));
    	ButtonGroup buttonGr2 = new ButtonGroup();
    	buttonGr2.add(toolChainRB);
    	buttonGr2.add(singleToolRB);
    	
    	gbc.gridy = 3;
    	gbc.insets = new Insets(20, 0, 2, 0);
    	add(toolLabel, gbc);
    	gbc.insets = new Insets(2, 20, 2, 0);
    	gbc.gridy = 5;
    	add(singleToolRB, gbc);
    	
    	gbc.gridy = 4;
    	gbc.gridwidth = 1;
    	add(toolChainRB, gbc);
    	
    	gbc.gridx = 1;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.weightx = 0.0;
    	add(chainHelpButton, gbc);
    	
    	gbc.gridy = 6;
    	gbc.gridx = 0;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weighty = 1.0;
    	add(new JPanel(), gbc);
    	
    	// load prefs
    	String stringPref = Preferences.getString("WebLicht.UploadContents", null);
    	
    	if (stringPref != null) {
    		if ("text".equals(stringPref)) {
    			fromScratchRB.setSelected(true);//
    		} else if ("tier".equals(stringPref)) {
    			uploadTiersRB.setSelected(true);
    		}
    	}
    	
    	stringPref = Preferences.getString("WebLicht.UploadTo", null);
    	if (stringPref != null) {
    		if (stringPref.equals("chain_service")) {
    			toolChainRB.setSelected(true);
    		} else if (stringPref.equals("single_tool")) {
    			singleToolRB.setSelected(true);
    		}
    	}
    	
    	chainHelpButton.addActionListener(this);
    }

    /**
     * Returns the title
     */
	@Override
	public String getStepTitle() {
		return ElanLocale.getString("WebServicesDialog.WebLicht.StepTitle1");
	}

	/**
	 * Store which radio button is selected.
	 * 
	 *  @return true
	 */
	@Override
	public boolean leaveStepForward() {
		if (fromScratchRB.isSelected()) {
			multiPane.putStepProperty("UploadContents", "Plain Text");
			Preferences.set("WebLicht.UploadContents", "text", null);
		} else {
			multiPane.putStepProperty("UploadContents", "Tiers");
			Preferences.set("WebLicht.UploadContents", "tier", null);
		}

		if (toolChainRB.isSelected()) {
			multiPane.putStepProperty("UploadTo", "Toolchain");
			Preferences.set("WebLicht.UploadTo", "chain_service", null);
		} else {
			multiPane.putStepProperty("UploadTo", "Single Tool");
			Preferences.set("WebLicht.UploadTo", "single_tool", null);
		}
		
		return true;
	}

	@Override
	public void enterStepBackward() {
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
	}

	@Override
	public void enterStepForward() {
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
	}

	
	/**
	 * The next step depends on the selected radio button.
	 */
	@Override
	public String getPreferredNextStep() {
		if (fromScratchRB.isSelected()) {
			return "TextStep2";
		} else {
			return "TierStep2";
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String uriString = "https://weblicht.sfs.uni-tuebingen.de";
		try {
			URI uri = new URI(uriString);
			//Desktop.getDesktop().browse(uri);
			UrlOpener.openUrl(uri, true);
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(multiPane, 
				ElanLocale.getString("Message.Web.NoConnection") + ": \n" + 
					t.getMessage(), ElanLocale.getString("Message.Warning"), 
					JOptionPane.WARNING_MESSAGE);
		}
		
	}
    
    
}
