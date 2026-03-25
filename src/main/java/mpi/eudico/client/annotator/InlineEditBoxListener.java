package mpi.eudico.client.annotator;

/**
 * Interface that defines methods for a listener for inline edit events. 
 * 
 * @author aarsom
 */
public interface InlineEditBoxListener {	
	
	/**
	 * Called when an edit is committed by the InlineEditbox.	 
	 */
	public void editingCommitted();
	
	/**
	 * Called when an edit action is cancelled by the InlineEditbox.	 
	 */
	public void editingCancelled();
	
//	public void editingInterrupted();

}
