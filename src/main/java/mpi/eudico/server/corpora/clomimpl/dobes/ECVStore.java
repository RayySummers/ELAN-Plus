package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.util.ServerLogger;
import mpi.eudico.util.ExternalCV;

/**
 * Stores and load an External CV
 * 
 * @author Micha Hulsbosch
 * @version jul 2010
 */
public class ECVStore {
	
	/**
	 * Creates a new ECV store instance.
	 */
	public ECVStore() {
		super();
	}

	/**
	 * Loads an External CV from a url. This is not optimal for the case where 
	 * an external file contains multiple CV's.
	 * 
	 * @param cv the  external CV
	 * @param url the location of the ECV  
	 * 
	 * @throws ParseException any SAX parse exception
	 */
	public void loadExternalCV(ExternalCV cv, String url) 
		throws ParseException {
		
		List<ExternalCV> ecvList = new ArrayList<ExternalCV>(1);
		ecvList.add(cv);
		loadExternalCVS(ecvList, url);
		
//		ECV02Parser ecvParser = null;
//        try {
//        	ecvParser = new ECV02Parser(url);
//        	ecvParser.parse(null);
//        } catch (ParseException pe) {
//        	System.out.println("Parse failed " + url);
//        	throw(pe);
//        }
//        
//        // get the ext refs mappings
//		Map<String, ExternalReference> extReferences = ecvParser.getExternalReferences();
//        
//        ArrayList<ControlledVocabulary> allCVs = ecvParser.getControlledVocabularies();
//
//        ExternalCV cvFromUrl = null;
//        for (int i = 0; i < allCVs.size(); i++) {
//        	cvFromUrl = (ExternalCV) allCVs.get(i);
//        	if(cvFromUrl.getName().equals(cv.getName())) {
//        		cv.moveAll(cvFromUrl);
//        	}
//        } 
	}
	
	/**
	 * Loads all entries for External Controlled Vocabularies from the specified url.
	 * The ECV objects have been created beforehand (e.g. when parsing an eaf file).
	 * 
	 * Any ECVs that are found at the url will be modified by adding the found entries.
	 * 
	 * @param ecvList the list of ECV objects, should not be null
	 * @param url the url of the file containing the controlled vocabularies
	 * 
	 * @throws ParseException any SAX parse exception
	 */
	public void loadExternalCVS(List<ExternalCV> ecvList, String url) throws ParseException {
		if (ecvList == null || ecvList.size() == 0) {
			return;// return silently
		}
		ECV02Parser ecvParser = null;
        try {
        	ecvParser = new ECV02Parser(url);
        	ecvParser.parse(ecvList);
        } catch (ParseException pe) {
        	ServerLogger.LOG.severe("Parse failed " + url);
        	throw(pe);
        }
        
        // get the ext refs mappings
//		Map<String, ExternalReference> extReferences = ecvParser.getExternalReferences();
        
//        ArrayList<ControlledVocabulary> allCVs = ecvParser.getControlledVocabularies();
//
//        ExternalCV cvFromUrl = null;
//        ExternalCV cvFromList = null;
//        for (int j = 0; j < ecvList.size(); j++) {
//        	cvFromList = ecvList.get(j);
//        	
//	        for (int i = 0; i < allCVs.size(); i++) {
//	        	cvFromUrl = (ExternalCV) allCVs.get(i);
//	        	// checking equality by name might fail if the name has been changed there where
//	        	// it is used, by the "client"
//	        	if(cvFromUrl.getName().equals(cvFromList.getName())) {
//	        		cvFromList.moveAll(cvFromUrl);
//	        		break;
//	        	}
//	        }
//        }
	}
	
	/**
	 * Stores an External CV (not implemented yet)
	 * 
	 * @see ECV02Encoder
	 *  
	 * @param cv a single external CV
	 * @param cachePath the cache base folder
	 * @param urlString the location of the source file
	 */
	public void storeExternalCV(ExternalCV cv, String cachePath, String urlString) {
		if (cv == null) {
			ServerLogger.LOG.warning("Could not create a cached version: no external CV provided.");
			return;
		}
		List<ExternalCV> list = new ArrayList<ExternalCV>(1);
		storeExternalCVS(list, cachePath, urlString);
	}
	
