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

class PhraseTiersHandlerTest {

    @Mock
    private WhisperStandaloneRecognizer recognizer;

    @Test
    void testGetSegments() throws IOException {
        Path resourceDirectory = Paths.get("src", "test", "resources", "whisper", "phrase_segments_202403191054.json");
        String content = new String(Files.readAllBytes(resourceDirectory));
        JSONObject jsonObject = new JSONObject(content);
        JSONArray segments = jsonObject.getJSONArray("segments");

        List<RSelection> rSelections = new PhraseTiersHandler(recognizer).getSegments(segments);
        assertEquals(2, rSelections.size());
        assertItem(rSelections.get(0), 0, 3620, " Lorem ipsum.");
        assertItem(rSelections.get(1), 5380, 7380, " Aut quisquam.");
    }

    private static void assertItem(RSelection rSelection, long start, long end, String label) {
        assertEquals(start, rSelection.beginTime);
        assertEquals(end, rSelection.endTime);
        assertEquals(label, ((Segment) rSelection).label);
    }

    @Test
    void testGetSegmentsForNull() {
        PhraseTiersHandler phraseTiersHandler = new PhraseTiersHandler(recognizer);
        assertThrows(NullPointerException.class, () -> phraseTiersHandler.getSegments(null));
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(0, new JSONObject());
        assertThrows(JSONException.class, () -> phraseTiersHandler.getSegments(jsonArray));
    }
}
