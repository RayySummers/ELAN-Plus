package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * Modifies the reference (a URL) to an external resource.
 *
 * @author Han Sloetjes
 */
public class ModifyAnnotationResourceURLCommand implements UndoableCommand {
    private final String commandName;
    private Transcription transcription;
    private AnnotationDataRecord annotationRecord;
    private String oldValue;
    private String newValue;

    /**
     * Constructor.
     *
     * @param commandName the name of the command
     */
    public ModifyAnnotationResourceURLCommand(String commandName) {
        super();
        this.commandName = commandName;
    }

    /**
     * The redo action
     */
    @Override
    public void redo() {
        if ((annotationRecord != null) && (transcription != null)) {
            if (oldValue == null && newValue == null) {
                return;
            }
            TierImpl tier = (TierImpl) transcription.getTierWithId(annotationRecord.getTierName());
            AbstractAnnotation annotation = (AbstractAnnotation) tier.getAnnotationAtTime(annotationRecord.getBeginTime());

            // double check to see if we have the right annotation
            if ((annotation != null) && (annotation.getEndTimeBoundary() == annotationRecord.getEndTime())) {
                if (oldValue != null) {
                    annotation.removeExtRef(new ExternalReferenceImpl(oldValue, ExternalReference.RESOURCE_URL));
                }
                if (newValue != null) {
                    annotation.addExtRef(new ExternalReferenceImpl(newValue, ExternalReference.RESOURCE_URL));
                }
            }
        }
    }

    /**
     * The undo action
     */
    @Override
    public void undo() {
        if ((annotationRecord != null) && (transcription != null)) {
            if (oldValue == null && newValue == null) {
                return;
            }
            TierImpl tier = (TierImpl) transcription.getTierWithId(annotationRecord.getTierName());
            AbstractAnnotation annotation = (AbstractAnnotation) tier.getAnnotationAtTime(annotationRecord.getBeginTime());

            // double check to see if we have the right annotation
            if ((annotation != null) && (annotation.getEndTimeBoundary() == annotationRecord.getEndTime())) {
                if (newValue != null) {
                    annotation.removeExtRef(new ExternalReferenceImpl(newValue, ExternalReference.RESOURCE_URL));
                }
                if (oldValue != null) {
                    annotation.addExtRef(new ExternalReferenceImpl(oldValue, ExternalReference.RESOURCE_URL));
                }
            }
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Annotation (AbstractAnnotation)
     * @param arguments the arguments:  <ul><li>arg[0] = the new resource URL value or {@code null} to remove the current
     *     URL (String)</li></ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        AbstractAnnotation annotation = (AbstractAnnotation) receiver;
        if (annotation == null) {
            return;
        }

        transcription = annotation.getTier().getTranscription();
        oldValue = annotation.getExtRefValue(ExternalReference.RESOURCE_URL);
        annotationRecord = new AnnotationDataRecord(annotation);

        newValue = (String) arguments[0];
        if (newValue.isBlank()) {
            newValue = null;
        }

        if (newValue != null && newValue.equals(oldValue)) {
            oldValue = null; // to make sure undo/redo doesn't change anything
            newValue = null;
            return;
        }

        if (oldValue != null) {
            annotation.removeExtRef(new ExternalReferenceImpl(oldValue, ExternalReference.RESOURCE_URL));
        }

        if (newValue != null) {
            annotation.addExtRef(new ExternalReferenceImpl(newValue, ExternalReference.RESOURCE_URL));
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
