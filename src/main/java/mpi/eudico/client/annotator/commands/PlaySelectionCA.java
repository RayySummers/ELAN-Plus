package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.SelectionListener;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;

import static mpi.eudico.client.annotator.Constants.ICON_LOCATION;
import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;


/**
 * An action to start playback of the selected time interval.
 */
@SuppressWarnings("serial")
public class PlaySelectionCA extends CommandAction implements SelectionListener {
    private Icon icon;

    /**
     * Creates a new PlaySelectionCA instance
     *
     * @param theVM the viewer manager
     */
    public PlaySelectionCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.PLAY_SELECTION);
        getImageIconFrom(ICON_LOCATION + "PlaySelectionButton.gif").ifPresent(imageIcon -> {
            icon = imageIcon;
            putValue(SMALL_ICON, icon);
        });
        putValue(Action.NAME, "");
        vm.connectListener(this);
    }

    /**
     * Play around selection and play selection use the same command; play selection passes 0 as offset.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.PLAY_SELECTION);
    }

    /**
     * @return the media player
     */
    @Override
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }

    /**
     * @return an array of size 3, containing the selection, the player controller and the play around selection value of
     *     {@code 0}.
     */
    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[3];
        args[0] = vm.getSelection();
        args[1] = vm.getMediaPlayerController();
        args[2] = Integer.valueOf(0);

        return args;
    }

    /**
     * If the selection changes while playing a selection stop playing the (old) selection. Stop the player as well??
     *
     * @see mpi.eudico.client.annotator.SelectionListener#updateSelection()
     */
    @Override
    public void updateSelection() {
        if (vm.getMasterMediaPlayer().isPlaying()) {

            if (vm.getMediaPlayerController().isPlaySelectionMode()) {
                vm.getMediaPlayerController().setPlaySelectionMode(false);
                vm.getMasterMediaPlayer().stop();
                vm.getMasterMediaPlayer().setStopTime(vm.getMasterMediaPlayer().getMediaDuration());
            }
        }
    }
}
