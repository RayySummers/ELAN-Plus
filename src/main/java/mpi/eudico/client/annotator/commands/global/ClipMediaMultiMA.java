package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ClipMediaCommand;
import mpi.eudico.client.annotator.commands.ClipMediaMultiCommand;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.ReportDialog;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ClipWithScriptUtil;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AnnotationCoreImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextReader;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.SimpleReport;
import mpi.eudico.util.TimeFormatter;
import nl.mpi.util.FileExtension;
import nl.mpi.util.FileUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.logging.Level;

/**
 * A menu action to initiate clipping of media fragments based on a tab-delimited text file and using the clip-with-script
 * facility.
 *
 * @author Han Sloetjes
 * @version May 2013
 */
@SuppressWarnings("serial")
public class ClipMediaMultiMA extends FrameMenuAction {
    private static final String SCRIPT_FILE_NAME = "clip-media.txt";

    /**
     * Creates a new menu action instance.
     *
     * @param name name of the action
     * @param frame the parent frame
     */
    public ClipMediaMultiMA(String name, ElanFrame2 frame) {
        super(name, frame);

    }

    /**
     * Prompts for a tab-delimited text file or csv file and starts the parsing and clipping process. The action returns
     * immediately in the case of an erroneous situation occurring.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        ClipWithScriptUtil scriptUtil = new ClipWithScriptUtil();
        SimpleReport report = new SimpleReport();
        report.append("Starting multiple file clipping...");
        // check if executable and param line can be extracted
        File scriptFile = scriptUtil.getScriptFile(SCRIPT_FILE_NAME);
        if (scriptFile == null) {
            showWarningMessage("The script file (clip-media.txt) containing the configuration for clipping is not found");
            return;
        }
        report.append("Script file: " + scriptFile.getAbsolutePath());
        String[] scriptLine = scriptUtil.parseScriptLine(scriptFile);

        if (scriptLine == null || scriptLine.length < 2) {
            showWarningMessage(ElanLocale.getString("ClipMedia.Error.Message") + " " + ElanLocale.getString(
                "ClipMedia.Error.Message.NoParameters"));
            return;
        }
        // the executable part of the script
        String executable = scriptLine[0];
        // the parameter part of the script
        String paramLine = scriptLine[1];
        report.append("Executable: " + executable);
        report.append("Parameter part: " + paramLine);

        FileChooser chooser = new FileChooser(frame);
        chooser.createAndShowFileDialog(ElanLocale.getString("Frame.ElanFrame.OpenDialog.Title"),
                                        FileChooser.OPEN_DIALOG,
                                        FileExtension.CSV_EXT,
                                        "LastUsedCSVDir"); // or use txt extension?

        File tabDelFile = chooser.getSelectedFile();
        if (tabDelFile != null) {
            report.append("Input file: " + tabDelFile.getAbsolutePath());
            try {
                DelimitedTextReader reader = new DelimitedTextReader(tabDelFile);
                reader.detectDelimiter();
                int numCol = reader.detectNumColumns();
                if (numCol < 3) { // at least 4 columns are expected
                    showWarningMessage("Too few columns in the tab-delimited text/csv file to perform clipping.");
                    return;
                }
                int[] columns = new int[4];
                columns[0] = 0;
                columns[1] = 1;
                columns[2] = numCol - 2; // if there are only 3 columns, annotation value will be the end time value
                columns[3] = numCol - 1;

                List<String[]> rowDataList = reader.getRowDataForColumns(reader.getFirstRowIndex(), columns);

                if (rowDataList == null || rowDataList.isEmpty()) {
                    showWarningMessage("No row data could be extracted from the file.");
                    return;
                }

                // prompt for output folder
                File outputFolder;
                FileChooser foldChooser = new FileChooser(frame);
                foldChooser.createAndShowFileDialog(ElanLocale.getString("ActivityMonitoringDialog.OpenDialog.Title"),
                                                    FileChooser.OPEN_DIALOG,
                                                    ElanLocale.getString("Button.OK"),
                                                    null,
                                                    null,
                                                    false,
                                                    "LastUsedExportDir",
                                                    FileChooser.DIRECTORIES_ONLY,
                                                    null);

                outputFolder = foldChooser.getSelectedFile();
                if (outputFolder == null) {
                    // report
                    showWarningMessage("No output folder provided.");
                    return;
                } else if (!outputFolder.exists()) {
                    // report
                    showWarningMessage("The provided output folder does not exist.");
                    return;
                }
                report.append("Output folder is: " + outputFolder.getAbsolutePath());

                // store the segments per file before continuing
                Map<String, List<AnnotationCore>> groupedSegments = new HashMap<>();
                // read each line, extract bt, et, annotation, filepath
                for (int i = 0; i < rowDataList.size(); i++) {
                    String[] rowData = rowDataList.get(i);

                    if (rowData == null || rowData.length < 3) {
                        // message or log or report error, then continue.
                        report.append("Encountered a line holding no or invalid information for clipping: " + (i + 1));
                        continue;
                    }
                    long bt = extractTime(rowData[0]);
                    if (bt < 0) {
                        report.append("Invalid begin time on line: " + (i + 1));
                        continue;
                    }
                    long et = extractTime(rowData[1]);
                    if (et <= bt) {
                        report.append("Invalid end time on line: " + (i + 1));
                        continue;
                    }
                    String annValue = rowData[rowData.length - 2].trim();
                    String filePath = rowData[rowData.length - 1].trim();
                    if (filePath.charAt(0) == '"') {
                        filePath = filePath.substring(1);
                    }
                    if (filePath.charAt(filePath.length() - 1) == '"') {
                        filePath = filePath.substring(0, filePath.length() - 1);
                    }

                    if (groupedSegments.containsKey(filePath)) {
                        groupedSegments.get(filePath).add(new AnnotationCoreImpl(annValue, bt, et));
                    } else {
                        List<AnnotationCore> segList = new ArrayList<>();
                        groupedSegments.put(filePath, segList);
                        segList.add(new AnnotationCoreImpl(annValue, bt, et));
                        report.append("Encountered file " + filePath + ". Linked media files will be clipped.");
                    }

                }

                if (groupedSegments.isEmpty()) {
                    // warn
                    showWarningMessage("No information for clipping could be extracted from the text file.");
                    return;
                }

                ClippingProgressThread progressMonitor = new ClippingProgressThread(executable,
                                                                                    paramLine,
                                                                                    outputFolder.getAbsolutePath(),
                                                                                    groupedSegments,
                                                                                    report);
                progressMonitor.start();

            } catch (FileNotFoundException fileNotFoundException) {
                showWarningMessage("The selected file is not found.");
            } catch (IOException ioe) {
                showWarningMessage("Error while reading the file.");
            }
        }
    }

    /**
     * Converts a string to a long value in milliseconds.
     *
     * @param time the time string
     *
     * @return the value in milliseconds or -1 if it couldn't be converted
     */
    private long extractTime(String time) {
        if (time != null) {
            return TimeFormatter.toMilliSeconds(time);
        }

        return -1;
    }

