package mpi.eudico.client.annotator.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Shows a message in a text area in a {@link JOptionPane}.
 */
public class TextAreaMessageDlg {

    public TextAreaMessageDlg(Component c, String[] text, String title) {
        showMessage(c, text, title);
    }

    public TextAreaMessageDlg(Component c, String text, String title) {
        showMessage(c, new String[] {text}, title);
    }

    private void showMessage(Component parent, String[] text, String title) {
        JTextArea ta = new JTextArea();
        final String nl = "\n";
        for (String s : text) {
            ta.append(s);
            if (text.length > 1) {
                ta.append(nl);
            }
        }
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        JScrollPane pane = new JScrollPane(ta);
        pane.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
                                      pane,
                                      title == null ? "ELAN" : title,
                                      JOptionPane.PLAIN_MESSAGE,
                                      null);
    }
}
