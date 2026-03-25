package mpi.eudico.client.annotator.lexicon.api;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that is to be used to create a Lexicon Service Client Factory from an extension.
 * Accumulates properties and parameters for a lexicon service client.
 * 
 * @author Micha Hulsbosch
 */
public class LexSrvcClntBundle {
	private String description;
	private ClassLoader loader;
	private List<Param> paramList;
	private String name;
	private URL[] javaLibs;
	private URL[] nativeLibs;
	private File baseDir;
	private String type;
	private String binaryName;
	
	/**
	 * Creates a new bundle instance.
	 */
	public LexSrvcClntBundle() {
		super();
	}
	
	/**
	 * Creates a new bundle instance.
	 * 
	 * @param name the name of the bundle
	 */	
	public LexSrvcClntBundle(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the description of the bundle.
	 * 
	 * @return the bundle description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 * 
	 * @param description the description of the bundle
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Returns the loader of this class.
	 * 
	 * @return the {@code ClassLoader}
	 */
	public ClassLoader getLoader() {
		return loader;
	}

	/**
	 * Sets the loader of this class.
	 * 
	 * @param loader the {@code ClassLoader}
	 */
	public void setLoader(ClassLoader loader) {
		this.loader = loader;
	}

	/**
	 * Returns a list of parameter objects.
	 * 
	 * @return parameter objects, can be {@code null}
	 */
	public List<Param> getParamList() {
		if (paramList == null) {
			return null;
		}
		//make a copy of the list
		ArrayList<Param> params = new ArrayList<Param>(paramList.size());
		for (Param p : paramList) {
			try {
				params.add(p.clone());
			} catch (CloneNotSupportedException cnse) {
				
			}
		}
		
		return params;
	}

	/**
	 * Sets the list of parameter objects.
	 * 
	 * @param paramList the list of parameters
	 */
	public void setParamList(List<Param> paramList) {
		this.paramList = paramList;
	}

	/**
	 * Returns the name of the client bundle.
	 * 
	 * @return the name of the bundle
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the bundle.
	 * 
	 * @param name the name of the bundle
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns an array of URL's of required Java libraries.
	 * 
	 * @return an array of URL's or {@code null}
	 */
	public URL[] getJavaLibs() {
		return javaLibs;
	}

	/**
	 * Sets the array of Java library URL's.
	 * 
	 * @param javaLibs the URL's of Java libraries
	 */
	public void setJavaLibs(URL[] javaLibs) {
		this.javaLibs = javaLibs;
	}

	/**
	 * Returns an array of the URL's of native libraries.
	 * 
	 * @return an array of the URL's or {@code null}
	 */
	public URL[] getNativeLibs() {
		return nativeLibs;
	}

	/**
	 * Sets the array of URL's of native libraries.
	 * 
	 * @param nativeLibs the URL's of native libraries
	 */
	public void setNativeLibs(URL[] nativeLibs) {
		this.nativeLibs = nativeLibs;
	}

	/**
	 * Returns the base directory of the client.
	 * 
	 * @return the base directory
	 */
	public File getBaseDir() {
		return baseDir;
	}

	/**
	 * Sets the base directory of the client.
	 * 
	 * @param baseDir the base directory
	 */
	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	/**
	 * Set the fully qualified class name of this client.
	 * 
	 * @param binaryName the fully qualified name
	 */
	public void setLexSrvcClntClass(String binaryName) {
		this.binaryName = binaryName;
	}

	/**
	 * Sets the lexicon execution type. 
	 * 
	 * @param type the lexicon execution type
	 */
	public void setLexExecutionType(String type) {
		this.type = type;
	}

	/**
	 * Returns the lexicon execution type.
	 * 
	 * @return the lexicon execution type
	 */
	public String getLexExecutionType() {
		return type;
	}
	
	/**
	 * Returns the fully qualified class name of this client.
	 * 
	 * @return the fully qualified name
	 */
	public String getLexSrvcClntClass() {
		return binaryName;
	}
}
