package mpi.eudico.client.util;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Functions like a JButton, but extends JEditorPane and looks like a link
 * in a HTML-page.
 * 
 * @author Alexander Klassmann
 * @version Oct 26, 2004
 */
@SuppressWarnings("serial")
public class LinkButton extends JEditorPane {
	private String actionName;
	private boolean enabled = true;

	/**
	 * Creates a new LinkButton instance.
	 * 
	 * @param action the action to perform
	 */
	public LinkButton(final Action action) {
		setContentType("text/html");
		setEditable(false);
		actionName = (String) action.getValue(Action.NAME);

		addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (enabled)
						action.actionPerformed(new ActionEvent(this, 0, e.getDescription()));
				}
			}
		});

		action.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if ("enabled".equals(e.getPropertyName()))
					setEnabled(action.isEnabled());
			}
		});
		
		setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
		// on masOS, JRE 11 the button sometimes has a width or height of 0 or 1
		// setting a border seems to help
		setBorder(new EmptyBorder(1, 1, 1, 1));
		reset();
	}

	/**
	 * Sets the label text.
	 * 
	 * @param label the text to show
	 */
	public void setLabel(String label){
		actionName = label != null ? label : "";
		reset();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			reset();
		}
	}

	/**
	 * Sets the HTML text of the editor pane.
	 */
	private void reset() {
		if (enabled)
			setText(
				"<a href=\"" + actionName + "\"><font size=\"3\">" + actionName + "</font></a>");
		else
			setText("<font size=\"3\">" + actionName + "</font>");
	}
	
	//	public Dimension getPreferredSize() {
	//		return new Dimension(super.getPreferredSize().width, super.getPreferredSize().height - 2);
	//	}

}
