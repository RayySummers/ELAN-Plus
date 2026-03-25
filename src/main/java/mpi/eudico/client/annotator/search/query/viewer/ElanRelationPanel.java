package mpi.eudico.client.annotator.search.query.viewer;

import mpi.search.content.model.CorpusType;

import mpi.search.content.query.model.Constraint;

import mpi.search.content.query.viewer.RelationPanel;

import java.awt.Font;


/**
 * Subclass with extra font support.
 * 
 * @author HS
 * @version Aug 2008
  */
@SuppressWarnings("serial")
public class ElanRelationPanel extends RelationPanel {
    /**
     * Creates a new ElanRelationPanel instance.
     *
     * @param type the corpus type
     * @param constraint the constraint
     * @param prefFont the preferred font (for some UI elements)
     */
    public ElanRelationPanel(CorpusType type, Constraint constraint,
        Font prefFont) {
        super(type, constraint);

        if (prefFont != null) {
            getUnitComboBox()
                .setFont(prefFont.deriveFont(getUnitComboBox().getFont()
                                                 .getStyle(),
                    getUnitComboBox().getFont().getSize()));
        }
    }
}
