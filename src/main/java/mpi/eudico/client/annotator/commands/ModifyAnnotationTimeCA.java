package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;

/**
 * A command action for modifying an annotation's begin and end time.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ModifyAnnotationTimeCA extends CommandAction {
    /**
     * Creates a new ModifyAnnotationTimeCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ModifyAnnotationTimeCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.MODIFY_ANNOTATION_TIME);
    }

    /**
     * Creates a new {@code ModifyAnnotationTimeCommand}. Checks the selection's begin- and endtime as well as the current
     * annotation's begin- and endtime to avoid the creation of meaningless undoable commands.
     */
    @Override
    protected void newCommand() {
        if (vm.getActiveAnnotation().getAnnotation() instanceof AlignableAnnotation aa) {

            if ((vm.getSelection().getBeginTime() != vm.getSelection().getEndTime()) && ((aa.getBeginTimeBoundary()
                                                                                          != vm.getSelection()
                                                                                               .getBeginTime())
                                                                                         || (aa.getEndTimeBoundary()
                                                                                             != vm.getSelection()
                                                                                                  .getEndTime()))) {
                command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.MODIFY_ANNOTATION_TIME);
            } else {
                command = null;
            }
        } else {
            command = null;
        }
    }

    /**
     * The receiver of this CommandAction is the Annotation that should be modified.
     *
     * @return the active annotation
     */
    @Override
    protected Object getReceiver() {
        return vm.getActiveAnnotation().getAnnotation();
    }

    /**
     * As arguments the new begin and end time are passed.
     *
     * @return an array of size 2, the begin and end time of the selection
     */
    @Override
    protected Object[] getArguments() {
        return new Object[] {Long.valueOf(vm.getSelection().getBeginTime()), Long.valueOf(vm.getSelection().getEndTime())};
    }
}
