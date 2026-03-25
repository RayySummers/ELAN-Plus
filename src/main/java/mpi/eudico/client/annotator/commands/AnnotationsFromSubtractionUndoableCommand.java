package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.tier.AnnotationFromSubtraction;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;


/**
 * A command that creates annotations based on subtracting the annotations on the the selected tiers. The new annotations are
 * created on a new tier.
 *
 * @author Han Sloetjes
 * @author aarsom
 * @version November, 2011
 */
public class AnnotationsFromSubtractionUndoableCommand extends AnnotationFromSubtraction implements UndoableCommand {
    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public AnnotationsFromSubtractionUndoableCommand(String name) {
        super(name);
    }

    /**
     * Recreates the new tier (if applicable) and the newly created annotations.
     */
    @Override
    public void redo() {
        if (transcription != null) {
            TierImpl dt = transcription.getTierWithId(destTierName);

            if (dt == null) {
                dt = transcription.getTierWithId(sourceTiers[0]);
                LinguisticType linguisticType = dt.getLinguisticType();

                if (linguisticType.getConstraints() != null) {
                    // the source tier should be a toplevel tier
                    LOG.severe("The source tier is not a root tier.");

                    return;
                }

                TierImpl destTier =
                    new TierImpl(null, destTierName, dt.getParticipant(), transcription, linguisticType);
                destTier.setAnnotator(dt.getAnnotator());
                destTier.setDefaultLocale(dt.getDefaultLocale());
                destTier.setLangRef(dt.getLangRef());
                transcription.addTier(destTier);
                dt = destTier;
            }

            if (createdAnnos == null || createdAnnos.isEmpty()) {
                LOG.info("No annotations to restore");

                return;
            }

            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            transcription.setNotifying(false);

            Annotation ann;

            for (AnnotationDataRecord createdAnno : createdAnnos) {
                ann = dt.createAnnotation(createdAnno.getBeginTime(), createdAnno.getEndTime());

                if ((ann != null) && (createdAnno.getValue() != null)) {
                    ann.setValue(createdAnno.getValue());
                }
            }

            transcription.setNotifying(true);
            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);
        }
    }

    /**
     * Deletes the new annotations and/or the new tier.
     */
    @Override
    public void undo() {
        if (transcription != null) {
            //if during the gap creation process, a new tier was added
            if (destTierCreated) {
                TierImpl dt = transcription.getTierWithId(destTierName);

                //remove that tier now
                if (dt != null) {
                    transcription.removeTier(dt);
                }
            } else {
                //annotations were added to an already existing tier, so remove annotations only
                TierImpl st = transcription.getTierWithId(destTierName);

                if (st != null) {
                    if ((createdAnnos != null) && !createdAnnos.isEmpty()) {
                        transcription.setNotifying(false);

                        Annotation ann;

                        for (AnnotationDataRecord createdAnno : createdAnnos) {
                            ann = st.getAnnotationAtTime((createdAnno.getBeginTime() + createdAnno.getEndTime()) / 2);

                            if (ann != null) {
                                st.removeAnnotation(ann);
                            }
                        }

                        transcription.setNotifying(true);
                    }
                }
            }
        }
    }
}
