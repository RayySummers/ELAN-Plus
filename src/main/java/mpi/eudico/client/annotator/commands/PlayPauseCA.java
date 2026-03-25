package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.StartEvent;
import mpi.eudico.client.mediacontrol.StopEvent;

import javax.swing.*;

import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;


/**
 * An action to start or pause the media player.
 */
@SuppressWarnings("serial")
public class PlayPauseCA extends CommandAction implements ControllerListener {
    private Icon playIcon;
    private Icon pauseIcon;

    /**
     * Creates a new PlayPauseCA instance
     *
     * @param theVM the viewer manager
     */
    public PlayPauseCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.PLAY_PAUSE);

        // ask ViewerManager to connect to player
        vm.connectListener(this);
        getImageIconFrom(Constants.ICON_LOCATION + "PlayButton.gif").ifPresent(imageIcon -> {
            playIcon = imageIcon;
            putValue(SMALL_ICON, playIcon);
        });
        getImageIconFrom(Constants.ICON_LOCATION + "PauseButton.gif").ifPresent(imageIcon -> {
            pauseIcon = imageIcon;
        });
        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code PlayPauseCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.PLAY_PAUSE);
    }

    /**
     * @return the media player
     */
    @Override
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }

    /**
     * @return an array of size 1, containing the media player controller
     */
    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[1];
        args[0] = vm.getMediaPlayerController();

        return args;
    }

    /**
     * Notification of a media controller event, the {@code StartEvent} and {@code StopEvent} are used to update the
     * play/pause button icon.
     *
     * @param event the controller event
     */
    @Override
    public void controllerUpdate(ControllerEvent event) {
        if (event instanceof StopEvent) {
            setPlayPauseButton(true);
        }

        if (event instanceof StartEvent) {
            setPlayPauseButton(false);
        }
    }

    private void setPlayPauseButton(boolean play) {
        if (play) {
            putValue(SMALL_ICON, playIcon);
        } else {
            putValue(SMALL_ICON, pauseIcon);
        }
    }
}
