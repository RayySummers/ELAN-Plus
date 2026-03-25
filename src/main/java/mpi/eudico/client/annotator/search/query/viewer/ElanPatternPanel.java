package mpi.eudico.client.annotator.search.query.viewer;

import mpi.search.content.model.CorpusType;

import mpi.search.content.query.viewer.PatternPanel;

import java.awt.Font;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.tree.TreeNode;


/**
 * ELAN subclass with additional font support.
 * 
 * @author HS
 * @version Aug 2008
  */
@SuppressWarnings("serial")
public class ElanPatternPanel extends PatternPanel {
    /**
     * Creates a new ElanPatternPanel instance and applies a default
     * ELAN font to relevant UI elements.
     *
     * @param type the corpus type
     * @param tierComboBox a box with tier names
     * @param node the tier tree node 
     * @param startAction the start action
     * @param prefFont the preferred font to use for the UI
     */
    public ElanPatternPanel(CorpusType type, JComboBox<String> tierComboBox,
        TreeNode node, Action startAction,  Font prefFont) {
        super(type, tierComboBox, node, startAction);

        if (prefFont != null) {
            textField.setFont(prefFont.deriveFont(Font.BOLD));
        }
    }

	@Override
	public void grabFocus() {
		textField.grabFocus();
	}
    
}
