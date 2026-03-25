package nl.mpi.recognizer.local.whisper.transcriber;

public class PlatformUtil {

    public static String getOperatingSystem() {
        String os = "";
        String property = System.getProperty("os.name");
        if (property != null && !property.isBlank()) {
            property = property.toLowerCase();
            if (property.contains("mac")) {
                os = "Mac.jar";
            } else if (property.contains("win")) {
                os = "Win.exe";
            } else {
                os = "Ubuntu";
            }
        }

        return os;
    }
}
