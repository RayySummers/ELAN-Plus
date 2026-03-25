package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;

import java.util.ArrayList;


/**
 * Action to save the transcription as a template file ({@code .etf}).
 */
@SuppressWarnings("serial")
public class SaveAsTemplateCA extends CommandAction {
    private final TranscriptionStore transcriptionStore;

    /**
     * Creates a new SaveAsTemplateCA instance
     *
     * @param viewerManager the viewer manager
     */
    public SaveAsTemplateCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SAVE_AS_TEMPLATE);

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
     * Returns the arguments array for the command, containing a boolean to indicate the transcription should be saved as a
     * template file.
     *
     * @return array of size 5
     */
    @Override
    protected Object[] getArguments() {
        if (vm.getMultiTierControlPanel() != null) {
            return new Object[] {transcriptionStore,
                                 Boolean.TRUE,
                                 Boolean.TRUE,
                                 vm.getMultiTierControlPanel().getVisibleTiers(),
                                 Integer.valueOf(TranscriptionStore.EAF)};
        } else {
            return new Object[] {transcriptionStore,
                                 Boolean.TRUE,
                                 Boolean.TRUE,
                                 new ArrayList(0),
                                 Integer.valueOf(TranscriptionStore.EAF)};
        }

    }
}
