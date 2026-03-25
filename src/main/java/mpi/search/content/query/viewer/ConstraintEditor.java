package mpi.search.content.query.viewer;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;

import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.DependentConstraint;


/**
 * Class for the user interface of a constraint editor as a tree cell editor.
 * 
 * @author klasal
 */
@SuppressWarnings("serial")
public class ConstraintEditor extends AbstractCellEditor
    implements TreeCellEditor {
    /** encapsulated constraint panel */
	protected AbstractConstraintPanel constraintPanel;
	/** the start action */
    protected final Action startAction;
    /** the corpus type */
    protected final CorpusType type;
    /** the model of the tree  */
    protected final DefaultTreeModel treeModel;

    /**
     * Creates a new ConstraintEditor object.
     *
     * @param treeModel the tree data model
     * @param type the corpus type
     * @param startAction the action to perform
     */
    public ConstraintEditor(DefaultTreeModel treeModel, CorpusType type,
        Action startAction) {
        this.treeModel = treeModel;
        this.type = type;
        this.startAction = startAction;
    }

    /**
     *
     * @return the editor value
     */
    @Override
	public Object getCellEditorValue() {
        return constraintPanel.getConstraint();
    }

    /**
     * Configures the tree cell editor, depending on corpus type.
     *
     * @param tree the tree
     * @param value the constraint to render and edit
     * @param selected whether the cell is selected
     * @param expanded whether the node is expanded
     * @param leaf whether the node is a leaf node
     * @param row the tree row
     *
     * @return configured tree cell editor
     */
    @Override
	public Component getTreeCellEditorComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row) {
    	
        if (value instanceof AnchorConstraint) {
            constraintPanel = new AnchorConstraintPanel((AnchorConstraint) value,
                    treeModel, type, startAction);
        } else if (value instanceof DependentConstraint) {
            constraintPanel = new DependentConstraintPanel((DependentConstraint) value,
                    treeModel, type, startAction);
        }

        return constraintPanel;
    }
}
