package mpi.eudico.client.annotator.export.multiplefiles;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.util.AnnotationCoreComparator;
import nl.mpi.util.FileExtension;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.UTF_8;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * Last step of this export function, the actual calculation and saving of the results.
 *
 * @see MFExportAnnotatedPartStep1
 */
@SuppressWarnings("serial")
public class MFExportAnnotatedPartStep3 extends ProgressStepPane {
    private List<String> files;
    /**
     * list of selected tiers
     */
    protected List<String> selectedTiers;
    private final AnnotationCoreComparator comparator = new AnnotationCoreComparator();
    // out, could use a SimpleReport instead maybe
    private final List<String> outputList = new ArrayList<String>();
    // the number of processed files
    private int totalNumFiles;
    // the total number of inspected tiers
    private int totalNumTiers;
    // the total number of empty (inspected) tiers
    private int totalNumEmptyTiers;
    // the total number of tiers that were not present or were dependent tiers
    private int totalNumNotProcessedTiers;
    // the total number of annotations on inspected tiers
    private int totalNumAnnotations;
    // the total number of empty annotations on inspected tiers
    private int totalNumEmptyAnnotations;
    // the total duration of all annotated segments
    private long totalSegmentsDuration = 0;
    // the total duration of empty annotations (i.e. before merging of segments)
    private long totalEmptyAnnosDuration = 0;
    // the total duration of non-empty annotations (i.e. before merging of segments)
    private long totalNonEmptyAnnosDuration = 0;
    // the total duration of the "last annotated time" in all files
    private long totalLastTimeDuration = 0;
    // the total number of files which could not be processed
    private int numFailedFiles;
    // name; total duration non-empty annotations; total duration empty annotations;
    // total annotated part seconds; last annotation time seconds; annotated part as a percentage of last annotation
    // time
    // num annotations; num empty annotations; num processed tiers; num empty tiers; num not-processed tiers
    private final String formatLine = "%s\t%,.3f\t%,.3f\t%,.3f\t%,.3f\t%.2f\t%d\t%d\t%d\t%d\t%d\n";

    /**
     * Creates the panel for the final step of the  export.
     *
     * @param multiPane the parent pane
     */
    public MFExportAnnotatedPartStep3(MultiStepPane multiPane) {
        super(multiPane);
        initComponents();
    }

    @Override
    public void enterStepForward() {
        doFinish();
    }

    /**
     * Disables all buttons, starts the export in a separate thread and returns {@code false}.
     *
     * @return {@code false}, unless an error was detected before starting the actual computation
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean doFinish() {
        // getProperties
        files = (List<String>) multiPane.getStepProperty("OpenedFiles");
        selectedTiers = (List<String>) multiPane.getStepProperty("SelectedTiers");
        completed = false;

        // disable buttons
        multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);
        // error conditions should not occur
        if (files == null || files.size() == 0) {
            // show message and stop
            return true;
        }
        if (selectedTiers == null || selectedTiers.size() == 0) {
            // show message and stop
            return true;
        }

        Thread t = new ExportThread();
        t.start();

        // the action is performed on a separate thread, don't close
        return false;
    }

    /**
     * This export collects information from multiple files and stores that at the end in a single export action.
     *
     * @param transImpl a loaded transcription
     */
    protected void doExport(TranscriptionImpl transImpl) {
        int numTiers = 0;
        int numEmptyTiers = 0;
        int numAbsentTiers = 0;
        int numDepTiers = 0;
        int numAnnos = 0;
        int numEmptyAnnos = 0;
        long totalDur = 0;
        long totalEmptyDur = 0;
        long totalNonEmptyDur = 0;
        List<AbstractAnnotation> allAnnotations = new ArrayList<AbstractAnnotation>();

        for (String tierName : selectedTiers) {
            // check if the tier is a top-level tier, ignore if not
            TierImpl t = transImpl.getTierWithId(tierName);
            if (t == null) {
                numAbsentTiers++;
                continue;
            }
            if (t.getParentTier() != null) {
                numDepTiers++;
                continue;
            }
            numTiers++;
            if (t.getNumberOfAnnotations() == 0) {
                numEmptyTiers++;
            }

            allAnnotations.addAll(t.getAnnotations());
        }
        numAnnos = allAnnotations.size();
        if (numAnnos > 0) {
            //sort annotations
            Collections.sort(allAnnotations, comparator);
            // could use a mutable TimeInterval
            List<long[]> mergList = new ArrayList<long[]>();
            for (AbstractAnnotation a : allAnnotations) {
                // check empty
                if (a.getValue().strip().length() == 0) {
                    numEmptyAnnos++;
                    totalEmptyDur += (a.getEndTimeBoundary() - a.getBeginTimeBoundary());
                } else {
                    totalNonEmptyDur += (a.getEndTimeBoundary() - a.getBeginTimeBoundary());
                }

                // check overlap
                if (mergList.size() == 0) {
                    mergList.add(new long[] {a.getBeginTimeBoundary(), a.getEndTimeBoundary()});
                    continue;
                }
                long[] last = mergList.get(mergList.size() - 1);
                // no overlap
                // |----|
                //       |--|
                if (a.getBeginTimeBoundary() > last[1]) {
                    mergList.add(new long[] {a.getBeginTimeBoundary(), a.getEndTimeBoundary()});
                    continue;
                }
                // overlap, no need to check (a.begin >= last[0] && a.begin <= last[1]), e.g.
                // |----|
                //     |------|
                if (a.getEndTimeBoundary() > last[1]) {
                    last[1] = a.getEndTimeBoundary();
                }
            }
            // calculate total extent
            for (long[] interval : mergList) {
                totalDur += (interval[1] - interval[0]);
            }
        }
        // add result line
        outputList.add(String.format(formatLine,
                                     transImpl.getName(),
                                     (float) totalNonEmptyDur / 1000f,
                                     (float) totalEmptyDur / 1000f,
                                     //TimeFormatter.toSSMSString(totalDur),
                                     (float) totalDur / 1000f,
                                     //TimeFormatter.toSSMSString(transImpl.getLatestTime()),
                                     (float) transImpl.getLatestTime() / 1000f,
                                     transImpl.getLatestTime() == 0
                                     ? 0.0f
                                     : (float) (totalDur * 100) / (float) transImpl.getLatestTime(),
                                     numAnnos,
                                     numEmptyAnnos,
                                     numTiers,
                                     numEmptyTiers,
                                     numAbsentTiers + numDepTiers));
        // add totals
        totalNumTiers += numTiers;
        totalNumEmptyTiers += numEmptyTiers;
        totalNumNotProcessedTiers += (numAbsentTiers + numDepTiers);
        totalNumAnnotations += numAnnos;
        totalNumEmptyAnnotations += numEmptyAnnos;
        totalSegmentsDuration += totalDur;
        totalLastTimeDuration += transImpl.getLatestTime();
        totalEmptyAnnosDuration += totalEmptyDur;
        totalNonEmptyAnnosDuration += totalNonEmptyDur;
    }

