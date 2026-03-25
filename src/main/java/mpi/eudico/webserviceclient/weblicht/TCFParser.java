package mpi.eudico.webserviceclient.weblicht;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mpi.eudico.server.corpora.util.ServerLogger;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A parser for TCF content.
 * 
 * @author Han Sloetjes
 */
public class TCFParser {
	// content
	private String inputContent;
	private TCFHandler tcfHandler;
	
	/**
	 * Constructor
	 * 
	 * @param inputContent the input string
	 */
	public TCFParser(String inputContent) {
		this.inputContent = inputContent;
	}
	
	/**
	 * Returns the parsed text.
	 * 
	 * @return the text
	 */
	public String getText() {
		return tcfHandler.getText();
	}
	
	/**
	 * Returns the list of elements of the specific type.
	 * 
	 * @param type the type to get the elements for
	 * @return the list of TCF elements or null
	 */
	public List<TCFElement> getElementsByType(TCFType type) {
		return tcfHandler.getElementsByType(type);
	}
	
	/**
	 * Parses the contents of the string.
	 * 
	 * @throws SAXException any SAX parser exception
	 * @throws IOException any IO exception
	 */
	public void parse() throws SAXException, IOException {
		if (inputContent == null) {
			return;// throw exception
		}
		tcfHandler = new TCFHandler();
		
        try {           
	   		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	   		parserFactory.setNamespaceAware(true);
	   		parserFactory.setValidating(false);
	   		parserFactory.newSAXParser().parse(new InputSource(
	   				new StringReader(inputContent)), tcfHandler);
        } catch (SAXException se) {
        	//se.printStackTrace();
        	ServerLogger.LOG.warning("Parser exception: " + se.getMessage());
        	throw se;
        } catch (IOException ioe) {
        	//ioe.printStackTrace();
        	ServerLogger.LOG.warning("IO exception: " + ioe.getMessage());
        	throw ioe;
        } catch (ParserConfigurationException pce) {
        	ServerLogger.LOG.warning("Parser configuration exception: " + pce.getMessage());
			throw new SAXException(pce);
		}
	}

}
