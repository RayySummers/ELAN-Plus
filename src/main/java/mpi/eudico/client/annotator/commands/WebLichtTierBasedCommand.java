package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ELAN;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.multilangcv.ISOCodeTables;
import mpi.eudico.webserviceclient.WsClientRest;
import mpi.eudico.webserviceclient.weblicht.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.UTF_8;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A command that calls a WebLicht service with sentence and (therefore) token input.
 *
 * @version Apr 2022: Added support for calling WaaS (WebLicht as a Service) with a tool chain file. For each (top level)
 *     input annotation a call is made, instead of putting all annotations in a single TCF string first and calling the
 *     service only once.
 */
public class WebLichtTierBasedCommand extends AbstractProgressCommand implements UndoableCommand {
    private MergeTranscriptionsByAddingCommand mergeCommand;
    private TranscriptionImpl transcription;
    private WLServiceDescriptor wlDescriptor;
    private TiersToTCF tiersToTCF;
    private String tierName;
    private String contentType; // plain text or sentence or token (word) level of input
    private TCFType inputType;

    // or with a tool chain file to the WebLicht as a Service service
    private Path chainPath = null;
    private boolean tcfFormat = false;
    private String accessKey;

    /**
     * Constructor.
     *
     * @param theName the name of the command
     */
    public WebLichtTierBasedCommand(String theName) {
        super(theName);
    }

    @Override
    public void undo() {
        if (mergeCommand != null) {
            mergeCommand.undo();
        }
    }

    @Override
    public void redo() {
        if (mergeCommand != null) {
            mergeCommand.redo();
        }
    }

    /**
     * @param receiver the TranscriptionImpl
     * @param arguments <ul>
     *     <li>arg[0] = a web service descriptor (WLServiceDescriptor) or null</li>
     *     <li>arg[1] = the name of the input tier (String)</li>
     *     <li>arg[2] = the type of input content, Sentence or Token or Text (String)</li>
     *     <li>arg[3] = the path to a tool chain xml file (String, optional)</li>
     *     <li>arg[4] = the input format of the first tool in the chain (String, optional)</li>
     *     <li>arg[5] = the user's access key for the tool chain service (in the
     *     case of execution with a tool chain file) (String, optional)</li>
     *     </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        wlDescriptor = (WLServiceDescriptor) arguments[0];
        tierName = (String) arguments[1];

        TierImpl tier = transcription.getTierWithId(tierName);
        if (tier == null || tier.getNumberOfAnnotations() == 0) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "The tier is null or has 0 annotations: " + tierName);
            }
            progressInterrupt("The tier is null or has no annotations");
            return;
        }

        contentType = (String) arguments[2];
        if (contentType == null || contentType.equals(TCFConstants.SENT)) {
            // assume sentence level
            contentType = TCFConstants.SENT;
            inputType = TCFType.SENTENCE;
        } else {
            inputType = TCFType.TOKEN;
        }
        if (arguments.length >= 6) {
            String chainFilePath = (String) arguments[3];
            try {
                chainPath = Path.of(chainFilePath, "");
            } catch (InvalidPathException ipe) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.log(Level.WARNING, "Invalid tool chain file path: " + chainFilePath);
                }
                progressInterrupt("The tool chain file path is invalid");
                return;
            }
            String inputFormat = (String) arguments[4];
            if (inputFormat != null && inputFormat.equals("TCF")) {
                tcfFormat = true;
            }
            accessKey = (String) arguments[5];
        }

        if (wlDescriptor != null) {
            // start thread
            if (!cancelled) {
                new WLTThread().start();
            } else {
                progressInterrupt("The process was cancelled");
            }
        } else if (chainPath != null) {
            if (!cancelled) {
                new ChainThread().start();
            } else {
                progressInterrupt("The process was cancelled");
            }
        }
    }

    /**
     * @param refTrans the transcription the new tiers have to be added to
     * @param nextTrans the transcription resulting from the returned content by the service
     * @param tiersToAdd effectively all tiers in the next transcription except for the (equivalent of) the source tier
     */
    private void updateTierNames(Transcription refTrans, Transcription nextTrans, List<String> tiersToAdd) {
        String tierName;
        final int MAX_COUNT = 50; // arbitrary number of attempts

        for (Tier t : nextTrans.getTiers()) {
            tierName = t.getName();

            if (tiersToAdd.contains(tierName) && refTrans.getTierWithId(tierName) != null) {
                // add or update counter suffix
                int count = 1;
                String newName = tierName;
                do {
                    newName = tierName + "-" + count++;
                } while (refTrans.getTierWithId(newName) != null && count < MAX_COUNT);

                if (count < MAX_COUNT) {
                    t.setName(newName);
                    tiersToAdd.remove(tierName);
                    tiersToAdd.add(newName);
                }
            }
        }

    }

