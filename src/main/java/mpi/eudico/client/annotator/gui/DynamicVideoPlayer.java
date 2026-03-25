package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.mediadisplayer.MediaDisplayerFactory.MEDIA_ORIENTATION;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.NoPlayerException;
import mpi.eudico.client.annotator.player.PlayerFactory;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.util.MediaDescriptorUtility;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A class to show a video within a bounding box.
 *
 * @author michahulsbosch
 */
public class DynamicVideoPlayer implements Runnable {

    private final JComponent component;
    private Rectangle bounds;
    private MediaDescriptor md;
    private final int delay;
    private MEDIA_ORIENTATION horizontalOrientation;
    private MEDIA_ORIENTATION verticalOrientation;

    private ElanMediaPlayer player;
    private Boolean cleanedUp = false;
    private final ReentrantLock playLock = new ReentrantLock();

    /**
     * Creates a object containing all necessary info to display a (short) CV-entry video in a specified component at a
     * specified place.
     *
     * <p>The orientation arguments can be used to determine which corner of the video is the anchor of
     * the bounds.
     *
     * @param component the host component for the video panel
     * @param bounds the bounds for the video panel
     * @param delay a delay in milliseconds before the player pops up
     * @param videoFile the file to play
     * @param horizontalOrientation the horizontal orientation of the video bounds anchor
     * @param verticalOrientation the vertical orientation of the video bounds anchor
     */
    public DynamicVideoPlayer(JComponent component, Rectangle bounds, int delay, File videoFile,
                              MEDIA_ORIENTATION horizontalOrientation, MEDIA_ORIENTATION verticalOrientation) {
        this.component = component;
        setBounds(bounds);
        setVideoFile(videoFile);
        this.delay = delay;
        setOrientation(horizontalOrientation, verticalOrientation);
    }

    /**
     * Creates a dynamic video player with bounds anchor at {@code MEDIA_ORIENTATION#WEST} and
     * {@code MEDIA_ORIENTATION#NORTH}.
     *
     * @param component the host component for the video panel
     * @param bounds the bounds for the video panel
     * @param delay a delay in milliseconds before the player pops up
     * @param videoFile the file to play
     */
    public DynamicVideoPlayer(JComponent component, Rectangle bounds, int delay, File videoFile) {
        this(component, bounds, delay, videoFile, MEDIA_ORIENTATION.WEST, MEDIA_ORIENTATION.NORTH);
    }

    /**
     * Creates a object containing all necessary info to display a (short) CV-entry video in a specified component at a
     * specified place.
     *
     * <p>The orientation arguments can be used to determine which corner of the video is the anchor of
     * the bounds.
     *
     * @param component the host component for the video panel
     * @param bounds the bounds for the video panel
     * @param delay a delay in milliseconds before the player pops up
     * @param mediaURL the URL of the file to play
     * @param horizontalOrientation the horizontal orientation of the video bounds anchor
     * @param verticalOrientation the vertical orientation of the video bounds anchor
     */
    public DynamicVideoPlayer(JComponent component, Rectangle bounds, int delay, String mediaURL,
                              MEDIA_ORIENTATION horizontalOrientation, MEDIA_ORIENTATION verticalOrientation) {
        this.component = component;
        setBounds(bounds);
        setVideoUrl(mediaURL);
        this.delay = delay;
        setOrientation(horizontalOrientation, verticalOrientation);
    }

    /**
     * Creates a dynamic video player with bounds anchor at {@code MEDIA_ORIENTATION#WEST} and
     * {@code MEDIA_ORIENTATION#NORTH}.
     *
     * @param component the host component for the video panel
     * @param bounds the bounds for the video panel
     * @param delay a delay in milliseconds before the player pops up
     * @param mediaURL the URL of the file to play
     */
    public DynamicVideoPlayer(JComponent component, Rectangle bounds, int delay, String mediaURL) {
        this(component, bounds, delay, mediaURL, MEDIA_ORIENTATION.WEST, MEDIA_ORIENTATION.NORTH);
    }

