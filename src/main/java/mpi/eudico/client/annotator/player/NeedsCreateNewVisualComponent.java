package mpi.eudico.client.annotator.player;

import java.awt.Component;

/**
 * Defines a media player that needs to create a new visual component in the 
 * case of detaching from or attaching of the video to the main application 
 * window. 
 */
public interface NeedsCreateNewVisualComponent {
    /**
     * A Media Player that implements this interface needs special treatment when
     * creating and destroying a detached player. This is used in class
     * {@link mpi.eudico.client.annotator.layout.PlayerLayoutModel}.
     * 
     * @return a new visual component
     */
    public Component createNewVisualComponent();
}