    private class WLTThread extends Thread {

        /**
         * The actual processing: gets the input tier and calls a method for either sentence level input or token level
         * input.
         */
        @Override
        public void run() {

            if (transcription != null) {
                TierImpl tier = transcription.getTierWithId(tierName);
                // check ??
                if (tier == null) {
                    progressInterrupt("A tier of that name has not been found: " + tierName);
                    return;
                }

                if (contentType.equals(TCFConstants.SENT)) {
                    processSentenceLevel(tier);
                } else if (contentType.equals(TCFConstants.TOKEN)) {
                    processWordLevel(tier);
                } else {
                    LOG.warning("Unknown content type of the tier, cannot proceed");
                    progressInterrupt("Unknown content type of the tier, cannot proceed");
                }

            } else { // no transcription, we shouldn't get here at all probably
                LOG.warning("There is no transcription, cannot do anything");
                progressInterrupt("There is no transcription, cannot do anything");
            }
        }

        /**
         * The input tier is considered to contain sentence level annotations.
         *
         * @param tier the input tier
         */
        private void processSentenceLevel(TierImpl tier) {
            if (tier == null) {
                LOG.warning("There is no tier to process.");
                return;
            }
            /*
             * The actual processing:
             * - (10%) convert annotations to tcf
             * - (40%) upload to selected service(s)
             * - (10%) convert result tcf to transcription
             * - (10%) update times to match the original annotation times
             * - (25%) merge transcription
             */
            tiersToTCF = new TiersToTCF();
            WebLichtWsClient wsClient = new WebLichtWsClient();
            String tcfString = null;
            // Three possible approaches:
            // 1. Put all annotations together to one text and upload. Match what is returned with
            //    the original annotation. This does not work, the sentences can be different from the original
            //    input, the annotations.
            // 2. Tokenize the annotations and create tcf ourselves.
            //
            // 3. Process one annotation at a time in either of the 2 ways

            tcfString = tiersToTCF.toTCFString(tier, "Sentence");

            //System.out.println("TCF Sentence:");
            //System.out.println(tcfString); // cache result?

            if (tcfString != null) {
                progressUpdate(10, "Created TCF format from input tier");

                String tcfString2 = null;
                //String posTagUrl = "service-opennlp/annotate/postag";
                //http://weblicht.sfs.uni-tuebingen.de/rws/service-opennlp/annotate/postag

                try {
                    tcfString2 = wsClient.callWithTCF(wlDescriptor.fullURL, tcfString);
                    progressUpdate(50, "Uploaded the input to the service");
                } catch (IOException ioe) {
                    LOG.warning("An error occurred while calling a web service: " + ioe.getMessage());
                    progressInterrupt("An error occurred while calling a web service: " + ioe.getMessage());
                    return;
                }
                // convert to transcription
                if (tcfString2 != null) {
                    progressUpdate(52, "Uploaded the input to the service, received output");
                    //System.out.println("TCF 2:");
                    //System.out.println(tcfString2); // cache result?

                    TCFtoTranscription tctt = new TCFtoTranscription();
                    tctt.setDefaultDuration(1000);
                    try {
                        TranscriptionImpl nextTrans = tctt.createTranscription(tcfString2);
                        //update times based on existing transcription
                        progressUpdate(60, "Converted returned content to tiers");

                        TierImpl refTier = nextTrans.getTierWithId(TCFConstants.SENT);
                        if (refTier == null) {
                            // message
                            progressInterrupt("Could not find the Sentence level tier in the output");
                            return;
                        }

                        if (refTier.getAnnotations().size() == 0) {
                            progressInterrupt("There are no annotations on the Sentence tier");
                            return;
                        }

                        // can check number of annotations and / or mappings of orig annotations to sentence id's?

                        List<AbstractAnnotation> origAnns = tier.getAnnotations();
                        int numOrigAnns = origAnns.size();
                        List<AbstractAnnotation> procAnns = refTier.getAnnotations();
                        int numProcAnns = procAnns.size();
                        // shift all new annotations beyond the end of the original annotations
                        long origEndTime = origAnns.get(numOrigAnns - 1).getEndTimeBoundary();
                        nextTrans.shiftAllAnnotations(origEndTime);

                        AlignableAnnotation procAnn; // the created annotation is time aligned
                        AbstractAnnotation origAnn; // the original can be symbolic association

                        for (int i = 0; i < numProcAnns; i++) {
                            if (i < numOrigAnns) {
                                origAnn = origAnns.get(i);
                                procAnn = (AlignableAnnotation) procAnns.get(i);

                                procAnn.updateTimeInterval(origAnn.getBeginTimeBoundary(), origAnn.getEndTimeBoundary());
                            }
                        }
                        // rename the reference tier
                        refTier.setName(tier.getName());
                        progressUpdate(70,
                                       "Realigned the new annotations with the existing annotations, adding new "
                                       + "annotations");

                        // now merge tiers that are on a lower level then the sentence tier
                        List<String> tiersToAdd = new ArrayList<String>();
                        List<TierImpl> depTiers = refTier.getDependentTiers();
                        for (int i = 0; i < depTiers.size(); i++) {
                            TierImpl depTier = depTiers.get(i);
                            if (depTier.getAnnotations().size() > 0) {
                                tiersToAdd.add(depTier.getName());
                            }
                        }
                        // pop up a message if there are no non-empty tiers?
                        if (tiersToAdd.size() == 0) {
                            progressInterrupt("There were no annotation produced under the sentence level");
                            return;
                        }
                        // check dependent tier names and rename where necessary so that tiers/annotations are not
                        // overwritten
                        updateTierNames(transcription, nextTrans, tiersToAdd);

                        // April 2015 don't add the reference tier otherwise the existing source annotations will be
                        // overwritten.
                        //tiersToAdd.add(0, refTier.getName());

                        mergeCommand = (MergeTranscriptionsByAddingCommand) ELANCommandFactory.createCommand(transcription,
                                                                                                             ELANCommandFactory.WEBLICHT_MERGE_TRANSCRIPTIONS);
                        // don't overwrite the input annotations, don't add linked files and don't perform a tier
                        // compatibility test
                        mergeCommand.execute(transcription,
                                             new Object[] {nextTrans,
                                                           tiersToAdd,
                                                           Boolean.TRUE,
                                                           Boolean.FALSE,
                                                           Boolean.FALSE,
                                                           Boolean.FALSE});


                        LOG.info("Merged in the produced tiers and annotations.");
                        progressComplete("Merged in the produced tiers and annotations.");
                    } catch (SAXException sax) {
                        LOG.warning("An error occurred while parsing the returned TCF file: " + sax.getMessage());
                        progressInterrupt("An error occurred while parsing the returned TCF file: " + sax.getMessage());
                    } catch (IOException ioe) { // is this still possible here?
                        LOG.warning("An error occurred while calling a web service: " + ioe.getMessage());
                        progressInterrupt("An error occurred while calling a web service: " + ioe.getMessage());
                    }
                } else {
                    progressInterrupt("Calling the web service did not succeed, no content returned.");
                }
            } else {
                progressInterrupt("Unable to create meaningful input for the web service");
            }
        }

