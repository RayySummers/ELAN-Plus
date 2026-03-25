package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A command action for copying the current time to the pasteboard/clipboard.
 *
 * @author Aarthy Somasundaram
 * @version Dec 2010
 */
@SuppressWarnings("serial")
public class CopyCurrentTimeToPasteBoardCA extends CommandAction {

    /**
     * Creates a new CopyCurrentTimeToPasteBoardCA instance
     *
     * @param theVM the viewer manager
     */
    public CopyCurrentTimeToPasteBoardCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.COPY_CURRENT_TIME);
    }

    /**
     * Creates a new {@link CopyCurrentTimeToPasteBoardCommand}
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.COPY_CURRENT_TIME);
    }

    /**
     * @return the media player controller
     */
    @Override
    protected Object getReceiver() {
        return vm.getMediaPlayerController();
    }

    /**
     * @return the master media player
     */
    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[1];
        args[0] = vm.getMasterMediaPlayer();

        return args;
    }
}
