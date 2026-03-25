package mpi.eudico.webserviceclient.weblicht;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import mpi.eudico.server.corpora.util.ServerLogger;

/**
 * A parser that can be used in a loop for parsing multiple TCF strings in a 
 * row. Each call to {@link TCFParser2#parse(String)} creates a new
 * {@code Handler} and the contents of the handler are returned in the end.
 * 
 * @author Han Sloetjes
 *
 */
public class TCFParser2 {
	//private TCFHandler tcfHandler;
	private SAXParser saxParser;
	private int initErrorCount = 0;
	
	/**
	 * Creates a new parser instance.
	 */
	public TCFParser2() {
		// stub
	}

	/**
	 * Parses the contents and returns a map with type to element list
	 * combinations.
	 * 
	 * @param inputContent the input to parse
	 * @return a map with {@code TCFType} keys and {@code List<TCFElement>} 
	 * values
	 * @throws SAXException parsing exception
	 * @throws IOException IO exception
	 */
	public Map<TCFType, List<TCFElement>> parse(String inputContent) throws 
		SAXException, IOException {
		if (initErrorCount >= 2) {
			// don't try to create a parser and throw initialization exceptions
			// more than twice
			return null;
		}
		
		if (saxParser == null) {
			initParser();
		}
		
		// try-with-resources closes the reader
		try (StringReader reader = new StringReader(inputContent)) {
			TCFHandler tcfHandler = new TCFHandler();
			saxParser.parse(new InputSource(
   				reader), tcfHandler);
			return tcfHandler.getAllElements();
		}

	}
	
	private void initParser () throws SAXException, IOException {
		if (saxParser == null) {
			try {
		   		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		   		parserFactory.setNamespaceAware(true);
		   		parserFactory.setValidating(false);
		   		saxParser = parserFactory.newSAXParser();
			} catch (ParserConfigurationException pce) {
	        	ServerLogger.LOG.warning("Parser configuration exception: " + pce.getMessage());
	        	initErrorCount++;
				throw new SAXException(pce);
			}
		}
	}
}
