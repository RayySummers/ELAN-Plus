package mpi.eudico.client.annotator.transcriptionMode;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.util.TableSubHeaderObject;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
 * JTable specially made for easy transcription and translation,
 * in the transcription mode.
 * 
 * @author Aarthy Somasundaram
 *
 */
@SuppressWarnings("serial")
public class TranscriptionTable extends JTable {
	
	private Map<String, Color> prefTierFontColors;
	private Map<String, Color> prefTierBrighterFontColors;
	private Map<String, Color> prefTierBrightestFontColors;
	private Map<String, Font> prefTierFonts;	
	
	private List<String> nonEditableTiersList;
	
	/** stores width of parent component */
	//private int width = -1;
	
	/** Default height of a row (renamed from rowHeight to defRowHeight to more
	 * clearly distinguish it from JTable.rowHeight */	
	private int defRowHeight;
	
	private int currentRowIndex = -1;
	private int currentColumnIndex = -1;
	
	private boolean moveViaColumn = false;	
	private boolean scrollToCenter = false;		
	private boolean storeColumnOrder = false;
	private boolean autoCreateAnn = true;
	
	private int extraRowHeight = 8;
	private int extraRowMargin = 2;
	private JTextArea textArea = new JTextArea();
	private int subHeaderRowHeight;
	private Font indexColumnFont;
	private Font tierNameFont;
	
	/**
	 * Creates an instance of TranscriptionTable.
	 */
	public TranscriptionTable( ) {	
		defRowHeight = getFontSize() + extraRowHeight;
		subHeaderRowHeight = getFontSize() + extraRowMargin;
		prefTierFontColors = new HashMap<String, Color>();
		prefTierBrighterFontColors = new HashMap<String, Color>();
		prefTierBrightestFontColors = new HashMap<String, Color>();
		prefTierFonts = new HashMap<String, Font>();		
		
		// table settings
		setFont(Constants.DEFAULTFONT);
		setCellSelectionEnabled(true);		
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);		
		setShowGrid(true);
	    //setGridColor(Color.BLACK);
	    setSurrendersFocusOnKeystroke(true);	    
	    getTableHeader().setReorderingAllowed(true);
	    
	    // textarea
	    textArea.setLineWrap(true);
	    textArea.setWrapStyleWord(true);    	
	    textArea.setMargin(new Insets(0,3,0,3));
    	
	    // column model
	    this.setColumnModel(new DefaultTableColumnModel( ){	
			   @Override
			public void moveColumn(int columnIndex, int newIndex){				   
				   if(columnIndex == 0 || newIndex== 0) {
					  return;
				   }
				   
				   String columnName = TranscriptionTable.this.getColumnName(getCurrentColumn());
				   super.moveColumn(columnIndex, newIndex);					   
				  
				   if(storeColumnOrder){
					   if(columnIndex == getCurrentColumn() || newIndex == getCurrentColumn()){
						   setCurrentColumn(TranscriptionTable.this.getColumnModel().getColumnIndex(columnName));
					   }
					   TranscriptionViewer viewer = (TranscriptionViewer) TranscriptionTable.this.getParent().getParent().getParent();
					   viewer.storeColumnOrder();					  
					   boolean playBack = viewer.isAutoPlayBack();
					   viewer.setAutoPlayBack(false);					   
					   startEdit(null);
					   viewer.setAutoPlayBack(playBack);
				   }	
			   }
			   
			   @Override
			protected void fireColumnMarginChanged(){
				   if(!storeColumnOrder){
					   return;
				   }		   
				   super.fireColumnMarginChanged();					  
				   reCalculateRowHeight();	
//				   	if(TranscriptionTable.this.getParent() !=null){
//				   		TranscriptionViewer viewer = (TranscriptionViewer) TranscriptionTable.this.getParent().getParent().getParent();
//				   		boolean playBack = viewer.isAutoPlayBack();
//				   		viewer.setAutoPlayBack(false);				   		
//				   		//startEdit(null);
//				   		viewer.setAutoPlayBack(playBack);
//				   	}				   	
			   }
		});
		