        /**
         * The input tier is considered to contain word or token level annotations.
         *
         * @param tier the input tier
         */
        private void processWordLevel(TierImpl tier) {
            if (tier == null) { // should never happen
                LOG.warning("There is no tier to process.");
                return;
            }
            /*
             * The actual processing:
             * - (10%) convert annotations to tcf
             * - (40%) upload to selected service(s)
             * - (10%) convert result tcf to transcription
             * - (10%) update times to match the original annotation times
             * - (25%) merge transcription
             */
            TierImpl parentTier; // sentence level probably
            String tcfString = null;
            // get the parent tier
            if (tier.hasParentTier()) {
                parentTier = tier.getParentTier();
                if (parentTier.hasParentTier()) {
                    // this is considered an error. Assume that the parent tier is a sentence tier and is a top level
                    // tier
                    // or a symbolically associated child of a top level tier ?
                    // now return
                    //                    progressInterrupt("The parent tier is not a top level tier, this setup is
                    //                    currently not supported
                    //                    .");
                    //                    return;
                }
                tiersToTCF = new TiersToTCF();
                tcfString = tiersToTCF.toTCFString(tier, "Token");
                //System.out.println("TCF Token:");

                //System.out.println(tcfString); // maybe cache the result?
                progressUpdate(10, "Created TCF format from input tier");
            } else {
                // this is an error. Treat all annotations as words in one sentence?
                // for now return
                progressInterrupt("The tier has no parent; token type of input is expected to be on a child tier: "
                                  + tierName);
                return;
            }

            if (tcfString != null) {
                WebLichtWsClient wsClient = new WebLichtWsClient();
                String tcfString2 = null;

                //String posTagUrl = "service-opennlp/annotate/postag";
                //http://weblicht.sfs.uni-tuebingen.de/rws/service-opennlp/annotate/postag
                try {
                    tcfString2 = wsClient.callWithTCF(wlDescriptor.fullURL, tcfString);
                    progressUpdate(50, "Uploaded the input to the service");
                } catch (IOException ioe) {
                    LOG.warning("An error occurred while calling a web service: " + ioe.getMessage());
                    progressInterrupt("An error occurred while calling a web service: " + ioe.getMessage());
                    return;
                }

                // convert to transcription
                if (tcfString2 != null) {
                    progressUpdate(52, "Uploaded the input to the service, received output");
                    TCFtoTranscription tctt = new TCFtoTranscription();
                    tctt.setDefaultDuration(100); // safe assumption that this way all new annotations will be left
                    // of the original counterpart and will have to be shifted to the right.
                    try {
                        TranscriptionImpl nextTrans = tctt.createTranscription(tcfString2);
                        progressUpdate(60, "Converted returned content to tiers");
                        //update times based on existing transcription
                        TierImpl refSentTier = nextTrans.getTierWithId(TCFConstants.SENT);

                        if (refSentTier == null) {
                            // message
                            LOG.warning("Could not find the Sentence level tier in the output");
                            progressInterrupt("Could not find the Sentence level tier in the output");
                            return;
                        }
                        // if there are no annotations return
                        if (refSentTier.getAnnotations().size() == 0) {
                            LOG.warning("There are no annotations on the Sentence tier");
                            progressInterrupt("There are no annotations on the Sentence tier");
                            return;
                        }

                        TierImpl refTokTier = nextTrans.getTierWithId(TCFConstants.TOKEN);
                        if (refTokTier == null) {
                            // message
                            LOG.warning("Could not find the Token level tier in the output");
                            progressInterrupt("Could not find the Token level tier in the output");
                            return;
                        }
                        // if there are no annotations return
                        if (refTokTier.getAnnotations().size() == 0) {
                            LOG.warning("There are no annotations on the Token tier");
                            progressInterrupt("There are no annotations on the Token tier");
                            return;
                        }
                        // relinking has to be done via the sentence level tier
                        // there is no guarantee that the tokens are returned unchanged
                        // maybe just create and add a copy of the word/token tier//??
                        List<AbstractAnnotation> origAnns = new ArrayList<AbstractAnnotation>();
                        // filter out sentences with word/token children
                        AbstractAnnotation parAnn;
                        for (AbstractAnnotation tokAnn : tier.getAnnotations()) {
                            parAnn = (AbstractAnnotation) tokAnn.getParentAnnotation();
                            if (!origAnns.contains(parAnn)) {
                                origAnns.add(parAnn);
                            }
                        }

                        int numOrigAnns = origAnns.size();
                        List<AbstractAnnotation> procAnns = refSentTier.getAnnotations();
                        int numProcAnns = procAnns.size();

                        AbstractAnnotation origAnn;
                        AlignableAnnotation procAnn;

                        if (numOrigAnns == numProcAnns) { // assume that the original and resulting sentences match
                            for (int i = numOrigAnns - 1; i >= 0; i--) {
                                origAnn = origAnns.get(i);
                                procAnn = (AlignableAnnotation) procAnns.get(i);
                                // how to effectively update annotation times? Bulldozer mode, direct time slot
                                // manipulation?
                                if (procAnn.getBeginTimeBoundary() <= origAnn.getBeginTimeBoundary()) {
                                    procAnn.updateTimeInterval(origAnn.getBeginTimeBoundary(), origAnn.getEndTimeBoundary());
                                } else {
                                    // something wrong, wrong estimation when the returned tcf is converted to a
                                    // transcription
                                    LOG.warning("A new annotation has been positioned to the right of the original, cannot "
                                                + "realign");
                                    // it will likely be overwritten by other annotations
                                }
                            }
                        } else { // different number of sentences, maybe create copies of tiers?
                            for (int i = numOrigAnns - 1; i >= 0; i--) {
                                if (i < numProcAnns) {
                                    origAnn = origAnns.get(i);
                                    procAnn = (AlignableAnnotation) procAnns.get(i);
                                    // how to effectively update annotation times? Bulldozer mode, direct time slot
                                    // manipulation?
                                    if (procAnn.getBeginTimeBoundary() <= origAnn.getBeginTimeBoundary()) {
                                        if (procAnn.getValue()
                                                   .equals(origAnn.getValue())) { // there might always be slight
                                            // differences, how to
                                            // deal with that?
                                            procAnn.updateTimeInterval(origAnn.getBeginTimeBoundary(),
                                                                       origAnn.getEndTimeBoundary());
                                        } else {
                                            // continue the loop?
                                        }
                                    }
                                }
                            }
                        }
                        // match the token tiers, rename the token tier in the new transcription,
                        // create unique tier names for lower level tiers
                        refTokTier.setName(tier.getName());
                        // now merge tiers that are on a lower level then the word tier
                        List<String> tiersToAdd = new ArrayList<String>();
                        List<TierImpl> depTiers = refTokTier.getDependentTiers();
                        for (int i = 0; i < depTiers.size(); i++) {
                            TierImpl depTier = depTiers.get(i);
                            // skip empty tiers?
                            if (depTier.getAnnotations().size() > 0) {
                                tiersToAdd.add(depTier.getName());
                            }
                        }
                        // pop up a message if there are no non-empty tiers, interrupt the process
                        if (tiersToAdd.size() == 0) {
                            progressInterrupt("There were no annotations produced under the sentence level");
                            return;
                        }

                        updateTierNames(transcription, nextTrans, tiersToAdd);
                        //tiersToAdd.add(0, tier.getName());

                        mergeCommand = (MergeTranscriptionsByAddingCommand) ELANCommandFactory.createCommand(transcription,
                                                                                                             ELANCommandFactory.WEBLICHT_MERGE_TRANSCRIPTIONS);
                        mergeCommand.execute(transcription,
                                             new Object[] {nextTrans,
                                                           tiersToAdd,
                                                           Boolean.TRUE,
                                                           Boolean.FALSE,
                                                           Boolean.FALSE});


                        LOG.info("Added the produced tiers and annotations.");
                        progressComplete("Added in the produced tiers and annotations.");
                    } catch (SAXException sax) {
                        LOG.warning("An error occurred while parsing the returned TCF file: " + sax.getMessage());
                        progressInterrupt("An error occurred while parsing the returned TCF file: " + sax.getMessage());
                    } catch (IOException ioe) { // is this still possible here?
                        LOG.warning("An error occurred while calling a web service: " + ioe.getMessage());
                        progressInterrupt("An error occurred while calling a web service: " + ioe.getMessage());
                    }
                }
            }
        }

    }

