package mpi.eudico.client.annotator.tiersets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.util.IoUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A class for reading and writing tier sets in XML format.
 *  
 * @author Aarthy Somasundaram
 */
public class TierSetIO {
	
	private final String DESC ="DESCRIPTION";
	private final String NAME ="NAME";	
	private final String TIER = "TIER";
	private final String TIERS = "TIERS";
	private final String TIERSET ="TIERSET";
	private final String TIERSETS = "TIERSETS";
	private final String VISIBLE ="VISIBLE";
	
	/**
	 * Constructor.
	 */
	public TierSetIO() {
		super();
	}	
   
	/** 
	 * Reads tier sets from the file.
	 * 
	 * @param file the tier set file
	 * @return a list of tier sets
	 * 
	 * @throws IOException if reading or parsing fails
	 */
	public List<TierSet> read(File file) throws IOException {
		if (file == null) {
			throw new IOException("Cannot read from file: file is null");
		}
		
		try {
			ParamHandler ph = new ParamHandler(); 

    		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
    		parserFactory.setNamespaceAware(true);
    		parserFactory.setValidating(false);   		
    		parserFactory.newSAXParser().parse(file, ph);
    		
			return ph.getTierSetList();
		} catch (SAXException sax) {
			LOG.warning("Parsing failed: " + sax.getMessage());
			throw new IOException(sax.getMessage());
		} catch (ParserConfigurationException e) {
			LOG.warning("No parser for tier set file: " + e.getMessage());
			throw new IOException(e.getMessage());
		}
	}
	
