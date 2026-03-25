package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.multiplefiles.MFExportAnnotatedPartStep1;
import mpi.eudico.client.annotator.export.multiplefiles.MFExportAnnotatedPartStep3;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Creates a dialog to select tiers from multiple files for the export of an overview of the total extent of the annotated
 * media segments in each file.
 */
@SuppressWarnings("serial")
public class ExportAnnotatedPartMultiMA extends FrameMenuAction {
    /**
     * Creates a new action instance.
     *
     * @param name name of the action
     * @param frame the containing frame
     */
    public ExportAnnotatedPartMultiMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MultiStepPane multipane = new MultiStepPane();
        multipane.addStep(new MFExportAnnotatedPartStep1(multipane, null));
        // step two, providing options, is not there at the moment
        multipane.addStep(new MFExportAnnotatedPartStep3(multipane));

        JDialog dialog = multipane.createDialog(frame, ElanLocale.getString("MultiFileExportAnnPart.Title"), true);
        dialog.setPreferredSize(new Dimension(600, 600));
        dialog.pack();
        dialog.setVisible(true);
    }

}
