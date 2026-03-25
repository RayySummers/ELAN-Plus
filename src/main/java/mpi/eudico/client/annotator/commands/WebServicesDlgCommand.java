package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.webserviceclient.weblicht.*;

import javax.swing.*;
import java.awt.*;

/**
 * A command that creates and shows the Web Services window.
 *
 * @author Han Sloetjes
 */
public class WebServicesDlgCommand implements Command {
    private final String name;

    /**
     * Constructor.
     *
     * @param name name of the command
     */
    public WebServicesDlgCommand(String name) {
        this.name = name;
    }

    /**
     * @param receiver the viewermanager (or transcription?)
     * @param arguments null
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        ViewerManager2 vm = (ViewerManager2) receiver;
        /*
        WebServicesDialog dialog = null;
        if (vm != null) {
            dialog = new WebServicesDialog(
                ELANCommandFactory.getRootFrame(vm.getTranscription()), false);
        } else {
            dialog = new WebServicesDialog();
        }
        dialog.setTranscription((TranscriptionImpl) vm.getTranscription());
        dialog.setVisible(true);
        */
        if (arguments != null && arguments.length > 0) {
            if (ELANCommandFactory.WEBLICHT_DLG.equals(arguments[0])) {
                MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
                StepPane step1 = new WebLichtStep1(pane);
                pane.addStep(step1);
                step1.setName("TextOrTierStep1");

                StepPane step2 = new WebLichtStep2(pane);
                step2.setName("TextStep2");
                pane.addStep(step2);

                StepPane step3 = new WebLichtStep3(pane);
                step3.setName("TextStep3");
                pane.addStep(step3);

                StepPane step4 = new WebLichtStep4(pane);
                step4.setName("TextStep4");
                pane.addStep(step4);

                StepPane stTier2 = new WebLichtTierBasedStep2(pane);
                stTier2.setName("TierStep2");
                pane.addStep(stTier2);

                StepPane stTier3 = new WebLichtTierBasedStep3(pane);
                stTier3.setName("TierStep3");
                pane.addStep(stTier3);

                StepPane stTier4 = new WebLichtTierBasedStep4(pane);
                stTier4.setName("TierStep4");
                pane.addStep(stTier4);

                StepPane chainStep3 = new WebLichtChainStep3(pane);
                chainStep3.setName("ChainStep3");
                pane.addStep(chainStep3);

                JDialog dialog;
                if (vm != null) {
                    pane.putStepProperty("transcription", vm.getTranscription());
                    dialog = pane.createDialog(ELANCommandFactory.getRootFrame(vm.getTranscription()),
                                               ElanLocale.getString(ELANCommandFactory.WEBLICHT_DLG),
                                               true);
                } else {
                    dialog = pane.createDialog((Frame) null, ElanLocale.getString(ELANCommandFactory.WEBLICHT_DLG), true);
                }
                dialog.setVisible(true);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

}