		// remove the default behavior of TAB and ENTER
		Action doNothing = new AbstractAction() {
		    @Override
			public void actionPerformed(ActionEvent e) { }
		};		
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "doNothing");
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),	"doNothing");
		getActionMap().put("doNothing", doNothing);		
	}
	
	@Override
	public void columnMarginChanged(ChangeEvent e) {		
		TableColumn resizingColumn = this.getTableHeader().getResizingColumn();
		// Need to do this here, before the parent's
		// layout manager calls getPreferredSize().
		if (resizingColumn != null && autoResizeMode == AUTO_RESIZE_OFF) {
		    resizingColumn.setPreferredWidth(resizingColumn.getWidth());
		}
		
		if((TranscriptionTableCellEditor)this.getCellEditor() != null){
   			((TranscriptionTableCellEditor)this.getCellEditor()).commitChanges();
   		} 		
		resizeAndRepaint();
	}
	
	/**
	 * Set the list of tiers that are frozen (non-editable).
	 * 
	 * @param list a list of all tier names
	 */
	public void setNoneditableTiers( List<String> list){
		nonEditableTiersList = list;		
		((TranscriptionTableCellRenderer)this.getDefaultRenderer(Object.class)).setNonEditableTiers(list);
		((TranscriptionTableModel)this.getModel()).setNonEditableTiers(list);
		this.repaint();
	}
	
	/**
	 * Sets the flag for automatic creation of annotations.
	 * 
	 * @param create if {@code true} auto-creation of annotation is enabled
	 */
	public void setAutoCreateAnnotations(boolean create){
		autoCreateAnn  = create;
		((TranscriptionTableCellRenderer)this.getDefaultRenderer(Object.class)).setAutoCreateAnnotations(autoCreateAnn);
		((TranscriptionTableModel)this.getModel()).setAutoCreateAnnotations(autoCreateAnn);
		repaint();
	}
	
	/**
	 * Returns whether auto creation of annotations is enabled.
	 * 
	 * @return {@code true} if auto creation is enabled, {@code false} otherwise
	 */
	public boolean isAnnotationsCreatedAutomatically(){
		return autoCreateAnn;
	}
	
	/**
	 * Sets a flag whether the column order should be
	 * stored or not.
	 * 
	 * @param store if {@code true} the column order is stored,
	 *   otherwise it isn't
	 */
	public void setStoreColumnOrder(boolean store){
		storeColumnOrder = store;
	}
	
	/**
	 * Returns whether the column order will be stored.
	 * 
	 * @return {@code true} if the column order is to be stored
	 */
	public boolean getStoreColumnOrder(){
		return storeColumnOrder;
	}
	
	/**
	 * Sets a flag whether the {@code Enter} key moves to the next cell in the
	 * column or the next cell in the row.
	 * 
	 * @param selected if {@code true} moves to the next cell in the column,
	 * 		if {@code false} moves to the next cell in the row
	 */
	public void moveViaColumn(boolean selected) {
		moveViaColumn = selected;			
	}
	
	/**
	 * Sets a flag whether the editing cell should always be in the
	 * center of the screen/table, or not.
	 * 
	 * @param selected if {@code true} scrolls the current editing cell to the
	 * center of the table, otherwise the default behavior of the table applies
	 */
	public void scrollActiveCellInCenter(boolean selected) {
		scrollToCenter = selected;
	}

	@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {		
		if(condition != WHEN_ANCESTOR_OF_FOCUSED_COMPONENT){
			return false;
		}
				
		if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.COMMIT_CHANGES,ELANCommandFactory.TRANSCRIPTION_MODE)){			
			if(!isEditing()){
				int selectedRow = getSelectedRow();
				int selectedColumn = getSelectedColumn();
				if(editCellAt(selectedRow,selectedColumn)){
					setCurrentRow(selectedRow);
					setCurrentColumn(selectedColumn);
				}else { 
					goToNextEditableCell();	
				}
			}	
			startEdit(e);
		}   else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.MOVE_UP, ELANCommandFactory.TRANSCRIPTION_MODE)) {  
        	//e.consume();       
            goToEditableCellUp();                 
        }  
		// move down
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.MOVE_DOWN, ELANCommandFactory.TRANSCRIPTION_MODE)) {  
        	//e.consume();        
            goToEditableCellDown();           
        } 
		//move left
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.MOVE_LEFT, ELANCommandFactory.TRANSCRIPTION_MODE)) {        
        	//e.consume();
        	goToEditableCellLeft();                   
        } 		
		//move right
        else if(ks == ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.MOVE_RIGHT, ELANCommandFactory.TRANSCRIPTION_MODE)) {        
        	//e.consume();
        	goToEditableCellRight();        
        } 
		
		return false;
	}
	
	/**
	 * Returns the tier name for the given cell.
	 * 
	 * @param row the row index
	 * @param column the column index
	 * @return the name of the tier in the given address
	 * 					 in the table
	 */
	public String getTierName(int row, int column){ 
    	String tierName = null;	      	
    	Object val = getValueAt(row, column);
    	if(val instanceof Annotation){
    		AbstractAnnotation ann = (AbstractAnnotation)val;
    		 tierName = ann.getTier().getName();
    	} else if (val instanceof AnnotationCellPlaceholder) {
    		return ((AnnotationCellPlaceholder) val).tierName;
    	} else if( val instanceof TableSubHeaderObject){
    		tierName = val.toString();
    	}
    	return tierName;
    }
	
	/**
	 * Returns the current row in the table.
	 * 
	 * @return the current row index
	 */
	public int getCurrentRow() {		
		return currentRowIndex;
	}
	
	/**
	 * Sets the current row in the table.
	 * 
	 * @param newRow the new current row index
	 */
	private void setCurrentRow(int newRow){		
		currentRowIndex = newRow;		
	}
	
	/** 
	 * Returns the default height of a row.
	 * 
	 * @return the default row height
	 */
	public int getDefaultRowHeight(){
		return this.defRowHeight;
	}
	
	/**
	 * Returns the current column in the table.
	 * 
	 * @return the index of the current column
	 */
	public int getCurrentColumn() {
		return currentColumnIndex;
	}
	
	/**
	 * Sets the current column in the table.
	 * 
	 * @param newColumn the new current column index
	 */
	private void setCurrentColumn(int newColumn){
		currentColumnIndex = newColumn;		
	}
	
	/**
	 * Jumps to the first editable cell moving up.
	 */
	public void goToEditableCellUp(){
		int rowBefore = getCurrentRow() -1;
		
   		while(true){
   			if(rowBefore < 0){
   				rowBefore = 0;
   			}
   			
   			if(getValueAt(rowBefore,getCurrentColumn()) instanceof TableSubHeaderObject){	
   				if(rowBefore == 0){
   					rowBefore = getCurrentRow();
   					break;
   				} else {
   					rowBefore = rowBefore-1;   									
   				} 
   			}   	
   				
   			if (getValueAt(rowBefore,getCurrentColumn()) == null ){
   				if(rowBefore == 0){
   					rowBefore = getCurrentRow();
   					break;
   				} else {
					rowBefore = rowBefore-1;
				} 
   				continue;
   			}   
   			
   			Object cellValue = getValueAt(rowBefore,getCurrentColumn());
   			if (cellValue instanceof AnnotationCellPlaceholder) {
   				if (!((AnnotationCellPlaceholder) cellValue).canCreate	// eq. to null see above	
   						|| !autoCreateAnn){
   	   				if(rowBefore == 0){
   	   					rowBefore = getCurrentRow();
   	   					break;
   	   				} else {
   						rowBefore = rowBefore-1;
   					} 
   	   				continue;
   				}
   			}        
   			
   			String tierName = getTierName(rowBefore, getCurrentColumn());
			if(nonEditableTiersList != null){
				if(nonEditableTiersList.contains(tierName)){
					if(rowBefore == 0){
	   					rowBefore = getCurrentRow();
	   					break;
	   				} else {
						rowBefore = rowBefore-1;
					} 
					continue;
				}
			}
   					
   			break;
   		}
		
		if(rowBefore != getCurrentRow()){
			changeSelection(rowBefore, getCurrentColumn(), false, false);	
			if(this.isEditing()){
				editingStopped(null);
			}
			startEdit(null);
		}
  	}
	
	/**
	 * Jumps to the first editable cell moving down.
	 */
	public void goToEditableCellDown(){
		int rowNext = getCurrentRow() + 1;
   		
   		while(true){
   			if(rowNext >= getRowCount()){
   				rowNext = getCurrentRow();
   			}
   				
   			if(getValueAt(rowNext,getCurrentColumn()) instanceof TableSubHeaderObject){
   				if(rowNext >= getRowCount()){
   					rowNext = getCurrentRow();
   					break;
   				} else {
   					rowNext = rowNext+1;
   				}
   			}   	
   			
   			if (getValueAt(rowNext,getCurrentColumn()) == null ){
   				if(rowNext >= getRowCount()){
   					rowNext = getCurrentRow();
   					break;
   				} else {
   					rowNext = rowNext+1;
   				}
   				continue;
   			}
   			
   			Object cellValue = getValueAt(rowNext,getCurrentColumn());
   			if (cellValue instanceof AnnotationCellPlaceholder) {
   				if (!((AnnotationCellPlaceholder) cellValue).canCreate	// eq. to null see above	
   						|| !autoCreateAnn){
   	   				if(rowNext >= getRowCount()){
   	   					rowNext = getCurrentRow();
   	   					break;
   	   				} else {
   	   					rowNext = rowNext+1;
   	   				}
   	   				continue;
   				}
   			} 
   			
   			String tierName = getTierName(rowNext, getCurrentColumn());
			if(nonEditableTiersList != null){
				if(nonEditableTiersList.contains(tierName)){
					if(rowNext >= getRowCount()){
	   					rowNext = getCurrentRow();
	   					break;
	   				} else {
	   					rowNext = rowNext+1;
	   				}
					continue;
				}
			}
			
   			break;
   		}
		
		if(rowNext != getCurrentRow()){
			changeSelection(rowNext, getCurrentColumn(), false, false);		
			if(this.isEditing()){
				editingStopped(null);
			}
			startEdit(null);
		}
  	}
	
	/**
	 * Jumps to the first editable cell moving left.
	 */
	public void goToEditableCellLeft(){
		int leftColumn = getCurrentColumn() - 1;
		
		while(true){
			if(leftColumn == 0){
	   			leftColumn = getCurrentColumn();
	   			break;
	   		}
				
			if (getValueAt(getCurrentRow(),leftColumn) == null ){
				leftColumn = leftColumn - 1;
				continue;
	   		}
			
   			Object cellValue = getValueAt(getCurrentRow(), leftColumn);
   			if (cellValue instanceof AnnotationCellPlaceholder) {
   				if (!((AnnotationCellPlaceholder) cellValue).canCreate	// eq. to null see above	
   						|| !autoCreateAnn){
   					leftColumn = leftColumn - 1;
   					continue;
   				}
   			}
			
			String tierName = getTierName(getCurrentRow(), leftColumn);
			if(nonEditableTiersList != null){
				if(nonEditableTiersList.contains(tierName)){
					leftColumn = leftColumn-1;
					continue;
				}
			}
			
			break;
		} 
   	
		if(leftColumn != getCurrentColumn()){
			changeSelection(getCurrentRow(), leftColumn, false, false);	
			if(this.isEditing()){
				editingStopped(null);
			}
			startEdit(null);
		}
  	}
	
	/**
	 * Jumps to the first editable cell moving right.
	 */
	public void goToEditableCellRight(){
		int rightColumn = getCurrentColumn() + 1;
		
		while(true){
			if(rightColumn > (getColumnCount()-1)){   						
				rightColumn = getCurrentColumn();
				break;
			}
				
			if (getValueAt(getCurrentRow(),rightColumn) == null ){
				rightColumn = rightColumn + 1;
				continue;
			}
			
   			Object cellValue = getValueAt(getCurrentRow(), rightColumn);
   			if (cellValue instanceof AnnotationCellPlaceholder) {
   				if (!((AnnotationCellPlaceholder) cellValue).canCreate	// eq. to null see above	
   						|| !autoCreateAnn){
   					rightColumn = rightColumn + 1;
   					continue;
   				}
   			}
			
			String tierName = getTierName(getCurrentRow(), rightColumn);
			if(nonEditableTiersList != null){
				if(nonEditableTiersList.contains(tierName)){
					rightColumn = rightColumn+1;
					continue;
				}
			}
				
			break;
		}			 
   		
		if(rightColumn != getCurrentColumn()){
			changeSelection(getCurrentRow(), rightColumn, false, false);	
			if(this.isEditing()){
				editingStopped(null);
			}
			startEdit(null);
		}			
  	}
	
	/**
	 * Jumps to the next editable cell.
	 */
	public void goToNextEditableCell(){
   		if(moveViaColumn){   			
   			int nextRow = getCurrentRow();
   	   		int nextColumn = getCurrentColumn()+1;
   			while(true){
   				if(nextColumn > (getColumnCount()-1)){   						
   					nextColumn = 1;
   					nextRow = nextRow+1;   
   					if(nextRow >= getRowCount()){
   	   					nextRow = 0;
   	   				}
   				}  
   				
   				if(getValueAt(nextRow,nextColumn) instanceof TableSubHeaderObject){	
   					nextRow = nextRow+1;   						
   				} 
   					
   				if (getValueAt(nextRow,nextColumn) == null ){
   					nextColumn = nextColumn+1;
   					continue;
   				}
   				
   	   			Object cellValue = getValueAt(nextRow, nextColumn);
   	   			if (cellValue instanceof AnnotationCellPlaceholder) {
   	   				if (!((AnnotationCellPlaceholder) cellValue).canCreate	// eq. to null see above	
   	   						|| !autoCreateAnn){
   	   					nextColumn = nextColumn + 1;
   	   					continue;
   	   				}
   	   			}
   				   				
   				String tierName = getTierName(nextRow, nextColumn);
   				if(nonEditableTiersList != null){
   					if(nonEditableTiersList.contains(tierName)){
   						nextColumn = nextColumn+1;
   						continue;
   					}
   				}
   				break;
   			}
   			changeSelection(nextRow, nextColumn, false, false);   			
			if(this.isEditing()){
				editingStopped(null);
			}
			startEdit(null);
  		} else {
  			int nextRow = getCurrentRow()+1;
   	   		int nextColumn = getCurrentColumn();  
   			while(true){
   				if(nextRow > (getRowCount()-1)){ 
   					nextRow = 0;   	
   					nextColumn = nextColumn + 1;
   					if(nextColumn > (getColumnCount()-1)){   						
   						nextColumn = 1;   	   					
   	   				}   					
   				}  
   				
   				if(getValueAt(nextRow,nextColumn) instanceof TableSubHeaderObject){	
   					nextRow = nextRow+1;   						
   				} 
   					
   				if (getValueAt(nextRow,nextColumn) == null ){
   					nextRow = nextRow+1;
   					continue;
   				}				
   				
   	   			Object cellValue = getValueAt(nextRow, nextColumn);
   	   			if (cellValue instanceof AnnotationCellPlaceholder) {
   	   				if (!((AnnotationCellPlaceholder) cellValue).canCreate	// eq. to null see above	
   	   						|| !autoCreateAnn){
   	   					nextRow = nextRow+1;
   	   					continue;
   	   				}
   	   			}	
   				
   				String tierName = getTierName(nextRow, nextColumn);
   				if(nonEditableTiersList != null){
   					if(nonEditableTiersList.contains(tierName)){
   						nextRow = nextRow+1;
   						continue;
   					}
   				}
   				break;
   			}
   			changeSelection(nextRow, nextColumn, false, false);
   			if(this.isEditing()){
				editingStopped(null);
			}
			startEdit(null);
  		} 
  	}
	
	/**
	 * Scrolls the current editing row to the center of the table, if needed.
	 * 
	 */
	public void scrollIfNeeded(){		
//		if(!scrollToCenter){
//			return;
//		}
		
		JViewport viewport = (JViewport)getParent();	
		Rectangle rect = getCellRect(getCurrentRow(), getCurrentColumn(), true);		   
		Rectangle viewRect = viewport.getViewRect();
		rect.setLocation(rect.x-viewRect.x, rect.y-viewRect.y);
		
		int centerX = (viewRect.width-rect.width)/2;
		int centerY = (viewRect.height-rect.height)/2;
		
		if (rect.x < centerX) {
			centerX = -centerX;
		}
		
		if (rect.y < centerY) {
			centerY = -centerY;
		}
		rect.translate(centerX, centerY);
	
		viewport.scrollRectToVisible(rect);
	}   		
	
	/**
	 * Automatically scrolls the current editing row to
	 * the center of the table, if needed.
	 * 
	 */
	public void scrollIfNeededAutomatically(){		
		if(scrollToCenter){
			return;
		}
		
		JViewport viewport = (JViewport)getParent();	
		Rectangle rect = getCellRect(getCurrentRow(), getCurrentColumn(), true);		   
		Rectangle viewRect = viewport.getViewRect();
		rect.setLocation(rect.x-viewRect.x, rect.y-viewRect.y);

		rect.translate(0, 0);
	
		viewport.scrollRectToVisible(rect);
	}   		
	
	/**
	 * Starts editing of the current cell.
	 * 
	 * @param e the key event
	 */
	public void startEdit(KeyEvent e){			
		if (!isEditing()) {
			if (!editCellAt(getCurrentRow(), getCurrentColumn(), e)) {
				return ;
			}
			
			String tierName = getTierName(getCurrentRow(), getCurrentColumn());{
				if(tierName != null && nonEditableTiersList!= null &&  nonEditableTiersList.contains(tierName)){	
					goToNextEditableCell();
					return;
				}
			}
		}			
		
		TranscriptionTableEditBox editorComponent = ((TranscriptionTableCellEditor)getCellEditor()).getEditorComponent();
	    if (editorComponent != null) {
	    	if (getSurrendersFocusOnKeystroke()) {
	    		if(editorComponent.isUsingControlledVocabulary()){
	    			scrollIfNeededAutomatically();
	    		}	    		
	    		editorComponent.grabFocus();
            }
	    }
	}
    
	/**
	 * Deletes all the rows in the table.
	 */
    public void clearRows(){	    	    	
    	while(getRowCount() > 0){
    		((TranscriptionTableModel)getModel()).removeRow(0);
    	}    	
    	revalidate();
    	setCurrentRow(0); 
    	setCurrentColumn(1);   
    }    
    
    @Override
	public void changeSelection(int row,  int column, boolean  toggle, boolean extend) {    	    	
    	if(column == 0){
    		startEdit(null);
//    		if(TranscriptionTable.this.getParent() !=null){
//		   		TranscriptionViewer viewer = (TranscriptionViewer) TranscriptionTable.this.getParent().getParent().getParent();
//		   		boolean playBack = viewer.isAutoPlayBack();
//		   		viewer.setAutoPlayBack(false);	
//		   		long mediaTime = viewer.getViewerManager().getMasterMediaPlayer().getMediaTime();	 
//		   		startEdit(null);
//		   		viewer.setAutoPlayBack(playBack);
//		   		viewer.getViewerManager().getMasterMediaPlayer().setMediaTime(mediaTime);		   		
//		   	}
	    	return;  
	    }     	
    	
    	setCurrentRow(row); 
    	setCurrentColumn(column); 
    	
    	if(scrollToCenter){
    		scrollIfNeeded();
		}
        super.changeSelection(row, column, toggle, extend);	      
    }

   	@Override
	public void editingStopped(ChangeEvent e) {	   	
   		if((TranscriptionTableCellEditor)this.getCellEditor() != null){ 
   			((TranscriptionTableCellEditor)this.getCellEditor()).commitChanges();
   		}  
		super.editingStopped(e);
		recalculateCurrentRowHeight();
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
		super.editingCanceled(e);
		recalculateCurrentRowHeight();
		this.requestFocusInWindow();	
	}
	
	/**
	 * Removes all settings and preferences concerning font colors.
	 */
	public void clearColorPreferences(){
		prefTierFontColors.clear();
		prefTierBrighterFontColors.clear();
		prefTierBrightestFontColors.clear();
	}
	
	/**
	 * Set the font color of the tiers. 
	 * 
	 * @param fontColors a tier name to color map
	 */
	public void setFontColorForTiers(Map<String, Color> fontColors) {
		//prefTierFontColors.clear();
		if (fontColors != null) {
			prefTierFontColors.putAll(fontColors);
			
			for (Map.Entry<String, Color> e : fontColors.entrySet()) {
				String key = e.getKey();
				Color val = e.getValue();
				if (val != null) {
					prefTierBrighterFontColors.put(key, brighter(val));
					prefTierBrightestFontColors.put(key, brightest(val));
				}
			}
		}		
	}
	
	/**
	 * Calculates the brightest version of the color, comparable to alpha of 40.
	 * 
	 * @param orig the original color
	 * @return the brightest color
	 */
	private Color brightest(Color orig) {
		//return new Color(orig.getRed(), orig.getGreen(), orig.getBlue(), 40);
        double FACTOR = 0.16;

        return new Color((int) (255 - ((255 - orig.getRed()) * FACTOR)),
        		(int) (255 - ((255 - orig.getGreen()) * FACTOR)),
        		(int) (255 - ((255 - orig.getBlue()) * FACTOR)));
	}
	
	/**
	 * Calculates a brighter version of the color, comparable to alpha of 70.
	 * 
	 * @param orig the original color
	 * @return the brighter color
	 */
	private Color brighter(Color orig) {
		//return new Color(orig.getRed(), orig.getGreen(), orig.getBlue(), 70);
        double FACTOR = 0.3;

        return new Color((int) (255 - ((255 - orig.getRed()) * FACTOR)),
        		(int) (255 - ((255 - orig.getGreen()) * FACTOR)),
        		(int) (255 - ((255 - orig.getBlue()) * FACTOR)));
	}
	
	/**
	 * Returns the font color of the tier.
	 * 
	 * @param tierName tier for which the font color is requested
	 * @return the font color 
	 */
	public Color getFontColorForTier(String tierName) {
		return prefTierFontColors.get(tierName);
	}
	
	/**
	 * Returns the brighter font color of the tier.
	 * 
	 * @param tierName tier for which the brighter font color is returned
	 * @return brighter font color 
	 */
	public Color getBrighterFontColorForTier(String tierName) {
		return prefTierBrighterFontColors.get(tierName);
	}
	
	/**
	 * Returns the brightest version of the font color of a tier.
	 * 
	 * @param tierName tier for which the brightest font color is returned
	 * @return brightest font color 
	 */
	public Color getBrightestFontColorForTier(String tierName) {
		return prefTierBrightestFontColors.get(tierName);
	}

	/**
	 * Returns the tier to font color map.
	 * 
	 * @return a mapping from tier name to font color
	 */
	public Map<String, Color> getFontColorTierMap() {
		return prefTierFontColors;
	}

	/**
	 * Sets the fonts of the tiers.
	 * 
	 * @param fonts a mapping from tier name to a font object
	 */
	public void setFontsForTiers(Map<String, Font> fonts) {
		prefTierFonts.clear();
		if (fonts != null) {
			for (Map.Entry<String, Font> e : fonts.entrySet()) {
				String key = e.getKey();
				String fn = e.getValue().getName();
				if (fn != null) {
					prefTierFonts.put(key, new Font(fn, Font.PLAIN, getFontSize()));
				} 
			}
		}	
	}
	
	/**
	 * (Re)sets the overall font of the table.
	 */
	@Override
	public void setFont(Font font) {
        super.setFont(font);
        defRowHeight = font.getSize() + extraRowHeight;
        indexColumnFont = font.deriveFont(Font.PLAIN, font.getSize());
        tierNameFont = font.deriveFont(Font.PLAIN, font.getSize() + extraRowMargin);
        
        // sub-header font size, not sure if this is different from getFontSize() + extraRowMargin
		FontMetrics fm = getFontMetrics(getFont());
		subHeaderRowHeight = fm.getAscent() + fm.getDescent() + extraRowMargin;
        
        if (prefTierFonts != null && !prefTierFonts.isEmpty()) {
        	for (Map.Entry<String, Font> fontEntry : prefTierFonts.entrySet()) {
        		Font f = fontEntry.getValue();
        		
        		prefTierFonts.put(fontEntry.getKey(), new Font(f.getName(), f.getStyle(), getFontSize()));
        	}
        }
        
        if (isEditing()) {	
			TranscriptionTableCellEditor editor = (TranscriptionTableCellEditor)getCellEditor(getCurrentRow(), 
					getCurrentColumn());
			TranscriptionTableEditBox editBox = editor.getEditorComponent();
			if (editBox != null) {
				editBox.setFont(font);
			}
		}
    }
	
	/**
	 * Returns the size of the font.
	 * 
	 * @return the size of the current font
	 */
	public int getFontSize() {
		return getFont().getSize();
	}
	
	/**
	 * Returns the font used for the tier.
	 * 
	 * @param tierName tier for which the font is requested
	 * @return the preferred font for that tier 
	 */
	public Font getFontForTier(String tierName) {
		return prefTierFonts.get(tierName);
	}
	
	/**
	 * Returns the font to use for the row-index numbers in the index column.
	 * 
	 * @return the font for the index column
	 */
	public Font getIndexColumnFont() {
		if (indexColumnFont != null) {
			return indexColumnFont;
		}
		
		return getFont();
	}
	
	/**
	 * Returns the font to use for the tier name sub-headers.
	 * 
	 * @return the font for tier name labels in sub-header rows
	 */
	public Font getTierNameFont() {
		if (tierNameFont != null) {
			return tierNameFont;
		}
		
		return getFont();
	}

	/**
	 * Recalculates the height of the current row after editing was cancelled.	 
	 */
	private void recalculateCurrentRowHeight(){		
		int rowHeightValue = 0;
	 	 
		for(int i = 1; i< getColumnCount(); i++){
			 Object c  = getValueAt(getCurrentRow(), i);
			 if(c instanceof Annotation){
				 Annotation ann = (Annotation)c;
				 int h = getPreferredRowHeight(ann, getCurrentRow(), i);
				 if (h > rowHeightValue) {
					 rowHeightValue = h;
				 }
			 }
		}
		
		if (rowHeightValue > defRowHeight) {
			setRowHeight(getCurrentRow(), rowHeightValue + extraRowHeight); 	
		} else {
			setRowHeight(getCurrentRow(), defRowHeight + extraRowHeight);  
		}
		revalidate();
	}
	
	/* 
	 * Try with an in memory JTextArea. Its properties should be kept in sync 
	 * with the JTextArea of the cell editor and cell renderer. 
	 * After a call to setSize(), the textarea's getPreferredSize() seems to
	 * return an accurate required size for the (wrapped) text. 
	 */
	private int getPreferredRowHeight(Annotation ann, int row, int column) {
		textArea.setText(ann.getValue());
		Font f = getFontForTier(ann.getTier().getName());    		
		if (f != null) {
			textArea.setFont(f);
		} else {
			textArea.setFont(getFont());
		} 
		Rectangle r = getCellRect(row, column, true);
		textArea.setSize(r.width, r.height);
		
		return textArea.getPreferredSize().height;
		
		/* 
		Or use the cell renderer to provide the preferred size information. 
		This would in principle be better (less duplication) but that render performs
		more tests, sets colors etc. which is not relevant for this calculation.
		
		return getCellRenderer(row, column).getTableCellRendererComponent(this, ann, false, false, 
				row, column).getPreferredSize().height;
		*/
	}
	
	/**
	 * Recalculates the row height for all the rows,
	 * according to the content of each row.
	 */
	public void reCalculateRowHeight(){		 	
	 	// row iteration
		for (int x = 0 ; x < getRowCount(); x++) {
			reCalculateRowHeight(x);
		}
	}

	/**
	 * Recalculates the height for the specified row.
	 * 
	 * @param row the row index
	 */
	public void reCalculateRowHeight(int row){		 	
		int rowHeightValue = 0;
		
		// column iteration
		for (int i = 1; i< getColumnCount(); i++) {				 
			 Object c  = getValueAt(row, i);
			 
			 if (c instanceof Annotation) {
				 Annotation ann = (Annotation)c;
				 
				 int h = getPreferredRowHeight(ann, row, i);
				 if (h > rowHeightValue) {
					 rowHeightValue = h;
				 }			 
			 } else if (c instanceof TableSubHeaderObject) {
				 // this has to be checked and set only once per row
				 rowHeightValue = subHeaderRowHeight;
				 break;
			}
		}
		
		if (rowHeightValue > defRowHeight){
			setRowHeight(row, rowHeightValue + extraRowHeight); 	
		} else {
			setRowHeight(row, defRowHeight + extraRowHeight);  
		}		
	}
	
}
			
		  
