package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.ResourceUtil;

import javax.swing.*;


/**
 * An action to move the playhead one pixel left (backward).
 */
@SuppressWarnings("serial")
public class PixelLeftCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new PixelLeftCA instance
     *
     * @param theVM the viewer manager
     */
    public PixelLeftCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.PIXEL_LEFT);
        ResourceUtil.getImageIconFrom(Constants.ICON_LOCATION + "1PixelLeftButton.gif").ifPresent(imageIcon -> {
            icon = imageIcon;
            putValue(SMALL_ICON, icon);
        });
        putValue(Action.NAME, "");
    }

    /**
     * Create a new {@code PixelLeftCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.PIXEL_LEFT);
    }

    /**
     * @return the media player
     */
    @Override
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }

    /**
     * @return an array only containing the {@code TimeScale instance}
     */
    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[1];
        args[0] = vm.getTimeScale();

        return args;
    }
}
