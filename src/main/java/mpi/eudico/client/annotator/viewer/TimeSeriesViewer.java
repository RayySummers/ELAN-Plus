package mpi.eudico.client.annotator.viewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections; // Lefvert - May 9 2006
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.DetachedViewerFrame;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.timeseries.AbstractTSTrack;
import mpi.eudico.client.annotator.timeseries.ExportTrack;
import mpi.eudico.client.annotator.timeseries.ExtractDataMultiStep;
import mpi.eudico.client.annotator.timeseries.TSRulerImpl;
import mpi.eudico.client.annotator.timeseries.TSTrackManager;
import mpi.eudico.client.annotator.timeseries.TSTrackPanelImpl;
import mpi.eudico.client.annotator.timeseries.TimeSeriesChangeEvent;
import mpi.eudico.client.annotator.timeseries.TimeSeriesChangeListener;
import mpi.eudico.client.annotator.timeseries.TimeSeriesTrack;
import mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration;
import mpi.eudico.client.annotator.timeseries.config.TSTrackConfiguration;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StartEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A viewer that can handle one or more TimeSeriesTrack panels, adding a
 * horizontal ruler, a crosshair, selection (time interval) etc.
 */
@SuppressWarnings("serial")
public class TimeSeriesViewer extends DefaultTimeScaleBasedViewer
    implements AdjustmentListener, TimeSeriesChangeListener, MouseListener {
    private TranscriptionImpl transcription;
    private TSTrackManager trackManager;
    private static final String ADD_PR = "add-";
    private static final String REM_PR = "rem-";
    private static final String RANGE = "ran-";
    private static final String EXP_PR = "exp-";
    private static final int DEF_PANEL_HEIGHT = 300;

    // a separate buf img for the hor. ruler
    private BufferedImage ri;
    private Graphics2D rug2d;

    private int vertRulerWidth;
    private int verticalScrollOffset;

    // vertical scrolling
    private JScrollBar scrollBar;

    /** vertical scrollbar width */
    private int defBarWidth;

    /** a list of trackpanels */
    private List<TSTrackPanelImpl> trackPanels;
    private int trackPanelHeight;
    private Insets panelMargins;
    private TSTrackPanelImpl selTrackPanel;
    private boolean autoFitVertical = true;

    /** menu's and menu items */
    private JCheckBoxMenuItem fitVerticalMI;
    private JCheckBoxMenuItem showTrackValueMenu;
    private JMenu trPanelMenu;
    private JMenuItem addPanelMI;
    private JMenuItem removePanelMI;
    private JMenu setRangeMenu;
    private JMenu addTrackMenu;
    private JMenu removeTrackMenu;
    private JMenuItem extractDataMI;
    private JMenuItem configureTrMI;
    private JMenuItem removeAllPanelsMI;
    private JMenuItem addPanelForEachTrackMI;
    private JMenuItem detachItem;
    private JMenuItem combinedRangeMI;
    private JMenu exportTrackMenu;

    private boolean showTrackValues;
    private boolean isPlaying;
    
    // $sidgrid
    private JMenuItem addAllTrackMI;
    private JMenuItem removeAllTrackMI;
    
    /** for use synchronisation mode */
    private boolean syncModeViewer = false;
    private boolean syncConnected = false;
    
    private final ReentrantLock paintLock = new ReentrantLock();
    private int paintLockTimeOut = 20;

    /**
     * Constructor.
     */
    public TimeSeriesViewer() {
        super();
        initViewer();
        defBarWidth = getDefaultBarWidth();

        paintBuffer();
        addMouseWheelListener(this);
        addEmptyTrackPanel();
    }

    /**
     * Constructor. The specified transcription is used as a document within
     * ELAN this viewer is associated with.
     *
     * @param transcription the transcription
     */
    public TimeSeriesViewer(Transcription transcription) {
        this();
        this.transcription = (TranscriptionImpl) transcription;

        //testTrack();
    }

    /**
     * Constructor. The specified transcription is used as a document within
     * ELAN this viewer is associated with.
     *
     * @param transcription the transcription
     * @param trackManager the track manager
     */
    public TimeSeriesViewer(Transcription transcription,
        TSTrackManager trackManager) {
        this();
        this.transcription = (TranscriptionImpl) transcription;
        this.trackManager = trackManager;

        if (trackManager != null) {
            // register as a listener
            trackManager.addTimeSeriesChangeListener(this);
        }

        //testTrack();
    }

    // testing for sync mode
    /**
     * Returns whether the viewer is in media synchronization mode.
     * 
     * @return {@code true} if the viewer is in synchronization mode
     */
    public boolean isSyncModeViewer() {
		return syncModeViewer;
	}

    /**
     * Sets whether the viewer is in synchronization mode.
     * 
     * @param syncModeViewer if {@code true} the viewer is in synchronization
     * mode
     */
	public void setSyncModeViewer(boolean syncModeViewer) {
		this.syncModeViewer = syncModeViewer;
	}
	
	/**
	 * Sets whether the viewer is connected in sync mode.
	 * 
	 * @param syncConnected if {@code true} the viewer is sync connected, 
	 * otherwise it is disconnected
	 */
	public void setSyncConnected(boolean syncConnected) {
		this.syncConnected = syncConnected;
	}
	
	@Override
	public void setPlayer(ElanMediaPlayer player) {
		//if (!syncModeViewer || syncConnected) {
			super.setPlayer(player);
			controllerUpdate(new TimeEvent(player));
			System.out.println("Player: " + player);
		//}
	}

	/**
     * Returns the track manager.
     *
     * @return the track manager
     */
    public TSTrackManager getTrackManager() {
        return trackManager;
    }

    /**
     * Sets the track manager.
     *
     * @param manager the track manager
     */
    public void setTrackManager(TSTrackManager manager) {
        trackManager = manager;

        if (trackManager != null) {
            // register as a listener
            trackManager.addTimeSeriesChangeListener(this);
        }
    }

    /**
     * Performs the initialization of fields and sets up the viewer.
     */
    @Override
	protected void initViewer() {
        super.initViewer();
        trackPanels = new ArrayList<TSTrackPanelImpl>(4);
        vertRulerWidth = 43;
        trackPanelHeight = DEF_PANEL_HEIGHT;
        panelMargins = new Insets(3, 3, 3, 0);
        showTrackValues = true;
        isPlaying = false;

        verticalScrollOffset = 0;
        scrollBar = new JScrollBar(JScrollBar.VERTICAL, 0, 50, 0, 200);
        scrollBar.setBlockIncrement(trackPanelHeight);
        //scrollBar.setUnitIncrement(5);
        scrollBar.addAdjustmentListener(this);
        //setLayout(null);
        add(scrollBar);
    }

    /**
     * Override <code>JComponent</code>'s paintComponent.
     * It paints:<ul>
     * <li> a BufferedImage with a ruler and the tags
     * <li> the cursor / crosshair
     * </ul>
     *
     * @param g the graphics object
     */
    @Override
	public void paintComponent(Graphics g) {    		
        super.paintComponent(g);

        synchronized (getTreeLock()) {
	        Graphics2D g2d = (Graphics2D) g;//g.create();
	        int h = getHeight();
			
	        if (!useBufferedImage) {
	        	paintUnbuffered(g2d);
	        } else {
		        if (SystemReporting.antiAliasedText) {
			        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		        }        
		
		        if (h > imageHeight) {
		            g2d.setColor(Color.WHITE);
		            g2d.fillRect(0, 0, imageWidth, h);
		
		            if (bi != null) {
		                // keeps the image at the bottom, with the hor. ruler
		                g2d.drawImage(bi, 0, h - imageHeight - verticalScrollOffset,
		                    null);
		            }
		        } else {
		            if (bi != null) {
		                g2d.drawImage(bi, 0, -verticalScrollOffset, null);
		            }
		        }
		
		        if (ri != null) {
		        	g2d.drawImage(ri, vertRulerWidth, h - rulerHeight, null);
		        	//g2d.drawImage(ri, vertRulerWidth, h - rulerHeight, imageWidth, h, vertRulerWidth, 0, ri.getWidth(), ri.getHeight(), null);
		        }
	
	        }
	        // paint selection: here or in paintBuffer
	        // paint selection, not in the vertical ruler
	        if ((selectionBeginPos != selectionEndPos) &&
	                (selectionEndPos > vertRulerWidth)) {
	            g2d.setColor(Constants.SELECTIONCOLOR);
	            g2d.setComposite(alpha05);
	
	            if (selectionBeginPos < vertRulerWidth) {
	                selectionBeginPos = vertRulerWidth;
	            }
	
	            g2d.fillRect(selectionBeginPos, h - rulerHeight,
	                (selectionEndPos - selectionBeginPos), rulerHeight);
	            //g2d.setComposite(alpha07);
	            g2d.fillRect(selectionBeginPos, 0,
	                (selectionEndPos - selectionBeginPos), h - rulerHeight);
	            g2d.setComposite(AlphaComposite.Src);
	        }
	
	        if ((crossHairPos >= vertRulerWidth) && (crossHairPos <= imageWidth)) {
	            // prevents drawing outside the component on Mac
	            g2d.setColor(Constants.CROSSHAIRCOLOR);
	            g2d.drawLine(crossHairPos, 0, crossHairPos, h);
	
	            // Added by AR
	            if (showTrackValues && !isPlaying) {
		            long beginTime = timeAt(crossHairPos);
		            long endTime = timeAt(crossHairPos + 1);
		            if (beginTime >= endTime) {
		            	return;
		            }
		            int labelHeight = g2d.getFontMetrics(g2d.getFont()).getHeight() + 1;
		            for (int i = 1; i <= trackPanels.size(); i++) {
		            	TSTrackPanelImpl panel = getPanelAtY(h - rulerHeight - (i * trackPanelHeight) + 2);
	
		            	if (panel != null) {
			            	List<AbstractTSTrack> tracks = panel.getTracks();
			            	for (int j = 0; j < tracks.size(); j++) {
			            		//int trackHeight = trackPanelHeight / tracks.size();
				            	Color trackColor = tracks.get(j).getColor();
				            	float average = tracks.get(j).getAverage(beginTime, endTime);
				            	if (!Float.isNaN(average)) {
				            		String label = Float.toString(average);
				            		g2d.setColor(trackColor);
				            		g2d.drawString(label, crossHairPos + 4, h - rulerHeight - (i * trackPanelHeight) + ((j + 1) * labelHeight));
				            	}
			            	}
		            	}
		            }
	            }
	        }
        }
    }

    /**
     * Paint to a buffer.
     * First paint the top ruler, next the annotations of the current tier.
     */
    private void paintBuffer() {
        if ((getWidth() <= 0) || (getHeight() <= 0)) {
            return;
        }

        if (imageWidth != (getWidth() - defBarWidth)) {
            imageWidth = Math.max(getWidth() - defBarWidth, 1);
        }

        int h = (trackPanels.size() * trackPanelHeight) + rulerHeight;

        if (imageHeight != h) {
            imageHeight = (getHeight() > h) ? getHeight() : h;
            if (imageHeight <= 0) {
            	return;
            }
        }
        imageHeight = Math.max(imageHeight, 1);

        intervalEndTime = intervalBeginTime +
            (int) (intervalWidth * msPerPixel);
        
        // make sure imageWidth and imageHeight are always calculated
    	if (!useBufferedImage) {
    		repaint();
    		return;
    	}
    	//System.out.println("Paint Buffer Thread: " + Thread.currentThread().getId());
        if ((bi == null) || (bi.getWidth() < imageWidth) ||
                (bi.getHeight() < imageHeight)) {
            bi = new BufferedImage(imageWidth, imageHeight,
                    BufferedImage.TYPE_INT_RGB);
            big2d = bi.createGraphics();
        }

        if (SystemReporting.antiAliasedText) {
	        big2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        big2d.setColor(Color.WHITE);
        big2d.fillRect(0, 0, imageWidth, bi.getHeight());

        TSTrackPanelImpl panel;
        int y = imageHeight - rulerHeight;

        for (int i = 0; i < trackPanels.size(); i++) {
            panel = trackPanels.get(i);
            y -= panel.getHeight();
            big2d.translate(0, y);
            panel.paint(big2d, intervalBeginTime);
            big2d.translate(0, -y);

            //big2d.setTransform(identity);
        }

        big2d.setTransform(identity); //reset transform

        /*paint time ruler */
        if (timeRulerVisible) {
	        if ((ri == null) || (ri.getWidth() < imageWidth - vertRulerWidth) ||
	                (ri.getHeight() != rulerHeight)) {
	            ri = new BufferedImage(imageWidth - vertRulerWidth, rulerHeight,
	                    BufferedImage.TYPE_INT_RGB);
	            rug2d = ri.createGraphics();
	        }

	        rug2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
	        rug2d.fillRect(0, 0, imageWidth, rulerHeight);
	        rug2d.setColor(Color.DARK_GRAY);
	        rug2d.drawLine(0, 0, imageWidth, 0);
	        rug2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
	        rug2d.translate(-(intervalBeginTime / msPerPixel), 0.0);
	        //rug2d.translate(vertRulerWidth, 0);
	        //big2d.translate(vertRulerWidth, y);
	        ruler.paint(rug2d, intervalBeginTime, imageWidth, msPerPixel,
	            SwingConstants.TOP);
	        rug2d.setTransform(identity); //reset transform
        }

        //big2d.setTransform(identity); //reset transform
        repaint();
    }
    
    private void paintUnbuffered(Graphics2D g2d) {
        if (SystemReporting.antiAliasedText) {
	        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        
        int h = (trackPanels.size() * trackPanelHeight) + rulerHeight;
        h = getHeight() > h ? getHeight() : h;
        int w = getWidth() - defBarWidth;
        // get current transform
        AffineTransform orgTransform = g2d.getTransform();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, w, h);
        
        TSTrackPanelImpl panel;
        int y = h - rulerHeight - verticalScrollOffset;

        for (int i = 0; i < trackPanels.size(); i++) {
            panel = trackPanels.get(i);
            y -= panel.getHeight();
            g2d.translate(0, y);
            panel.paint(g2d, intervalBeginTime);
            g2d.translate(0, -y);

        }
        
        g2d.setTransform(orgTransform); //reset transform
        int h2 = getHeight() - rulerHeight;
        /*paint time ruler */
        if (timeRulerVisible) {
	        g2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
	        g2d.fillRect(vertRulerWidth, h2, w - vertRulerWidth, rulerHeight);
	        g2d.setColor(Color.DARK_GRAY);
	        g2d.drawLine(vertRulerWidth, h2, w, h2);
	        g2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
        	g2d.translate(-(intervalBeginTime / msPerPixel) + vertRulerWidth, h2);
	        //rug2d.translate(vertRulerWidth, 0);
	        //big2d.translate(vertRulerWidth, y);
        	g2d.setClip((int)(intervalBeginTime / msPerPixel), 0, w, h);
	        ruler.paint(g2d, intervalBeginTime, w - vertRulerWidth, msPerPixel,
	            SwingConstants.TOP);
	        g2d.setTransform(orgTransform); //reset transform
	        g2d.setClip(null);
        }
    }

    /**
     * Adds a track panel to the viewer. The panel's settings are forced to
     * comply to the viewer's settings.
     *
     * @param trackPanel the track panel to add
     */
    public void addTSTrackPanel(TSTrackPanelImpl trackPanel) {
        trackPanel.setHeight(trackPanelHeight);
        trackPanel.setWidth(getWidth() - defBarWidth);
        trackPanel.setMargin(panelMargins);
        trackPanel.setRulerWidth(vertRulerWidth - panelMargins.left);
        trackPanel.setMsPerPixel(msPerPixel);
        trackPanels.add(trackPanel);
        adjustPanelHeight();
        paintBuffer();
        updateScrollBar();
    }

    /**
     * Adds an empty default track panel.
     */
    public void addEmptyTrackPanel() {
        TSTrackPanelImpl tsp = new TSTrackPanelImpl();
        TSRulerImpl ruler = new TSRulerImpl();
        if (Constants.DEFAULT_LF_LABEL_FONT != null) {
        	Constants.deriveSmallFont(Constants.DEFAULT_LF_LABEL_FONT);
        } else {
        	ruler.setFont(Constants.SMALLFONT);
        }        
        tsp.setRuler(ruler);
        addTSTrackPanel(tsp);
    }

    /**
     * Removes a track panel from the viewer.
     *
     * @param trackPanel the track panel to remove
     */
    public void removeTSTrackPanel(TSTrackPanelImpl trackPanel) {
        trackPanels.remove(trackPanel);
        adjustPanelHeight();
        paintBuffer();
        updateScrollBar();
    }

    /**
     * Returns the width for the vertical ruler.
     *
     * @return the width for the vertical ruler
     */
    public int getVerticalRulerWidth() {
        return vertRulerWidth;
    }

    /**
     * Sets the width for the vertical ruler.
     *
     * @param rulerWidth the width for the vertical ruler
     */
    public void setVerticalRulerWidth(int rulerWidth) {
        vertRulerWidth = rulerWidth;

        TSTrackPanelImpl panel;
        for (int i = 0; i < trackPanels.size(); i++) {
            panel = trackPanels.get(i);
            panel.setRulerWidth(vertRulerWidth - panelMargins.left);
        }
        paintBuffer();
    }

    /**
     * Returns the vertical scroll offset.
     *
     * @return the vertical scroll offset
     */
    public int getVerticalScrollOffset() {
        return verticalScrollOffset;
    }

    /**
     * Sets the vertical scroll offset on this component.
     *
     * @param offset the new vertical scroll offset
     */
    public void setVerticalScrollOffset(int offset) {
        verticalScrollOffset = offset;
        repaint();
    }

    /**
     * Returns the current number of track panels.
     *
     * @return the number of track panels
     */
    public int getNumberOfTrackPanels() {
        return trackPanels.size();
    }

    /**
     * Sets the number of track panels. Adds a number of empty track panels
     * until there are the given number of panels. If there are already
     * more panels nothing happens.
     *
     * @param num the number of panels
     */
    public void setNumberOfTrackPanels(int num) {
        int curSize = trackPanels.size();

        for (int i = 0; i < (num - curSize); i++) {
            addEmptyTrackPanel();
        }
    }

    /**
     * Returns the names of the tracks of the track panel at the specified index.
     *
     * @param panelIndex index of the panel
     *
     * @return an {@code ArrayList} of track names
     */
    public List<String> getTracksForPanel(int panelIndex) {
        if ((panelIndex < 0) || (panelIndex >= trackPanels.size())) {
            return null; // rather than ArrayIndexOutOfBoundsException
        }

        TSTrackPanelImpl panel = trackPanels.get(panelIndex);
        List<AbstractTSTrack> tracks = panel.getTracks();
        AbstractTSTrack tr;
        //String[] names = new String[tracks.size()];
        List<String> names = new ArrayList<String>(tracks.size());

        for (int i = 0; i < tracks.size(); i++) {
            tr = tracks.get(i);
            names.add(tr.getName());
        }

        return names;
    }

    /**
     * Tells the viewer to add the given predefined tracks to the panel
     * at index <code>panelIndex</code>.
     *
     * @param panelIndex the index of the panel
     * @param trackNames the names of the tracks to add
     */
    public void setTracksForPanel(int panelIndex, List<String> trackNames) {
        if ((panelIndex < 0) || (panelIndex >= trackPanels.size()) ||
                (trackNames == null)) {
            return; // rather than ArrayIndexOutOfBoundsException
        }

        TSTrackPanelImpl panel = trackPanels.get(panelIndex);
        String name;
        AbstractTSTrack tr;

        for (int i = 0; i < trackNames.size(); i++) {
            name = trackNames.get(i);

            if (name != null) {
            	if (panel.getTrack(name) != null) {
            		continue;
            	}
                tr = trackManager.getTrack(name);

                if (tr != null) {
                    panel.addTrack(tr);
                    panel.getRuler().setRange(tr.getRange());
                    panel.getRuler().setUnitString(tr.getUnitString());
                }
            }
        }

        paintBuffer();
    }

    /**
     * Update the values of the scrollbar.
     * Called after a change in the number of visible tracks.
     */
    private void updateScrollBar() {
        int value = scrollBar.getValue();
        int max = (imageHeight > getHeight()) ? imageHeight : getHeight();

        // before changing scrollbar values do a setValue(0), otherwise
        // setMaxiimum and/or setVisibleAmount will not be accurate
        scrollBar.setValue(0);
        scrollBar.setMaximum(max);
        scrollBar.setVisibleAmount(getHeight());

        if ((value + getHeight()) > max) {
            value = max - getHeight();
        }

        scrollBar.setValue(value);
        scrollBar.revalidate();
    }

    /**
     * 
     */
    @Override
	public int getLeftMargin() {
        return vertRulerWidth;
    }

    /**
     * 
     */
    @Override
	public int getRightMargin() {
        return defBarWidth;
    }

    /**
     * Allows the viewer to be adapted to another viewer's right margin.
     * @param width the new width for the scroll bar
     */
    public void setRightMargin(int width) {
    	defBarWidth = width;
    }

    /**
     * @see mpi.eudico.client.annotator.viewer.DefaultTimeScaleBasedViewer#createPopupMenu()
     */
    @Override
	protected void createPopupMenu() {
        super.createPopupMenu();
        if (!attached) {
        	timeScaleConMI.setSelected(false);
            timeScaleConMI.setEnabled(false);
        } else {
        	timeScaleConMI.setEnabled(true);
        }
        fitVerticalMI = new JCheckBoxMenuItem(ElanLocale.getString(
                    "TimeSeriesViewer.TrackPanel.FitVertically"));
        fitVerticalMI.setSelected(autoFitVertical);
        fitVerticalMI.addActionListener(this);
        popup.add(fitVerticalMI);

        showTrackValueMenu = new JCheckBoxMenuItem(ElanLocale.getString(
        		"TimeSeriesViewer.Track.ShowValues"));
        showTrackValueMenu.addActionListener(this);
        showTrackValueMenu.setSelected(showTrackValues);
        popup.add(showTrackValueMenu);

        detachItem = new JMenuItem();
        if (isAttached()) {
        	detachItem.setText(ElanLocale.getString("Detachable.Detach"));
        } else {
        	detachItem.setText(ElanLocale.getString("Detachable.Attach"));
        }
        detachItem.addActionListener(this);
        popup.add(detachItem);
        popup.addSeparator();

        addPanelMI = new JMenuItem(ElanLocale.getString(
                    "TimeSeriesViewer.TrackPanel.AddPanel"));
        addPanelMI.addActionListener(this);
        popup.add(addPanelMI);
        removePanelMI = new JMenuItem(ElanLocale.getString(
                    "TimeSeriesViewer.TrackPanel.RemovePanel"));
        removePanelMI.addActionListener(this);
        popup.add(removePanelMI);
        //popup.addSeparator();
        addPanelForEachTrackMI = new JMenuItem(ElanLocale.getString(
                "TimeSeriesViewer.TrackPanel.AddPanelForEachTrack"));
        addPanelForEachTrackMI.addActionListener(this);
        popup.add(addPanelForEachTrackMI);
        removeAllPanelsMI = new JMenuItem(ElanLocale.getString(
                "TimeSeriesViewer.TrackPanel.RemoveAllPanels"));
        removeAllPanelsMI.addActionListener(this);
        popup.add(removeAllPanelsMI);
        popup.addSeparator();

        trPanelMenu = new JMenu(ElanLocale.getString(
                    "TimeSeriesViewer.TrackPanel"));
        popup.add(trPanelMenu);
        setRangeMenu = new JMenu(ElanLocale.getString(
                    "TimeSeriesViewer.TrackPanel.SetRange"));
        trPanelMenu.add(setRangeMenu);
        addTrackMenu = new JMenu(ElanLocale.getString(
                    "TimeSeriesViewer.Track.Add"));
        trPanelMenu.add(addTrackMenu);
        removeTrackMenu = new JMenu(ElanLocale.getString(
                    "TimeSeriesViewer.Track.Remove"));
        trPanelMenu.add(removeTrackMenu);


        popup.addSeparator();

        // $sidgrid
        trPanelMenu.addSeparator();
        addAllTrackMI = new JMenuItem(ElanLocale.getString(
							     "TimeSeriesViewer.Track.Addall"));
        addAllTrackMI.addActionListener(this);
        trPanelMenu.add(addAllTrackMI);
        removeAllTrackMI = new JMenuItem(ElanLocale.getString(
								"TimeSeriesViewer.Track.Removeall"));
        removeAllTrackMI.addActionListener(this);
        trPanelMenu.add(removeAllTrackMI);
	    // end $sidgrid

        extractDataMI = new JMenuItem(ElanLocale.getString(
                    "TimeSeriesViewer.Extract"));
        extractDataMI.addActionListener(this);
        popup.add(extractDataMI);
        configureTrMI = new JMenuItem(ElanLocale.getString(
                    "TimeSeriesViewer.Tracks.Configure"));
        configureTrMI.addActionListener(this);
        popup.add(configureTrMI);
        popup.addSeparator();
        exportTrackMenu = new JMenu(ElanLocale.getString("TimeSeriesViewer.ExportTrack"));
        popup.add(exportTrackMenu);
        combinedRangeMI = new JMenuItem(ElanLocale.getString(
			"TimeSeriesViewer.TrackPanel.SetRangeCombined"));
        combinedRangeMI.setActionCommand(RANGE + "ALL");
        combinedRangeMI.addActionListener(this);
    }

    /**
     * @see mpi.eudico.client.annotator.viewer.DefaultTimeScaleBasedViewer#updatePopup(java.awt.Point)
     */
    @Override
	protected void updatePopup(Point p) {
        selTrackPanel = null;
        removePanelMI.setEnabled(false);
        trPanelMenu.setEnabled(false);

        if (!pointInHorizontalRuler(p.y)) {
            Point inverse = new Point(p);
            inverse.y += verticalScrollOffset;
            selTrackPanel = getPanelAtY(inverse.y);

            if (selTrackPanel != null) {
                removePanelMI.setEnabled(true);
                trPanelMenu.setEnabled(true);
            }
        }

        addTrackMenu.removeAll();
        removeTrackMenu.removeAll();
        setRangeMenu.removeAll();
        exportTrackMenu.removeAll();

        removeAllPanelsMI.setEnabled(trackPanels.size() > 0);


        if (selTrackPanel == null) {
            return;
        }

        if (trackManager != null) {
            List<TimeSeriesTrack> trs = trackManager.getRegisteredTracks();

            if ((trs.size() == 0) || (transcription.getTiers().size() == 0)) {
                extractDataMI.setEnabled(false);
                exportTrackMenu.setEnabled(false);
            } else {
                extractDataMI.setEnabled(true);
                exportTrackMenu.setEnabled(true);
            }
	    // Lefvert - May 9 2006
	    // Sort the menu items in alphabetical order
	    Collections.sort(trs);

            AbstractTSTrack tra;

            for (int i = 0; i < trs.size(); i++) {
                tra = (AbstractTSTrack) trs.get(i);

                if ((tra != null) && !selTrackPanel.getTracks().contains(tra)) {
                    JMenuItem it = new JMenuItem(tra.getName());
                    it.setActionCommand(ADD_PR + tra.getName());
                    it.addActionListener(this);
                    addTrackMenu.add(it);
                }
                if (tra != null) {
                    JMenuItem it = new JMenuItem(tra.getName());
                    it.setActionCommand(EXP_PR + tra.getName());
                    it.addActionListener(this);
                    exportTrackMenu.add(it);
                }
            }

            List<AbstractTSTrack> curTracks = selTrackPanel.getTracks();

            if (curTracks.size() > 1) {
            	setRangeMenu.add(combinedRangeMI);
            	setRangeMenu.addSeparator();
            }
            for (int i = 0; i < curTracks.size(); i++) {
                tra = curTracks.get(i);

                if (tra != null) {
                    JMenuItem it = new JMenuItem(tra.getName());
                    it.setActionCommand(REM_PR + tra.getName());
                    it.addActionListener(this);
                    removeTrackMenu.add(it);

                    JMenuItem is = new JMenuItem(tra.getName());
                    is.setActionCommand(RANGE + tra.getName());
                    is.addActionListener(this);
                    setRangeMenu.add(is);
                }
            }

            removeAllTrackMI.setEnabled(curTracks.size() > 0);
        }
    }

    /**
     * Recalculates the milliseconds per pixel such that the selection fills 
     * (almost) the entire width of the viewer.
     */
    @Override
	protected void zoomToSelection() {
    	long selInterval = getSelectionEndTime() - getSelectionBeginTime();
    	if (selInterval < 150) {
    		selInterval = 150;
    	}
    	int sw = imageWidth != 0 ? imageWidth - (2 * SCROLL_OFFSET) - vertRulerWidth : getWidth() - defBarWidth - (2 * SCROLL_OFFSET);
    	float nextMsPP = selInterval / (float) sw;
    	//System.out.println("interval: " + selInterval + " mspp: " + nextMsPP);
    	// set a limit of zoom = 5% or mspp = 200
//    	if (nextMsPP > 200) {
//    		nextMsPP = 200;
//    	}
    	setMsPerPixel(nextMsPP);
    	//customZoomMI.setSelected(true);
    	//customZoomMI.setText(ElanLocale.getString("TimeScaleBasedViewer.Zoom.Custom") + " - " + (int)(100 / ((float) msPerPixel / 10)) + "%");
    	if (!playerIsPlaying()) {
    		long ibt = getSelectionBeginTime() - (long)(SCROLL_OFFSET * msPerPixel);
    		if (ibt < 0) {
    			ibt = 0;
    		}
    		setIntervalBeginTime(ibt);
    	}
    }

    /**
     * Integrate the vertical ruler's width in calculation.
     *
     * @see mpi.eudico.client.annotator.viewer.DefaultTimeScaleBasedViewer#timeAt(int)
     */
    @Override
	public long timeAt(int x) {
        return intervalBeginTime + (int) ((x - vertRulerWidth) * msPerPixel);
    }

    /**
     * Integrate the vertical ruler's width in calculation.
     *
     * @see mpi.eudico.client.annotator.viewer.DefaultTimeScaleBasedViewer#xAt(long)
     */
    @Override
	public int xAt(long t) {
        return super.xAt(t) + vertRulerWidth;
    }

    /**
     * @see mpi.eudico.client.annotator.viewer.TimeScaleBasedViewer#updateTimeScale()
     */

    //public void updateTimeScale() {

    //}
    /**
     * Sets the interval begin time, applying it to this viewer only.
     *
     * @param begin the interval begin time
     */
    @Override
	protected void setLocalTimeScaleIntervalBeginTime(long begin) {
    	//System.out.println("TS Set IBT: " + begin + " Thread: " + Thread.currentThread().getId());
        if (begin == intervalBeginTime) {
            return;
        }
        try {
        	if (paintLock.tryLock() || paintLock.tryLock(20, TimeUnit.MILLISECONDS)) {
                try {
        	        intervalBeginTime = begin;
        	        intervalEndTime = intervalBeginTime +
        	            (long) (intervalWidth * msPerPixel);
        	        crossHairPos = xAt(crossHairTime);
        	        selectionBeginPos = xAt(getSelectionBeginTime());
        	        selectionEndPos = xAt(getSelectionEndTime());
        	        
        	        paintBuffer();
                } finally {
                	paintLock.unlock();
                }
        	}
        } catch (InterruptedException ie) {
        	// ignore
        }
    }

    /**
     * Sets the local resolution, the milliseconds per sample value.
     *
     * @param step new msPerPixel value
     */
    @Override
	protected void setLocalTimeScaleMsPerPixel(float step) {
        if (msPerPixel == step) {
            return;
        }

        //msPerPixel = step;
        if (step >= TimeScaleBasedViewer.MIN_MSPP) {
            msPerPixel = step;
        } else {
            msPerPixel = TimeScaleBasedViewer.MIN_MSPP;
        }

        /*stop the player if necessary*/
        boolean playing = playerIsPlaying();

        if (playing) {
            stopPlayer();
        }

        long mediaTime = getMediaTime();
        int oldScreenPos = crossHairPos;
        long newMediaX = (long) (mediaTime / msPerPixel);
        int numScreens;

        if (intervalWidth > 0) {
            numScreens = (int) (mediaTime / (intervalWidth * msPerPixel));
        } else {
            numScreens = 0;
        }

        int newScreenPos = (int) newMediaX - (numScreens * intervalWidth) + vertRulerWidth;
        int diff = oldScreenPos - newScreenPos;

        //new values
        intervalBeginTime = (long) (((numScreens * intervalWidth) - diff) * msPerPixel);

        if (intervalBeginTime < 0) {
            intervalBeginTime = 0;
        }

        intervalEndTime = intervalBeginTime +
            (long) (intervalWidth * msPerPixel);

        crossHairPos = xAt(mediaTime);
        selectionBeginPos = xAt(getSelectionBeginTime());
        selectionEndPos = xAt(getSelectionEndTime());

        TSTrackPanelImpl panel;

        for (int i = 0; i < trackPanels.size(); i++) {
            panel = trackPanels.get(i);
            panel.setMsPerPixel(msPerPixel);
        }

        paintBuffer();

        if (playing) {
            startPlayer();
        }

        int zoom = (int) (100f * (10f / msPerPixel));

        if (zoom <= 0) {
            zoom = 100;
        }

        updateZoomPopup(zoom);
    }

    /**
     * Removes the contents of the panel.
     * 
     * @author sidgrid
     */
    private void clearPanel() {
		trackPanels.clear();
		adjustPanelHeight();
		paintBuffer();
		updateScrollBar();
    }

    /**
     * Sets new media offset. Should only be called if all tracks in all panels
     * in the viewer should have the same offset.
     *
     * @param offset the new media offset in ms
     */
    @Override
	public void setMediaTimeOffset(long offset) {
        if (offset != mediaTimeOffset) {
            super.setMediaTimeOffset(offset);

            for (int i = 0; i < trackPanels.size(); i++) {
                TSTrackPanelImpl panel = trackPanels.get(i);
                List<AbstractTSTrack> tracks = panel.getTracks();

                for (int j = 0; j < tracks.size(); j++) {
                    AbstractTSTrack track = tracks.get(j);

                    if (track != null) {
                        track.setTimeOffset((int) mediaTimeOffset);
                    }
                }
            }
        }
    }

    /**
     * @see DefaultTimeScaleBasedViewer#pointInHorizontalRuler(int)
     */
    @Override
	protected boolean pointInHorizontalRuler(int yPos) {
        int h = getHeight();

        return (yPos <= h) && (yPos >= (h - rulerHeight));
    }

    /**
     * Finds and returns the track panel at the specified y-coordinate.
     *
     * @param y the y coordinate
     *
     * @return the panel at that point or {@code null}
     */
    private TSTrackPanelImpl getPanelAtY(int y) {
        TSTrackPanelImpl panel = null;
        int h = Math.max(getHeight(), imageHeight);
        int ymin = h;
        int ymax = h - rulerHeight;

        for (int i = 0; i < trackPanels.size(); i++) {
            panel = trackPanels.get(i);
            ymin = ymax - panel.getHeight();

            if ((ymax >= y) && (ymin <= y)) {
                return panel;
            }

            ymax -= panel.getHeight();
        }

        return null;
    }

    /**
     * Change the height of the track panels depending on the {@code auto fit
     * vertically} setting.
     */
    private void adjustPanelHeight() {
        int numPan = trackPanels.size();

        if (numPan > 0) {
            TSTrackPanelImpl panel;

            if (autoFitVertical) {
                trackPanelHeight = (getHeight() - rulerHeight) / numPan;
            } else {
                trackPanelHeight = DEF_PANEL_HEIGHT;
            }

            for (int i = 0; i < trackPanels.size(); i++) {
                panel = trackPanels.get(i);
                panel.setHeight(trackPanelHeight);
            }
        } else {
            if (autoFitVertical) {
                trackPanelHeight = getHeight();
            }
        }
    }

    /**
     * Create a wizard to extract data from a track to a tier, based on
     * annotation intervals of a parent tier.
     */
    private void extractTrackData() {
        // shows a modal dialog
        new ExtractDataMultiStep(transcription, trackManager);
    }

    /**
	 * Scrolls if needed and repaints, now runs on the AWT Event
	 * Dispatching Thread.
	 * 
     * @see mpi.eudico.client.annotator.viewer.AbstractViewer#controllerUpdate(mpi.eudico.client.mediacontrol.ControllerEvent)
     */
    @Override
    void controllerUpdate0(ControllerEvent event) {
    	//System.out.println("TS Media Event Thread: " + Thread.currentThread().getId());
    	if (event instanceof StartEvent) {
    		if (showTrackValues) {
	    		isPlaying = true;
	    		repaint();
    		}
    	} else if (event instanceof StopEvent) {
    		if (showTrackValues) {
	    		isPlaying = false;
	    		repaint();
    		}
    	}
        //super.controllerUpdate(event);
    	else if (event instanceof TimeEvent) {
            crossHairTime = getMediaTime();

            if (!playerIsPlaying()) {
                //if (scroller == null) { to do: drag scrolling
                recalculateInterval(crossHairTime);
                crossHairPos = xAt(crossHairTime);
                repaint();

                //} else {
                //	recalculateInterval(crossHairTime);
                //}
            } else {
                if ((crossHairTime < intervalBeginTime) ||
                        (crossHairTime > intervalEndTime)) {
                    recalculateInterval(crossHairTime);
                } else {
                    // repaint a part of the viewer
                    int oldPos = crossHairPos;
                    crossHairPos = xAt(crossHairTime);

                    int newPos = crossHairPos;

                    if (useBufferedImage) {
	                    if (newPos >= oldPos) {
	                        repaint(oldPos - 2, 0, newPos - oldPos + 4, getHeight());
	
	                        //repaint();
	                    } else {
	                        repaint(newPos - 2, 0, oldPos - newPos + 4, getHeight());
	
	                        //repaint();
	                    }
                    } else {
                    	repaint();
                    }
                }
            }
        }
    }

    /**
     * @see mpi.eudico.client.annotator.viewer.AbstractViewer#updateSelection()
     */
    @Override
	public void updateSelection() {
        selectionBeginPos = xAt(getSelectionBeginTime());

        if (selectionBeginPos < vertRulerWidth) {
            selectionBeginPos = vertRulerWidth;
        }

        selectionEndPos = xAt(getSelectionEndTime());

        if (selectionEndPos < selectionBeginPos) {
            selectionEndPos = selectionBeginPos;
        }

        repaint();
    }

    /**
     * Updates the popup menu items.
     * @see mpi.eudico.client.annotator.viewer.AbstractViewer#updateLocale()
     */
    @Override
	public void updateLocale() {
        super.updateLocale();

        if (popup != null) {
            fitVerticalMI.setText(ElanLocale.getString(
                    "TimeSeriesViewer.TrackPanel.FitVertically"));
            detachItem.setText(ElanLocale.getString(
            	"Detachable.Detach"));
            addPanelMI.setText(ElanLocale.getString(
                    "TimeSeriesViewer.TrackPanel.AddPanel"));
            removePanelMI.setText(ElanLocale.getString(
                    "TimeSeriesViewer.TrackPanel.RemovePanel"));
            trPanelMenu.setText(ElanLocale.getString(
                    "TimeSeriesViewer.TrackPanel"));
            setRangeMenu.setText(ElanLocale.getString(
                    "TimeSeriesViewer.TrackPanel.SetRange"));
            addTrackMenu.setText(ElanLocale.getString(
                    "TimeSeriesViewer.Track.Add"));
            removeTrackMenu.setText(ElanLocale.getString(
                    "TimeSeriesViewer.Track.Remove"));
            extractDataMI.setText(ElanLocale.getString(
                    "TimeSeriesViewer.Extract"));
            configureTrMI.setText(ElanLocale.getString(
                    "TimeSeriesViewer.Tracks.Configure"));
            showTrackValueMenu.setText(ElanLocale.getString(
        		"TimeSeriesViewer.Track.ShowValues"));

            // $sidgrid
            addAllTrackMI.setText(ElanLocale.getString(
							 "TimeSeriesViewer.Track.Addall"));
            removeAllTrackMI.setText(ElanLocale.getString(
							    "TimeSeriesViewer.Track.Removeall"));
            // end $sidgrid
            combinedRangeMI.setText(ElanLocale.getString(
				"TimeSeriesViewer.TrackPanel.SetRangeCombined"));
            exportTrackMenu.setText(ElanLocale.getString(
            		"TimeSeriesViewer.ExportTrack"));
        }
    }

	/**
	 * Adjusts panel sizes, updates scroll bars, repaints.
     * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    @Override
	public void componentResized(ComponentEvent e) {
        TSTrackPanelImpl panel;
        intervalWidth = getWidth() - vertRulerWidth - defBarWidth;

        for (int i = 0; i < trackPanels.size(); i++) {
            panel = trackPanels.get(i);
            panel.setWidth(getWidth() - defBarWidth);
        }

        adjustPanelHeight();

        paintBuffer();

        scrollBar.setBounds(getWidth() - defBarWidth, 0, defBarWidth,
            getHeight());
        scrollBar.revalidate();
        updateScrollBar();
        storeLocation();
    }

    /**
     * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
     */
    @Override
	public void componentShown(ComponentEvent e) {
        paintBuffer();

        scrollBar.setBounds(getWidth() - defBarWidth, 0, defBarWidth,
            getHeight());
        scrollBar.revalidate();
        updateScrollBar();
    }

    /**
     * Handle scrolling of the viewer image.
     *
     * @param e the event
     */
    @Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
        int value = e.getValue();

        setVerticalScrollOffset(value);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == detachItem) {
    		if (isAttached()) {
    			setPreference("TimeSeriesViewer.Detached", Boolean.TRUE, transcription);
    			ELANCommandFactory.getLayoutManager(transcription).detach(this);
    			// detach
    			setAttached(false);
    			detachItem.setText(ElanLocale.getString("Detachable.Attach"));
    			setVerticalRulerWidth(43);//??
    			paintBuffer();
    		} else {
    			setPreference("TimeSeriesViewer.Detached", Boolean.FALSE, transcription);
    			ELANCommandFactory.getLayoutManager(transcription).attach(this);
    			//attach
    			setAttached(true);
    			detachItem.setText(ElanLocale.getString("Detachable.Detach"));
    		}
    		return;
    	}
        if (e.getSource() == fitVerticalMI) {
            autoFitVertical = fitVerticalMI.isSelected();
            adjustPanelHeight();
            paintBuffer();
            updateScrollBar();
            setPreference("TimeSeriesViewer.FitVertically", Boolean.valueOf(autoFitVertical),
            		transcription);
            return;
        }


        if (e.getSource() == showTrackValueMenu) {
        	showTrackValues = !showTrackValues;
        	showTrackValueMenu.setSelected(showTrackValues);
        	paintBuffer();
        	setPreference("TimeSeriesViewer.ShowTrackValues", Boolean.valueOf(showTrackValues),
    				transcription);
        	return;
        }

        if (e.getSource() == timeRulerVisMI) {
        	timeRulerVisible = timeRulerVisMI.isSelected();
        	if (timeRulerVisible) {
        		rulerHeight = ruler.getHeight();
        	} else {
        		rulerHeight = 0;
        	}
        	adjustPanelHeight();
        	paintBuffer();
        	setPreference("TimeSeriesViewer.TimeRulerVisible", Boolean.valueOf(timeRulerVisible),
    				transcription);
        	return;
        }

        if (e.getSource() == addPanelMI) {
            addEmptyTrackPanel();
            storePanelSetup();
            return;
        }

        if (e.getSource() == removePanelMI) {
            if (selTrackPanel != null) {
                removeTSTrackPanel(selTrackPanel);
                storePanelSetup();
            }

            return;
        }

        if (e.getSource() == configureTrMI) {
            if (trackManager != null) {
                trackManager.configureTracks(this);
            }

            return;
        }

        if (e.getSource() == extractDataMI) {
            extractTrackData();

            return;
        }

        //$sidgrid

        if (e.getSource() == addAllTrackMI) {
            if (selTrackPanel != null) {
            	List<TimeSeriesTrack> tracks = trackManager.getRegisteredTracks();
                AbstractTSTrack tr;

                for (int i = 0; i < tracks.size(); i++) {
                    tr = (AbstractTSTrack) tracks.get(i);
                    if (selTrackPanel.getTrack(tr.getName()) == null) {
                        selTrackPanel.addTrack(tr);
                        selTrackPanel.getRuler().setRange(tr.getRange());
                        selTrackPanel.getRuler().setUnitString(tr.getUnitString());
                    }
                }

                paintBuffer();
                storePanelSetup();
            }
            return;
        }

        if (e.getSource() == removeAllTrackMI) {
            if (selTrackPanel != null) {
                List<AbstractTSTrack> tracks = selTrackPanel.getTracks();

                for (int i = tracks.size() - 1; i >= 0; i--) {
                    selTrackPanel.removeTrack(tracks.get(i));
                }

                paintBuffer();
                storePanelSetup();
            }

            return;
        }


		if (e.getSource() == addPanelForEachTrackMI) {
		    clearPanel();
		    List<TimeSeriesTrack> trs = trackManager.getRegisteredTracks();
		    int totPanels = 0;
		    if (trackPanels.isEmpty()) {
		    	totPanels = trs.size();
		    }
	            // else
		    //  totPanels=trs.size()-trackPanels.size();

	            /*( for (int i = 0; i < totPanels; i++) {

	                 addEmptyTrackPanel();
			 }*/
		    for (int i = 0; i < totPanels; i++) {
		        addEmptyTrackPanel();
		        AbstractTSTrack    tra = (AbstractTSTrack) trs.get(i);
		        TSTrackPanelImpl tsp = trackPanels.get(i);
	            if((tra != null) && !tsp.getTracks().contains(tra)) {
	                tsp.addTrack(tra);
	                tsp.getRuler().setRange(tra.getRange());
	                tsp.getRuler().setUnitString(tra.getUnitString());
	                selTrackPanel=tsp;
	                paintBuffer();
	            }
		    }
		    storePanelSetup();
		    return;
	     }

		if (e.getSource() == removeAllPanelsMI) {
		    clearPanel();
		    storePanelSetup();
		    return;
		}

		// end $sidgrid

        String command = e.getActionCommand();

        if ((command != null) && (command.indexOf(ADD_PR) > -1)) {
            // add Track
            String trackName = command.substring(4);

            if ((trackManager != null) && (selTrackPanel != null)) {
                AbstractTSTrack tr = trackManager.getTrack(trackName);

                if (tr != null) {
                    selTrackPanel.addTrack(tr);
                    selTrackPanel.getRuler().setRange(tr.getRange());
                    selTrackPanel.getRuler().setUnitString(tr.getUnitString());
                    paintBuffer();
                }
                storePanelSetup();
            }

            return;
        } else if ((command != null) && (command.indexOf(REM_PR) > -1)) {
            // remove track
            String trackName = command.substring(4);

            if (selTrackPanel != null) {
                List<AbstractTSTrack> tracks = selTrackPanel.getTracks();
                AbstractTSTrack tr;

                for (int i = 0; i < tracks.size(); i++) {
                    tr = tracks.get(i);

                    if (trackName.equals(tr.getName())) {
                        selTrackPanel.removeTrack(tr);
                        paintBuffer();
                    }
                }
                storePanelSetup();
            }

            return;
        } else if ((command != null) && (command.indexOf(RANGE) > -1)) {
            // setRange and unit string
            String trackName = command.substring(4);

            if (selTrackPanel != null) {
                List<AbstractTSTrack> tracks = selTrackPanel.getTracks();
                AbstractTSTrack tr;
                boolean combined = trackName.equals("ALL");
                float[] allr = new float[]{Float.MAX_VALUE, Float.MIN_VALUE};

                for (int i = 0; i < tracks.size(); i++) {
                    tr = tracks.get(i);
                    if (combined) {
                    	if (tr.getRange()[0] < allr[0]) {
                    		allr[0] = tr.getRange()[0];
                    	}
                    	if (tr.getRange()[1] > allr[1]) {
                    		allr[1] = tr.getRange()[1];
                    	}
                    } else if (trackName.equals(tr.getName())) {
                        selTrackPanel.getRuler().setRange(tr.getRange());
                        selTrackPanel.getRuler()
                                     .setUnitString(tr.getUnitString());
                        paintBuffer();
                        break;
                    }
                }
                if (combined) {
                    selTrackPanel.getRuler().setRange(allr);
                    selTrackPanel.getRuler()
                                 .setUnitString("");
                    paintBuffer();
                }
            }

            return;
        } else if ((command != null) && (command.indexOf(EXP_PR) > -1)) {
        	String trackName = command.substring(EXP_PR.length());
        	
        	List<TimeSeriesTrack> trs = trackManager.getRegisteredTracks();
        	AbstractTSTrack tr;
        	for (int i = 0; i < trs.size(); i++) {
        		tr = (AbstractTSTrack) trs.get(i);
        		if (tr.getName().equals(trackName)) {
        			// create exporter for track
        			new ExportTrack().exportTrack(this, tr);
        			break;
        		}
        	}
        	return;
        }

        // if we get here the super implementation (zoom) will be called
        float oldMsPP = msPerPixel;
        super.actionPerformed(e);
        if (msPerPixel != oldMsPP) {
            setPreference("TimeSeriesViewer.ZoomLevel", Float.valueOf(100f * (10f / msPerPixel)),
            		transcription);
        }
    }

    /**
     * Scrolls horizontally or vertically or zooms in or out, depending on 
     * modifier keys.
     *
     * @param e the event
     */
    @Override
	public void mouseWheelMoved(MouseWheelEvent e) {
    	if (e.isControlDown()) {
    		super.mouseWheelMoved(e);
    	} else if (e.isShiftDown()) {// on Mac this is the same as hor. scroll with two fingers on the trackpad
	        super.mouseWheelMoved(e);
        }  else if (e.isAltDown()) {// page up/down
	        super.mouseWheelMoved(e);
        } else {
	        if (e.getUnitsToScroll() > 0) {
	            scrollBar.setValue(scrollBar.getValue() + 25);
	        } else {
	            scrollBar.setValue(scrollBar.getValue() - 25);
	        }
    	}
    }
    
    /**
	 * @see mpi.eudico.client.annotator.viewer.DefaultTimeScaleBasedViewer#preferencesChanged()
	 */
	@Override
	public void preferencesChanged() {
		Integer numPanels = Preferences.getInt("TimeSeriesViewer.NumberOfPanels",
				transcription);
		if (numPanels != null) {
			int np = numPanels.intValue();
			setNumberOfTrackPanels(np);
			// only read the tracknames per panel if the panels are created
			List<String> trackNames = null;
			for (int i = 1; i <= np; i++) {// 1 based
				trackNames = Preferences.getListOfString("TimeSeriesViewer.Panel-" + i,
						transcription);
				setTracksForPanel(i - 1, trackNames);
			}
		}
        Rectangle dialogBounds = Preferences.getRect("TimeSeriesViewer.Detached.Bounds",
                transcription);

        if (dialogBounds != null &&
        		SwingUtilities.windowForComponent(this) instanceof DetachedViewerFrame) {
        	SwingUtilities.windowForComponent(this).setBounds(dialogBounds);
        }

		Float zoomLevel = Preferences.getFloat("TimeSeriesViewer.ZoomLevel",
				transcription);
		if (zoomLevel != null) {
			float zl = zoomLevel.floatValue();
            float newMsPerPixel =  ((100f / zl) * 10);
            setMsPerPixel(newMsPerPixel);
			updateZoomPopup((int)zl);
		}

		Boolean fitVert = Preferences.getBool("TimeSeriesViewer.FitVertically",
				transcription);
		if (fitVert != null) {
			if (fitVerticalMI != null) {
				fitVerticalMI.setSelected(fitVert.booleanValue());
			}
			if (autoFitVertical != fitVert.booleanValue()) {
				autoFitVertical = fitVert.booleanValue();
	            adjustPanelHeight();
	            paintBuffer();
	            updateScrollBar();
			}
		}

		Boolean rulerVis = Preferences.getBool("TimeSeriesViewer.TimeRulerVisible",
				transcription);
		if (rulerVis != null) {
			timeRulerVisible = rulerVis.booleanValue();
			if (timeRulerVisMI != null) {
				timeRulerVisMI.setSelected(timeRulerVisible);
			}
			if (timeRulerVisible) {
				rulerHeight = ruler.getHeight();
			} else {
				rulerHeight = 0;
			}
		}
		
		Boolean showTV = Preferences.getBool("TimeSeriesViewer.ShowTrackValues", transcription);
		
		if (showTV != null) {
			showTrackValues = showTV.booleanValue();
			if (showTrackValueMenu != null) {
				showTrackValueMenu.setSelected(showTrackValues);
			}
		}
				
		super.preferencesChanged();

		paintBuffer();
	}

	/**
	 * Save the current setup of panels and tracks as preferences.
	 */
	private void storePanelSetup() {
		int np = getNumberOfTrackPanels();
		if (np > 0) {
			setPreference("TimeSeriesViewer.NumberOfPanels", Integer.valueOf(np),
					transcription);
			List<String> names = null;
			for (int i = 0; i < np; i++) {
				names = getTracksForPanel(i);
				if (names != null) {
					setPreference("TimeSeriesViewer.Panel-" + (i + 1), names,
							transcription);
				}
			}
		}
	}

	/**
	 * If the viewer is in a detached window, store the bounds of the window.
	 */
	private void storeLocation() {
		if (SwingUtilities.windowForComponent(this) instanceof DetachedViewerFrame) {
			setPreference("TimeSeriesViewer.Detached.Bounds",
					SwingUtilities.windowForComponent(this).getBounds(), transcription);
		}
	}
	//####################################################################################
    // testing
    private void testTrack() {
        //String fileName = "D:\\MPI\\ELAN docs\\dataglove\\onno.log";
        String fileName = System.getProperty("user.dir") + File.separator +
            "glove.log";
        mpi.eudico.client.annotator.timeseries.glove.DataGloveFileReader reader = new mpi.eudico.client.annotator.timeseries.glove.DataGloveFileReader(fileName);

        try {
            int sampleRate = reader.detectSampleFrequency();
            float[] data = (float[]) reader.readTrack(10, 5);
            mpi.eudico.client.annotator.timeseries.ContinuousRateTSTrack track = new mpi.eudico.client.annotator.timeseries.ContinuousRateTSTrack();
            track.setType(mpi.eudico.client.annotator.timeseries.TimeSeriesTrack.VALUES_FLOAT_ARRAY);
            track.setRange(new float[] { -180, 180 });
            track.setColor(new Color(128, 0, 128));
            track.setSampleRate(sampleRate);
            track.setData(data);

            float[] data2 = (float[]) reader.readTrack(14, 5);
            mpi.eudico.client.annotator.timeseries.ContinuousRateTSTrack track2 = new mpi.eudico.client.annotator.timeseries.ContinuousRateTSTrack();
            track2.setType(mpi.eudico.client.annotator.timeseries.TimeSeriesTrack.VALUES_FLOAT_ARRAY);
            track2.setRange(new float[] { -180, 180 });
            track2.setColor(new Color(0, 128, 0));
            track2.setSampleRate(sampleRate);
            track2.setData(data2);

            if (trackPanels.size() > 0) {
                TSTrackPanelImpl tsp = trackPanels.get(0);
                tsp.getRuler().setRange(track.getRange());
                tsp.addTrack(track);
                tsp.addTrack(track2);
            }
        } catch (IOException ioe) {
            System.out.println("Read error: " + ioe.getMessage());
        }
    }

    /**
     * Notification of changes in tracks and / or track sources (files).
     * Especially change and delete events should be handled here.
     *
     * @param event the event
     */
    @Override
	public void timeSeriesChanged(TimeSeriesChangeEvent event) {
    	if (event.getEditSourceType() == TimeSeriesChangeEvent.TRACK_AND_PANEL) {
    		if (event.getEditType() == TimeSeriesChangeEvent.ADD) {
    			AbstractTSTrack tr = (AbstractTSTrack) event.getSource();
            	if (trackPanels.size() == 1 && trackPanels.get(0).getTracks().size() == 0) {
            		// the first track
            		TSTrackPanelImpl trsp = trackPanels.get(0);
            		trsp.addTrack(tr);
            		trsp.getRuler().setRange(tr.getRange());
            		trsp.getRuler().setUnitString(tr.getUnitString());
                    paintBuffer();
                    storePanelSetup();
                    return;
            	} else {
	    	        TSTrackPanelImpl tsp = new TSTrackPanelImpl();
	    	        TSRulerImpl ruler = new TSRulerImpl();
	    	        if (Constants.DEFAULT_LF_LABEL_FONT != null) {
	    	        	ruler.setFont(Constants.deriveSmallFont(Constants.DEFAULT_LF_LABEL_FONT));
	    	        } else {
	    	        	ruler.setFont(Constants.SMALLFONT);
	    	        }
	    	        tsp.setRuler(ruler);
	    	        tsp.addTrack(tr);
            		tsp.getRuler().setRange(tr.getRange());
            		tsp.getRuler().setUnitString(tr.getUnitString());
	    	        addTSTrackPanel(tsp);
            	}
    		}
    	} else
    	if (event.getEditSourceType() == TimeSeriesChangeEvent.TRACK) {
            if (event.getSource() instanceof TSTrackConfiguration ||
                    event.getSource() instanceof AbstractTSTrack) {
                AbstractTSTrack tr = null;

                if (event.getSource() instanceof TSTrackConfiguration) {
                    TSTrackConfiguration trc = (TSTrackConfiguration) event.getSource();
                    tr = (AbstractTSTrack) trc.getObject(trc.getTrackName());
                } else {
                    tr = (AbstractTSTrack) event.getSource();
                }

                if (event.getEditType() == TimeSeriesChangeEvent.ADD) {
                	if (trackPanels.size() == 1 && trackPanels.get(0).getTracks().size() == 0) {
                		// the first track
                		TSTrackPanelImpl trsp = trackPanels.get(0);
                		trsp.addTrack(tr);
                		trsp.getRuler().setRange(tr.getRange());
                		trsp.getRuler().setUnitString(tr.getUnitString());
                        paintBuffer();
                        storePanelSetup();
                        return;
                	}
                }

                AbstractTSTrack tr2;

                // find the panel
                TSTrackPanelImpl pan;

                for (int i = 0; i < trackPanels.size(); i++) {
                    pan = trackPanels.get(i);

                    List<AbstractTSTrack> tracks = pan.getTracks();

                    for (int j = 0; j < tracks.size(); j++) {
                        tr2 = tracks.get(j);

                        if (tr2 == tr) {
                            if (event.getEditType() == TimeSeriesChangeEvent.CHANGE) {
                                // make it the active track ??
                                pan.getRuler().setUnitString(tr.getUnitString());
                                pan.getRuler().setRange(tr2.getRange());
                                paintBuffer();

                                break;
                            } else if (event.getEditType() == TimeSeriesChangeEvent.DELETE) {
                                // check
                                boolean active = false;

                                if ((pan.getRuler().getRange()[0] == tr.getRange()[0]) &&
                                        (pan.getRuler().getRange()[1] == tr.getRange()[1]) &&
                                        ((pan.getRuler().getUnitString() != null) &&
                                        pan.getRuler().getUnitString()
                                               .equals(tr.getUnitString()))) {
                                    // assume it is the active track
                                    active = true;
                                }

                                pan.removeTrack(tr2);

                                if (active && (pan.getTracks().size() > 0)) {
                                    pan.getRuler()
                                       .setRange(tracks.get(
                                            0).getRange());
                                    pan.getRuler()
                                       .setUnitString(tracks.get(
                                            0).getUnitString());
                                }

                                paintBuffer();

                                break;
                            }
                        }
                    }
                }
            }
        } else if (event.getEditSourceType() == TimeSeriesChangeEvent.TS_SOURCE) {
        	if (event.getSource() instanceof TSSourceConfiguration) {
        		TSSourceConfiguration configuration = (TSSourceConfiguration) event.getSource();

        		if (event.getEditType() == TimeSeriesChangeEvent.ADD) {
	        		Set<Object> keySet = configuration.objectKeySet();
	        		if (keySet.size() == 1) {
	        			Object val = configuration.getObject(keySet.iterator().next());
	        			if (val instanceof TSTrackConfiguration) {
	                        TSTrackConfiguration trc = (TSTrackConfiguration) val;
	                        AbstractTSTrack tr = (AbstractTSTrack) trc.getObject(trc.getTrackName());

	                    	if (trackPanels.size() == 1 && trackPanels.get(0).getTracks().size() == 0) {
	                    		// the first track
	                    		TSTrackPanelImpl trsp = trackPanels.get(0);
	                    		trsp.addTrack(tr);
	                    		trsp.getRuler().setRange(tr.getRange());
	                    		trsp.getRuler().setUnitString(tr.getUnitString());
	                            paintBuffer();
	                            storePanelSetup();
	                    	}
	        			}
	        		}
        		} else if (event.getEditType() == TimeSeriesChangeEvent.CHANGE) {
        			// e.g. offset
        			paintBuffer();
        		}
        	}
        }
    }

}
