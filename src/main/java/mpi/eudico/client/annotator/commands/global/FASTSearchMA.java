package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.search.viewer.FASTSearchFrame;

import java.awt.event.ActionEvent;

/**
 * A menu action to show the FAST Search frame.
 *
 * @author Larwan Berke, DePaul
 * @version 1.0
 * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
 * @since June 2013
 */
public class FASTSearchMA extends FrameMenuAction {
    private static final long serialVersionUID = -501944440841850928L;

    /**
     * Constructor to initialize the menu action
     *
     * @param name the name of the menu
     * @param frame the frame
     */
    public FASTSearchMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FASTSearchFrame searchFrame = new FASTSearchFrame(frame);
        searchFrame.setLocationRelativeTo(frame);
        searchFrame.setVisible(true);
    }
}
