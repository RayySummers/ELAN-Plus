package nl.mpi.jds;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

/**
 * A Java video host canvas for a native DirectShow media player. 
 */
@SuppressWarnings("serial")
public class JDSCanvas extends Canvas implements ComponentListener, HierarchyListener {
	/** the connected player */
	private JDSPlayer player;
	
	/**
	 * Creates a new video host canvas.
	 */
	public JDSCanvas() {
		super(null);
		addComponentListener(this);
		addHierarchyListener(this);
	}

	/**
	 * Creates a new video host canvas for the specified player.
	 * 
	 * @param player the media player
	 */
	public JDSCanvas(JDSPlayer player) {
		super(null);
		this.player = player;
		this.setBackground(new Color(0, 64, 64));
		addComponentListener(this);
		addHierarchyListener(this);
	}
	
	/**
	 * When the canvas is added to a layout, the native player is informed so
	 * that it can get the window handle of the canvas and configure the 
	 * rendering.  
	 */
	@Override
	public void addNotify() {
		synchronized (getTreeLock()) {
			super.addNotify();
			//System.out.println("Panel add notify...");
			if (player != null && this.isDisplayable()) {
				//System.out.println("Panel add notify, displayable...");
				player.setVisualComponent(this);
				player.setVisible(true);
			}
		}
	}

	/**
	 * Informs the player so that it can disconnect from the canvas.
	 */
	@Override
	public void removeNotify() {
		synchronized (getTreeLock()) {
			if (player != null) {
				player.setVisualComponent(null);
				player.setVisible(false);
			}
			super.removeNotify();
		}
	}

	/**
	 * Informs the player to make the video invisible.
	 */
	@Override
	public void componentHidden(ComponentEvent ce) {
		if (player != null) {
			player.setVisible(false);
		}
	}

	@Override
	public void componentMoved(ComponentEvent ce) {
		componentResized(ce);
	}

	/**
	 * After a change of location and/or size of the canvas, the player is 
	 * informed of the new bounds and can update the renderer.
	 */
	@Override
	public void componentResized(ComponentEvent ce) {
		if (player != null && this.isDisplayable()) {
			player.setVisualComponentPos(0, 0, getWidth(), getHeight());
//			int w = getWidth();
//			int h = getHeight();
//			player.setVisualComponentPos(-w/4, -h/4, 2*w, 2*h);
		}
	}

	@Override
	public void componentShown(ComponentEvent ce) {
		componentResized(ce);
	}

	@Override
	public void hierarchyChanged(HierarchyEvent e) {
		if (e.getChangeFlags() == HierarchyEvent.DISPLAYABILITY_CHANGED && isDisplayable()) {
			//System.out.println("Hierarchy...");
			player.setVisualComponent(this);
			player.setVisible(true);
		}
	}

}
