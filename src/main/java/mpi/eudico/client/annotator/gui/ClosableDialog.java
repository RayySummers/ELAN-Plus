package mpi.eudico.client.annotator.gui;

import javax.swing.*;
import java.awt.*;


/**
 * A dialog with registered actions to close (dispose) the dialog with the {@code Escape} or {@code Ctrl-W}
 * ({@code Command-W}) key events.
 */
@SuppressWarnings("serial")
public class ClosableDialog extends JDialog {
    /**
     * Constructor
     *
     * @throws HeadlessException if the dialog is used in a headless environment
     */
    public ClosableDialog() throws
                            HeadlessException {
        this((Frame) null, false);
    }

    /**
     * Constructor
     *
     * @param owner the owner frame
     *
     * @throws HeadlessException if the dialog is used in a headless environment
     */
    public ClosableDialog(Frame owner) throws
                                       HeadlessException {
        this(owner, null, false);
    }

    /**
     * Constructor
     *
     * @param owner the owner frame
     * @param modal whether the dialog is modal or not
     *
     * @throws HeadlessException if the dialog is used in a headless environment
     */
    public ClosableDialog(Frame owner, boolean modal) throws
                                                      HeadlessException {
        this(owner, null, modal);
    }

    /**
     * Constructor
     *
     * @param owner the owner frame
     * @param title the dialog title
     *
     * @throws HeadlessException if the dialog is used in a headless environment
     */
    public ClosableDialog(Frame owner, String title) throws
                                                     HeadlessException {
        this(owner, title, false);
    }

    /**
     * Constructor, calls super's constructor and adds close actions.
     *
     * @param owner the owner frame
     * @param title the dialog title
     * @param modal whether the dialog is modal or not
     *
     * @throws HeadlessException if the dialog is used in a headless environment
     */
    public ClosableDialog(Frame owner, String title, boolean modal) throws
                                                                    HeadlessException {
        super(owner, title, modal);
        addCloseActions();
    }

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param title the dialog title
     * @param modal whether the dialog is modal or not
     * @param gc the graphics configuration
     */
    public ClosableDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
        addCloseActions();
    }

    /**
     * Constructor.
     *
     * @param owner the owner frame
     *
     * @throws HeadlessException if the dialog is used in a headless environment
     */
    public ClosableDialog(Dialog owner) throws
                                        HeadlessException {
        this(owner, false);
    }

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param modal whether the dialog is modal or not
     *
     * @throws HeadlessException if the dialog is used in a headless environment
     */
    public ClosableDialog(Dialog owner, boolean modal) throws
                                                       HeadlessException {
        this(owner, null, modal);
    }

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param title the dialog title
     *
     * @throws HeadlessException if the dialog is used in a headless environment
     */
    public ClosableDialog(Dialog owner, String title) throws
                                                      HeadlessException {
        this(owner, title, false);
    }

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param title the dialog title
     * @param modal whether the dialog is modal or not
     *
     * @throws HeadlessException if the dialog is used in a headless environment
     */
    public ClosableDialog(Dialog owner, String title, boolean modal) throws
                                                                     HeadlessException {
        super(owner, title, modal);
        addCloseActions();
    }

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param title the dialog title
     * @param modal whether the dialog is modal or not
     * @param gc the graphics configuration
     *
     * @throws HeadlessException if the dialog is used in a headless environment
     */
    public ClosableDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) throws
                                                                                               HeadlessException {
        super(owner, title, modal, gc);
        addCloseActions();
    }

    /**
     * Add the {@code Escape} and {@code Ctrl-W} close actions.
     */
    protected void addCloseActions() {
        EscCloseAction escAction = new EscCloseAction(this);
        CtrlWCloseAction controlWAction = new CtrlWCloseAction(this);

        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        if (inputMap instanceof ComponentInputMap && (actionMap != null)) {
            String esc = "esc";
            inputMap.put((KeyStroke) escAction.getValue(Action.ACCELERATOR_KEY), esc);
            actionMap.put(esc, escAction);

            String wcl = "cw";
            inputMap.put((KeyStroke) controlWAction.getValue(Action.ACCELERATOR_KEY), wcl);
            actionMap.put(wcl, controlWAction);
        }
    }
}
