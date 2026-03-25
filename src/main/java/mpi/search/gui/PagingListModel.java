package mpi.search.gui;

import java.util.List;

import javax.swing.AbstractListModel;

import mpi.search.content.result.model.ContentMatch;

/**
 * Model that allows paging through list data, 
 * corresponds to the Paging(Table)Model of O'Reilly book JavaSwing
 *
 * @author klasal
 */
@SuppressWarnings("serial")
public class PagingListModel extends AbstractListModel {
	/** a default page size */
    public static final int DEFAULT_PAGE_SIZE = 50;
    /** the list of matches */
    protected List<ContentMatch> data;
    /** the page offset */
    protected int pageOffset = 0;
    /** the page size */
    protected int pageSize = DEFAULT_PAGE_SIZE;

    /**
     * Creates a new model instance.
     */
    public PagingListModel() {
		super();
	}

	// Work only on the visible part of the table.
    @Override
	public Object getElementAt(int index) {
    		//dummy exception if attempt to get data beyond viewable part
        if (index >= pageSize) {
            throw new IndexOutOfBoundsException("Index "+ index + ", viewable Size "+ pageSize);
        }

        int realIndex = index + (pageOffset * pageSize);

        return data.get(realIndex);
    }

    /**
     * Returns the index of the first visible match of the page.
     * 
     * @return real index of first entry in viewable page
     */
    public int getFirstShownRealIndex() {
        return (data.size() == 0) ? (-1) : (pageOffset * pageSize);
    }

    /**
     * Returns the index of the last visible match of the page.
     * 
     * @return real index of last entry in viewable page
     */
    public int getLastShownRealIndex() {
        return ((pageOffset * pageSize) + getSize()) - 1;
    }

    /**
     * Returns the number of pages needed to cover the data (the total number
     * of matches divided by the size of a page).
     * 
     * @return number of pages needed to cover the data
     */
    public int getPageCount() {
        return (int) Math.ceil((double) getRealSize() / pageSize);
    }

    /**
     * Returns the page offset.
     * 
     * @return actual page number that is accessible
     */
    public int getPageOffset() {
        return pageOffset;
    }

    /**
     * Sets the size of the viewable part.
     *
     * @param s the new page size
     */
    public void setPageSize(int s) {
        if (s == pageSize) {
            return;
        }

        int oldPageSize = pageSize;
        pageSize = s;
        pageOffset = (oldPageSize * pageOffset) / pageSize;

        fireContentsChanged(this, 0, getSize());
    }

    /**
     * Returns the page size.
     * 
     * @return page size (maximum size of viewable data)
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Returns the total number of matches in the list.
     * 
     * @return size of total data
     */
    // Use this method if you want to know how big the real list is . . . we
    // could also write "getRealValueAt()" if needed.
    public int getRealSize() {
        return data.size();
    }

    /**
     * Returns the number of matches of the page.
     * 
     * @return size of viewable part
     */
    @Override
	public int getSize() {
        return Math.min(pageSize, data.size() - (pageOffset * pageSize));
    }

    /**
     * Update the page offset and fire a data changed.
     */
    public void pageDown() {
        if (pageOffset > 0) {
            pageOffset--;
            fireContentsChanged(this, 0, getSize());
        }
    }

    /**
     * Update the page offset and fire a data changed.
     */
    public void pageUp() {
        if (pageOffset < (getPageCount() - 1)) {
            pageOffset++;
            fireContentsChanged(this, 0, getSize());
        }
    }
}
