package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

import javax.swing.*;
import java.awt.*;


/**
 * Creates a simple dialog to change the reference to an external resource URL of an annotation.
 *
 * @author Han Sloetjes
 */
public class ModifyAnnotationResourceURLDlgCommand implements Command {
    private final String commandName;

    /**
     * Constructor.
     *
     * @param commandName the name of the command
     */
    public ModifyAnnotationResourceURLDlgCommand(String commandName) {
        super();
        this.commandName = commandName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Annotation (AbstractAnnotation)
     * @param arguments the arguments:  <ul><li>arg[0] = the transcription, for the creation of a dialog with the
     *     transcription's window as parent (Transcription)</li> </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        Transcription transcription = null;
        AbstractAnnotation annotation = (AbstractAnnotation) receiver;
        if (annotation == null) {
            return;
        }

        transcription = (Transcription) arguments[0];
        String oldValue = annotation.getExtRefValue(ExternalReference.RESOURCE_URL);

        // show option pane
        String newValue = showInputDialog(transcription, oldValue);

        if (newValue == null || newValue.equals(oldValue)) {
            return;
        }
        // create a ModifyAnnotationResourceURLCommand, pass new value as argument
        Command modCmd = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.MODIFY_ANNOTATION_RESOURCE_URL);
        modCmd.execute(annotation, new Object[] {newValue});
    }

    private String showInputDialog(Transcription transcription, String curValue) {
        // the default width of the dialog is rather small, set a minimum width
        JOptionPane pane = new JOptionPane(ElanLocale.getString("ModifyAnnotationResourceURLDialog.Label"),
                                           JOptionPane.PLAIN_MESSAGE,
                                           JOptionPane.OK_CANCEL_OPTION);
        pane.setWantsInput(true);
        pane.setInitialSelectionValue(curValue);
        JFrame frame = ELANCommandFactory.getRootFrame(transcription);
        Dimension parentDim = frame.getSize();
        Dialog d = pane.createDialog(frame, ElanLocale.getString("ModifyAnnotationResourceURLDialog.Title"));
        Dimension paneDim = d.getSize();
        d.setMinimumSize(new Dimension(Math.max(paneDim.width, (int) (parentDim.getWidth() * 0.5)), paneDim.height));
        d.setLocationRelativeTo(frame);
        d.setResizable(true);
        d.setVisible(true);

        Object option = pane.getValue();
        if (option == null) {
            // the dialog was closed via window close button
            return null;
        }
        // OK or Cancel has been clicked
        // the Cancel button case, return null
        if (option instanceof Integer && JOptionPane.OK_OPTION != ((Integer) option).intValue()) {
            return null;
        }
        // the OK case
        Object rf = pane.getInputValue();
        if (rf == null) {
            return "";
        }

        if (rf instanceof String) {
            return (String) rf;
        } else {
            return rf.toString();
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
