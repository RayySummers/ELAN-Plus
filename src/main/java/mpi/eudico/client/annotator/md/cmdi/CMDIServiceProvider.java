package mpi.eudico.client.annotator.md.cmdi;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.md.imdi.MDKVData;
import mpi.eudico.client.annotator.md.spi.MDConfigurationPanel;
import mpi.eudico.client.annotator.md.spi.MDContentLanguageUser;
import mpi.eudico.client.annotator.md.spi.MDServiceProvider;
import mpi.eudico.client.annotator.md.spi.MDViewerComponent;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import nl.mpi.util.FileExtension;

/**
 * A metadata service provider for visualization of CMDI metadata.
 * Uses the MPI-TLA CMDIApi.
 * Note: as a result of the current behavior of the MDServiceRegistry a provider is never 
 * re-used for another file of the same type. In principle this class is ready for re-use
 * but this might need checking if the registry behavior changes. 
 * <p>
 * 03-2024: the dependency on the MPI-TLA metadata-api library has been removed,
 * it is now always a 'basic' metadata tree which is created. The additional
 * functionality, like localized description, depended on a data category registry
 * which is no longer supported. This provider no longer implements 
 * {@link MDContentLanguageUser}.
 *  
 * @author Han Sloetjes
 */
public class CMDIServiceProvider implements MDServiceProvider {
	private String sourcePath;
	/** for offline mode, loads the xml and creates a tree of the first Component */
	private CMDIDom simpleDom;
	private CMDIViewerPanel viewerPanel;
	private DefaultMutableTreeNode rootTreeNode;
	private boolean isLoading = false;
	private final ReentrantLock treeLock = new ReentrantLock();
	// concept registry related
	private final String HANDLE = "hdl.handle.net";
	private final Pattern HANDLE_PAT;
	
	/**
	 * Constructor
	 */
	public CMDIServiceProvider() {
		super();
		HANDLE_PAT = Pattern.compile("CCR_C-");
	}

