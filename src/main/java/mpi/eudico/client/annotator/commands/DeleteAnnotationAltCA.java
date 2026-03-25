package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * An alternative for the {@code Alt-D} CommandAction; on (some?) MacOS 10.4 systems {@code Alt-D} (sometimes) doesn't delete
 * the annotation but invokes some kind of input method window. This is an alternative. Also the {@code Alt-<single-char>}
 * combinations interfere with the mnemonics access to the menu's.
 *
 * @author Han Sloetjes, MPI
 */
public class DeleteAnnotationAltCA extends DeleteAnnotationCA {

    /**
     * Constructor.
     *
     * @param viewerManager the viewer manager
     */
    public DeleteAnnotationAltCA(ViewerManager2 viewerManager) {
        super(viewerManager);

        putValue(Action.ACCELERATOR_KEY,
                 KeyStroke.getKeyStroke(KeyEvent.VK_D,
                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + ActionEvent.ALT_MASK));
    }

}
