package nl.mpi.recognizer.local.whisper.transcriber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SupportedOSTest {

    @Test
    void testGetName() {
        assertEquals("MAC", SupportedOS.MAC.toString());
    }

}
