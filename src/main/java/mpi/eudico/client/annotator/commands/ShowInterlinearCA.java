package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A command action for making the Interlinear viewer visible in the Elan Frame.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ShowInterlinearCA extends CommandAction {
    private final ElanLayoutManager layoutManager;
    private final Object[] args;

    /**
     * Creates a new ShowInterlinearCA instance
     *
     * @param theVM the viewer manager
     * @param layoutManager the layout manager
     */
    public ShowInterlinearCA(ViewerManager2 theVM, ElanLayoutManager layoutManager) {
        super(theVM, ELANCommandFactory.SHOW_INTERLINEAR);
        this.layoutManager = layoutManager;
        args = new Object[] {ELANCommandFactory.SHOW_INTERLINEAR};
    }

    /**
     * Creates a new {@code ShowMultitierViewerCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SHOW_MULTITIER_VIEWER);
    }

    /**
     * The receiver of this CommandAction is the layoutManager.
     *
     * @return the layout manager
     */
    @Override
    protected Object getReceiver() {
        return layoutManager;
    }

    /**
     * Argument[0] is ELANCommandFactory.SHOW_INTERLINEAR.
     *
     * @return an array of size 1, containing the constant {@code ELANCommandFactory#SHOW_INTERLINEAR}
     */
    @Override
    protected Object[] getArguments() {
        return args;
    }
}
