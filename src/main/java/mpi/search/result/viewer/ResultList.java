package mpi.search.result.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;

import mpi.search.result.model.Match;
import mpi.search.result.model.ResultHandler;


/**
 * A list of results.
 * 
 * @author klasal
 */
@SuppressWarnings("serial")
public class ResultList extends JList<Match> implements MouseListener {
    /** to choose an action which the result handler should perform */
    private JPopupMenu actionsMenu;

    /** handler that does something with selected match number */
    private ResultHandler resultHandler;

    /**
     * Creates a new ResultList object.
     */
    public ResultList() {
        this(null);
    }

    /**
     * Creates a new ResultList object.
     *
     * @param resultHandler the handler of the results
     */
    public ResultList(ResultHandler resultHandler) {
        super();
        this.resultHandler = resultHandler;
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addMouseListener(this);
     }

    /**
     * Sets the options to add to the context menu.
     *  
     * @param popupChoices the actions to add to the context popup menu of the list 
     */
    public void setPopupChoices(String[] popupChoices) {
        if (popupChoices.length > 0) {
            actionsMenu = new JPopupMenu();

            ActionListener actionListener = new ActionListener() {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        if (1 >= 0) {
                            resultHandler.handleMatch(getSelectedValue(),
                                e.getActionCommand());
                        }
                    }
                };

            JMenuItem menuItem;

            for (int i = 0; i < popupChoices.length; i++) {
                menuItem = new JMenuItem(popupChoices[i]);
                menuItem.addActionListener(actionListener);
                actionsMenu.add(menuItem);
            }
        } else {
            actionsMenu = null;
        }
    }

    /**
     * Sets the result handler.
     * 
     * @param resultHandler the new result handler
     */
    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    /**
     * Handles the mouse click event, notifies the handler
     *
     * @param e the event
     */
    @Override
	public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() > 1) {
            // first registered action is considered default action
            resultHandler.handleMatch(getSelectedValue(),
                ((JMenuItem) actionsMenu.getSubElements()[0]).getActionCommand());
        }
    }

    @Override
	public void mouseEntered(MouseEvent e) {
    }

    @Override
	public void mouseExited(MouseEvent e) {
    }

    // on a pc this is the popup trigger method
    @Override
	public void mousePressed(MouseEvent e) {
        if ((actionsMenu != null) && e.isPopupTrigger() &&
                (getSelectedValue() != null)) {
            showPopup(e);
        }
    }

    // On the sun this is the popup trigger method
    @Override
	public void mouseReleased(MouseEvent e) {
        if ((actionsMenu != null) && e.isPopupTrigger() &&
                (getSelectedValue() != null)) {
            showPopup(e);
        }
    }

    private void showPopup(MouseEvent e) {
        actionsMenu.show(ResultList.this, e.getX(), e.getY());
        actionsMenu.setVisible(true);
        requestFocus();
    }
}
