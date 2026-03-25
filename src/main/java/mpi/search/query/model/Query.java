/*
 * Created on 19.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mpi.search.query.model;

import java.util.Date;

import mpi.search.result.model.Result;


/**
 * Base class for a search query.
 * 
 * @author klasal
 */
public abstract class Query {
    private final Date creationDate;

    /**
     * Creates a new Query object.
     */
    public Query() {
        creationDate = new Date();
    }

    /**
     * Returns the results available after execution finished.
     *
     * @return the search results
     */
    public abstract Result getResult();

    /**
     * Returns the creation time.
     * 
     * @return the creation time of the query
     */
    public Date getCreationDate() {
        return creationDate;
    }
}
