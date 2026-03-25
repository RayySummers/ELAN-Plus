package nl.mpi.recognizer.local.whisper.transcriber;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class DirectoryPathTestDataProvider {

    public static Stream<Arguments> provideParamsForGetDirPath() {

        return Stream.of(
            Arguments.of("/whisper/", "testExecMac.jar", "Mac", "/whisper/testExecMac.jar"),
            Arguments.of("/whisper/", "testExecWin.exe", "Windows 11", "/whisper/testExecWin.exe"),
            Arguments.of("/whisper/", "testExecWin", "Windows 11", "/whisper/testExecWin.exe"),
            Arguments.of("/whisper/", "testExecUbuntu", "Linux", "/whisper/testExecUbuntu")
        );
    }

    public static Stream<Arguments> provideParamsForGetOptionalOutDir() {

        String userHome = System.getProperty("user.home");
        return Stream.of(
            Arguments.of("/whisper-out-test", "/whisper-out-test"),
            Arguments.of("\\test", userHome + "/whisper/test"),
            Arguments.of("test", userHome + "/whisper/test"),
            Arguments.of("", userHome + "/whisper/"),
            Arguments.of("      ", userHome + "/whisper/"),
            Arguments.of(null, userHome + "/whisper/")
        );
    }

}
