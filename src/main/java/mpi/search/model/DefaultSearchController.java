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
package mpi.search.model;

import mpi.search.query.model.Query;
import mpi.search.result.model.Result;
import mpi.search.gui.SwingWorker;;

/**
 * Default implementation of a search controller.
 * 
 * @author klasal
 */
public class DefaultSearchController implements SearchController {
    /** A progress listener */
    protected ProgressListener progressListener;

    /** The search result */
    protected Result result;

    /** A search listener */
    protected SearchListener searchListener;
    /** the worker thread */
    protected SwingWorker worker;
    private SearchEngine searchEngine;
    private boolean interrupted = false;
    private boolean isExecuting = false;
    private long endTime = -1;
    private long startTime = -1;

    /**
     * Creates a new DefaultSearchController object.
     *
     * @param searchTool the search listener
     * @param searchEngine the search engine
     */
    public DefaultSearchController(SearchListener searchTool, SearchEngine searchEngine) {
        this.searchListener = searchTool;
        this.searchEngine = searchEngine;
    }

    /**
     * 
     * 
     * @return whether or not a search is executing now
     */
    @Override
	public boolean isExecuting() {
        return isExecuting;
    }

    /**
     *
     * @param progressListener the single progress listener
     */
    @Override
	public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    @Override
	public Result getResult() {
        return result;
    }

    /**
     * Returns elapsed time for current or last performed search
     *
     * @return the duration in milliseconds
     */
    @Override
	public long getSearchDuration() {
        return (((worker != null) && worker.isRunning()) ? System.currentTimeMillis() : endTime) -
        startTime;
    }

    /**
     * Calls thread that will perform the actual search. Note that the thread should finish by
     * calling notifySearchIsReady(). This is not done here because the thread may contain yet
     * another thread.
     *
     * @param query Query
     */
    @Override
	public void execute(final Query query) {
        if (isExecuting()) {
            return;
        }

        isExecuting = true;
        searchListener.executionStarted();
        this.result = query.getResult();
        result.setStatus(Result.INIT);
        interrupted = false;
        worker = new SwingWorker() {
                    @Override
					public Object construct() {
                        try {
                            searchEngine.performSearch(query);
                        }
                        catch (Exception e) {
                            //Thread interrupt during parsing might cause
                            // ConcurrentModificationException,
                            //yet the (wrapper) Exception is of instance SAXException,
                            //which should not be catched always
                            if (
                                e.toString().startsWith(
                                        "java.util.ConcurrentModificationException")) {
                                System.out.println(e.toString() + " - ignored");
                            }
                            else {
                                searchListener.handleException(e);
                            }
                        }
                        finally {
                            executionStopped();
                        }

                        return DefaultSearchController.this.result; // not used
                    }

                    @Override
					public void finished() {
                    }
                };
        startTime = System.currentTimeMillis();

        if (progressListener != null) {
            progressListener.setProgress(0);
        }

        worker.start();
    }

    /**
     * Stops search execution.
     */
    @Override
	public void stopExecution() {
        if (result != null) {
            interrupted = true;

            if ((worker != null) && worker.isRunning()) {
                worker.interrupt();
            }

            executionStopped();
        }
    }

    /**
     * Setting status of result; notifying searchListener
     */
    protected void executionStopped() {
        isExecuting = false;
        result.setStatus(interrupted ? Result.INTERRUPTED : Result.COMPLETE);

        if (progressListener != null) {
            progressListener.setIndeterminate(false);
            progressListener.setStatus(result.getStatus());
        }

        endTime = System.currentTimeMillis();
        searchListener.executionStopped();
    }
}
