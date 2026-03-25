package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;

import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;


/**
 * An action to move the media playhead one frame forward.
 */
@SuppressWarnings("serial")
public class NextFrameCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new NextFrameCA instance
     *
     * @param theVM the viewer manager
     */
    public NextFrameCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.NEXT_FRAME);
        getImageIconFrom(Constants.ICON_LOCATION + "NextButton.gif").ifPresent(imageIcon -> {
            icon = imageIcon;
            putValue(SMALL_ICON, icon);
        });
        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code NextFrameCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.NEXT_FRAME);
    }

    /**
     * @return the media player
     */
    @Override
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }
}
