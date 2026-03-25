package nl.mpi.recognizer.local.whisper.transcriber;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;


public class IntegerFieldTestDataProvider {

    public static Stream<Arguments> provideParamsForRoundIntegerEntries() {

        return Stream.of(
            Arguments.of("best_of", 18.1619f, "18"),
            Arguments.of("best_of", 19f, "19")
        );
    }
}
