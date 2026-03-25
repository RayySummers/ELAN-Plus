package mpi.search.content.query.model;

/**
 * An exception class that indicates an error in the formulation of a search
 * query.
 * 
 * @author Alexander Klassmann
 */
@SuppressWarnings("serial")
public class QueryFormulationException extends Exception {
    /**
     * Constructor.
     * 
     * @param message the message
     */
    public QueryFormulationException(String message){
        super(message);
    }
    
}
