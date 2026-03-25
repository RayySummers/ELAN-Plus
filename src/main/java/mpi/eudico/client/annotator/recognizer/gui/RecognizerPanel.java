package mpi.eudico.client.annotator.recognizer.gui;

import mpi.eudico.client.annotator.ViewerManager2;


/** 
 * Concrete implementation of AbstractRecognizerPanel, doesn't add 
 * functionality (yet).
 */
@SuppressWarnings("serial")
public class RecognizerPanel extends AbstractRecognizerPanel  {
	
	/**
	 * Calls the super constructor which checks the available audio files and
	 * updates the selection panel if needed.
	 * 
	 * @param viewerManager the manager providing access to the media files
	 */
	public RecognizerPanel(ViewerManager2 viewerManager) {
		super(viewerManager);	
	}
}
