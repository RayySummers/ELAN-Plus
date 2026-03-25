package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.subtitletext.SubtitleDecoderInfo;
import mpi.eudico.server.corpora.clomimpl.subtitletext.SubtitleFormat;
import nl.mpi.util.FileExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A command that creates an import dialog for subtitle text or Audacity Label files, gets the settings from the dialog and
 * starts the actual, undoable import process.
 */
public class ImportSubtitleTextDlgCommand implements Command {
    private final String commandName;

    /**
     * Creates a Import Subtitle text dialog.
     *
     * @param name the name of the command
     */
    public ImportSubtitleTextDlgCommand(String name) {
        super();
        commandName = name;
    }

    /**
     * Creates an import dialog which prompts for an input file and its encoding. It creates a DecoderInfo object based on
     * the file extension and finally executes the actual import command.
     *
     * @param receiver the Transcription
     * @param arguments null
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object, java.lang.Object[])
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl trans = (TranscriptionImpl) receiver;

        FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(trans));
        // next section is more or less a copy of the ImportSubtitleTextMA action
        List<String[]> extensions = new ArrayList<>();
        extensions.add(FileExtension.SUBRIP_EXT); // for now only add srt as extension
        extensions.add(FileExtension.WEBVTT_EXT); // supported as far as it is almost identical to SubRip
        extensions.add(FileExtension.TEXT_EXT);

        chooser.createAndShowFileAndEncodingDialog(ElanLocale.getString("Frame.ElanFrame.OpenDialog.Title"),
                                                   // generic Open title
                                                   FileChooser.OPEN_DIALOG,
                                                   null,
                                                   // or Import
                                                   extensions,
                                                   FileExtension.SUBRIP_EXT,
                                                   false,
                                                   // add the all files filter?
                                                   "LastUsedSubtitlesDir",
                                                   FileChooser.getEncodings(),
                                                   // utf-8, utf-16, CP-1252?
                                                   FileChooser.UTF_8,
                                                   // utf-8
                                                   FileChooser.FILES_ONLY,
                                                   null);

        File subFile = chooser.getSelectedFile();
        String charSet = chooser.getSelectedEncoding();

        if (subFile != null) {
            String fullPath = subFile.getAbsolutePath();
            fullPath = fullPath.replace('\\', '/');
            String lowerPath = fullPath.toLowerCase(Locale.getDefault());

            SubtitleDecoderInfo decoderInfo = new SubtitleDecoderInfo();
            decoderInfo.setSourceFilePath(fullPath);

            if (lowerPath.endsWith("srt")) {
                decoderInfo.setFormat(SubtitleFormat.SUBRIP);
            } else if (lowerPath.endsWith("vtt")) {
                decoderInfo.setFormat(SubtitleFormat.WEBVTT);
            } else if (lowerPath.endsWith("txt")) {
                decoderInfo.setFormat(SubtitleFormat.AUDACITY_lABELS);
            }

            decoderInfo.setFileEncoding(charSet);
            // this action triggers the same command as the import of csv, tsv
            Command com = ELANCommandFactory.createCommand(trans, ELANCommandFactory.IMPORT_SUBTITLE);
            com.execute(trans, new Object[] {decoderInfo});

        } // else silently return
    }

    @Override
    public String getName() {
        return commandName;
    }

}
