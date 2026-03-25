package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ELAN;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.webserviceclient.WsClientRest;
import mpi.eudico.webserviceclient.weblicht.TCFtoTranscription;
import mpi.eudico.webserviceclient.weblicht.WLServiceDescriptor;
import mpi.eudico.webserviceclient.weblicht.WebLichtWsClient;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A command that sends text to a WebLicht web service and adds results to the transcription. The undoable part is delegated
 * to a merging command.
 *
 * @author Han Sloetjes
 * @version Apr 2022: added support for web service calls with a tool chain file.
 */
public class WebLichtTextBasedCommand extends AbstractProgressCommand implements UndoableCommand {
    private MergeTranscriptionsByAddingCommand mergeCommand;
    private Transcription transcription;
    private Transcription transcription2;
    private String inputText;
    private int sentenceDuration;

    // upload either to a single service
    private WLServiceDescriptor wlDescriptor;
    // or with a tool chain file to the WebLicht as a Service service
    private Path chainPath = null;
    private String accessKey;

    /**
     * Constructor
     *
     * @param name the name of the command
     */
    public WebLichtTextBasedCommand(String name) {
        super(name);
    }

    /**
     * @param receiver the TranscriptionImpl
     * @param arguments <ul><li>arg[0] = the input text (String)</li>
     *     <li>arg[1] = custom duration per sentence (Integer)</li>
     *     <li>arg[2] = a tokenizer descriptor (WLServiceDescriptor) OR </li>
     *     <li>arg[2] = the path to a tool chain xml file (String)</li>
     *     <li>arg[3] = the user's access key for the tool chain service (in the
     *     case of execution with a tool chain file) (String)</li>
     *     </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        transcription = (Transcription) receiver;

        inputText = (String) arguments[0];
        sentenceDuration = (Integer) arguments[1];
        if (arguments[2] instanceof WLServiceDescriptor) {
            wlDescriptor = (WLServiceDescriptor) arguments[2];

            // start thread
            if (!cancelled) {
                new WLThread().start();
            } else {
                progressInterrupt("The process was cancelled");
            }
        } else if (arguments[2] instanceof String chainFilePath) {
            try {
                chainPath = Path.of(chainFilePath, "");
            } catch (InvalidPathException ipe) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.log(Level.WARNING, "Invalid tool chain file path: " + chainFilePath);
                }
                progressInterrupt("The tool chain file path is invalid");
                return;
            }
            if (arguments.length >= 4) {
                accessKey = (String) arguments[3];
            }

            // start thread
            if (!cancelled) {
                new ChainThread().start();
            } else {
                progressInterrupt("The process was cancelled");
            }
        }
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

    private void processTCFResponse(String tcfString) {
        if (tcfString == null) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "The TCF to parse is null");
            }
            progressInterrupt("The TCF to parse is null");
            return;
        }

        progressUpdate(62, "Converting returned content to a transcription.");
        TCFtoTranscription tctt = new TCFtoTranscription();
        tctt.setDefaultDuration(sentenceDuration);
        tctt.setTiersToInclude(true, true, true); // (true, false, false);

        try {
            transcription2 = tctt.createTranscription(tcfString);

            // check tier names and rename where necessary so that tiers/annotations are not overwritten
            updateTierNames(transcription, transcription2);

            progressUpdate(70, "Created a transcription, starting to merge");
            mergeCommand = (MergeTranscriptionsByAddingCommand) ELANCommandFactory.createCommand(transcription,
                                                                                                 ELANCommandFactory.WEBLICHT_MERGE_TRANSCRIPTIONS);
            mergeCommand.execute(transcription,
                                 new Object[] {transcription2, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE});

            progressComplete("New tiers were added to the transcription");
        } catch (SAXException | IOException ex) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "Error parsing tcf file: " + ex.getMessage());
            }
            progressInterrupt("Error parsing tcf file: " + ex.getMessage());
        }
    }

    /**
     * @param refTrans the transcription the new tiers have to be added to
     * @param nextTrans the transcription resulting from the returned content by the service
     */
    private void updateTierNames(Transcription refTrans, Transcription nextTrans) {
        String tierName;
        final int MAX_COUNT = 50; // arbitrary number of attempts

        for (Tier t : nextTrans.getTiers()) {
            tierName = t.getName();

            if (refTrans.getTierWithId(tierName) != null) {
                // add or update counter suffix
                int count = 1;
                String newName = tierName;
                do {
                    newName = tierName + "-" + count++;
                } while (refTrans.getTierWithId(newName) != null && count < MAX_COUNT);

                if (count < MAX_COUNT) {
                    t.setName(newName);
                }
            }
        }
    }

