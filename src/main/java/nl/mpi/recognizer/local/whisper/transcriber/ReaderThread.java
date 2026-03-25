package nl.mpi.recognizer.local.whisper.transcriber;

import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import nl.mpi.recognizer.local.whisper.WhisperStandaloneRecognizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ReaderThread extends Thread {

    private static final System.Logger LOG = System.getLogger("whisper_standalone_reader");
    public static final String RECOGNIZER_FAILED_MESSAGE = "Recognizer failed, end of transmission (there may be partial results).";
    public static final String RECOGNIZER_TERMINATED_SUCCESSFULLY = "Recognizer terminated successfully.";
    public static final String STOPPED_UNEXPECTEDLY = "Recognizer stopped unexpectedly with exit code: ";
    public static final String EXCEPTION_WHILE_RECOGNIZING = "Exception while recognizing: ";
    public static final String RECOGNIZER_TERMINATED_SUCCESSFULLY_WITH_EXIT_CODE = "Recognizer terminated successfully with exit code: ";
    public static final String THERE_MAY_BE_PARTIAL_RESULTS = " (there may be partial results)";
    public static final String LINE_BREAK = "\n";
    private final WhisperStandaloneRecognizer recognizer;

    public ReaderThread(WhisperStandaloneRecognizer pRecognizer) {
        recognizer = pRecognizer;
    }

    @Override
    public void run() {

        try {
            BufferedReader bufferedOutputReader = getBufferedReader(recognizer.getRecognizerProcess().getInputStream());
            readStreamBlocking(bufferedOutputReader);
            logClosingInformation();
            recognizer.processOutput();
        } catch (IOException | InterruptedException ioe) {
            LOG.log(System.Logger.Level.INFO, EXCEPTION_WHILE_RECOGNIZING + ioe.getMessage(), ioe);
            Thread.currentThread().interrupt();
        }
    }

    public BufferedReader getBufferedReader(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    public void readStreamBlocking(BufferedReader bufferedOutputReader) throws IOException {
        String line;
        while ((line = bufferedOutputReader.readLine()) != null) {
            recognizer.getHost().appendToReport(line + LINE_BREAK);
            if (terminationLogs(line)) {
                break;
            }
        }
    }

    public boolean terminationLogs(String line) {
        boolean isTerminated = false;

        if (line != null && line.length() == 1 && line.charAt(0) == '\u0004') { // end of transmission
            logReport(RECOGNIZER_FAILED_MESSAGE, true);
            isTerminated = true;
        } else {
            try {
                int eof = Integer.parseInt(line);
                if (eof == -1) { // != 0 ??
                    logReport(RECOGNIZER_FAILED_MESSAGE, true);
                    isTerminated = true;
                } else if (eof == 0) {
                    logReport(RECOGNIZER_TERMINATED_SUCCESSFULLY, false);
                    recognizer.getHost().setProgress(0.9f);// or 0.9 to leave time for passing the segmentation?
                    isTerminated = true;
                }
            } catch (NumberFormatException nfe) {
                LOG.log(System.Logger.Level.WARNING, nfe.getMessage(), nfe);
            }
        }

        return isTerminated;
    }

    public void logClosingInformation() throws InterruptedException {
        // end of stream
        int exitVal = recognizer.getRecognizerProcess().waitFor();
        RecognizerHost host = recognizer.getHost();
        if (exitVal == 0) {
            LOG.log(System.Logger.Level.INFO, RECOGNIZER_TERMINATED_SUCCESSFULLY_WITH_EXIT_CODE + exitVal);
            host.appendToReport(RECOGNIZER_TERMINATED_SUCCESSFULLY_WITH_EXIT_CODE + exitVal + LINE_BREAK);
            host.setProgress(0.9f);// or 0.9 to leave time for passing the segmentation?
        } else {
            LOG.log(System.Logger.Level.WARNING, STOPPED_UNEXPECTEDLY + exitVal);
            host.errorOccurred(STOPPED_UNEXPECTEDLY + exitVal + THERE_MAY_BE_PARTIAL_RESULTS);
            host.appendToReport(STOPPED_UNEXPECTEDLY + exitVal + THERE_MAY_BE_PARTIAL_RESULTS + LINE_BREAK);
        }
    }

    public void logReport(String message, boolean isError) {
        LOG.log(isError ? System.Logger.Level.WARNING : System.Logger.Level.INFO, message);
        RecognizerHost host = recognizer.getHost();
        host.appendToReport(message + LINE_BREAK);
        if (isError) {
            host.errorOccurred(message);
        }
    }

}
