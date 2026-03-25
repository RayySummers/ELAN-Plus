package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;


/**
 * Alternative for the Ctrl + / combination.
 *
 * @author HS
 * @version 1.0
 */
public class ActiveSelectionBoundaryAltCA extends ActiveSelectionBoundaryCA {
    /**
     * Creates a new ActiveSelectionBoundaryAltCA instance
     *
     * @param theVM the viewer manager
     */
    public ActiveSelectionBoundaryAltCA(ViewerManager2 theVM) {
        super(theVM);

        putValue(Action.ACCELERATOR_KEY,
                 KeyStroke.getKeyStroke(KeyEvent.VK_K,
                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + ActionEvent.SHIFT_MASK));
    }
}
