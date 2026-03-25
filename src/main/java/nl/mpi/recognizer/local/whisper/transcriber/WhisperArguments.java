package nl.mpi.recognizer.local.whisper.transcriber;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public record WhisperArguments(
        String executablePath,
        String inputAudioVideoFilePath,
        WhisperModel whisperModelToUse,
        Optional<WhisperLanguage> expectedLanguageOfInputFile,
        Optional<String> prompt,
        Optional<WhisperResponseFormats> responseFormat,
        Optional<String> temperature,
        Optional<Boolean> wordTimestamps,
        Optional<URI> outputDirectory,
        List<String> misc
) {
}
