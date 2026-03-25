package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;


/**
 * Deletes multiple annotations selected by the user.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class DeleteSelectedAnnotationsCommand implements UndoableCommand {
    private final String commandName;
    private TranscriptionImpl transcription;

    private List<DefaultMutableTreeNode> delAnnRecords;

    /**
     * Creates a new DeleteSelectedAnnotationsCommand instance.
     *
     * @param name the name of the command
     */
    public DeleteSelectedAnnotationsCommand(String name) {
        commandName = name;
    }

    /**
     * Deletes the annotations again
     */
    @Override
    public void redo() {
        deleteAnnotations();
    }

    /**
     * Restores the annotations that have been deleted
     */
    @Override
    public void undo() {
        if ((transcription != null) && (delAnnRecords != null)) {

            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }
            transcription.setNotifying(false);
            setWaitCursor(true);

            for (DefaultMutableTreeNode n : delAnnRecords) {
                AnnotationRecreator.createAnnotationFromTree(transcription, n, true);
            }

            setWaitCursor(false);

            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);
            transcription.setNotifying(true);
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcriptionImpl
     * @param arguments the arguments:
     *     <ul>
     *     <li>arg[0] = the selected annotations ({@code List<AbstractAnnotation>})</li>
     *     </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        if (arguments != null && arguments[0] instanceof List) {
            List<AbstractAnnotation> selectedAnnos = (List<AbstractAnnotation>) arguments[0];

            delAnnRecords = new ArrayList<DefaultMutableTreeNode>(selectedAnnos.size());

            setWaitCursor(true);
            storeAnnotationTrees(selectedAnnos);

            deleteAnnotations();
            setWaitCursor(false);
        }
    }

    private void storeAnnotationTrees(List<AbstractAnnotation> selectedAnnos) {
        DefaultMutableTreeNode node;
        if (selectedAnnos != null) {
            // first check if there are no parent-child (ancestor-descendant) combinations in the list
            outerloop:
            for (int i = 0; i < selectedAnnos.size(); i++) {
                AbstractAnnotation annotation = selectedAnnos.get(i);

                for (int j = 0; j < selectedAnnos.size(); j++) {
                    if (j == i) {
                        continue;
                    }
                    AbstractAnnotation annotation2 = selectedAnnos.get(j);

                    if ((annotation.getTier()).hasAncestor(annotation2.getTier())) {
                        if (annotation.getBeginTimeBoundary() >= annotation2.getBeginTimeBoundary()
                            && annotation.getEndTimeBoundary() <= annotation2.getEndTimeBoundary()) {
                            continue outerloop;
                        }
                    }
                }
                // if we get here there is no ancestor annotation of this annotation
                // in the list, so add the annotation
                node = AnnotationRecreator.createTreeForAnnotation(annotation);

                if (node != null) {
                    delAnnRecords.add(node);
                }
            }
        }
    }

    /**
     * Deletes the annotations based on the stored records.
     */
    private void deleteAnnotations() {
        if ((delAnnRecords != null) && (delAnnRecords.size() > 0)) {
            List<Annotation> toDelete = new ArrayList<Annotation>(delAnnRecords.size());
            TierImpl tier = null;
            AbstractAnnotation annotation;
            AnnotationDataRecord annotationDataRecord;

            for (DefaultMutableTreeNode n : delAnnRecords) {
                annotationDataRecord = (AnnotationDataRecord) n.getUserObject();
                tier = transcription.getTierWithId(annotationDataRecord.getTierName());

                if (tier == null) {
                    LOG.warning("The tier could not be found: " + annotationDataRecord.getTierName());
                    continue;
                }

                annotation = (AbstractAnnotation) tier.getAnnotationAtTime((annotationDataRecord.getBeginTime()
                                                                            + annotationDataRecord.getEndTime()) / 2);

                if (annotation != null) {
                    toDelete.add(annotation);
                }
            }

            transcription.setNotifying(false);
            for (Annotation ann : toDelete) {
                ann.getTier().removeAnnotation(ann);
            }
            transcription.setNotifying(true);
        }
    }

    @Override
    public String getName() {
        return commandName;
    }

    /**
     * Changes the cursor to either a 'busy' cursor or the default cursor.
     *
     * @param showWaitCursor when <code>true</code> show the 'busy' cursor
     */
    private void setWaitCursor(boolean showWaitCursor) {
        if (showWaitCursor) {
            ELANCommandFactory.getRootFrame(transcription)
                              .getRootPane()
                              .setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            ELANCommandFactory.getRootFrame(transcription).getRootPane().setCursor(Cursor.getDefaultCursor());
        }
    }
}
