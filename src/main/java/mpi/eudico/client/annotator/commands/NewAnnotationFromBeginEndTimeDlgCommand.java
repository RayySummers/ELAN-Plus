package mpi.eudico.client.annotator.commands;


import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.NewAnnotationFromTimeIntervalDialog;
import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

import javax.swing.*;
import java.util.List;


/**
 * A Command that creates a dialog to type in the Begin time and End time of an annotation, annotation value and also lists
 * the tiers with multiple select option. Once the user inputs are filled, respective commands are called for creating new
 * annotation.
 */
public class NewAnnotationFromBeginEndTimeDlgCommand implements Command {

    private final String commandName;
    private long beginTime;
    private long endTime;
    private String annotationValue;
    private ViewerManager2 vm;
    private Object[] args;

    Boolean isCreateDependingAnnotations = Preferences.getBool("CreateDependingAnnotations", null);

    /**
     * Constructor.
     *
     * @param commandName name of the command
     */
    public NewAnnotationFromBeginEndTimeDlgCommand(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public String getName() {
        return commandName;
    }


    /**
     * @param receiver the ViewerManager, gives access to the transcription, the frame it is in and to the master media
     *     player (for current media time)
     * @param arguments no arguments are used
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {

        vm = (ViewerManager2) receiver;
        TranscriptionImpl transcription = (TranscriptionImpl) vm.getTranscription();
        TierImpl tier;

        NewAnnotationFromTimeIntervalDialog dialog =
            new NewAnnotationFromTimeIntervalDialog(vm, ELANCommandFactory.getRootFrame(vm.getTranscription()), true);


        dialog.setBeginTime(vm.getMediaPlayerController().getMediaTime());
        dialog.setEndTime(dialog.getBeginTime() + 1000);
        dialog.setLimits(0, vm.getMasterMediaPlayer().getMediaDuration());
        WindowLocationAndSizeManager.postInit(dialog, "NewAnnotationFromTimeIntervalDialog");
        dialog.setVisible(true);

        beginTime = dialog.getBeginTime();
        endTime = dialog.getEndTime();
        annotationValue = dialog.getAnnotationValue();
        List<String> listOfTiers = dialog.getTiers();

        if (!dialog.isActionApplied() || beginTime < 0 || beginTime >= endTime) {
            return;
        }


        boolean activeTierPresent = false;

        if (listOfTiers.isEmpty()) {
            activeTierPresent =
                vm.getMultiTierControlPanel() != null && vm.getMultiTierControlPanel().getActiveTier() != null;

            if (activeTierPresent) {
                tier = (TierImpl) vm.getMultiTierControlPanel().getActiveTier();
                newCommand(tier);
            }
        } else {

            for (String selectedTier : listOfTiers) {
                tier = transcription.getTierWithId(selectedTier);
                if (tier != null) {
                    newCommand(tier);
                }
            }

        }

    }


    /**
     * Validates if new annotation can be created and calls the respected commands based on the conditions.
     *
     * @param tier - tier on which the annotation has to be created
     */
    private void newCommand(TierImpl tier) {

        Command command = null;
        args = new Object[] {Long.valueOf(beginTime), Long.valueOf(endTime), annotationValue};

        if (checkTierConstraints(tier)) {

            if (isCreateDependingAnnotations != null) {
                if (isCreateDependingAnnotations.booleanValue()) {
                    command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.NEW_ANNOTATION_REC);
                } else {
                    command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.NEW_ANNOTATION);
                }
            } else {
                command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.NEW_ANNOTATION);
            }

            command.execute(tier, args);

        }

    }


    /**
     * Checks tier constraints and decides whether to create new annotation or not
     *
     * @param tier - tier on which the new annotation to be created
     *
     * @return returns true when tier can have a new annotation,false otherwise
     */
    protected boolean checkTierConstraints(TierImpl tier) {

        if (tier.isTimeAlignable()) {
            return true;
        } else {
            Constraint c = null;

            if (tier.getLinguisticType() != null) {
                c = tier.getLinguisticType().getConstraints();
            }

            if ((tier.getParentTier() != null) && (c != null)) {

                if ((c.getStereoType() == Constraint.SYMBOLIC_ASSOCIATION) || (c.getStereoType()
                                                                               == Constraint.SYMBOLIC_SUBDIVISION)) {
                    long time = (beginTime + endTime) / 2;
                    Annotation refA = tier.getParentTier().getAnnotationAtTime(time);
                    Annotation curA = tier.getAnnotationAtTime(time);

                    if ((refA != null) && (curA == null)) {
                        args = new Object[] {Long.valueOf(time), Long.valueOf(time), annotationValue};
                        return true;
                    } else if (refA != null) {
                        if (isCreateDependingAnnotations != null && isCreateDependingAnnotations.booleanValue()) {
                            return false;
                        }
                        // there is already a child annotation. show dialog and return false
                        JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(vm.getTranscription()),
                                                      ElanLocale.getString("Message.ExistingAnnotation"),
                                                      ElanLocale.getString("Message.Warning"),
                                                      JOptionPane.WARNING_MESSAGE);
                    } else {
                        //there is no parent
                        JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(vm.getTranscription()),
                                                      ElanLocale.getString("Message.NoParent"),
                                                      ElanLocale.getString("Message.Warning"),
                                                      JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }

        return false;
    }


}
