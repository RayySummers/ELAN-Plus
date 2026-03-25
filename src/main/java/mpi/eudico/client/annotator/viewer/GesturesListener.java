package mpi.eudico.client.annotator.viewer;

/**
 * Interface to be implemented by viewers/components that don't automatically
 * receive gesture events from the native system.
 * 
 * @author Han Sloetjes
 */
public interface GesturesListener {

	/**
	 * Listener to swipe events.
	 * @param x if {@code > 0}, swipe to the right, if {@code < 0}, swipe to the left
	 * @param y if {@code > 0}, swipe down, if {@code < 0}, swipe up
	 */
	public void swipe(int x, int y);
	
	/**
	 * Listener to zooming events.
	 * @param zoom the zoom factor (can be {@code < 0} as well)
	 */
	public void magnify(double zoom);
}
