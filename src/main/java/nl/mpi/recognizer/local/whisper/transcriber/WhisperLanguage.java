package nl.mpi.recognizer.local.whisper.transcriber;

import java.util.Arrays;
import java.util.List;

import static nl.mpi.recognizer.local.whisper.transcriber.CommandCreator.UNKNOWN_OPTION;

public enum WhisperLanguage {
    EN("en", "english", "--" /*default language*/),
    YUE("yue", "cantonese"),
    ZH("zh", "chinese", "mandarin"),
    DE("de", "german"),
    ES("es", "spanish", "castilian"),
    RU("ru", "russian"),
    KO("ko", "korean"),
    FR("fr", "french"),
    JA("ja", "japanese"),
    PT("pt", "portuguese"),
    TR("tr", "turkish"),
    PL("pl", "polish"),
    CA("ca", "catalan", "valencian"),
    NL("nl", "dutch", "flemish"),
    AR("ar", "arabic"),
    SV("sv", "swedish"),
    IT("it", "italian"),
    ID("id", "indonesian"),
    HI("hi", "hindi"),
    FI("fi", "finnish"),
    VI("vi", "vietnamese"),
    HE("he", "hebrew"),
    UK("uk", "ukrainian"),
    EL("el", "greek"),
    MS("ms", "malay"),
    CS("cs", "czech"),
    RO("ro", "romanian", "moldavian", "moldovan"),
    DA("da", "danish"),
    HU("hu", "hungarian"),
    TA("ta", "tamil"),
    NO("no", "norwegian"),
    TH("th", "thai"),
    UR("ur", "urdu"),
    HR("hr", "croatian"),
    BG("bg", "bulgarian"),
    LT("lt", "lithuanian"),
    LA("la", "latin"),
    MI("mi", "maori"),
    ML("ml", "malayalam"),
    CY("cy", "welsh"),
    SK("sk", "slovak"),
    TE("te", "telugu"),
    FA("fa", "persian"),
    LV("lv", "latvian"),
    BN("bn", "bengali"),
    SR("sr", "serbian"),
    AZ("az", "azerbaijani"),
    SL("sl", "slovenian"),
    KN("kn", "kannada"),
    ET("et", "estonian"),
    MK("mk", "macedonian"),
    BR("br", "breton"),
    EU("eu", "basque"),
    IS("is", "icelandic"),
    HY("hy", "armenian"),
    NE("ne", "nepali"),
    MN("mn", "mongolian"),
    BS("bs", "bosnian"),
    KK("kk", "kazakh"),
    SQ("sq", "albanian"),
    SW("sw", "swahili"),
    GL("gl", "galician"),
    MR("mr", "marathi"),
    PA("pa", "punjabi", "panjabi"),
    SI("si", "sinhala", "sinhalese"),
    KM("km", "khmer"),
    SN("sn", "shona"),
    YO("yo", "yoruba"),
    SO("so", "somali"),
    AF("af", "afrikaans"),
    OC("oc", "occitan"),
    KA("ka", "georgian"),
    BE("be", "belarusian"),
    TG("tg", "tajik"),
    SD("sd", "sindhi"),
    GU("gu", "gujarati"),
    AM("am", "amharic"),
    YI("yi", "yiddish"),
    LO("lo", "lao"),
    UZ("uz", "uzbek"),
    FO("fo", "faroese"),
    HT("ht", "haitian creole", "haitian"),
    PS("ps", "pashto", "pushto"),
    TK("tk", "turkmen"),
    NN("nn", "nynorsk"),
    MT("mt", "maltese"),
    SA("sa", "sanskrit"),
    LB("lb", "luxembourgish", "letzeburgesch"),
    MY("my", "myanmar", "burmese"),
    BO("bo", "tibetan"),
    TL("tl", "tagalog"),
    MG("mg", "malagasy"),
    AS("as", "assamese"),
    TT("tt", "tatar"),
    HAW("haw", "hawaiian"),
    LN("ln", "lingala"),
    HA("ha", "hausa"),
    BA("ba", "bashkir"),
    JW("jw", "javanese"),
    SU("su", "sundanese");

    private final String code;
    private final String name;
    private final List<String> alternativeNames;

    WhisperLanguage(final String code, final String name, final String... otherNames) {
        this.code = code;
        this.name = name;
        alternativeNames = Arrays.asList(otherNames);
    }

    @Override
    public String toString() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * Gets enumerated item corresponding to the list.
     *
     * @param identifier language 2 char code, language name or language alternative name.
     * @return item from of enumerated list corresponding to the identifier.
     * @throws IllegalArgumentException if the identifier is not known.
     */
    public static WhisperLanguage fromString(String identifier) {
        if (identifier != null) {
            for (WhisperLanguage language : WhisperLanguage.values()) {
                if (matches(language, identifier)) {
                    return language;
                }
            }
            throw new IllegalArgumentException(UNKNOWN_OPTION.formatted(identifier));
        }
        return null;
    }

    public static boolean matches(WhisperLanguage language, String identifier) {
        return language.code.equalsIgnoreCase(identifier) ||
                language.name.equalsIgnoreCase(identifier) ||
                language.alternativeNames.contains(identifier);
    }

}
