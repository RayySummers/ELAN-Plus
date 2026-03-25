package nl.mpi.recognizer.local.whisper.transcriber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoreInputFieldTest {

    @Test
    void testGetName() {
        assertEquals("model", CoreInputField.MODEL.getName());
    }

    @Test
    void testContains() {
        assertTrue(CoreInputField.contains("output_dir"));
        assertFalse(CoreInputField.contains("unknown field"));
    }

}
