package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.interannotator.AnnotatorCompare;

import java.awt.event.ActionEvent;

/**
 * A menu action that creates and shows the AnnotatorCompare dialog, a dialog for rater comparison.
 *
 * <p>The dialog provides several methods and algorithms to calculate an
 * interrater reliability score.
 *
 * @author keeloo
 */
public class AnnotatorCompareMA extends FrameMenuAction {

    /**
     * ide generated id
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new compare instance.
     *
     * @param name locale key of the string naming the menu action
     * @param frame ElanFrame2 object that is parent to the dialog
     */
    public AnnotatorCompareMA(String name, ElanFrame2 frame) {
        // let a FrameMenuAction object remember the name and the frame
        super(name, frame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create the dialog

        new AnnotatorCompare(this.frame, false);
    }
}
