package nl.mpi.recognizer.local.whisper.transcriber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WhisperLanguageTest {

    @Test
    void testToString() {
        assertEquals("ar", WhisperLanguage.AR.toString());
    }

    @Test
    void testGetName() {
        assertEquals("arabic", WhisperLanguage.AR.getName());
    }

}
