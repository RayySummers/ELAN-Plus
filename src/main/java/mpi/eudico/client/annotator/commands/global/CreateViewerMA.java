package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.Preferences;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * A menu action to create and destroy the TimeSeries Viewer.
 */
@SuppressWarnings("serial")
public class CreateViewerMA extends FrameMenuAction {
    /**
     * Creates a new createTimeSeriesViewerMA instance
     *
     * @param name name of the action
     * @param frame the parent frame
     */
    public CreateViewerMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Sets the preference setting when changed
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        boolean value;
        if (e.getSource() instanceof JCheckBoxMenuItem) {
            value = ((JCheckBoxMenuItem) e.getSource()).isSelected();
            Preferences.set(commandId, value, null, false);
            if (frame.isInitialized()) {
                frame.getLayoutManager().updateViewer(commandId, value);
                if (!value) {
                    frame.getViewerManager().destroyViewerByName(commandId);
                }
            }
        }
    }
}
