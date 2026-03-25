package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;

import java.util.ArrayList;


/**
 * Action to save the transcription in a new location.
 *
 * @author Hennie Brugman
 */
@SuppressWarnings("serial")
public class SaveAsCA extends CommandAction {
    private final TranscriptionStore transcriptionStore;

    /**
     * Creates a new SaveAsCA instance
     *
     * @param viewerManager the viewer manager
     */
    public SaveAsCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SAVE_AS);

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
     * Returns the argument array for the command, containing the constant for the current, latest version of the EAF
     * format.
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
                                 Integer.valueOf(TranscriptionStore.EAF)};
        } else {
            return new Object[] {transcriptionStore,
                                 Boolean.FALSE,
                                 Boolean.TRUE,
                                 new ArrayList(0),
                                 Integer.valueOf(TranscriptionStore.EAF)};
        }
    }
}
