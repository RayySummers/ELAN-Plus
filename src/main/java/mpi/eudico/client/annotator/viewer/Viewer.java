package mpi.eudico.client.annotator.viewer;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Defines a data viewer component managed by a {@code ViewerManager2}.
 */
public interface Viewer {
    // viewer manager
	/**
	 * Sets the viewer manager this viewer is connected to.
	 * 
	 * @param viewerManager the viewer manager
	 */
    public void setViewerManager(ViewerManager2 viewerManager);

    /**
     * Returns the viewer manager this viewer is connected to.
     *
     * @return the viewer manager
     */
    public ViewerManager2 getViewerManager();
}