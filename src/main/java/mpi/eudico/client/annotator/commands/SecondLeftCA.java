package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;

import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;


/**
 * A command action to move the media playhead one second backward.
 */
@SuppressWarnings("serial")
public class SecondLeftCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new SecondLeftCA instance
     *
     * @param theVM the viewer manager
     */
    public SecondLeftCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.SECOND_LEFT);

        getImageIconFrom(Constants.ICON_LOCATION + "1SecLeftButton.gif").ifPresent(imageIcon -> {
            icon = imageIcon;
            putValue(SMALL_ICON, icon);
        });

        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code SecondLeftCommand}
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SECOND_LEFT);
    }

    /**
     * @return the media player
     */
    @Override
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }
}
