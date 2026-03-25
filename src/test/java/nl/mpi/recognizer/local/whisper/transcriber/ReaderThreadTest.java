package nl.mpi.recognizer.local.whisper.transcriber;

import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import nl.mpi.recognizer.local.whisper.WhisperStandaloneRecognizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReaderThreadTest {

    @Mock
    private WhisperStandaloneRecognizer pRecognizer;

    @Mock
    private Process process;

    @Mock
    private InputStream inputStream;

    @Mock
    private RecognizerHost host;

    @BeforeEach
    public void setup() {
        reset(inputStream);
        reset(pRecognizer);
    }

    @Test
    void testStart() throws IOException, InterruptedException {
        when(pRecognizer.getRecognizerProcess()).thenReturn(process);
        when(process.getInputStream()).thenReturn(inputStream);
        when(pRecognizer.getHost()).thenReturn(host).thenReturn(host);

        new ReaderThreadFake(pRecognizer).start();
        Thread.sleep(100);

        verify(process, times(1)).getInputStream();
        verify(host, atLeast(3)).appendToReport(anyString());
        verify(pRecognizer, atLeast(1)).getRecognizerProcess();
        verify(pRecognizer, times(1)).processOutput();
    }

    @Test
    void testTerminationLogs() {
        ReaderThread readerThread = new ReaderThread(pRecognizer);
        boolean result = readerThread.terminationLogs(null);
        assertFalse(result);
        result = readerThread.terminationLogs("");
        assertFalse(result);
    }

    @ParameterizedTest
    @MethodSource("nl.mpi.recognizer.local.whisper.transcriber.ReaderThreadTestDataProvider#provideParamsForTerminationLogsForNonEmpty")
    void testTerminationLogsForNonEmpty(
        String line,
        int numberOfInteractionsWithLog,
        int numberOfInteractionsWithError,
        int numberOfInteractionsWithProgress,
        boolean isTerminated
    ) {
        when(pRecognizer.getHost()).thenReturn(host);
        boolean actualTerminationResult = new ReaderThread(pRecognizer).terminationLogs(line);
        verify(host, times(numberOfInteractionsWithLog)).appendToReport(anyString());
        verify(host, times(numberOfInteractionsWithError)).errorOccurred(anyString());
        verify(host, times(numberOfInteractionsWithProgress)).setProgress(anyFloat());
        assertEquals(isTerminated, actualTerminationResult);
    }

    @ParameterizedTest
    @MethodSource("nl.mpi.recognizer.local.whisper.transcriber.ReaderThreadTestDataProvider#provideParamsFortestTerminationLogsForException")
    void testTerminationLogsForException(String line, boolean isTerminated) {
        assertEquals(isTerminated, new ReaderThread(pRecognizer).terminationLogs(line));
    }

    @ParameterizedTest
    @MethodSource("nl.mpi.recognizer.local.whisper.transcriber.ReaderThreadTestDataProvider#provideParamsForLogClosingInformation")
    void testLogClosingInformation(int exitCode, int numberOfInteractions, int numberOfProgressSet) throws InterruptedException {
        when(pRecognizer.getHost()).thenReturn(host);
        when(pRecognizer.getRecognizerProcess()).thenReturn(process);
        when(process.waitFor()).thenReturn(exitCode);
        new ReaderThread(pRecognizer).logClosingInformation();
        verify(host, times(1)).appendToReport(anyString());
        verify(host, times(numberOfInteractions)).errorOccurred(anyString());
        verify(host, times(numberOfProgressSet)).setProgress(anyFloat());
    }

    @ParameterizedTest
    @MethodSource("nl.mpi.recognizer.local.whisper.transcriber.ReaderThreadTestDataProvider#provideParamsForLogReport")
    void testLogReport(String message, boolean isError, int numOfInteractions) {
        when(pRecognizer.getHost()).thenReturn(host);
        new ReaderThread(pRecognizer).logReport(message, isError);
        verify(host, times(1)).appendToReport(anyString());
        verify(host, times(numOfInteractions)).errorOccurred(anyString());
    }
}