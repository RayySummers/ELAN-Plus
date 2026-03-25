package nl.mpi.recognizer.local.whisper.transcriber;

public enum CoreInputField implements InputField {
    RUN_COMMAND("run-command"),
    AUDIO("audio"),
    MODEL("model"),
    LANGUAGE("language"),
    OUTPUT_FORMAT("output_format"),
    PROMPT("initial_prompt"),
    TEMPERATURE("temperature"),
    WORD_TIMESTAMPS("word_timestamps"),
    OUTPUT_DIR("output_dir");

    private final String fieldName;

    CoreInputField(String pFieldName) {
        fieldName = pFieldName;
    }

    @Override
    public String getName() {
        return fieldName;
    }

    public static boolean contains(String identifier) {
        boolean isPresent = false;
        for (CoreInputField fields : CoreInputField.values()) {
            if (fields.fieldName.equals(identifier)) {
                isPresent = true;
                break;
            }
        }
        return isPresent;
    }
}

