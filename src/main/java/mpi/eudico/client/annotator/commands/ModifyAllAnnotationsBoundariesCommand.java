package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A command for modifying the begin and end time boundaries of all annotations of a tier
 */
public class ModifyAllAnnotationsBoundariesCommand implements UndoableCommand {

    private final String commandName;

    private long oldBeginTime;
    private long oldEndTime;

    private long beginTimeShift;
    private long endTimeShift;

    private long newBeginTime;
    private long newEndTime;

    private Boolean overrideLeftAnnotation = false;
    private Boolean overrideRightAnnotation = false;

    private List<TierImpl> allTiers;

    private TranscriptionImpl transcription;

    private ArrayList<Object> changedAnnotations;

    private final Map<String, List<Object>> changedTiers = new HashMap<>();

    /**
     * Creates a new ModifyAllAnnotationsBoundaryCommand instance.
     *
     * @param name the name of the command
     */
    public ModifyAllAnnotationsBoundariesCommand(String name) {
        commandName = name;
    }

    /**
     * Executes the command.
     *
     * @param receiver the TranscriptionImpl
     * @param arguments the arguments:
     *     <ul>
     *     <li>arg[0] = begin time shift value (Long)</li>
     *     <li>arg[1] = end time shift value (Long)</li>
     *     <li>arg[2] = override left annotation (Boolean)</li>
     *     <li>arg[3] = override right annotation (Boolean)</li>
     *     <li>arg[4] = list of selected tiers (List&lt;&gt;)</li>
     *     </ul>
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(Object receiver, Object[] arguments) {

        transcription = (TranscriptionImpl) receiver;

        beginTimeShift = ((Long) arguments[0]).longValue();
        endTimeShift = ((Long) arguments[1]).longValue();

        overrideLeftAnnotation = (Boolean) arguments[2];
        overrideRightAnnotation = (Boolean) arguments[3];
        allTiers = (List<TierImpl>) arguments[4];
        // move included_in tiers to the end of the list, after the top level
        // tiers, if both types are present in the list
        boolean topTierPresent = false;
        boolean depTierPresent = false;
        for (TierImpl t : allTiers) {
            if (!t.getLinguisticType().hasConstraints()) {
                topTierPresent = true;
            } else {
                depTierPresent = true;
            }

            if (topTierPresent && depTierPresent) {
                allTiers = sortTiers(allTiers);
                break;
            }
        }

        int curPropMode = 0;
        curPropMode = transcription.getTimeChangePropagationMode();
        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        iterateTiers();
        transcription.setTimeChangePropagationMode(curPropMode);

    }

    /**
     * Ensures that the top level tiers are in the list before dependent tiers so that annotations on those tiers are
     * modified first.
     *
     * @param curList the list of tiers in mixed order
     *
     * @return a list with top level tiers first
     */
    private List<TierImpl> sortTiers(List<TierImpl> curList) {
        List<TierImpl> sortedList = new ArrayList<TierImpl>(curList.size());
        for (TierImpl t : curList) {
            // add top level tier
            if (!t.getLinguisticType().hasConstraints()) {
                sortedList.add(t);
            }
        }
        for (TierImpl t : curList) {
            // add dependent tier
            if (t.getLinguisticType().hasConstraints()) {
                sortedList.add(t);
            }
        }
        return sortedList;
    }

