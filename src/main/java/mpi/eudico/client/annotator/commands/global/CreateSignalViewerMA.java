package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.util.FrameConstants;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * A menu action to create or destroy the Signal Viewer.
 */
@SuppressWarnings("serial")
public class CreateSignalViewerMA extends FrameMenuAction {
    /**
     * Creates a new CreateSignalViewerMA instance
     *
     * @param name name of the action
     * @param frame the parent frame
     */
    public CreateSignalViewerMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Adds or removes the signal viewer to/from the layout. Sets the preference setting when changed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        boolean value;
        if (e.getSource() instanceof JCheckBoxMenuItem) {
            value = ((JCheckBoxMenuItem) e.getSource()).isSelected();
            frame.setMenuEnabled(FrameConstants.WAVE_FORM_VIEWER, value);
            Preferences.set(commandId, value, null, false);
            if (frame.isInitialized()) {
                frame.getLayoutManager().updateViewer(ELANCommandFactory.SIGNAL_VIEWER, value);
                if (!value) {
                    frame.getViewerManager().destroyViewerByName(ELANCommandFactory.SIGNAL_VIEWER);
                } else {
                    frame.getWaveFormViewerMenuManager().reinitializeWaveFormMenu();
                }
            }
        }
    }

}
