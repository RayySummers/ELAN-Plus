package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * First step of merging groups of tiers, i.e. tiers with their depending tiers.
 */
@SuppressWarnings("serial")
public class MergeTierGroupStep1 extends CalcOverlapsStep1 {

	/**
	 * Constructor.
	 * 
	 * @param multiPane the container pane
	 * @param transcription the transcription containing the tiers
	 */
	public MergeTierGroupStep1(MultiStepPane multiPane,
			TranscriptionImpl transcription) {
		super(multiPane, transcription);
	}

	/**
	 * Removes all non top-level tiers from the tables.
	 */
	@Override
	public void initComponents() {
		super.initComponents();
		String name;
		TierImpl t1;
		for (int i = model1.getRowCount() - 1; i >= 0; i--) {
			name = (String) model1.getValueAt(i, model1.findColumn(TierTableModel.NAME));
			t1 = (TierImpl) transcription.getTierWithId(name);
			if (t1 != null && t1.getParentTier() != null) {
				model1.removeRow(i);
			}
		}
		for (int i = model2.getRowCount() - 1; i >= 0; i--) {
			name = (String) model2.getValueAt(i, model2.findColumn(TierTableModel.NAME));
			t1 = (TierImpl) transcription.getTierWithId(name);
			if (t1 != null && t1.getParentTier() != null) {
				model2.removeRow(i);
			}
		}
	}
	
}
