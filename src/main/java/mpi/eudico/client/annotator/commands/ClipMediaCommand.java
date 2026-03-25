package mpi.eudico.client.annotator.commands;


import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clomimpl.abstr.AnnotationCoreImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.util.TimeFormatter;
import nl.mpi.util.FileUtility;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A Command to export a clip of one or more media files by means of a script. The script should specify the application to
 * call and the parameters and should contain some placeholders for filenames and time information to be filled in.
 */
public class ClipMediaCommand implements Command {
    /**
     * in file constant
     */
    public static final String IN_FILE = "$in_file";
    /**
     * out time constant
     */
    public static final String OUT_FILE = "$out_file";
    /**
     * begin time constant
     */
    public static final String BEGIN_TIME = "$begin";
    /**
     * end time constant
     */
    public static final String END_TIME = "$end";
    /**
     * duration constant
     */
    public static final String DUR = "$duration";

    /**
     * hh time foramt constant
     */
    public static final String HH = "hour:min:sec.ms";
    /**
     * hhff time format  constant
     */
    public static final String HHFF = "hour:min:sec:fr";
    /**
     * hhff ntsc time constant
     */
    public static final String HHFF_NTSC = "hour:min:sec:fr_NTSC";
    /**
     * sec time format constant
     */
    public static final String SEC = "sec.ms";
    /**
     * ms time format constant
     */
    public static final String MS = "ms";
    /**
     * fr time constant
     */
    public static final String FR = "fr";
    /**
     * fr ntsc time constant
     */
    public static final String FR_NTSC = "fr_NTSC";

    static final String[] FORMATS = {SEC, MS, HH, FR, HHFF, HHFF_NTSC, FR_NTSC};

    private final String commandName;

    // global flags
    boolean asynchronous = false; // default: wait for the process to end
    boolean unattendedMode = false; //pop up error messages by default
    boolean masterMediaOnly = false;
    boolean promptForFile = true;
    boolean useAnnotationValueForFileName = false;
    /**
     * process report
     */
    protected ProcessReport report;

