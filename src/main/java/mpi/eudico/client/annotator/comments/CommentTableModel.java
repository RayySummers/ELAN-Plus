package mpi.eudico.client.annotator.comments;

import javax.swing.table.TableModel;
import java.util.List;

/**
 * An interface for tables with comments. Used to that the comment table can be extended with extra columns by creating a new
 * model, with the same interface, for that.
 *
 * @author olasei
 */
public interface CommentTableModel extends TableModel {

    /**
     * Tell the Model about the List which contains the comments. It does not make a copy of all comments (or references to
     * comments), the model will work on the main list itself (or likely a read-only instance of it).
     *
     * @param comments the list of comments, the data for the model
     */
    void setComments(List<CommentEnvelope> comments);

    /**
     * Get a comment from the backing array. Since this method can be called while the table is getting updated, we need to
     * be careful to check we're using an index which is in range. The backing array can have an element removed, and then
     * the rest of the table will get updated, but parts may still use the removed index.
     *
     * @param index the row index
     *
     * @return the comment envelope at that index
     */
    CommentEnvelope getComment(int index);
}
