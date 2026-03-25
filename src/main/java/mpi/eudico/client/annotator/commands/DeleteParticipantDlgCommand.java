package mpi.eudico.client.annotator.commands;


import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.DeleteParticipantDialog;

/**
 * A Command to create a the delete participant dialog
 */
public class DeleteParticipantDlgCommand implements Command {
    private final String commandName;
    private ViewerManager2 vm;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public DeleteParticipantDlgCommand(String name) {
        commandName = name;
    }


    /**
     * Creates the delete participant dialog.
     *
     * @param receiver the viewer manager
     * @param arguments null
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object, java.lang.Object[])
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        vm = (ViewerManager2) receiver;

        new DeleteParticipantDialog(vm, ELANCommandFactory.getRootFrame(vm.getTranscription()), true);

    }

    @Override
    public String getName() {
        return commandName;
    }


}
