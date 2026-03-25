package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An action to display annotations by means of a density plot
 *
 * @author Allan van Hulst
 */
public class AnnotationDensityPlotCA extends CommandAction {

    /**
     * Constructor.
     *
     * @param theVM the viewer manager
     */
    public AnnotationDensityPlotCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.ANNOTATION_DENSITY_PLOT);
    }

    /**
     * Creates a new command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.ANNOTATION_DENSITY_PLOT);
    }

    /**
     * Returns the transcription.
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#getReceiver()
     */
    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Returns the Transcription and the Selection object.
     *
     * @return an array containing the Transcription and the Selection
     */
    @Override
    protected Object[] getArguments() {
        return new Object[] {vm.getTranscription(), vm.getSelection()};
    }
}
