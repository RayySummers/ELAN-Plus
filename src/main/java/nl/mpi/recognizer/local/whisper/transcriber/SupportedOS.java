package nl.mpi.recognizer.local.whisper.transcriber;

public enum SupportedOS {
    LINUX("LINUX"),
    MAC("MAC"),
    WIN("WINDOWS");

    private final String osName;

    SupportedOS(String name) {
        osName = name;
    }

    @Override
    public String toString() {
        return osName;
    }
}
