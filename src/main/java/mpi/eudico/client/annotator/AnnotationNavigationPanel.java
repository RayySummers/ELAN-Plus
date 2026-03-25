package mpi.eudico.client.annotator;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComponent;

/**
 * A panel with buttons to navigate from the active annotation to a neighboring
 * annotation.
 * Allows to activate the next or previous annotation on the same tier, or the 
 * (nearest) annotation on the tier above or below the current tier.
 */
@SuppressWarnings("serial")
public class AnnotationNavigationPanel extends JComponent {
    private JButton butGoToPreviousAnnotation;
    private JButton butGoToNextAnnotation;
    private JButton butGoToLowerAnnotation;
    private JButton butGoToUpperAnnotation;

    /**
     * Creates a new AnnotationNavigationPanel instance, adds buttons to
     * the layout.
     *
     * @param buttonSize the size of the buttons
     * @param theVM the viewer manager
     */
    public AnnotationNavigationPanel(Dimension buttonSize, ViewerManager2 theVM) {
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 0, 0);
        setLayout(flowLayout);

        butGoToPreviousAnnotation = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(),
                    ELANCommandFactory.PREVIOUS_ANNOTATION));
        butGoToPreviousAnnotation.setPreferredSize(buttonSize);
        add(butGoToPreviousAnnotation);

        butGoToNextAnnotation = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.NEXT_ANNOTATION));
        butGoToNextAnnotation.setPreferredSize(buttonSize);
        add(butGoToNextAnnotation);

        butGoToLowerAnnotation = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.ANNOTATION_DOWN));
        butGoToLowerAnnotation.setPreferredSize(buttonSize);
        add(butGoToLowerAnnotation);

        butGoToUpperAnnotation = new JButton(ELANCommandFactory.getCommandAction(
                    theVM.getTranscription(), ELANCommandFactory.ANNOTATION_UP));
        butGoToUpperAnnotation.setPreferredSize(buttonSize);
        add(butGoToUpperAnnotation);
    }
}
