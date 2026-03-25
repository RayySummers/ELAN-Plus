package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;

/**
 * A CoammandAction to paste a complete annotation tree from the system clipboard.
 */
@SuppressWarnings("serial")
public class PasteAnnotationTreeCA extends PasteAnnotationCA {

    /**
     * Creates a new command action instance.
     *
     * @param viewerManager the viewer manager
     */
    public PasteAnnotationTreeCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.PASTE_ANNOTATION_TREE);
        putValue(Action.NAME, ELANCommandFactory.PASTE_ANNOTATION_TREE);
        updateLocale();
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#newCommand()
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.PASTE_ANNOTATION_TREE);
    }

}
