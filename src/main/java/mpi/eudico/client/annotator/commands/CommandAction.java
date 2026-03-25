package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.SystemReporting;

import javax.swing.*;
import java.awt.event.ActionEvent;


/**
 * The base class for actions that combine with a command. The {@code actionPerformed()} calls the
 * {@link Command#execute(Object, Object[])} method with the appropriate parameters
 */
@SuppressWarnings("serial")
public abstract class CommandAction extends AbstractAction implements ElanLocaleListener {
    /**
     * a prefix for mnemonic keys
     */
    public static final String MNEMONIC = "MNEMONIC.";
    /**
     * the command
     */
    protected Command command;
    private final String commandId;

    /**
     * the viewer manager
     */
    protected ViewerManager2 vm;
    private static boolean useMnemonics = true;

    static {
        if (SystemReporting.isMacOS()) {
            useMnemonics = false;
        }
    }

    /**
     * Creates a new command action instance.
     *
     * @param theVM the viewer manager
     * @param name the name or id of the command
     */
    public CommandAction(ViewerManager2 theVM, String name) {
        super(name);

        vm = theVM;
        commandId = name;

        ElanLocale.addElanLocaleListener(vm.getTranscription(), this);
        //        putValue(Action.ACCELERATOR_KEY,
        //                ShortcutsUtil.getInstance().getKeyStrokeForAction(commandId, null));
        updateLocale();
    }

    /**
     * Creates a new CommandAction instance
     *
     * @param theVM the viewer manager
     * @param name the name of the command
     * @param icon an optional icon for the action (for use on a button or label)
     */
    public CommandAction(ViewerManager2 theVM, String name, Icon icon) {
        super(name, icon);

        vm = theVM;
        commandId = name;

        ElanLocale.addElanLocaleListener(vm.getTranscription(), this);
        updateLocale();
    }

    /**
     * Creates a new command
     */
    protected abstract void newCommand();

    /**
     * Returns the receiver of the command
     *
     * @return the receiver of the command
     */
    protected Object getReceiver() {
        return null;
    }

    /**
     * Returns arguments for the command.
     *
     * @return an array of arguments for the execution of the command
     */
    protected Object[] getArguments() {
        return null;
    }

    /**
     * Creates a command, if necessary, and calls its {@code execute} method
     *
     * @param event the action event, not used in this method
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        newCommand();

        if (command != null) {
            command.execute(getReceiver(), getArguments());
        }
    }

    /**
     * Sets the keystroke action
     *
     * @param ks the keystroke
     */
    public void setActionKeyStroke(KeyStroke ks) {
        putValue(Action.ACCELERATOR_KEY, ks);
    }

    /**
     * Updates the label or description and the tooltip based on the current Locale.
     */
    @Override
    public void updateLocale() {
        Object newString = null;

        if (commandId != null) {
            newString = ElanLocale.getString(commandId);
        }

        //when there is an icon, set text to empty string (otherwise text appears on a button)
        //also handle the tooltip text
        Object[] obj = getKeys();

        for (Object o : obj) {
            if (o.equals("SmallIcon")) {
                newString = "";
                break;
            }
        }

        Object object = getValue(Action.SHORT_DESCRIPTION);

        if ((object == null) || (!object.equals(""))) {
            putValue(Action.SHORT_DESCRIPTION, ElanLocale.getString(commandId + "ToolTip"));
        }

        putValue(Action.NAME, newString);

        if (useMnemonics) {
            String mnemonic = ElanLocale.getString(MNEMONIC + commandId);
            if (!mnemonic.isEmpty()) {
                try {
                    putValue(Action.MNEMONIC_KEY, Integer.valueOf(mnemonic.charAt(0)));
                } catch (NumberFormatException nfe) {
                    try {
                        putValue(Action.MNEMONIC_KEY, Integer.valueOf(Integer.parseInt(mnemonic)));
                    } catch (NumberFormatException nfe2) {
                        putValue(Action.MNEMONIC_KEY, null);
                    }
                }
            }
        }
    }
}
