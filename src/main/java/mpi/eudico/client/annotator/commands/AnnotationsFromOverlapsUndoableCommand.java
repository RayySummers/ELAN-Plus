package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.tier.AnnotationFromOverlaps;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;


/**
 * A command that creates annotations based on the overlaps on the annotations on the the selected tiers. The new annotations
 * are created on a new tier.
 *
 * @author Han Sloetjes
 * @author Jeffrey Lemein
 * @author aarsom
 * @version November, 2011
 */
public class AnnotationsFromOverlapsUndoableCommand extends AnnotationFromOverlaps implements UndoableCommand {
    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public AnnotationsFromOverlapsUndoableCommand(String name) {
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
                    // the source tier should be a top level tier
                    LOG.severe("The source tier is not a root tier.");

                    return;
                }

                TierImpl destinationTier =
                    new TierImpl(null, destTierName, dt.getParticipant(), transcription, linguisticType);
                destinationTier.setAnnotator(dt.getAnnotator());
                destinationTier.setDefaultLocale(dt.getDefaultLocale());
                destinationTier.setLangRef(dt.getLangRef());
                transcription.addTier(destinationTier);
                dt = destinationTier;
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
                TierImpl destinationTier = transcription.getTierWithId(destTierName);

                if (destinationTier != null) {
                    if ((createdAnnos != null) && !createdAnnos.isEmpty()) {
                        transcription.setNotifying(false);

                        Annotation ann;

                        for (AnnotationDataRecord createdAnno : createdAnnos) {
                            ann = destinationTier.getAnnotationAtTime((createdAnno.getBeginTime()
                                                                       + createdAnno.getEndTime()) / 2);

                            if (ann != null) {
                                destinationTier.removeAnnotation(ann);
                            }
                        }

                        transcription.setNotifying(true);
                    }
                }
            }
        }
    }
}