    /**
     * A thread that sends a tool chain file and textual contents to WaaS (WebLicht as a Service). The content should be
     * compatible with the input expected by the first tool in the chain. <p> This variant is based on {@link HttpClient}.
     */
    private class ChainThread extends Thread {
        String waasUrl = "https://weblicht.sfs.uni-tuebingen.de/WaaS/api/1.0/chain/process";
        int loggedErrors = 0;
        int connectionErrors = 0;

        @Override
        public void run() {
            if (cancelled) {
                return;
            }

            if (transcription != null) {
                TierImpl tier = transcription.getTierWithId(tierName);
                // check ??
                if (tier == null) {
                    progressInterrupt("A tier of that name has not been found: " + tierName);
                    return;
                }

                // create a HTTP client, iterate over annotations and call the web service
                // and add results to the temporary transcription
                WsClientRest wsClient = new WsClientRest();
                HttpClient client = null;
                HttpRequest.Builder requestBuilder = null;
                String boundary = null;
                String chainContents = null;
                Map<String, Object> inputMap = new HashMap<String, Object>(2);

                try {
                    client = HttpClient.newHttpClient();
                    boundary = UUID.randomUUID().toString();
                    chainContents = wsClient.getFileContentsAsString(chainPath, UTF_8);
                    if (chainContents == null) {
                        // log
                        progressInterrupt("The tool chain file is null or is empty");
                        return;
                    }

                    inputMap.put("chains", chainContents);
                    inputMap.put("apikey", accessKey);
                    requestBuilder = HttpRequest.newBuilder()
                                                .uri(URI.create(waasUrl))
                                                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                                                .header("Accept", "*/*")
                                                .header("User-Agent", "ELAN/" + ELAN.getVersionString())
                                                .timeout(Duration.ofSeconds(15));
                } catch (IllegalArgumentException e) {
                    progressInterrupt("Error while creating a http client: " + e.getMessage());
                    return;
                } catch (Throwable t) {
                    progressInterrupt("Error while creating a http client: " + t.getMessage());
                    return;
                }

                TierImpl rootTier = tier;
                if (tier.hasParentTier()) {
                    rootTier = tier.getRootTier();
                }
                // copy the tier hierarchy to a copy transcription
                TranscriptionImpl nextTrans = createTempTranscription(tier);
                nextTrans.setNotifying(false);

                TiersToTCF ttcf = null;
                TCFtoTranscription tcfToTr = new TCFtoTranscription();
                Map<TCFType, TierImpl> tierMapping = tcfToTr.createTiers(nextTrans, tier, inputType, null);

                TCFParser2 parser = new TCFParser2();

                String lang = "en";
                if (tier.getLangRef() != null) {
                    lang = tier.getLangRef();
                    if (lang.length() > 2) {
                        String iso1 = ISOCodeTables.toPart1Code(lang);
                        if (iso1 != null) {
                            lang = iso1;
                        }
                    }
                }

                List<AbstractAnnotation> processedList = new ArrayList<AbstractAnnotation>();
                float curProg = 10f;
                progressUpdate((int) curProg, "Prepared the web service client, starting annotation upload");
                // 80 % for annotation processing, from 10 to 90%
                // iterate over annotations
                List<AbstractAnnotation> allAnnotations = tier.getAnnotations();
                float perAnno = 80f / allAnnotations.size();

                for (int i = 0; i < allAnnotations.size(); i++) {
                    if (cancelled) {
                        progressInterrupt("The process was cancelled");
                        return;
                    }
                    curProg += perAnno;
                    progressUpdate((int) curProg, "Processing annotations...");

                    AbstractAnnotation a = allAnnotations.get(i);
                    if (!tcfFormat) {
                        inputMap.put("content", a.getValue());
                        //System.out.println("TCF IN 1: " + a.getValue());
                        try {
                            HttpRequest request =
                                requestBuilder.POST(wsClient.getMultiPartTextPublisher(inputMap, boundary)).build();
                            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                            int status = handleResponseStatus(response.statusCode(), response.body());
                            if (status == 0) {
                                continue;
                            } else if (status == -1) {
                                progressInterrupt("The process stopped because of an error (1)");
                                return;
                            }
                            String tcfString = response.body();
                            try {
                                Map<TCFType, List<TCFElement>> parsed = parser.parse(tcfString);
                                if (parsed != null) {
                                    tcfToTr.createAnnotations(a, inputType, parsed, tierMapping);
                                } else {
                                    if (LOG.isLoggable(Level.WARNING)) {
                                        LOG.log(Level.WARNING,
                                                "The produced TCF could not be parsed (1); no annotation produced");
                                    }
                                }
                            } catch (SAXException | IOException ex) {
                                // continue or interrupt?
                                if (LOG.isLoggable(Level.WARNING)) {
                                    LOG.log(Level.WARNING, "The produced TCF could not be parsed (1): " + ex.getMessage());
                                }
                            }
                            // System.out.println("TCF OUT 1: " + tcfString);
                        } catch (IOException | InterruptedException e) {
                            connectionErrors++;
                            if (connectionErrors == 0 && LOG.isLoggable(Level.WARNING)) {
                                LOG.log(Level.WARNING, "A connection error occurred (1): " + e.getMessage());
                            }
                            connectionErrors++;
                            if (connectionErrors >= 5) {
                                progressInterrupt("Connection errors ocurred, stopping the process");
                                return;
                            }
                        }
                    } else { // upload TCF contents
                        if (ttcf == null) {
                            ttcf = new TiersToTCF();
                        }

                        if (TCFConstants.SENT.equals(contentType)) {
                            inputMap.put("content", ttcf.sentenceToTCF5String(a, lang));
                            //System.out.println("TCF IN 2: " + inputMap.get("content"));

                            try {
                                HttpRequest request =
                                    requestBuilder.POST(wsClient.getMultiPartTextPublisher(inputMap, boundary)).build();
                                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                                int status = handleResponseStatus(response.statusCode(), response.body());
                                if (status == 0) {
                                    continue;
                                } else if (status == -1) {
                                    progressInterrupt("The process stopped because of an error (2)");
                                    return;
                                }
                                String tcfString = response.body();
                                try {
                                    Map<TCFType, List<TCFElement>> parsed = parser.parse(tcfString);
                                    if (parsed != null) {
                                        tcfToTr.createAnnotations(a, inputType, parsed, tierMapping);
                                    } else {
                                        if (LOG.isLoggable(Level.WARNING)) {
                                            LOG.log(Level.WARNING,
                                                    "The produced TCF could not be parsed (2); no annotation produced");
                                        }
                                    }
                                } catch (SAXException | IOException ex) {
                                    // continue or interrupt?
                                    if (LOG.isLoggable(Level.WARNING)) {
                                        LOG.log(Level.WARNING,
                                                "The produced TCF could not be parsed (2): " + ex.getMessage());
                                    }
                                }
                                //System.out.println("TCF OUT 2: " + tcfString);
                            } catch (IOException | InterruptedException e) {
                                connectionErrors++;
                                if (connectionErrors == 0 && LOG.isLoggable(Level.WARNING)) {
                                    LOG.log(Level.WARNING, "A connection error occurred (2): " + e.getMessage());
                                }
                                connectionErrors++;
                                if (connectionErrors >= 5) {
                                    progressInterrupt("Connection errors ocurred, stopping the process");
                                    return;
                                }
                            }
                        } else {
                            // input is the token level:
                            // should sentence level be constructed if the input tier is not a top level tier?
                            // Only collect tokens under same root into one list
                            if (processedList.contains(a)) {
                                continue;
                            }
                            List<AbstractAnnotation> tokenList = new ArrayList<AbstractAnnotation>();
                            if (!tier.hasParentTier()) { // or tier != rootTier
                                inputMap.put("content", ttcf.tokenToTCF5String(a, lang));
                                tokenList.add(a);
                                processedList.add(a);
                                //System.out.println("TCF IN 3a: " + inputMap.get("content"));
                            } else {
                                AbstractAnnotation rootAnn =
                                    (AbstractAnnotation) rootTier.getAnnotationAtTime((a.getBeginTimeBoundary()
                                                                                       + a.getEndTimeBoundary()) / 2);
                                // check null?
                                tokenList.add(a);
                                processedList.add(a);
                                // continue iteration over tier.getAnnotations
                                int j = i + 1;
                                for (; j < allAnnotations.size(); j++) {
                                    AbstractAnnotation a2 = allAnnotations.get(j);
                                    if (rootTier.getAnnotationAtTime((a2.getBeginTimeBoundary() + a2.getEndTimeBoundary())
                                                                     / 2) == rootAnn) {
                                        tokenList.add(a2);
                                        processedList.add(a2);
                                    } else {
                                        break;
                                    }
                                }
                                // set the index 1 back
                                i = j - 1;

                                // process the tokenList
                                inputMap.put("content", ttcf.tokensToTCF5String(tokenList, lang));
                                //System.out.println("TCF IN 3b: " + inputMap.get("content"));
                            }


                            try {
                                HttpRequest request =
                                    requestBuilder.POST(wsClient.getMultiPartTextPublisher(inputMap, boundary)).build();
                                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                                int status = handleResponseStatus(response.statusCode(), response.body());
                                if (status == 0) {
                                    continue;
                                } else if (status == -1) {
                                    progressInterrupt("The process stopped because of an error (3)");
                                    break;
                                }
                                String tcfString = response.body();
                                try {
                                    Map<TCFType, List<TCFElement>> parsed = parser.parse(tcfString);
                                    if (parsed != null) {
                                        tcfToTr.createAnnotations(tokenList, parsed, tierMapping);
                                    } else {
                                        if (LOG.isLoggable(Level.WARNING)) {
                                            LOG.log(Level.WARNING,
                                                    "The produced TCF could not be parsed (3); no annotation produced");
                                        }
                                    }
                                } catch (SAXException | IOException ex) {
                                    // continue or interrupt?
                                    if (LOG.isLoggable(Level.WARNING)) {
                                        LOG.log(Level.WARNING,
                                                "The produced TCF could not be parsed (3): " + ex.getMessage());
                                    }
                                }
                                //System.out.println("TCF OUT 3: " + tcfString);
                            } catch (IOException | InterruptedException e) {
                                connectionErrors++;
                                if (connectionErrors == 0 && LOG.isLoggable(Level.WARNING)) {
                                    LOG.log(Level.WARNING, "A connection error occurred (3): " + e.getMessage());
                                }
                                connectionErrors++;
                                if (connectionErrors >= 5) {
                                    progressInterrupt("Connection errors ocurred, stopping the process");
                                    return;
                                }
                            }
                        }
                    }
                }// end annotation iteration

                if (cancelled) {
                    progressInterrupt("The process was cancelled");
                    return;
                }

                // skip empty tiers
                List<String> tiersToAdd = new ArrayList<String>();
                TierImpl sourceTierCp = nextTrans.getTierWithId(tierName);
                for (TierImpl nextTier : nextTrans.getTiers()) {
                    if (nextTier == sourceTierCp || sourceTierCp.hasAncestor(nextTier)) {
                        continue;
                    }
                    if (nextTier.getNumberOfAnnotations() > 0) {
                        tiersToAdd.add(nextTier.getName());
                    }
                }
                // merge
                // pop up a message if there are no non-empty tiers, interrupt the process
                if (tiersToAdd.size() == 0) {
                    progressInterrupt(String.format("There were no annotations produced under the %s level",
                                                    (inputType == TCFType.SENTENCE
                                                     ? TCFConstants.SENT
                                                     : TCFConstants.TOKEN)));
                    return;
                }

                updateTierNames(transcription, nextTrans, tiersToAdd);
                //tiersToAdd.add(0, tier.getName());

                mergeCommand = (MergeTranscriptionsByAddingCommand) ELANCommandFactory.createCommand(transcription,
                                                                                                     ELANCommandFactory.WEBLICHT_MERGE_TRANSCRIPTIONS);
                mergeCommand.execute(transcription,
                                     new Object[] {nextTrans, tiersToAdd, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE});


                LOG.info("Added the produced tiers and annotations.");
                progressComplete("Added the produced tiers and annotations.");
            }

            progressComplete("Completed processing of annotations");
        }

