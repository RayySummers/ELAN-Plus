package mpi.search.model;
/**
 * Defines a search progress listener.
 * 
 * Created on May 14, 2005
 * @author klasal
 */
public interface ProgressListener {
    /**
     * Set the current progress.
     * 
     * @param i the progress value
     */
    public void setProgress(int i);
    
    /**
     * Sets the status of the operation.
     * 
     * @param i a status constant
     */
    public void setStatus(int i);
    
    /** 
     * Sets the progress to indeterminate.
     * 
     * @param b if {@code true} the progress is indeterminate
     */
    public void setIndeterminate(boolean b);

}
