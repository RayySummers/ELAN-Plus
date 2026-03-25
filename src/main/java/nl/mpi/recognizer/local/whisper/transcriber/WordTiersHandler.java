package nl.mpi.recognizer.local.whisper.transcriber;

import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import nl.mpi.recognizer.local.whisper.WhisperStandaloneRecognizer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.IntStream;

public class WordTiersHandler extends TranscriptionSegmentationStrategy {

    public static final String START = "start";
    public static final String END = "end";
    public static final String WORD = "word";
    public static final String WORDS = "words";
    public static final int MILLI_SECONDS_IN_SECOND = 1000;

    public WordTiersHandler(WhisperStandaloneRecognizer pRecognizer) {
        super(pRecognizer);
    }

    protected List<RSelection> getSegments(JSONArray segments) {
        // Convert the outer JSONArray to a Stream, then flatMap to "words" JSONArray
        return IntStream.range(0, segments.length())
            .mapToObj(segments::getJSONObject)
            .map(segment -> segment.getJSONArray(WORDS))
            .flatMap(words -> IntStream.range(0, words.length()).mapToObj(words::getJSONObject))
            .map(WordTiersHandler::getRSelection)
            .toList();
    }

    private static RSelection getRSelection(JSONObject word) {
        return new Segment(
                convertStringToLongWithFactor(word.get(START).toString(), MILLI_SECONDS_IN_SECOND),
                convertStringToLongWithFactor(word.get(END).toString(), MILLI_SECONDS_IN_SECOND),
                word.get(WORD).toString()
            );
    }

}
