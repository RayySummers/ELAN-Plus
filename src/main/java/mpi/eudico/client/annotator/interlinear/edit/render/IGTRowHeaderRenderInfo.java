package mpi.eudico.client.annotator.interlinear.edit.render;

import mpi.eudico.client.annotator.interlinear.edit.IGTConstants;

/**
 * Rendering information for row headers (the tier names).
 */
public class IGTRowHeaderRenderInfo extends IGTRenderInfo {
	/** the left margin */
	public int leftMargin = IGTConstants.TEXT_MARGIN_LEFT;
	/** the right margin */
	public int rightMargin = IGTConstants.TEXT_MARGIN_RIGHT;

	/**
	 * Constructor.
	 */
	public IGTRowHeaderRenderInfo() {
		super();
	}

	/**
	 * Returns the sum of the left and right margins.
	 *
	 * @return the horizontalMargins, the sum of the left margin and right margin.
	 */
	public int getHorizontalMargins() {
		return leftMargin + rightMargin;
	}

}
