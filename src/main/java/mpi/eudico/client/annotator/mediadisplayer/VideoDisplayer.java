package mpi.eudico.client.annotator.mediadisplayer;

import java.awt.Rectangle;
import java.io.File;

import javax.swing.JComponent;

import mpi.eudico.client.annotator.gui.DynamicVideoPlayer;
import mpi.eudico.client.annotator.mediadisplayer.MediaDisplayerFactory.MEDIA_ORIENTATION;
import mpi.eudico.client.annotator.util.UrlOpener;

/**
 * Displays a video; can be discarded at any time.
 * Typically the video display will be in a popup window or similar.
 * 
 * @author michahulsbosch
 *
 */
public class VideoDisplayer implements MediaDisplayer {
	private Thread videoThread;
	private DynamicVideoPlayer videoPlayer = null;
	private MediaBundle mediaBundle;
	
	/**
	 * Creates a new displayer instance.
	 */
	public VideoDisplayer() {
		super();
	}

	@Override
	public void setMediaBundle(MediaBundle mediaBundle) {
		this.mediaBundle = mediaBundle;
	}
	
	@Override
	public void displayMedia(JComponent component, Rectangle bounds, int delay, 
			MEDIA_ORIENTATION horizontalOrientation, MEDIA_ORIENTATION verticalOrientation) {
		discard();
		
		if(mediaBundle == null) {
			return;
		}
        		
        File videoFile = new File(mediaBundle.getMediaUrl());
		if(videoFile != null && videoFile.exists() && !videoFile.isDirectory()) {
			// Create or update the video player
//			if(videoPlayer == null) {
				videoPlayer = new DynamicVideoPlayer(component, bounds, 500, videoFile, 
						horizontalOrientation, verticalOrientation);
//			} else {
//				videoPlayer.setVideoFile(videoFile);
//				videoPlayer.setBounds(bounds);
//				videoPlayer.setOrientation(horizontalOrientation, verticalOrientation);
//			}
			
			// Create and start the player thread
			videoThread = new Thread(videoPlayer);
			videoThread.start();
		} else if (UrlOpener.isRemoteVideoLink(mediaBundle.getMediaUrl())) {
			// Create or update the video player for a remote file
//			if(videoPlayer == null) {
				videoPlayer = new DynamicVideoPlayer(component, bounds, 500, mediaBundle.getMediaUrl(), 
						horizontalOrientation, verticalOrientation);
//			} else {
//				videoPlayer.setVideoUrl(mediaBundle.getMediaUrl());
//				videoPlayer.setBounds(bounds);
//				videoPlayer.setOrientation(horizontalOrientation, verticalOrientation);
//			}
			
			// Create and start the player thread
			videoThread = new Thread(videoPlayer);
			videoThread.start();
		}
	}

	@Override
	public void discard() {
		// Clean up video player stuff
        if(videoPlayer != null) {
        	videoPlayer.cleanUp();
        }
        if(videoThread != null) {
        	videoThread.interrupt();
        }

	}

}
