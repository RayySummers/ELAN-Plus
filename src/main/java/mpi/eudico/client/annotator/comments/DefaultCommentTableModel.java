package mpi.eudico.client.annotator.comments;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.util.TimeFormatter;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * The default implementation of the CommentTableModel.
 *
 * @author olasei
 * @see CommentTableModel
 */
@SuppressWarnings("serial")
public class DefaultCommentTableModel extends AbstractTableModel implements CommentTableModel {

    private List<CommentEnvelope> comments;    // a possibly read-only instance of the underlying comment list
    private CommentManager commentManager = null;
    private static final CommentEnvelope noComment = new CommentEnvelope();

    /**
     * Create a model for a comment table (which is a table that shows comments). It needs a reference to the CommentManager,
     * so it can tell it when editing has taken place. If you don't want the comments to be editable, you can pass null.
     *
     * @param cm the comment manager
     */
    public DefaultCommentTableModel(CommentManager cm) {
        super();
        this.comments = new ArrayList<CommentEnvelope>(0);
        this.commentManager = cm;
    }

    /**
     * Tell the Model about the List which contains the comments. It does not make a copy of all comments (or references to
     * comments), the model will work on the main list itself (or likely a read-only instance of it). It fires a
     * TableDataChanged event so that the GUI will update itself.
     *
     * @param comments the list of comments
     */
    @Override // CommentTableModel
    public void setComments(List<CommentEnvelope> comments) {
        this.comments = comments;
        fireTableDataChanged();
    }

    /**
     * Get a comment from the backing array. Since this method can be called while the table is getting updated, we need to
     * be careful to check we're using an index which is in range. The backing array can have an element removed, and then
     * the rest of the table will get updated, but parts may still use the removed index.
     *
     * @param index the row index
     *
     * @return the comment envelope or the empty {@code noComment} comment envelope
     */
    @Override // CommentTableModel
    public CommentEnvelope getComment(int index) {
        if (index >= 0 && index < comments.size()) {
            return comments.get(index);
        }
        return noComment;
    }

    /**
     * Constant start time
     */
    public static final int START_TIME = 0;
    /**
     * Constant end time
     */
    public static final int END_TIME = 1;
    /**
     * Constant tier time
     */
    public static final int TIER_NAME = 2;
    /**
     * Constant initials
     */
    public static final int INITIALS = 3;
    /**
     * Constant comment
     */
    public static final int COMMENT = 4;
    /**
     * Constant thread id
     */
    public static final int THREAD_ID = 5;
    /**
     * Constant sender
     */
    public static final int SENDER = 6;
    /**
     * Constant recipient
     */
    public static final int RECIPIENT = 7;
    /**
     * Constant creation date
     */
    public static final int CREATION_DATE = 8;
    /**
     * Constant modification date
     */
    public static final int MODIFICATION_DATE = 9;

    // Note: this doesn't update itself when the locale changes.
    private static final String[] columnNames = {
        /* 0 */ ElanLocale.getString("CommentTable.StartTime"),
        /* 1 */ ElanLocale.getString("CommentTable.EndTime"),
        /* 2 */ ElanLocale.getString("CommentTable.Tier"),
        /* 3 */ ElanLocale.getString("CommentTable.Initials"),
        /* 4 */ ElanLocale.getString("CommentTable.Comment"),
        /* 5 */ ElanLocale.getString("CommentTable.Thread"),
        /* 6 */ ElanLocale.getString("CommentTable.Sender"),
        /* 7 */ ElanLocale.getString("CommentTable.Recipient"),
        /* 8 */ ElanLocale.getString("CommentTable.CreationDate"),
        /* 9 */ ElanLocale.getString("CommentTable.ModificationDate")
    };

    @Override // TableModel
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override // TableModel
    public int getRowCount() {
        return comments.size();
    }

    @Override // TableModel
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /**
     * Returns <code>String.class</code> regardless of column index.
     */
    @Override // TableModel
    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    @Override // TableModel
    public Object getValueAt(int row, int col) {
        if (row >= 0 && row < comments.size()) {
            CommentEnvelope e = comments.get(row);

            return getValueAtColumn(e, col);
        }
        return null;
    }

    /**
     * A method to select the value from a passed column
     *
     * @param e the commentEnvelope
     * @param col the integer value for selection
     *
     * @return the string value
     */
    public static String getValueAtColumn(CommentEnvelope e, int col) {
        switch (col) {
            case START_TIME:
                return TimeFormatter.toString(e.getStartTime());
            case END_TIME:
                return TimeFormatter.toString(e.getEndTime());
            case TIER_NAME:
                return e.getTierName();
            case INITIALS:
                return e.getInitials();
            case COMMENT:
                return e.getMessage();
            case THREAD_ID:
                return e.getThreadID();
            case SENDER:
                return e.getSender();
            case RECIPIENT:
                return e.getRecipient();
            case CREATION_DATE:
                return readableTime(e.getCreationDateString());
            case MODIFICATION_DATE:
                return readableTime(e.getModificationDateString());
            default:
                return "-";
        }
    }

    /**
     * Make a string of the form 1234-56-78T12:34:56Z a bit more human-friendly.
     *
     * @param formal the time string
     *
     * @return the less formal time string
     */
    public static String readableTime(String formal) {
        return formal.replace('T', ' ').replace("Z", " UTC");
    }

    /**
     * Columns INITIALS, COMMENT, THREAD_ID and SENDER are editable, but only if we have a CommentManager.
     */
    @Override // TableModel
    public boolean isCellEditable(int row, int col) {
        if (commentManager == null) {
            return false;
        }

        CommentEnvelope e = comments.get(row);
        if (e.isReadOnly()) {
            return false;
        }

        switch (col) {
            case INITIALS:
            case COMMENT:
            case THREAD_ID:
            case SENDER:
            case RECIPIENT:
                return true;
            default:
                return false;
        }
    }

    @Override // TableModel
    public void setValueAt(Object object, int row, int col) {
        if (!(object instanceof String value)) {
            return;
        }
        if (commentManager == null) {
            return;
        }
        CommentEnvelope e = commentManager.undoableGet(row);

        switch (col) {
            case INITIALS:
                value = value.trim();
                e.setInitials(value);
                break;
            case COMMENT:
                e.setMessage(value);
                break;
            case THREAD_ID:
                value = value.trim();
                e.setThreadID(value);
                break;
            case SENDER:
                value = value.trim();
                e.setSender(value);
                break;
            case RECIPIENT:
                value = value.trim();
                e.setRecipient(value);
                break;
        }
        e.setModificationDate();
        fireTableCellUpdated(row, col);
        fireTableCellUpdated(row, MODIFICATION_DATE);

        commentManager.undoableRelease(row, e);
    }
}