    /**
     * Tries to create a Transcription from a file path.
     *
     * @param path the path to a file, probably an eaf file
     *
     * @return a transcription or null
     */
    private Transcription loadEafFile(String path) {
        if (path != null) {
            path = FileUtility.urlToAbsPath(path);
            // currently the constructor of TranscriptionImpl does not throw an exception
            // therefore check here if the file exists
            File transFile = new File(path);
            if (!transFile.exists()) {
                return null;
            }
            return new TranscriptionImpl(new File(path).getAbsolutePath());
        }
        return null;
    }

    /**
     * Show a (gui) warning message.
     *
     * @param message the message to show
     */
    private void showWarningMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * A thread for monitored progress of the clipping.
     *
     * @author Han Sloetjes
     */
    class ClippingProgressThread extends Thread implements
                                         ClientLogger {
        private ProgressMonitor monitor;
        private final Map<String, List<AnnotationCore>> groupedSegments;
        private final ProcessReport report;
        private final String executable;
        private final String paramLine;
        private final String outputPath;
        private static final int MULTIPLICATION_FACTOR = 10;

        /**
         * Constructor
         */
        ClippingProgressThread(String executable, String paramLine, String outputPath,
                               Map<String, List<AnnotationCore>> groupedSegments, ProcessReport report) {
            this.executable = executable;
            this.paramLine = paramLine;
            this.outputPath = outputPath;
            this.groupedSegments = groupedSegments;
            this.report = report;
        }

        /**
         * Processes the clipping by calling the ClipMediaMultiCommand for the segments of every eaf file in the map.
         */
        @Override
        public void run() {
            Iterator<String> groupIterator = groupedSegments.keySet().iterator();
            int max = groupedSegments.size() * MULTIPLICATION_FACTOR + 2;
            int progress = 0;
            int fileCount = 0;

            JLabel message = new JLabel("Clipping progress");
            message.setMinimumSize(new Dimension(400, message.getMinimumSize().height));
            message.setPreferredSize(new Dimension(400, message.getMinimumSize().height));
            monitor = new ProgressMonitor(frame, message, "Preparing clipping...", 0, max);
            monitor.setMillisToDecideToPopup(10);
            monitor.setMillisToPopup(50);
            // allow the monitor to pop up
            for (int i = 0; i < 2; i++) {
                progress = i;
                monitor.setProgress(i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    LOG.log(Level.WARNING, "Thread got interrupted.", ie);
                }
            }

            while (groupIterator.hasNext()) {
                if (monitor.isCanceled()) {
                    report.append("The process has been canceled after: " + fileCount + " transcription files.");
                    monitor.close();
                    break;
                }
                String curEafPath = groupIterator.next();
                progress += MULTIPLICATION_FACTOR;
                monitor.setNote("Processing clips based on eaf file: " + FileUtility.fileNameFromPath(curEafPath));

                // parse eaf
                Transcription trans = loadEafFile(curEafPath);
                fileCount++;

                if (trans == null) {
                    report.append("Unable to load the .eaf file: "
                                  + curEafPath
                                  + ". The linked media files cannot be retrieved and are skipped.");
                    monitor.setProgress(progress);
                    continue;
                }
                // clip all segments of the media of this transcription (take preferences into account)
                // create command and execute
                ClipMediaCommand command = new ClipMediaMultiCommand("ClipMulti"); // localize?
                List<AnnotationCore> curSegments = groupedSegments.get(curEafPath);

                command.execute(trans, new Object[] {executable, paramLine, curSegments, outputPath, report});
                // in principle execute should return after all clipping has finished, but this depends on the
                // application called to do the clipping, otherwise the system can run out of memory
                report.append("Processing of " + curEafPath + " done. (But the actual clipping might still continue.)");
                monitor.setProgress(progress);
            }
            monitor.setProgress(monitor.getMaximum()); // ensure the monitor disappears

            // show report
            ReportDialog repDialog = new ReportDialog(frame, report);
            repDialog.setVisible(true);
        }
    }
}
