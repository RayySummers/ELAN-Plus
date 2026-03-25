package mpi.search.content.query.viewer;

import java.awt.Component;
import java.awt.event.*;
import javax.swing.JPopupMenu;

/**
 * Base class for popup menus for constraint panels.
 * 
 * @author Alexander Klassmann
 * @version Sep 28, 2004
 */
@SuppressWarnings("serial")
abstract public class AbstractPopupMenu extends JPopupMenu implements MouseListener, ActionListener{
	/** the source component */
	protected final Component component;
	/** the constraint panel to provide th context menu for */
	protected final AbstractConstraintPanel constraintPanel;
	
	/**
	 * Constructor to initialize the instance variables.
	 * 
	 * @param component the component instance
	 * @param constraintPanel the constraint panel instance
	 */
	public AbstractPopupMenu(Component component, AbstractConstraintPanel constraintPanel){
		this.component = component;
		this.constraintPanel = constraintPanel;
		component.addMouseListener(this);
		fillMenu();
	}
	
	/** Populate the menu. */
	abstract protected void fillMenu();
}
