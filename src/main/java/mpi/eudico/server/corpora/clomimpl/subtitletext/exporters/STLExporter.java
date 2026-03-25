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

public class STLExporter extends SubtitleFileExporter {

    public static final String SUPPORTED_EXTENSION = "stl";

    private STLExporter() {
    }

    public static STLExporter getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    protected boolean exportFile(SubtitleEncoderInfo subtitleEncoderInfo) throws IOException {
        long beginTime = subtitleEncoderInfo.inputFilter().beginTime();
        long offset = subtitleEncoderInfo.inputFilter().offset();
        long recalculateTimeInterval = 0L;
        int selection = 0;
        if (subtitleEncoderInfo.subtitleTransformationOptions().reCalculateTime()) {
            recalculateTimeInterval = beginTime;
            offset = 0L;
        }

        List<SubtitleUnit> allUnits = new SubtitleSequencer().createSequence(subtitleEncoderInfo.transcription(), subtitleEncoderInfo.inputFilter().selectedForExportTierNames(),
            beginTime, subtitleEncoderInfo.inputFilter().endTime(), subtitleEncoderInfo.inputFilter().minimalDuration(), offset, true);

        writeFile(subtitleEncoderInfo.subtitleFile().targetFile(), subtitleEncoderInfo.subtitleFile().fileEncoding(), subtitleEncoderInfo.subtitleTransformationOptions().frameRate(), allUnits, selection, recalculateTimeInterval);
        return true;
    }

    private void writeFile(File exportFile, String encoding, double frameRate, List<SubtitleUnit> allUnits, int selection, long recalculateTimeInterval) throws IOException {
        SubtitleUnit unit;

        try (BufferedWriter writer = getBufferedWriter(exportFile, encoding)) {

            // Write the preamble.
            writer.write("$TapeOffset = False");
            writer.write(lineSeparator());

            for (int i = 0; i < allUnits.size(); i++) {
                unit = allUnits.get(i);

                Long b = unit.getBegin();
                Long e = unit.getCalcEnd();
                if (selection == 0 && (b < recalculateTimeInterval)) {
                        recalculateTimeInterval = b;
                        selection = 1;

                }
                writer.write(toSTLTimecode(b - recalculateTimeInterval, frameRate));
                writer.write(',');
                writer.write(toSTLTimecode(e - recalculateTimeInterval, frameRate));
                writer.write(',');

                String[] values = unit.getValues();
                for (int j = 0; j < values.length; j++) {
                    writer.write(values[j].replace('\n', '|'));

                    if (j < values.length - 1) {
                        writer.write('|');
                    }
                }

                writer.write(lineSeparator());
            }

        }
    }

    /**
     * Reformats a standard timecode into the format expected in Spruce STL.
     *
     * @param time      the time to convert
     * @param frameRate assumed frame rate; if negative, assume default
     * @return an STL-formatted timecode
     */
    private String toSTLTimecode(long time, double frameRate) {
        String tc = null;

        if (frameRate == 25.0) {
            // If PAL, return a PAL timecode.
            tc = TimeFormatter.toTimecodePAL(time);
        } else if (frameRate == 29.97) {
            // If NTSC (drop-frame), return a 30drop-SMPTE timecode.
            tc = TimeFormatter.toTimecodeNTSC(time);
        } else if (frameRate == 30.0) {
            // If this is NTSC (non-drop-frame), return an SMPTE timecode in
            // STL format, assuming a literal 29.27fps.  (This may seem odd,
            // but for DVD Studio Pro 4, using 30drop SMPTE with NTSC sources
            // produces synchronization drift).
            int fc = (int) ((time * 29.97) / 1000);
            String hours = twoDigits.format((((fc / 30) / 60) / 60) % 24);
            String minutes = twoDigits.format(((fc / 30) / 60) % 60);
            String seconds = twoDigits.format((fc / 30) % 60);
            String frames = twoDigits.format(fc % 30);
            tc = hours + ":" + minutes + ":" + seconds + "." + frames;
        } else {
            // Otherwise, default to PAL.
            tc = TimeFormatter.toTimecodePAL(time);
        }

        // Replace the final comma in the timecode with a period.
        tc = tc.substring(0, 8) + '.' + tc.substring(9);
        return tc;
    }

    private static class InstanceHolder {
        private static final STLExporter instance = new STLExporter();
    }

}
