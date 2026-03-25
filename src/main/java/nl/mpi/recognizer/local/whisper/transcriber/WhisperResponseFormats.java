package nl.mpi.recognizer.local.whisper.transcriber;

public enum WhisperResponseFormats {
    TXT("txt"),
    VTT("vtt"),
    SRT("srt"),
    TSV("tsv"),
    JSON("json"),
    ALL("all");

    private final String whisperOutputFormat;

    WhisperResponseFormats(String type) {
        whisperOutputFormat = type;
    }

    @Override
    public String toString() {
        return whisperOutputFormat;
    }
}