    /**
     * A thread that performs the calling of the web services.
     *
     * @author Han Sloetjes
     */
    private class WLThread extends Thread {

        /**
         * Starts uploading text and possibly calls additional services.
         */
        @Override
        public void run() {
            /*
             * The actual processing:
             * - (60%) uploading the text (and subsequently tcf) to predefined service(s)
             * - (10%) convert result tcf to transcription
             * - (25%) merge transcriptions
             */
            int perCycle = 30;

            progressUpdate(5, "Uploading text to WebLicht...");
            if (cancelled) {
                return;
            }
            WebLichtWsClient wsClient = new WebLichtWsClient();
            String tcfToParse = null;
            // configure
            String tcf = null;
            try {
                tcf = wsClient.convertPlainText(inputText);
                progressUpdate(perCycle, "Converted input text to TCF format");
            } catch (IOException ioe) {
                LOG.warning("Error converting text to TCF: " + ioe.getMessage());
                //progressInterrupt("Error converting text to TCF: " + ioe.getMessage());
                // programmatically convert to TCF
                tcf = textInTCF(inputText);
                progressUpdate(perCycle, "An error occurred, converted input text to TCF format locally");
                //return;
            }
            // immediately upload the contents to a sentence splitter
            if (tcf != null) {
                if (cancelled) {
                    return;
                }
                //System.out.println("TCF 1:");
                //System.out.println(tcf);
                tcfToParse = tcf;
                //String sentenceTokenUrl = "service-opennlp-1_5/tcf/detect-sentences/tokenize";
                //String sentenceTokenUrl = "service-opennlp/annotate/tok-sentences";

                String tcf2 = null;
                try {
                    tcf2 = wsClient.callWithTCF(wlDescriptor.fullURL, tcf);
                    progressUpdate(2 * perCycle, "Uploaded for tokenization of the sentences");
                } catch (IOException ioe) {
                    LOG.warning("Error while calling the tokenizer service: " + ioe.getMessage());
                    progressUpdate(2 * perCycle, "Error while calling the tokenizer service: " + ioe.getMessage());
                }

                if (tcf2 != null) {
                    //System.out.println("TCF 2:");
                    //System.out.println(tcf2);
                    if (cancelled) {
                        return;
                    }
                    tcfToParse = tcf2;
                } else {
                    progressUpdate(60, "Unable to get tokens, creating tiers...");
                }

                if (tcfToParse != null) {
                    if (cancelled) {
                        return;
                    }
                    processTCFResponse(tcfToParse);
                    /*
                    progressUpdate(62, "Converting returned content to a transcription.");
                    TCFtoTranscription tctt = new TCFtoTranscription();
                    tctt.setDefaultDuration(sentenceDuration);
                    tctt.setTiersToInclude(true, false, false);

                    try {
                        transcription2 = tctt.createTranscription(tcfToParse);

                        // check tier names and rename where necessary so that tiers/annotations are not overwritten
                        updateTierNames(transcription, transcription2);

                        progressUpdate(70, "Created a transcription, starting to merge");
                        mergeCommand = (MergeTranscriptionsByAddingCommand)
                                ELANCommandFactory.createCommand(transcription, ELANCommandFactory
                                .WEBLICHT_MERGE_TRANSCRIPTIONS);
                        mergeCommand.execute(transcription, new Object[]{transcription2, null, Boolean.TRUE, Boolean
                        .FALSE, Boolean.FALSE});

                        progressComplete("New tiers were added to the transcription");
                    } catch (SAXException sax) {
                        LOG.warning("Error parsing tcf file: " + sax.getMessage());
                        progressInterrupt("Error parsing tcf file: " + sax.getMessage());
                    } catch (IOException ioe) { //??
                        LOG.warning("Error parsing tcf file: " + ioe.getMessage());
                        progressInterrupt("Error parsing tcf file: " + ioe.getMessage());
                    }
                    */
                }
            } else {
                LOG.warning("Unknown error converting text to TCF");
                progressInterrupt("Unknown error converting text to TCF");
            }

        }

