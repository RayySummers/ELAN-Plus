package nl.mpi.recognizer.local.whisper.transcriber;

import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static nl.mpi.recognizer.local.whisper.transcriber.CoreInputField.LANGUAGE;
import static nl.mpi.recognizer.local.whisper.transcriber.CoreInputField.MODEL;
import static nl.mpi.recognizer.local.whisper.transcriber.WhisperLanguage.EN;
import static nl.mpi.recognizer.local.whisper.transcriber.WhisperModel.BASE;
import static nl.mpi.recognizer.local.whisper.transcriber.WhisperModel.SMALL_EN;
import static nl.mpi.recognizer.local.whisper.transcriber.WhisperResponseFormats.JSON;

public class CommandCreatorTestDataProvider {

    private static final String USER_HOME = System.getProperty("user.home").replace("\\", "/");

    private static final String BASE_PATH = "/Users/hafreh/elan_extensions/whisper-standalone";
    public static Stream<Arguments> provideParamsForGetWhisperArguments() throws URISyntaxException {

        return Stream.of(
            Arguments.of(getStringStringMap(), getStringFloatMap(), BASE_PATH, getWhisperArguments()),
            Arguments.of(getStringStringMapWithDifferentValues(), getStringFloatMap(), BASE_PATH,getWhisperArgumentsWithDifferentValues()),
            Arguments.of(getMandatoryArguments(BASE.toString()), emptyMap(), BASE_PATH,getArgumentsWithEmptyOptionals()),
            Arguments.of(getMandatoryArguments(SMALL_EN.toString()), emptyMap(), BASE_PATH, getArgumentsWithDifferentValues()),
            Arguments.of(getMandatoryArguments(BASE.toString()), null, BASE_PATH, getArgumentsWithEmptyOptionals()),
            Arguments.of(emptyMap(), emptyMap(), BASE_PATH, getArgumentsWithMandatoriesNull()),
            Arguments.of(null, null, BASE_PATH, getArgumentsWithMandatoriesNull())
        );
    }

    public static Stream<Arguments> provideParamsForGetWhisperArgumentsExceptionCase() {
        LinkedHashMap<String, String> invalidLanguageCodeArguments = getStringStringMap();
        invalidLanguageCodeArguments.put(LANGUAGE.getName(), "notALanguageCode");

        LinkedHashMap<String, String> invalidModelArguments = getStringStringMap();
        invalidModelArguments.put(MODEL.getName(), "notAKnownModel");

        return Stream.of(
            Arguments.of(invalidLanguageCodeArguments, getStringFloatMap(), BASE_PATH),
            Arguments.of(invalidModelArguments, getStringFloatMap(), BASE_PATH),
            Arguments.of(invalidModelArguments, Collections.singletonMap("unprocessedFloat", 20240318.1447), BASE_PATH)
        );
    }

    public static Stream<Arguments> provideParamsForToCommandLineFormat() throws URISyntaxException {
        return Stream.of(
            Arguments.of(new CommandCreator().getWhisperArguments(getStringStringMap(), getStringFloatMap(), new File("/Users/hafreh/elan_extensions/whisper-standalone")), new String[]{
                USER_HOME + "/elan_extensions/whisper-standalone/whisper_standalone202403141150",
                "a202403141151.mp4",
                "--model=base",
                "--language=en",
                "--initial_prompt=samplePrompt2024031800",
                "--output_format=json",
                "--temperature=1.0",
                "--word_timestamps=True",
                "--output_dir="+USER_HOME+"/whisper/whisper_output",
                "--best_of=1",
                "--beam_size=1",
                "--length_penalty=20240314.0"
            })
        );
    }

    private static Map<String, Float> getStringFloatMap() {
        Map<String, Float> floatMap = new HashMap<>();
        floatMap.put("best_of", 1.0f);
        floatMap.put("beam_size", 1.0f);
        floatMap.put("length_penalty", 20240314.1207f);
        return floatMap;
    }

