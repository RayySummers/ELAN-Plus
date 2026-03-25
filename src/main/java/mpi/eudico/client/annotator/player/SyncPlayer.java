package mpi.eudico.client.annotator.player;

/**
 * Identifies a media player as a synchronisation-only player. 
 * This means the layout manager should only add this player to the layout 
 * and to the viewer manager in synchronisation mode.
 * Used for non-audio, non-video media/files that need to be synchronized.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public interface SyncPlayer {
	/**
	 * Returns whether this player currently is connected to the controls and
	 * is the player that is being synced.
	 * 
	 * @return {@code true} if this player is connected
	 */
	public boolean isSyncConnected();

	/**
	 * Informs the player it is being connected to or disconnected from
	 * the controls.
	 * 
	 * @param syncConnected the new connected status
	 */
	public void setSyncConnected(boolean syncConnected);
}