	/**
	 * Creates a cached version of the controlled vocabularies loaded from the same
	 * external source.
	 * 
	 * @param ecvList the list of controlled vocabularies
	 * @param cachePath the path to the cache base folder
	 * @param urlString the source file
	 */
	public void storeExternalCVS(List<ExternalCV> ecvList, String cachePath, String urlString) {
		if (ecvList == null || ecvList.size() == 0) {
			ServerLogger.LOG.warning("Could not create a cached version: no external CV's provided.");
			return; // return silently
		}
		if (cachePath == null) {
			ServerLogger.LOG.warning("Could not create a cached version: no cache folder specified.");
			return;
		}
		if (urlString == null) {
			ServerLogger.LOG.warning("Could not create a cached version: no source URL specified.");
			return;
		}
		
		ExternalReferenceImpl eri = new ExternalReferenceImpl(urlString, ExternalReference.EXTERNAL_CV);
		try {
			ECV02Encoder encoder = new ECV02Encoder();
			encoder.encodeAndSave(ecvList, cachePath, eri);
		} catch (Throwable thr) {// catch anything that can go wrong, caching is not crucial
			ServerLogger.LOG.severe("Could not create a cached version: " + thr.getMessage());
		}
	}
	
	/**
	 * Performs a quick test on the version of the ECV file and returns it
	 * as a string. 
	 * 
	 * Version: Jan 2018
	 * @param path the location of the file to test 
	 * @return the version as a string, currently 0.1 or 0.2
	 * 
	 * @see ACMTranscriptionStore#eafFileFormatTaster(String)
	 */
	public String ecvFileFormatTest(String path) {
		SAXParser parser = null;
		FormatTestHandler handler = new FormatTestHandler();
		InputSource is = null;
		String version = ECV02Encoder.VERSION;
    	try {
    		is = ACMTranscriptionStore.toInputSource(path);
    		if (is == null) {
    			return version;
    		}
    	} catch (IOException ioe) {
    		//
    		return version;
    	}	
    	
    	try {
			// first get general XML parsing warnings and errors
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
			parserFactory.setNamespaceAware(true);
			
			// to get a validating parser, set the schema to the proper xsd schema
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema eafSchema = schemaFactory.newSchema(ECVStore.class.getResource(
					ACMTranscriptionStore.getCurrentEAFSchemaLocal()));
			
			parserFactory.setSchema(eafSchema);
			parser = parserFactory.newSAXParser();
			
			parser.parse(is, handler);
    	} catch (SAXParseException spe) {
    		// It's okay, we threw that ourselves.
    	} catch(SAXException sax) {
			if (ServerLogger.LOG.isLoggable(Level.WARNING)) {
				ServerLogger.LOG.warning(sax.getMessage());
			}
    		return version;
    	} catch (ParserConfigurationException pce) {
			if (ServerLogger.LOG.isLoggable(Level.WARNING)) {
				ServerLogger.LOG.warning(pce.getMessage());
			} 
    		return version;
		} catch (IOException ioe) {
			if (ServerLogger.LOG.isLoggable(Level.WARNING)) {
				ServerLogger.LOG.warning(ioe.getMessage());
			}
			return version;
		} finally {
			try {
				if (is.getByteStream() != null) {
					is.getByteStream().close();
				} else if (is.getCharacterStream() != null) {
					is.getCharacterStream().close();
				}
			} catch (Throwable t) {}
		}
		
		if (handler.version != null) {
			if (ServerLogger.LOG.isLoggable(Level.FINE)) {
				ServerLogger.LOG.fine("The .ecv file has version: " + handler.version);
			}
			return handler.version;
		}
		
		return version;
	}
	
	/**
	 * Handler class that retrieves the version and then stops (by throwing an exception).
	 * @version Jan 2018
	 */
	class FormatTestHandler extends DefaultHandler {
		String version;
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (localName.isEmpty()) {
				localName = qName;
			}
			if (localName.equals("CV_RESOURCE")) {
				version = attributes.getValue("VERSION");
			}
			// Now we're done... we can stop.
			throw new SAXParseException("Seen enough of the document", null);
		}
	}
}
