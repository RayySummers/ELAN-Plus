package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A command action for making the Timeline viewer visible in the Elan Frame.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ShowTimelineCA extends CommandAction {
    private final ElanLayoutManager layoutManager;
    private final Object[] args;

    /**
     * Creates a new ShowTimelineCA instance
     *
     * @param theVM the viewer manager
     * @param layoutManager the layout manager
     */
    public ShowTimelineCA(ViewerManager2 theVM, ElanLayoutManager layoutManager) {
        super(theVM, ELANCommandFactory.SHOW_TIMELINE);
        this.layoutManager = layoutManager;
        args = new Object[] {ELANCommandFactory.SHOW_TIMELINE};
    }

    /**
     * Creates a new {@code ShowMultitierViewerCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SHOW_MULTITIER_VIEWER);
    }

    /**
     * The receiver of this CommandAction is the LayoutManager.
     *
     * @return the layout manager
     */
    @Override
    protected Object getReceiver() {
        return layoutManager;
    }

    /**
     * Argument[0] is ELANCommandFactory.SHOW_TIMELINE.
     *
     * @return an array of size 1, containing {@code ELANCommandFactory#SHOW_TIMELINE}
     */
    @Override
    protected Object[] getArguments() {
        return args;
    }
}
