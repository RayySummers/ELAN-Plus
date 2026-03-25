package mpi.search.query.viewer;

import javax.swing.JPanel;

import mpi.search.model.SearchController;
import mpi.search.model.SearchListener;
import mpi.search.query.model.Query;
import mpi.search.result.viewer.MatchCounter;
import mpi.search.result.viewer.ResultViewer;
import mpi.search.viewer.ProgressViewer;


/**
 * A base class for a search panel.
 * 
 * Created on Apr 14, 2005
 * @author klasal
 */
public abstract class AbstractSearchPanel extends JPanel
    implements SearchListener {
    /** The progress viewer */
    protected final ProgressViewer progressViewer;

    /** Counter, shows current number of result and amount of matches */
    protected MatchCounter matchCounter;

    /** A search result viewer */
    protected ResultViewer resultViewer;

    /** The search engine */
    protected SearchController searchEngine;

    /**
     * Creates a new AbstractSearchPanel object.
     */
    public AbstractSearchPanel() {
        progressViewer = new ProgressViewer();
        matchCounter = new MatchCounter();
    }

    @Override
	public void executionStarted() {
        matchCounter.setVisible(true);
        progressViewer.setVisible(true);
    }

    /**
     * Stops the search engine.
     */
    public void stopSearch() {
        if (searchEngine != null) {
            searchEngine.stopExecution();
        }
    }

    /**
     * Get query abstract method.
     * 
     * @return returns the query
     */
    protected abstract Query getQuery();

    /**
     * Gets the query and calls the search engine's {@code execute} method.
     */
    protected void startSearch() {
        if (searchEngine.getResult() != null) {
            searchEngine.getResult().removeListeners();
        }

        Query query = getQuery();

        if (query != null) {
            query.getResult().reset();

            query.getResult().addResultChangeListener(matchCounter);
            query.getResult().addResultChangeListener(resultViewer);

            searchEngine.execute(query);
        }
    }
}
