package mpi.eudico.client.annotator.recognizer.load;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.recognizer.data.Param;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A class that collects information and resources concerning recognizers
 * that have been detected in the extensions folder.
 * 
 * @author Han Sloetjes
 */
public class RecognizerBundle {
	private String id;
	/** a friendly name */
	private String name;
	/** the loader for this recognizer */ //?? needed?
	private ClassLoader loader;
	private String recognizerClassName;// binary name or fully qualified class name
	private String recExecutionType; // direct, local, shared.
	
	private List<Param> paramList;
	private String helpFile;
	private URL[] javaLibs;
	private URL[] nativeLibs;
	private File baseDir;
	private String iconRef;
	
	/**
	 * No-arg constructor.
	 */
	public RecognizerBundle() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param name the name of the recognizer
	 */
	public RecognizerBundle(String name) {
		super();
		this.name = name;
	}

	/**
	 * Returns the name of the recognizer.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the recognizer.
	 * 
	 * @param name the name of the recognizer
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the {@code ClassLoader} of this bundle.
	 * 
	 * @return the {@code ClassLoader}
	 */
	public ClassLoader getLoader() {
		return loader;
	}

	/**
	 * Sets the {@code ClassLoader} of this bundle.
	 * 
	 * @param loader the class loader
	 */
	public void setLoader(ClassLoader loader) {
		this.loader = loader;
	}

	/**
	 * Creates and returns a copy of the list of parameters.
	 * 
	 * @return a copy of the parameter list or {@code null}
	 */
	public List<Param> getParamList() {
		if (paramList == null) {
			return null;
		}
		//make a copy of the list
		ArrayList<Param> params = new ArrayList<Param>(paramList.size());
		for (Param p : paramList) {
			try {
			params.add((Param) p.clone());
			} catch (CloneNotSupportedException cnse) {
				
			}
		}
		
		return params;
	} 
	/**
	 * Sets the list of parameters.
	 * 
	 * @param paramList the parameter list
	 */
	public void setParamList(List<Param> paramList) {
		this.paramList = paramList;
	}
	
	/**
	 * Sets the help file name or path.
	 * 
	 * @param file the help name or path
	 */
	public void setHelpFile(String file) {		
		this.helpFile = file;		
	}
	
	/**
	 * Get an URL for the help file (properly quoted if it contains "weird" characters).
	 * If relative, it is resolved to the base directory.
	 * 
	 * @return the URL of the help file or {@code null}
	 */
	public String getHelpFile() {
		URL url = getURL(helpFile);
		if (url != null) {
			return url.toString();
		}
		return null;
	}

	/**
	 * Returns the id of the recognizer.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id of the recognizer.
	 * 
	 * @param id the recognizer id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the fully qualified name of the recognizer {@code Class}.
	 * 
	 * @return the qualified class name
	 */
	public String getRecognizerClass() {
		return recognizerClassName;
	}

	/**
	 * Sets the fully qualified class name of the recognizer.
	 * 
	 * @param recognizerClassName the qualified class name
	 */
	public void setRecognizerClass(String recognizerClassName) {
		this.recognizerClassName = recognizerClassName;
	}

	/**
	 * Returns an array of Java libraries.
	 * 
	 * @return array of Java libraries or {@code null}
	 */
	public URL[] getJavaLibs() {
		return javaLibs;
	}

	/**
	 * Sets the Java libraries of the recognizer.
	 * 
	 * @param javaLibs required Java libraries
	 */
	public void setJavaLibs(URL[] javaLibs) {
		this.javaLibs = javaLibs;
	}

	/**
	 * Returns an array of native libraries.
	 * 
	 * @return array of native libraries or {@code null}
	 */
	public URL[] getNativeLibs() {
		return nativeLibs;
	}

	/**
	 * Sets the native libraries for the recognizer.
	 * 
	 * @param nativeLibs the native libraries
	 */
	public void setNativeLibs(URL[] nativeLibs) {
		this.nativeLibs = nativeLibs;
	}

	/**
	 * Returns the execution type of the recognizer.
	 * 
	 * @return the execution type
	 */
	public String getRecExecutionType() {
		return recExecutionType;
	}

	/**
	 * Sets the execution type of this recognizer.
	 * 
	 * @param recExecutionType the execution type
	 */
	public void setRecExecutionType(String recExecutionType) {
		this.recExecutionType = recExecutionType;
	}

	/**
	 * Returns the base directory of the recognizer process.
	 * 
	 * @return the base directory
	 */
	public File getBaseDir() {
		return baseDir;
	}

	/**
	 * Sets the base directory for the recognizer process.
	 * 
	 * @param baseDir the base directory
	 */
	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	/**
	 * Sets a reference to the recognizer's icon.
	 * 
	 * @param iconRef the icon reference
	 */
	public void setIconRef(String iconRef) {
		this.iconRef = iconRef;
	}
	
	/**
	 * Returns the icon reference.
	 * 
	 * @return the icon reference
	 */
	public String getIconRef() {
		return this.iconRef;
	}

	/**
	 * Get an URL for the icon (properly quoted if it contains "weird" characters).
	 * If relative, it is resolved to the base directory.
	 * 
	 * @return the URL of an icon or {@code null}
	 */
	public URL getIconURL() {
		return getURL(iconRef);
	}

	/**
	 * Takes a potentially relative URL and resolves it relative
	 * to the directory of the .cmdi file. If the given URL is
	 * absolute, it can be of a different scheme, such as http:. 
	 * The URL properly quotes "weird" characters in the path.
	 * 
	 * @param relative the path to resolve
	 * @return the resolved URL, could be file: or some other protocol such as http:
	 */
	private URL getURL(String relative) {
		if (relative != null) {
			try {
				String path = getBaseDir().getAbsolutePath();
				// on Windows the path contains back slashes 
				// replace by forward slashes to prevent a URISyntaxException 
				path = path.replace('\\', '/');
				if (!path.endsWith("/")) {
					path = path + '/';
				}
				// to prevent a URISyntaxException. If the path does not start with '/' it is considered to be relative
				if (path.charAt(0) != '/') {
					path = '/' + path;
				}
				// This constructor actually properly quotes the path!
				//                  scheme, host, path, fragment
				URI base = new URI("file", null,  path, null);
				// Note that new URL(protocol, host, port, path) does not quote the path.
				URI icon = base.resolve(relative);
				return icon.toURL();
			} catch (URISyntaxException e) {
				LOG.warning("URISyntaxException: " + e.getMessage());
			} catch (MalformedURLException e) {
				LOG.warning("MalformedURLException: " + e.getMessage());
			} catch (IllegalArgumentException e) {
				LOG.warning("IllegalArgumentException: " + e.getMessage());
			}
		}
		return null;
	}
}
