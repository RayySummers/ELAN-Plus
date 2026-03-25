package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * An action to show the Syntax viewer (obsolete).
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class SyntaxViewerCA extends CommandAction {
    /**
     * Creates a new instance
     *
     * @param viewerManager the viewer manager
     */
    public SyntaxViewerCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SYNTAX_VIEWER);
    }

    /**
     * Creates a new {@code SyntaxViewerCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SYNTAX_VIEWER);
    }

    /**
     * There's no logical receiver for this CommandAction.
     *
     * @return {@code null}
     */
    @Override
    protected Object getReceiver() {
        return null;
    }

    /**
     * @return an array of size 2, the transcription and the viewer manager
     */
    @Override
    protected Object[] getArguments() {
        return new Object[] {vm.getTranscription(), vm};
    }
}
