package mpi.eudico.client.annotator.mediadisplayer;

import java.awt.Rectangle;

/**
 * Defines the host of a {@code MediaDisplayer} object. 
 *
 */
public interface MediaDisplayerHost {
	
	/**
	 * A request to host a media displayer.
	 * 
	 * @param arguments an array containing required information for hosting a displayer
	 * @param sourceBounds the display bounds 
	 */
	public void hostMediaDisplayer(Object[] arguments, Rectangle sourceBounds);
	
	/**
	 * A request to remove and delete all resources of or for the current media
	 * displayer.
	 */
	public void discardMediaDisplayer();
}
