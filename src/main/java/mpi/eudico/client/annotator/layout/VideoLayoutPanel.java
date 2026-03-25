package mpi.eudico.client.annotator.layout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;

import mpi.eudico.client.annotator.player.NeedsCreateNewVisualComponent;

/**
 * A host panel for the visual component of a single video player.
 * It interacts with the visual component via a {@link PlayerLayoutModel} and
 * handles resizing, detaching and attaching of the video.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class VideoLayoutPanel extends JPanel implements ComponentListener {
	/**
	 * The model for the single player case. 
	 * A list for multiple players could be added later
	 */
	PlayerLayoutModel playerLoModel;

	/**
	 * Constructor, sets a {@code null} layout.
	 */
	public VideoLayoutPanel() {
		super();
		setLayout(null);
	}
	
	/**
	 * Sets the {@code PlayerLayoutModel} for this panel and thus the visual
	 * component of a video.
	 * 
	 * @param layoutModel the layout model for a single player component
	 */
	public void setPlayerLayoutModel(PlayerLayoutModel layoutModel) {
		this.playerLoModel = layoutModel;
		if (playerLoModel.isVisual()) {// double check
			if (playerLoModel.player instanceof NeedsCreateNewVisualComponent) {
				Component visComponent = ((NeedsCreateNewVisualComponent) playerLoModel.player).createNewVisualComponent();
				playerLoModel.visualComponent = visComponent;
			}
			add(playerLoModel.visualComponent);
			updateSizes();
		}
	}

	/**
	 * Updates the preferred and minimum sizes of the panel.
	 */
	private void updateSizes() {
		if (playerLoModel != null) {
			float ratio = playerLoModel.player.getAspectRatio();
			int width = this.getWidth();
			int height = width;// start with an initial value in case ration = 0
			if (ratio > 0) {
				height = (int) (width / ratio);
			}
			playerLoModel.visualComponent.setBounds(0, 0, width, height);
			setPreferredSize(new Dimension(width / 4, height));
			setMinimumSize(new Dimension(width / 4, height));
			revalidate();
		}
	}
	
	/**
	 * Notification that the video has been detached.
	 * The panel resizes to 0x0.
	 * 
	 * @param layoutModel the model of the player
	 */
	public void detach(PlayerLayoutModel layoutModel) {
		if (layoutModel == playerLoModel) {
			// the visual component is probably already detached from this panel
			remove(playerLoModel.visualComponent);
			setPreferredSize(new Dimension(0, 0));
			setMinimumSize(new Dimension(0, 0));
			revalidate();
		}
	}
	
	/**
	 * Notification that the video has been re-attached, recalculates
	 * the sizes again.
	 *  
	 * @param layoutModel the model of the player
	 */
	public void attach(PlayerLayoutModel layoutModel) {
		if (layoutModel == playerLoModel) {
			
			if (playerLoModel.isVisual()) {// double check
				if (playerLoModel.player instanceof NeedsCreateNewVisualComponent) {
					Component visComponent = ((NeedsCreateNewVisualComponent) playerLoModel.player).createNewVisualComponent();
					playerLoModel.visualComponent = visComponent;
				}
				add(playerLoModel.visualComponent);
				updateSizes();
			}
		}
		
	}
	
	@Override
	public void componentResized(ComponentEvent e) {
		// update preferred and minimum sizes
		updateSizes();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// ignore
	}

	@Override
	public void componentShown(ComponentEvent e) {
		updateSizes();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// ignore
	}

}
