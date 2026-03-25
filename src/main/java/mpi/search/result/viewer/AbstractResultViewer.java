/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.search.result.viewer;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mpi.search.result.model.Result;
import mpi.search.result.model.ResultEvent;


/**
 * Base result viewer.
 *
 * @author klasal
 */
@SuppressWarnings("serial")
public abstract class AbstractResultViewer extends JPanel
    implements ResultViewer {
    /** The next action */
    protected final Action nextAction;

    /** The previous action */
    protected final Action previousAction;
    /** the current result label */
    protected final JLabel currentLabel;
    /** the control panel */
    protected final JPanel controlPanel;

    /** The result to display */
    protected Result result;

    /**
     * Creates a new AbstractResultViewer object.
     */
    public AbstractResultViewer() {
        controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        currentLabel = new JLabel();
        nextAction = new AbstractAction() {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        result.pageUp();
                        updateButtons();
                    }
                };
        nextAction.putValue(Action.NAME, ">");
        previousAction = new AbstractAction() {
                    @Override
					public void actionPerformed(ActionEvent e) {
                        result.pageDown();
                        updateButtons();
                    }
                };
        previousAction.putValue(Action.NAME, "<");
        nextAction.setEnabled(false);
        previousAction.setEnabled(false);
    }

    /**
     * Returns the control panel for the viewer.
     *
     * @return the control panel
     */
    public JPanel getControlPanel() {
        return controlPanel;
    }

    /**
     * Resets the viewer.
     */
    @Override
	public void reset() {
        nextAction.setEnabled(false);
        previousAction.setEnabled(false);
        controlPanel.setVisible(false);
    }

    /**
     * Request to show the specified results.
     *
     * @param result the results
     */
    @Override
	public abstract void showResult(Result result);

    /**
     * Notification of a change in the results.
     *
     * @param e the result event
     */
    @Override
	public void resultChanged(ResultEvent e) {
        result = (Result) e.getSource();

        if (result.getRealSize() == 0) {
            reset();
        }
        else {
            	if ((e.getType() == ResultEvent.PAGE_COUNT_INCREASED) || ((e.getType() == ResultEvent.STATUS_CHANGED) &&
                    ((result.getStatus() == Result.COMPLETE) ||
                    (result.getStatus() == Result.INTERRUPTED)))) {
                updateButtons();
            }
        }
    }

    /**
     * Returns the indexes of start and end of the next page of results.
     *
     * @return an array of size two, begin and end index
     */
    protected int[] getNextInterval() {
        if (result.getPageOffset() < result.getPageCount()) {
            return new int[] {
                ((result.getPageOffset() + 1) * result.getPageSize()) + 1,
                Math.min(((result.getPageOffset() + 2) * result.getPageSize()) +
                    1, result.getRealSize())
            };
        }

        return null;
    }

    /**
     *  Returns the indexes of start and end of the previous page of results.
     *
     * @return an array of size two, begin and end index
     */
    protected int[] getPreviousInterval() {
        if (result.getPageOffset() > 0) {
            return new int[] {
                ((result.getPageOffset() - 1) * result.getPageSize()) + 1,
                result.getPageOffset() * result.getPageSize()
            };
        }

        return null;
    }

    /**
     * Returns a string representation of an interval.
     *
     * @param interval the interval to convert
     *
     * @return the interval as a string
     */
    protected String intervalToString(int[] interval) {
        return (interval != null)
        ? (interval[0] +
        ((interval[0] != interval[1]) ? ("-" + interval[1]) : "")) : null;
    }

    /**
     * Updates the button states.
     */
    protected void updateButtons() {
        previousAction.setEnabled(result.getPageOffset() > 0);
        nextAction.setEnabled(result.getPageOffset() < (result.getPageCount() -
            1));

        currentLabel.setText(intervalToString(
                new int[] {
                    result.getFirstShownRealIndex() + 1,
                    result.getLastShownRealIndex() + 1
                }));

        previousAction.putValue(Action.SHORT_DESCRIPTION,
            intervalToString(getPreviousInterval()));
        nextAction.putValue(Action.SHORT_DESCRIPTION,
            intervalToString(getNextInterval()));
    }
}
