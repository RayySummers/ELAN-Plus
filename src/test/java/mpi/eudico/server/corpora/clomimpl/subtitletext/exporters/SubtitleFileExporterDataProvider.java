package mpi.eudico.server.corpora.clomimpl.subtitletext.exporters;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.subtitletext.dto.InputFilter;
import mpi.eudico.server.corpora.clomimpl.subtitletext.dto.SubtitleEncoderInfo;
import mpi.eudico.server.corpora.clomimpl.subtitletext.dto.SubtitleFile;
import mpi.eudico.server.corpora.clomimpl.subtitletext.dto.SubtitleTransformationOptions;

import java.io.File;
import java.util.Arrays;

public class SubtitleFileExporterDataProvider {

    static Object[][] testDataGetExporter() {
        return new Object[][]{
            new Object[]{new File("/a202310051254.stl"), STLExporter.class},
            new Object[]{new File("/a202310051255.srt"), SRTExporter.class},
            new Object[]{new File("/a202310051256.lrc"), LRCExporter.class},
            new Object[]{new File("/a202310051257.xml"), TTMLExporter.class},
            new Object[]{new File("/a202310051258.unknown"), SRTExporter.class}
        };
    }

    static Object[][] testDataSuccessExport() {
        return new Object[][]{
            new Object[]{getSubtitleEncoderInfo("/a202310051259.txt", true), true},
            new Object[]{getSubtitleEncoderInfo("/a202310051301.txt", false), true}
        };
    }

    static Object[][] testDataFailureExport() {
        return new Object[][]{
            new Object[]{getSubtitleEncoderInfo("/a202310051302.txt", true), false},
            new Object[]{getSubtitleEncoderInfo("/a202310051303.txt", false), false}
        };
    }

    private static SubtitleEncoderInfo getSubtitleEncoderInfo(String filePath, boolean exportTiersSeparately) {
        SubtitleEncoderInfo subtitleEncoderInfo = new SubtitleEncoderInfo(
            new SubtitleFile(
                new File(filePath),
                null
            ),
            new TranscriptionImpl(),
            new InputFilter(
                Arrays.asList("Selected Tier 202310051304"),
                0,
                0,
                0,
                0
            ),
            new SubtitleTransformationOptions(1, true, exportTiersSeparately)
        );
        return subtitleEncoderInfo;
    }

}
