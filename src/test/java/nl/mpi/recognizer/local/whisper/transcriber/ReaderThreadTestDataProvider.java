package nl.mpi.recognizer.local.whisper.transcriber;

import org.junit.jupiter.params.provider.Arguments;

import java.net.URISyntaxException;
import java.util.stream.Stream;

public class ReaderThreadTestDataProvider {

    public static Stream<Arguments> provideParamsForTerminationLogsForNonEmpty() throws URISyntaxException {

        return Stream.of(
            Arguments.of("" + '\u0004', 1, 1, 0, true),
            Arguments.of("-1", 1, 1, 0, true),
            Arguments.of("0", 1, 0, 1, true)
        );
    }

    public static Stream<Arguments> provideParamsFortestTerminationLogsForException() throws URISyntaxException {

        return Stream.of(
            Arguments.of(null, false),
            Arguments.of("", false)
        );
    }

    public static Stream<Arguments> provideParamsForLogClosingInformation() throws URISyntaxException {

        return Stream.of(
            Arguments.of(0, 0, 1),
            Arguments.of(1, 1, 0)
        );
    }

    public static Stream<Arguments> provideParamsForLogReport() throws URISyntaxException {

        return Stream.of(
            Arguments.of(" sampleMessage202403201518", true, 1),
            Arguments.of(" sampleMessage202403201518", false, 0)
        );
    }
}
