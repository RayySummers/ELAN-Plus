package mpi.eudico.client.annotator.webserviceclient.weblicht;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.WebLichtTextBasedCommand;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.webserviceclient.weblicht.WLServiceDescriptor;

/**
 * The final step in uploading text to WebLicht and converting the result to a
 * transcription.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class WebLichtStep4 extends ProgressStepPane {
	private Transcription trans;

	// some fields for the settings resulting from the previous steps
	// necessary because the interaction takes place in a separate thread
	private String inputText;
	private Integer sentenceDuration;
	private WLServiceDescriptor wlDescriptor;

	private WebLichtTextBasedCommand wltCommand;
		
	/**
	 * Constructor
	 * @param multiPane the container pane
	 */
	public WebLichtStep4(MultiStepPane multiPane) {
		super(multiPane);
		initComponents();
	}

	
	@Override
	protected void initComponents() {
		super.initComponents();
		progressLabel.setText("");
	}

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("WebServicesDialog.WebLicht.Uploading");
	}

	@Override
	protected void endOfProcess() {
		if (wltCommand != null) {
			wltCommand.removeProgressListener(this);
		}
		
		if (completed) {
			// closes the window
			super.endOfProcess();
		} else {
			progressBar.setValue(0);
			multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
			multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
		}
	}

	@Override
	public void enterStepForward() {
		if (progressLabel != null) {
			progressLabel.setText("");
		}
		progressBar.setValue(0);
		
		doFinish();
	}
	
	/**
	 * Collects information from the pane and creates and executes a command.
	 * 
	 * @return {@code false}
	 */
	@Override
	public boolean doFinish() {
		// everything has been set here. Start processing
		multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);
		
		// get all info from the properties, upload text in a separate thread
		inputText = (String) multiPane.getStepProperty("InputText");
		sentenceDuration = (Integer) multiPane.getStepProperty("SentenceDuration");
		wlDescriptor = (WLServiceDescriptor) multiPane.getStepProperty("WLTokenizerDescriptor");
		
		String uploadTo = (String) multiPane.getStepProperty("UploadTo");
		String chainFile = (String) multiPane.getStepProperty("ToolchainFile");
		String accKey = (String) multiPane.getStepProperty("ToolchainKey");
		
		trans = (Transcription) multiPane.getStepProperty("transcription");
		Object[] args = null;
		
		if ("Toolchain".equals(uploadTo)) {
			args = new Object[]{inputText, sentenceDuration, chainFile, accKey};
		} else {
			args = new Object[]{inputText, sentenceDuration, wlDescriptor};
		}		
		
		wltCommand = new WebLichtTextBasedCommand("WebLichtTextBased");
		wltCommand.addProgressListener(this);
		wltCommand.execute(trans, args);
		
//		multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
		return false;
	}

}
