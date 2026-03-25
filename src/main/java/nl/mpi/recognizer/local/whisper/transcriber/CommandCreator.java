package nl.mpi.recognizer.local.whisper.transcriber;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static nl.mpi.recognizer.local.whisper.transcriber.CoreInputField.*;
import static nl.mpi.recognizer.local.whisper.transcriber.InterFrameworkTranslationHelper.CMDI_TRUE;
import static nl.mpi.recognizer.local.whisper.transcriber.InterFrameworkTranslationHelper.toPythonBooleanString;
import static nl.mpi.recognizer.local.whisper.transcriber.WhisperResponseFormats.JSON;

public class CommandCreator {

    public static final String UNSELECTED = "--";
    public static final String KEY_PAIR_PLACEHOLDER = "--%s=%s";
    public static final String UNKNOWN_OPTION = "%s is unknown option";


    public String[] toCommandLineFormat(WhisperArguments whisperArguments) {
        List<String> command = new ArrayList<>();

        command.add(whisperArguments.executablePath());
        command.add(whisperArguments.inputAudioVideoFilePath());
        add(command, MODEL, whisperArguments.whisperModelToUse());

        optionalAdd(command, LANGUAGE, whisperArguments.expectedLanguageOfInputFile());
        optionalAdd(command, PROMPT, whisperArguments.prompt());
        optionalAdd(command, OUTPUT_FORMAT, whisperArguments.responseFormat());
        optionalAdd(command, TEMPERATURE, whisperArguments.temperature());
        optionalAdd(command, WORD_TIMESTAMPS, toPythonBooleanString(whisperArguments.wordTimestamps()));
        optionalAdd(command, OUTPUT_DIR, whisperArguments.outputDirectory());

        command.addAll(whisperArguments.misc());

        return command.toArray(new String[0]);
    }

    private <T> void add(List<String> command, CoreInputField key, T value) {
        command.add(KEY_PAIR_PLACEHOLDER.formatted(key.getName(), value));
    }

    private <T> void optionalAdd(List<String> command, CoreInputField key, Optional<T> optional) {
        optional.ifPresent(optionalValue -> add(command, key, optionalValue.toString()));
    }

    public WhisperArguments getWhisperArguments(
        Map<String, String> paramMapString,
        Map<String, Float> paramMapFloat,
        File baseDir) throws URISyntaxException {
        DirectoryPath directoryPath = new DirectoryPath();
        return new WhisperArguments(
            directoryPath.getDirPath(getMappedValue(paramMapString, RUN_COMMAND), baseDir),
            getMappedValue(paramMapString, AUDIO),
            WhisperModel.fromString(getMappedValue(paramMapString, MODEL)),
            Optional.ofNullable(WhisperLanguage.fromString(getMappedValue(paramMapString, LANGUAGE))),
            Optional.ofNullable(getMappedValue(paramMapString, PROMPT)),
            Optional.of(JSON),
            Optional.ofNullable(getMappedValue(paramMapString, TEMPERATURE)),
            Optional.of(CMDI_TRUE.equals(getMappedValue(paramMapString, WORD_TIMESTAMPS))),
            directoryPath.getOptionalOutDir(getMappedValue(paramMapString, OUTPUT_DIR)),
            getAdditionalParams(paramMapString, paramMapFloat)
        );
    }

    private String getMappedValue(Map<String, String> paramMapString, CoreInputField field) {
        String result = null;
        if (paramMapString != null) {
            result = paramMapString.get(field.getName());
        }
        return result;
    }

    private List<String> getAdditionalParams(
        Map<String, String> paramMapString,
        Map<String, Float> paramMapFloat
    ) {
        List<String> list = new ArrayList<>();

        includeUnlistedFields(paramMapString, list);
        includeUnlistedFields(paramMapFloat, list);

        return list;
    }

    private void includeUnlistedFields(Map<String, ?> paramMap, List<String> list) {
        Optional.ofNullable(paramMap)
            .orElse(emptyMap())
            .entrySet()
            .stream()
            .filter(entry -> !CoreInputField.contains(entry.getKey()))
            .filter(entry -> !UNSELECTED.equals(entry.getValue()))
            .map(RoundedFloatField::roundFloatEntries)
            .map(IntegerField::roundIntegerEntries)
            .forEach(entry -> list.add(KEY_PAIR_PLACEHOLDER.formatted(entry.getKey(), entry.getValue())));

    }

}