package nl.mpi.recognizer.local.whisper.transcriber;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class DirectoryPath {

    public static final String BACK_SLASHES = "\\";
    public static final String FORWARD_SLASH = "/";
    public static final String KEY_USER_HOME = "user.home";
    public static final String KEY_OS_NAME = "os.name";
    public static final String OS_NAME = System.getProperty(KEY_OS_NAME);
    public static final String USER_HOME = System.getProperty(KEY_USER_HOME).replace("\\", "/");
    public static final String WHISPER_HOME = "/whisper/";
    public static final String WHISPER_STANDALONE_EXEC_LOCATION = "/elan_extensions/whisper-standalone/";

    public static final String WHISPER_OUT_HOME = USER_HOME.concat(WHISPER_HOME);
    public static final String EXE = ".exe";

    public String getDirPath(String path, File baseDir) {
        String commandExecutable = path;

        if (isNotAbsolutePath(commandExecutable)) {
            commandExecutable = baseDir.toString().concat(FORWARD_SLASH).concat(path);
        }
        if (getOsName() != null && getOsName().toLowerCase().startsWith("win")) {
            commandExecutable = addMissingExeExtension(commandExecutable);
        }

        return commandExecutable;
    }

    private static String addMissingExeExtension(String commandExecutable) {
        if (commandExecutable != null && !commandExecutable.endsWith(EXE)) {
            return commandExecutable + EXE;
        }
        return commandExecutable;
    }

    private boolean isNotAbsolutePath(String commandExecutable) {
        return commandExecutable != null &&
              !commandExecutable.isBlank() &&
              !new File(commandExecutable).isAbsolute();
    }

    public Optional<URI> getOptionalOutDir(String path) throws URISyntaxException {
        String outDirPath = path;

        if (path != null && !path.isBlank()) {
            if (isNotAbsolutePath(path)) {
                outDirPath = WHISPER_OUT_HOME.concat(getWithoutDuplicateSlash(path));
            }
        } else {
            outDirPath = WHISPER_OUT_HOME;
        }

        outDirPath = outDirPath.replace(BACK_SLASHES, FORWARD_SLASH);

        return Optional.of(new URI(outDirPath));
    }

    private static String getWithoutDuplicateSlash(String path) {
        return hasStartingSlash(path) ? path.substring(1) : path;
    }

    private static boolean hasStartingSlash(String path) {
        return path.startsWith(FORWARD_SLASH) || path.startsWith(BACK_SLASHES);
    }

    public String getOsName() {
        return OS_NAME;
    }
}
