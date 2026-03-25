package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
* Panel for step 3 of merging tiers: destination tier specification panel.
* 
* This panel allows to specify the attributes like tier name and type
* for the new destination tier.
*
* @author aarsom
* @version Feb, 2014
*/
@SuppressWarnings("serial")
public class MergeMultiTiersStep3 extends AbstractDestTierAndTypeSpecStepPane{

	/**
	 * Constructor.
	 * 
	 * @param mp the container pane
	 * @param trans the transcription in case of single document operation
	 */
	public MergeMultiTiersStep3(MultiStepPane mp, TranscriptionImpl trans) {
		super(mp, trans);
	}

	@Override
	public String getStepTitle(){	
		return ElanLocale.getString("MergeTiers.Title.Step3");
	}
	
	@Override
	public void enterStepForward(){		
		super.enterStepForward();
		parentTierCB.setEnabled(false);
		childTierRB.setEnabled(false);
	}
}
