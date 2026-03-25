package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
//import nl.mpi.util.FileUtility;

/**
 * Allows to enter or paste a URL to an EAF file to be opened in ELAN.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class OpenRemoteMA extends OpenMA {

    /**
     * Creates the new OpenRemoteMA instance
     *
     * @param name the menu action
     * @param frame the containing frame
     */
    public OpenRemoteMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Instead of a file chooser for a local file, this action shows an input dialog where a URL can be typed or pasted.
     *
     * @param e the menu action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // the width of the dialog is rather small, set a minimum width
        JOptionPane pane = new JOptionPane(ElanLocale.getString("Frame.ElanFrame.OpenDialog.RemoteLabel"),
                                           JOptionPane.PLAIN_MESSAGE,
                                           JOptionPane.OK_CANCEL_OPTION);
        pane.setWantsInput(true);

        Dimension parentDim = frame.getSize();
        Dialog d = pane.createDialog(frame, ElanLocale.getString("Frame.ElanFrame.NewDialog.RemoteMedia"));
        Dimension paneDim = d.getSize();
        d.setMinimumSize(new Dimension(Math.max(paneDim.width, (int) (parentDim.getWidth() * 0.5)), paneDim.height));
        d.setLocationRelativeTo(frame);
        d.setResizable(true);
        d.setVisible(true);

        Object option = pane.getValue();
        if (option == null) {
            // the dialog was closed via window close button
            return;
        }
        // OK or Cancel has been clicked
        if (option instanceof Integer && JOptionPane.OK_OPTION != ((Integer) option).intValue()) {
            return;
        }
        Object rf = pane.getInputValue();

        if (rf == null) {
            return;
        }

        String url = ((String) rf).strip();
        if (url.isEmpty()) {
            return;
        }
        url = url.replace('\\', '/');
        // try some simple repairs
        //boolean valid = FileUtility.isRemoteFile(url);
        // additional checks

        createFrameForPath(url);
    }


}
