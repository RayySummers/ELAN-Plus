/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package mpi.eudico.client.annotator.grid;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clomimpl.abstr.ConcatAnnotation;
import mpi.eudico.util.TimeFormatter;

import javax.swing.table.AbstractTableModel;
import java.util.*;


/**
 * Class that holds the data that are displayed in the table. The Vector of column names are only used as internal
 * identifiers; the text that is displayed in the headers is stored in the ColumnModel.
 *
 * <p>The ordering of the columns in the table is more or less fixed. In single tier
 * mode the columns from left to right are:
 * <ul>
 * <li>
 * triangle icon (mediatime indicator)
 * </li>
 * <li>
 * row/annotation number
 * </li>
 * <li>
 * file name
 * </li>
 * <li>
 * tier name
 * </li>
 * <li>
 * left context
 * </li>
 * <li>
 * annotation value
 * </li>
 * <li>
 * right context
 * </li>
 * <li>
 * annotation begintime
 * </li>
 * <li>
 * annotation endtime
 * </li>
 * <li>
 * annotation duration
 * </li>
 * </ul>
 *
 * <p>In multiple tier mode the subsequent dependent tiers that are added to the table are inserted
 * between the column of the parent annotation's value and the begintime column. Currently hiding
 * a column means setting the width to zero, which means it is not removed from the model.
 */
@SuppressWarnings("serial")
public class GridViewerTableModel extends AbstractTableModel {
    /**
     * Constant for the time indicator column
     */
    public static final String TIMEPOINT = "ColumnTriangle";

    /**
     * Constant for the column showing the (row) index of the annotations
     */
    public static final String COUNT = "ColumnCount";

    /**
     * Constant for the file name column
     */
    public static final String FILENAME = "ColumnFileName";

    /**
     * Constant for the tier name column
     */
    public static final String TIERNAME = "ColumnTierName";

    /**
     * Constant for the left context column
     */
    public static final String LEFTCONTEXT = "ColumnLeftContext";

    /**
     * Constant for the main annotation column
     */
    public static final String ANNOTATION = "ColumnAnnotation";

    /**
     * Constant for the right context column
     */
    public static final String RIGHTCONTEXT = "ColumnRightContext";
    //mod. Coralie Villes add child and parent column
    /**
     * Constant for the column for the Annotation value of the parent Tier
     */
    public static final String PARENT = "ColumnParentTier";

    /**
     * Constant for the colimn for theAnnotation value of the subsequent dependent tier
     */
    public static final String CHILD = "ColumnChildTier";

    /**
     * Constant for the begin time column
     */
    public static final String BEGINTIME = "ColumnBeginTime";

    /**
     * Constant for the end time column
     */
    public static final String ENDTIME = "ColumnEndTime";

    /**
     * Constant for the duration column
     */
    public static final String DURATION = "ColumnDuration";

    /**
     * Constant for the twelve, default column names
     */
    private static final String[] fixedColumnNames = {TIMEPOINT,
                                                      COUNT,
                                                      FILENAME,
                                                      TIERNAME,
                                                      LEFTCONTEXT,
                                                      ANNOTATION,
                                                      RIGHTCONTEXT,
                                                      PARENT,
                                                      CHILD,
                                                      BEGINTIME,
                                                      ENDTIME,
                                                      DURATION};

    /**
     * Constant for the fixed colum names
     */
    protected static final Set<String> fixedColumnNamesSet = Set.of(fixedColumnNames);

    /**
     * Constant for the hh:mm:ss.ms time format
     */
    protected static final String HHMMSSsss = "TIMECODE";

    /**
     * Constant for the milliseconds time format
     */
    protected static final String MILLISECONDS = "MILLISECONDS";

    /**
     * Constant for the PAL (hh:mm:ss.ff) time format
     */
    protected static final String PAL = "PAL";

    /**
     * Constant for the NTSC (hh:mm:ss.ff) time format
     */
    protected static final String NTSC = "NTSC";

    /**
     * list of annotations
     */
    protected List<AnnotationCore> annotations;
    /**
     * holds the internal column identifiers
     */
    protected List<String> usedColumnNames;
    /**
     * is filtering boolean
     */
    protected boolean isFiltering = false;
    //   private final Hashtable numberHash = new Hashtable();
    private final String EMPTY = "";

    /*
     * a table containing the Lists of annotations/empty slots of each child
     * tier of the proper kind, stored with the tiername as key
     */
    private final Map<String, List<AnnotationCore>> childTierHash = new HashMap<String, List<AnnotationCore>>();
    private String strTimeFormat = HHMMSSsss;
    // these formats should be
    private final int TC = 0;
    private final int TC_PAL = 1;
    private final int TC_NTSC = 2;
    private final int MS = 3;
    private final int SEC_MS = 4;
    private final int TC_PAL_50 = 5;

