package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.ModifyBoundariesOfAllAnnotationsStep1;
import mpi.eudico.client.annotator.tier.ModifyBoundariesOfAllAnnotationsStep2;
import mpi.eudico.client.annotator.tier.ModifyBoundariesOfAllAnnotationsStep3;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.*;
import java.awt.*;

/**
 * A command for modifying all annotation's boundaries of selected tiers dialog
 */
public class ModifyAllAnnotationsDlgCommand implements Command {
    private final String commandName;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public ModifyAllAnnotationsDlgCommand(String name) {
        this.commandName = name;
    }


    /**
     * Creates the modify all annotation boundaries dialog.
     *
     * @param receiver the transcription
     * @param arguments null
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object, java.lang.Object[])
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl transcription = (TranscriptionImpl) receiver;

        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
        StepPane step1 = new ModifyBoundariesOfAllAnnotationsStep1(pane, transcription);
        StepPane step2 = new ModifyBoundariesOfAllAnnotationsStep2(pane, null);
        StepPane step3 = new ModifyBoundariesOfAllAnnotationsStep3(pane, transcription);

        pane.addStep(step1);
        pane.addStep(step2);
        pane.addStep(step3);

        JDialog dialog = pane.createDialog(ELANCommandFactory.getRootFrame(transcription),
                                           ElanLocale.getString("ModifyBoundariesOfAllAnnotations.Title"),
                                           true);
        dialog.setPreferredSize(new Dimension(600, 600));
        dialog.pack();
        dialog.setVisible(true);

    }

    @Override
    public String getName() {
        return commandName;
    }

}