    /**
     * Iterates over tiers, calculates the begin and end time values based on the user provided shift values and override
     * parameters.
     */
    private void iterateTiers() {

        for (TierImpl tier : allTiers) {
            changedAnnotations = new ArrayList<Object>();

            transcription = tier.getTranscription();

            List<AlignableAnnotation> annotations = tier.getAlignableAnnotations();

            for (AlignableAnnotation annotation : annotations) {
                changedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(annotation));
            }

            for (AlignableAnnotation annotation : annotations) {
                this.oldBeginTime = annotation.getBeginTimeBoundary();
                this.oldEndTime = annotation.getEndTimeBoundary();

                if (beginTimeShift < 0) {
                    if (overrideLeftAnnotation) {
                        newBeginTime = oldBeginTime - Math.abs(beginTimeShift);
                    } else {
                        newBeginTime = oldBeginTime - Math.abs(beginTimeShift);
                        List<Annotation> overlappingAnnotations = tier.getOverlappingAnnotations(newBeginTime, oldBeginTime);
                        if (overlappingAnnotations.size() > 0) {
                            AbstractAnnotation overlappingAnnotation = (AbstractAnnotation) overlappingAnnotations.get(0);
                            newBeginTime = overlappingAnnotation.getEndTimeBoundary();
                        }
                    }
                } else {
                    newBeginTime = oldBeginTime + beginTimeShift;
                }

                if (endTimeShift <= 0) {
                    newEndTime = oldEndTime - Math.abs(endTimeShift);
                } else {
                    if (overrideRightAnnotation) {
                        newEndTime = oldEndTime + endTimeShift;
                    } else {
                        newEndTime = oldEndTime + endTimeShift;
                        List<Annotation> overlappingAnnotations = tier.getOverlappingAnnotations(oldEndTime, newEndTime);
                        if (overlappingAnnotations.size() > 0) {
                            AbstractAnnotation overlappingAnnotation = (AbstractAnnotation) overlappingAnnotations.get(0);
                            newEndTime = overlappingAnnotation.getBeginTimeBoundary();
                        }
                    }
                }

                modifyAnnotation(annotation, newBeginTime, newEndTime);

            }
            changedTiers.put(tier.getName(), changedAnnotations);

        }

    }

    /**
     * Updates the annotation with new begin/end time values.
     *
     * @param annotation the annotation to be updated
     * @param newBeginTime the new begin time
     * @param newEndTime the new end time
     */
    public void modifyAnnotation(AlignableAnnotation annotation, long newBeginTime, long newEndTime) {
        oldBeginTime = annotation.getBeginTimeBoundary();
        oldEndTime = annotation.getEndTimeBoundary();

        if (newBeginTime < 0) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("New begin time < 0, changing to 0");
            }
            newBeginTime = 0;
        }
        if (newEndTime <= newBeginTime) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Cannot modify annotation times: end time <= begin time");
            }
            return;
        }

        // only do something if begin and/or end time has changed
        if ((oldBeginTime == newBeginTime) && (oldEndTime == newEndTime)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Cannot modify annotation times: no changes in begin or end time");
            }
            return;
        }

        TierImpl tier = (TierImpl) annotation.getTier();

        transcription = tier.getTranscription();

        if (MonitoringLogger.isInitiated()) {
            MonitoringLogger.getLogger(transcription).log(MonitoringLogger.CHANGE_ANNOTATION_TIME);
        }

        // finally make the change
        annotation.updateTimeInterval(newBeginTime, newEndTime);

    }

    @Override
    public String getName() {
        return commandName;
    }

    /**
     * Undo changes made by this command
     */
    @Override
    public void undo() {
        if ((oldBeginTime == newBeginTime) && (oldEndTime == newEndTime)) {
            return;
        }
        if (newEndTime <= newBeginTime) {
            return;
        }

        transcription.setNotifying(false);

        restore();

        if (MonitoringLogger.isInitiated()) {
            MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.CHANGE_ANNOTATION_TIME);
        }

        transcription.setNotifying(true);

    }

    /**
     * Restore the situation before the edit action; normal mode.
     */
    private void restore() {

        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        DefaultMutableTreeNode node;
        // reverse loop over changed tiers
        for (int ri = allTiers.size() - 1; ri >= 0; ri--) {
            TierImpl ti = allTiers.get(ri);
            String entryKey = ti.getName();
            if (!changedTiers.containsKey(entryKey)) {
                continue;
            }
            //for (Map.Entry<String, List<Object>> entry : changedTiers.entrySet()) {

            TierImpl tier = transcription.getTierWithId(entryKey); // entry.getKey());
            List<AlignableAnnotation> currentAnnotations = tier.getAlignableAnnotations();

            if (currentAnnotations.size() > 0) {
                for (int i = 0; i < currentAnnotations.size(); i++) {
                    AlignableAnnotation currentAnnotation = currentAnnotations.get(i);
                    if (currentAnnotation != null) {
                        tier.removeAnnotation(currentAnnotation);
                    }

                }
            }

            //List<Object> changedAnnotations = entry.getValue();
            List<Object> changedAnnotations = changedTiers.get(entryKey);
            if (changedAnnotations.size() > 0) {
                for (int i = 0; i < changedAnnotations.size(); i++) {
                    node = (DefaultMutableTreeNode) changedAnnotations.get(i);
                    AnnotationRecreator.createAnnotationFromTree(transcription, node, true);
                }
            }
        }

        transcription.setTimeChangePropagationMode(curPropMode);
    }

    /**
     * Re-do the changes made by this command
     */
    @Override
    public void redo() {

        int curPropMode = 0;
        curPropMode = transcription.getTimeChangePropagationMode();
        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        iterateTiers();

        transcription.setTimeChangePropagationMode(curPropMode);
    }

}