        /**
         * Creates a new transcription and copies the source tier and its ancestors to the new transcription.
         *
         * @param sourceTier the tier to copy
         *
         * @return the new transcription
         */
        private TranscriptionImpl createTempTranscription(TierImpl sourceTier) {
            List<String> tiersToAdd = new ArrayList<String>(5);
            tiersToAdd.add(sourceTier.getName());

            TierImpl parentTier = sourceTier.getParentTier();
            while (parentTier != null) {
                tiersToAdd.add(0, parentTier.getName());
                parentTier = parentTier.getParentTier();
            }
            TranscriptionImpl tempTranscription = new TranscriptionImpl();
            MergeTranscriptionsByAddingCommand cmd =
                new MergeTranscriptionsByAddingCommand(ELANCommandFactory.MERGE_TRANSCRIPTIONS);
            cmd.execute(tempTranscription,
                        new Object[] {transcription, tiersToAdd, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE});

            return tempTranscription;
        }

        /**
         * Checks the HTTP status code, prints error messages if required and returns a simplified response code.
         *
         * @param statusCode the received HTTP status code
         * @param message the returned TCF containing the error message
         *
         * @return {@code 1} if no error occurred and processing of the result can continue, {@code 0} if there was a
         *     (client) error and the next call might succeed, {@code -1} if a serious (server) error occurred (including
         *     authorization) and the iteration can be stopped
         */
        private int handleResponseStatus(int statusCode, String message) {
            if (statusCode >= 200 && statusCode < 300) {
                // process the result
                return 1;
            }
            if (statusCode >= 401 && statusCode <= 403) {
                // show message: is the access key still valid?
                if (loggedErrors == 0) {
                    // show warning
                    JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription),
                                                  String.format("An error occurred [%d], is the access key still valid?",
                                                                statusCode),
                                                  ElanLocale.getString("Message.Warning"),
                                                  JOptionPane.WARNING_MESSAGE);
                }
                if (loggedErrors < 5) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.log(Level.WARNING,
                                String.format("An error occurred, is the access key still valid? Code [%d], Message [%s]",
                                              statusCode,
                                              message));
                    }
                }
                loggedErrors++;
                return -1;
            }
            if (statusCode >= 500) {
                // report a server error and stop
                if (loggedErrors == 0) {
                    // show warning
                    JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription),
                                                  String.format("An error occurred [%d], stopping the process", statusCode),
                                                  ElanLocale.getString("Message.Warning"),
                                                  JOptionPane.WARNING_MESSAGE);
                }
                if (loggedErrors < 5) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.log(Level.WARNING,
                                String.format("An error occurred, stopping the process. Code [%d], Message [%s]",
                                              statusCode,
                                              message));
                    }
                }
                loggedErrors++;
                return -1;
            }
            if (statusCode >= 300 && statusCode < 400) {
                // redirection not supported, stop
                if (loggedErrors == 0) {
                    // show warning
                    JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription),
                                                  String.format("A redirection error occurred [%d], stopping the process",
                                                                statusCode),
                                                  ElanLocale.getString("Message.Warning"),
                                                  JOptionPane.WARNING_MESSAGE);
                }
                if (loggedErrors < 5) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.log(Level.WARNING,
                                String.format("A redirection error occurred, stopping the process. Code [%d], Message [%s]",
                                              statusCode,
                                              message));
                    }
                }
                loggedErrors++;
                return -1;
            }
            if ((statusCode >= 400 && statusCode < 500) || statusCode < 200) {
                // 400-500: a client error, the next annotation might succeed, continue
                // the same for < 200
                if (loggedErrors < 5) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.log(Level.WARNING,
                                String.format("A client error occurred, continuing. Code [%d], Message [%s]",
                                              statusCode,
                                              message));
                    }
                }
                loggedErrors++;
            }
            return 0;
        }
    }

}