	/**
	 * Writes the tier sets to the file.
	 * 
	 * @param file the file to write to
	 * @param tierSetList list of tier sets
	 * 
	 * @throws IOException if writing fails
	 * @deprecated substituted by {@link #writeLS(File, List)}
	 */
	public void write(File file, List<TierSet> tierSetList ) throws IOException {
		if (file == null) {
			throw new IOException("Cannot write to file: file is null");
		}
		
		if(tierSetList == null || tierSetList.isEmpty()){
			throw new IOException("Cannot write to file: the tier set list is null or empty");
		}

		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file), StandardCharsets.UTF_8)));
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<TIERSETS>");
		
		for(TierSet tierSet : tierSetList){
			writer.println("<" + TIERSET + " " + NAME + "=\"" + tierSet.getName() + "\" " +
					DESC + "=\"" + tierSet.getDescription() + "\" " +
					VISIBLE + "=\"" + tierSet.isVisible() +"\">");
			writer.println("<TIERS>");
			
			List<String> visibleTierList = tierSet.getVisibleTierList();
			List<String> tierList = tierSet.getTierList();
			for(String tier: tierList){
				writer.println("<" + TIER + " " + NAME + "=\"" + tier + "\" " +
						VISIBLE + "=\"" + visibleTierList.contains(tier) +"\" />");
			}
			writer.println("</TIERS>");
			writer.println("</TIERSET>");
		}
		
		writer.println("</TIERSETS>");
		
		writer.close();
	}
	
	/**
	 * Alternative implementation of writing the TierSet file using {@link IoUtil},
	 * which uses an {@link LSSerializer}. This takes care of XML escaping of tier 
	 * names etc. as opposed to the raw XML writing in the 
	 * {@link #write(File, List)} method.
	 * <br>
	 * Added by: Han Sloetjes
	 * @since November 2018
	 * 
	 * @param file the file to write to
	 * @param tierSetList the list containing the current tier sets
	 * 
	 * @throws IOException any exception (wrapped in an IOException) that can occur 
	 * while constructing a Document and writing to the file
	 */
	public void writeLS(File file, List<TierSet> tierSetList) throws IOException {
		if (file == null) {
			throw new IOException("Cannot write to file: file is null");
		}
		
		if(tierSetList == null || tierSetList.isEmpty()){
			throw new IOException("Cannot write to file: the tier set list is null or empty");
		}
		
		Document doc = createDoc(tierSetList);
		
		try {
			IoUtil.writeEncodedFile("UTF-8", file.getAbsolutePath(), doc.getDocumentElement());
		} catch (Throwable t) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Error while writing the tierset file: " +t.getMessage());
			}
			throw new IOException(t);
		}
	}
	
	/**
	 * Creates a TIERSETS DOM Document which is not based on an XML schema.
	 * 
	 * @since November 2018
	 * @author Han Sloetjes
	 * 
	 * @param tierSetList the list of tier sets to serialize
	 * @return a DOM Document 
	 * 
	 * @throw IOException any exception or error that can occur is wrapped in a
	 * (new) IOException
	 */
	private Document createDoc(List<TierSet> tierSetList) throws IOException {
		Document doc = null;
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder        db  = dbf.newDocumentBuilder();
			doc = db.newDocument();
			// root element
			Element docEl = doc.createElement(TIERSETS);
			if (docEl != null) {
				doc.appendChild(docEl);
			}
			
			// add each tier set
			for (TierSet tierSet : tierSetList){
				Element tierSetEl = doc.createElement(TIERSET);
				docEl.appendChild(tierSetEl);
				tierSetEl.setAttribute(NAME, tierSet.getName());
				tierSetEl.setAttribute(DESC, tierSet.getDescription());
				tierSetEl.setAttribute(VISIBLE, String.valueOf(tierSet.isVisible()));
				
				// the wrapper TIERS element for the tiers in the set
				Element tiersEl = doc.createElement(TIERS);
				tierSetEl.appendChild(tiersEl);
				
				List<String> visibleTierList = tierSet.getVisibleTierList();
				List<String> tierList = tierSet.getTierList();
				for (String tier: tierList){
					Element tierEl = doc.createElement(TIER);
					tiersEl.appendChild(tierEl);
					// add name and visible attributes
					tierEl.setAttribute(NAME, tier);
					tierEl.setAttribute(VISIBLE, String.valueOf(visibleTierList.contains(tier)));
				}
			}
			
			return doc;
		} catch (ParserConfigurationException pce) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Could not create a document: " + pce.getMessage());
			}
			
			throw new IOException(pce);
		} catch (Throwable t) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Could not create a document: " + t.getMessage());
			}
			
			throw new IOException(t);
		}
	}
	
	// ###############  Parser content handler  ############################################# */
	class ParamHandler extends DefaultHandler {
		//private String curContent = "";
		private String curName = "";
		private String desc = "";
		private boolean isVisible;
		
		private List<String> tierList = null;
		private List<String> visibleTierList = null;
		private List<TierSet> tierSetList = null;
		
		/**
		 * Constructor.
		 */
		public ParamHandler() {
			super();
			tierSetList= new ArrayList<TierSet>();
		}

		/**
		 * Returns the list of tier set objects.
		 * 
		 * @return the list of tier set objects
		 */
		public List<TierSet> getTierSetList() {
			return tierSetList;
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			//curContent += new String(ch, start, length);			
		}

		@Override
		public void startElement(String nameSpaceURI, String name,
	            String rawName, Attributes attributes) throws SAXException {
			if (name.equals(TIERSET)) {
				curName = attributes.getValue(NAME);
				desc = attributes.getValue(DESC);
				isVisible = Boolean.valueOf(attributes.getValue(VISIBLE));
			} 
			else if(name.equals(TIERS)){
				tierList = new ArrayList<String>();
				visibleTierList = new ArrayList<String>();
			}
			else if (name.equals(TIER)){
				tierList.add(attributes.getValue(NAME));
				if(Boolean.valueOf(attributes.getValue(VISIBLE))){
					visibleTierList.add(attributes.getValue(NAME));
				}
			}
		}

		@Override
		public void endElement(String nameSpaceURI, String name, String rawName)
				throws SAXException {
			if(name.equals(TIERSET)){
				TierSet tierSet = new TierSet(curName, tierList);
				tierSet.setDescription(desc);
				tierSet.setVisibleTiers(visibleTierList);
				tierSet.setVisible(isVisible);			
				
				tierSetList.add(tierSet);
				
				curName = "";
				tierList = null;
				visibleTierList = null;
				desc = "";
				//curContent = "";
			}			
		}		
	}
}
