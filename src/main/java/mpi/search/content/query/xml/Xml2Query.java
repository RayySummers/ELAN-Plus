package mpi.search.content.query.xml;

import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import mpi.search.content.query.model.ContentQuery;

/**
 * Evokes parser to read a query from an XML file. 
 * 
 * @author Alexander Klassmann
 * @version Sep 30, 2004
 */
public class Xml2Query {
	private static SAXParser parser;
	
	/** Private constructor. */
	private Xml2Query() {
		super();
	}


	/**
	 * Reads a query from an XML file and initialize a {@code ContentQuery}.
	 * 
	 * @param file the file to read
	 * @param query the query object to update
	 * @throws Exception any exception that can occur
	 */
	public static void translate(String file, ContentQuery query) throws Exception {
		String filePath = null;
		
		if (file != null) {
			filePath = file.replace('\\','/');
		}

		if (filePath != null) {
			
			if (parser == null) {
		   		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		   		parserFactory.setNamespaceAware(true);
		   		parserFactory.setValidating(false);
		   		parser = parserFactory.newSAXParser();
			}

			FileInputStream stream = null;
			try {
				parser.parse(new InputSource(stream = new FileInputStream(filePath)), 
						new QueryContentHandler(query));
	        } finally {
				try {
					if (stream != null) {
						stream.close();
					}
				} catch (IOException e) {
				}
			}
		}
		    
	}

}
