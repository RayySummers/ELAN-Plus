package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.Annotation;


/**
 * A command action for deleting an annotation.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class DeleteAnnotationCA extends CommandAction implements ActiveAnnotationListener {
    private Annotation activeAnnotation;

    /**
     * Creates a new DeleteAnnotationCA instance
     *
     * @param viewerManager the viewer manager
     */
    public DeleteAnnotationCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.DELETE_ANNOTATION);
        viewerManager.connectListener(this);
        setEnabled(false);
    }

    /**
     * Creates a new {@code DeleteAnnotationCommand}
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.DELETE_ANNOTATION);
    }

    /**
     * The receiver of this CommandAction is the TierImpl object from which the annotation should be removed.
     *
     * @return the tier the annotation is on
     */
    @Override
    protected Object getReceiver() {
        return activeAnnotation.getTier();
    }

    /**
     * @return an array size 2, containing the viewer manager and the active annotation
     */
    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = vm;
        args[1] = activeAnnotation;

        return args;
    }

    /**
     * On a change of ActiveAnnotation perform a check to determine whether this action should be enabled or disabled.<br>
     *
     * @see ActiveAnnotationListener#updateActiveAnnotation()
     */
    @Override
    public void updateActiveAnnotation() {
        activeAnnotation = vm.getActiveAnnotation().getAnnotation();
        checkState();
    }

    /**
     * Checks if there is an active annotation.
     */
    protected void checkState() {
        setEnabled(activeAnnotation != null);
    }
}
