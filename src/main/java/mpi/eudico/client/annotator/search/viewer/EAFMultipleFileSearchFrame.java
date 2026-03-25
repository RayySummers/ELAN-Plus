package mpi.eudico.client.annotator.search.viewer;

import javax.swing.ImageIcon;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.gui.ClosableFrame;
import mpi.search.SearchLocale;

/**
 * A frame for EAF multiple file search.
 */
@SuppressWarnings("serial")
public class EAFMultipleFileSearchFrame extends ClosableFrame {
	EAFMultipleFileSearchPanel searchPanel;	
	
	/**
	 * Constructor.
	 * 
	 * @param elanFrame the parent frame
	 */
	public EAFMultipleFileSearchFrame(ElanFrame2 elanFrame) {
		super(SearchLocale.getString("MultipleFileSearch.Title"));
        ImageIcon icon = new ImageIcon(this.getClass()
                .getResource("/mpi/eudico/client/annotator/resources/ELAN16.png"));

		if (icon != null) {
			setIconImage(icon.getImage());
		}
		
		searchPanel = new EAFMultipleFileSearchPanel(elanFrame);
		getContentPane().add(searchPanel);
		pack();
	}
	// moet vanuit Elan worden aangeroepen
	// GEEN NIEUWE ELANS creeeren vanuit result list maar alles binnen de
	// huidige runtime houden
	/**
	 * the main method
	 * @param args the args
	 */
	static public void main(String[] args) {
		new EAFMultipleFileSearchFrame(null);
	}
}
