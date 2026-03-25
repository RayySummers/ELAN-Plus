package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.multiplefiles.MultipleFileThemeExportStep1;
import mpi.eudico.client.annotator.export.multiplefiles.MultipleFileThemeExportStep2;
import mpi.eudico.client.annotator.export.multiplefiles.MultipleFileThemeExportStep3;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * An action for starting the export to Theme format process.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ExportThemeMultiMA extends FrameMenuAction {

    /**
     * Constructor.
     *
     * @param name the name of the action
     * @param frame the main frame
     */
    public ExportThemeMultiMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MultiStepPane multipane = new MultiStepPane();

        multipane.addStep(new MultipleFileThemeExportStep1(multipane, null));
        multipane.addStep(new MultipleFileThemeExportStep2(multipane));
        multipane.addStep(new MultipleFileThemeExportStep3(multipane));

        JDialog dialog = multipane.createDialog(frame, ElanLocale.getString("ExportThemeDialog.Title"), true);
        dialog.setPreferredSize(new Dimension(600, 600));
        dialog.pack();
        dialog.setVisible(true);
    }


}
