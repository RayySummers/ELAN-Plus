package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * A menu action to create a dialog for the multiple file annotations from overlaps procedure.
 */
@SuppressWarnings("serial")
public class MultiFileAnnotationsFromOverlapsMA extends FrameMenuAction {

    /**
     * Creates the new multiple-file annotation from overlap instance
     *
     * @param name the menu action
     * @param frame the containing frame
     */
    public MultiFileAnnotationsFromOverlapsMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates a multiple step pane and adds five step panes providing options for customizing the annotations from overlaps
     * process.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
        StepPane step1 = new OverlapsOrSubtractionStep1(pane, null);
        StepPane step2 = new OverlapsStep2(pane, frame);
        StepPane step3 = new OverlapsOrSubtractionStep3(pane, null);
        StepPane step4 = new OverlapsOrSubtractionStep4(pane);
        StepPane step5 = new OverlapsOrSubtractionStep5(pane, null);

        pane.addStep(step1);
        pane.addStep(step2);
        pane.addStep(step3);
        pane.addStep(step4);
        pane.addStep(step5);

        JDialog dialog = pane.createDialog(frame, ElanLocale.getString("OverlapsDialog.Title"), true);
        dialog.setPreferredSize(new Dimension(600, 600));
        dialog.pack();
        dialog.setVisible(true);
    }
}
