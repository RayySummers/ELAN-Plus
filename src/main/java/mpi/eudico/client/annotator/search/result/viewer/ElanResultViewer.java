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
package mpi.eudico.client.annotator.search.result.viewer;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.grid.AbstractEditableGridViewer;
import mpi.eudico.client.annotator.grid.AnnotationTable;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.util.AnnotationValueComparator;
import mpi.eudico.util.EmptyStringComparator;
import mpi.eudico.util.IntStringComparator;
import mpi.eudico.util.TimeStringComparator;
import mpi.search.content.result.model.ContentMatch;
import mpi.search.content.result.model.ContentResult;
import mpi.search.result.model.Result;
import mpi.search.result.model.ResultEvent;
import mpi.search.result.viewer.ResultViewer;


/**
 * A result viewer for a search in ELAN files.
 *
 * @author Alexander Klassmann
 */
@SuppressWarnings("serial")
public class ElanResultViewer extends AbstractEditableGridViewer implements ResultViewer, ListDataListener {
    private ContentResult result;
    private TableRowSorter<EAFResultViewerTableModel> rowSorter;

    /**
     * Creates a new ElanResultViewer object.
     */
    public ElanResultViewer() {
        super(new AnnotationTable(new EAFResultViewerTableModel()));
        
        rowSorter = new TableRowSorter<EAFResultViewerTableModel>((EAFResultViewerTableModel)table.getModel());
        EmptyStringComparator emptyComp = new EmptyStringComparator();
        IntStringComparator indexComparator = new IntStringComparator();
        AnnotationValueComparator<Object> annValComp = new AnnotationValueComparator<Object>();
        TimeStringComparator timeComp = new TimeStringComparator();        
        /*
          columns:
          0: TIMEPOINT
          1: COUNT
          2: FILENAME
          3: TIERNAME
          4: LEFTCONTEXT
          5: ANNOTATION
          6: RIGHTCONTEXT
          7: PARENT
          8: CHILD
          9: BEGINTIME
         10: ENDTIME
         11: DURATION
         */
        rowSorter.setComparator(1, indexComparator);
        rowSorter.setComparator(2, emptyComp);
        rowSorter.setComparator(3, emptyComp);
        rowSorter.setComparator(4, annValComp);
        rowSorter.setComparator(5, annValComp);
        rowSorter.setComparator(6, annValComp);
        rowSorter.setComparator(7, annValComp);
        rowSorter.setComparator(8, emptyComp);
        rowSorter.setComparator(9, timeComp);
        rowSorter.setComparator(10, timeComp);
        rowSorter.setComparator(11, timeComp);
        
        table.setRowSorter(rowSorter);
    }

    /**
     * After setting the viewer manager, create the mapping from Tier name and CveId to Color,
     * which is used in the (EAFResultViewer)GridRenderer. This process needs the viewer manager.
     * When drawing,
     * {@code GridRenderer#setComponentLayout} calls
     * {@code AnnotationTable#getColorForAnnotation}.
     */
    @Override // AbstractViewer
    public void setViewerManager(ViewerManager2 viewerManager) {
    	super.setViewerManager(viewerManager);
    	if (viewerManager != null) {
    		preferencesChanged();
    	}
    }

    /**
     * Creates and returns a new {@code EAFResultViewerGridRenderer}.
     *
     * @return a new {@code EAFResultViewerGridRenderer} 
     */
    @Override
	protected TableCellRenderer createTableCellRenderer() {
        return new EAFResultViewerGridRenderer(this, dataModel);
    }

    /**
     * Sets the visibility of a column in the table.
     *
     * @param columnName the name of the column
     * @param visible the visibility flag
     */
    public void setColumnVisible(String columnName, boolean visible) {
        table.setColumnVisible(columnName, visible);
    }

    /**
     * Sets the data for this viewer (for its data model).
     *
     * @param list a list of annotations
     */
    public void setData(List<? extends AnnotationCore> list) {
        updateDataModel(list);
        updateSelection();
        doUpdate();
    }
    
    /**
     * The {@code super} implementation of scrolling is less suitable for the
     * situation with sorting possibilities.
     */
    @Override
	protected void doUpdate() {
    	// could maybe scroll to the first row containing the active annotation
    	// or otherwise the first row overlapping the selection (if any)
    	checkScroll();
    	repaint();
	}
    
