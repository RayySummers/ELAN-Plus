package mpi.eudico.client.annotator.tier;

/**
 * Minimal data structure for storing tier settings from a 5 column table.
 * A tier name and 4 boolean values, the first one usually representing the
 * selected state.
 */
public class TierExportSetting {
	/** the name of the tier */
	public String tierName;
	/** first column, usually the selected state */
	public boolean c1;
	/** second boolean value */
	public boolean c2;
	/** third boolean value */
	public boolean c3;
	/** fourth boolean value */
	public boolean c4;
	
	/**
	 * Constructor.
	 * @param tierName the name of the tier
	 * @param c1 flag 1
	 * @param c2 flag 2
	 * @param c3 flag 3
	 * @param c4 flag 4
	 */
	public TierExportSetting(String tierName, boolean c1, boolean c2, boolean c3, boolean c4) {
		super();
		this.tierName = tierName;
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
		this.c4 = c4;
	}
	
}
