package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.abstr.ParserFactory;
import nl.mpi.util.FileUtility;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A class with a single method that returns the current ACM TranscriptionStore.
 * To be used when no specific version is required, it returns the latest version.
 * <br>
 * This way there will be a single location to be changed when a new version of 
 * the transcription store becomes available.
 *  
 * @author Han Sloetjes
 * @version 1.0
  */
public class ACMTranscriptionStore {
    /**
     * Creates a new ACMTranscriptionStore instance
     */
    private ACMTranscriptionStore() {
        // not to be instantiated
    }

    /**
     * Returns the current version of ACM Transcription Store.
     * Note: this methods creates a new instance of the transcription store 
     * for each call
     *
     * @return the current version of the ACM Transcription Store
     */
    public static final TranscriptionStore getCurrentTranscriptionStore() {
        return new ACM30TranscriptionStore();
    }
    
    /**
     * Returns the current (latest) parser for .eaf files.
     * 
     * @return the current (latest) parser for .eaf files
     */
    public static final Parser getCurrentEAFParser() {
    	return new EAF30Parser();
    }
    
    /**
     * The entity resolver is used to determine which local xsd to use for parsing
     * 
     * @return the current (latest) entity resolver
     */
    public static final EntityResolver getCurrentEAFResolver() {
    	//return new EAF30Parser.EAFResolver();
    	return null;
    }
    
    /**
     * Returns the path to the current (latest) local version of the EAF schema.
     * Local means the location in the source tree.
     * 
     * @return the path to the current EAF schema
     */
    public static final String getCurrentEAFSchemaLocal() {
    	return EAF30.EAF30_SCHEMA_RESOURCE;
    }
    
    /**
     * Returns the path to the current (latest) remote version of the EAF schema.
     * Remote means the (official) URL of the EAF schema.
     * 
     * @return the URL (as a string) to the current EAF schema
     */
    public static final String getCurrentEAFSchemaRemote() {
    	return EAF30.EAF30_SCHEMA_LOCATION;
    }

	/**
	 * Check the barest minimum from the file in order to
	 * find the format version number.
	 * 
	 * @param trPathName the path to the transcription
	 * @return ParserFactory.EAF27, or .EAF28, or newer.
	 */
    public static int eafFileFormatTaster(String trPathName) {
    	return eafFileFormatTaster(trPathName, true, true);
    }
    
	/**
	 * Check the barest minimum from the file in order to
	 * find the format version number.
	 * 
	 * @param trPathName the path to the transcription
	 * @param validating if {@code true} a schema is set and
	 * validation is enabled, otherwise validation is disabled
	 * @param namespaceAware if {@code true} namespace awareness is enabled,
	 * otherwise it is disabled
	 * @return ParserFactory.EAF27, or .EAF28, or newer.
	 */
    private static int eafFileFormatTaster(String trPathName, boolean validating,
    		boolean namespaceAware) {
    	InputSource is = null;
    	int version = ParserFactory.EAF30;
    	try {
    		is = toInputSource(trPathName);
    		if (is == null) {
    			return version;
    		}
    	} catch (IOException ioe) {
    		//
    		return version;
    	}
    	TasterContentHandler handler = new TasterContentHandler();
    	try {
			// first get general XML parsing warnings and errors
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
			parserFactory.setNamespaceAware(namespaceAware);
			
			if (validating) {
				// to get a validating parser, set the schema to the proper xsd schema
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				Schema eafSchema = schemaFactory.newSchema(
						ACMTranscriptionStore.class.getResource(getCurrentEAFSchemaLocal()));
				
				parserFactory.setSchema(eafSchema);
			} else {
				parserFactory.setValidating(false);// has to be false (the default) when a schema is set!
			}
			
			SAXParser parser = parserFactory.newSAXParser();
			
	    	parser.parse(is, handler);
    	} catch (SAXParseException spe) {
    		// It's okay, we threw that ourselves.
    		// unless there was a real exception, e.g. due to a missing namespace
    		if (spe.getLineNumber() >= 0 && (validating || namespaceAware)) {
    			// retry without validation and without namespace awareness
    			return eafFileFormatTaster(trPathName, false, false);
    		}
    	} catch(SAXException sax) {
    		// 
    		return version;
    	} catch (ParserConfigurationException pce) {
			// 
    		return version;
		} catch (IOException ioe) {
			//
			return version;
		} finally {
			try {
				if (is.getByteStream() != null) {
					is.getByteStream().close();// already closed here
				} else if (is.getCharacterStream() != null) {
					is.getCharacterStream().close();
				}
			} catch (Throwable t) {}
		}
    	
		if (handler.format != null) {
			// Okay, parse version numbers such as 2.7, 3.14, etc.
			int majorVersion = 0;
			int minorVersion = 0;
			String[] mmVersion = handler.format.split("\\.");
			if (mmVersion.length >=2) {
				try {
					majorVersion = Integer.parseInt(mmVersion[0]);
					minorVersion = Integer.parseInt(mmVersion[1]);
				} catch (NumberFormatException nfe) {}
			}
			
			int toolow = ParserFactory.EAF26;
			int toohigh = ParserFactory.EAF30;
			
			// Don't check all versions, use some defaults.
			if (majorVersion < 2) {
				version = toolow;
			} else if (majorVersion == 2) {
				if (minorVersion < 7) {
					version = toolow;
				} else if (minorVersion == 7) {
					version = ParserFactory.EAF27;
				} else if (minorVersion == 8) {
					version = ParserFactory.EAF28;
				} else if (minorVersion > 8) {
					version = toohigh;
				}
			} else if (majorVersion > 2) {
				version = toohigh;
			}			
		}
		
		return version;
	}
    
