package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.export.ExportJSONDialog;
import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;
import mpi.eudico.server.corpora.clom.Transcription;

import static mpi.eudico.client.annotator.commands.ELANCommandFactory.getRootFrame;

/**
 * Shows a dialog window to export transcription-data to JSON
 *
 * @author Allan van Hulst
 */
public class ExportJSONCommand implements Command {
    private final String name;

    /**
     * Constructor.
     *
     * @param name the name
     */
    public ExportJSONCommand(String name) {
        this.name = name;
    }

    /**
     * @param receiver null
     * @param arguments [0] the transcription, [1] the selection
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        if (arguments[0] instanceof Transcription transcription) {
            ExportJSONDialog window =
                new ExportJSONDialog(getRootFrame(transcription), transcription, (Selection) arguments[1]);
            WindowLocationAndSizeManager.postInit(window, "ExportJSONDialog");
            window.setVisible(true);
            WindowLocationAndSizeManager.storeLocationAndSizePreferences(window, "ExportJSONDialog");
        }
    }

    @Override
    public String getName() {
        return name;
    }

}
