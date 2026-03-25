package nl.mpi.recognizer.local.whisper.transcriber;

import static nl.mpi.recognizer.local.whisper.transcriber.CommandCreator.UNKNOWN_OPTION;

public enum WhisperModel {
    TINY_EN("tiny.en"),
    TINY("tiny"),
    BASE_EN("base.en"),
    BASE("base"),
    SMALL_EN("small.en"),
    SMALL("small"),
    MEDIUM_EN("medium.en"),
    MEDIUM("medium"),
    LARGE_V1("large-v1"),
    LARGE_V2("large-v2"),
    LARGE_V3("large-v3"),
    LARGE("large");

    private final String modelNameKey;

    WhisperModel(String modelName) {
        modelNameKey = modelName;
    }

    /**
     * Gets enumerated item corresponding to the list.
     *
     * @param identifier model name.
     * @return item form enumerated list corresponding to the identifier.
     * @throws IllegalArgumentException if the identifier is not known.
     */
    public static WhisperModel fromString(String identifier) {
        if (identifier != null) {
            for (WhisperModel model : WhisperModel.values()) {
                if (model.modelNameKey.equals(identifier)) {
                    return model;
                }
            }
            throw new IllegalArgumentException(UNKNOWN_OPTION.formatted(identifier));
        }
        return null;
    }

    @Override
    public String toString() {
        return modelNameKey;
    }
}
