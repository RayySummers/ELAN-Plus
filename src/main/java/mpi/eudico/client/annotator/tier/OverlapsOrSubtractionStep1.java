package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Panel for step 1: file and tier selection.
 * 
 * Allows to select multiple files and multiple tiers on which the overlaps or 
 * subtractions are to be computed
 * 
 * @author aarsom
 * @version November, 2011
 */
@SuppressWarnings("serial")
public class OverlapsOrSubtractionStep1 extends AbstractFileAndTierSelectionStepPane{	
	boolean subtractionDialog;
	
	/**
	 * Constructor.
	 * 
	 * @param mp the container multiStepPane
	 * @param transcription the transcription	  
	 */
	public OverlapsOrSubtractionStep1(MultiStepPane mp, TranscriptionImpl transcription){
		this(mp, transcription , false);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param mp the container multiStepPane
	 * @param transcription the transcription
	 * @param subtractionDialog if {@code true} the operation is subtraction,
	 * otherwise it is annotations from overlaps
	 */
	public OverlapsOrSubtractionStep1(MultiStepPane mp, TranscriptionImpl transcription, boolean subtractionDialog){
		super(mp, transcription);
		this.subtractionDialog = subtractionDialog;
	}

	@Override
	public String getStepTitle(){		
		return ElanLocale.getString("OverlapsDialog.Title.Step1Title");
	}	
	
}