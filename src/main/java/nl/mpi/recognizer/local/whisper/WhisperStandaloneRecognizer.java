package nl.mpi.recognizer.local.whisper;

import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import nl.mpi.recognizer.local.whisper.transcriber.CommandCreator;
import nl.mpi.recognizer.local.whisper.transcriber.ReaderThread;
import nl.mpi.recognizer.local.whisper.transcriber.TranscriptionSegmentationStrategy;
import nl.mpi.recognizer.local.whisper.transcriber.WhisperArguments;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * A recognizer which calls a local executable of <a href="https://openai.com/blog/whisper/">Whisper</a> to transcribe
 * or translate an audio file.
 */
public class WhisperStandaloneRecognizer implements Recognizer {
    private static final System.Logger LOG = System.getLogger("whisper_standalone");
    public static final String COULD_NOT_RUN_THE_RECOGNIZER = "Could not run the recognizer: ";
    public static final String STARTING_PROCESS_WITH_COMMAND = "Starting process with command:";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String SPACE_DELIMITER = " ";
    public static final String STOPPING_RECOGNIZER = "Stopping recognizer...";
    public static final String TRYING_TO_STOP_THE_RUNNING_RECOGNIZER = "Trying to stop the running recognizer";
    public static final String LINE_BREAK = "\n";

    private String name;
    private final Map<String, String> paramMapString;
    private final Map<String, Float> paramMapFloat;
    private RecognizerHost host;
    private Process recognizerProcess;
    private volatile boolean isRecognizerProcessRunning = false;
    private WhisperArguments whisperArguments;
    private File baseDir;

    public WhisperStandaloneRecognizer() {
        paramMapString = HashMap.newHashMap(10);
        paramMapFloat = HashMap.newHashMap(10);
    }

    /**
     * The media file will be processed by {@code ffmpeg}, assume here that it is supported.
     *
     * @return {@code true}
     */
    @Override
    public boolean canHandleMedia(String mediaFilePath) {
        return true;
    }

    /**
     * Returns {@code false}, although {@code whisper} can be called with multiple audio files, they are still processed
     * individually.
     *
     * @return {@code false}
     */
    @Override
    public boolean canCombineMultipleFiles() {
        return false;
    }

    @Override
    public void setRecognizerHost(RecognizerHost host) {
        this.host = host;
    }

    @Override
    public void setParameterValue(String param, String value) {
        paramMapString.put(param, value);
    }

    @Override
    public void setParameterValue(String param, float value) {
        paramMapFloat.put(param, value);
    }

    @Override
    public Object getParameterValue(String param) {
        Object value = null;

        if (param != null && !param.isBlank()) {
            if (paramMapString.containsKey(param)) {
                value = paramMapString.get(param);
            } else if (paramMapFloat.containsKey(param)) {
                value = paramMapFloat.get(param);
            }
        }

        return value;
    }

    @Override
    public void start() {
        try {
            CommandCreator commandCreator = new CommandCreator();
            whisperArguments = commandCreator.getWhisperArguments(paramMapString, paramMapFloat, baseDir);
            String[] commands = commandCreator.toCommandLineFormat(whisperArguments);
            logToReport(commands);

            isRecognizerProcessRunning = true;
            host.setProgress(-1f);

            recognizerProcess = new ProcessBuilder(commands)
                    .redirectErrorStream(true)
                    .start();

            new ReaderThread(this).start();
        } catch (Exception e) {
            final String msg = COULD_NOT_RUN_THE_RECOGNIZER + e.getMessage();
            LOG.log(Level.ERROR, msg);
            host.appendToReport(msg + '\n');
            host.errorOccurred(msg);
        }
    }

    private void logToReport(String[] commands) {
        String message = STARTING_PROCESS_WITH_COMMAND + LINE_BREAK +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)) + LINE_BREAK +
                String.join(SPACE_DELIMITER, commands) + LINE_BREAK;
        host.appendToReport(message);
    }

    @Override
    public void stop() {
        // if there is a process stop it?
        if (isRecognizerProcessRunning && recognizerProcess != null) {
            // send a message first? Is there a way of closing gracefully?
            LOG.log(Level.INFO, STOPPING_RECOGNIZER);
            host.appendToReport(TRYING_TO_STOP_THE_RUNNING_RECOGNIZER + LINE_BREAK);
            recognizerProcess.destroy();
            isRecognizerProcessRunning = false;
        }
    }

    @Override
    public void dispose() {
        if (isRecognizerProcessRunning) {
            stop();
        }

        host = null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * This recognizer identifies as an audio recognizer (although video files are also supported).
     *
     * @return {@code Recognizer#AUDIO_TYPE}
     */
    @Override
    public int getRecognizerType() {
        return Recognizer.AUDIO_TYPE;
    }

    public RecognizerHost getHost() {
        return host;
    }

    public Process getRecognizerProcess() {
        return recognizerProcess;
    }

    public WhisperArguments getWhisperArguments() {
        return whisperArguments;
    }

    public void processOutput() throws IOException {
        TranscriptionSegmentationStrategy
                .getStrategy(this)
                .extractData();
    }
}
