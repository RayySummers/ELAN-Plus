package mpi.eudico.client.annotator.interlinear.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import mpi.eudico.util.ExtClassLoader;
import nl.mpi.lexan.analyzers.TextAnalyzer;
import nl.mpi.lexan.analyzers.helpers.Information;

/**
 * A registry which discovers and loads text analyzer extensions.
 */
public class TextAnalyzerRegistry {
    private static TextAnalyzerRegistry analyzerRegistry;
    private final Logger LOG = Logger.getLogger("ClientLogger");

	private List<TextAnalyzer> instantiatedAnalyzers;// analyzers as discovered on the class path
	private List<Class<? extends TextAnalyzer>> analyzersClassList;
	private List<Information> informationList;
	private Map<Information, Class<? extends TextAnalyzer>> info2analyzerClass;
	private Map<Information, TextAnalyzer> info2Analyzer;
	/** maintain a map per document */
	private Map<String, Map<Information, TextAnalyzer>> doc2AnalyzerMap;
	//private Map<String, Map<AnalyzerConfig, TextAnalyzer>> doc2AnalyzerConfigMap;
	private Map<String, Map<String, TextAnalyzer>> doc2AnalyzerConfigIdMap;

	/**
	 * Private constructor.
	 */
	private TextAnalyzerRegistry() {
		super();
		instantiatedAnalyzers = new ArrayList<TextAnalyzer>();
		analyzersClassList = new ArrayList<Class<? extends TextAnalyzer>>();
		informationList = new ArrayList<Information>();
		info2analyzerClass = new HashMap<Information, Class<? extends TextAnalyzer>>();
		info2Analyzer = new HashMap<Information, TextAnalyzer>();
		doc2AnalyzerMap = new HashMap<String, Map<Information, TextAnalyzer>>();
		// doc2AnalyzerConfigMap = new HashMap<String, Map<AnalyzerConfig, TextAnalyzer>>();
		doc2AnalyzerConfigIdMap = new HashMap<String, Map<String, TextAnalyzer>>();
		initLoad();
	}

    /**
     * Returns the single instance of the registry.
     *
     * @return the single instance of the registry
     */
    public static TextAnalyzerRegistry getInstance() {
    	if (analyzerRegistry == null) {
    		analyzerRegistry = new TextAnalyzerRegistry();
    	}

    	return analyzerRegistry;
    }

    private void initLoad() {
     	List<TextAnalyzer> instList = ExtClassLoader.getInstance().getInstanceList(TextAnalyzer.class);
    	if (instList != null) {

    		for (TextAnalyzer ta : instList) {
    			instantiatedAnalyzers.add(ta);
    			analyzersClassList.add(ta.getClass());
    			// share, instead of duplicate
    			final Information information = ta.getInformation();
				informationList.add(information);
    			info2analyzerClass.put(information, ta.getClass());
    			info2Analyzer.put(information, ta);
    		}
    	}
    }

    /**
     * Returns a list of registered {@code TextAnalyzer}s.
     *
     * @return a list of all instantiated analyzers
     */
    public List<TextAnalyzer> getTextAnalyzers() {
    	return instantiatedAnalyzers;
    }

    /**
     * Returns a list of registered analyzer {@code Information} objects.
     *
     * @return a list of the analyzer information objects of all detected
     * analyzers
     */
    public List<Information> getAnalyzersInfo() {
    	return informationList;
    }

    /**
     * Creates and returns a new instance of the analyzer identified by the
     * specified Information object.
     *
     * @param info the information object
     *
     * @return a new instance of the analyzer
     */
    public TextAnalyzer getAnalyzerInstance(Information info) {
    	TextAnalyzer ta = info2Analyzer.get(info);
    	if (ta != null) {
    		return ta;
    	}

    	// Should not get here, since all classes are already instantiated...
        Class<? extends TextAnalyzer> taClass = info2analyzerClass.get(info);
        if (taClass != null) {
        	ta = ExtClassLoader.getInstance().createInstance(taClass);
        	info2Analyzer.put(info, ta);
        }

    	return ta;
    }

