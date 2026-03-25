package mpi.eudico.client.annotator;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;


/**
 * Class which makes up a media player control slider.
 * Used for setting media time and making selections.
 */
@SuppressWarnings("serial")
public class MediaPlayerControlSlider extends AbstractViewer {
    private static Dimension SLIDERDIMENSION = new Dimension(600, 22);
    private int x1; //value of slider is x3
                    //x1 is left coordinate of horizontal lines
                    //x2 is right coordinate of horizontal lines
                    //x3 is x-coordinate of vertical line
    private int x2; //value of slider is x3
                    //x1 is left coordinate of horizontal lines
                    //x2 is right coordinate of horizontal lines
                    //x3 is x-coordinate of vertical line
    private int x3; //value of slider is x3
                    //x1 is left coordinate of horizontal lines
                    //x2 is right coordinate of horizontal lines
                    //x3 is x-coordinate of vertical line
    private int y1 = 2; //y1 is y-coordinate of highest horizontal line
    private int y2 = 20; //y2 is y-coordinate of lowest horizontal line
    private boolean bDraggedInMediaSlider = false;
    private int dragStart = -1;
    private int dragEnd = -1;
    private float sliderValue;
    //private boolean paintHighlight = false;

    /**
     * Constructor.
     */
    MediaPlayerControlSlider() {
        super();
        setPreferredSize(SLIDERDIMENSION);

        //setBorder(BorderFactory.createLineBorder(Color.black));
        setValue(0);

        addMouseListener(new MediaPlayerControlSliderMouseListener());
        addMouseMotionListener(new MediaPlayerControlSliderMouseMotionListener());
    }

    //called from ElanFrame when it is resized
    //   public static void setSliderDimension(int width)
    //   {
    //       SLIDERDIMENSION = new Dimension(width - 20, 30);
    //   }
    /**
     * Sets the flag indicating a drag action in the media slider.
     * 
     * @param b if {@code true} the media slider was dragged, if {@code false} 
     * it wasn't (and an update of the media time had a different source
     */
    public void setBDraggedInMediaSlider(boolean b) {
        bDraggedInMediaSlider = b;
    }

    /**
     * Returns whether the media slider was dragged.
     *
     * @return the media slider dragged flag
     */
    public boolean getBDraggedInMediaSlider() {
        return bDraggedInMediaSlider;
    }

    /**
     * Sets the location (x coordinate) of the start of a drag action.
     *
     * @param dragStart the x coordinate of the drag start location
     */
    public void setDragStart(int dragStart) {
        this.dragStart = dragStart;
    }

    /**
     * Returns the x coordinate of the start of a drag action.
     *
     * @return the x coordinate of the drag start
     */
    public int getDragStart() {
        return dragStart;
    }

    /**
     * Sets the x coordinate of the location where a drag action ended. 
     *
     * @param dragEnd the x coordinate of a drag end
     */
    public void setDragEnd(int dragEnd) {
        this.dragEnd = dragEnd;
    }

    /**
     * Returns the x coordinate of the end of a drag action.
     *
     * @return the x coordinate of the drag end
     */
    public int getDragEnd() {
        return dragEnd;
    }

    /**
     * Sets media time when clicked on the slider.
     * The x coordinate is converted to the corresponding media time.
     *
     * @param xcoord the location of the mouse click
     */
    public void updateMediaTime(int xcoord) {
        long lngTime = (long) ((getMediaDuration() * xcoord) / getWidth());

        if (lngTime > getMediaDuration()) {
            lngTime = getMediaDuration();
        } else if (lngTime < 0) {
            lngTime = 0;
        }

        setMediaTime(lngTime);
    }

    /**
     * Sets the value of the slider.
     *
     * @param value_in The value the slider should be set at
     */
    public void setValue(float value_in) {
        sliderValue = value_in;

        //      float f = value_in * (getWidth() - 1);
        //      int value = Math.round(f);
        //      if (value < 0) { value = 0; }
        //      if (value > getWidth() - 1) { value = getWidth() - 1; }
        //      x3 = value;
        //      x1 = x3 - 5;
        //      x2 = x3 + 5;
        //      if (x2 > getWidth() - 1) { x2 = getWidth() - 1; }
        repaint();
    }

    /**
     * Handles the painting of the slider.
     * Consists of a horizontal bar, a crosshair and maybe a selection.
     *
     * @param g the graphics context
     */
    @Override
	public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //g2.setColor(Constants.MEDIAPLAYERCONTROLSLIDERSELECTIONCOLOR);
        
//        if (paintHighlight) {
//        	g2.setColor(Color.WHITE);
//        	g2.fillRect(1, 0, getWidth() - 2, 12);
//        }
        g2.setColor(Constants.MEDIAPLAYERCONTROLSLIDERSELECTIONCOLOR);
        //		g2.draw3DRect(0, 15, SLIDERDIMENSION.width, 4, false);
        g2.draw3DRect(0, 7, getWidth(), 4, false);

