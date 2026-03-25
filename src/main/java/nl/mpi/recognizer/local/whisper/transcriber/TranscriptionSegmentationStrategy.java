package nl.mpi.recognizer.local.whisper.transcriber;

import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import nl.mpi.recognizer.local.whisper.WhisperStandaloneRecognizer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static nl.mpi.recognizer.local.whisper.transcriber.DirectoryPath.FORWARD_SLASH;
import static nl.mpi.recognizer.local.whisper.transcriber.DirectoryPath.USER_HOME;
import static nl.mpi.recognizer.local.whisper.transcriber.WhisperResponseFormats.JSON;

public abstract class TranscriptionSegmentationStrategy {

    public static final String SEGMENTS = "segments";
    public static final String WORDS = "words";
    public static final String PHRASE = "phrase";
    public static final String NO_SEGMENTATION_WAS_LOADED = "No segmentation was loaded\n";
    public static final String FILE_EXTENSION_PATTERN = "\\.[^.]+$";
    protected WhisperStandaloneRecognizer recognizer;

    protected TranscriptionSegmentationStrategy(WhisperStandaloneRecognizer pRecognizer) {
        recognizer = pRecognizer;
    }

    public static TranscriptionSegmentationStrategy getStrategy(WhisperStandaloneRecognizer pRecognizer) {

        TranscriptionSegmentationStrategy strategy;
        if (pRecognizer.getWhisperArguments().wordTimestamps().orElse(false)) {
            strategy = new WordTiersHandler(pRecognizer);
        } else {
            strategy = new PhraseTiersHandler(pRecognizer);
        }

        return strategy;
    }

    public void extractData() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(getOutputFilePath(recognizer.getWhisperArguments()))));
        JSONObject jsonObject = new JSONObject(content);
        JSONArray segments = jsonObject.getJSONArray(SEGMENTS);

        String segmentationType = recognizer.getWhisperArguments().wordTimestamps().orElse(false) ? WORDS : PHRASE;
        createTiers(getSegments(segments), segmentationType);
    }

    protected abstract List<RSelection> getSegments(JSONArray segments);

    protected void createTiers(List<RSelection> segments, String segmentationType) {
        if (!segments.isEmpty()) {
            final Segmentation wordSegmentation = new Segmentation(segmentationType, segments, recognizer.getWhisperArguments().inputAudioVideoFilePath());
            // add the segmentation on the event dispatch thread
            EventQueue.invokeLater(() -> recognizer.getHost().addSegmentation(wordSegmentation));
            recognizer.getHost().setProgress(1.0f);
        } else {
            recognizer.getHost().appendToReport(NO_SEGMENTATION_WAS_LOADED);
        }
    }

    private String getOutputFilePath(WhisperArguments whisperArguments) {

        String fileName = Paths
            .get(whisperArguments.inputAudioVideoFilePath())
            .getFileName()
            .toString()
            .replaceAll(FILE_EXTENSION_PATTERN, "." + JSON);

        return whisperArguments
            .outputDirectory()
            .map(URI::toString)
            .map(dirPath -> !dirPath.endsWith(FORWARD_SLASH) ? dirPath.concat(FORWARD_SLASH) : dirPath)
            .orElse(USER_HOME)
            .concat(fileName);
    }

    public static long convertStringToLongWithFactor(String str, int factor) {
        return (long) (Double.parseDouble(str) * factor);
    }
}
