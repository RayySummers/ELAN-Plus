package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.tier.AddDependentTiersToTierStructureDialog;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Creates the dialog to generate the new dependent tiers based on tier structure
 */
public class AddDependentTiersToTierStructureDlgCommand implements Command {

    private final String commandName;
    private ViewerManager2 vm;

    /**
     * Constructor.
     *
     * @param commandName name of the command
     */
    public AddDependentTiersToTierStructureDlgCommand(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public String getName() {
        return commandName;
    }


    /**
     * Creates the add dependent tiers to tier structure dialog.
     *
     * @param receiver the transcription
     * @param arguments null
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object, java.lang.Object[])
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        vm = (ViewerManager2) receiver;
        TranscriptionImpl transcription = (TranscriptionImpl) vm.getTranscription();
        new AddDependentTiersToTierStructureDialog(transcription, ELANCommandFactory.getRootFrame(transcription));
    }


}
