package mpi.eudico.client.annotator.gui;

import javax.swing.*;
import java.awt.*;


/**
 * A frame with registered actions to close (dispose) the frame with the {@code Escape} or {@code Ctrl-W} ({@code Command-W})
 * key events.
 */
@SuppressWarnings("serial")
public class ClosableFrame extends JFrame {
    /**
     * Constructor.
     *
     * @throws HeadlessException if the frame is created in a headless environment
     */
    public ClosableFrame() throws
                           HeadlessException {
        super();
        addCloseActions();
    }

    /**
     * Constructor.
     *
     * @param gc the graphics configuration
     */
    public ClosableFrame(GraphicsConfiguration gc) {
        super(gc);
        addCloseActions();
    }

    /**
     * Constructor.
     *
     * @param title the title
     *
     * @throws HeadlessException if the frame is created in a headless environment
     */
    public ClosableFrame(String title) throws
                                       HeadlessException {
        super(title);
        addCloseActions();
    }

    /**
     * Constructor.
     *
     * @param title the title
     * @param gc the graphics configuration
     */
    public ClosableFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
        addCloseActions();
    }

    /**
     * Add the {@code Escape} and {@code Ctrl-W} close actions.
     */
    protected void addCloseActions() {
        EscCloseAction escAction = new EscCloseAction(this);
        CtrlWCloseAction ctrlWCloseAction = new CtrlWCloseAction(this);

        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        if (inputMap instanceof ComponentInputMap && (actionMap != null)) {
            String esc = "esc";
            inputMap.put((KeyStroke) escAction.getValue(Action.ACCELERATOR_KEY), esc);
            actionMap.put(esc, escAction);

            String wcl = "cw";
            inputMap.put((KeyStroke) ctrlWCloseAction.getValue(Action.ACCELERATOR_KEY), wcl);
            actionMap.put(wcl, ctrlWCloseAction);
        }
    }
}