    private static LinkedHashMap<String, String> getStringStringMap() {
        LinkedHashMap<String, String> stringParams = getMandatoryArguments(BASE.toString());
        stringParams.put("language", EN.getCode());
        stringParams.put("output_format", JSON.toString());
        stringParams.put("initial_prompt", "samplePrompt2024031800");
        stringParams.put("temperature", "1.0");
        stringParams.put("word_timestamps", "True");
        stringParams.put("output_dir", "whisper_output");
        return stringParams;
    }

    private static LinkedHashMap<String, String> getStringStringMapWithDifferentValues() {
        LinkedHashMap<String, String> stringParams = getMandatoryArguments(BASE.toString());
        stringParams.put("language", EN.getCode());
        stringParams.put("output_format", JSON.toString());
        stringParams.put("initial_prompt", "samplePrompt2024031800");
        stringParams.put("temperature", "1.0");
        stringParams.put("word_timestamps", "True");
        stringParams.put("output_dir", "whisper_output");
        stringParams.put("unlistedField202403190956", "--");
        stringParams.put("unlistedField202403190957", "valid unseen value");
        return stringParams;
    }

    private static LinkedHashMap<String, String> getMandatoryArguments(String model) {
        LinkedHashMap<String, String> stringParams = new LinkedHashMap<>();
        stringParams.put("run-command", "whisper_standalone202403141150");
        stringParams.put("audio", "a202403141151.mp4");
        stringParams.put("model", model);
        return stringParams;
    }

    private static WhisperArguments getWhisperArguments() throws URISyntaxException {
        return new WhisperArguments(USER_HOME + "/elan_extensions/whisper-standalone/whisper_standalone202403141150",
            "a202403141151.mp4",
            BASE,
            Optional.of(EN),
            Optional.of("samplePrompt2024031800"),
            Optional.of(JSON),
            Optional.of("1.0"),
            Optional.of(TRUE),
            Optional.of(new URI(USER_HOME + "/whisper/whisper_output")),
            Arrays.asList("--best_of=1", "--beam_size=1", "--length_penalty=20240314.0"));
    }

    private static WhisperArguments getWhisperArgumentsWithDifferentValues() throws URISyntaxException {
        return new WhisperArguments(USER_HOME + "/elan_extensions/whisper-standalone/whisper_standalone202403141150",
            "a202403141151.mp4",
            BASE,
            Optional.of(EN),
            Optional.of("samplePrompt2024031800"),
            Optional.of(JSON),
            Optional.of("1.0"),
            Optional.of(TRUE),
            Optional.of(new URI(USER_HOME + "/whisper/whisper_output")),
            Arrays.asList("--unlistedField202403190957=valid unseen value", "--best_of=1", "--beam_size=1", "--length_penalty=20240314.0"));
    }

    private static WhisperArguments getArgumentsWithEmptyOptionals() throws URISyntaxException {
        return new WhisperArguments(USER_HOME + "/elan_extensions/whisper-standalone/whisper_standalone202403141150",
            "a202403141151.mp4",
            BASE,
            Optional.empty(),
            Optional.empty(),
            Optional.of(JSON),
            Optional.empty(),
            Optional.of(FALSE),
            Optional.of(new URI(USER_HOME + "/whisper/")),
            emptyList());
    }

    private static WhisperArguments getArgumentsWithDifferentValues() throws URISyntaxException {
        return new WhisperArguments(USER_HOME + "/elan_extensions/whisper-standalone/whisper_standalone202403141150",
            "a202403141151.mp4",
            SMALL_EN,
            Optional.empty(),
            Optional.empty(),
            Optional.of(JSON),
            Optional.empty(),
            Optional.of(FALSE),
            Optional.of(new URI(USER_HOME + "/whisper/")),
            emptyList());
    }

    private static WhisperArguments getArgumentsWithMandatoriesNull() throws URISyntaxException {
        return new WhisperArguments(null,
            null,
            null,
            Optional.empty(),
            Optional.empty(),
            Optional.of(JSON),
            Optional.empty(),
            Optional.of(FALSE),
            Optional.of(new URI(USER_HOME + "/whisper/")),
            emptyList());
    }

}
