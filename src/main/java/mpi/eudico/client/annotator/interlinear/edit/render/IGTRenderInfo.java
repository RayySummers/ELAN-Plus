package mpi.eudico.client.annotator.interlinear.edit.render;

import java.awt.Point;

/**
 * Abstract class for storing rendering information.
 *
 * @author Han Sloetjes
 */
public abstract class IGTRenderInfo {
	/** the x coordinate */
	public int x;
	/** the y coordinate */
	public int y;
	/** the width of the area to render */
	public int width;
	/** the height of the area to render */
	public int height;

	/**
	 * Creates a new render info instance.
	 */
	public IGTRenderInfo() {
		super();
	}

	/**
	 * Returns whether a specific point is within the rendering area (rectangle) of
	 * this render info object. The test is performed including the boundaries.
	 *
	 * @param p the point to test
	 * @return true if the point is in the rectangular space of this rendering object.
	 */
	public boolean isPointInRenderArea(Point p) {
		if (p == null) {
			return false;
		}

		return p.x >= x && p.x <= x + width &&
				p.y >= y && p.y <= y + height;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		//buf.append("IGTRenderInfo:[");
		buf.append(" x=");
		buf.append(String.valueOf(x));
		buf.append(" y=");
		buf.append(String.valueOf(y));
		buf.append(" width=");
		buf.append(String.valueOf(width));
		buf.append(" height=");
		buf.append(String.valueOf(height));
		//buf.append("]");

		return buf.toString();
	}
}
