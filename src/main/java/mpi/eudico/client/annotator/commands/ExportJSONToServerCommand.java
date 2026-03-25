package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.export.ExportJSONDialog;
import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.json.WebAnnotationClient;

import java.util.logging.Level;

import static mpi.eudico.server.corpora.util.ServerLogger.LOG;

/**
 * Shows a dialog window to select the tiers to be loaded to annotation server
 */
public class ExportJSONToServerCommand implements Command {

    private final String name;
    private Transcription transcription;

    /**
     * Constructor.
     *
     * @param name the name
     */
    public ExportJSONToServerCommand(String name) {
        this.name = name;
    }

    /**
     * @param receiver null
     * @param arguments [0] the transcription, [1] the selection
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        if (arguments[0] instanceof Transcription) {
            transcription = (Transcription) arguments[0];
        }

        if (transcription == null) {
            return;
        }
        WebAnnotationClient annotationClient = new WebAnnotationClient();
        Boolean isAuthenticationEnabled = false;
        try {
            isAuthenticationEnabled = annotationClient.isAuthenticationEnabled();
        } catch (Exception e1) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "Error when getting the server info");
            }
        }

        ExportJSONDialog window = new ExportJSONDialog(ELANCommandFactory.getRootFrame(transcription),
                                                       transcription,
                                                       (Selection) arguments[1],
                                                       true,
                                                       isAuthenticationEnabled);
        WindowLocationAndSizeManager.postInit(window, "ExportJSONDialog");
        window.setVisible(true);
        WindowLocationAndSizeManager.storeLocationAndSizePreferences(window, "ExportJSONDialog");
    }

    @Override
    public String getName() {
        return name;
    }


}
