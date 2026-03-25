package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.gui.ImportCollectionFromServerDialog;
import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.json.JSONWADecoderInfo;
import mpi.eudico.server.corpora.clomimpl.json.WebAnnotationClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Menu action to import the collection(tier) from the annotation server and load it by creating a new transcription file.
 */
public class ImportCollectionFromServerMA extends FrameMenuAction {

    /**
     * serial version uid
     */
    private static final long serialVersionUID = -7744870159577304886L;

    /**
     * temp Json file name
     */
    public static final String JSON_FILE_NAME = "collection.json";

    /**
     * http status code 200
     */
    public static final int OK = 200;

    /**
     * http status code 401
     */
    public static final int UNAUTHORIZED = 401;

    private static String jsonFileName;

    /**
     * Creates a new ImportCollectionFromServerMA instance
     *
     * @param name the name of the action
     * @param frame the parent frame
     */
    public ImportCollectionFromServerMA(String name, ElanFrame2 frame) {
        super(name, frame);

    }

    /**
     * Creates a dialog to add the collection url to be loaded into the transcription file
     *
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        WebAnnotationClient annotationClient = new WebAnnotationClient();
        Boolean isAuthenticationEnabled = false;
        try {
            isAuthenticationEnabled = annotationClient.isAuthenticationEnabled();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        ImportCollectionFromServerDialog importDialog =
            new ImportCollectionFromServerDialog(frame, true, isAuthenticationEnabled);
        WindowLocationAndSizeManager.postInit(importDialog, "ImportCollectionFromServerDialog");
        importDialog.setVisible(true);

        String collectionIDUrl = importDialog.getCollectionID();

        String authenticationBearerKey = importDialog.getAuthenticationKey();

        if (!importDialog.isActionApplied() || collectionIDUrl.isEmpty()) {
            return;
        }
        collectionIDUrl = collectionIDUrl.replace('\\', '/');

        Map<Integer, String> response = annotationClient.importCollection(collectionIDUrl, authenticationBearerKey);

        if (response.containsKey(OK)) {
            try {
                String dir = System.getProperty("java.io.tmpdir");
                if (dir != null) {
                    jsonFileName = dir + Constants.FILESEPARATOR + JSON_FILE_NAME;
                }
                File jsonFile = new File(jsonFileName);
                FileWriter writer = new FileWriter(jsonFile);
                writer.write(response.get(OK));
                writer.close();

                JSONWADecoderInfo decoderInfo = new JSONWADecoderInfo(jsonFile.getAbsolutePath().replace('\\', '/'));
                decoderInfo.setCharsetName("UTF-8");

                TranscriptionImpl transcription = new TranscriptionImpl(jsonFile.toString(), decoderInfo);

                transcription.setName(TranscriptionImpl.UNDEFINED_FILE_NAME);
                transcription.setPathName(TranscriptionImpl.UNDEFINED_FILE_NAME);
                transcription.setChanged();

                FrameManager.getInstance().createFrame(transcription);

            } catch (IOException e1) {

                JOptionPane.showMessageDialog(frame,
                                              ElanLocale.getString("ImportCollection.Error.Message"),
                                              ElanLocale.getString("Message.Error"),
                                              JOptionPane.ERROR_MESSAGE);
            }

        } else if (response.containsKey(UNAUTHORIZED)) {
            String errorMessage = response.get(UNAUTHORIZED);
            JOptionPane.showMessageDialog(frame,
                                          errorMessage,
                                          ElanLocale.getString("Message.Error"),
                                          JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame,
                                          ElanLocale.getString("ImportCollection.Error.Message"),
                                          ElanLocale.getString("Message.Error"),
                                          JOptionPane.ERROR_MESSAGE);
        }

    }

}