	/**
	 * Checks the extension of the specified file or URL.
	 *  
	 * @see mpi.eudico.client.annotator.md.spi.MDServiceProvider#setMetadataFile(java.lang.String)
	 * 
	 * @param filePath a path to a local file or the URI of a metadata file 
	 * @return true if the specified file seems to be a CMDI file
	 */
	@Override
	public boolean setMetadataFile(String filePath) {
        if (filePath == null) {
            return false;
        }
        
        File f = new File(filePath);

        if (!f.exists()) {

            LOG.warning("The CMDI metadata file does not exist: " +
                filePath);
            return false;
        }
        
        if (!f.canRead()) {
            LOG.warning("The CMDI metadata file cannot be read: " +
                filePath);

            return false;
        }

        if (f.isDirectory()) {
            LOG.warning("The path is a directory not a CMDI file: " + filePath);

            return false;
        }

        if (f.length() == 0) {
            LOG.warning("The CMDI metadata file has zero length: " +
                filePath);

            return false;
        }
        
        // check extension
        boolean extFound = false;
        String lowerPath = filePath.toLowerCase();       
        
        for (String ext : FileExtension.CMDI_EXT) {
        	if (lowerPath.endsWith(ext)) {
        		extFound = true;
        		break;
        	}
        }
        
        if (extFound) {
        	// perform a quick test for <CMD ?? to see if it really is cmdi?
        	sourcePath = filePath;
        	// set rootTreeNode to null so that a tree will be created when necessary
        	rootTreeNode = null;// or update the panel immediately?
        	return true;
        } else if (lowerPath.endsWith(FileExtension.XML_EXT[0])) {
        	// check if it is a DC (oai_dc:dc) xml file
        	try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
        		for (int i = 0; i < 10; i++) {
        			String line = reader.readLine();
        			if (line != null && line.indexOf("<oai_dc:dc") >= 0 || line.indexOf("<dc:") >= 0) {
        	        	sourcePath = filePath;
        	        	// set rootTreeNode to null so that a tree will be created when necessary
        	        	rootTreeNode = null;
        	        	return true;
        			}
        		}
        	} catch (IOException ioe) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "Error while trying to read from an XML metadata file: " + filePath);
				}
			}
        }
        
		return false;
	}

	/**
	 * @return the path to CMDI metadata file or null if not specified
	 */
	@Override
	public String getMetadataFile() {
		return sourcePath;
	}

	/**
	 * @return the description of the format, CMDI
	 */
	@Override
	public String getMDFormatDescription() {
		return "CMDI";
	}

	/**
	 * @return the value for the specified key or null if key was null or was not in the document
	 */
	@Override
	public String getValue(String key) {
		return null;
	}

	/**
	 * @return a list of values for the specified key or null if key is null or was not found
	 */
	@Override
	public List<String> getValues(String key) { 
		return null;
	}

	/**
	 * 
	 * @return a list of all metadata keys or null if no metadata were found or recognized.
	 */
	@Override
	public List<String> getKeys() {
		return null;
	}

	/**
	 * 
	 * @return a list of selected keys or null if no keys are selected (i.e. all keys are used)
	 */
	@Override
	public List<String> getSelectedKeys() {
		return null;
	}

	/**
	 * Sets the selected keys. Only if the provider is configurable.
	 * 
	 * @param selectedKeys a list of selected keys
	 */
	@Override
	public void setSelectedKeys(List<String> selectedKeys) {
		// stub
	}

	/**
	 * Returns a map of selected keys and their values. If nothing can be configured it returns all keys.
	 * 
	 * @return a map of selected keys and their values
	 */
	@Override
	public Map<String, String> getSelectedKeysAndValues() {
		return null;
	}
	
	/**
	 *  Returns the root node of a tree representation of the CMDI.
	 *  
	 * @return the root of a tree representation of the CMDI
	 */
	public DefaultMutableTreeNode getAsTree() {
		if (isLoading) {
			return new DefaultMutableTreeNode(ElanLocale.getString("MetadataViewer.Loading.CMDI"));
		}
		
		if (simpleDom != null) {
			if (rootTreeNode != null) {
				return rootTreeNode;
			}
			treeLock.lock();
			try {
				rootTreeNode = simpleDom.getAsTree();
			} finally {
				treeLock.unlock();
			}
			
			return rootTreeNode;
		}
		
		return null;
	}

	/**
	 * Returns false for the time being.
	 * 
	 * @return {@code false} for now
	 */
	@Override
	public boolean isConfigurable() {
		return false;
	}

	/**
	 * @return {@code null} for the time being
	 */
	@Override
	public MDConfigurationPanel getConfigurationPanel() {
		return null;
	}

	/**
	 * @return a {@link CMDIViewerPanel} with the default representation of the metadata as a tree
	 */
	@Override
	public MDViewerComponent getMDViewerComponent() {
		if (viewerPanel == null) {
			viewerPanel = new CMDIViewerPanel(this);
		}
		return viewerPanel;
	}

	/**
	 * Initializes the CMDI API.
	 * Currently the API throws an exception when used offline (the schema cannot be accessed).
	 * In that case the XML is read "as is" and switching language has no effect.
	 * 
	 *  Since loading can take a considerable time try to do it in a separate thread
	 */
	@Override
	public void initialize() {
		if (sourcePath == null) {
			return;
		}
		
		isLoading = true;
		Thread loadThread = new Thread(new CMDILoader());
		loadThread.setPriority(Thread.MIN_PRIORITY);
		loadThread.start();
	}
	
	/**
	 * Notification of background loading of CMDI document. 
	 */
	private void delayedLoadingComplete() {
		isLoading = false;
		if (viewerPanel != null) {
			EventQueue.invokeLater(new PanelUpdater());
		}
	}
	
	// copied from ImdiDoc
	@SuppressWarnings("unused")
    private MDKVData getDataObject(String key, String value, String datCatID, String valueDatCatID) {
    	if (value != null) {
    		value = value.trim();
    		if (value.length() == 1 && value.charAt(0) == '\n') {
    			value = null;
    		}
    	}

    	datCatID = handleToDCId(datCatID);
    	valueDatCatID = handleToDCId(valueDatCatID);
    	
    	return new CMDIKVData(key, value, datCatID, valueDatCatID);
    }
    
    /**
     * The CLARIN Component Registry switched from ISOcat DC id's to Concept Registry handle uri's.
     * The ID of converted data categories is part of the handle production. 
     * This method tries to extract the ISOcat ID (until there is support for the OpenSKOS handles.
     * Maybe more check are needed?
     * @param ccrHandle the handle URL (http://hdl.handle.net/11459/CCR_C-4146_5ccc45c8-d729-c180-2bf1-fccc56dde24d)
     * @return the ISOcat 4 letter code if present
     */
    private String handleToDCId(String ccrHandle) {
    	if (ccrHandle != null && ccrHandle.indexOf(HANDLE) > -1) {
    		String[] parts = HANDLE_PAT.split(ccrHandle);
    		if (parts.length >= 2) {
    			if (parts[1].length() >= 5) {
    				return parts[1].substring(0, 4);
    			}
    		}
    	}
    	
    	return ccrHandle;
    }
	
	/**
	 * A Runnable for background loading of the metadata file. 
	 * 
	 * @author Han Sloetjes
	 */
	private class CMDILoader implements Runnable {

		@Override
		public void run() {
			URL cmdiUrl = null;

			try {							
				File f = new File(sourcePath);
				if (f.exists()) {
					// a local file
					URI fUri = f.toURI();
					cmdiUrl = fUri.toURL();
				} else {// not very useful at the moment although a user might edit an eaf to point to an online cmdi?
					cmdiUrl = new URI(sourcePath).toURL();
				}

			} catch(MalformedURLException mue) {
				// log
	            LOG.warning("Not a valid cmdi url: " +
	                    sourcePath);
	            CMDIServiceProvider.this.delayedLoadingComplete();
	            return;
			} catch (Throwable th) {
	            LOG.warning("Could not create an url: " +
	                    sourcePath);
	            CMDIServiceProvider.this.delayedLoadingComplete();
	            return;
			}
			treeLock.lock();
			
			try {
				simpleDom = new CMDIDom(cmdiUrl);		
			} catch (IOException ioe) {
				LOG.warning("CMDI DOM exception: " + ioe.getMessage());
			} finally {
				treeLock.unlock();
			}
			
			CMDIServiceProvider.this.delayedLoadingComplete();
		}		
	}
	
	/**
	 * A Runnable to update the tree UI on the EventQueue.
	 */
	private class PanelUpdater implements Runnable {

		@Override
		public void run() {
			if (CMDIServiceProvider.this.viewerPanel != null) {
				CMDIServiceProvider.this.viewerPanel.reinitializeTree();
			}			
		}
	}
}
