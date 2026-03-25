package mpi.eudico.server.corpora.clomimpl.subtitletext.exporters;

import java.io.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import mpi.eudico.server.corpora.clomimpl.subtitletext.dto.SubtitleEncoderInfo;

import static java.nio.charset.StandardCharsets.UTF_8;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import static nl.mpi.util.FileUtility.getFileExtension;

public abstract class SubtitleFileExporter {

    public static final String FILE_EXPORT_STATUS_MSG = "The number of expected files / files exported : %d / %d";
    DecimalFormat twoDigits = new DecimalFormat("00");

    public static <E extends SubtitleFileExporter> E getExporter(File exportFile) {
        Optional<String> fileExtension = getFileExtension(exportFile);

        SubtitleFileExporter subtitleFileExporter = null;

        if (fileExtension.isPresent()) {
            String expectedFileExtension = fileExtension.get();

            subtitleFileExporter = switch (expectedFileExtension) {
                case STLExporter.SUPPORTED_EXTENSION -> STLExporter.getInstance();
                case LRCExporter.SUPPORTED_EXTENSION -> LRCExporter.getInstance();
                case TTMLExporter.SUPPORTED_EXTENSION -> TTMLExporter.getInstance();
                default -> SRTExporter.getInstance();
            };
        }
        //noinspection unchecked
        return (E) subtitleFileExporter;
    }

    private static void logExportStatus(int numberOfFilesToBeExported, int numberOfFilesSuccessfullyExported) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info(FILE_EXPORT_STATUS_MSG.formatted(numberOfFilesToBeExported, numberOfFilesSuccessfullyExported));
        } else if (LOG.isLoggable(Level.WARNING) && numberOfFilesSuccessfullyExported != numberOfFilesToBeExported) {
            LOG.warning(FILE_EXPORT_STATUS_MSG.formatted(numberOfFilesToBeExported, numberOfFilesSuccessfullyExported));
        }
    }

    protected abstract boolean exportFile(SubtitleEncoderInfo subtitleEncoderInfo) throws IOException;

    public boolean export(SubtitleEncoderInfo subtitleEncoderInfo) {
        int numberOfFilesToBeExported = 1;
        int numberOfFilesSuccessfullyExported;
        if (subtitleEncoderInfo.subtitleTransformationOptions().exportTiersSeparately()) {

            List<String> tiers = subtitleEncoderInfo.inputFilter().selectedForExportTierNames();

            numberOfFilesToBeExported = tiers.size();

            numberOfFilesSuccessfullyExported = tiers
                .stream()
                .map(tier -> {
                    SubtitleEncoderInfo subtitleEncoderInfoWithFilteredTiers = filterOutOtherTiers(subtitleEncoderInfo, tier);
                    return exportFiles(subtitleEncoderInfoWithFilteredTiers);
                })
                .mapToInt(Integer::intValue)
                .sum();
        } else {
            numberOfFilesSuccessfullyExported = exportFiles(subtitleEncoderInfo);
        }

        logExportStatus(numberOfFilesToBeExported, numberOfFilesSuccessfullyExported);

        return numberOfFilesSuccessfullyExported == numberOfFilesToBeExported;
    }

    protected BufferedWriter getBufferedWriter(File exportFile, String encoding) throws FileNotFoundException {
        FileOutputStream out = null;

        out = new FileOutputStream(exportFile);

        BufferedWriter writer;
        if (encoding != null) {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(out, encoding));
            } catch (UnsupportedEncodingException uee) {
                LOG.warning("Encoding not supported: " + encoding);
                writer = new BufferedWriter(new OutputStreamWriter(out, UTF_8));
            }
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(out, UTF_8));
        }
        return writer;
    }

    private int exportFiles(SubtitleEncoderInfo subtitleEncoderInfoWithFilteredTiers) {
        int filesExported = 0;
        try {
            if (exportFile(subtitleEncoderInfoWithFilteredTiers)) {
                filesExported = 1;
            }
        } catch (IOException e) {
            LOG.warning("The file could not be exported: " + e.getMessage());
        }
        return filesExported;
    }

    private SubtitleEncoderInfo filterOutOtherTiers(SubtitleEncoderInfo subtitleEncoderInfo, String tierName) {
        return SubtitleEncoderInfo
            .withTargetFileAndExportTier(
                subtitleEncoderInfo,
                tierName
            );
    }

}