    private void writeResults() {
        if (!outputList.isEmpty()) {
            File f = getOutputFile();
            if (f != null) {
                // try-with-resources
                try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),
                                                                                                    UTF_8)))) {
                    // write header
                    writer.write("File name \tTotal duration non-empty annotations (seconds)"
                                 + "\tTotal duration empty annotations (seconds) \tTotal annotated part (seconds)"
                                 + "\tLast annotation time (seconds) \tTotal as a percentage of last time "
                                 + "\tNumber of annotations"
                                 + "\tNumber of empty annotations \tNumber of processed tiers"
                                 + "\tNumber of empty tiers \tNumber of tiers not processed");
                    writer.write("\n\n");
                    for (String s : outputList) {
                        writer.write(s);
                    }

                    writer.write("\nNumber of files processed: " + totalNumFiles);
                    writer.write("\nNumber of files not processed: " + numFailedFiles);

                } catch (IOException ioe) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.log(Level.WARNING, "Error saving the file: " + ioe.getMessage());
                    }
                    showWarningDialog("An error occurred while saving the file: " + ioe.getMessage());
                }
            } else {
                // show message
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "No output file selected, closing the dialog without saving the results");
                }
                showMessageDialog("No output file selected, results are not saved");
            }
        } else {
            // show message
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, "There are no results to save, 0 files have been processed");
            }
            showMessageDialog("There are no results to save, no files have been processed.");
        }
        progressCompleted(this, "Export complete");
    }

    /**
     * Returns the selected file object or {@code null}.
     *
     * @return the selected file object or {@code null}
     */
    private File getOutputFile() {
        FileChooser fc = new FileChooser(this);
        fc.createAndShowFileDialog(ElanLocale.getString(""),
                                   FileChooser.SAVE_DIALOG,
                                   FileExtension.TEXT_EXT,
                                   "LastUsedExportDir");

        return fc.getSelectedFile();
    }

    /**
     * A thread to loop over files and tiers and update the progress monitor.
     */
    private class ExportThread extends Thread {

        /**
         * Constructor.
         */
        public ExportThread() {
        }

        @Override
        public void run() {
            // iterate files
            for (int f = 0; f < files.size(); f++) {
                String fileName = files.get(f);

                if (fileName == null) {
                    continue;
                }

                try {
                    doExport(new TranscriptionImpl(fileName));
                } catch (Exception ex) {
                    // catch any exception that could occur and continue
                    numFailedFiles++;
                    LOG.warning("Could not handle file: " + fileName);
                }
                totalNumFiles++;
                //update progress bar
                if ((f + 1) < files.size()) {
                    progressUpdated(MFExportAnnotatedPartStep3.this, Math.round(100 * (f + 1) / (float) files.size()), null);
                }
            }
            // add final line for the totals
            // add result line
            outputList.add(String.format(formatLine,
                                         "\nTotal",
                                         (float) totalNonEmptyAnnosDuration / 1000f,
                                         (float) totalEmptyAnnosDuration / 1000f,
                                         //TimeFormatter.toSSMSString(totalSegmentsDuration),
                                         (float) totalSegmentsDuration / 1000f,
                                         //TimeFormatter.toSSMSString(totalLastTimeDuration),
                                         (float) totalLastTimeDuration / 1000f,
                                         totalLastTimeDuration == 0
                                         ? 0.0f
                                         : (float) (totalSegmentsDuration * 100) / (float) totalLastTimeDuration,
                                         totalNumAnnotations,
                                         totalNumEmptyAnnotations,
                                         totalNumTiers,
                                         totalNumEmptyTiers,
                                         totalNumNotProcessedTiers));
            // after the loop show a save as dialog
            writeResults();
        }

    }
}
