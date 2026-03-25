package mpi.eudico.client.annotator.dcr;

import mpi.dcr.DCRConnectorException;
import mpi.dcr.DCSmall;
import mpi.dcr.isocat.Profile;
import mpi.dcr.isocat.RestDCRConnector;
import mpi.eudico.util.IoUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;


/**
 * A reader and writer of the locally stored cache. In this first version the GMT format is used.
 *
 * @author Han Sloetjes
 * @version 2.0 profiles are now stored as a struct with an id and a feature of type "name"
 */
@Deprecated
/**
 * TODO: <b>Reporter:</b> Hafeez <br />
 * <b>Task Creation Date:</b> 2023-10-31 <br />
 * <b>Description:</b> The corresponding web service is not functional anymore, so best not spend
 * time on this now, pending decision what to do with this.
 */ public class DCRCacheReaderWriter {
    private static final String STRUCT = "struct";
    private static final String FEAT = "feat";
    //private static final String dcr = "DCR"; //or DCS
    private static final String DCS = "DCS";
    private static final String DC = "DC";
    private static final String AI = "AI";
    private static final String AR = "AR";
    private static final String DESC = "Desc";
    private static final String DEFINITION = "definition";
    private static final String IDENTIFIER = "identifier";
    private static final String PROFILE = "profile";
    private static final String BROADER_CONCEPT_GENERIC = "broaderConceptGeneric";
    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String LANG = "lang";
    private static final String LOADED = "loaded";
    private static final String LAST_UPDATED = "lastUpdated";
    private static final String NAME = "name";
    private static final String URL_PREF = "http://www.isocat.org/datcat/DC-";
    private final DocumentBuilder db;
    private SAXParser parser;
    private String filePath;

    // indicates whether stored data categories were found without an id value
    private boolean profileIdsNeeded = false;

    private boolean newVersion = false;

    /**
     * Creates a new DCRCacheReaderWriter instance
     *
     * @throws ParserConfigurationException parser config exception
     */
    public DCRCacheReaderWriter() throws
                                  ParserConfigurationException {
        super();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        db = dbf.newDocumentBuilder();

        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            parserFactory.setNamespaceAware(true);
            parserFactory.setValidating(false); // don't specify a schema
            parser = parserFactory.newSAXParser();
        } catch (SAXException se) {
            LOG.warning("Could not create a parser for the DCR cache: " + se.getMessage());
        } /*catch (IOException ioe) {
           ioe.printStackTrace();
           }*/
    }

    /**
     * Returns the path to the local file.
     *
     * @return Returns the filePath.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Sets the path to the cache file
     *
     * @param filePath The filePath to set.
     */
    public void setFilePath(String filePath) {
        setFilePath(filePath, false);
    }

    /**
     * Sets the path to the cache file
     *
     * @param filePath The filePath to set.
     * @param newVersion true if DCSelection2.xml is found
     */
    public void setFilePath(String filePath, boolean newVersion) {
        this.filePath = filePath;
        this.newVersion = newVersion;
    }

    /**
     * Creates a GMT format DOM with DCR or DCS as root struct element.
     *
     * @param categories list of DCSmall objects
     *
     * @throws IOException if there is no filepath specified
     * @throws ParserConfigurationException if no Document could be created
     * @throws NullPointerException if the category list is null
     */
    public synchronized void save(List<DCSmall> categories) throws
                                                            IOException,
                                                            ParserConfigurationException {
        if (filePath == null) {
            throw new IOException("No filepath specified.");
        }

        if (categories == null) {
            throw new NullPointerException("The list of categories is null");
        }

        if (db != null) {
            Document doc = db.newDocument();
            Element dcsElem = doc.createElement(STRUCT);
            doc.appendChild(dcsElem);
            dcsElem.setAttribute(TYPE, DCS);

            DCSmall dcsmall = null;

            for (DCSmall category : categories) {
                dcsmall = category;

                Element dcEl = doc.createElement(STRUCT);
                dcEl.setAttribute(TYPE, DC);
                dcEl.setAttribute(ID, dcsmall.getId());
                dcsElem.appendChild(dcEl);

                if ((dcsmall.getIdentifier() != null) && (!dcsmall.getIdentifier().isEmpty())) {
                    Element aiEl = doc.createElement(STRUCT);
                    aiEl.setAttribute(TYPE, AI);
                    dcEl.appendChild(aiEl);

                    Element arEl = doc.createElement(STRUCT);
                    arEl.setAttribute(TYPE, AR);
                    aiEl.appendChild(arEl);

                    Element idEl = doc.createElement(FEAT);
                    idEl.setAttribute(TYPE, IDENTIFIER);
                    idEl.appendChild(doc.createTextNode(dcsmall.getIdentifier()));
                    arEl.appendChild(idEl);
                }

                Element descEl = doc.createElement(STRUCT);
                descEl.setAttribute(TYPE, DESC);
                dcEl.appendChild(descEl);

                if (!dcsmall.getLanguages().isEmpty()) {
                    String language;
                    for (int l = 0; l < dcsmall.getLanguages().size(); l++) {
                        language = dcsmall.getLanguages().get(l);
                        Element langEl = doc.createElement(STRUCT);
                        langEl.setAttribute(TYPE, LANG);

                        Element defEl = doc.createElement(FEAT);
                        defEl.setAttribute(TYPE, DEFINITION);
                        defEl.setAttribute(LANG, language);
                        defEl.appendChild(doc.createTextNode(dcsmall.getDesc(language)));
                        langEl.appendChild(defEl);

                        Element nameEl = doc.createElement(FEAT);
                        nameEl.setAttribute(TYPE, NAME);
                        nameEl.setAttribute(LANG, language);
                        nameEl.appendChild(doc.createTextNode(dcsmall.getName(language)));
                        langEl.appendChild(nameEl);

                        descEl.appendChild(langEl);
                    }
                }


                if ((dcsmall.getProfiles() != null) && (dcsmall.getProfiles().length > 0)) {
                    for (int j = 0; j < dcsmall.getProfiles().length; j++) {
                        if (dcsmall.getProfiles()[j].getName().isEmpty()) {
                            continue;
                        }

                        Element prEl = doc.createElement(STRUCT);
                        prEl.setAttribute(TYPE, PROFILE);
                        prEl.setAttribute(ID, dcsmall.getProfiles()[j].getId());
                        Element nameEl = doc.createElement(FEAT);
                        nameEl.setAttribute(TYPE, NAME);
                        nameEl.appendChild(doc.createTextNode(dcsmall.getProfiles()[j].getName()));
                        prEl.appendChild(nameEl);
                        descEl.appendChild(prEl);
                    }
                }

                if ((dcsmall.getBroaderDCId() != null) && (!dcsmall.getBroaderDCId().isEmpty())) {
                    Element brEl = doc.createElement(FEAT);
                    brEl.setAttribute(TYPE, BROADER_CONCEPT_GENERIC);
                    brEl.appendChild(doc.createTextNode(dcsmall.getBroaderDCId()));
                    descEl.appendChild(brEl);
                }

                Element loadEl = doc.createElement(FEAT);
                loadEl.setAttribute(TYPE, "loaded");
                loadEl.appendChild(doc.createTextNode(Boolean.toString(dcsmall.isLoaded())));
                descEl.appendChild(loadEl);

                if (dcsmall.isLoaded()) {
                    Element lastUpdateEl = doc.createElement(FEAT);
                    lastUpdateEl.setAttribute(TYPE, LAST_UPDATED);
                    lastUpdateEl.appendChild(doc.createTextNode(new Timestamp(dcsmall.getLastUpdated()).toString()));
                    dcEl.appendChild(lastUpdateEl);
                }
            }

            // write
            try {
                IoUtil.writeEncodedFile("UTF-8", filePath, doc.getDocumentElement());
            } catch (Exception ioe) {
                throw new IOException(ioe.getMessage());
            }
        }
    }

    /**
     * Reads the data categories from the cache.
     *
     * @return the list of categories from the cache
     *
     * @throws IOException any io exception
     */
    public synchronized List<DCSmall> read() throws
                                             IOException {
        if (parser != null) {
            try {
                DcrGmtAdapter adapter;
                if (newVersion) {
                    adapter = new DcrGmtAdapter2();
                } else {
                    adapter = new DcrGmtAdapter();
                }

                //reader.setContentHandler(adapter);
                parser.parse(filePath, adapter);
                List<DCSmall> curDCList = adapter.getDCS();

                if (profileIdsNeeded) {
                    try {
                        RestDCRConnector rconn = new RestDCRConnector();
                        List<Profile> profs = rconn.getProfiles();

                        Profile[] curProfs;
                        for (DCSmall dcSmall : curDCList) {
                            curProfs = dcSmall.getProfiles();
                            for (Profile curProf : curProfs) {
                                if (curProf.getId().isEmpty()) {
                                    for (Profile prof : profs) {
                                        if (prof.getName().equals(curProf.getName())) {
                                            curProf.setId(prof.getId());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (DCRConnectorException dce) {
                        LOG.warning("Could not retrieve additional information from ISOCat");
                    }
                }

                return curDCList;
            } catch (SAXException se) {
                LOG.warning("Could not read the local data categories cache: " + se.getMessage());
            }
        }

        return new ArrayList<>();
    }

    /**
     * A parser adapter for parsing GMT style dc selection file.
     *
     * @author Han Sloetjes
     */
    private class DcrGmtAdapter extends DefaultHandler {
        protected List<DCSmall> datcats;

        // parse time objects
        protected String idAttr;
        protected String identifierAttr;
        protected String descAttr;
        protected DCSmall dcsmall;
        protected String broaderDCIdAttr;
        protected List<Profile> profiles;
        protected String curProfId;
        protected String curStruct;
        protected String curFeat;
        protected String content = "";
        protected int structLevel = 0;

        boolean inFeat = false;

        /**
         * Creates a new DcrGmtAdapter instance
         */
        public DcrGmtAdapter() {
            super();
            //super(xmlReader);
            datcats = new ArrayList<>();
            profiles = new ArrayList<>();
        }

        /**
         * Return the list of small Data Category objects.
         *
         * @return the list of data categories
         */
        public List<DCSmall> getDCS() {
            return datcats;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws
                                                                 SAXException {
            //System.out.println("characters..." + start + " " + length);
            // for some reason all kinds of in between element characters (spaces, newlines) are passed
            // to this method ??
            if (inFeat) {
                content = new String(ch, start, length);

                if (IDENTIFIER.equals(curFeat)) {
                    identifierAttr = content;
                } else if (DEFINITION.equals(curFeat)) {
                    descAttr = content;
                } else if (BROADER_CONCEPT_GENERIC.equals(curFeat)) {
                    broaderDCIdAttr = content;
                } else if (PROFILE.equals(curFeat)) { //old profile feature
                    //profiles.add(content);
                    profiles.add(new Profile("", content));
                } else if (NAME.equals(curFeat)) { //new profile feature
                    profiles.add(new Profile(curProfId, content));
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws
                                                                           SAXException {
            //System.out.println("end..." + localName);
            inFeat = false;

            if (localName.equals(STRUCT)) {
                structLevel--;
            }

            if (structLevel == 1) { // dc level, dcs is 0

                if (idAttr != null && !profiles.isEmpty()) {
                    dcsmall = new DCSmall(null, idAttr, identifierAttr);
                    dcsmall.setBroaderDCId(broaderDCIdAttr);
                    dcsmall.setProfiles(profiles.toArray(new Profile[profiles.size()]));
                    dcsmall.setDesc(descAttr);
                    dcsmall.setLoaded(false);
                    datcats.add(dcsmall);
                }
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws
                                                                                              SAXException {
            //System.out.println("start..." + localName);
            if (localName.equals(STRUCT)) {
                structLevel++;

                String ty = atts.getValue(TYPE);

                if (DC.equals(ty)) {
                    curStruct = DC;
                    // reset
                    idAttr = atts.getValue(ID);
                    // HS Dec 2010 if the ID from the cache is a simple numeric value (3057)
                    // prefix the isocat url to change it into an official PID
                    if (!idAttr.isEmpty()) { // test other assumptions, like start with http://...?
                        try {
                            Integer.parseInt(idAttr);
                            // no error, then add the prefix
                            idAttr = URL_PREF + idAttr;
                        } catch (NumberFormatException nfe) {
                            // assume the id is in the right format (or check the affix?)
                        }
                    }
                    identifierAttr = null;
                    descAttr = null;
                    broaderDCIdAttr = null;
                    profiles.clear();
                    content = "";
                } else if (AR.equals(ty)) {
                    curStruct = AR;
                } else if (DESC.equals(ty)) {
                    curStruct = DESC;
                } else if (PROFILE.equals(ty)) {
                    curStruct = PROFILE; // the new profile struct
                    curProfId = atts.getValue(ID);
                }
            } else if (localName.equals(FEAT)) {
                inFeat = true;

                String fty = atts.getValue(TYPE);

                if (IDENTIFIER.equals(fty)) {
                    curFeat = IDENTIFIER;
                } else if (DEFINITION.equals(fty)) {
                    curFeat = DEFINITION;
                } else if (PROFILE.equals(fty)) {
                    curFeat = PROFILE; // the old profile feature, replaced by a struct
                    profileIdsNeeded = true;
                } else if (BROADER_CONCEPT_GENERIC.equals(fty)) {
                    curFeat = BROADER_CONCEPT_GENERIC;
                } else if (NAME.equals(fty)) {
                    curFeat = NAME; // the new profile name feature
                }
            }
        }
    }

    /**
     * A parser adapter for parsing GMT style dc selection file.
     *
     * @author aarsom
     */
    private class DcrGmtAdapter2 extends DcrGmtAdapter {

        // parse time objects
        protected HashMap<String, String> descMap;
        protected HashMap<String, String> nameMap;

        private boolean inProfile;
        private boolean load;
        private long lastUpdate = 0;

        private String language;

        /**
         * Creates a new DcrGmtAdapter instance
         */
        public DcrGmtAdapter2() {
            super();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws
                                                                 SAXException {
            if (inFeat) {
                content = new String(ch, start, length);

                if (IDENTIFIER.equals(curFeat)) {
                    identifierAttr = content;
                } else if (DEFINITION.equals(curFeat)) {
                    descMap.put(language, content);
                } else if (BROADER_CONCEPT_GENERIC.equals(curFeat)) {
                    broaderDCIdAttr = content;
                } else if (PROFILE.equals(curFeat)) { //old profile feature
                    //profiles.add(content);
                    profiles.add(new Profile("", content));
                } else if (NAME.equals(curFeat)) { //new profile feature
                    if (inProfile) {
                        profiles.add(new Profile(curProfId, content));
                    } else {
                        nameMap.put(language, content);
                    }
                } else if (LOADED.equals(curFeat)) {
                    load = Boolean.parseBoolean(content);
                } else if (LAST_UPDATED.equals(curFeat)) {
                    lastUpdate = Timestamp.valueOf(content).getTime();
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws
                                                                           SAXException {
            //System.out.println("end..." + localName);
            inFeat = false;
            inProfile = false;

            if (localName.equals(STRUCT)) {
                structLevel--;
            }

            if (structLevel == 1) { // dc level, dcs is 0

                if (idAttr != null && !profiles.isEmpty()) {
                    dcsmall = new DCSmall(null, idAttr, identifierAttr);
                    dcsmall.setBroaderDCId(broaderDCIdAttr);
                    dcsmall.setProfiles(profiles.toArray(new Profile[profiles.size()]));
                    dcsmall.setLoaded(load);
                    dcsmall.setDescMap(descMap);
                    dcsmall.setNameMap(nameMap);

                    dcsmall.setLastUpdate(lastUpdate);

                    datcats.add(dcsmall);
                }
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws
                                                                                              SAXException {
            //System.out.println("start..." + localName);
            if (localName.equals(STRUCT)) {
                structLevel++;

                String ty = atts.getValue(TYPE);

                if (DC.equals(ty)) {
                    curStruct = DC;
                    // reset
                    idAttr = atts.getValue(ID);
                    // HS Dec 2010 if the ID from the cache is a simple numeric value (3057)
                    // prefix the isocat url to change it into an official PID
                    if (!idAttr.isEmpty()) { // test other assumptions, like start with http://...?
                        try {
                            Integer.parseInt(idAttr);
                            // no error, then add the prefix
                            idAttr = URL_PREF + idAttr;
                        } catch (NumberFormatException nfe) {
                            // assume the id is in the right format (or check the affix?)
                        }
                    }
                    identifierAttr = null;
                    descAttr = null;
                    broaderDCIdAttr = null;
                    profiles.clear();
                    content = "";

                    descMap = new HashMap<>();
                    nameMap = new HashMap<>();
                    inProfile = false;
                    load = false;
                    language = "";
                } else if (AR.equals(ty)) {
                    curStruct = AR;
                } else if (DESC.equals(ty)) {
                    curStruct = DESC;
                } else if (PROFILE.equals(ty)) {
                    curStruct = PROFILE; // the new profile struct
                    inProfile = true;
                    curProfId = atts.getValue(ID);
                }
            } else if (localName.equals(FEAT)) {
                inFeat = true;

                String fty = atts.getValue(TYPE);

                if (IDENTIFIER.equals(fty)) {
                    curFeat = IDENTIFIER;
                } else if (DEFINITION.equals(fty)) {
                    curFeat = DEFINITION;
                    language = atts.getValue(LANG);
                } else if (PROFILE.equals(fty)) {
                    curFeat = PROFILE; // the old profile feature, replaced by a struct
                    profileIdsNeeded = true;
                } else if (BROADER_CONCEPT_GENERIC.equals(fty)) {
                    curFeat = BROADER_CONCEPT_GENERIC;
                } else if (NAME.equals(fty)) {
                    curFeat = NAME;
                    if (!inProfile) {
                        language = atts.getValue(LANG);
                    }
                } else if (LOADED.equals(fty)) {
                    curFeat = LOADED;
                } else if (LAST_UPDATED.equals(fty)) {
                    curFeat = LAST_UPDATED;
                }
            }
        }
    }
}
