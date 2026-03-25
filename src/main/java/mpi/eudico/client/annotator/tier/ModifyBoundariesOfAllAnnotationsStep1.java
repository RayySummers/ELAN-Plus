package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * Panel for step 1 : file and tier selection.
 * 
 * Allows to select multiple files and multiple tiers for modifying the boundaries
 */
@SuppressWarnings("serial")
public class ModifyBoundariesOfAllAnnotationsStep1 extends AbstractFileAndTierSelectionStepPane {

	
	/**
	 * Constructor.
	 * 
	 * @param mp the container multiStepPane
	 * @param transcription the transcription	  
	 */
	public ModifyBoundariesOfAllAnnotationsStep1(MultiStepPane mp, TranscriptionImpl transcription){
		super(mp, transcription, new int[]{-1, Constraint.INCLUDED_IN});
	}
	
	
	@Override
	public String getStepTitle() {
		return ElanLocale.getString("ModifyBoundariesOfAllAnnotations.Title.Step1Title");
	}
	
	
	/**
	 * Updates the button states according to some constraints
	 */
	@Override
	public void updateButtonStates(){
		try{
			SelectableContentTableModel model = (SelectableContentTableModel) tierTable.getModel();
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, model.getSelectedValues().size() > 0);
			multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, false);
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
		}catch(ClassCastException e){
			//if there is no selection model, then no selection is made
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
		}
	}

}
