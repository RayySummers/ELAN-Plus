package nl.mpi.recognizer.local.whisper.transcriber;

import java.util.AbstractMap;
import java.util.Map;

public enum IntegerField implements InputField {
    BEST_OF("best_of"),
    BEAM_SIZE("beam_size");

    private final String fieldName;

    IntegerField(String pFieldName) {
        fieldName = pFieldName;
    }

    @Override
    public String getName() {
        return fieldName;
    }

    public static boolean contains(String identifier) {
        boolean isPresent = false;
        for (IntegerField fields : IntegerField.values()) {
            if (fields.getName().equals(identifier)) {
                isPresent = true;
                break;
            }
        }
        return isPresent;
    }

    public static Map.Entry<String, ?> roundIntegerEntries(Map.Entry<String, ?> entry) {
        Map.Entry<String, ?> result = entry;

        if (entry != null) {
            String key = entry.getKey();
            if (contains(key)) {
                result = new AbstractMap.SimpleEntry<>(key, Math.round((Float) entry.getValue()));
            }
        }

        return result;
    }

}
