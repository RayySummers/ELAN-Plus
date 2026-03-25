package nl.mpi.recognizer.local.whisper.transcriber;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.AbstractMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IntegerFieldTest {

    @Test
    void testGetName() {
        assertEquals("best_of", IntegerField.BEST_OF.getName());
    }

    @Test
    void testContains() {
        assertTrue(IntegerField.contains("best_of"));
        assertFalse(IntegerField.contains("unknown field"));
    }

    @ParameterizedTest
    @MethodSource("nl.mpi.recognizer.local.whisper.transcriber.IntegerFieldTestDataProvider#provideParamsForRoundIntegerEntries")
    void testRoundIntegerEntries(String key, float value, String expectedResult) {
        Map.Entry<String, Float> mapEntry = new AbstractMap.SimpleEntry<>(key, value);
        Map.Entry<String, ?> entry = IntegerField.roundIntegerEntries(mapEntry);
        assertEquals(expectedResult, String.valueOf(entry.getValue()));
    }

    @Test
    void testRoundIntegerEntriesForUnknownFields() {
        Map.Entry<String, Float> mapEntry = new AbstractMap.SimpleEntry<>("unknownField", 20240318.1620f);
        Map.Entry<String, ?> entry = IntegerField.roundIntegerEntries(mapEntry);
        assertEquals(20240318.1620f, entry.getValue());
    }

}