    /**
     * Creates an {@link InputSource} for the specified file name,
     * which may represent a local or a remote file. A local file might be
     * represented as a platform dependent path or as a valid URI as a string. 
     * Caller must ensure the enclosed InputStream or Reader is 
     * closed after usage.
     *  
     * @param trPathName the file path or URL string
     * @return an {@link InputSource} or null
     * 
     * @throws IOException any exception that can occur during creation of an 
     * InputSource, wrapped in an IOException
     */
    public static InputSource toInputSource(String trPathName) throws IOException {
    	if (trPathName == null) {
    		return null;
    	}
    	// only remove the 'file:' part of a path if it is not a valid URI
    	if (trPathName.toLowerCase().startsWith("file:")) { 
    		try {
    			new URI(trPathName);
    		} catch (Throwable tr) {
    			// starts with "file:" but is not a valid URI, remove the "file:" part etc
    			trPathName = FileUtility.urlToAbsPath(trPathName);
    		}
    	}
    	
    	File trFile = new File(trPathName);
    	if (trFile.exists()) {
    		if (!trFile.isDirectory() && trFile.canRead()) {
    			FileInputStream fis = new FileInputStream(trFile);
    			InputSource is = new InputSource(fis);
    			is.setSystemId(trPathName);
    			return is;
    		}
    	} else {
    		// try remote file or local file path that is a valid URI
    		try {
	    		URI fileURI = new URI(trPathName);
				InputStream inStream = fileURI.toURL().openStream();
				InputSource is = new InputSource(inStream);
				is.setSystemId(trPathName);
				return is;
    		} catch (URISyntaxException usex) {
    			throw new IOException(usex);
    		} catch (Throwable thr) {
    			throw new IOException(thr);
    		}
    	}
    	
    	return null;
    }
    
    /**
     * The content handler now extends the {@link DefaultHandler},
     * only some of the {@link ContentHandler} methods are overridden.
     */
	static class TasterContentHandler extends DefaultHandler {
		String format;

		@Override
		public void startElement(String uri, String localname, String qName,
				Attributes atts) throws SAXException {
			if (localname.isEmpty()) {
				localname = qName;
			}
			if (localname.equals("ANNOTATION_DOCUMENT")) {
				format = atts.getValue("FORMAT");
			}
			// Now we're done... we can stop.
			throw new SAXParseException("Seen enough of the document", null);
		}
	}

}