    /**
     * Collects all instances of a specific analyzer and set up for a specific
     * configuration, regardless of the document it is created for.
     *
     * @param info the information object of an analyzer
     * @param configKey the id of a configuration, can be {@code null}
     * @return a list of all instances of the analyzer identified by {@code info}
     * and created for the configuration {@code configKey} (which can be
     * {@code null}). Returns {@code null} if {@code info} is null.
     */
    public List<TextAnalyzer> getAnalyzersForConfig(Information info, String configKey) {
    	if (info == null) {
    		return null;//new ArrayList<TextAnalyzer>(0)?
    	}
    	List<TextAnalyzer> analyzers = new ArrayList<TextAnalyzer>();

    	for (Map<String, TextAnalyzer> analyzerMap : doc2AnalyzerConfigIdMap.values()) {
    		TextAnalyzer ta = analyzerMap.get(configKey);
    		if (ta != null && info.equals(ta.getInformation())) {
    			analyzers.add(ta);
    		}
    	}

    	return analyzers;
    }

    /**
     * Create and or retrieve an analyzer for a specific document(window).
     * This way each document has its own instance of an analyzer. An alternative could be
     * to have only one instance per analyzer and have multiple settings panels etc.
     * communicate with that one instance.
     *
     * @param documentId the URN of an annotation document
     * @param info the information object of an analyzer
     *
     * @return the instance of the analyzer for that document or a new instance
     */
    public TextAnalyzer getAnalyzerForDoc(String documentId, Information info) {
    	return getAnalyzerForDocAndConfig(documentId, info, null);
    	/*
    	if (documentId == null || info == null) {
    		return null;
    	}
    	TextAnalyzer ta = null;
    	Map<Information, TextAnalyzer> mapForDoc = doc2AnalyzerMap.get(documentId);

    	if (mapForDoc == null) {
    		mapForDoc = new HashMap<Information, TextAnalyzer>();
    		doc2AnalyzerMap.put(documentId, mapForDoc);
    	}

    	ta = mapForDoc.get(info);

    	if (ta == null) {
            Class<? extends TextAnalyzer> taClass = info2analyzerClass.get(info);
            if (taClass != null) {
            	ta = ExtClassLoader.getInstance().createInstance(taClass);
            	mapForDoc.put(info, ta);
            }
		}

    	return ta;
    	*/
    }

    /**
     * Create and or retrieve an analyzer for a specific document(window).
     * This way each document has its own instance of an analyzer. An alternative could be
     * to have only one instance per analyzer and have multiple settings panels etc.
     * communicate with that one instance.
     *
     * @param documentId the URN of an annotation document
     * @param info the information object of an analyzer
     * @param configId the id or key for a specific configuration, {@code null}
     * indicates the global, generic configuration of the analyzer
     *
     * @return the instance of the analyzer for that document or a new instance
     */
    public TextAnalyzer getAnalyzerForDocAndConfig(String documentId, Information info,
    		String configId) {
    	if (documentId == null || info == null) {
    		return null;
    	}
    	TextAnalyzer ta = null;
    	Map<String, TextAnalyzer> mapForDocAndConfigId = doc2AnalyzerConfigIdMap.get(documentId);

    	if (mapForDocAndConfigId == null) {
    		mapForDocAndConfigId = new HashMap<String, TextAnalyzer>();
    		doc2AnalyzerConfigIdMap.put(documentId, mapForDocAndConfigId);
    	}

    	ta = mapForDocAndConfigId.get(configId);

    	if (ta == null) {
            Class<? extends TextAnalyzer> taClass = info2analyzerClass.get(info);
            if (taClass != null) {
            	ta = ExtClassLoader.getInstance().createInstance(taClass);
            	mapForDocAndConfigId.put(configId, ta);
            }
		}

    	return ta;
    }

