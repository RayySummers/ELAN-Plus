package mpi.eudico.client.annotator.md.imdi;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;


/**
 * Performs a quick trivial test on a metadata file.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ImdiSaxCheck {
    /**
     * Creates a new ImdiSaxCheck instance
     */
    public ImdiSaxCheck() {
        super();
    }

    /**
     * Starts parsing the file, only 2 elements
     * <br>
     * Version July 2013: added check to support CMDI (in fact IMDI within CMDI)
     * 
     * @param file the metadata file
     *
     * @return true if it is a "METATRANSCRIPT" file of type "SESSION" and the
     *         first element is "Session", or if it is a CMDI file with a Session component
     */
    public boolean isSessionFile(File file) {
        if ((file == null) || !file.exists()) {
            return false;
        }

    	FileInputStream fis = null;
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            parserFactory.setValidating(false);
            
            ImdiCheckHandler handler = new ImdiCheckHandler();

            fis = new FileInputStream(file);
            InputSource source = new InputSource(fis);

            try {
                parserFactory.newSAXParser().parse(source, handler);
            } catch (SAXException sax) {
            	LOG.info("Is CMDI/IMDI Session file: " + handler.isSessionFile());
                return handler.isSessionFile();
            } catch (IOException ioe) {
                LOG.warning("Cannot read file: " +
                    ioe.getMessage());
            } catch (ParserConfigurationException pce) {
            	 LOG.warning("Cannot create parser for file: " +
                         pce.getMessage());
			}
        } catch (FileNotFoundException fnfe) {
            LOG.warning("Cannot find file: " + fnfe.getMessage());
        } finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
			}
        }

        return false;
    }
}
