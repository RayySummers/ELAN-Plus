package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;

import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;

/**
 * An action to move the media player crosshair to the begin of the selection.
 */
@SuppressWarnings("serial")
public class ActiveSelectionBeginCA extends CommandAction {

    public static final String CROSS_HAIR_IN_SELECTION_LEFT = Constants.ICON_LOCATION + "CrosshairInSelectionLeft.gif";

    /**
     * Constructor
     *
     * @param theVM the viewer manager
     */
    public ActiveSelectionBeginCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.SELECTION_BEGIN);

        getImageIconFrom(CROSS_HAIR_IN_SELECTION_LEFT).ifPresent(icon -> putValue(SMALL_ICON, icon));

        putValue(Action.NAME, "");
    }

    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SELECTION_BEGIN);
    }

    @Override
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }

    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = vm.getSelection();
        args[1] = ELANCommandFactory.SELECTION_BEGIN;

        return args;
    }

}
