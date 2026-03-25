package mpi.eudico.client.annotator.interlinear.edit;

import javax.swing.tree.DefaultMutableTreeNode;
import mpi.eudico.client.annotator.interlinear.AnnotationBlockCreator;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
 * A class containing height, width collapse information for block layout
 *
 */
public class BlockLayout {
	private AbstractAnnotation aa;
	private int width;
	private int height;
	private int collapsedHeight;

	private boolean collapsed;
	private DefaultMutableTreeNode node;

	/**
	 * Constructor
	 * @param aa the annotation
	 */
	public BlockLayout(AbstractAnnotation aa) {
		super();
		this.aa = aa;
		collapsed = true;
	}

	/**
	 * Returns the width
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the width
	 * @param width the width parameter
	 */
	public void setWidth(int width) {
		this.width = width;
		AnnotationBlockCreator creator = new AnnotationBlockCreator();
		node = creator.createBlockForAnnotation(aa, null);
		// hier... wait for call to a "calculate" method that accepts a Graphics object as the context
	}

	/**
	 * Returns the height
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the collapsed height
	 * @return the collapsed height
	 */
	public int getCollapsedHeight() {
		return collapsedHeight;
	}


	/**
	 * Returns isCollpased boolean
	 * @return collapsed the collapsed boolean variable
	 */
	public boolean isCollapsed() {
		return collapsed;
	}

	/**
	 * Sets collapsed boolean
	 * @param collapsed the boolean param
	 */
	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}


}
