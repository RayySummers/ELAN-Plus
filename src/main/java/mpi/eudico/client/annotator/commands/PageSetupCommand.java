package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.server.corpora.clom.Transcription;

import java.awt.print.PageFormat;


/**
 * Show a Page Setup dialog.
 *
 * @author Hennie Brugman
 * @author Han Sloetjes
 */
public class PageSetupCommand implements Command {
    private final String commandName;

    /**
     * Creates a new PageSetupCommand instance
     *
     * @param name name of the command
     */
    public PageSetupCommand(String name) {
        commandName = name;
    }

    /**
     * Shows the dialog.
     *
     * @param receiver the transcription
     * @param arguments null
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        Transcription tr = (Transcription) receiver;
        PrintCommand printCommand = PrintCommand.getInstance();

        PageFormat pf = printCommand.getPrintJob().pageDialog(printCommand.getPageFormat());
        printCommand.setPageFormat(pf);

        // store in preferences
        Preferences.set("PageFormat.Height", pf.getHeight(), null);
        Preferences.set("PageFormat.Width", pf.getWidth(), null);
        Preferences.set("PageFormat.ImgX", pf.getImageableX(), null);
        Preferences.set("PageFormat.ImgY", pf.getImageableY(), null);
        Preferences.set("PageFormat.ImgHeight", pf.getImageableHeight(), null);
        Preferences.set("PageFormat.ImgWidth", pf.getImageableWidth(), null);
        Preferences.set("PageFormat.Orientation", pf.getOrientation(), null);
    }

    @Override
    public String getName() {
        return commandName;
    }
}
