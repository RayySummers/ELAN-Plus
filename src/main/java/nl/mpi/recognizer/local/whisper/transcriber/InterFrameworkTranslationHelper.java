package nl.mpi.recognizer.local.whisper.transcriber;

import java.util.Optional;

public class InterFrameworkTranslationHelper {

    public static final String CMDI_TRUE = "True";

    /**
     * Use this function to convert the string to correct capitalization case. Java stringifies the boolean values in
     * small letters. Python has first letter capital for boolean literals.
     *
     * Java : true & false
     * Python : True & False
     *
     * @param maybeBool is optionally available param with boolean literals.
     *
     * @return python acceptable capitalized string value corresponding to boolean literals.
     */
    public static Optional<String> toPythonBooleanString(Optional<Boolean> maybeBool) {
        Optional<String> maybeString = Optional.empty();
        if (maybeBool.isPresent()) {
            String boolString = maybeBool.get().toString();
            String capitalizedLetter = Character.toString(boolString.charAt(0)).toUpperCase();
            capitalizedLetter = capitalizedLetter.concat(boolString.substring(1));
            maybeString = Optional.of(capitalizedLetter);
        }
        return maybeString;
    }
}
