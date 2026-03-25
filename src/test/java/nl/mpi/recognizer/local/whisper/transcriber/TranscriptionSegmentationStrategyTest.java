package nl.mpi.recognizer.local.whisper.transcriber;

import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import nl.mpi.recognizer.local.whisper.WhisperStandaloneRecognizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Optional;

import static nl.mpi.recognizer.local.whisper.transcriber.TranscriptionSegmentationStrategy.NO_SEGMENTATION_WAS_LOADED;
import static nl.mpi.recognizer.local.whisper.transcriber.TranscriptionSegmentationStrategy.convertStringToLongWithFactor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranscriptionSegmentationStrategyTest {

    @Mock
    private WhisperStandaloneRecognizer recognizer;

    @Mock
    private WhisperArguments whisperArguments;

    @Mock
    private RecognizerHost host;

    @InjectMocks
    private TranscriptionSegmentationStrategyFake transcriptionSegmentationStrategyFake;

    @BeforeEach
    public void setup(){
        reset(whisperArguments);
        reset(host);
        reset(recognizer);
    }

    @ParameterizedTest
    @MethodSource("nl.mpi.recognizer.local.whisper.transcriber.TranscriptionSegmentationStrategyTestDataProvider#provideDataForTestGetStrategy")
    void testGetStrategy(Boolean requiresWordStrategy, Class<?> clazz) {
        when(recognizer.getWhisperArguments()).thenReturn(whisperArguments);
        when(whisperArguments.wordTimestamps()).thenReturn(Optional.ofNullable(requiresWordStrategy));
        assertEquals(clazz, TranscriptionSegmentationStrategy.getStrategy(recognizer).getClass());
    }

    @ParameterizedTest
    @MethodSource("nl.mpi.recognizer.local.whisper.transcriber.TranscriptionSegmentationStrategyTestDataProvider#provideDataForTestExtractData")
    void testExtractData(String path, Optional<URI> outDirPath, String segType, int numMessageLogging, int numberOfSegments) throws IOException {
        when(whisperArguments.wordTimestamps()).thenReturn(Optional.of("words".equals(segType)));
        when(recognizer.getWhisperArguments()).thenReturn(whisperArguments);
        when(recognizer.getHost()).thenReturn(host);
        when(whisperArguments.inputAudioVideoFilePath()).thenReturn(path);
        when(whisperArguments.outputDirectory()).thenReturn(outDirPath);

        transcriptionSegmentationStrategyFake.extractData();

        verify(host, times(numMessageLogging)).appendToReport(startsWith(NO_SEGMENTATION_WAS_LOADED));
        verify(host, times(numberOfSegments)).setProgress(anyFloat());
        verify(host, times(numberOfSegments)).addSegmentation(assertArg(segmentation -> assertEquals(segType, segmentation.getName())));
    }

    @ParameterizedTest
    @MethodSource("nl.mpi.recognizer.local.whisper.transcriber.TranscriptionSegmentationStrategyTestDataProvider#provideDataForTestExtractDataForException")
    void testExtractDataForException(String path, Optional<URI> outDirPath, String segType, int numMessageLogging, int numberOfSegments) {
        when(recognizer.getWhisperArguments()).thenReturn(whisperArguments);
        when(whisperArguments.inputAudioVideoFilePath()).thenReturn(path);
        when(whisperArguments.outputDirectory()).thenReturn(outDirPath);

        assertThrows(NoSuchFileException.class, () -> transcriptionSegmentationStrategyFake.extractData());
    }

    @ParameterizedTest
    @MethodSource("nl.mpi.recognizer.local.whisper.transcriber.TranscriptionSegmentationStrategyTestDataProvider#provideDataForTestCreateTiers")
    void testCreateTiers(List<RSelection> segments, String segmentationType, String path, String outDirPath, int numberOfInvocations) {
        when(recognizer.getWhisperArguments()).thenReturn(whisperArguments);
        when(recognizer.getHost()).thenReturn(host);
        when(whisperArguments.inputAudioVideoFilePath()).thenReturn(path);

        transcriptionSegmentationStrategyFake.createTiers(segments, segmentationType);

        verify(host, times(numberOfInvocations)).appendToReport(startsWith(NO_SEGMENTATION_WAS_LOADED));
    }

    @ParameterizedTest
    @MethodSource("nl.mpi.recognizer.local.whisper.transcriber.TranscriptionSegmentationStrategyTestDataProvider#provideDataForTestCreateTiersWhenEmpty")
    void testCreateTiersWhenEmpty(List<RSelection> segments, String segmentationType, int numberOfInvocations) {
        when(recognizer.getHost()).thenReturn(host);

        transcriptionSegmentationStrategyFake.createTiers(segments, segmentationType);

        verify(host, times(numberOfInvocations)).appendToReport(startsWith(NO_SEGMENTATION_WAS_LOADED));
    }

    @Test
    void testConvertStringToLongWithFactor() {
        assertEquals(1L, convertStringToLongWithFactor("1.0", 1));
        assertEquals(2L, convertStringToLongWithFactor("1.0", 2));
        assertEquals(0L, convertStringToLongWithFactor("1.0", 0));
        assertEquals(-1L, convertStringToLongWithFactor("1.0", -1));
        assertEquals(-1L, convertStringToLongWithFactor("-1.0", 1));
        assertEquals(1L, convertStringToLongWithFactor("-1.0", -1));
        assertEquals(0L, convertStringToLongWithFactor("-1.0", 0));
    }
}
