package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.Annotation;


/**
 * A command action for modifying an annotation.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ModifyAnnotationCA extends CommandAction implements ActiveAnnotationListener {
    private Annotation activeAnnotation;

    /**
     * Creates a new ModifyAnnotationCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ModifyAnnotationCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.MODIFY_ANNOTATION);

        viewerManager.connectListener(this);
        setEnabled(false);
    }

    /**
     * Creates a new {@code ModifyAnnotationDlgCommand}
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.MODIFY_ANNOTATION_DLG);
    }

    /**
     * The receiver of this CommandAction is the Annotation that should be modified.
     *
     * @return the active annotation
     */
    @Override
    protected Object getReceiver() {
        return activeAnnotation;
    }

    /**
     * @return {@code null}
     */
    @Override
    protected Object[] getArguments() {
        return null;
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
     * Sets the enabled state of this action.
     */
    protected void checkState() {
        setEnabled(activeAnnotation != null);
    }
}
