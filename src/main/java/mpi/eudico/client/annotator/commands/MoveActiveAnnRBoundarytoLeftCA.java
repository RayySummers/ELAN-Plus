package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;

/**
 * A command action for moving the right boundary of the active annotation to the left.
 *
 * @author Aarthy Somasundaram
 * @version Jan 2010
 */
@SuppressWarnings("serial")
public class MoveActiveAnnRBoundarytoLeftCA extends CommandAction {

    /**
     * Creates a new MoveActiveAnnRBoundarytoLeftCA instance
     *
     * @param theVM the viewer manager
     */
    public MoveActiveAnnRBoundarytoLeftCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.MOVE_ANNOTATION_RBOUNDARY_LEFT);
    }

    /**
     * Creates a new {@code ModifyAnnotationTimeCommand}.
     */
    @Override
    protected void newCommand() {
        command = null;

        if (vm.getActiveAnnotation().getAnnotation() instanceof AlignableAnnotation aa) {
            command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.MODIFY_ANNOTATION_TIME);
        }
    }

    /**
     * @return the active annotation
     */
    @Override
    protected Object getReceiver() {
        return vm.getActiveAnnotation().getAnnotation();
    }

    /**
     * @return an array of size 2, containing the existing begin time and the new, calculated end time of the annotation
     */
    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = Long.valueOf(vm.getActiveAnnotation().getAnnotation().getBeginTimeBoundary());
        args[1] = Long.valueOf(vm.getActiveAnnotation().getAnnotation().getEndTimeBoundary() - (long) vm.getTimeScale()
                                                                                                        .getMsPerPixel());

        return args;
    }
}
