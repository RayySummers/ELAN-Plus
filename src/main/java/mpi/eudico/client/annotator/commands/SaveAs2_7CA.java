package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;

import java.util.ArrayList;

/**
 * A command action to save a transcription in {@code EAF 2.7} format.
 *
 * @author Olaf Seibert
 */
@SuppressWarnings("serial")
public class SaveAs2_7CA extends CommandAction {
    private final TranscriptionStore transcriptionStore;

    /**
     * Creates a new SaveAsCA instance
     *
     * @param viewerManager the viewer manager
     */
    public SaveAs2_7CA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_EAF_2_7);

        transcriptionStore = ACMTranscriptionStore.getCurrentTranscriptionStore();
    }

    /**
     * Creates a new {@code StoreCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.STORE);
    }

    /**
     * @return the transcription
     */
    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * The returned arguments array contains, among others, the integer constant for EAF 2.7,
     * {@link TranscriptionStore#EAF_2_7}.
     *
     * @return an array of size 5
     */
    @Override
    protected Object[] getArguments() {
        if (vm.getMultiTierControlPanel() != null) {
            return new Object[] {transcriptionStore,
                                 Boolean.FALSE,
                                 Boolean.TRUE,
                                 vm.getMultiTierControlPanel().getVisibleTiers(),
                                 Integer.valueOf(TranscriptionStore.EAF_2_7)};
        } else {
            return new Object[] {transcriptionStore,
                                 Boolean.FALSE,
                                 Boolean.TRUE,
                                 new ArrayList<TierImpl>(0),
                                 Integer.valueOf(TranscriptionStore.EAF_2_7)};
        }
    }
}