    private int time_mode = TC;

    /**
     * Creates a new GridViewerTableModel object.
     */
    public GridViewerTableModel() {
        this(new ArrayList<AnnotationCore>());
    }

    /**
     * Creates a GridResultModel using the specified annotations as the data provider and the id's as the column
     * identifiers.
     *
     * @param annotations the (parent) annotations
     */
    GridViewerTableModel(List<AnnotationCore> annotations) {
        this.annotations = annotations;

        //minimum set; to be extended by subclasses
        usedColumnNames = new ArrayList<String>();
        usedColumnNames.add(TIMEPOINT);
        usedColumnNames.add(COUNT);
        usedColumnNames.add(ANNOTATION);
        usedColumnNames.add(BEGINTIME);
        usedColumnNames.add(ENDTIME);
        usedColumnNames.add(DURATION);
    }

    /**
     * Returns the {@code AnnotationCore} of a specific row.
     *
     * @param row the row index to get the annotation from
     *
     * @return the annotation at that row
     */
    public AnnotationCore getAnnotationCore(int row) {
        return annotations.get(row);
    }

    /**
     * Returns the row index of the annotation in the data model.
     *
     * @param ac the {@code AnnotationCore} to find
     *
     * @return the row index or -1 if not found
     */
    public int getRowForAnnotation(AnnotationCore ac) {
        return annotations.indexOf(ac);
    }

    /**
     * Returns whether or not the cell at the given row and column can be edited. This is the case if the corresponding value
     * is an instance of Annotation (e.g. not a Match) or a (possibly) empty value in a column of a child tier. ! Be sure
     * that all editable cells can be handled by GridEditor !
     *
     * @param row the row index
     * @param column the column index
     *
     * @return true if the cell can be edited, false otherwise
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        if (getValueAt(row, column) instanceof ConcatAnnotation) {
            return false;
        }
        return getValueAt(row, column) instanceof Annotation || !fixedColumnNamesSet.contains(getColumnName(column));
    }

    /**
     * Returns the number of columns in the table model.
     *
     * @return the number of columns
     */
    @Override
    public int getColumnCount() {
        return usedColumnNames.size();
    }

    /**
     * Returns the identifier of the column at the specified model index.
     *
     * @param nr the index of the column
     *
     * @return the identifier of the column
     */
    @Override
    public String getColumnName(int nr) {
        if ((0 <= nr) && (nr < usedColumnNames.size())) {
            return usedColumnNames.get(nr);
        }

        return EMPTY;
    }

    /**
     * Sets filtering on or off.
     *
     * @param filtering the new filtering flag
     */
    public void setFiltering(boolean filtering) {
        isFiltering = filtering;
    }

    /**
     * Returns whether filtering is applied.
     *
     * @return whether or not filtering is on
     */
    public boolean isFiltering() {
        return isFiltering;
    }

    /**
     * Returns the number of annotations on the (parent) tier.
     *
     * @return the number of annotations on the (parent) tier
     */
    @Override
    public int getRowCount() {
        return annotations.size();
    }

    /**
     * Sets the time format to use in the time columns.
     *
     * @param strTimeFormat the new time format, one of the constants
     */
    public void setTimeFormat(String strTimeFormat) {
        if (strTimeFormat.equals(Constants.HHMMSSMS_STRING) || strTimeFormat.equals(HHMMSSsss)) {
            time_mode = TC;
            this.strTimeFormat = Constants.HHMMSSMS_STRING;
        } else if (strTimeFormat.equals(Constants.PAL_STRING)) {
            time_mode = TC_PAL;
            this.strTimeFormat = strTimeFormat;
        } else if (strTimeFormat.equals(Constants.PAL_50_STRING)) {
            time_mode = TC_PAL_50;
            this.strTimeFormat = strTimeFormat;
        } else if (strTimeFormat.equals(Constants.NTSC_STRING)) {
            time_mode = TC_NTSC;
            this.strTimeFormat = strTimeFormat;
        } else if (strTimeFormat.equals(Constants.MS_STRING) || strTimeFormat.equals(MILLISECONDS)) {
            time_mode = MS;
            this.strTimeFormat = Constants.MS_STRING;
        } else if (strTimeFormat.equals(Constants.SSMS_STRING)) {
            time_mode = SEC_MS;
            this.strTimeFormat = Constants.SSMS_STRING;
        }
    }

    /**
     * Returns the current format for time strings.
     *
     * @return the current time format of the time columns
     */
    public String getTimeFormat() {
        return strTimeFormat;
    }

