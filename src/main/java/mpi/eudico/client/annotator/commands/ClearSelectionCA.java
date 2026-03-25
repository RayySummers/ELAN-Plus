package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;

/**
 * A CommandAction to clear the current selection.
 *
 * <p>Jul 2006: changed the keyboard shortcut from ctrl/com + C to Alt + C to make Ctrl/Com + C available
 * for the copy annotation command.
 *
 * @author MPI
 * @version 1.1
 */
@SuppressWarnings("serial")
public class ClearSelectionCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new ClearSelectionCA instance
     *
     * @param theVM the viewer manager
     */
    public ClearSelectionCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.CLEAR_SELECTION);
        getImageIconFrom(Constants.ICON_LOCATION + "ClearSelectionButton.gif").ifPresent(imageIcon -> {
            icon = imageIcon;
            putValue(SMALL_ICON, icon);
        });

        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@link ClearSelectionCommand}
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.CLEAR_SELECTION);
    }

    /**
     * @return the Selection object
     */
    @Override
    protected Object getReceiver() {
        return vm.getSelection();
    }

    /**
     * @return an array of size 1, the media player controller
     */
    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[1];
        args[0] = vm.getMediaPlayerController();

        return args;
    }

    /**
     * Overrides CommandAction's actionPerformed by doing nothing when there is no  selection. The ClearSelectionCommand is
     * undoable and we don't want meaningless commands in the unod/redo list.
     *
     * @param event the action event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (vm.getSelection().getBeginTime() == vm.getSelection().getEndTime()) {
            return;
        }

        super.actionPerformed(event);
    }
}
