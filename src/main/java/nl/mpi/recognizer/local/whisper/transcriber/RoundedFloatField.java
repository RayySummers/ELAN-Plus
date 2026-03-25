package nl.mpi.recognizer.local.whisper.transcriber;

import java.util.AbstractMap;
import java.util.Locale;
import java.util.Map;

public enum RoundedFloatField implements InputField {
    LENGTH_PENALTY("length_penalty");

    private final String fieldName;

    RoundedFloatField(String pFieldName) {
        fieldName = pFieldName;
    }

    @Override
    public String getName() {
        return fieldName;
    }

    public static boolean contains(String identifier) {
        boolean isPresent = false;
        for (RoundedFloatField fields : RoundedFloatField.values()) {
            if (fields.getName().equals(identifier)) {
                isPresent = true;
                break;
            }
        }
        return isPresent;
    }

    public static Map.Entry<String, ?> roundFloatEntries(Map.Entry<String, ?> entry) {
        Map.Entry<String, ?> result = entry;
        String key = entry.getKey();
        if (contains(key)) {
            float value = (float) entry.getValue();
            result = new AbstractMap.SimpleEntry<>(key, String.format(Locale.US, "%.1f", value));
        }
        return result;
    }
}

