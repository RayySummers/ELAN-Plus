package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;

import static mpi.eudico.client.annotator.Constants.ICON_LOCATION;
import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;


/**
 * Action to move the media playhead one frame backward.
 */
@SuppressWarnings("serial")
public class PreviousFrameCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new PreviousFrameCA instance
     *
     * @param theVM the viewer manager
     */
    public PreviousFrameCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.PREVIOUS_FRAME);
        getImageIconFrom(ICON_LOCATION + "PreviousButton.gif").ifPresent(imageIcon -> {
            icon = imageIcon;
            putValue(SMALL_ICON, icon);
        });

        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code PreviousFrameCommand}
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.PREVIOUS_FRAME);
    }

    /**
     * @return the media player
     */
    @Override
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }
}