    /**
     * Retrieves an/or creates an analyzer for the specified document
     * and source-target configuration. Source-target configurations are
     * equal if the source are the same and the targets are the same and in
     * the same order.
     *
     * @param documentId the URN of an annotation document
     * @param config configuration object containing the analyzer and the
     * source-target mapping
     * @return an instance of the analyzer for that document and configuration.
     * It is created if not there yet. Might return null also.
     */
    /*
    public TextAnalyzer getAnalyzerForDocAndConfig(String documentId,
    		AnalyzerConfig config) {
    	if (documentId == null || config == null) {
    		return null;
    	}

    	TextAnalyzer ta = null;
    	Map<AnalyzerConfig, TextAnalyzer> mapForDocAndConfig = doc2AnalyzerConfigMap.get(documentId);

    	if (mapForDocAndConfig == null) {
    		mapForDocAndConfig = new HashMap<AnalyzerConfig, TextAnalyzer>();
    		doc2AnalyzerConfigMap.put(documentId, mapForDocAndConfig);
    	}

    	ta = mapForDocAndConfig.get(config);

    	if (ta == null) {
            Class<? extends TextAnalyzer> taClass = info2analyzerClass.get(config.getAnnotId());
            if (taClass != null) {
            	ta = ExtClassLoader.getInstance().createInstance(taClass);
            	mapForDocAndConfig.put(config, ta);
            }
    	}

    	return ta;
    }
    */

    /**
     * Removes the specified analyzer from the map for the specified document.
     *
     * @param documentId the URN of the document to unload
     * @param config the AnalyzerConfig object of the analyzer and source-
     * target combination
     */
    /*
    public void removeAnalyzerForDocAndConfig(String documentId, AnalyzerConfig config) {
    	Map<AnalyzerConfig, TextAnalyzer> mapForDocAndConfig = doc2AnalyzerConfigMap.remove(documentId);
    	if (mapForDocAndConfig != null) {
    		TextAnalyzer ta = mapForDocAndConfig.remove(config);
    		if (ta != null) {
    			// could log...
    			// unloading of the analyzer is done elsewhere
    		}
    	}
    }
    */
    /**
     * Removes the specified analyzer from the map for the specified document.
     *
     * @param documentId the URN of the document to unload
     * @param info the Information object of the analyzer
     */
    public void removeAnalyzerForDoc(String documentId, Information info) {
    	removeAnalyzerForDocAndConfig(documentId, info, null);
    	/*
    	Map<Information, TextAnalyzer> mapForDoc = doc2AnalyzerMap.remove(documentId);
    	if (mapForDoc != null) {
    		TextAnalyzer ta = mapForDoc.remove(info);
    		if (ta != null) {
    			// could log...
    			// unloading of the analyzer is done elsewhere
    		}
    	}
    	*/
    }

    /**
     * Removes the specified analyzer from the map for the specified document.
     *
     * @param documentId the URN of the document to unload
     * @param info the Information object of the analyzer
     * @param configId the id or key for a specific configuration, can be {@code null}
     */
    public void removeAnalyzerForDocAndConfig(String documentId, Information info,
    		String configId) {
    	Map<String, TextAnalyzer> mapForDocAndConfigId = doc2AnalyzerConfigIdMap.get(documentId);

    	if (mapForDocAndConfigId != null) {
    		TextAnalyzer ta = mapForDocAndConfigId.remove(configId);
    		if (ta != null) {
    			// could log...
    			// unloading of the analyzer is done elsewhere
    		}
    		if (mapForDocAndConfigId.size() == 0) {
    			doc2AnalyzerConfigIdMap.remove(documentId);
    		}
    	}
    }

    /**
     * Removes the analyzer map for the specified document, e.g. when the document is closed.
     *
     * @param documentId the URN of the document to unload
     */
    public void removeAnalyzersForDoc (String documentId) {
    	Map<Information, TextAnalyzer> mapForDoc = doc2AnalyzerMap.remove(documentId);
    	if (mapForDoc != null) {
    		mapForDoc.clear();
    	}
    }

    // etc...??
}
