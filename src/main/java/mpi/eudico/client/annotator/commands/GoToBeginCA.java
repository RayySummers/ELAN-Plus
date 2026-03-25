package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;

import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;


/**
 * An action to move the crosshair or media playhead to the beginning of the file.
 */
@SuppressWarnings("serial")
public class GoToBeginCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new GoToBeginCA instance
     *
     * @param theVM the viewer manager
     */
    public GoToBeginCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.GO_TO_BEGIN);
        getImageIconFrom(Constants.ICON_LOCATION + "GoToBeginButton.gif").ifPresent(imageIcon -> {
            icon = imageIcon;
            putValue(SMALL_ICON, icon);
        });

        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code GoToBeginCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.GO_TO_BEGIN);
    }

    /**
     * @return the master media player
     */
    @Override
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }
}
