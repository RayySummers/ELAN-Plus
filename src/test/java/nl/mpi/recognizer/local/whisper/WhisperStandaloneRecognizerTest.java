package nl.mpi.recognizer.local.whisper;

import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

import static nl.mpi.recognizer.local.whisper.transcriber.PlatformUtil.getOperatingSystem;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhisperStandaloneRecognizerTest {

    @Mock
    private RecognizerHost host;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams() {
        reset(host);
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testCanHandleMedia() {
        assertTrue(new WhisperStandaloneRecognizer().canHandleMedia(null));
    }

    @Test
    void testCanCombineMultipleFiles() {
        assertFalse(new WhisperStandaloneRecognizer().canCombineMultipleFiles());
    }

    @Test
    void testSetRecognizerHost() {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();
        whisperStandaloneRecognizer.setRecognizerHost(host);
        assertEquals(host, whisperStandaloneRecognizer.getHost());
    }

    @Test
    void testSetParameterValue() {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();
        String parmaKey202403191348 = "parmaKey202403191348";
        String paramValue202403191348 = "paramValue202403191348";
        whisperStandaloneRecognizer.setParameterValue(parmaKey202403191348, paramValue202403191348);
        assertEquals(paramValue202403191348, whisperStandaloneRecognizer.getParameterValue(parmaKey202403191348));
    }

    @Test
    void testTestSetParameterValue() {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();
        String parmaKey202403191348 = "parmaKey202403191348";
        float paramValue202403191348 = 202403f;
        whisperStandaloneRecognizer.setParameterValue(parmaKey202403191348, paramValue202403191348);
        assertEquals(paramValue202403191348, whisperStandaloneRecognizer.getParameterValue(parmaKey202403191348));
    }

    @Test
    void testGetParameterValue() {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();
        assertNull(whisperStandaloneRecognizer.getParameterValue(""));
    }



    @Test
    void testStart() throws URISyntaxException {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();

        whisperStandaloneRecognizer.setParameterValue("run-command", getPath("/whisper/testExec" + getOperatingSystem()));
        whisperStandaloneRecognizer.setParameterValue("audio", "/word_segments_202403191054.json");
        whisperStandaloneRecognizer.setParameterValue("model", "base");
        whisperStandaloneRecognizer.setParameterValue("output_dir", getPath("/whisper"));

        whisperStandaloneRecognizer.setRecognizerHost(host);
        whisperStandaloneRecognizer.start();

        verify(host, atLeast(1)).appendToReport(anyString());
        verify(host, atLeast(1)).setProgress(anyFloat());
    }

    @Test
    void testStartForException() {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();
        whisperStandaloneRecognizer.setParameterValue("run-command", null);
        whisperStandaloneRecognizer.setRecognizerHost(host);

        whisperStandaloneRecognizer.start();

        String couldNotRunTheRecognizer = "Could not run the recognizer";
        verify(host, atLeast(1)).appendToReport(contains(couldNotRunTheRecognizer));
        verify(host, atLeast(1)).errorOccurred(contains(couldNotRunTheRecognizer));
    }

    private String getPath(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(this.getClass().getResource(name)).toURI()).toString();
    }

    @Test
    void testStop() throws URISyntaxException {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();
        whisperStandaloneRecognizer.setParameterValue("run-command", getPath("/whisper/testExec" + getOperatingSystem()));
        whisperStandaloneRecognizer.setParameterValue("audio", "/word_segments_202403191054.json");
        whisperStandaloneRecognizer.setParameterValue("model", "base");
        whisperStandaloneRecognizer.setParameterValue("output_dir", getPath("/whisper"));
        whisperStandaloneRecognizer.setRecognizerHost(host);
        whisperStandaloneRecognizer.start();

        whisperStandaloneRecognizer.stop();

        verify(host, atLeast(1)).appendToReport(contains("Trying to stop the running recognizer"));
        verify(host, atLeast(1)).setProgress(anyFloat());
    }

    @Test
    void testStopForExceptions() throws URISyntaxException {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();

        whisperStandaloneRecognizer.setParameterValue("run-command", getPath("/whisper/testExec" + getOperatingSystem()));
        whisperStandaloneRecognizer.setParameterValue("audio", "/word_segments_202403191054.json");
        whisperStandaloneRecognizer.setParameterValue("model", "base");
        whisperStandaloneRecognizer.setParameterValue("output_dir", getPath("/whisper"));

        doNothing()
            .doThrow(new RuntimeException("Test20240320140000 Exception"))
            .when(host)
            .appendToReport(anyString());

        whisperStandaloneRecognizer.setRecognizerHost(host);
        whisperStandaloneRecognizer.start();

        assertThrows(Exception.class, whisperStandaloneRecognizer::stop);
    }

    @Test
    void testDispose() throws URISyntaxException {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();
        whisperStandaloneRecognizer.setParameterValue("run-command", getPath("/whisper/testExec" + getOperatingSystem()));
        whisperStandaloneRecognizer.setParameterValue("audio", "/word_segments_202403191054.json");
        whisperStandaloneRecognizer.setParameterValue("model", "base");
        whisperStandaloneRecognizer.setParameterValue("output_dir", getPath("/whisper"));
        whisperStandaloneRecognizer.setRecognizerHost(host);
        whisperStandaloneRecognizer.start();

        whisperStandaloneRecognizer.dispose();

        verify(host, atLeast(1)).appendToReport(contains("Trying to stop the running recognizer"));
        verify(host, atLeast(1)).setProgress(anyFloat());
    }

    @Test
    void testGetName() {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();
        assertNull(whisperStandaloneRecognizer.getName());
    }

    @Test
    void testSetName() {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();
        String dummyName202403191414 = "dummyName202403191414";
        whisperStandaloneRecognizer.setName(dummyName202403191414);
        assertEquals(dummyName202403191414, whisperStandaloneRecognizer.getName());
    }

    @Test
    void testGetRecognizerType() {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();
        assertEquals(Recognizer.AUDIO_TYPE, whisperStandaloneRecognizer.getRecognizerType());
    }

    @Test
    void testGetHost() {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();
        whisperStandaloneRecognizer.setRecognizerHost(host);
        assertEquals(host, whisperStandaloneRecognizer.getHost());
    }

    @Test
    void testGetRecognizerProcess() throws URISyntaxException {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();
        whisperStandaloneRecognizer.setParameterValue("run-command", getPath("/whisper/testExec" + getOperatingSystem()));
        whisperStandaloneRecognizer.setParameterValue("audio", "/word_segments_202403191054.json");
        whisperStandaloneRecognizer.setParameterValue("model", "base");
        whisperStandaloneRecognizer.setParameterValue("output_dir", getPath("/whisper"));
        whisperStandaloneRecognizer.setRecognizerHost(host);
        whisperStandaloneRecognizer.start();

        assertNotNull(whisperStandaloneRecognizer.getRecognizerProcess());
    }

    @Test
    void testGetWhisperArguments() throws URISyntaxException {
        WhisperStandaloneRecognizer whisperStandaloneRecognizer = new WhisperStandaloneRecognizer();
        whisperStandaloneRecognizer.setParameterValue("run-command", getPath("/whisper/testExec" + getOperatingSystem()));
        whisperStandaloneRecognizer.setParameterValue("audio", "/word_segments_202403191054.json");
        whisperStandaloneRecognizer.setParameterValue("model", "base");
        whisperStandaloneRecognizer.setParameterValue("output_dir", getPath("/whisper"));
        whisperStandaloneRecognizer.setRecognizerHost(host);
        whisperStandaloneRecognizer.start();

        assertNotNull(whisperStandaloneRecognizer.getWhisperArguments());
    }

}
