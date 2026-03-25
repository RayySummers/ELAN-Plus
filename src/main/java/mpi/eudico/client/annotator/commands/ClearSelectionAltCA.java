package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;


/**
 * Alternative with only Alt key.
 *
 * @author Han Sloetjes
 */
public class ClearSelectionAltCA extends ClearSelectionCA {
    /**
     * Constructor.
     *
     * @param theVM the viewermanager
     */
    public ClearSelectionAltCA(ViewerManager2 theVM) {
        super(theVM);
        putValue(SMALL_ICON, null);
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
    }
}
