package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ImportCollectionFromServerDialog;
import mpi.eudico.client.annotator.imports.UndoableTranscriptionMerger;
import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.json.JSONWADecoderInfo;
import mpi.eudico.server.corpora.clomimpl.json.WebAnnotationClient;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.UTF_8;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A command that imports the collection(tier) from the annotation server and loads tier to the existing opened transcription
 * file.
 */
public class ImportCollectionFromServerCommand implements UndoableCommand {

    private final String name;
    private TranscriptionImpl transcription;

    /**
     * name of the json file which saves the imported collection json
     */
    public static final String JSON_FILE_NAME = "collection.json";

    /**
     * file name string
     */
    private static String jsonFileName;

    /**
     * http response status OK
     */
    public static final int OK = 200;

    /**
     * http response status UNAUTHORIZED
     */
    public static final int UNAUTHORIZED = 401;

    TierImpl tier = null;

    /**
     * merger utility with undo/redo
     */
    protected UndoableTranscriptionMerger transMerger = null;

    /**
     * Constructor to create the new ImportCollectionFromServerCommand instance.
     *
     * @param name the name
     */
    public ImportCollectionFromServerCommand(String name) {
        this.name = name;
    }

    /**
     * Creates a dialog to enter the collection id to be imported from the server. After getting the collection, loads the
     * collection as a tier in the opened transcription file.
     *
     * @param receiver the receiver
     * @param arguments the arguments:
     *                 <ul>
     *                 <li>arguments[0] = the transcription file</li>
     *     </ul>
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object, java.lang.Object[])
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        if (arguments[0] instanceof Transcription) {
            transcription = (TranscriptionImpl) arguments[0];
        }

        if (transcription == null) {
            return;
        }
        WebAnnotationClient annotationClient = new WebAnnotationClient();
        Boolean isAuthenticationEnabled = false;
        try {
            isAuthenticationEnabled = annotationClient.isAuthenticationEnabled();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        Frame parent = ELANCommandFactory.getRootFrame(transcription);

        ImportCollectionFromServerDialog importDialog =
            new ImportCollectionFromServerDialog(parent, true, isAuthenticationEnabled);
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
                try (FileWriter writer = new FileWriter(jsonFile, UTF_8)) {
                    writer.write(response.get(OK));
                }

                JSONWADecoderInfo decoderInfo = new JSONWADecoderInfo(jsonFile.getAbsolutePath().replace('\\', '/'));
                decoderInfo.setCharsetName("UTF-8");

                try {
                    TranscriptionImpl newTranscription = new TranscriptionImpl(jsonFile.toString(), decoderInfo);

                    // start merging into the target transcription
                    if (!newTranscription.getTiers().isEmpty()) {

                        // use UndoableTranscriptionMerger here, or is it overkill?
                        transMerger = new UndoableTranscriptionMerger();
                        transMerger.mergeWith(transcription, newTranscription, true);
                    }
                } catch (Throwable t) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("Error while importing text: " + t.getMessage());
                    }
                }

            } catch (IOException e1) {
                JOptionPane.showMessageDialog(parent,
                                              ElanLocale.getString("ImportCollection.Error.Message"),
                                              ElanLocale.getString("Message.Error"),
                                              JOptionPane.ERROR_MESSAGE);

            }
        } else if (response.containsKey(UNAUTHORIZED)) {
            String errorMessage = response.get(UNAUTHORIZED);
            JOptionPane.showMessageDialog(parent,
                                          errorMessage,
                                          ElanLocale.getString("Message.Error"),
                                          JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(parent,
                                          ElanLocale.getString("ImportCollection.Error.Message"),
                                          ElanLocale.getString("Message.Error"),
                                          JOptionPane.ERROR_MESSAGE);
        }

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void undo() {
        if (transMerger != null) {
            transMerger.undo();
        }

    }

    @Override
    public void redo() {
        if (transMerger != null) {
            transMerger.redo();
        }
    }

}
