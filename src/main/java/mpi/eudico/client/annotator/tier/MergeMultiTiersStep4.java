package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

/**
 * Panel for step 4 of merging tiers: specification of the value of the 
 * resulting merged annotations. 
 * 
 * @author aarsom
 * @version Feb, 2014
 */
@SuppressWarnings("serial")
public class MergeMultiTiersStep4 extends AbstractDestTierAnnValueSpecStepPane{

	/**
	 * Constructor.
	 * 
	 * @param mp the container pane
	 */
	public MergeMultiTiersStep4(MultiStepPane mp) {
		super(mp);
	}
	
	@Override
	public String getStepTitle(){	
		return ElanLocale.getString("MergeTiers.Title.Step4");
	}
	
	@Override
	public void enterStepForward(){		
		boolean overlapOnly = (Boolean)multiPane.getStepProperty("OnlyProcessOverlapingAnnotations");
		tierValueRadioButton.setEnabled(overlapOnly);
		tierSelectBox.setEnabled(overlapOnly);
				
		super.enterStepForward();
	}	
	
	
}
