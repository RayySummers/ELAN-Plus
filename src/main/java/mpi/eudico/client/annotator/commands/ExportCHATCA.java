package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;

import java.util.ArrayList;


/**
 * Export to CHAT format action.
 *
 * @author Hennie Brugman
 */
@SuppressWarnings("serial")
public class ExportCHATCA extends CommandAction {
    private final TranscriptionStore transcriptionStore;

    /**
     * Creates a new instance
     *
     * @param viewerManager the viewer manager
     */
    public ExportCHATCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_CHAT);

        transcriptionStore = ACMTranscriptionStore.getCurrentTranscriptionStore();
    }

    /**
     * Creates a new {@code ExportCHATCommand}
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.EXPORT_CHAT);
    }

    /**
     * @return the transcription
     */
    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * @return an array containing a {@code TranscriptionStore} and a list of visible tiers
     */
    @Override
    protected Object[] getArguments() {
        if (vm.getMultiTierControlPanel() != null) {
            return new Object[] {transcriptionStore, vm.getMultiTierControlPanel().getVisibleTiers()};
        } else {
            return new Object[] {transcriptionStore, new ArrayList(0)};
        }
    }
}
