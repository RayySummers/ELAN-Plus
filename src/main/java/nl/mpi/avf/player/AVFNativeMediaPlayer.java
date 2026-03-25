package nl.mpi.avf.player;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

/**
 * A media player that encapsulates an AVFNativePlayer and, in case of video,
 * a canvas as host visual component for a native video panel.
 * 
 * @author Han Sloetjes
 *
 */
public class AVFNativeMediaPlayer extends AVFBaseMediaPlayer { 
	private AVFNCanvas visualComponent;
	//private Canvas visualComponent;
	private final ReentrantLock paintLock = new ReentrantLock();
	
	/**
	 * Creates a new player instance for the specified file.
	 * 
	 * @param mediaPath the url of the media file
	 * @throws JAVFPlayerException any exception occurring when creating a native player
	 */
	public AVFNativeMediaPlayer(String mediaPath) throws JAVFPlayerException {
		super(mediaPath);
		// super calls initMediaPlayer
	}
	
	/**
	 * Creates the player containing the native methods, which is wrapped by 
	 * this player.
	 * 
	 * @throws JAVFPlayerException any exception occurring when creating a native player
	 */
	@Override
	void initMediaPlayer() throws JAVFPlayerException {
		try {
			avfPlayer = new AVFNativePlayer(mediaPath);
		} catch (JAVFPlayerException je) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("Cannot create %s, message: %s", AVFNativePlayer.class.getName(), 
						je.getMessage()));
			}
			throw je;
		}
		
		if (avfPlayer.hasVideo()) {
			visualComponent = new AVFNCanvas();
		}
	}


	/**
	 * Returns the visual component.
	 * 
	 * @return the canvas for the player, can be null.
	 */
	public Component getVisualComponent() {
		return visualComponent;
	}
	
	/**
	 * Called when the player is going to be closed and resources need to be released
	 * as much as possible.
	 * This deletes the native player, stops the thread that checks the media time 
	 * (the presentation clock) clears cached video images.
	 */
	@Override
	public void deletePlayer() {
		avfPlayer.deletePlayer();
		// maybe something needs to be done with the visual component?
	}

	/*######### Wrapper getters for getter methods in AVFNativePlayer ########*/
	/**
	 * @return the original, encoded size of the video
	 */
	@Override
	public Dimension getOriginalSize() {
		return avfPlayer.getOriginalSize();
	}
	
	/**
	 * Sets the scale factor (zoom level) for the video.
	 * 
	 * @param scaleFactor the scale factor for the video (2.0 = 200%)
	 */
	@Override
	public void setVideoScaleFactor(float scaleFactor) {
		super.setVideoScaleFactor(scaleFactor);
		avfPlayer.setVideoScaleFactor(scaleFactor);

		componentResized();
		visualComponent.nativeVideoBoundsChanged();
	}

	@Override
	public float getVideoScaleFactor() {
		return super.getVideoScaleFactor();
	}

	@Override
	public void repaintVideo() {
		avfPlayer.repaintVideo();
	}

	@Override
	public void setVideoBounds(int x, int y, int w, int h) {		
		avfPlayer.setVideoBounds(x, y, w, h);

	}

	@Override
	public int[] getVideoBounds() {
		return avfPlayer.getVideoBounds();
	}

	/**
	 * Moves the native, enlarged video image in a separate thread.
	 * 
	 * @param dx x-axis movement
	 * @param dy y-axis movement
	 */
	@Override
	public void moveVideoPos(int dx, int dy) {
		if (dx == 0 && dy == 0) {
			return;
		}
	
		new MoveThread(dx, dy).start();
	}

	/**
	 * Called by the canvas when it receives a (real or relevant) resize event.
	 */
	void componentResized() {
		//System.out.println("Canvas resized");
		if (videoScaleFactor > 1) {
			int[] curBounds = getVideoBounds();
			if (curBounds != null) {
				int nw = visualComponent.getWidth();
				int nh = visualComponent.getHeight();
				float bw = nw * videoScaleFactor;
				float bh = nh * videoScaleFactor;
				// try to maintain the point in the video that is in the top left corner of the canvas
				int bx = (int) (vxToTlcPerc * bw);
				int by = (int) (vyToTlcPerc * bh);
				// after a change in the zoom or scale factor, it might happen that not the entire canvas
				// is covered by a part of the video, correct it here
				bx = bx + bw < nw ? (int)(nw - bw) : bx;
				by = by + bh < nh ? (int)(nh - bh) : by;
				setVideoBounds(bx, by, (int)bw, (int)bh);
			} else {
				System.out.println("Cannot set video bounds, old bounds is null");
			}			
		} else {
			setVideoBounds(0, 0, visualComponent.getWidth(), visualComponent.getHeight());
		}
	}

	/**
	 * A thread to recalculate the bounds of the native video layer after 
	 * dragging the layer with the mouse.
	 *
	 */
	class MoveThread extends Thread {
		private int dx;
		private int dy;
		
		public MoveThread(int dx, int dy) {
			super("MoveThread");
			this.dx = dx;
			this.dy = dy;
			//System.out.println("dx: " + dx + " dy: " + dy);
		}

		@Override
		public void run() {
			try {
				if (paintLock.tryLock(1, TimeUnit.MILLISECONDS)) {
					try {
						int[] oldBounds = getVideoBounds();
						Dimension curCanvasSize = visualComponent.getSize();
						//System.out.println(String.format("Vid: %d, %d, %d, %d", oldBounds[0], oldBounds[1], oldBounds[2], oldBounds[0]));
						 
						if (oldBounds != null && curCanvasSize != null) {
							int nx = oldBounds[0] + dx > 0 ? 0 : oldBounds[0] + dx;
							nx = nx + oldBounds[2] < curCanvasSize.width ? curCanvasSize.width - oldBounds[2] : nx;
							
							int ny = oldBounds[1] + dy > 0 ? 0 : oldBounds[1] + dy;
							ny = (ny + oldBounds[3]) < curCanvasSize.height ? (curCanvasSize.height - oldBounds[3]) : ny;
							if (nx != oldBounds[0] || ny != oldBounds[1]) {
								vxToTlcPerc = nx / (double) oldBounds[2];
								vyToTlcPerc = ny / (double) oldBounds[3];
								AVFNativeMediaPlayer.this.setVideoBounds(nx, ny, oldBounds[2], oldBounds[3]);
							}
						}
					} finally {
						paintLock.unlock();
					}
				}
			} catch (InterruptedException ie) {
				//System.out.println("Move interrupted");
			}
		}		
	}
	
	/**
	 * A host canvas for a native video player layer. Attempts are made to let 
	 * the native video stay in sync with the host when resizing and scaling etc.
	 */
	@SuppressWarnings("serial")
	class AVFNCanvas extends Canvas implements ComponentListener {
		// a workaround flag for the first time componentMoved is called,
		// to make sure that the native video bounds correspond to the canvas' bounds
		// which is not always the case initially (mismatch with the LW Peer's bounds)
		boolean firstMove = true;
		
		/**
		 * Constructor.
		 */
		public AVFNCanvas() {
			super();
			setBackground(new Color(0, 64, 64));// a dark green background
			this.addComponentListener(this);
		}

		@Override
		public void addNotify() {
			super.addNotify();
			((AVFNativePlayer) avfPlayer).setVisualComponent(visualComponent);

			Window w = null;
			Component parComp = this.getParent();
			if (parComp != null) {
				parComp.addComponentListener(this);
				
				while ((parComp = parComp.getParent()) != null) {
					if (parComp instanceof JRootPane) {
						parComp.addComponentListener(this);
						//break;
					}
					if (parComp instanceof Window) {
						w = (Window) parComp;
						break;
					}
				}
			}
			afterNotify(w, this);
		}

		/**
		 * Notify the native player so that it can remove its player layer (or layers)
		 * from the native component.
		 */
		@Override
		public void removeNotify() {
			((AVFNativePlayer) avfPlayer).disconnectVisualComponent(visualComponent);

			Window w = null;
			Component parComp = this.getParent();
			if (parComp != null) {
				parComp.removeComponentListener(this);
				
				while ((parComp = parComp.getParent()) != null) {
					if (parComp instanceof JRootPane) {
						parComp.removeComponentListener(this);
						//break;
					}
					if (parComp instanceof Window) {
						w = (Window) parComp;
						break;
					}
				}
			}
			
			super.removeNotify();
			
			afterNotify(w, this);
		}

		/**
		 * Forces the native peer to update (redraw) its view by changing the bounds of the
		 * canvas twice. This is necessary because the default NSView behavior is to 
		 * redraw when a resize event occurs (and in the JNI code we haven't found a way yet
		 * to either change this setting or to trigger a native resize event). 
		 */
		void nativeVideoBoundsChanged() {
			Rectangle rect = this.getBounds();
			int reshapeW = rect.width + 1;
			int reshapeH = rect.height;
			if (reshapeW <= 1 || reshapeH <= 0) {
				return;
			}
			setSize(reshapeW, reshapeH);
			
			reshapeW -= 1;
			setSize(reshapeW, reshapeH);
		}
		
		/**
		 * Tries to force visibility (and correct size) of the video after it 
		 * has been added to or removed from a window. By resizing the window
		 * twice, which causes a low level resize event, see comments with 
		 * {@link AVFNCanvas#nativeVideoBoundsChanged()}.
		 * 
		 * @param w the window containing the the video component
		 * @param videoComp the video canvas
		 * 
		 * @see AVFNCanvas#nativeVideoBoundsChanged()
		 */
		void afterNotify(final Window w, final AVFNCanvas videoComp) {
			if (w == null) return;
			
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					 if (w != null && w.isDisplayable()) {
						 Dimension d = w .getSize();
						 w.setSize(d.width + 1, d.height + 1);
						 w.setSize(d);
						 
						 if (videoComp != null) {
							 videoComp.nativeVideoBoundsChanged();
						 }
					 }				
				}
				
			});	
		}
		
		/**
		 * Component resized event handling. If the component's size has really been 
		 * changed, a recalculation of the video bounds relative to the canvas will
		 * be performed. If this event is triggered by an internal resize action in order
		 * to force the native view to redraw itself, this is detected here and the
		 * event is ignored.
		 * 
		 *  @param e the component event
		 */
		@Override
		public void componentResized(ComponentEvent e) {
			if (e.getSource() == this) {
				AVFNativeMediaPlayer.this.componentResized();
			} else if (e.getSource() == getParent()) {
				nativeVideoBoundsChanged();// force the video layer to reposition itself
			} else {
				// the root pane (the window) resized
				nativeVideoBoundsChanged();// force the video layer to reposition itself
			}
		}

		/**
		 * This event is ignored except for the first time it is received.
		 * In that case it tries to force the native video bounds to update
		 * it bounds to the canvases. Otherwise it is sometimes 10 to 20 pixels off
		 * until a resize event is received.
		 */
		@Override
		public void componentMoved(ComponentEvent e) {
			if (firstMove) {
				nativeVideoBoundsChanged();
				firstMove = false;
			}
		}

		@Override
		public void componentShown(ComponentEvent e) {}//stub

		@Override
		public void componentHidden(ComponentEvent e) {}//stub
		
	}

}
