package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.export.ExportToolboxDialog;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.*;


/**
 * A command that creates an export toolbox dialog.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ExportToolboxDlgCommand implements Command {
    private final String commandName;

    /**
     * Creates a new ExportToolboxCommand instance
     *
     * @param theName the name
     */
    public ExportToolboxDlgCommand(String theName) {
        commandName = theName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:  <ul><li>arg[0] = the Transcription object(TranscriptionImpl)</li> </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        if (arguments[0] instanceof TranscriptionImpl) {
            ExportToolboxDialog dialog =
                new ExportToolboxDialog(ELANCommandFactory.getRootFrame((Transcription) arguments[0]),
                                        true,
                                        (TranscriptionImpl) arguments[0]);
            dialog.setVisible(true);
            //System.out.println("Size: " + dialog.getSize());
            try {
                Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
                Insets ins = Toolkit.getDefaultToolkit().getScreenInsets(dialog.getGraphicsConfiguration());
                Dimension windowDim = dialog.getSize();
                int maxW = screenDim.width - ins.left - ins.top;
                int maxH = screenDim.height - ins.top - ins.bottom;
                if (windowDim.width > maxW || windowDim.height > maxH) {
                    dialog.setSize(Math.min(maxW, windowDim.width), Math.min(maxH, windowDim.height));
                }
            } catch (Throwable ex) {
                // just catch any throwable and do nothing
            }
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
