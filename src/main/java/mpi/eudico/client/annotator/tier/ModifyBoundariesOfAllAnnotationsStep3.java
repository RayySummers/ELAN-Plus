package mpi.eudico.client.annotator.tier;

import java.util.List;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * 
 * Panel for Step 3: the final step and actual calculation. A command is created
 * and this pane is connected as progress listener.
 *
 */
@SuppressWarnings("serial")
public class ModifyBoundariesOfAllAnnotationsStep3 extends AbstractProgressStepPane {

	/** list of files */
	protected List<String> files;
	/** list of selected tiers */
	protected List<String> selectedTiers;

	private Long beginTime = -1L;
	private Long endTime = -1L;

	private Boolean overrideLeftAnnotation = false;

	private Boolean overrideRightAnnotation = false;

	Transcription transcription;

	int processMode;

	/**
	 * Constructor.
	 *
	 * @param multiPane     the container pane
	 * @param transcription the transcription
	 */
	public ModifyBoundariesOfAllAnnotationsStep3(MultiStepPane multiPane, Transcription transcription) {
		this(multiPane, transcription, false);
	}

	/**
	 * Constructor.
	 *
	 * @param multiPane         the container pane
	 * @param transcription     the transcription
	 * @param subtractionDialog if {@code true} the dialog is for the subtraction
	 *                          process
	 */
	public ModifyBoundariesOfAllAnnotationsStep3(MultiStepPane multiPane, Transcription transcription,
			boolean subtractionDialog) {
		super(multiPane);
		this.transcription = transcription;
	}

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("ModifyAllAnnotationlengths.Action");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void enterStepForward() {

		files = (List<String>) multiPane.getStepProperty("OpenedFiles");
		selectedTiers = (List<String>) multiPane.getStepProperty("SelectedTiers");
		beginTime = (Long) multiPane.getStepProperty("beginTime");
		endTime = (Long) multiPane.getStepProperty("endTime");
		overrideLeftAnnotation = (Boolean) multiPane.getStepProperty("overrideLeftAnnotation");
		overrideRightAnnotation = (Boolean) multiPane.getStepProperty("overrideRightAnnotation");

		Integer procMode = (Integer) multiPane.getStepProperty("ProcessMode");
		if (procMode != null) {
			processMode = procMode;
		}

		super.enterStepForward();

	}

	@Override
	public void startProcess() {
		String sourceTiers[] = new String[selectedTiers.size()];
		for (int i = 0; i < selectedTiers.size(); i++) {
			sourceTiers[i] = selectedTiers.get(i);
		}

		Object[] fileNames = null;
		if (files != null) {
			fileNames = files.toArray();
		}

		Object args[] = null;
		ModifyAllAnnotationBoundaries command = null;

		args = new Object[] { sourceTiers, fileNames, beginTime, endTime, overrideLeftAnnotation,
				overrideRightAnnotation, processMode };

		command = new ModifyAllAnnotationBoundaries(ELANCommandFactory.MODIFY_BOUNDARIES_ALL_ANNOS_CMD);

		command.addProgressListener(this);
		command.execute(transcription, args);

	}

}
