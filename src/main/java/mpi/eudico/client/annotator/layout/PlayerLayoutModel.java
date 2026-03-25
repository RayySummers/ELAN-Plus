package mpi.eudico.client.annotator.layout;

import java.awt.Component;

import mpi.eudico.client.annotator.DetachedFrame;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.EmptyMediaPlayer;
import mpi.eudico.client.annotator.player.NeedsCreateNewVisualComponent;
import mpi.eudico.client.annotator.player.SyncPlayer;

/**
 * A class for convenient storage of layout related attributes,
 * to avoid we have to administer a number of lists of attributes for players.
 * 
 * @author Han Sloetjes
 */
public class PlayerLayoutModel {
	/** the source player */
	public ElanMediaPlayer player;
	private ElanLayoutManager layoutManager;
	private boolean attached;
	/** a detached frame */
	public DetachedFrame detachedFrame;
	/** the visual component of the player */
	public Component visualComponent;
	private boolean displayedFirst;
	private boolean syncOnly = false;
	
	/**
	 * Constructor.
	 * 
	 * @param player the media player
	 * @param layoutManager the main layout manager
	 */
	public PlayerLayoutModel(ElanMediaPlayer player, ElanLayoutManager layoutManager) {
		this.player = player;
		this.layoutManager = layoutManager;
		visualComponent = player.getVisualComponent();
		attached = true;
		detachedFrame = null;
		displayedFirst = false;
	}
	
	/**
	 * Sets the value of syncOnly, indicating that this model is only used for
	 * the synchronization mode.
	 * 
	 * @param bool if {@code true} this player is only for the synchronization
	 * mode 
	 */
	public void setSyncOnly(boolean bool) {
		syncOnly = bool;
	}
	
	/**
	 * Returns whether this player model is only for the synchronization mode.
	 *  
	 * @return syncOnly {@code true} if this player model is only for 
	 * synchronization purposes, {@code false} otherwise
	 */
	public boolean isSyncOnly() {
		return syncOnly;
	}
	
	/**
	 * Changes the flag that determines whether or not this player should 
	 * be displayed as the first (and largest) of all attached videos 
	 * (if this player has a visual component).
	 * 
	 * @param first when {@code true} this video will be given the first position 
	 *  and will get the largest area for its visual component
	 */
	public void setDisplayedFirst(boolean first) {
		displayedFirst = first;
	}
	
	/**
	 * Returns whether or not this player is marked as the one to be displayed 
	 * as the first (and the largest) of the attached players.
	 * 
	 * @return {@code true} if this player is to be given the first position 
	 * (and the largest area for its visual component) of the attached players
	 */
	public boolean isDisplayedFirst() {
		return displayedFirst;
	}
	
	/**
	 * Returns the attached/detached state of this player.
	 * 
	 * @return the attached/detached state
	 */
	public boolean isAttached() {
		return attached;
	}
	
	/**
	 * Returns whether this player has a (meaningful) visual component.
	 * The EmptyMediaPlayer only has a dummy component that is not to be 
	 * added to any layout.
	 * 
	 * @return whether this player has a (meaningful) visual component
	 */
	public boolean isVisual() {
		if (player instanceof EmptyMediaPlayer && !(player instanceof SyncPlayer)) {
			return false;
		} else {
			return visualComponent != null;
		}
	}
	
	/**
	 * Detaches the player. The visual component is added to the content pane 
	 * of its own Frame.
	 */
	public void detach() {
		if (!attached || !isVisual()) {
			return;
		}
		if (player instanceof NeedsCreateNewVisualComponent) {
			visualComponent = ((NeedsCreateNewVisualComponent)player).createNewVisualComponent();
		}
		detachedFrame = new DetachedFrame(layoutManager,
			visualComponent, player.getMediaDescriptor().mediaURL);
		detachedFrame.setAspectRatio(player.getAspectRatio());

		detachedFrame.setSize(400, 400);
		detachedFrame.setVisible(true);
		
		attached = false;
	}
	
	/**
	 * Removes the visual component from the content pane of a detached frame
	 * and disposes the frame.
	 * Then the visual component can then be added to the content pane of the main 
	 * application frame. This is not done here.
	 */
	public void attach() {
		if (attached || !isVisual() || detachedFrame == null) {
			return;
		}
		detachedFrame.getContentPane().remove(visualComponent);
		detachedFrame.setVisible(false);
		detachedFrame.dispose();
		detachedFrame = null;
		attached = true;
		if (player instanceof NeedsCreateNewVisualComponent) {
			visualComponent = ((NeedsCreateNewVisualComponent)player).createNewVisualComponent();
		}
	}

}
