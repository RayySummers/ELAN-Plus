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
 * A menu action to create a multiple step window for the annotations from subtraction process applied to multiple files.
 */
@SuppressWarnings("serial")
public class MultiFileAnnotationsFromSubtractionMA extends FrameMenuAction {

    /**
     * Creates the new multiple file annotations from subtraction instance
     *
     * @param name the menu action
     * @param frame the containing frame
     */
    public MultiFileAnnotationsFromSubtractionMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
        StepPane step1 = new OverlapsOrSubtractionStep1(pane, null, true);
        StepPane step2 = new SubtractionStep2(pane);
        StepPane step3 = new OverlapsOrSubtractionStep3(pane, null, true);
        StepPane step4 = new OverlapsOrSubtractionStep4(pane, true);
        StepPane step5 = new OverlapsOrSubtractionStep5(pane, null, true);

        pane.addStep(step1);
        pane.addStep(step2);
        pane.addStep(step3);
        pane.addStep(step4);
        pane.addStep(step5);

        JDialog dialog = pane.createDialog(frame, ElanLocale.getString("SubtractAnnotationDialog.Title"), true);
        dialog.setPreferredSize(new Dimension(600, 600));
        dialog.pack();
        dialog.setVisible(true);
    }
}
