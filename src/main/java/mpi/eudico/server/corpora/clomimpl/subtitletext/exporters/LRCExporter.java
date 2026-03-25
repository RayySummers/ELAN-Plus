package mpi.eudico.server.corpora.clomimpl.subtitletext.exporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;

import mpi.eudico.client.util.SubtitleUnit;
import mpi.eudico.server.corpora.clomimpl.subtitletext.SubtitleSequencer;
import mpi.eudico.server.corpora.clomimpl.subtitletext.dto.SubtitleEncoderInfo;

import static java.lang.System.lineSeparator;

public class LRCExporter extends SubtitleFileExporter {

    public static final String SUPPORTED_EXTENSION = "lrc";

    private LRCExporter() {
    }

    public static LRCExporter getInstance() {
        return LRCExporter.InstanceHolder.instance;
    }

    @Override
    public boolean exportFile(SubtitleEncoderInfo subtitleEncoderInfo) throws IOException {
        long beginTime = subtitleEncoderInfo.inputFilter().beginTime();
        long offset = subtitleEncoderInfo.inputFilter().offset();

//        format
//        [COLOUR]0xFF66FF//
//        [00:03.120]Owaranai
//        [00:05.548]mugen no hikari ...
//        [00:09.927]
//        [00:21.016]taeran ninmu
        long recalculateTimeInterval = 0L;
        int selection = 0;
        if (subtitleEncoderInfo.subtitleTransformationOptions().reCalculateTime()) {
            recalculateTimeInterval = beginTime;
            offset = 0L;
        }

        List<SubtitleUnit> allUnits = new SubtitleSequencer().createSequence(
            subtitleEncoderInfo.transcription(),
            subtitleEncoderInfo.inputFilter().selectedForExportTierNames(),
            subtitleEncoderInfo.inputFilter().beginTime(),
            subtitleEncoderInfo.inputFilter().endTime(),
            subtitleEncoderInfo.inputFilter().minimalDuration(),
            offset,
            true);

        writeFile(subtitleEncoderInfo.subtitleFile().targetFile(), subtitleEncoderInfo.subtitleFile().fileEncoding(), allUnits, selection, recalculateTimeInterval);
        return true;
    }

    private void writeFile(File exportFile, String encoding, List<SubtitleUnit> allUnits, int selection, long recalculateTimeInterval) throws IOException {
        SubtitleUnit unit;
        SubtitleUnit nextUnit;

        try (BufferedWriter writer = getBufferedWriter(exportFile, encoding)) {

            // Write the preamble.
            //writer.write("");// could write ID tags [ti: name.eaf] and [by: ELAN]
            writer.write(lineSeparator());

            for (int i = 0; i < allUnits.size(); i++) {
                unit = allUnits.get(i);

                Long b = unit.getBegin();
                Long e = unit.getCalcEnd();
                if (selection == 0 && (b < recalculateTimeInterval)) {
                        recalculateTimeInterval = b;
                        selection = 1;

                }

                writer.write('[');
                writer.write(toLRCTimeCode(b - recalculateTimeInterval));
                writer.write(']');

                String[] values = unit.getValues();
                for (int j = 0; j < values.length; j++) {
                    writer.write(values[j].replace('\n', ' '));

                    if (j < values.length - 1) {
                        writer.write(' ');
                    }
                }

                writer.write(lineSeparator());
                // check if there is a next unit and if there is a time gap insert an empty line
                // starting with end time
                if (i < allUnits.size() - 1) {
                    nextUnit = allUnits.get(i + 1);
                    if (e < nextUnit.getBegin() - 100) {// only insert empty line if gaps is > 100 ms?
                        writer.write('[');
                        writer.write(toLRCTimeCode(e - recalculateTimeInterval));
                        writer.write(']');
                        writer.write(lineSeparator());
                    }
                }
            }
        }
    }

    /**
     * Converts a time value to min:sec.ms format (should ms be rounded to two decimals?).
     *
     * @param t the time in milliseconds
     * @return string representation
     */
    private String toLRCTimeCode(long t) {
        long minutes = t / 60000;
        String minString = twoDigits.format(minutes);
        long seconds = (t - (60000 * minutes)) / 1000;
        String secString = twoDigits.format(seconds);
        long millis = (t - (60000 * minutes) - (1000 * seconds)) / 10;
        String msString = twoDigits.format(millis);// or three digits

        return minString + ":" + secString + "." + msString;
    }

    private static class InstanceHolder {
        private static final LRCExporter instance = new LRCExporter();
    }
}
