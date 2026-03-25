package mpi.search.content.result.model;

/**
 * An abstract class for content matches or "hits".
 *
 * @author klasal
 */
@SuppressWarnings("serial")
public abstract class AbstractContentMatch implements ContentMatch {
	/** the file name variable  */
    protected String fileName = "";
    /** left context  */
    protected String leftContext = "";
    /** right context  */
    protected String rightContext = "";
    //add parent and children context mod. Coralie Villes
    /** parent context  */
    protected String parentContext="";
    /** the children context */
    protected String childrenContext="";
    /** the tier name variable  */
    protected String tierName = "";
    /** matched substring indices array */
    protected int[][] matchedSubstringIndices;
    /** index within the tier  */
    protected int indexWithinTier = -1;
    
    /** the begin time */
    protected long beginTime;
    
    /** the end Time */
    protected long endTime;

    /**
     * Creates a new content match instance.
     */
    public AbstractContentMatch() {
		super();
	}

	@Override
	public long getBeginTimeBoundary() {
        return beginTime;
    }

    @Override
	public long getEndTimeBoundary() {
        return endTime;
    }

    @Override
	public String getFileName() {
        return fileName;
    }

    /**
     * Sets the index of the match.
     * 
     * @param i the index of a match (annotation) in the tier
     */
    public void setIndex(int i) {
        indexWithinTier = i;
    }

    /** 
     * @return the index of the match in the tier
     */
    @Override
	public int getIndex() {
        return indexWithinTier;
    }

    @Override
	public String getLeftContext() {
        return leftContext;
    }

    @Override
	public int[][] getMatchedSubstringIndices() {
        return matchedSubstringIndices;
    }

    @Override
	public String getRightContext() {
        return rightContext;
    }

    @Override
	public String getTierName() {
        return tierName;
    }
}
