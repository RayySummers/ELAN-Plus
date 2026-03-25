package mpi.search.content.query.viewer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import mpi.search.content.model.CorpusType;
import mpi.search.content.query.model.AnchorConstraint;
import mpi.search.content.query.model.Constraint;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.content.query.model.DependentConstraint;
import mpi.search.content.query.model.RestrictedAnchorConstraint;
import mpi.search.content.result.model.ContentResult;


/**
 * The SearchConfigPanel is the GUI for defining a complete Query. 
 * It is composed of one or more Constraint-Panels 
 * @author klasal
 */
@SuppressWarnings("serial")
public class QueryPanel extends JPanel {
    /** the corpus type the query is built for */
    protected final CorpusType type;
    /** the tree model */
    protected final DefaultTreeModel treeModel;
    /** the tree for hierarchical constraints */
    protected JTree jTree;

    /**
     * Creates a new QueryPanel instance
     *
     * @param type the corpus type
     * @param startAction the start action to perform
     */
    public QueryPanel(final CorpusType type, final Action startAction) {
        this.type = type;
        treeModel = new DefaultTreeModel(new AnchorConstraint());
        createTree(startAction);
    }

    /**
     * Create tree in separate method for easier subclassing.
     *  
     * @param startAction the start action
     */
    protected void createTree(final Action startAction) {       
        jTree = new JTree(treeModel) {
                    @Override
					public boolean isPathEditable(TreePath path) {
                        return ((Constraint) path.getLastPathComponent()).isEditable();
                    }
                };

        jTree.setEditable(true);
        jTree.setCellRenderer(new ConstraintRenderer());
        jTree.setCellEditor(new ConstraintEditor(treeModel, type, startAction));

        //hack to kill mouse event (otherwise they would activate subcomponents of ConstraintPanel)
        jTree.setUI(new BasicTreeUI() {
                @Override
				protected boolean startEditing(TreePath path, MouseEvent event) {
                    return super.startEditing(path, null);
                }
            });

        //explicitly overwriting default height defined by Mac
        jTree.setRowHeight(0);

        jTree.setBorder(new EmptyBorder(5, 5, 5, 5));
        jTree.setOpaque(false);

        setFont(getFont().deriveFont(Font.PLAIN));
        setLayout(new BorderLayout());
        add(jTree, BorderLayout.CENTER);

        jTree.startEditingAtPath(jTree.getPathForRow(0));

        treeModel.addTreeModelListener(new TreeModelListener() {
            @Override
			public void treeNodesInserted(final TreeModelEvent e) {
                try {
                	//editing has to start after JTree has updated itself. Otherwise one gets a bad layout. 
                    javax.swing.SwingUtilities.invokeLater(new java.lang.Runnable() {
                            @Override
							public void run() {
                                jTree.startEditingAtPath(e.getTreePath()
                                                          .pathByAddingChild(e.getChildren()[0]));
                            }
                        });
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }

            @Override
			public void treeNodesChanged(TreeModelEvent e) {
            }

            @Override
			public void treeStructureChanged(TreeModelEvent e) {
            }

            @Override
			public void treeNodesRemoved(TreeModelEvent e) {
                jTree.startEditingAtPath(e.getTreePath());
            }
        });	
    }
    
    /**
     * Configure the panel with a previously defined Query.
     *
     * @param query the query to load
     */
    public void setQuery(ContentQuery query) {
        jTree.stopEditing();
        treeModel.setRoot(cloneTree(query.getAnchorConstraint()));

        for (int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
        }

        jTree.revalidate();
        jTree.repaint();
    }

    /**
     * Returns the content query.
     * 
     * @return the content query as configured by the user
     */
    public ContentQuery getQuery() {
        jTree.stopEditing();

        return new ContentQuery((AnchorConstraint) cloneTree(
                (AnchorConstraint) treeModel.getRoot()), type);
    }

    /**
     * Initiates a search on earlier results
     *
     * @param result the results to narrow down
     * @param comment a comment for the query
     */
    public void newRestrictedQuery(ContentResult result, String comment) {
        if (result.getRealSize() == 0) {
            return;
        }

        jTree.stopEditing();

        RestrictedAnchorConstraint ac = new RestrictedAnchorConstraint(result,
                comment);
        ac.insert(new DependentConstraint(ac.getTierNames()), 0);

        treeModel.setRoot(ac);
        jTree.startEditingAtPath(jTree.getPathForRow(1));
    }

    /**
     * Stops editing and resets the tree.
     */
    public void reset() {
        jTree.stopEditing();
        treeModel.setRoot(new AnchorConstraint());
        jTree.startEditingAtPath(jTree.getPathForRow(0));
    }

	private Constraint cloneTree(Constraint node) {
        Constraint newNode = (Constraint) node.clone();

        for (int i = 0; i < node.getChildCount(); i++) {
            newNode.insert(cloneTree(node.getChildAt(i)), i);
        }

        return newNode;
    }
}
