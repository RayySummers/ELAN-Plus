package mpi.eudico.client.annotator.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;


/**
 * An action to close a window by pressing the {@code Escape} key.
 */
@SuppressWarnings("serial")
public class EscCloseAction extends AbstractAction {
    private final Window window;

    /**
     * Constructor.
     *
     * @param window the window to close
     */
    public EscCloseAction(Window window) {
        super();
        this.window = window;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (window != null) {
            window.dispose();
        }
    }
}
