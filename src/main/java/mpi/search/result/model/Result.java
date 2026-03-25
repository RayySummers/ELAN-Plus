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
package mpi.search.result.model;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;

import mpi.search.content.result.model.ContentMatch;
import mpi.search.gui.PagingListModel;

/**
 * Class for search results.
 * 
 * @author klasal
 */
@SuppressWarnings("serial")
public class Result extends PagingListModel {
    /** Constant for the modified status */
    public static final int MODIFIED = 2;

    /** Constant for the completed status */
    public static final int COMPLETE = 1;

    /** Constant for the initialization status */
    public static final int INIT = 0;

    /** Constant for the interrupted status */
    public static final int INTERRUPTED = -1;

    /** Default status is INTERRUPTED */
    protected int status = INTERRUPTED;
    
    /** List with result change listeners */
    private final List<ResultChangeListener> listeners = new ArrayList<ResultChangeListener>();

    /**
     * Creates a new Result object.
     */
    public Result() {
        data = new ArrayList<ContentMatch>();
    }

    /**
     * Creates a new Result object.
     *
     * @param pageSize the size of a page
     */
    public Result(int pageSize) {
        data = new ArrayList<ContentMatch>();
        setPageSize(pageSize);
    }

    /**
     * Sets the status
     *
     * @param status the new status
     */
    public final void setStatus(int status) {
        if (this.status != status) {
            this.status = status;
            fireResultChanged(new ResultEvent(this, ResultEvent.STATUS_CHANGED));

            if ((status == COMPLETE) || (status == INTERRUPTED)) {
                if (getSize() > 0) {
                    SwingUtilities.invokeLater(new Runnable() {
                            @Override
							public void run() {
                                fireIntervalAdded(this, 0, getSize() - 1);
                            }
                        });
                }
            }
        }
    }

    /**
     * Returns the status.
     * 
     * @return the current status
     */
    public final int getStatus() {
        return status;
    }

    /**
     * Returns the match at the specified index.
     * 
     * @param i number of match (starting with 1!)
     *
     * @return the content match 
     */
    public ContentMatch getMatch(int i) {
        return (ContentMatch) (((0 < i) && (i <= data.size())) ? data.get(i - 1) : null);
    }

    /**
     * Get all matches.
     *
     * @return list of matches
     */
    public final List<ContentMatch> getMatches() {
        return data;
    }

    /**
     * Returns sublist data in current page as list
     *
     * @return gets list of actual visible matches
     */
    public List<ContentMatch> getSubList() {
        return getSubList(0, getSize());
    }

    /**
     * Returns matches which have positions between given indices
     *
     * @param index0 first index (in current page)
     * @param index1 last index (in current page)
     *
     * @return sublist data in current page as list
     */
    public List<ContentMatch> getSubList(int index0, int index1) {
        return data.subList(getFirstShownRealIndex() + index0,
            getFirstShownRealIndex() + index1);
    }

    /**
     * Adds a match.
     *
     * @param match match to be added
     */
    public void addMatch(ContentMatch match) {
        data.add(match);

        //looks good but doesn't work well

        /*if (data.size() <= pageSize) {
               SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                       fireIntervalAdded(this, data.size() -1, getSize() - 1);
                   }
               });
        }*/
        if ((data.size() % pageSize) == 0) {
            fireResultChanged(new ResultEvent(this,
                    ResultEvent.PAGE_COUNT_INCREASED));
        }
    }

    /**
     * Adds all matches from the specified list.
     * 
     * @param matches the matches to add
     */
    public void addAllMatches(List<ContentMatch> matches) {
        data.addAll(matches);
    }

    /**
     * Adds result change listener.
     *
     * @param listener to be notified if match is added
     */
    public void addResultChangeListener(ResultChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            listener.resultChanged(new ResultEvent(this,
                    ResultEvent.STATUS_CHANGED));
        }
    }

    /**
     * Remove all listeners
     */
    public void removeListeners() {
        listeners.clear();
    }

    /**
     * Resets the status, clears current data.
     */
    public void reset() {
        int index1 = data.size() - 1;
        data.clear();

        if (index1 >= 0) {
            fireIntervalRemoved(this, 0, index1);
        }
        ListDataListener[] listDataListeners = getListDataListeners();
        for(int i=0; i<listDataListeners.length; i++){
        		removeListDataListener(listDataListeners[i]);
        }

        status = INIT;
        pageOffset = 0;
        fireResultChanged(new ResultEvent(this, ResultEvent.STATUS_CHANGED));        
    }

    /**
     * Notifies all listeners that a match has been added
     *
     * @param event the result event
     */
    public final void fireResultChanged(ResultEvent event) {
        try {
            for (Iterator<ResultChangeListener> iter = listeners.iterator(); iter.hasNext();) {
                iter.next().resultChanged(event);
            }
        } catch (ConcurrentModificationException e) {
            //ignore
            System.out.println("Concurrent modification exception - ignored");
        }
    }
}
