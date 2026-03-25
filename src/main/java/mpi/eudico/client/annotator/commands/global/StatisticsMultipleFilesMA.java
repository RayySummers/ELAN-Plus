package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.gui.StatisticsMultipleFilesDialog;

import java.awt.event.ActionEvent;


/**
 * Menu action that creates a Statistics Dialog for multiple files.
 *
 * @author Jeffrey Lemein
 * @version 1.0
 */
@SuppressWarnings("serial")
public class StatisticsMultipleFilesMA extends FrameMenuAction {
    /**
     * Creates a new StatisticsMultipleFilesMA instance
     *
     * @param name the name of the action
     * @param frame the parent frame
     */
    public StatisticsMultipleFilesMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new StatisticsMultipleFilesDialog(frame).setVisible(true);
    }
}
