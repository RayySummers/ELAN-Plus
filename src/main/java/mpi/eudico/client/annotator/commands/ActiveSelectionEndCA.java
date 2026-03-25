package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;

import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;

/**
 * An action to move the media player crosshair to the end of the selection.
 */
@SuppressWarnings("serial")
public class ActiveSelectionEndCA extends CommandAction {

    public static final String CROSSHAIR_IN_SELECTION_RIGHT_GIF = "CrosshairInSelectionRight.gif";

    /**
     * Constructor
     *
     * @param theVM the viewer manager
     */
    public ActiveSelectionEndCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.SELECTION_END);

        getImageIconFrom(Constants.ICON_LOCATION + CROSSHAIR_IN_SELECTION_RIGHT_GIF).ifPresent(imageIcon -> putValue(
            SMALL_ICON,
            imageIcon));
        putValue(Action.NAME, "");
    }

    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SELECTION_END);
    }

    @Override
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }

    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = vm.getSelection();
        args[1] = ELANCommandFactory.SELECTION_END;

        return args;
    }
}
