package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.TimeEvent;

import javax.swing.*;

import static mpi.eudico.client.annotator.Constants.ICON_LOCATION;
import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;


/**
 * Sets the active boundary of the selection (selected time interval). Usually the crosshair of the media player jumps to the
 * active boundary, and in selection mode e.g. the frame forward and backward buttons change the selection on the side of the
 * active boundary
 */
@SuppressWarnings("serial")
public class ActiveSelectionBoundaryCA extends CommandAction implements ControllerListener {
    public static final String CROSSHAIR_IN_SELECTION_LEFT_GIF = "CrosshairInSelectionLeft.gif";
    public static final String CROSSHAIR_IN_SELECTION_RIGHT_GIF = "CrosshairInSelectionRight.gif";
    private Icon leftIcon;
    private Icon rightIcon;
    private boolean leftActive = false;

    /**
     * Creates a new ActiveSelectionBoundaryCA instance
     *
     * @param theVM the viewer manager
     */
    public ActiveSelectionBoundaryCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.SELECTION_BOUNDARY);

        // ask ViewerManager to connect to player
        vm.connectListener(this);

        getImageIconFrom(ICON_LOCATION + CROSSHAIR_IN_SELECTION_LEFT_GIF).ifPresent(icon -> {
            leftIcon = icon;
            putValue(SMALL_ICON, leftIcon);
        });
        getImageIconFrom(ICON_LOCATION + CROSSHAIR_IN_SELECTION_RIGHT_GIF).ifPresent(icon -> rightIcon = icon);

        putValue(Action.NAME, "");
    }

    /**
     * Creates a new set active boundary command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SELECTION_BOUNDARY);
    }

    /**
     * @return the media player controller managing the active boundary and related buttons
     */
    @Override
    protected Object getReceiver() {
        return vm.getMediaPlayerController();
    }

    /**
     * @return an array containing the master media player, the selection and this action
     */
    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[3];
        args[0] = vm.getMasterMediaPlayer();
        args[1] = vm.getSelection();
        args[2] = this;

        return args;
    }

    @Override
    public void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent) {
            updateBoundaryIcon();
        }
    }

    /**
     * Sets the icon for the left or right boundary.
     *
     * @param left if {@code true} the icon for the left boundary is shown, otherwise the icon for the right boundary is
     *     shown
     */
    public void setLeftIcon(boolean left) {
        if (left) {
            putValue(SMALL_ICON, leftIcon);
        } else {
            putValue(SMALL_ICON, rightIcon);
        }
    }

    /**
     * Checks if the arrow icon needs to be updated.
     */
    public void updateBoundaryIcon() {
        if (vm.getMediaPlayerController().isBeginBoundaryActive() && !leftActive) {
            setLeftIcon(false);
            leftActive = true;
        }

        if (!vm.getMediaPlayerController().isBeginBoundaryActive() && leftActive) {
            setLeftIcon(true);
            leftActive = false;
        }
    }
}
