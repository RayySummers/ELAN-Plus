package mpi.eudico.client.annotator.md.cmdi;

import java.io.IOException;
import java.net.URL;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.md.imdi.MDKVData;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

/**
 * Creates a simple XML Document object and creates a tree based on the first element within 
 * the Components element. No checking of schemas and data category references. 
 * <p>
 * 03-2024 extended with support for basic Dublin Core metadata xml files.
 * 
 * @author Han Sloetjes
 */
public class CMDIDom {
	private Document cmdiDoc;
	
	/**
	 * Constructor. Creates a Document object, converts most expected exceptions to an IOException.
	 * 
	 *  @param cmdiUrl the url of the cmdi or xml file
	 *  @throws IOException for almost everything that can go wrong in handling the file and building the DOM
	 */
	public CMDIDom(URL cmdiUrl) throws IOException {
		if (cmdiUrl == null) {
			throw new NullPointerException("The CMDI url is null");
		}
		
		try {
		    DOMImplementationLS domImplementation = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
		    LSInput lsInput = domImplementation.createLSInput();
		    lsInput.setEncoding("UTF-8");
		    lsInput.setByteStream(cmdiUrl.openStream());
		    
		    LSParser parser = domImplementation.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
		    //parser.parseURI(cmdiUrl.toURI().toString());
		    cmdiDoc = parser.parse(lsInput);
		} catch (IllegalAccessException iae) {
			throw new IOException(iae.getMessage());
		} catch (ClassCastException cce) {
			throw new IOException(cce.getMessage());
		} catch (ClassNotFoundException cnfe) {
			throw new IOException(cnfe.getMessage());
		} catch (InstantiationException ie) {
			throw new IOException(ie.getMessage());
		} catch (DOMException de) {
			throw new IOException(de.getMessage());
		} catch (LSException lse) {
			throw new IOException(lse.getMessage());
		}
	}

	/**
	 * Creates a tree from the Document structure. 
	 *  
	 * @return the root node of the tree
	 */
	public DefaultMutableTreeNode getAsTree() {
		if (cmdiDoc == null) {
			return null;
		}

		NodeList compList = cmdiDoc.getElementsByTagName("Components");

		if (compList.getLength() > 0) {
			// take the first one
			Element compElem = (Element) compList.item(0);
		
			if (compElem != null) {
				NodeList nodeList = compElem.getChildNodes();
				int length = nodeList.getLength();
				for (int i = 0; i < length; i++) {
					Node nextNode = nodeList.item(i);
					// can attributes be in the list?
					if (nextNode instanceof Element) {
						// use the first element as the "session" element
						Element rootElem = (Element) nextNode;
						
						DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
						CMDIKVData rootData = new CMDIKVData(rootElem.getTagName(), "", null);
						rootNode.setUserObject(rootData);
						// recursively add child nodes
						addChildElements(rootNode, rootElem);
						
						return rootNode;
					}
				}
			} else {
				// log
				LOG.info("The first Component is null");
			}
		} else {
			//log
			LOG.info("No list of CMDI Components found in the metadata file, trying DC");
			if (cmdiDoc.getDocumentElement().getNodeName().contains(":dc")|| 
					cmdiDoc.getDocumentElement().getLocalName().equalsIgnoreCase("dc") ||
					cmdiDoc.getDocumentElement().getNodeName().equals("metadata")) {
				// use the document element for the root node and add all child elements
				Element rootElem = cmdiDoc.getDocumentElement();
				
				DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
				CMDIKVData rootData = new CMDIKVData(rootElem.getLocalName(), "", null);
				rootNode.setUserObject(rootData);
				// recursively add child nodes
				addChildElements(rootNode, rootElem);
				
				return rootNode;

			}
			
		}
		
		return null;
	}
	
	/**
	 * Creates a tree node for the given element and adds the children.
	 * Assumes treeNode is not null.
	 * 
	 * @param treeNode the tree node
	 * @param domElem the cmdi or other metadata element element
	 */
	private void addChildElements(DefaultMutableTreeNode treeNode, Element domElem) {
		if (domElem != null) {
			NodeList children = domElem.getChildNodes();

			for (int i = 0; i < children.getLength(); i++) {
				Node n = children.item(i);
				if (n instanceof Element) {
					Element ne = (Element) n;
					DefaultMutableTreeNode treeChild = new DefaultMutableTreeNode();
					MDKVData userData = getDataObject(ne.getLocalName(), getTextNodeContent(ne));
					treeChild.setUserObject(userData);
					treeNode.add(treeChild);
					// recursive
					addChildElements(treeChild, ne);
				} 				
			}
		}
	}
	
	/**
	 * Extracts the text content of an element.
	 * 
	 * @param e the element
	 * @return the textual content
	 */
	private String getTextNodeContent(Element e) {
		if (e == null) {
			return "";
		}
		
		NodeList children = e.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.TEXT_NODE) {
				return ((Text) n).getWholeText();
			}
		}
		
		return "";
	}
	
	// copied from ImdiDoc
    private MDKVData getDataObject(String key, String value) {
    	if (value != null) {
    		value = value.trim();
    		if (value.length() == 1 && value.charAt(0) == '\n') {
    			value = null;
    		}
    	}

    	return new MDKVData(key, value);
    }
}
