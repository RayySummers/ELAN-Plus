package nl.mpi.jmmf;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

/**
 * Initially, with the first tests, the native video was displayed on the 
 * canvas, but not in the right place and setting the window position didn't
 * seem to work.
 * Now (April '23, on Windows 11 with java 20) the {@code Canvas} based 
 * variant seems to work as well and may be tested in situations where
 * the video, for unknown reasons, does not become visible (only the
 * rectangle).
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class JMMFCanvas extends Canvas implements ComponentListener, HierarchyListener {
	/** native logger */
	private final static System.Logger LOG = System.getLogger("NativeLogger");
	
	/** the connected player */
	JMMFPlayer player;

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
	 * Creates a new canvas instance.
	 */
	public JMMFCanvas() {
		super();
		this.setBackground(new Color(0, 64, 64));// a dark green background
		super.setIgnoreRepaint(true);
		addComponentListener(this);
		addHierarchyListener(this);
	}

	/**
	 * Creates a new canvas instance for the specified player.
	 * 
	 * @param player the connected media player
	 */
	public JMMFCanvas(JMMFPlayer player) {
		super();
		this.player = player;
		this.setBackground(new Color(0, 64, 64));// a dark green background
		super.setIgnoreRepaint(true);
		addComponentListener(this);
		addHierarchyListener(this);
	}
	
	/**
	 * Creates a new canvas instance for the specified configuration.
	 * 
	 * @param gc the graphics configuration
	 */
	public JMMFCanvas(GraphicsConfiguration gc) {
		super(gc);
	}

	/**
	 * Sets the player connected to this canvas.
	 * 
	 * @param player the media player
	 */
	public void setPlayer(JMMFPlayer player) {
		this.player = player;
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		//System.out.println("Panel add notify...");
		if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
			LOG.log(System.Logger.Level.DEBUG, "Video canvas added to window");
		}
		if (player != null && this.isDisplayable()) {
			//System.out.println("Panel add notify, displayable...");
			if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
				LOG.log(System.Logger.Level.DEBUG, "Setting the video canvas for the player");
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
				LOG.log(System.Logger.Level.DEBUG, "Video canvas is not displayable or player is null");
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
		long afterAdd = System.currentTimeMillis() - firstAddTime;
		if (afterAdd < initWaitTime) {
			try {
				Thread.sleep(initWaitTime);
			} catch (Throwable thr) {}
		}
		
		if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
			LOG.log(System.Logger.Level.DEBUG, "Video canvas removed from window");
		}
		
		if (player != null) {
			player.setVisualComponent(null);
			player.setVisible(false);
		}
		super.removeNotify();
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
		player.repaintVideo();
	}

	@Override
	public void componentResized(ComponentEvent ce) {
		if (player != null && this.isDisplayable()) {
			player.setVisualComponentSize(getWidth(), getHeight());
//			int w = getWidth();
//			int h = getHeight();
//			player.setVisualComponentPos(-w/4, -h/4, 2*w, 2*h);
			player.repaintVideo();
		}
	}

	@Override
	public void componentShown(ComponentEvent ce) {
		componentResized(ce);
		player.repaintVideo();
	}

	@Override
	public void hierarchyChanged(HierarchyEvent e) {
		if (e.getChangeFlags() == HierarchyEvent.DISPLAYABILITY_CHANGED && isDisplayable()) {
			//System.out.println("Hierarchy...");
			if (player != null) {
				player.setVisualComponent(this);
				player.setVisible(true);
				player.repaintVideo();
			}
		}
	}

	@Override
	public void repaint() {
		//super.repaint();
		if (player != null) {
			player.repaintVideo();
		}
	}

	@Override
	public void paint(Graphics g) {
		//super.paint(g);
		if (player != null) {
			player.repaintVideo();
		}
	}

	@Override
	public void update(Graphics g) {
		//super.update(g);
		if (player != null) {
			player.repaintVideo();
		}
	}

}
