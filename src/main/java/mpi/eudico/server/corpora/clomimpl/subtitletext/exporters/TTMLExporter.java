package mpi.eudico.server.corpora.clomimpl.subtitletext.exporters;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import mpi.eudico.client.util.SubtitleUnit;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.subtitletext.SubtitleSequencer;
import mpi.eudico.server.corpora.clomimpl.subtitletext.dto.SubtitleEncoderInfo;
import mpi.eudico.util.TimeFormatter;

import static java.lang.System.lineSeparator;

public class TTMLExporter extends SubtitleFileExporter {

    public static final String SUPPORTED_EXTENSION = "xml";

    private TTMLExporter() {
    }

    public static TTMLExporter getInstance() {
        return TTMLExporter.InstanceHolder.instance;
    }

    /***
     * example:
     *  <tt xmlns="http://www.w3.org/ns/ttml" xml:lang="en">
     *  <body>
     *   <div>
     *     <p begin="00:00:22" end="00:00:27">
     *       I'll teach thee Bugology, Ignatzes
     *     </p>
     *     <p begin="00:00:40" end="00:00:43">
     *       Something tells me
     *     </p>
     *     <p begin="00:00:58" end="00:00:64">
     *       Look, Ignatz, a sleeping bee
     *     </p>
     *   </div>
     *  </body>
     * </tt>
     *
     * @param subtitleEncoderInfo
     * @return True/False as answer to question : Was the export successful?
     * @throws IOException
     */
    @Override
    public boolean exportFile(SubtitleEncoderInfo subtitleEncoderInfo) throws IOException {
        Transcription transcription = subtitleEncoderInfo.transcription();
        String encoding = subtitleEncoderInfo.subtitleFile().fileEncoding();
        long beginTime = subtitleEncoderInfo.inputFilter().beginTime();
        long offset = subtitleEncoderInfo.inputFilter().offset();
        long recalculateTimeInterval = 0L;
        int selection = 0;
        if (subtitleEncoderInfo.subtitleTransformationOptions().reCalculateTime()) {
            recalculateTimeInterval = beginTime;
            offset = 0L;
        }

        SubtitleSequencer sequencer = new SubtitleSequencer();

        List<SubtitleUnit> allUnits = sequencer.createSequence(transcription, subtitleEncoderInfo.inputFilter().selectedForExportTierNames(),
            beginTime, subtitleEncoderInfo.inputFilter().endTime(), subtitleEncoderInfo.inputFilter().minimalDuration(), offset, true);

        SubtitleUnit unit;
        try (BufferedWriter writer = getBufferedWriter(subtitleEncoderInfo.subtitleFile().targetFile(), encoding)) {
            final String IND1 = "    ";
            final String IND2 = "        ";
            final String IND3 = "            ";
            // write "header"
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write(lineSeparator());
            writer.write("<tt xmlns=\"http://www.w3.org/ns/ttml\">");
            writer.write(lineSeparator());
            writer.write(IND1 + "<head>");
            writer.write(lineSeparator());
            writer.write(IND2 + "<metadata xmlns:ttm=\"http://www.w3.org/ns/ttml#metadata\">");
            writer.write(lineSeparator());
            writer.write(IND3 + "<ttm:title>" + transcription.getName() + "</ttm:title>");
            writer.write(lineSeparator());
            writer.write(IND2 + "</metadata>");
            writer.write(lineSeparator());
            writer.write(IND1 + "</head>");
            writer.write(lineSeparator());
            writer.write(IND1 + "<body><div>");
            writer.write(lineSeparator());

            // body contents
            for (int i = 0; i < allUnits.size(); i++) {
                unit = allUnits.get(i);

                Long b = unit.getBegin();
                Long e = unit.getCalcEnd();
                if (selection == 0 && (b < recalculateTimeInterval)) {
                        recalculateTimeInterval = b;
                        selection = 1;

                }

                writer.write(IND2 + "<p begin=\"");
                writer.write(TimeFormatter.toSSMSString(b - recalculateTimeInterval));
                writer.write("s\" end=\"");
                writer.write(TimeFormatter.toSSMSString(e - recalculateTimeInterval));
                writer.write("s\">");
                writer.write(lineSeparator());

                for (int j = 0; j < unit.getValues().length; j++) {
                    writer.write(IND3 + unit.getValues()[j].replace('\n', ' '));

                    if (j != (unit.getValues().length - 1)) {
                        writer.write("<br/>");
                        writer.write(lineSeparator());
                    }
                }

                writer.write(lineSeparator());
                writer.write(IND2 + "</p>");
                writer.write(lineSeparator());
            }
            // closing tags
            //writer.write(WIN_NEWLINE);
            writer.write(IND1 + "</div></body>");
            writer.write(lineSeparator());
            writer.write("</tt>");

        }
        return true;
    }

    private static class InstanceHolder {
        private static final TTMLExporter instance = new TTMLExporter();
    }

}