    /**
     * This method is only to be used to update a table cell in a column for the Annotation of a child tier; so it is only
     * useful in multiple tier mode. <br> This method does not call fireTableDataChanged(); changes in an Annotation in a
     * table cell will be visible after the next repaint.
     *
     * @param annotation the value for the cell
     * @param row the row index
     * @param column the column index
     */
    @Override
    public void setValueAt(Object annotation, int row, int column) {
        if (annotation instanceof AnnotationCore) {
            int size = childTierHash.size();

            if (size > 0) {
                if ((column > 2) && (column <= (2 + size)) && (row < getRowCount())) {
                    String tierName = getColumnName(column);

                    if (childTierHash.containsKey(tierName)) {
                        List<AnnotationCore> anns = childTierHash.get(tierName);
                        anns.set(row, (AnnotationCore) annotation);
                    }
                }
            }
        }
    }

    /**
     * Returns the value for the table cell at the specified row and column.
     *
     * @param row the row index
     * @param column the column index
     *
     * @return the value that has to be rendered
     */
    @Override
    public Object getValueAt(int row, int column) {
        try {
            if ((column < 0) || (column >= usedColumnNames.size()) || (row < 0) || (row >= annotations.size())) {
                return null;
            }

            String cn = getColumnName(column);

            if (cn.equals(TIMEPOINT)) {
                return null;
            }

            if (cn.equals(COUNT)) {
                return EMPTY + (row + 1);
            }

            if (cn.equals(ANNOTATION)) {
                return annotations.get(row);
            }

            if (cn.equals(BEGINTIME)) {
                return getStringRepresentation(annotations.get(row).getBeginTimeBoundary()); //begintime
            }

            if (cn.equals(ENDTIME)) {
                return getStringRepresentation(annotations.get(row).getEndTimeBoundary()); //endtime
            }

            if (cn.equals(DURATION)) {
                AnnotationCore ann = annotations.get(row);

                return getStringRepresentation(ann.getEndTimeBoundary() - ann.getBeginTimeBoundary()); //duration
            }

            if (childTierHash.containsKey(cn)) {
                List<AnnotationCore> v = childTierHash.get(cn);

                if (row < v.size()) {
                    return v.get(row);
                }
            }
        } catch (ArrayIndexOutOfBoundsException aie) {
            return null;
        }

        return null;
    }

    /**
     * Adds an annotation to the list
     *
     * @param aa the annotation to add
     */
    public void addAnnotation(AnnotationCore aa) {
        annotations.add(aa);
        fireTableDataChanged();
    }

    /**
     * Add annotations to the list
     *
     * @param list the list containing annotations
     */
    public void addAnnotations(List<? extends AnnotationCore> list) {
        annotations.addAll(list);
        fireTableDataChanged();
    }

    /**
     * Adds a child tier and its annotations/empty slots to the model. Insert the new tier just before the begin time
     * column.
     *
     * @param tierName the name of the tier
     * @param annotations the annotations
     */
    public void addChildTier(String tierName, List<AnnotationCore> annotations) {
        if (childTierHash.containsKey(tierName)) {
            return;
        }

        childTierHash.put(tierName, annotations);

        int index = findColumn(BEGINTIME);
        usedColumnNames.add(index, tierName);
        fireTableDataChanged();
    }


    /**
     * Replaces the List of annotations of the current tier. Called when the parent tier has not changed, but annotations on
     * the tier might have been changed.
     *
     * <p>FIXME TYPE We take over the ownership of the list, so it can relatively safely be cast
     * to List of AnnotationCore.
     *
     * @param annotations2 the annotations
     */
    public void updateAnnotations(List<? extends AnnotationCore> annotations2) {
        this.annotations = (List<AnnotationCore>) annotations2;
        // FIXME TYPE use this variant to make even more sure:
        //this.annotations = new ArrayList<AnnotationCore>(annotations2);
        removeChildTiers();
        fireTableDataChanged();
    }

    private String getStringRepresentation(long l) {
        switch (time_mode) {
            case MS:
                return String.valueOf(l);
            case TC_PAL:
                return TimeFormatter.toTimecodePAL(l);
            case TC_PAL_50:
                return TimeFormatter.toTimecodePAL50(l);
            case TC_NTSC:
                return TimeFormatter.toTimecodeNTSC(l);
            case SEC_MS:
                return TimeFormatter.toSSMSString(l);
            default:
                return TimeFormatter.toString(l);
        }

        //return strTimeFormat.equals(HHMMSSsss) ? TimeFormatter.toString(l) : (EMPTY + l);
    }

    /**
     * Removes the annotations of child tiers from the model.
     */
    private void removeChildTiers() {
        int numChildTiers = childTierHash.size();

        if (numChildTiers > 0) {
            for (String key : childTierHash.keySet()) {
                usedColumnNames.remove(key);
            }

            childTierHash.clear();
        }
    }
}
