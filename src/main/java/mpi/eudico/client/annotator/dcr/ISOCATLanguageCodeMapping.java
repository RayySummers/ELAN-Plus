package mpi.eudico.client.annotator.dcr;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * Mapping of ISOCat language codes( 2 letter code) with the Elan language code(3 letter code)
 *
 * @author aarsom
 */
public class ISOCATLanguageCodeMapping {
    //private static final String sourceURL = "http://www.isocat.org/rest/info/languages.xml";

    //private static final String cacheFileName = Constants.ELAN_DATA_DIR + File.separator + "languages.xml";

    /**
     * After 100 days (expressed in seconds), fetch a new copy of the language information file.
     */
    //private static final long REFRESH_TIME = 100L * 24L * 60L * 60L;

    private static HashMap<String, String> languageCodes;

    /**
     * Private constructor.
     */
    private ISOCATLanguageCodeMapping() {
        super();
    }

    /**
     * Returns the two letter language code
     *
     * @param lang the language
     *
     * @return the language code
     */
    public static String get2LetterLanguageCode(String lang) {

        if (languageCodes == null) {
            loadLanguageCodesFromCacheFile();
        }

        return languageCodes.get(lang);

    }

    /**
     * Parses the language codes from the cache file. If the file is absent or old, fetch it from the web service.
     * Unfortunately, as of january 2015, the web service has ended. Instead we load the codes directly from our own copy in
     * our resources, and don't bother with a copy of that.
     */
    private static synchronized void loadLanguageCodesFromCacheFile() {
        languageCodes = new HashMap<String, String>();

        InputStream istr = null;
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setNamespaceAware(false);
            parserFactory.setValidating(false);

            URL url = ISOCATLanguageCodeMapping.class.getResource("/org/isocat/resources/languages.xml");
            istr = url.openStream();
            InputSource iso = new InputSource(istr);
            parserFactory.newSAXParser().parse(iso, new LanguageCodeHandler());
        } catch (SAXException se) {
            LOG.warning("Unable to parse the ISOcat languages cache file: " + se.getMessage());
            //se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            LOG.warning("Unable to parse the ISOcat languages cache file: " + pce.getMessage());
        } catch (IOException e) {
            LOG.warning("Unable to read the ISOcat languages cache file: " + e.getMessage());
        } finally {
            try {
                if (istr != null) {
                    istr.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * Fetch the data from the URL to a temporary file, and then if all went well, rename that temporary file to the desired
     * file name.
     *
     * <p>Returns true if it seems to have worked.
     *
     * @param cacheFileName
     */
    //    private static boolean getLanguageCodesFileFromServer(String cacheFileName) {
    //        return LanguageCollection.getLanguagesFromServer(sourceURL, cacheFileName);
    //    }

    //#######################
    // Content handler
    //#######################
    static class LanguageCodeHandler extends DefaultHandler {

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws
                                                                                              SAXException {
            if (qName.equals("language")) {
                String lang = atts.getValue("tag");
                String langList = atts.getValue("tags");

                String[] languages = langList.split(" ");
                for (String language : languages) {
                    languageCodes.put(language, lang);
                }
            }
        }
    }
}
