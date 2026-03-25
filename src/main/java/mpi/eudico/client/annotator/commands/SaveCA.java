package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;

import java.util.ArrayList;
import java.util.Locale;


/**
 * Action to save the transcription in the current version of the native EAF format.
 *
 * @author Hennie Brugman
 */
@SuppressWarnings("serial")
public class SaveCA extends CommandAction {
    private final TranscriptionStore transcriptionStore;

    /**
     * Creates a new SaveCA instance
     *
     * @param viewerManager the viewer manager
     */
    public SaveCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SAVE);

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
     * Returns the arguments array for the command. Checks whether the transcription has already been stored before, i.e.
     * whether it has a valid location or path where it can be saved, and switches to {@code Save As} if not.
     *
     * @return an array of size 5
     */
    @Override
    protected Object[] getArguments() {
        boolean saveNewCopy = false;
        String fileName = vm.getTranscription().getName();

        if (fileName.equals(TranscriptionImpl.UNDEFINED_FILE_NAME) || !fileName.toLowerCase(Locale.getDefault())
                                                                               .endsWith(".eaf")) {
            saveNewCopy = true;
        }

        if (vm.getMultiTierControlPanel() != null) {
            return new Object[] {transcriptionStore,
                                 Boolean.FALSE,
                                 Boolean.valueOf(saveNewCopy),
                                 vm.getMultiTierControlPanel().getVisibleTiers(),
                                 Integer.valueOf(TranscriptionStore.EAF)};
        } else {
            return new Object[] {transcriptionStore,
                                 Boolean.FALSE,
                                 Boolean.valueOf(saveNewCopy),
                                 new ArrayList(0),
                                 Integer.valueOf(TranscriptionStore.EAF)};
        }
    }
}
