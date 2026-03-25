package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ActiveAnnotationListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * An action that creates a command that lets the user specify a number  of milliseconds to shift the active annotation and
 * depending annotations.
 *
 * @author Han Sloetjes
 * @version 1.0, Nov 2008
 */
@SuppressWarnings("serial")
public class ShiftActiveAnnotationCA extends CommandAction implements ActiveAnnotationListener {
    /**
     * Creates a new ShiftActiveAnnotationCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ShiftActiveAnnotationCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SHIFT_ACTIVE_ANNOTATION);

        viewerManager.connectListener(this);
    }

    /**
     * Creates a {@code ShiftAnnotationsDlgCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SHIFT_ANN_DLG);
    }

    /**
     * Returns the receiver of the command.
     *
     * @return the receiver of the command, the transcription
     */
    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * The tier the active annotation is on, its begin time and end time.
     *
     * @return an Object array size = 3, Tier, Long, Long
     */
    @Override
    protected Object[] getArguments() {
        if (vm.getActiveAnnotation().getAnnotation() != null) {
            Long bt = Long.valueOf(vm.getActiveAnnotation().getAnnotation().getBeginTimeBoundary());
            Long et = Long.valueOf(vm.getActiveAnnotation().getAnnotation().getEndTimeBoundary());
            return new Object[] {vm.getActiveAnnotation().getAnnotation().getTier(), bt, et};
        }
        return null;
    }

    /**
     * @see mpi.eudico.client.annotator.ActiveAnnotationListener#updateActiveAnnotation()
     */
    @Override
    public void updateActiveAnnotation() {
        if (vm.getActiveAnnotation().getAnnotation() != null) {
            setEnabled(!vm.getActiveAnnotation().getAnnotation().getTier().hasParentTier()
                       && ((TierImpl) vm.getActiveAnnotation().getAnnotation().getTier()).isTimeAlignable());
        } else {
            setEnabled(false);
        }
    }
}
