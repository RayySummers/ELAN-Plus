package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.gui.EditRecentLanguagesDialog;

import java.awt.event.ActionEvent;

/**
 * A MenuAction that brings up a JDialog for editing a list of "recent" content languages.
 *
 * @author Olaf Seibert
 */
@SuppressWarnings("serial")
public class EditLanguagesMA extends MenuAction {

    /**
     * The constructor to set the menu action name
     *
     * @param name the name of the menu action
     */
    public EditLanguagesMA(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new EditRecentLanguagesDialog(null).setVisible(true);
    }
}
