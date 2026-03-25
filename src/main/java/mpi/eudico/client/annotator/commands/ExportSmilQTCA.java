package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action to show a dialog for the export as QuickTime SMIL.
 *
 * @author Aarthy
 * @version Oct 8, 2010
 */
@SuppressWarnings("serial")
public class ExportSmilQTCA extends CommandAction {
    /**
     * Creates a new ExportSmilQTCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ExportSmilQTCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_SMIL_QT);
    }

    /**
     * Creates a new {@code ExportSmilQTCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.EXPORT_SMIL_QT);
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
     * @return an array of size 2, containing the transcription and the selection
     */
    @Override
    protected Object[] getArguments() {
        return new Object[] {vm.getTranscription(), vm.getSelection()};
    }

}