        //painting of selection in slider
        int selectedBeginTime = (int) getSelectionBeginTime();
        int selectedEndTime = (int) getSelectionEndTime();
        int duration = (int) getMediaDuration();

        if (duration == 0) {
            return;
        }

        if (selectedBeginTime != selectedEndTime) {
            //			int begin = (int)((SLIDERDIMENSION.width * selectedBeginTime) / duration);
            //			int end   = (int)((SLIDERDIMENSION.width * selectedEndTime) / duration);
            int begin = (int) ((getWidth() * selectedBeginTime) / duration);
            int end = (int) ((getWidth() * selectedEndTime) / duration);

            g2.fillRect(begin, 7, end - begin, 4);

            //blue rectangle outside of selection
            //g2.setColor(Color.blue.darker());
            //g2.drawRect(begin - 1, 15 - 1, end - begin + 1, 4 + 1);
        }

        //painting of crosshair
        float f = sliderValue * (getWidth() - 1);
        int value = (int) Math.floor(f);

        if (value < 0) {
            value = 0;
        }

        if (value > (getWidth() - 1)) {
            value = getWidth() - 1;
        }

        x3 = value;
        x1 = x3 - 5;
        x2 = x3 + 5;

        if (x2 > (getWidth() - 1)) {
            x2 = getWidth() - 1;
        }

        g2.setColor(Constants.MEDIAPLAYERCONTROLSLIDERCROSSHAIRCOLOR);
        g2.drawLine(x1, y1, x2, y1); //highest horizontal line
        g2.drawLine(x1, y2, x2, y2); //lowest horizontal line
        g2.drawLine(x3, y1, x3, y2); //vertical line
    }

    /**
     * Empty.
     */
    @Override
	public void updateLocale() {
    }

    /**
     * Empty.
     */
    @Override
	public void updateActiveAnnotation() {
    }

    /**
     * Empty.
     */
    @Override
	public void updateSelection() {
        repaint();
    }

    /**
     * Notification of a media controller event, (re)calculates the playhead
     * location and repaints. 
     *
     * @param event the controller event
     */
    @Override
	public void controllerUpdate(ControllerEvent event) {
        if (event instanceof TimeEvent || event instanceof StopEvent) {
            //Dimension d = getPreferredSize();
            float position = (getMediaTime() * 1.0f) / (getMediaDuration() * 1.0f);
            setValue(position);
        }
    }

	@Override
	public void preferencesChanged() {		
	}
	
    /**
     * Used for handling mouse events on the slider.
     */
    private class MediaPlayerControlSliderMouseListener extends MouseAdapter {
        /**
         * Creates a new MediaPlayerControlSliderMouseListener instance.
         */
        MediaPlayerControlSliderMouseListener() {
        }

        /**
         * Sets the media time and stores the drag start location.
         *
         * @param e the event
         */
        @Override
		public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
            	return;
            }
            int intX = e.getX();

            updateMediaTime(intX);
            setDragStart(intX);
        }

        /**
         * Ends the drag action and resets dragging related fields.
         *
         * @param e the event
         */
        @Override
		public void mouseReleased(MouseEvent e) {
            setBDraggedInMediaSlider(false);
            setDragStart(-1);
            setDragEnd(-1);
        }
//
//		@Override
//		public void mouseEntered(MouseEvent e) {
//			paintHighlight = true;
//			repaint();        		
//		}
//
//		@Override
//		public void mouseExited(MouseEvent e) {
//			paintHighlight = false;
//			repaint();
//		}
        
    }
     //end of MediaPlayerControlSliderMouseListener

    /**
     * Used for handling drag mouse events on the slider.
     */
    private class MediaPlayerControlSliderMouseMotionListener
        extends MouseMotionAdapter {
        /**
         * Updates the drag end field while dragging the slider. 
         * Sets the media time and the selection.
         * 
         * @param e the event
         */
        @Override
		public void mouseDragged(MouseEvent e) {
            if (!e.isShiftDown()) {
                updateMediaTime(e.getX());

                return;
            }

            setBDraggedInMediaSlider(true);
            setDragEnd(e.getX());

            int duration = (int) getMediaDuration();
            long lngBeginSelectionTime = (duration * getDragStart()) / getWidth();
            long lngEndSelectionTime = (duration * getDragEnd()) / getWidth();

            //used when dragging the mouse from right to left
            if (lngBeginSelectionTime > lngEndSelectionTime) {
                long temp = lngBeginSelectionTime;
                lngBeginSelectionTime = lngEndSelectionTime;
                lngEndSelectionTime = temp;
            }

            setSelection(lngBeginSelectionTime, lngEndSelectionTime);
            updateMediaTime(getDragEnd());
        }
    }
     //end of MediaPlayerControlSliderMouseMotionListener


}
 //end of MediaPlayerControlSlider
