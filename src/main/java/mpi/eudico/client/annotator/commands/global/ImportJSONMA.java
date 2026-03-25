package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.json.JSONWADecoderInfo;
import nl.mpi.util.FileExtension;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A menu action to import the JSON file
 */
@SuppressWarnings("serial")
public class ImportJSONMA extends FrameMenuAction {

    /**
     * The constructor to initialize the menu action
     *
     * @param name the name of the menu
     * @param frame the elan frame
     */
    public ImportJSONMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FileChooser chooser = new FileChooser(frame);
        //        chooser.createAndShowFileDialog(ElanLocale.getString("Frame.ElanFrame.OpenDialog.Title"), FileChooser
        //        .OPEN_DIALOG,
        //            FileExtension.JSON_EXT, "LastUsedJSONDir");
        chooser.createAndShowFileAndEncodingDialog(ElanLocale.getString("Frame.ElanFrame.OpenDialog.Title"),
                                                   FileChooser.OPEN_DIALOG,
                                                   FileExtension.JSON_EXT,
                                                   "LastUsedJSONDir",
                                                   "UTF-8");
        File fileTemp = chooser.getSelectedFile();
        if (fileTemp == null) {
            return;
        }
        try {
            String encoding = chooser.getSelectedEncoding();
            String filePath = fileTemp.getAbsolutePath().replace('\\', '/');
            JSONWADecoderInfo decoderInfo = new JSONWADecoderInfo(filePath);
            decoderInfo.setCharsetName(encoding);

            Transcription transcription = new TranscriptionImpl(filePath, decoderInfo);
            transcription.setChanged();

            // check valid media?
            int dotIndex = filePath.lastIndexOf('.');
            int slashIndex = filePath.lastIndexOf('/');
            if (slashIndex > -1 && dotIndex > slashIndex + 1) {
                transcription.setName(filePath.substring(slashIndex + 1, dotIndex));
            }

            FrameManager.getInstance().createFrame(transcription);
        } catch (Throwable t) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "Could not convert the JSON file: " + t.getMessage());
                t.printStackTrace();
            }
        }
    }


}