        /**
         * Wraps the input text in a TCF body with {@code lang} set to unknown.
         *
         * @param inputText the input text, should be escaped probably
         *
         * @return the TCF XML as a string
         */
        private String textInTCF(String inputText) {

            String builder = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                             + "<D-Spin xmlns=\"http://www.dspin.de/data\" version=\"0.4\">"
                             + "<MetaData xmlns=\"http://www.dspin.de/data/metadata\">"
                             + "<source></source>"
                             + "</MetaData>"
                             + "<TextCorpus xmlns=\"http://www.dspin.de/data/textcorpus\" lang=\"unknown\">"
                             + "<text>"
                             + inputText
                             + "</text>"
                             + "</TextCorpus>"
                             + "</D-Spin>";

            return builder;
        }

    }

    /**
     * A thread that sends a tool chain file and textual contents to WaaS (WebLicht as a Service). The content should be
     * compatible with the input expected by the first tool in the chain. <p> This variant is based on {@link HttpClient}.
     */
    private class ChainThread extends Thread {
        String waasUrl = "https://weblicht.sfs.uni-tuebingen.de/WaaS/api/1.0/chain/process";

        @Override
        public void run() {
            if (cancelled) {
                return;
            }

            progressUpdate(5, "Preparing data for uploading to WebLicht...");

            WsClientRest wsClient = new WsClientRest();
            // starts uploading the text with the chain file
            Map<String, Object> inputMap = new HashMap<String, Object>(2);
            inputMap.put("content", inputText);
            inputMap.put("chains", chainPath);
            inputMap.put("apikey", accessKey);
            String boundary = UUID.randomUUID().toString();
            HttpRequest.BodyPublisher bodyPub = wsClient.getMultiPartTextPublisher(inputMap, boundary);
            if (bodyPub == null) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.log(Level.WARNING, "Failed to created multipart content to send to the web service");
                }
                progressInterrupt("Error while constructing the text and tool chain contents");
                return;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                                             .uri(URI.create(waasUrl))
                                             .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                                             .header("Accept", "*/*")
                                             .header("User-Agent", "ELAN/" + ELAN.getVersionString())
                                             .timeout(Duration.ofSeconds(15))
                                             .POST(bodyPub)
                                             .build();

            progressUpdate(20, "Uploading text to tool chain service...");
            String tcfResp = null;
            try {
                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) { // expected: 200 OK
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.log(Level.WARNING, "Received an error response code from web service: " + response.statusCode());
                        LOG.log(Level.WARNING, "Error message: " + response.body());
                    }
                    progressInterrupt("Error code returned by the web service: " + response.statusCode());
                    return;
                }
                tcfResp = response.body();

            } catch (IOException | InterruptedException e) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.log(Level.WARNING, "An error occurred while sending the text to the service: " + e.getMessage());
                }
                //e.printStackTrace();
                progressInterrupt("Error while sending the text to the service: " + e.getMessage());
            }

            if (tcfResp == null) { // unlikely
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.log(Level.WARNING, "Did not receive a response body from the service");
                }
                progressInterrupt("Did not receive a response body from the service");
                return;
            }
            if (cancelled) {
                return;
            }
            progressUpdate(60, "Received results from tool chain service...");
            // process tcf like before
            //System.out.println(tcfResp);
            processTCFResponse(tcfResp);
        }

    }


}
