package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;


/**
 * Action to move the media playhead one second to the right, one second forward.
 */
@SuppressWarnings("serial")
public class SecondRightCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new SecondRightCA instance
     *
     * @param theVM the viewer manager
     */
    public SecondRightCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.SECOND_RIGHT);
        try {
            icon = new ImageIcon(this.getClass().getResource(Constants.ICON_LOCATION + "1SecRightButton.gif"));

            putValue(SMALL_ICON, icon);
        } catch (Throwable t) {

        }
        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code SecondRightCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SECOND_RIGHT);
    }

    /**
     * @return the media player
     */
    @Override
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }
}
