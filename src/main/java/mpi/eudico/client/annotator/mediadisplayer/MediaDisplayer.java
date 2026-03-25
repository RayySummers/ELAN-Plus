package mpi.eudico.client.annotator.mediadisplayer;

import java.awt.Rectangle;

import javax.swing.JComponent;

import mpi.eudico.client.annotator.mediadisplayer.MediaDisplayerFactory.MEDIA_ORIENTATION;

/**
 * Interface for a media displaying component.
 * The type of media is undefined.
 * 
 * @author michahulsbosch
 */
public interface MediaDisplayer {
	/**
	 * Sets the media bundle for the displayer.
	 * 
	 * @param mediabundle the media bundle containing the URL and possibly
	 * additional information concerning the media
	 */
	public void setMediaBundle(MediaBundle mediabundle);
	
	/**
	 * Call to actually display the media.
	 * 
	 * @param component the component to use for displaying the media
	 * @param bounds the bounds for the display
	 * @param delay the time to wait before starting display
	 * @param horizontalOrientation the horizontal orientation
	 * @param verticalOrientation the vertical orientation
	 */
	public void displayMedia(JComponent component, Rectangle bounds, int delay, MEDIA_ORIENTATION horizontalOrientation, MEDIA_ORIENTATION verticalOrientation);
	
	/**
	 * Stops the display and destroys resources.
	 */
	public void discard();
}
