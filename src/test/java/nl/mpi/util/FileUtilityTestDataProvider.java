package nl.mpi.util;

import java.util.Optional;

public class FileUtilityTestDataProvider {

    static Object[][] testDataSourceGetFileExtension() {
        return new Object[][]{
            new Object[]{"C:/folderName/fileName202310040801.Extension", Optional.of("extension")},
            new Object[]{"C:/folderName/fileName202310040802.c", Optional.of("c")},
            new Object[]{"/folderName/fileName202310040803.m", Optional.of("m")},
            new Object[]{"C:\\B\\A202310040804.D", Optional.of("d")},
            new Object[]{"C:/folderName/fileName202310040805.", Optional.empty()},
            new Object[]{"C:/folderName/fileName202310040806", Optional.empty()}
        };
    }
}
