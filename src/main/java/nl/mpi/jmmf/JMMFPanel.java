package nl.mpi.jmmf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

/**
 * A panel that serves as the host (video window, the {@code HWND} handle) for
 * a native MediaFoundation video display control. The video {@code Topology}
 * will only be initialized after this panel has been added to a frame or
 * window and is displayable.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class JMMFPanel extends Panel implements ComponentListener, HierarchyListener {
	/** native logger */
	private final static System.Logger LOG = System.getLogger("NativeLogger");
	/** the connected player */
	private JMMFPlayer player;
	/** flag to indicate if the panel has been added for the first time */
	private boolean firstAddOccurred = false;
	/** the time of first addition */
	private long firstAddTime = 0L;
	/** initialization waiting time */
	private static long initWaitTime = 500L;
	
	static {
		String waitProp = System.getProperty("JMMFPlayer.MinimalInitTimeMS");
		if (waitProp != null) {
			try {
				initWaitTime = Long.parseLong(waitProp);
			} catch (NumberFormatException nfe) {
				if (LOG.isLoggable(System.Logger.Level.INFO)) {
					LOG.log(System.Logger.Level.INFO, 
						"A 'JMMFPlayer.MinimalInitTimeMS' property exists, but its value cannot be parsed");
				}
			}
		}
	}
	
	/**
	 * Constructor.
	 */
	public JMMFPanel() {
		super(null);
		this.setBackground(new Color(0, 0, 128));
		addComponentListener(this);
		addHierarchyListener(this);
		super.setIgnoreRepaint(true);
	}

	/**
	 * Constructor.
	 * 
	 * @param player the player instance using this panel for display
	 */
	public JMMFPanel(JMMFPlayer player) {
		super(null);
		this.player = player;
		this.setBackground(new Color(0, 0, 128));
		addComponentListener(this);
		addHierarchyListener(this);
		super.setIgnoreRepaint(true);
	}
	
	/**
	 * Sets the player using this panel for video display.
	 * 
	 * @param player the player
	 */
	public void setPlayer(JMMFPlayer player) {
		this.player = player;
	}
	
	/**
	 * Called when this panel is added to a component hierarchy of a frame or
	 * window.
	 */
	@Override
	public void addNotify() {
		synchronized (getTreeLock()) {					
			super.addNotify();
			
			if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
				LOG.log(System.Logger.Level.DEBUG, "Video panel added to window");
			}
			if (player != null && this.isDisplayable()) {
				if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
					LOG.log(System.Logger.Level.DEBUG, "Setting the video panel for the player");
				}
				player.setVisualComponent(this);
				player.setVisible(true);
				player.repaintVideo();
				// for first time initialization allow to build in a wait time for (asynchronous) 
				// creation of the native player 
				if (!firstAddOccurred) {
					firstAddOccurred = true;
					firstAddTime = System.currentTimeMillis();
				}

			} else {
				if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
					LOG.log(System.Logger.Level.DEBUG, "Video panel is not displayable or player is null");
				}
			}
		}
	}

	/**
	 * If, in the process of building the layout of a window, the video component
	 * is removed from a parent container almost immediately after it has been
	 * added to that container, this seems to interfere with the creation of the
	 * native media player. Therefore a minimal wait time can be specified to
	 * allow full initialization before the visual component is removed after
	 * the first time it has been added to a container.
	 */
	@Override
	public void removeNotify() {
		synchronized(getTreeLock()) {
			long afterAdd = System.currentTimeMillis() - firstAddTime;
			if (afterAdd < initWaitTime) {
				try {
					Thread.sleep(initWaitTime);
				} catch (Throwable thr) {}
			}
			
			if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
				LOG.log(System.Logger.Level.DEBUG, "Video panel removed from window");
			}
			if (player != null) {
				//player.setVisualComponent(null);
				//player.setVisible(false);
			}
			super.removeNotify();
		}
	}

	@Override
	public void componentHidden(ComponentEvent ce) {
		if (player != null) {
			player.setVisible(false);
		}
	}

	@Override
	public void componentMoved(ComponentEvent ce) {
		componentResized(ce);
//		player.repaintVideo();
	}

	@Override
	public void componentResized(ComponentEvent ce) {
		if (player != null && this.isDisplayable()) {
			player.setVisualComponentSize(getWidth(), getHeight());

			player.repaintVideo();
		}
	}

	@Override
	public void componentShown(ComponentEvent ce) {
		componentResized(ce);
//		player.repaintVideo();
	}

	@Override
	public void hierarchyChanged(HierarchyEvent e) {
		if (e.getChangeFlags() == HierarchyEvent.DISPLAYABILITY_CHANGED && isDisplayable()) {
			if (player != null) {
				player.setVisualComponent(this);
				player.setVisible(true);
				player.repaintVideo();
			}
		}
	}

	@Override
	public void repaint() {
//		System.out.println("repaint...");
//		//super.repaint();
//		if (player != null) {
//			player.repaintVideo();
//		}
	}

	@Override
	public void paint(Graphics g) {
//		System.out.println("paint...");
//		//super.paint(g);
//		if (player != null) {
//			player.repaintVideo();
//		}
	}

	@Override
	public void update(Graphics g) {
//		System.out.println("update...");
//		//super.update(g);
//		if (player != null) {
//			player.repaintVideo();
//		}
	}

	@Override
	public void paintComponents(Graphics g) {
//		System.out.println("paintComponents...");
//		if (player != null) {
//			player.repaintVideo();
//		}
	}

	@Override
	public void validate() {// validate is called regularly
//		System.out.println("validate...");
//		if (player != null) {
//			player.repaintVideo();
//		}
	}

	@Override
	public void paintAll(Graphics g) {
//		System.out.println("paintAll...");
//		if (player != null) {
//			player.repaintVideo();
//		}
	}

	@Override
	public void repaint(int x, int y, int width, int height) {
//		System.out.println("repaint(xywh)...");
//		if (player != null) {
//			player.repaintVideo();
//		}
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
//		System.out.println("repaint(txywh)...");
//		if (player != null) {
//			player.repaintVideo();
//		}
	}

	@Override
	public void repaint(long tm) {
//		System.out.println("repaint(t)...");
//		if (player != null) {
//			player.repaintVideo();
//		}
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int w,
			int h) {
		return false;
	}

	@Override
	public void setIgnoreRepaint(boolean ignoreRepaint) {

		super.setIgnoreRepaint(true);
	}

}
