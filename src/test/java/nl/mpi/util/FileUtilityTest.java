package nl.mpi.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilityTest {

    @DisplayName("The file extension should be correctly identified as per provided example from test data.")
    @ParameterizedTest
    @MethodSource("nl.mpi.util.FileUtilityTestDataProvider#testDataSourceGetFileExtension")
    void testGetFileExtension(String filePath, Optional<String> expectedExtension) {
        Optional<String> mayBeExtension = FileUtility.getFileExtension(new File(filePath));
        assertEquals(expectedExtension.isPresent(), mayBeExtension.isPresent());

        expectedExtension.ifPresent(s -> assertEquals(s, mayBeExtension.get()));
    }
}
