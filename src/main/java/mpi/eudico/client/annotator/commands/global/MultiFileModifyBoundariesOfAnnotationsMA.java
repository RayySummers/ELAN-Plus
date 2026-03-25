package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.ModifyBoundariesOfAllAnnotationsStep1;
import mpi.eudico.client.annotator.tier.ModifyBoundariesOfAllAnnotationsStep2;
import mpi.eudico.client.annotator.tier.ModifyBoundariesOfAllAnnotationsStep3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * A menu action to create a multiple step window for changing the boundaries of all annotations in multiple tiers also can
 * be applied to multiple files.
 */
@SuppressWarnings("serial")
public class MultiFileModifyBoundariesOfAnnotationsMA extends FrameMenuAction {

    /**
     * Creates the new multiple-file modify all annotation boundaries instance
     *
     * @param name the menu action
     * @param frame the containing frame
     */
    public MultiFileModifyBoundariesOfAnnotationsMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates a multiple step pane and adds three step panes for modification process.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
        StepPane step1 = new ModifyBoundariesOfAllAnnotationsStep1(pane, null);
        StepPane step2 = new ModifyBoundariesOfAllAnnotationsStep2(pane, frame);
        StepPane step3 = new ModifyBoundariesOfAllAnnotationsStep3(pane, null);

        pane.addStep(step1);
        pane.addStep(step2);
        pane.addStep(step3);

        JDialog dialog = pane.createDialog(frame, ElanLocale.getString("ModifyBoundariesOfAllAnnotations.Title"), true);
        dialog.setPreferredSize(new Dimension(600, 600));
        dialog.pack();
        dialog.setVisible(true);
    }

}