    /**
     * Creates a new ClipMediaCommand instance
     *
     * @param theName name of the command
     */
    public ClipMediaCommand(String theName) {
        commandName = theName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct. There are two ways to use this command: a) by passing a ViewerManager object; begin and end time of the clip
     * will be retrieved from the Selection object (and the user will be prompted for a file name?) b) by passing a media
     * path, a begin and an end time. The output file name will be based on input filename. Useful for batch operation.
     *
     * @param receiver null
     * @param arguments the arguments:
     *     <ul>
     *         <li>arg[0] = the Viewer Manager (ViewerManager2) or the media path (String)</li>
     *         <li>arg[1] = either the executable part  of the script,
     *             or an Exception if there is no script file (String or Exception)</li>
     *         <li>arg[2] = the command part of the script (String)</li>
     *         <li>arg[i] = (optional) the destination folder for the new media clips (String)</li>
     *         <li>arg[i] = (optional) the begin time of the segment (Long)</li>
     *         <li>arg[i+1] = (optional) the end time of the segment to clip (Long)</li>
     *         <li>arg[i+1] = (optional) the offset into the media (Long)</li>
     *         <li>arg[last] = (present) a flag to indicate unattended mode (Boolean)</li>
     *     </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        String executable = null;
        String command = "";
        long timeBegin = -1L;
        long timeEnd = -1L;
        String sourceFileName = null;
        String mainDestFile = null;
        String outFilePath = null;

        // if the last argument is  a boolean use it as a flag for unattended mode
        if (arguments.length > 0 && arguments[arguments.length - 1] instanceof Boolean) {
            unattendedMode = ((Boolean) arguments[arguments.length - 1]).booleanValue();
        }

        readPrefs();

        if (arguments[0] instanceof ViewerManager2 viewerManager) {

            if (arguments.length > 1 && arguments[1] instanceof Exception) {
                if (unattendedMode) {
                    LOG.warning(ElanLocale.getString("ClipMedia.Error.Message")
                                + " "
                                + ((Exception) arguments[1]).getMessage());
                } else {
                    // show message
                    showErrorMessage(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()),
                                     ((Exception) arguments[1]).getMessage());
                }
                return;
            }

            if (arguments.length > 1) {
                executable = (String) arguments[1];
                if (arguments.length > 2) {
                    command = (String) arguments[2];
                } else {
                    if (unattendedMode) {
                        // no executable or no command or parameter line? return
                        LOG.warning(ElanLocale.getString("ClipMedia.Error.Message") + " " + ElanLocale.getString(
                            "ClipMedia.Error.Message.NoParameters"));
                    } else {
                        showErrorMessage(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()),
                                         ElanLocale.getString("ClipMedia.Error.Message") + " " + ElanLocale.getString(
                                             "ClipMedia.Error.Message.NoParameters"));
                    }
                    return;
                }

                int argIndex = 3;

                if (arguments.length > argIndex && arguments[argIndex] instanceof String) {
                    outFilePath = (String) arguments[argIndex];
                    argIndex++;
                    // replace (back)slashes to the system default
                    if (File.separatorChar == '/') {
                        outFilePath = outFilePath.replace('\\', File.separatorChar);
                    } else {
                        outFilePath = outFilePath.replace('/', File.separatorChar);
                    }

                    if (!outFilePath.endsWith(File.separator)) {
                        outFilePath = outFilePath + File.separator;
                    }
                }

                if (arguments.length > argIndex && arguments[argIndex] instanceof Long) {
                    timeBegin = ((Long) arguments[argIndex]).longValue();
                    argIndex++;
                }

                if (arguments.length > argIndex && arguments[argIndex] instanceof Long) {
                    timeEnd = ((Long) arguments[argIndex]).longValue();
                }

                String annotValue = null;
                if (useAnnotationValueForFileName) {
                    if (viewerManager.getActiveAnnotation().getAnnotation() != null) {
                        annotValue = viewerManager.getActiveAnnotation().getAnnotation().getValue();
                        if (annotValue.isEmpty()) {
                            annotValue = null;
                        }
                    }
                }

                Selection selection = viewerManager.getSelection();
                if (selection != null && timeBegin == -1) {
                    // assume a check has been performed on begin and end time
                    timeBegin = selection.getBeginTime();
                    timeEnd = selection.getEndTime();
                    // the clip is saved with a name based on source name and selection
                    // unless user settings demand otherwise

                    if (promptForFile && outFilePath == null) {
                        FileChooser chooser =
                            new FileChooser(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()));
                        chooser.createAndShowFileDialog(null,
                                                        FileChooser.SAVE_DIALOG,
                                                        null,
                                                        null,
                                                        "MediaClipDir",
                                                        annotValue);

                        File selFile = chooser.getSelectedFile();
                        if (selFile != null) {
                            mainDestFile = selFile.toString();
                        } else {
                            return;
                        }
                    }
                }

                // loop over all media players..., start with master
                // check for audio or video?
                List<MediaDescriptor> players = viewerManager.getTranscription().getMediaDescriptors();
                int numPl = players.size();
                List<MediaClipper> clipThreads = new ArrayList<MediaClipper>(numPl);

                for (int i = 0; i < numPl; i++) {
                    if (i > 0 && masterMediaOnly) {
                        break;
                    }
                    MediaDescriptor md = players.get(i);
                    sourceFileName = processSourceFileName(md.mediaURL);
                    // sourceFileName = md.mediaURL.substring(5);
                    long offset = md.timeOrigin;

                    String destFileName = null;
                    if (outFilePath != null) {
                        String fileName = FileUtility.fileNameFromPath(sourceFileName);
                        if (fileName != null) {
                            destFileName = outFilePath + fileName;
                        }
                    } else {
                        destFileName = sourceFileName;
                    }

                    if (mainDestFile != null) {
                        destFileName = createDestinationName(mainDestFile, i, sourceFileName);
                    } else {
                        // there has been no prompt, destFileName is sourceFileName or other full path including the
                        // media file name
                        if (useAnnotationValueForFileName && annotValue != null) {
                            destFileName = createDestinationNameFromAnnotation(sourceFileName,
                                                                               i,
                                                                               new AnnotationCoreImpl(annotValue,
                                                                                                      timeBegin + offset,
                                                                                                      timeEnd + offset));
                        } else {
                            destFileName = createDestinationName(destFileName, timeBegin + offset, timeEnd + offset);
                        }
                    }

                    // begin and end time should be known, fill in the relevant parts of the script
                    List<String> defCommand =
                        processCommand(command, sourceFileName, destFileName, timeBegin + offset, timeEnd + offset);
                    defCommand.add(0, executable);

                    clipThreads.add(new MediaClipper(defCommand));
                }
                ClipRunner clipRunner = new ClipRunner(clipThreads);
                clipRunner.start();

                try {
                    clipRunner.join();
                    if (clipRunner.getErrorMessage() != null) {
                        if (unattendedMode) {
                            LOG.warning(clipRunner.getErrorMessage() + "(" + clipRunner.numErrors + ")");
                        } else {
                            showErrorMessage(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()),
                                             clipRunner.getErrorMessage() + "(" + clipRunner.numErrors + ")");
                        }
                    }
                } catch (InterruptedException ie) {
                    LOG.warning(ElanLocale.getString("Message.Error") + ": " + ie.getMessage());
                }
            }
        } else if (arguments[0] instanceof String) { // a media path has been provided
            // not fully implemented
            // new preferences (promptForFileName, onlyClipFirstMedia,...)  and
            // other arguments (location to store the clip) are yet to be updated
            sourceFileName = (String) arguments[0];
            long offset = 0L;
            if (arguments.length > 1 && arguments[1] instanceof Exception) {
                if (unattendedMode) {
                    // log message
                    LOG.warning(ElanLocale.getString("ClipMedia.Error.Message")
                                + " "
                                + ((Exception) arguments[1]).getMessage());
                } else {
                    showErrorMessage(null,
                                     ElanLocale.getString("ClipMedia.Error.Message")
                                     + " "
                                     + ((Exception) arguments[1]).getMessage());
                }
                return;
            }
            if (arguments.length > 1) {
                executable = (String) arguments[1];
                if (arguments.length > 2) {
                    command = (String) arguments[2];
                } else {
                    if (unattendedMode) {
                        // no executable or no command or parameter line? return
                        LOG.warning(ElanLocale.getString("ClipMedia.Error.Message") + " " + ElanLocale.getString(
                            "ClipMedia.Error.Message.NoParameters"));
                    } else {
                        showErrorMessage(null,
                                         ElanLocale.getString("ClipMedia.Error.Message") + " " + ElanLocale.getString(
                                             "ClipMedia.Error.Message.NoParameters"));
                    }
                    return;
                }
                if (arguments.length > 3 && arguments[3] instanceof Long) {
                    timeBegin = ((Long) arguments[3]).longValue();
                }
                if (arguments.length > 4 && arguments[4] instanceof Long) {
                    timeEnd = ((Long) arguments[4]).longValue();
                }
                if (arguments.length > 5 && arguments[5] instanceof Long) {
                    offset = ((Long) arguments[5]).longValue();
                }

                if (sourceFileName.startsWith("file:")) {
                    sourceFileName = sourceFileName.substring(5);
                }
                if (sourceFileName.startsWith("///")) {
                    sourceFileName = sourceFileName.substring(3);
                }
                // replace (back)slashes in the sourcefiles to the system default
                if (File.separatorChar == '/') {
                    sourceFileName = sourceFileName.replace('\\', File.separatorChar);
                } else {
                    sourceFileName = sourceFileName.replace('/', File.separatorChar);
                }
                String destFileName = createDestinationName(sourceFileName, timeBegin + offset, timeEnd + offset);
                // begin and end time should be known, fill in the relevant parts of the script
                List<String> defCommand =
                    processCommand(command, sourceFileName, destFileName, timeBegin + offset, timeEnd + offset);
                defCommand.add(0, executable);

                new MediaClipper(defCommand).start();
                // TODO check error message
            }
        }
    }

    /**
     * Replaces keywords in the command by paths and or time values.
     *
     * @param command the command part of the script
     * @param timeBegin begin time
     * @param timeEnd end time
     *
     * @return the final command
     */
    List<String> processCommand(String command, String inFile, String outFile, long timeBegin, long timeEnd) {
        if (inFile == null || outFile == null) {
            return null;
        }

        Pattern pat = Pattern.compile(" ");
        String[] parts = pat.split(command);
        List<String> partList = new ArrayList<String>(parts.length);
        Collections.addAll(partList, parts);

        int from = 0;
        int to = 0;
        String sub;
        String replSub;

        for (int i = 0; i < partList.size(); i++) {
            sub = partList.get(i);
            if (sub.isEmpty()) {
                continue;
            }

            if (sub.contains(BEGIN_TIME)) {
                partList.set(i, replaceTime(sub, BEGIN_TIME, timeBegin));
            } else if (sub.contains(END_TIME)) {
                partList.set(i, replaceTime(sub, END_TIME, timeEnd));
            } else if (sub.contains(DUR)) {
                partList.set(i, replaceTime(sub, DUR, timeEnd - timeBegin));
            } else if ((from = sub.indexOf(IN_FILE)) > -1) {
                to = from + IN_FILE.length();
                replSub = sub.substring(0, from) + inFile + sub.substring(to);
                partList.set(i, replSub);
            } else if ((from = sub.indexOf(OUT_FILE)) > -1) {
                to = from + OUT_FILE.length();
                replSub = sub.substring(0, from) + outFile + sub.substring(to);
                partList.set(i, replSub);
            }
        }

        for (int i = partList.size() - 1; i >= 0; i--) {
            sub = partList.get(i);
            if (sub == null || sub.isEmpty()) {
                partList.remove(i);
            }
        }

        return partList;
    }

    /**
     * @param part one of the placeholders in the script for begin time, end time or duration.
     * @param key the begin time, end time or duration identifier
     * @param time the time to insert in the format as parsed from the part
     *
     * @return a formatted time value string
     */
    private String replaceTime(String part, String key, long time) {
        int from = part.indexOf(key);
        int from2 = part.indexOf("(", from) + 1;
        int to = part.indexOf(")", from2);
        String format = part.substring(from2, to);
        for (String f : FORMATS) {
            if (format.equals(f)) {
                String repl = part.substring(0, from) + toTimeString(f, time);
                if (to + 1 < part.length()) {
                    repl += part.substring(to + 1);
                }
                return repl;
            }
        }
        return "";
    }

    /**
     * Transforms a time value into a string, in the specified format. Note: the format is checked using the "==" parameter,
     * the format should be one of the time format constants!
     *
     * @param format the time format one of FORMATS
     * @param time the time value
     *
     * @return a formatted time string
     */
    private String toTimeString(String format, long time) {
        if (SEC.equals(format)) {
            return TimeFormatter.toSSMSString(time);
        }
        if (MS.equals(format)) {
            return String.valueOf(time);
        }
        if (HH.equals(format)) {
            return TimeFormatter.toString(time);
        }
        if (HHFF.equals(format)) {
            return TimeFormatter.toTimecodePAL(time);
        }
        if (HHFF_NTSC.equals(format)) {
            return TimeFormatter.toTimecodeNTSC(time);
        }
        if (FR.equals(format)) {
            return TimeFormatter.toFrameNumberPAL(time);
        }
        if (FR_NTSC.equals(format)) {
            return TimeFormatter.toFrameNumberNTSC(time);
        }

        return String.valueOf(time);
    }

    /**
     * Converts a media url to a platform dependent file name.
     *
     * @param sourceUrl the source url
     *
     * @return the platform specific file name
     */
    String processSourceFileName(String sourceUrl) {
        String sourceFileName = sourceUrl.substring(5);

        if (sourceFileName.startsWith("///")) {
            if (SystemReporting.isWindows()) {
                sourceFileName = sourceFileName.substring(3);
            } else {
                //sourceFileName = sourceFileName.substring(2); // ??
            }
        }
        // replace (back)slashes in the source files to the system default
        if (File.separatorChar == '/') {
            sourceFileName = sourceFileName.replace('\\', File.separatorChar);
        } else {
            sourceFileName = sourceFileName.replace('/', File.separatorChar);
        }

        return sourceFileName;
    }

    /**
     * Constructs a destination name based on source name and a general destination name.
     *
     * @param sourceFileName the name of the source file
     * @param destFile the general name for destination files
     * @param i the index in the list of players
     * @return the destination file name
     */
    /*
    private String createDestinationName(String sourceFileName, String destFile, int i) {
        int si = sourceFileName.lastIndexOf(".");
        int di = destFile.lastIndexOf(".");
        if (si > -1 && di > -1) {
            // keep the source's extension ?
            if (i == 0) {
                return destFile.substring(0, di) + sourceFileName.substring(si);
            } else {
                return destFile.substring(0, di) + "_" + i + sourceFileName.substring(si);
            }
        } else if (si > -1) {
            if (i == 0) {
                return destFile + sourceFileName.substring(si);
            } else {
                return destFile + "_" + i + sourceFileName.substring(si);
            }
        }
        return destFile;
    }
    */

    /**
     * Constructs and returns a destination file name.
     *
     * @param sourceFileName the complete path to a file
     * @param begin the begin time
     * @param end the end time
     *
     * @return the destination of form: source_begin_end.extension
     */
    public String createDestinationName(String sourceFileName, long begin, long end) {
        int si = sourceFileName.lastIndexOf(".");
        if (si > -1) {
            return sourceFileName.substring(0, si) + "_" + begin + "_" + end + sourceFileName.substring(si);
        } else {
            return sourceFileName + "_" + begin + "_" + end;
        }
    }

    /**
     * Creates an output file name.
     *
     * @param mainDestFile the main output file path
     * @param i the index of the media file in the list of media descriptors
     * @param sourceName the original file name, used for the extension if the destination has no extension specified
     *
     * @return returns a file name with an index number inserted.
     */
    public String createDestinationName(String mainDestFile, int i, String sourceName) {

        int stopIndex = mainDestFile.lastIndexOf('.');
        if (stopIndex > -1) {
            if (i > 0) {
                return mainDestFile.substring(0, stopIndex) + "_" + (i) + mainDestFile.substring(stopIndex);
            } //else return mainDestFile;

        } else {
            if (sourceName != null) {
                int sourceStop = sourceName.lastIndexOf('.');
                if (sourceStop > -1 && sourceStop < sourceName.length() - 1) {
                    if (i > 0) {
                        return mainDestFile + "_" + (i) + sourceName.substring(sourceStop);
                    } else {
                        return mainDestFile + sourceName.substring(sourceStop);
                    }
                }
            }

            if (i > 0) {
                return mainDestFile + "_" + (i);
            } //else return mainDestFile;
        }

        return mainDestFile;
    }

    /**
     * Creates a file name and path based on the annotation value.
     *
     * @param baseOutputName the output folder path, not null
     * @param mediaFileExt the file extension
     * @param annotation the annotation core holding boundaries and annotation value
     *
     * @return the computed output file path and name
     */
    public String createDestinationNameFromAnnotation(String baseOutputName, String mediaFileExt,
                                                      AnnotationCore annotation) {
        if (annotation.getValue() != null && !annotation.getValue().isEmpty()) {
            return baseOutputName
                   + File.separator
                   + annotation.getValue().replaceAll("[?:;\\\\/]", "_")
                   + "."
                   + mediaFileExt;
        } else {
            return baseOutputName
                   + File.separator
                   + "no_value_"
                   + annotation.getBeginTimeBoundary()
                   + "_"
                   + annotation.getEndTimeBoundary()
                   + "."
                   + mediaFileExt;
        }
    }

    /**
     * Creates an output file name based on the source filename, an annotation value and an index.
     *
     * @param sourcePath the full path of the (source) media file
     * @param i the index of the file in the list of linked media
     * @param annotation the annotation core holding the annotation value
     *
     * @return a computed output file path
     */
    public String createDestinationNameFromAnnotation(String sourcePath, int i, AnnotationCore annotation) {
        int stopIndex = sourcePath.lastIndexOf('.');
        int slashIndex = sourcePath.lastIndexOf(File.separatorChar);
        String value = annotation.getValue();
        if (value == null || value.isEmpty()) {
            value = "no_value";
        }

        String index = i > 0 ? String.valueOf(i) : "";
        if (slashIndex > -1) {
            if (stopIndex > slashIndex) {
                return sourcePath.substring(0, slashIndex + 1) + value + "_" + index + sourcePath.substring(stopIndex);
            } else {
                return sourcePath.substring(0, slashIndex + 1) + value + "_" + index;
            }
        } else {
            if (stopIndex > 0) {
                return value + "_" + index + sourcePath.substring(stopIndex);
            } else {
                return value + "_" + index;
            }
        }
    }

    @Override
    public String getName() {
        return commandName;
    }

    /**
     * Shows an error message on screen.
     *
     * @param frame the parent frame, can be null
     * @param message the message to display
     */
    private void showErrorMessage(Frame frame, String message) {
        JOptionPane.showMessageDialog(frame, message, ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Read user preferences concerning clipping.
     */
    protected void readPrefs() {
        Boolean promptObj = Preferences.getBool("Media.PromptForFilename", null);
        if (promptObj != null) {
            promptForFile = promptObj;
        }

        Boolean mmOnlyObj = Preferences.getBool("Media.OnlyClipFirstMediaFile", null);
        if (mmOnlyObj != null) {
            masterMediaOnly = mmOnlyObj;
        }

        Boolean runAsync = Preferences.getBool("Media.ClipInParallel", null);
        if (runAsync != null) {
            asynchronous = runAsync;
        }

        Boolean annValueForNameObj = Preferences.getBool("Media.Clip.UseAnnotationForFileName", null);
        if (annValueForNameObj != null) {
            useAnnotationValueForFileName = annValueForNameObj;
        }
    }

    /**
     * A thread that creates a process and starts it. A reader is created to capture response messages from the called
     * application.
     *
     * @author Han
     */
    class MediaClipper extends Thread {
        private final List<String> command;
        // TODO store error message?
        private String errorMessage = null;

        /**
         * @param command the list of commands (executable and parameters)
         */
        public MediaClipper(List<String> command) {
            super();
            this.command = command;
        }

        /**
         * Error message getter.
         *
         * @return an error message or null.
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public void run() {
            if (command == null) {
                return;
            }
            try {
                //System.out.println("Start process... " + command.hashCode());
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                long startTime = System.currentTimeMillis();
                //System.out.println("Start process... Time: " + System.currentTimeMillis());
                //System.out.println("Start... reader " + reader.hashCode());
                ReaderThread rt =
                    new ReaderThread(new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8)));
                rt.start();

                if (!asynchronous) {

                    // some processes (e.g. ffmpeg) seem to return an exit value while the
                    // underlying application is still running
                    try {
                        //System.out.println("Start wait...");
                        // in Java 1.8 it is possible to specify a timeout for the waiting
                        int exit = process.waitFor();
                        //int exit = process.exitValue();
                        //System.out.println("Exit: " + exit);

                        //rt.interrupt();
                        // if the process returns within 2 seconds assume that this is inaccurate
                        // and wait (a random) 5 seconds
                        if (System.currentTimeMillis() - startTime < 2000) {
                            //System.out.println("Forcing a wait...");
                            while (System.currentTimeMillis() - startTime < 5000) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ie) {
                                    break;
                                }
                            }
                        }
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }

            } catch (IOException ioe) {
                errorMessage = ElanLocale.getString("ClipMedia.Error.Message") + " " + ioe.getMessage();
                LOG.warning(errorMessage);
            } catch (SecurityException se) {
                errorMessage = ElanLocale.getString("ClipMedia.Error.Message.Security") + " " + se.getMessage();
                LOG.warning(errorMessage);
            }
        }
    }

    /**
     * A thread containing a reader that captures the output messages from an application that is called to clip a media
     * file. Contains a hack to ensure that the reader is closed after some time. Waiting for the process to exit doesn't
     * always work, because sometime the process seems to exit before the underlying application is ready.
     *
     * @author Han
     */
    class ReaderThread extends Thread {
        private final BufferedReader reader;
        private long startTime;
        private static final int MAX_READ_TIME = 2 * 60 * 1000; // 2 minutes

        /**
         * Constructor.
         *
         * @param reader the buffered reader for a clipping process
         */
        ReaderThread(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            startTime = System.currentTimeMillis();
            //System.out.println("Reader started... " + reader.hashCode());
            while (reader != null && !isInterrupted()) {
                try {
                    //System.out.println("Try read... " + hashCode());
                    if (reader.ready()) {
                        String line = reader.readLine();
                        LOG.info(line);
                        if (report != null) {
                            report.append(line);
                        }
                    } else {
                        //System.out.println("Not ready... " + hashCode());
                        if (System.currentTimeMillis() - startTime > MAX_READ_TIME) {
                            break;
                        }
                    }
                } catch (IOException ioe) {
                    //ioe.printStackTrace();
                }
                // sleep
                try {
                    Thread.sleep(40);
                } catch (InterruptedException ie) {
                    //ie.printStackTrace();
                    break;
                }
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "reader closing failed.", ex);
                }
            }
            //System.out.println("Reader closed... " + reader.hashCode());
        }
    }

    /**
     * A thread that runs a bundle of clipping threads, one after the other. Instead of having a real queue... Some
     * applications don't allow to be executed several times simultaneously, therefore the clipping is done sequentially.
     * (Though this doesn't work with all applications)
     *
     * @author Han
     */
    class ClipRunner extends Thread {
        List<MediaClipper> clips;
        // save error messages and return them to the caller
        String errorMessage = null;
        int numErrors = 0;

        public ClipRunner(List<MediaClipper> clips) {
            super();
            this.clips = clips;
        }

        /**
         * Error message getter.
         *
         * @return an error message or null.
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * Returns the number of errors that occurred.
         *
         * @return the number of errors
         */
        public int getnumErrors() {
            return numErrors;
        }

        @Override
        public void run() {
            //System.out.println("Running clip jobs...");
            if (clips != null && !clips.isEmpty()) {

                for (MediaClipper mc : clips) {
                    mc.start();
                    //System.out.println("Starting thread... " + mc.hashCode());
                    while (mc.isAlive()) {
                        //System.out.println("alive... " + mc.hashCode());
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {
                        }
                    }
                    if (mc.getErrorMessage() != null) {
                        numErrors++;
                        if (errorMessage == null) {
                            errorMessage = mc.getErrorMessage(); // store the first one
                        }
                    }
                }
            }
        }
    }
}
