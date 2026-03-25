package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;

import static mpi.eudico.client.annotator.Constants.ICON_LOCATION;
import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;

/**
 * Action to start or stop play back in step-and-repeat mode.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class PlayStepAndRepeatCA extends CommandAction {
    private Icon playIcon;
    private Icon pauseIcon;

    /**
     * Constructor.
     *
     * @param theVM the viewer manager
     */
    public PlayStepAndRepeatCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.PLAY_STEP_AND_REPEAT);

        getImageIconFrom(ICON_LOCATION + "StepAndRepeat_Col16.gif").ifPresent(imageIcon -> {
            playIcon = imageIcon;
            putValue(SMALL_ICON, playIcon);
        });

        getImageIconFrom(ICON_LOCATION + "StepAndRepeatPause_Col16.gif").ifPresent(imageIcon -> pauseIcon = imageIcon);

        putValue(Action.NAME, "");
    }

    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.PLAY_STEP_AND_REPEAT);
    }

    @Override
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }

    @Override
    protected Object[] getArguments() {
        return new Object[] {vm.getMediaPlayerController()};
    }

    /**
     * Changes the icon of the action in a play or pause icon.
     *
     * @param play if true the play icon is set (indicating the paused state
     */
    public void setPlayIcon(boolean play) {
        if (play) {
            putValue(SMALL_ICON, playIcon);
        } else {
            putValue(SMALL_ICON, pauseIcon);
        }
    }
}
