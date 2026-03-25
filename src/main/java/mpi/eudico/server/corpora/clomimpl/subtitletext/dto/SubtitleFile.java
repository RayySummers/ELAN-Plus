package mpi.eudico.server.corpora.clomimpl.subtitletext.dto;

import java.io.File;
import java.util.Objects;

public record SubtitleFile(File targetFile, String fileEncoding) {

    public SubtitleFile {
        Objects.requireNonNull(targetFile, "targetFile cannot be null");
    }

    public static SubtitleFile withTierName(SubtitleFile subtitleFile, String tierName) {
        return new SubtitleFile(getFileWithTierName(subtitleFile.targetFile, tierName), subtitleFile.fileEncoding);
    }

    private static File getFileWithTierName(File exportFile, String tierName) {
        String originalFileName = exportFile.getAbsolutePath();
        int index = originalFileName.lastIndexOf('.');
        String individualFileName = "";
        if (index >= 0) {
            String extension = originalFileName.substring(index);
            individualFileName = originalFileName.substring(0, index) + " - " + tierName + extension;
        }

        return new File(individualFileName);
    }
}
