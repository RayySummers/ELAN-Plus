package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;

import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;


/**
 * An action to move the media position to the end of the media file.
 */
@SuppressWarnings("serial")
public class GoToEndCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new GoToEndCA instance
     *
     * @param theVM the viewer manager
     */
    public GoToEndCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.GO_TO_END);
        getImageIconFrom(Constants.ICON_LOCATION + "GoToEndButton.gif").ifPresent(imageIcon -> {
            icon = imageIcon;
            putValue(SMALL_ICON, icon);
        });

        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code GoToEndCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.GO_TO_END);
    }

    /**
     * @return the media player
     */
    @Override
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }
}
