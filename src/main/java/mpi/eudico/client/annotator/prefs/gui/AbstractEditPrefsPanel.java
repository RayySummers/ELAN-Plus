package mpi.eudico.client.annotator.prefs.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.Constants;

/**
 * Abstract class which sets a common user interface for different panels in 
 * the edit preferences dialog.
 * 
 * @author aarsom
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractEditPrefsPanel extends JPanel {

	/** scroll panel declaration */
	private JScrollPane scrollPane;
	/** outer panel declaration*/
	protected JPanel outerPanel;
	
	/** global inset initialization */
	protected Insets globalInset = new Insets(2, 6, 2, 6);
	/** global panel inset initialization */
	protected Insets globalPanelInset = new Insets(6, 15, 2, 6);
	/** single tab inset initialization */
	protected Insets singleTabInset = new Insets(2,34,2,0);
	/** double tab inset initialization */
	protected Insets doubleTabInset = new Insets(2,60,2,0);
	
	/** top inset initialization */
	protected Insets topInset = new Insets(4,0,0,0);
	/** left inset initialization */
	protected Insets leftInset = new Insets(0,6,0,0);
	/** cat inset initialization*/
	protected Insets catInset = new Insets(15,2,5,2); 
	/** panel inset initialization*/
	protected Insets catPanelInset = new Insets(2, 15, 2, 6);	
	/** small inset initialization*/
	protected Insets smallCatInset = new Insets(5,2,2,2); 
	
	/**
	 * Constructor.
	 */
	public AbstractEditPrefsPanel(){
		super();			
		initComponents("");
	}
	
	/**
	 * Constructor.
	 * 
	 * @param title title for the panel
	 */
	public AbstractEditPrefsPanel(String title){
		super();			
		initComponents(title);
	}
    
	/**
	 * Initialize basic components.
	 * 
	 * @param title title for the panel
	 */
	private void initComponents(String title){
		outerPanel = new JPanel(new GridBagLayout());
		
    	scrollPane = new JScrollPane(outerPanel);
        scrollPane.setBorder(new TitledBorder(title));   
        scrollPane.setBackground(outerPanel.getBackground());  
        scrollPane.getVerticalScrollBar().setUnitIncrement(
        		Constants.DEFAULT_LF_LABEL_FONT.getSize() + 4);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(
        		Constants.DEFAULT_LF_LABEL_FONT.getSize() + 4);
        
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 1.0;   
        gbc.weightx = 1.0;  
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(scrollPane , gbc);
    }
	
	/**
	 * Set the title for the panel.
	 * 
	 * @param title the title for the panel
	 */
	protected void setTitle(String title){
		((TitledBorder)scrollPane.getBorder()).setTitle(title);
	}
}