    /**
     * Forced scrolling now only occurs when the active annotation changes.
     */
    private void checkScroll() {
    	AnnotationCore ac = getActiveAnnotation(); 
    	if (ac != null) {
    		int modelRow = table.getModel().getRowForAnnotation(ac);
    		if (modelRow > -1) {
    			int viewRow = table.convertRowIndexToView(modelRow);
    			Rectangle rowRect = table.getCellRect(viewRow, 0, true);
    			
    			if (table.getParent() instanceof JScrollPane) {
    				Rectangle viewRect = ((JScrollPane) table.getParent()).getViewport().getViewRect();
    				if (!viewRect.contains(rowRect)) {
    					rowRect.height = viewRect.height - rowRect.height;
    				}
    			}
    			table.scrollRectToVisible(rowRect);
    		}
    	} else {
    		table.repaint();
    	}
    }

	/**
     * Checks the event status and updates the current results.
     *
     * @param e the result event
     */
    @Override
    public void resultChanged(ResultEvent e) {
        result = (ContentResult) e.getSource();

        if (result.getRealSize() == 0) {
            reset();
        }
        if ((e.getType() == ResultEvent.STATUS_CHANGED) &&
                (result.getStatus() == Result.INIT)) {
        	
            result.addListDataListener(this);
        }
    }

    /**
     * A request to show the specified results.
     *
     * @param result the new results to show
     */
    @Override // ResultViewer
    public void showResult(Result result) {
        this.result = (ContentResult) result;
        setData(result.getMatches());
    }

    /**
     * Resets (clears) the data model.
     */
    @Override // ResultViewer
    public void reset() {
        setData(new ArrayList<AnnotationCore>(0));
    }
   
   /**
    * Updates the data model. 
    *
    * @param e the event
    */
   @Override // ListDataListener
   public void contentsChanged(ListDataEvent e) {	
  		dataModel.updateAnnotations(result.getSubList());
   }

   @Override // ListDataListener
   public void intervalAdded(ListDataEvent e) {
       for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
           dataModel.addAnnotation((ContentMatch) result.getElementAt(i));
       }
   }

   /**
    * Empty implementation.
    *
    */
   @Override // ListDataListener
   public void intervalRemoved(ListDataEvent e) {
   }

	/**
	 * Method from ElanLocaleListener not implemented in AbstractViewer.
	 */
   @Override
	public void updateLocale() {
		super.updateLocale();
		popup = new EAFResultViewerPopupMenu(table);
	}
	
    /**
     * Checks the kind of edit that has happened and updates the table when necessary.
     *
     * @param e the ACMEditEvent
     */
    @Override
    public void ACMEdited(ACMEditEvent e) {
        if ((result == null) || (result.getTierNames().length == 0)) {
            return;
        }

        TierImpl changedTier = null;

        switch (e.getOperation()) {
        case ACMEditEvent.ADD_TIER:
            break;

        case ACMEditEvent.ADD_ANNOTATION_BEFORE:
            break;

        case ACMEditEvent.ADD_ANNOTATION_AFTER:
            break;

        case ACMEditEvent.CHANGE_ANNOTATION_TIME:
        case ACMEditEvent.CHANGE_ANNOTATIONS:

        // fallthrough
        case ACMEditEvent.REMOVE_ANNOTATION:
            repaint();

            break;

        case ACMEditEvent.CHANGE_TIER:

            // a tier is invalidated the kind of change is unknown
            changedTier = (TierImpl) e.getInvalidatedObject();

            break;
        case ACMEditEvent.REMOVE_TIER:
            changedTier = (TierImpl) e.getModification();

            

            break;

        default:
            super.ACMEdited(e);
        }
        
        // in case of a "change tier" or "remove tier" event
        if (changedTier != null) {
        	for (int i = 0; i < result.getTierNames().length; i++) {
                try {
                    if (result.getTierNames()[i].equals(changedTier.getName())) {
                        result.reset();

                        break;
                    }
                }
                catch (Exception er) {
                }
            }
        }
    }    
    
}
