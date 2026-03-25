package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Panel for step 1 of merging tiers: file and tier selection.
 * 
 * Allows to select multiple files and tiers which have to be merged.
 * 
 * @author aarsom
 * @version Feb, 2014
 */
@SuppressWarnings("serial")
public class MergeMultiTiersStep1 extends AbstractFileAndTierSelectionStepPane{	
		
	/**
	 * Constructor.
	 * 
	 * @param mp the container multiStepPane
	 * @param transcription the transcription in case of single document operation	  
	 */
	public MergeMultiTiersStep1(MultiStepPane mp, TranscriptionImpl transcription){
		super(mp, transcription);
	}

	@Override
	public String getStepTitle(){	
		return ElanLocale.getString("MergeTiers.Title.Step1");
	}	
	
}
