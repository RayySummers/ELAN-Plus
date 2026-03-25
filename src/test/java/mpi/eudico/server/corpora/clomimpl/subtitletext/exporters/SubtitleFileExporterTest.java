package mpi.eudico.server.corpora.clomimpl.subtitletext.exporters;

import mpi.eudico.server.corpora.clomimpl.subtitletext.dto.SubtitleEncoderInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.*;

class SubtitleFileExporterTest {

    @DisplayName("The file exporter instance should be corresponding to examples from test data.")
    @ParameterizedTest
    @MethodSource("mpi.eudico.server.corpora.clomimpl.subtitletext.exporters.SubtitleFileExporterDataProvider#testDataGetExporter")
    <E extends SubtitleFileExporter> void testGetExporter(File file, Object expectedExporterClass) {
        E exporter = SubtitleFileExporter.getExporter(file);

        Assertions.assertEquals(expectedExporterClass, exporter.getClass());
    }

    @DisplayName("The export should succeed corresponding to example from test data.")
    @ParameterizedTest
    @MethodSource("mpi.eudico.server.corpora.clomimpl.subtitletext.exporters.SubtitleFileExporterDataProvider#testDataSuccessExport")
    void testExportForSuccess(SubtitleEncoderInfo subtitleEncoderInfo, boolean expectedStatus) throws IOException {
        SubtitleFileExporter subtitleFileExporter = mock(SubtitleFileExporter.class, CALLS_REAL_METHODS);
        when(subtitleFileExporter.exportFile(ArgumentMatchers.any(SubtitleEncoderInfo.class))).thenReturn(true);
        boolean exportStatus = subtitleFileExporter.export(subtitleEncoderInfo);
        Assertions.assertEquals(expectedStatus, exportStatus);
    }

    @DisplayName("The export should fail corresponding to example from test data.")
    @ParameterizedTest
    @MethodSource("mpi.eudico.server.corpora.clomimpl.subtitletext.exporters.SubtitleFileExporterDataProvider#testDataFailureExport")
    void testExportForFailure(SubtitleEncoderInfo subtitleEncoderInfo, boolean expectedStatus) throws IOException {
        SubtitleFileExporter subtitleFileExporter = mock(SubtitleFileExporter.class, CALLS_REAL_METHODS);
        when(subtitleFileExporter.exportFile(ArgumentMatchers.any(SubtitleEncoderInfo.class))).thenThrow(new IOException("Test exception 2023-10-05"));

        boolean exportStatus = subtitleFileExporter.export(subtitleEncoderInfo);
        Assertions.assertEquals(expectedStatus, exportStatus);

        Assertions.assertEquals(expectedStatus, exportStatus);
    }
}
