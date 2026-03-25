package mpi.eudico.client.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;

/**
 * A tree cell editor based on a JCheckBox.
 * 
 * @see CheckboxTreeCellRenderer
 */
@SuppressWarnings("serial")
public class CheckboxTreeCellEditor extends JCheckBox implements
        TreeCellEditor, ItemListener {
    //  Colors
    /** Color to use for the foreground for selected nodes. */
    protected Color textSelectionColor;

    /** Color to use for the foreground for non-selected nodes. */
    protected Color textNonSelectionColor;

    /** Color to use for the background when a node is selected. */
    protected Color backgroundSelectionColor;

    /** Color to use for the background when the node isn't selected. */
    protected Color backgroundNonSelectionColor;
    
    private Object uObject;
    private boolean edited = false;
    private JTree tree;
    
    /**
     * Creates a new CheckboxTreeCellEditor instance.
     */
    public CheckboxTreeCellEditor() {
        super();
        addItemListener(this);
        initColors();
    }

    private void initColors() {
        setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
        setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
        setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
        setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
	}
   
    /**
     * Checks the type of the value, sets the text and foreground and 
     * background color and returns this JCheckBox.
     */
    @Override
	public Component getTreeCellEditorComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded, boolean leaf, int row) {
        this.tree = tree;

        if (value instanceof DefaultMutableTreeNode) {
            uObject = ((DefaultMutableTreeNode)value).getUserObject();
            if (uObject instanceof String) {
                setText(uObject.toString());
                setSelected(false);
            } else if (uObject instanceof SelectableObject) {
                setText(uObject.toString());
                setSelected(((SelectableObject<?>)uObject).isSelected());
            }
        } else if (value instanceof String) {
            setText((String) value);
            setSelected(false);
        } 
        edited = false;
        
        if (isSelected) {
            setForeground(getTextSelectionColor());
            setBackground(getBackgroundSelectionColor());
        } else {
            setForeground(getTextNonSelectionColor());
            setBackground(getBackgroundNonSelectionColor());
        }

        setComponentOrientation(tree.getComponentOrientation());
        return this;
    }

	/**
	 * Sets the color for the text in a node that is selected.
	 * 
	 * @param newColor the new text color
	 */
	public void setTextSelectionColor(Color newColor) {
		textSelectionColor = newColor;
	}

	/**
	 * Returns the color of the text in a selected node.
	 * 
	 * @return the text color in a selected node
	 */
	public Color getTextSelectionColor() {
		return textSelectionColor;
	}

	/**
	 * Sets the color for the text in a node that isn't selected.
	 * 
	 * @param newColor the new color for the text of a node that is not selected
	 */
	public void setTextNonSelectionColor(Color newColor) {
		textNonSelectionColor = newColor;
	}

	/**
	 * Returns the color of the text in a node that isn't selected.
	 * 
	 * @return the text color in a node that is not selected
	 */
	public Color getTextNonSelectionColor() {
		return textNonSelectionColor;
	}

	/**
	 * Sets the color to use for the background of a selected node.
	 * 
	 * @param newColor the new background selection color
	 */
	public void setBackgroundSelectionColor(Color newColor) {
		backgroundSelectionColor = newColor;
	}

	/**
	 * Returns the color of the background of a selected node.
	 * 
	 * @return the background color of a selected node
	 */
	public Color getBackgroundSelectionColor() {
		return backgroundSelectionColor;
	}

	/**
	 * Sets the background color to be used for a node that is not selected.
	 * 
	 * @param newColor the new background color for a node that is not selected
	 */
	public void setBackgroundNonSelectionColor(Color newColor) {
		backgroundNonSelectionColor = newColor;
	}

	/**
	 * Returns the background color to be used for a node that is not selected.
	 * 
	 * @return the background color of a node that is not selected
	 */
	public Color getBackgroundNonSelectionColor() {
		return backgroundNonSelectionColor;
	}

    /**
     * Returns the last object passed to the getTreeCellEditorComponent method.
     * Can be {@code null}.
     */
    @Override
	public Object getCellEditorValue() {
        return uObject;
    }

    /**
     * Returns {@code true} for now; could check the kind of object in the 
     * selected treepath. 
     */
    @Override
	public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    /**
     * Returns {@code true}.
     */
    @Override
	public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    /**
     * Returns {@code true}; always accept.
     */
    @Override
	public boolean stopCellEditing() {
        return true;
    }

    /**
     *
     */
    @Override
	public void cancelCellEditing() {
    }

    /**
     * 
     */
    @Override
	public void addCellEditorListener(CellEditorListener l) {
    }

    /**
     * 
     */
    @Override
	public void removeCellEditorListener(CellEditorListener l) {
    }

    /**
     * Updates the current value with the selected state of the checkbox.
     *
     */
    @Override
	public void itemStateChanged(ItemEvent e) {
        if (uObject instanceof SelectableObject && !edited) {
            //((SelectableString)uo).setSelected( !((SelectableString)uo).isSelected());
            ((SelectableObject<?>)uObject).setSelected(isSelected());
            edited = true;
        }
        if (tree != null) {
            tree.stopEditing();
        }
    }

}
