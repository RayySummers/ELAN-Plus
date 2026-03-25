package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.help.HelpException;
import mpi.eudico.client.annotator.help.HelpWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


/**
 * A menu action that creates a new help contents window, or brings the existing window to front.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class HelpMA extends FrameMenuAction {
    private static Window window;

    /**
     * Creates a new HelpMA instance
     *
     * @param name the name of the action
     * @param frame the frame
     */
    public HelpMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates and/or shows the help window.
     *
     * @param e the event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (window != null) {
            if (!window.isVisible()) {
                window.setVisible(true);
            }

            window.toFront();
        } else {
            try {
                window = HelpWindow.getHelpWindow();
                window.setLocationRelativeTo(frame);
                window.setVisible(true);
            } catch (HelpException he) {
                String message = he.getMessage();

                if (he.getCause() != null) {
                    message += ("\n" + he.getCause().getMessage());
                }

                JOptionPane.showMessageDialog(frame,
                                              ElanLocale.getString("Message.NoHelp") + "\n" + message,
                                              ElanLocale.getString("Menu.Help"),
                                              JOptionPane.ERROR_MESSAGE,
                                              null);
            }
        }
    }
}
