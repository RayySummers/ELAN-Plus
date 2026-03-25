package nl.mpi.recognizer.local.whisper.transcriber;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectoryPathTest {

    @ParameterizedTest
    @MethodSource("nl.mpi.recognizer.local.whisper.transcriber.DirectoryPathTestDataProvider#provideParamsForGetDirPath")
    void testGetDirPath(String baseDir, String str, String os, String result) {

        DirectoryPath directoryPath = new DirectoryPath() {
            @Override
            public String getOsName() {
                return os;
            }
        };

        assertEquals(result, directoryPath.getDirPath(str, new File(baseDir)));
    }

    @ParameterizedTest
    @MethodSource("nl.mpi.recognizer.local.whisper.transcriber.DirectoryPathTestDataProvider#provideParamsForGetOptionalOutDir")
    void testGetOptionalOutDir(String whisperOutPath, String expectedPath) throws URISyntaxException {
        assertEquals(expectedPath, new DirectoryPath().getOptionalOutDir(whisperOutPath).get().toString());
    }

    @EnabledOnOs({OS.MAC})
    @Test
    void testGetOsNameMac() {
        assertTrue(new DirectoryPath().getOsName().toLowerCase().contains("mac"));
    }

    @EnabledOnOs({OS.LINUX})
    @Test
    void testGetOsNameLinux() {
        assertTrue(new DirectoryPath().getOsName().toLowerCase().contains("ubuntu"));
    }

    @EnabledOnOs({OS.WINDOWS})
    @Test
    void testGetOsNameWin() {
        assertTrue(new DirectoryPath().getOsName().toLowerCase().contains("win"));
    }
}