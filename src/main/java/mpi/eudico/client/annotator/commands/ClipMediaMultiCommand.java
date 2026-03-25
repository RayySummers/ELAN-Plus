package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.util.ProcessReport;
import nl.mpi.util.FileUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A command to clip several segments from one or more media files.
 *
 * @author Han Sloetjes
 */
public class ClipMediaMultiCommand extends ClipMediaCommand {
    /**
     * Constructor
     *
     * @param theName the name of the command
     */
    public ClipMediaMultiCommand(String theName) {
        super(theName);
    }

    /**
     * @param receiver the transcription
     * @param arguments the arguments for the command
     *     <ul>
     *     <li>arguments[0] = the executable (String)
     *     <li>arguments[1] = the command part of the script (String)
     *     <li>arguments[2] = either a List of segments (AnnotationCore objects)
     *     or a List of tier names (String) from which to extract segments<br>
     *     <li>arguments[3] = output directory (String)
     *     <li>arguments[4] = report object, optional (Report)
     *     </ul>
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(Object receiver, Object[] arguments) {
        // receiver Transcription
        TranscriptionImpl transcription = (TranscriptionImpl) receiver;
        if (arguments.length >= 5) {
            report = (ProcessReport) arguments[4];
        }

        if (transcription.getMediaDescriptors().size() == 0) {
            LOG.warning("No media descriptors in the transcription, nothing to clip");
            if (report != null) {
                report.append("No media descriptors in the transcription, nothing to clip");
            }
            return;
        }

        String executable = (String) arguments[0];
        if (executable == null) {
            // log? has been checked before
            return;
        }
        String command = (String) arguments[1];
        if (command == null) {
            // log? has been checked before
            return;
        }
        // read relevant user preference settings
        readPrefs();

        // arguments[2] either list of tier names or a list of AnnotationCore objects
        List<AnnotationCore> segmentList = null;

        if (arguments[2] instanceof List<?> arg2) {
            if (arg2.size() > 0) {
                Object first = arg2.get(0);
                if (first instanceof AnnotationCore) {
                    // segments, use the List directly
                    segmentList = (List<AnnotationCore>) arg2;
                } else if (first instanceof String) {
                    // tier names, extract segments using the names in the list
                    segmentList = extractSegments(transcription, (List<String>) arg2);
                }
            }
        }

        if (segmentList == null || segmentList.size() == 0) {
            // log? has been checked before
            return;
        }

        String outputFolder = (String) arguments[3];

        unattendedMode = true;

        Map<String, Long> medMap = new HashMap<String, Long>(transcription.getMediaDescriptors().size());

        for (int i = 0; i < transcription.getMediaDescriptors().size(); i++) {
            if (i > 0 && masterMediaOnly) {
                break;
            }
            MediaDescriptor md = transcription.getMediaDescriptors().get(i);
            String medUrl = processSourceFileName(md.mediaURL);
            medMap.put(medUrl, md.timeOrigin);
        }
        // all clips as one bunch, or make and process a list per media descriptor?
        List<MediaClipper> clippingList = new ArrayList<MediaClipper>();

        for (Map.Entry<String, Long> e : medMap.entrySet()) {
            String mediaSource = e.getKey();
            long offset = e.getValue();

            if (report != null) {
                report.append("Clipping " + segmentList.size() + " segments from " + mediaSource);
            }

            String rawOutputName = outputFolder + File.separator + FileUtility.fileNameFromPath(mediaSource);
            String ext = FileUtility.getExtension(mediaSource);
            String completeOutputName = null;

            for (int i = 0; i < segmentList.size(); i++) {
                AnnotationCore segment = segmentList.get(i);
                if (useAnnotationValueForFileName) {
                    completeOutputName = createDestinationNameFromAnnotation(outputFolder, ext, segment);
                } else {
                    completeOutputName = createDestinationName(rawOutputName,
                                                               segment.getBeginTimeBoundary() + offset,
                                                               segment.getEndTimeBoundary() + offset);
                }

                // begin and end time should be known, fill in the relevant parts of the script
                List<String> defCommand = processCommand(command,
                                                         mediaSource,
                                                         completeOutputName,
                                                         segment.getBeginTimeBoundary() + offset,
                                                         segment.getEndTimeBoundary() + offset);
                defCommand.add(0, executable);

                clippingList.add(new MediaClipper(defCommand));
            }
        }

        ClipRunner clipRunner = new ClipRunner(clippingList);
        clipRunner.start();

        try {
            clipRunner.join();
            if (clipRunner.getErrorMessage() != null) {
                if (unattendedMode) {
                    LOG.warning(clipRunner.getErrorMessage() + "(" + clipRunner.numErrors + ")");
                }
                if (report != null) {
                    report.append("Errors occurred: " + clipRunner.getErrorMessage() + "(" + clipRunner.numErrors + ")");
                }
            }
        } catch (InterruptedException ie) {
            LOG.warning(ElanLocale.getString("Message.Error") + ": " + ie.getMessage());
        }

    }

    /**
     * Extracts time segments from (the annotations of) the given set of tiers.
     *
     * @param transcription the transcription
     * @param tierNames the set of tiers to process
     *
     * @return a list of long arrays
     */
    private List<AnnotationCore> extractSegments(TranscriptionImpl transcription, List<String> tierNames) {
        if (transcription != null && tierNames != null && tierNames.size() > 0) {
            List<AnnotationCore> segments = new ArrayList<AnnotationCore>();
            TierImpl t;
            Annotation a;

            for (String name : tierNames) {
                t = transcription.getTierWithId(name);
                if (t != null) {
                    List<AbstractAnnotation> annotations = t.getAnnotations();
                    final int size = annotations.size();
                    for (int i = 0; i < size; i++) {
                        a = annotations.get(i);
                        // could check for uniqueness, see if the same segment is already in the list?
                        segments.add(a);
                    }
                }
            }

            return segments;
        }

        return null;
    }


}
