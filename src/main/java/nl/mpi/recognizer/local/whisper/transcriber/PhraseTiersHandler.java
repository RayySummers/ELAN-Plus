package nl.mpi.recognizer.local.whisper.transcriber;

import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import nl.mpi.recognizer.local.whisper.WhisperStandaloneRecognizer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.IntStream;

public class PhraseTiersHandler extends TranscriptionSegmentationStrategy {

    public static final String START = "start";
    public static final String END = "end";
    public static final String TEXT = "text";
    public static final int MILLI_SECONDS_IN_SECOND = 1000;

    public PhraseTiersHandler(WhisperStandaloneRecognizer pRecognizer) {
        super(pRecognizer);
    }

    @Override
    protected List<RSelection> getSegments(JSONArray segments) {
        return IntStream.range(0, segments.length())
            .mapToObj(segments::getJSONObject)
            .map(PhraseTiersHandler::getRSelection)
            .toList();
    }

    private static RSelection getRSelection(JSONObject segment) {
        return new Segment(
            convertStringToLongWithFactor(segment.get(START).toString(), MILLI_SECONDS_IN_SECOND),
            convertStringToLongWithFactor(segment.get(END).toString(), MILLI_SECONDS_IN_SECOND),
            segment.get(TEXT).toString()
        );
    }

}
