package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * Panel for step 5 of merging tiers: the final step and actual calculation.  
 * A command is created and this pane is connected as progress listener.
 * The UI is a progress monitor.
 *
 * @author aarsom
 * @version Feb, 2014
 */
@SuppressWarnings("serial")
public class MergeMultiTiersStep5 extends OverlapsOrSubtractionStep5 {
	
	private boolean processOnlyOverlaps;

	/**
	 * Constructor.
	 * 
	 * @param multiPane the container pane
	 * @param transcription the transcription in case of single document operation
	 */
	public MergeMultiTiersStep5(MultiStepPane multiPane, Transcription transcription) {
		super(multiPane, transcription);
	}

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("MergeTiers.Title.Step5");
	}

	@Override
	public void enterStepForward() {
		processOnlyOverlaps = (Boolean) multiPane.getStepProperty("OnlyProcessOverlapingAnnotations");	
		
		super.enterStepForward();
    }

	@Override
	public void startProcess() {
		String sourceTiers[] = new String[selectedTiers.size()];
		for( int i=0; i<selectedTiers.size(); i++ ){
			sourceTiers[i] = (String) selectedTiers.get(i);		
		}
				
		Object[] filenames = null;
		if( files != null ){
			filenames = files.toArray();
		}
				
		Object args[] = null;
		MergeTiers com = null;
		
		args = new Object[]{ sourceTiers, destTierName, destLingType,
					annotationValueType, timeFormat, annWithValue, annFromTier, tierOrder, 
					overlapsCriteria, tierValuePairs, processMode, filenames,
					usePalFormat, processOnlyOverlaps };
		// create a command and connect as listener
		if( transcription != null ){
			com = (MergeTiers) ELANCommandFactory.createCommand(transcription, ELANCommandFactory.MERGE_TIERS);
		} else{
			com = new MergeTiers(ELANCommandFactory.MERGE_TIERS);				
		}	 	
		com.addProgressListener(this);
		com.execute(transcription, args);
	}
}
