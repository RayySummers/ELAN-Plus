package nl.mpi.recognizer.local.whisper.transcriber;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InterFrameworkTranslationHelperTest {

    @Test
    void toPythonBooleanString() {
        assertEquals("True", InterFrameworkTranslationHelper.toPythonBooleanString(Optional.of(Boolean.TRUE)).get());
        assertEquals("False", InterFrameworkTranslationHelper.toPythonBooleanString(Optional.of(Boolean.FALSE)).get());
        assertFalse(InterFrameworkTranslationHelper.toPythonBooleanString(Optional.empty()).isPresent());
    }
}