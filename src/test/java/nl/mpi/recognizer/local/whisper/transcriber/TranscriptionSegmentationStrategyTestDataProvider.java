package nl.mpi.recognizer.local.whisper.transcriber;

import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import org.junit.jupiter.params.provider.Arguments;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class TranscriptionSegmentationStrategyTestDataProvider {

    public static Stream<Arguments> provideDataForTestGetStrategy() {

        return Stream.of(
            Arguments.of(Boolean.TRUE, WordTiersHandler.class),
            Arguments.of(Boolean.FALSE, PhraseTiersHandler.class),
            Arguments.of(null, PhraseTiersHandler.class)
        );
    }

    public static Stream<Arguments> provideDataForTestExtractData() throws URISyntaxException {

        String whisperOut = getPathAsString();

        return Stream.of(
            Arguments.of("/word_segments_202403191054.json", Optional.of(new URI(whisperOut)), "words", 0, 1),
            Arguments.of("/phrase_segments_202403191054.json", Optional.of(new URI(whisperOut)), "phrase", 0, 1),
            Arguments.of("/phrase_segments_empty_202403191054.json", Optional.of(new URI(whisperOut)), "phrase", 0, 1)
        );
    }

    public static Stream<Arguments> provideDataForTestExtractDataForException() {
        return Stream.of(
            Arguments.of("/phrase_segments_empty_202403191054.json", Optional.empty(), "phrase", 0, 1)
        );
    }

    private static String getPathAsString() throws URISyntaxException {
        URL resource = TranscriptionSegmentationStrategyTestDataProvider.class.getResource("/whisper");
        return Paths.get(Objects.requireNonNull(resource).toURI()).toString().replace("\\", "/");
    }

    public static Stream<Arguments> provideDataForTestCreateTiers() throws URISyntaxException {
        RSelection segment = new Segment();
        String whisperOut = getPathAsString();
        return Stream.of(
            Arguments.of(singletonList(segment), "word", "/word_segments_202403191054.json", whisperOut, 0),
            Arguments.of(singletonList(segment), "phrase", "/word_segments_202403191054.json", whisperOut, 0)
        );
    }

    public static Stream<Arguments> provideDataForTestCreateTiersWhenEmpty() {
        return Stream.of(
            Arguments.of(emptyList(), "word", 1),
            Arguments.of(emptyList(), "phrase", 1)
        );
    }
}
