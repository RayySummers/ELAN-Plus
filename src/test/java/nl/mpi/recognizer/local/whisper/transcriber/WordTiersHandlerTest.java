package nl.mpi.recognizer.local.whisper.transcriber;

import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import nl.mpi.recognizer.local.whisper.WhisperStandaloneRecognizer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WordTiersHandlerTest {

    @Mock
    private WhisperStandaloneRecognizer recognizer;

    @Test
    void testGetSegments() throws IOException {
        Path resourceDirectory = Paths.get("src", "test", "resources", "whisper", "word_segments_202403191054.json");
        String content = new String(Files.readAllBytes(resourceDirectory));
        JSONObject jsonObject = new JSONObject(content);
        JSONArray segments = jsonObject.getJSONArray("segments");

        List<RSelection> rSelections = new WordTiersHandler(recognizer).getSegments(segments);
        assertEquals(5, rSelections.size());

        assertItem(rSelections.get(0), 0, 20, " It's");
        assertItem(rSelections.get(1), 20, 140, " a");
        assertItem(rSelections.get(2), 780, 1420, " wedding,");
        assertItem(rSelections.get(3), 2120, 2460, " Wasn't");
        assertItem(rSelections.get(4), 2460, 2700, " it?");
    }

    private static void assertItem(RSelection rSelection, long start, long end, String label) {
        assertEquals(start, rSelection.beginTime);
        assertEquals(end, rSelection.endTime);
        assertEquals(label, ((Segment) rSelection).label);
    }

    @Test
    void testGetSegmentsForNull() {
        WordTiersHandler wordTiersHandler = new WordTiersHandler(recognizer);
        assertThrows(NullPointerException.class, () -> wordTiersHandler.getSegments(null));
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(0, new JSONObject());
        assertThrows(JSONException.class, () -> wordTiersHandler.getSegments(jsonArray));
    }
}
