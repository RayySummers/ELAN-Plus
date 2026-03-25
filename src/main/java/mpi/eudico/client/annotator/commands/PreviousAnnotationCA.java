package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import javax.swing.*;

import static mpi.eudico.client.annotator.Constants.ICON_LOCATION;
import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;


/**
 * An action to activate the previous annotation (the annotation on the same tier before the current, active annotation).
 */
@SuppressWarnings("serial")
public class PreviousAnnotationCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new PreviousAnnotationCA instance
     *
     * @param theVM the viewer manager
     */
    public PreviousAnnotationCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.PREVIOUS_ANNOTATION);
        getImageIconFrom(ICON_LOCATION + "GoToPreviousAnnotation.gif").ifPresent(imageIcon -> {
            icon = imageIcon;
            putValue(SMALL_ICON, icon);
        });

        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code ActiveAnnotationCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.ACTIVE_ANNOTATION);
    }

    /**
     * @return the viewer manager
     */
    @Override
    protected Object getReceiver() {
        return vm;
    }

    /**
     * @return an array of size 1, containing the annotation to activate or containing {@code null}
     */
    @Override
    protected Object[] getArguments() {
        Annotation currentActiveAnnot = vm.getActiveAnnotation().getAnnotation();
        Annotation newActiveAnnot = null;

        if (currentActiveAnnot != null) {
            newActiveAnnot = ((TierImpl) (currentActiveAnnot.getTier())).getAnnotationBefore(currentActiveAnnot);

            if (newActiveAnnot == null) {
                newActiveAnnot = currentActiveAnnot;
            }
        } else { // try on basis of current time and active tier

            Tier activeTier = vm.getMultiTierControlPanel().getActiveTier();

            if (activeTier != null) {
                newActiveAnnot = ((TierImpl) activeTier).getAnnotationBefore(vm.getMasterMediaPlayer().getMediaTime());
            }
        }

        Object[] args = new Object[1];
        args[0] = newActiveAnnot;

        return args;
    }
}
