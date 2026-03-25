package mpi.eudico.client.annotator.prefs;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that maps old preference key values to new key values and vice versa.
 * Introduced with the transition to an xml based preferences storage.
 * (ELAN 3.2)
 * 
 * @author Han Sloetjes
 * @version 1.0
 */
public class PrefKeyMapper {
	/** maps old keys to new keys */
	public static final Map<String, String> keyMapper;
	
	/**
	 * Private constructor.
	 */
	private PrefKeyMapper() {
		super();
	}

	static {
		Map<String, String> tempKeyMapper = new HashMap<String, String>(20);
		tempKeyMapper.put("GridMultiMode", "GridViewer.MultiTierMode");
		tempKeyMapper.put("GridTierName", "GridViewer.TierName");
		tempKeyMapper.put("GridFontSize", "GridViewer.FontSize");
		tempKeyMapper.put("TextTierName", "TextViewer.TierName");
		tempKeyMapper.put("TextFontSize", "TextViewer.FontSize");
		tempKeyMapper.put("TextDotSeparated", "TextViewer.DotSeparated");
		tempKeyMapper.put("TextCenterVertical", "TextViewer.CenterVertical");
		tempKeyMapper.put("SubTitleTierName", "SubTitleViewer.TierName-"); // 1 - x
		tempKeyMapper.put("SubTitleFontSize", "SubTitleViewer.FontSize-"); // 1 - x
		tempKeyMapper.put("TimeLineFontSize", "TimeLineViewer.FontSize");
		tempKeyMapper.put("InterlinearFontSize", "InterlinearViewer.FontSize");
		tempKeyMapper.put("TimeSeriesNumPanels", "TimeSeriesViewer.NumPanels"); 
		tempKeyMapper.put("SelectedTabIndex", "LayoutManager.SelectedTabIndex");
		tempKeyMapper.put("VisibleMultiTierViewer", "LayoutManager.VisibleMultiTierViewer");
		tempKeyMapper.put("TierSortingMode", "MultiTierViewer.TierSortingMode");
		tempKeyMapper.put("ActiveTierName", "MultiTierViewer.ActiveTierName");		
		tempKeyMapper.put("TierOrder", "MultiTierViewer.TierOrder");
		tempKeyMapper.put("TimeSeriesNumPanels", "TimeSeriesViewer.NumberOfPanels");
		tempKeyMapper.put("SbxMarkerDir", "LastUsedShoeboxMarkerDir");
		//TimeSeriesPanelMap -> TimeSeriesViewer.Panel-x
		keyMapper = Map.copyOf(tempKeyMapper);
	}
}
