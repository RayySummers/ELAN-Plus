package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Enumeration;

import static mpi.eudico.client.annotator.commands.ELANCommandFactory.*;


/**
 * A command action that switches between the modes. By default the Annotation mode is selected
 *
 * @author Aarthy Somasundaram
 */
@SuppressWarnings("serial")
public class ChangeModeCA extends CommandAction {
    private final ElanLayoutManager layoutManager;
    private final String modeName;

    /**
     * Creates a new ChangeModeCA instance
     *
     * @param theVM the viewer manager
     * @param layoutManager the layout manager
     * @param mode the mode to change to
     */
    public ChangeModeCA(ViewerManager2 theVM, ElanLayoutManager layoutManager, String mode) {
        super(theVM, mode);
        modeName = mode;
        this.layoutManager = layoutManager;
        putValue(Action.LONG_DESCRIPTION, mode);
    }

    /**
     * Creates the command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), modeName);
    }

    /**
     * The receiver of this CommandAction is the layoutManager.
     *
     * @return the layout manager
     */
    @Override
    protected Object getReceiver() {
        return layoutManager;
    }

    /**
     * Argument[0] = current mode reference
     *
     * @return the single argument
     */
    @Override
    protected Object[] getArguments() {
        return new Object[] {getModeId()};
    }

    private int getModeId() {
        if (modeName.equals(SYNC_MODE)) {
            return ElanLayoutManager.SYNC_MODE;
        } else if (modeName.equals(TRANSCRIPTION_MODE)) {
            return ElanLayoutManager.TRANSC_MODE;
        } else if (modeName.equals(SEGMENTATION_MODE)) {
            return ElanLayoutManager.SEGMENT_MODE;
        } else if (modeName.equals(INTERLINEARIZATION_MODE)) {
            return ElanLayoutManager.INTERLINEAR_MODE;
        } else {
            return ElanLayoutManager.NORMAL_MODE;
        }
    }

    /**
     * Not very elegant way to reselect the JRadioMenuItem in case a switch to a certain mode is not allowed. When more modes
     * are added to ElanLayoutManager the switch statement should adapted.
     *
     * @see CommandAction#actionPerformed
     */
    @Override
    public void actionPerformed(final ActionEvent event) {
        JRadioButtonMenuItem oldItem = null;
        JRadioButtonMenuItem item = null;
        int mode = layoutManager.getMode();

        // if the same mode is selected, do nothing
        if (mode == getModeId()) {
            return;
        }

        if (event.getSource() instanceof JRadioButtonMenuItem) {
            item = (JRadioButtonMenuItem) event.getSource();

            if (item.getModel() instanceof JToggleButton.ToggleButtonModel) {
                ButtonGroup group = item.getModel().getGroup();
                Enumeration e = group.getElements();
                elementloop:
                while (e.hasMoreElements()) {
                    oldItem = (JRadioButtonMenuItem) e.nextElement();
                    switch (mode) {
                        case ElanLayoutManager.NORMAL_MODE:
                            if (oldItem.getAction() == getCommandAction(vm.getTranscription(), ANNOTATION_MODE)) {
                                break elementloop;
                            }
                            oldItem = null;
                            break;

                        case ElanLayoutManager.SYNC_MODE:

                            if (oldItem.getAction() == getCommandAction(vm.getTranscription(), SYNC_MODE)) {
                                break elementloop;
                            }
                            oldItem = null;
                            break;

                        case ElanLayoutManager.TRANSC_MODE:

                            if (oldItem.getAction() == getCommandAction(vm.getTranscription(), TRANSCRIPTION_MODE)) {
                                break elementloop;
                            }
                            oldItem = null;
                            break;

                        case ElanLayoutManager.SEGMENT_MODE:
                            if (oldItem.getAction() == getCommandAction(vm.getTranscription(), SEGMENTATION_MODE)) {
                                break elementloop;
                            }
                            oldItem = null;
                            break;

                        case ElanLayoutManager.INTERLINEAR_MODE:
                            if (oldItem.getAction() == getCommandAction(vm.getTranscription(), INTERLINEARIZATION_MODE)) {
                                break elementloop;
                            }
                            oldItem = null;
                            break;

                        default:
                            break;
                    }
                }
            }
        }

        super.actionPerformed(event);

        if (layoutManager.getMode() != this.getModeId()) {
            if (oldItem != null) {
                oldItem.setSelected(true);
            }
        }
    }
}