    /**
     * Sets the video to display.
     *
     * @param videoFile the video to display
     */
    public void setVideoFile(File videoFile) {
        md = MediaDescriptorUtility.createMediaDescriptor(videoFile.getAbsolutePath());
    }

    /**
     * Sets the (remote) URL of the video to display.
     *
     * @param uriString the {@code URL} of the video as a string
     */
    public void setVideoUrl(String uriString) {
        md = MediaDescriptorUtility.createMediaDescriptor(uriString);
    }

    /**
     * Sets the bounds for the component.
     *
     * @param bounds the bounds for the component
     */
    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    /**
     * Sets the orientation.
     *
     * @param horizontalOrientation the horizontal orientation
     * @param verticalOrientation the vertical orientation
     */
    public void setOrientation(MEDIA_ORIENTATION horizontalOrientation, MEDIA_ORIENTATION verticalOrientation) {
        this.horizontalOrientation = horizontalOrientation;
        this.verticalOrientation = verticalOrientation;
    }

    /**
     * Creates the player, applies the bounds and starts the player.
     */
    @Override
    public void run() {
        setCleanedUp(false);
        try {
            Thread.sleep(delay);
            playLock.lock();
            try {
                player = PlayerFactory.createElanMediaPlayer(md);

                // Fit the player within the given bounds, but
                // do not display black bars.
                float playerAspectRatio = player.getAspectRatio();
                float boundsAspectRatio = bounds.width / bounds.height;
                Rectangle displayBounds = new Rectangle(bounds);
                if (playerAspectRatio > 0) {
                    if (playerAspectRatio < boundsAspectRatio) {
                        displayBounds.setSize((int) Math.ceil(bounds.height * playerAspectRatio), bounds.height);
                    } else if (playerAspectRatio > boundsAspectRatio) {
                        displayBounds.setSize(bounds.width, (int) Math.ceil(bounds.width / playerAspectRatio));
                    }
                }

                if (horizontalOrientation == MEDIA_ORIENTATION.EAST) {
                    displayBounds.x = displayBounds.x - displayBounds.width;
                }

                if (verticalOrientation == MEDIA_ORIENTATION.SOUTH) {
                    displayBounds.y = displayBounds.y - displayBounds.height;
                }

                Component videoComponent = player.getVisualComponent();
                component.add(videoComponent);
                videoComponent.setBounds(displayBounds);
                videoComponent.setVisible(true);
                component.validate();
                component.repaint();
                player.setMediaTime(0);
                player.start();
            } finally {
                playLock.unlock();
            }
            if (player != null) {
                Thread.sleep(player.getMediaDuration() + 10);
            }
        } catch (InterruptedException ee) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Displaying video " + md.mediaURL + " interrupted (" + ee.getMessage() + ")");
            }
        } catch (NoPlayerException e) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.warning("Not able to create a player for " + md.mediaURL + " (" + e.getMessage() + ")");
            }
        } finally {
            cleanUp();
        }
    }

    /**
     * Removes the player from the hosting component and cleans it for garbage collection.
     */
    public void cleanUp() {
        if (component != null && player != null) {
            playLock.lock();
            try {
                // player.stop() doesn't always work, the sound keeps playing
                player.setVolume(0.0f);
                player.stop();
                component.remove(player.getVisualComponent());
                //player.cleanUpOnClose();
                player = null;
            } finally {
                playLock.unlock();
            }
        }
        setCleanedUp(true);
    }

    /**
     * Returns whether the player has been cleaned up.
     *
     * @return the cleaned up flag
     */
    public Boolean isCleanedUp() {
        return cleanedUp;
    }

    /**
     * Sets the cleaned up flag.
     *
     * @param cleanedUp the new value for the cleaned up flag
     */
    public void setCleanedUp(Boolean cleanedUp) {
        this.cleanedUp = cleanedUp;
    }
}
