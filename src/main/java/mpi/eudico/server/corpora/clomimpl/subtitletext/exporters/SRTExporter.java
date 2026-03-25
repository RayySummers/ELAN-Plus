package mpi.eudico.server.corpora.clomimpl.subtitletext.exporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;

import mpi.eudico.client.util.SubtitleUnit;
import mpi.eudico.server.corpora.clomimpl.subtitletext.SubtitleSequencer;
import mpi.eudico.server.corpora.clomimpl.subtitletext.dto.SubtitleEncoderInfo;
import mpi.eudico.util.TimeFormatter;

import static java.lang.System.lineSeparator;

public class SRTExporter extends SubtitleFileExporter {

    public static final String SUPPORTED_EXTENSION = "srt";

    private SRTExporter() {
    }

    public static SRTExporter getInstance() {
        return SRTExporter.InstanceHolder.instance;
    }

    private void writeFile(File exportFile, String encoding, List<SubtitleUnit> allUnits, int selection, long recalculateTimeInterval) throws IOException {
        SubtitleUnit unit;
        try (BufferedWriter writer = getBufferedWriter(exportFile, encoding)) {

            for (int i = 0; i < allUnits.size(); i++) {
                unit = allUnits.get(i);
                writer.write(String.valueOf(i + 1)); // some apps don't accept index 0
                writer.write(lineSeparator());
                Long b = unit.getBegin();
                Long e = unit.getCalcEnd();
                if (selection == 0 && (b < recalculateTimeInterval)) {
                        recalculateTimeInterval = b;
                        selection = 1;

                }

                writer.write(TimeFormatter.toString(b - recalculateTimeInterval).replace('.', ','));
                writer.write(" --> ");
                writer.write(TimeFormatter.toString(e - recalculateTimeInterval).replace('.', ','));
                writer.write(lineSeparator());


                for (int j = 0; j < unit.getValues().length; j++) {
                    writer.write(unit.getValues()[j].replace('\n', ' '));

                    if (j != (unit.getValues().length - 1)) {
                        writer.write(lineSeparator());
                    }
                }

                writer.write(lineSeparator());
                writer.write(lineSeparator());
            }
        }
    }

    @Override
    public boolean exportFile(SubtitleEncoderInfo subtitleEncoderInfo) throws IOException {
        long beginTime = subtitleEncoderInfo.inputFilter().beginTime();
        long offset = subtitleEncoderInfo.inputFilter().offset();

        long recalculateTimeInterval = 0L;
        int selection = 0;
        if (subtitleEncoderInfo.subtitleTransformationOptions().reCalculateTime()) {
            recalculateTimeInterval = beginTime;
            offset = 0L;
        }

        SubtitleSequencer sequencer = new SubtitleSequencer();

        List<SubtitleUnit> allUnits = sequencer.createSequence(subtitleEncoderInfo.transcription(), subtitleEncoderInfo.inputFilter().selectedForExportTierNames(),
            beginTime, subtitleEncoderInfo.inputFilter().endTime(), subtitleEncoderInfo.inputFilter().minimalDuration(), offset, true);

        writeFile(subtitleEncoderInfo.subtitleFile().targetFile(), subtitleEncoderInfo.subtitleFile().fileEncoding(), allUnits, selection, recalculateTimeInterval);
        return true;
    }

    private static class InstanceHolder {
        private static final SRTExporter instance = new SRTExporter();
    }

}
