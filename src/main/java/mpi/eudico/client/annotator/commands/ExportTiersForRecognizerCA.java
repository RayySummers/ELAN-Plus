package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action to create a dialog to export tiers in the AVATecH TIER format, in xml or csv.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ExportTiersForRecognizerCA extends CommandAction {

    /**
     * Constructor.
     *
     * @param theVM the viewer manager
     */
    public ExportTiersForRecognizerCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.EXPORT_RECOG_TIER);
    }

    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.EXPORT_RECOG_TIER);
    }

    /**
     * Returns the arguments
     *
     * @return the arguments
     */
    @Override
    protected Object[] getArguments() {
        return new Object[] {vm.getTranscription(), vm.getSelection()};
    }

}
