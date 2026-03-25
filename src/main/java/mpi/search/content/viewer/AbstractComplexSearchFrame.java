package mpi.search.content.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import mpi.search.SearchLocale;

/**
 * A base class for complex search frames.
 */
@SuppressWarnings("serial")
public class AbstractComplexSearchFrame extends JFrame {
	/** the help action */
	private Action helpAction;
	/**
	 * search panel to add to the search frame
	 */
	protected final AbstractComplexSearchPanel searchPanel;
	
	
	/**
	 * Constructor that takes serachPanel as an argument and assigns it to the 
	 * search frame, creates a menu bar and creates a window close event.
	 * @param searchPanel the serach panel to assign to frame
	 */
	public AbstractComplexSearchFrame(AbstractComplexSearchPanel searchPanel){
        super(SearchLocale.getString("SearchDialog.Title"));
		this.searchPanel = searchPanel;
		getContentPane().add(searchPanel);
		createMenuBar();
	    addWindowListener(
        new WindowAdapter() {
            @Override
			public void windowClosing(WindowEvent event) {
                AbstractComplexSearchFrame.this.searchPanel.stopSearch();
            }
        });
	}

	private void createMenuBar() {
	       helpAction = new AbstractAction(SearchLocale.getString("Action.Help")) {
	            @Override
				public void actionPerformed(ActionEvent e) {
	                showInfoDialog();
	            }
	        };
	        helpAction.putValue(Action.SHORT_DESCRIPTION, SearchLocale
	                .getString("Action.Tooltip.Help"));
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(SearchLocale.getString("SearchDialog.File"));
        fileMenu.add(searchPanel.getCloseAction());
        menuBar.add(fileMenu);

        JMenu queryMenu = new JMenu(SearchLocale.getString("SearchDialog.Query"));
        queryMenu.add(searchPanel.getStartAction());
        queryMenu.add(searchPanel.zoomAction);
        queryMenu.addSeparator();
        queryMenu.add(searchPanel.saveAction);
        queryMenu.add(searchPanel.readAction);
        queryMenu.add(searchPanel.getExportAction());
        menuBar.add(queryMenu);

        JMenu helpMenu = new JMenu(SearchLocale.getString("SearchDialog.Help"));
        helpMenu.add(helpAction);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }
	
	/**
	 * Now protected, the implementation moved to ElanSearchFrame.
	 */
    protected void showInfoDialog() {
    	JOptionPane.showMessageDialog(this, "Unable to load and show the Search help file", 
    				"Warning", JOptionPane.WARNING_MESSAGE, null);    	
    }

}
