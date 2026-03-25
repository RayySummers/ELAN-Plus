package nl.mpi.recognizer.local.whisper.transcriber;

import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import nl.mpi.recognizer.local.whisper.WhisperStandaloneRecognizer;
import org.json.JSONArray;

import java.util.List;

import static java.util.Collections.singletonList;

class TranscriptionSegmentationStrategyFake extends TranscriptionSegmentationStrategy {

    protected TranscriptionSegmentationStrategyFake(WhisperStandaloneRecognizer pRecognizer) {
        super(pRecognizer);
    }

    @Override
    protected List<RSelection> getSegments(JSONArray segments) {
        return singletonList(new Segment());
    }

}
