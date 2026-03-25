package mpi.eudico.client.annotator.comments;

import eu.dasish.annotation.schema.Action;
import eu.dasish.annotation.schema.*;
import eu.dasish.annotation.schema.AnnotationBody.XmlBody;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.DebugInputStream;
import mpi.eudico.util.RandomNumberGenerator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.*;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.xml.bind.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * A client for logging in to a comment web service and for uploading and downloading of comment envelopes.
 */
public class CommentWebClient implements ClientLogger  {
    public static final char[] MULTIPART_CHARS =
        "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    /**
     * Some constants which are basically compile-time selections to include or exclude some functionality.
     */
    static final boolean DEBUG = CommentManager.DEBUG;
    static final int HTTP_CLIENT_ERROR = 400; // any responses >= 400 are errors
    static final int HTTP_UNAUTHORIZED = 401;
    static final int HTTP_FORBIDDEN = 403;
    static final int HTTP_NOT_FOUND = 404;
    private static final boolean CREATE_CACHED_REPRESENTATION = false;
    private static final boolean ALWAYS_CREATE_CACHED_REPRESENTATION = false;
    private static final String CRLF = "\r\n";
    private static final String BOUNDARY = generateBoundary();
    private static final String BOUNDARY_LINE = "--" + BOUNDARY + CRLF;
    private static final String BOUNDARY_END = "--" + BOUNDARY + "--" + CRLF + CRLF;
    private static final String TEMP_TARGET_REF = "__TEMP_TARGET_REF__";
    private static final String TEMP_ANNOTATION_REF = "__TEMP_ANNOTATION_REF__";
    /**
     * Maps from Transcription urn to preferred DWAN Target URL. The server may assign different ones but if we have a choice
     * we'll use this one. NOTE: each transcription has its own web client, so there will be only one entry in the map.
     * Therefore, it is overkill. To be fixed later.
     */
    private final Map<String, String> urnToTargetURL;
    private final HttpClientContext context;
    private final CookieStore cookieStore;
    private final ObjectFactory objectFactory;
    private final TranscriptionImpl transcription;
    /**
     * The base URL for the web services
     */
    URI serviceURL;
    /**
     * Pre-resolved based on serviceURL
     */
    URI apiAnnotationsServiceURL;
    /**
     * resolve relative URLs from DWAN relative to this
     */
    URI resolveBaseURL;
    private JAXBContext jc;
    private Unmarshaller unmarshaller;
    /**
     * This is the path part from the serviceURL (without hostname) plus /api/
     */
    private String serviceUrlPath;
    private CloseableHttpClient httpClient;
    private Marshaller marshaller;
    private String userName;
    private String loggedInPincipalURIString;
    private boolean isLoggedIn;
    private DatatypeFactory datatypeFactory;
    private StatusLine lastStatusLine;

    /**
     * Make constructor private so users are forced to use the factory function. Initialize the unmarshaller (for use in the
     * unmarshal() method).
     *
     * @param t the transcription to create a web client for
     */
    private CommentWebClient(TranscriptionImpl t) {
        urnToTargetURL = new HashMap<>();
        transcription = t;

        try {
            jc = JAXBContext.newInstance(eu.dasish.annotation.schema.ObjectFactory.class);
            unmarshaller = jc.createUnmarshaller(); // XML -> tree
            marshaller = jc.createMarshaller();        // tree -> XML
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // The schema location will later be overridden, because it is
            // relative to the service URL.
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
                                   "http://lux17.mpi.nl/ds/webannotator-basic/SCHEMA/DASISH-schema.xsd");
        } catch (JAXBException e) {
            LOG.log(Level.WARNING, "(Un)marshaling issue.", e);

        }

        objectFactory = new ObjectFactory();

        // DatatypeFactory is used when converting dates
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            LOG.log(Level.WARNING, "Configuration issue.", e);
        }

        cookieStore = new BasicCookieStore();
        httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

        // The context is used in .execute() requests
        context = HttpClientContext.create();

        isLoggedIn = false;
    }

    /**
     * Factory for the singleton CommentWebClient. (Note: this doesn't really seem to be a singleton?)
     *
     * @param t the transcription to get the client for
     *
     * @return the one and only CommentWebClient.
     */
    public static CommentWebClient getCommentWebClient(TranscriptionImpl t) {
        return new CommentWebClient(t);
    }

    /**
     * Get the "file name" part of a URL path.
     * <p>Example: /ds/webannotatornonshibb/api/annotations/632911a9-9e4c-40f0-a45a-22b21e6615a9
     * => 632911a9-9e4c-40f0-a45a-22b21e6615a9
     *
     * @param url
     *
     * @return
     */
    private static String lastPathPart(String url) {
        int slashPos = url.lastIndexOf('/');
        if (slashPos > 0) {
            return url.substring(slashPos + 1);
        } else {
            return url;
        }
    }

    /**
     * Example result: "r4DfdqFJVA9dtYH0RodeGRzUeqenbDi1GdGx_I";
     */

    private static String generateBoundary() {
        // The pool of ASCII chars to be used for generating a multipart boundary.
        final StringBuilder buffer = new StringBuilder();
        RandomNumberGenerator randomNumberGenerator = RandomNumberGenerator.getInstance();
        final int count = randomNumberGenerator.getNewRandomNumber(11) + 30; // a random size from 30 to 40
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[randomNumberGenerator.getNewRandomNumber(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }

    /**
     * Log the user out. Since we're logged in because of a session cookie, this means clearing the cookies. There seems to
     * be no active request to the webserver to tell it that the session is over.
     */
    public void logout() {
        loggedInPincipalURIString = "";
        cookieStore.clear();
        isLoggedIn = false;
    }

    /**
     * Clean-up of the Web Client. Don't use it anymore after calling this.
     */
    public void close() {
        logout();
        try {
            httpClient.close();
            httpClient = null;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Exception while closing.", e);
        }
    }

    /**
     * Register who wants to connect to where.
     *
     * @param serviceURL The Service Provider to connect to (end with a /)
     * @param user the user
     *
     * @return {@code true} if login succeeded, {@code false} otherwise
     */
    public boolean login(String serviceURL, String user) {
        if (!serviceURL.endsWith("/")) {
            serviceURL += "/";
        }

        try {
            this.serviceURL = new URI(serviceURL);
            // The server will give relative URLs such as
            // /ds/webannotatornonshibb/api/targets/a355e057-eb58-4ba6-a0df-092a70afb4e2
            // Resolve these with respect to the resolveBaseURL.
            // this.resolveBaseURL = new URI(this.serviceURL.getScheme() + this.serviceURL.getAuthority());
            // Because it starts with a slash /, there is no need to chop off
            // the path from the serviceURL. This way, it will keep working if the relative URL
            // will change to start with "api/targets/....".
            this.resolveBaseURL = this.serviceURL;
        } catch (URISyntaxException e) {
            LOG.severe("Service URL is bad: URISyntaxException");
            return false;
        }
        try {
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, resolveBaseURL + "SCHEMA/DASISH-schema.xsd");
        } catch (PropertyException e) {
            LOG.log(Level.WARNING, "Exception while setting property.", e);
        }
        this.userName = user;

        // From here on we're going to assume that this.serviceURL represents
        // a valid URL.

        apiAnnotationsServiceURL = this.serviceURL.resolve("api/annotations");
        this.serviceUrlPath = this.serviceURL.getPath() + "api/";

        return reLogin();
    }

    /**
     * Ask the user for a password and login. Repeat until there is success, or the user cancelled. Could be used to
     * automatically re-login when the server thinks you are not authorized.
     *
     * @return whether the user was logged in eventually.
     */
    public boolean reLogin() {
        boolean success = false;
        while (!success) {
            char[] password = getPassword(serviceURL, userName);
            if (password == null) {
                LOG.warning("password == null, user probably hit CANCEL in getPassword() dialog");
                return false;
            }
            success = reLogin(userName, password);
            if (!success) {
                LOG.severe("reLogin(userName, password) failed; password is probably incorrect");
            }
        }
        return success;
    }

    /**
     * Log the user in with a given password, and find out their identity as seen by the server (their PrincipalURI).
     *
     * @param user the name of the user
     * @param password the password of the user
     *
     * @return boolean whether the login was successful.
     */
    public boolean reLogin(String user, char[] password) {
        boolean loggedIn = loginByPOST("api/authentication/login", user, new String(password));
        loggedIn = loggedIn && getPrincipalURI();

        this.isLoggedIn = loggedIn;

        return loggedIn;
    }

    /**
     * Puts up a dialog and asks the user for a password, or cancel.
     *
     * @param serviceURL the Service Provider to connect to (end with a /)
     * @param username the name of the user
     *
     * @return null if the user cancelled, a char[] with password otherwise.
     */
    private char[] getPassword(URI serviceURL, String username) {
        // Enter password for %s:
        String prompt = String.format(ElanLocale.getString("CommentViewer.EnterPassword"), username);
        JPanel panel = new JPanel();
        JLabel label = new JLabel(prompt);
        JPasswordField pass = new JPasswordField(16);
        panel.add(label);
        panel.add(pass);
        String[] options = new String[] {ElanLocale.getString("Button.OK"), ElanLocale.getString("Button.Cancel")};
        int option = JOptionPane.showOptionDialog(null,
                                                  panel,
                                                  serviceURL.toString(),
                                                  JOptionPane.YES_NO_CANCEL_OPTION,
                                                  JOptionPane.PLAIN_MESSAGE,
                                                  null,
                                                  options,
                                                  options[0]);

        if (option == 0) { // pressing OK button
            return pass.getPassword();
        }

        return new char[0];
    }

    /**
     * Process the login page which is based on a POST. Inspired by an Apache example:
     * https://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org/apache/http/examples/client
     * /ClientFormLogin.java
     *
     * @param loginPage the URL of the login page
     * @param user the name of the user
     * @param password the password of the user
     */
    private boolean loginByPOST(String loginPage, String user, String password) {
        URI startPage = serviceURL.resolve(loginPage);
        URI redirectedPage;
        String postToPage = "j_spring_security_check"; // TODO should detect this from form/@action, if possible
        boolean loggedIn = true;    // assume it will succeed

        try {
            HttpGet httpget = new HttpGet(startPage);

            try (CloseableHttpResponse response1 = httpClient.execute(httpget, context)) {
                HttpEntity entity = response1.getEntity();

                if (DEBUG) {
                    System.out.printf("Login form1 GET %s =>\n%s\n", startPage, response1.getStatusLine());
                }
                EntityUtils.consume(entity);
                if (response1.getStatusLine().getStatusCode() >= HTTP_CLIENT_ERROR) {
                    warnAboutLoginProblem(response1.getStatusLine().toString());
                    return false;
                }

                if (DEBUG) {
                    System.out.println("Initial set of cookies:");
                    List<Cookie> cookies = cookieStore.getCookies();
                    if (cookies == null || cookies.isEmpty()) {
                        System.out.println("None");
                    } else {
                        for (Cookie cookie : cookies) {
                            System.out.println("- " + cookie.toString());
                        }
                    }
                }
                /*
                 * Get the page where we were redirected to.
                 */
                HttpHost target = context.getTargetHost();
                List<URI> redirectLocations = context.getRedirectLocations();
                redirectedPage = URIUtils.resolve(httpget.getURI(), target, redirectLocations);
                if (DEBUG) {
                    System.out.println("Final HTTP location: " + redirectedPage.toASCIIString());
                }
                // Expected to be an absolute URI
                // redirectedPage = serviceURL.resolve(postToPage);
                redirectedPage = redirectedPage.resolve(postToPage);

                HttpUriRequest login = RequestBuilder.post()
                                                     .setUri(redirectedPage)
                                                     .addParameter("username", user)
                                                     .addParameter("password", password)
                                                     .addParameter("submit", "submit")
                                                     .build();

                try (CloseableHttpResponse response2 = httpClient.execute(login, context)) {
                    HttpEntity entity2 = response2.getEntity();

                    if (DEBUG) {
                        System.out.printf("Login form2 %s =>\n%s\n", login.toString(), response2.getStatusLine());
                    }
                    EntityUtils.consume(entity2);
                    if (response2.getStatusLine().getStatusCode() >= HTTP_CLIENT_ERROR) {
                        warnAboutLoginProblem(response1.getStatusLine().toString());
                        loggedIn = false;
                    }

                    if (DEBUG) {
                        System.out.println("Post logon cookies:");
                        List<Cookie> cookies = cookieStore.getCookies();
                        if (cookies.isEmpty()) {
                            System.out.println("None");
                        } else {
                            for (Cookie cookie : cookies) {
                                System.out.println("- " + cookie.toString());
                            }
                        }
                    }
                }
            } catch (javax.net.ssl.SSLException slException) {
                LOG.log(Level.SEVERE, "Logging in; got SSLException. Is 'sunjce_provider.jar' necessary?", slException);
            }


        } catch (javax.net.ssl.SSLException sslException) {
            LOG.log(Level.SEVERE, "Logging in; got SSLException. Is 'sunjce_provider.jar' necessary?", sslException);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Some thing wrong with the I/O or protocol.", e);
            return false;
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, "URI syntax issue.", e);
            return false;
        }

        return loggedIn;
    }

    /**
     * Returns whether the user is logged in.
     *
     * @return true if we seem to have logged in successfully.
     */
    public boolean isLoggedIn() {
        return this.isLoggedIn;
    }

    /**
     * Unmarshals (XML to Object) with JAXB methods.
     *
     * @param <T> the type of element
     * @param inputStream the stream to unmarshal
     *
     * @return the value object
     *
     * @throws JAXBException any marshalling exception
     */
    @SuppressWarnings("unchecked")
    public <T> T unmarshal(InputStream inputStream) throws JAXBException {
        JAXBElement<T> doc = (JAXBElement<T>) unmarshaller.unmarshal(inputStream);
        return doc.getValue();
    }

    /**
     * Unmarshals (XML to Object) with JAXB methods.
     */
    @SuppressWarnings({"unchecked", "unused"})
    private <T> T unmarshal(Class<T> docClass, InputStream inputStream) throws JAXBException {
        String packageName = docClass.getPackage().getName();   // "eu.dasish.annotation.schema"
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller u = jc.createUnmarshaller();
        // Up to here needs to be done only once, probably?
        JAXBElement<T> doc = (JAXBElement<T>) u.unmarshal(inputStream);
        return doc.getValue();
    }

    /**
     * Marshals (Object to XML output stream) with JAXB methods. See
     * https://jaxb.java.net/tutorial/section_4_5-Calling-marshal.html
     *
     * @param <T> the type of the element
     * @param document the element to marshal
     * @param os the output stream
     *
     * @throws JAXBException if an error occurs during marshalling
     */
    public <T> void marshal(JAXBElement<T> document, OutputStream os) throws JAXBException {
        marshaller.marshal(document, os);
    }

    /**
     * A somewhat general DELETE method. Has no argument and expects no response object.
     *
     * @param uri the service URL
     */
    public void doDELETE(URI uri) {
        if (DEBUG) {
            System.out.println("DELETE: " + uri.toASCIIString());
        }

        try (CloseableHttpResponse res = httpClient.execute(new HttpDelete(uri), context)) {
            if (DEBUG) {
                System.out.println("response: " + res.getStatusLine());
            }

            lastStatusLine = res.getStatusLine();
            boolean shouldWarn = false;

            if (lastStatusLine.getStatusCode() == HTTP_UNAUTHORIZED) {
                isLoggedIn = false;
                shouldWarn = true;
            } else if (lastStatusLine.getStatusCode() == HTTP_FORBIDDEN) {
                // this comment belongs to some other user (even though we checked for that earlier) "delete"
                warnAboutForbidden(ElanLocale.getString("CommentViewer.WarnAboutForbiddenDelete"));
            } else if (lastStatusLine.getStatusCode() >= HTTP_CLIENT_ERROR) {
                shouldWarn = true;
            }
            EntityUtils.consume(res.getEntity());

            if (shouldWarn) {
                warnAboutServerResponse(ElanLocale.getString("CommentViewer.WarnAboutSR.Removing"),
                                        lastStatusLine.toString());
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Some thing wrong with the I/O or protocol.", e);
        }
    }

    /**
     * A somewhat general GET method. Has no data to send to the server but expects a Response object.
     *
     * @param <R> the type of response object
     * @param uri the service URL
     *
     * @return the object that was retrieved or {@code null}
     */
    public <R> R doGET(URI uri) {

        if (DEBUG) {
            System.out.println("GET: " + uri.toASCIIString());
        }

        try (CloseableHttpResponse res = httpClient.execute(new HttpGet(uri), context)) {


            if (DEBUG) {
                System.out.println("response: " + res.getStatusLine());
            }

            R result = null;
            lastStatusLine = res.getStatusLine();

            if (lastStatusLine.getStatusCode() < HTTP_CLIENT_ERROR) {
                InputStream inputStream = res.getEntity().getContent();
                if (DEBUG) {
                    inputStream = new DebugInputStream(inputStream);
                }
                result = this.unmarshal(inputStream);
            } else {
                if (lastStatusLine.getStatusCode() == HTTP_UNAUTHORIZED) {
                    isLoggedIn = false;
                }
                warnAboutServerResponse(ElanLocale.getString("CommentViewer.WarnAboutSR.Getting"),
                                        lastStatusLine.toString());

            }

            return result;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error consuming the resource.", e);
        }

        // If failed, return nothing.
        return null;
    }

    /**
     * A somewhat general POST or PUT method with an Argument and Result type. The argument has already been converted to an
     * InputStreamEntity.
     *
     * @param <R> the type of the result object
     * @param req the HTTP request object
     *
     * @return the result {@code null}
     */
    public <R> R doPOSTorPUTWithResult(HttpEntityEnclosingRequestBase req) {
        try (CloseableHttpResponse res = httpClient.execute(req, context)) {
            // Execute the request
            if (DEBUG) {
                System.out.println("response: " + res.getStatusLine());
            }

            R object = null;
            lastStatusLine = res.getStatusLine();
            boolean shouldWarn = false;

            if (lastStatusLine.getStatusCode() < HTTP_CLIENT_ERROR) {
                // Catch the return data stream and create an object from it.
                InputStream inputStream = res.getEntity().getContent();
                if (DEBUG) {
                    inputStream = new DebugInputStream(inputStream); // print a copy to the console
                }
                object = this.unmarshal(inputStream);
            } else if (lastStatusLine.getStatusCode() == HTTP_UNAUTHORIZED) {
                isLoggedIn = false;
                shouldWarn = true;
            } else if (lastStatusLine.getStatusCode() == HTTP_FORBIDDEN) {
                // this comment belongs to some other user (even though we checked for that earlier)
                // "modify
                warnAboutForbidden(ElanLocale.getString("CommentViewer.WarnAboutForbiddenModify"));
            } else {
                shouldWarn = true;
            }

            if (shouldWarn) {
                warnAboutServerResponse(ElanLocale.getString("CommentViewer.WarnAboutSR.Putting"),
                                        lastStatusLine.toString());
            }

            return object;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Some thing wrong with the I/O or protocol.", e);
        } catch (JAXBException e) {
            LOG.log(Level.SEVERE, "Marshaling or parsing error.", e);
        }

        // If failed, return nothing.
        return null;
    }

    /**
     * POST or PUT a plain String.
     *
     * @param <R> the type of the result object
     * @param req the HTTP request object
     * @param textPlain the string to POST or PUT
     *
     * @return the result or {@code null}
     */
    public <R> R doPOSTorPUTWithResult(HttpEntityEnclosingRequestBase req, String textPlain) {
        if (DEBUG) {
            System.out.printf("POST/PUT text/plain:\n%s\n", textPlain);
        }
        StringEntity reqEntity = new StringEntity(textPlain, ContentType.TEXT_PLAIN);
        req.setEntity(reqEntity);
        return doPOSTorPUTWithResult(req);
    }

    /**
     * POST or PUT an object of type ARG, which will be converted to XML. This version can do without a buffered version of
     * je (if, as here, the Entity is used for sending its value).
     *
     * @param <P> the type of payload to POST or PUT
     * @param <R> the type of the result object
     * @param req the HTTP request object
     * @param je the element to marshal
     *
     * @return the object that was retrieved or {@code null}
     */
    public <P, R> R doPOSTorPUTWithResult(HttpEntityEnclosingRequestBase req, final JAXBElement<P> je) {
        ContentProducer producer = outStream -> {
            try {
                // Serialize the argument
                marshal(je, outStream);
                if (DEBUG) {
                    marshal(je, System.out);
                }
            } catch (JAXBException e) {
                LOG.log(Level.SEVERE, "(Un)marshaling issue.", e);
            }
        };
        AbstractHttpEntity reqEntity = new EntityTemplate(producer);
        reqEntity.setContentType(ContentType.APPLICATION_XML.getMimeType());
        req.setEntity(reqEntity);
        return doPOSTorPUTWithResult(req);
    }

    /**
     * POST or PUT an object of type ARG, which will be converted to XML. This older version needs to buffer the marshalled
     * version of je.
     *
     * @param <P> the type of payload to POST or PUT
     * @param <R> the type of result object
     * @param req the HTTP request object
     * @param je the element to marshal to an entity
     *
     * @return the result or {@code null}
     */
    @SuppressWarnings("unused")
    private <P, R> R doPOSTorPUTWithResultOLD(HttpEntityEnclosingRequestBase req, final JAXBElement<P> je) {
        try {
            byte[] bytes;

            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                marshal(je, output);
                bytes = output.toByteArray();
            }

            AbstractHttpEntity reqEntity =
                new InputStreamEntity(new ByteArrayInputStream(bytes), bytes.length, ContentType.APPLICATION_XML);
            if (DEBUG) {
                System.out.printf("POST/PUT application/xml:\n%s\n", new String(bytes));
            }
            req.setEntity(reqEntity);
            return doPOSTorPUTWithResult(req);
        } catch (JAXBException | IOException e) {
            LOG.log(Level.SEVERE, "(Un)marshaling or I/O issue.", e);
        }

        return null;
    }

    /**
     * A somewhat general POST method with an Argument and Result type.
     *
     * @param <P> the type of payload to POST
     * @param <R> the type of result object
     * @param uri the service URL
     * @param je the element to marshal
     *
     * @return the result or {@code null}
     */
    public <P, R> R doPOST(URI uri, JAXBElement<P> je) {
        HttpPost req = new HttpPost(uri);
        if (DEBUG) {
            System.out.println("POST: " + uri.toASCIIString());
        }
        return doPOSTorPUTWithResult(req, je);
    }

    /**
     * A somewhat general PUT method with an Argument and Result type.
     *
     * @param <P> the type of object to PUT
     * @param <R> the type of result object
     * @param uri the service URL
     * @param je the element to marshal
     *
     * @return the result or {@code null}
     */
    public <P, R> R doPUT(URI uri, JAXBElement<P> je) {
        HttpPut req = new HttpPut(uri);
        if (DEBUG) {
            System.out.println("PUT: " + uri.toASCIIString());
        }
        return doPOSTorPUTWithResult(req, je);
    }

    /**
     * A somewhat general PUT method with a string argument and result type.
     *
     * @param <R> the type of result object
     * @param uri the service URL
     * @param textPlain the string to put
     *
     * @return the result or {@code null}
     */
    public <R> R doPUT(URI uri, String textPlain) {
        HttpPut req = new HttpPut(uri);
        if (DEBUG) {
            System.out.println("PUT: " + uri.toASCIIString());
        }
        return doPOSTorPUTWithResult(req, textPlain);
    }

    /**
     * Dialog containing the warning message
     *
     * @param action the message to shown
     */
    public void warnAboutForbidden(String action) {
        // Permission to %s the comment\n
        // has been denied.\n
        // The comment has not been modified on the server.
        String fmt = ElanLocale.getString("CommentViewer.WarnAboutForbidden");
        String msg = String.format(fmt, action);
        JOptionPane.showMessageDialog(null, msg, ElanLocale.getString("Message.Warning"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Dialog to show about the server error
     *
     * @param process the process
     * @param message the message to be displayed
     */
    public void warnAboutServerResponse(String process, String message) {
        // "There is a problem with %s.\n" +
        // "The server responded:\n"
        String fmt = ElanLocale.getString("CommentViewer.WarnAboutServerResponse");
        String msg = String.format(fmt, process) + message;
        LOG.severe(msg);

        JOptionPane.showMessageDialog(null, msg, ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Method to show about the login problem
     *
     * @param message the message to be displayed
     */
    public void warnAboutLoginProblem(String message) {
        warnAboutServerResponse(ElanLocale.getString("CommentViewer.WarnAboutSR.Login"), message);

    }

    /**
     * After logging in, we can find the URI that has been assigned to this principal. To do that, GET
     * (https://corpus1.mpi.nl/ds/webannotator-basic/)api/authentication/principal
     *
     * @return a boolean which indicates we appear to be logged in successfully.
     */
    private boolean getPrincipalURI() {
        URI uri = serviceURL.resolve("api/authentication/principal");
        Principal p;

        p = doGET(uri);
        if (p != null) {
            loggedInPincipalURIString = p.getHref();
        } else {
            loggedInPincipalURIString = "";
        }

        return !loggedInPincipalURIString.isEmpty();
    }

    /**
     * GET an AnnotationInfoList from a web service URI.
     * <p>{@code
     * api/annotations?access=read&link={uri}&matchMode=exact }
     *
     * @param uri the service URI
     *
     * @return an annotation info list or {@code null}
     */
    public AnnotationInfoList getAnnotationInfoList(URI uri) {
        try {
            URIBuilder ub;
            ub = new URIBuilder(serviceURL.toString() + "api/annotations");
            ub.addParameter("access", "read");
            ub.addParameter("link", uri.toString()); // takes care of necessary escaping
            ub.addParameter("matchMode", "exact");

            AnnotationInfoList ail = this.doGET(ub.build());
            if (ail != null) {
                return ail;
            }
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, "URI malformed.", e);
        }

        // If failed, return null to indicate  failure
        return null;
    }

    /**
     * Get an annotation.
     * <p>Example URL:
     * https://corpus1.mpi.nl/ds/webannotator-basic/api/annotations/09e1ebaf-fca6-4509-ac26-362b2301f37f
     *
     * @param uri the service URI
     *
     * @return Annotation or null.
     */
    public Annotation getAnnotation(URI uri) {
        return this.doGET(uri);
    }

    /**
     * Check if the Annotation can be modified (or not). This is the case when the public permission is "write" or when the
     * logged-in user occurs in the permission list with access level "write". In all other cases, the Annotation is
     * read-only.
     *
     * @param ann the annotation to check
     *
     * @return true if the Annotation is read-only.
     */
    private boolean isReadOnly(Annotation ann) {
        PermissionList pl = ann.getPermissions();

        Access ac = pl.getPublic();
        if (ac.value().equals("write")) {
            return false;
        }

        for (Permission p : pl.getPermission()) {
            if (p.getPrincipalHref().equals(this.loggedInPincipalURIString)) {
                return !p.getLevel().value().equals("write");
            }
        }

        return true;
    }

    /**
     * Take an AnnotationInfoList, and look up the full information on all annotations in it. Extract the CommentEnvelope out
     * of each.
     * <p>If null is passed in (presumably due to an auth or communication failure),
     * return null as well.
     *
     * @param ail the annotation info list
     *
     * @return a list of the recovered CommentEnvelopes or {@code null}
     */
    private List<CommentEnvelope> getCommentEnvelopes(AnnotationInfoList ail, URI urn) {
        if (DEBUG) {
            System.out.println("getCommentEnvelopes: for " + urn.toASCIIString());
        }

        List<CommentEnvelope> lce = new ArrayList<>();
        String targetURI = null;

        Set<String> fetchedIDs = new HashSet<>();

        for (AnnotationInfo ai : ail.getAnnotationInfo()) {
            String firstTarget = getFirstTarget(ai);
            if (targetURI == null) {
                targetURI = firstTarget;
            } else {
                if (!targetURI.equals(firstTarget)) {
                    // something potentially weird? It can happen but we try to avoid it.
                    if (DEBUG) {
                        System.err.printf("Targets not consistent: first=%s, now=%s\n", firstTarget, targetURI);
                    }
                }
            }

            URI annotationRef =
                resolveBaseURL.resolve(ai.getHref()); // the (possibly relative) URI that identifies the actual annotation
            Annotation a = getAnnotation(annotationRef);
            AnnotationBody body;
            if (a != null && (body = a.getBody()) != null) {
                XmlBody xb = body.getXmlBody();
                if (xb != null) {
                    Element e = xb.getAny();
                    CommentEnvelope ce = new CommentEnvelope(e);

                    String id = ce.getMessageID();
                    if (fetchedIDs.contains(id)) {
                        // The database has multiple instances of the same ID!
                        // This is never going to work properly unless we delete one.
                        if (DEBUG) {
                            System.err.printf("Duplicate AnnotationID! %s\n", id);
                        }
                    } else {
                        // Override the URLs embedded in the message
                        // in favour of values that should always be correct;
                        // somebody may have deleted, then undeleted, the comment, which gives it
                        // a new URL and/or a new target.
                        ce.setMessageURL(a.getHref());
                        ce.setAnnotationFileURL(firstTarget);
                        // Get the server's Last Modified time.
                        ce.setLastModifiedOnServer(a.getLastModified());
                        // Check if we will be able to modify this comment.
                        ce.setReadOnly(isReadOnly(a));

                        lce.add(ce);
                        fetchedIDs.add(id);
                    }
                }
            }
        }

        // Remember this association for later, when we're making more Annotations on this Transcription
        if (targetURI != null) {
            if (DEBUG) {
                System.out.printf("urnToTargetURL: %s -> %s\n", urn.toString(), targetURI);
            }
            urnToTargetURL.put(urn.toString(), targetURI);
        }

        return lce;
    }

    /**
     * Get all comment envelopes from the server which are new to us. Don't add them yet or anything, that is for another
     * part of code to decide. (Maybe also save all comments that are not on the server yet?)
     * <p>We restrict the fetching of full annotations to those that seem new to
     * us. This is based on a cached value of the lastModified date of the server. That is really just an optimization. If a
     * comment is wrongly considered new enough here, further down the line it will be checked more thoroughly.
     * <p>If either {@code existing} or {@code hadThemAlready} is null, it just
     * fetches all annotations.
     * <p>If no annotations were fetched, it returns an empty list.
     * <p>If there is some kind of error in the process to fetch them, it returns
     * null. This indicates that the true value of the list is unknown, and should (for instance) not be used to delete local
     * comments.
     *
     * @param urn The URN of the Transcription
     * @param existing the Comments we already have (a non-modifiable list)
     * @param hadThemAlready pass an empty list. The list will be filled with the members from {@code existing} that were
     *     on the server but not fetched. This is so you can know they were not deleted.
     *
     * @return a list of comment envelopes
     */
    public List<CommentEnvelope> getCommentEnvelopes(URI urn, List<CommentEnvelope> existing,
                                                     List<CommentEnvelope> hadThemAlready) {
        AnnotationInfoList ail = getAnnotationInfoList(urn);

        if (ail == null) {
            return Collections.emptyList();
        }

        List<AnnotationInfo> lai = ail.getAnnotationInfo();
        if (lai == null || lai.isEmpty()) {
            return Collections.emptyList();
        }

        if (DEBUG) {
            System.out.printf("Start with %d annotations to fetch\n", ail.getAnnotationInfo().size());
        }

        // Now check all AnnotationInfo for potential freshness.
        if (existing != null && !existing.isEmpty() && hadThemAlready != null) {
            Map<String, CommentEnvelope> map = new HashMap<>();

            // make a map for quick access
            for (CommentEnvelope ce : existing) {
                map.put(ce.getMessageURL(), ce);
            }

            // Walk through the received list and see which annotations we have already
            Iterator<AnnotationInfo> iterator = lai.iterator();
            while (iterator.hasNext()) {
                AnnotationInfo ai = iterator.next();
                CommentEnvelope ce = map.get(ai.getHref());

                if (ce != null) {
                    XMLGregorianCalendar date = ce.getLastModifiedOnServer();

                    if (date != null && date.equals(ai.getLastModified())) {
                        // Ok, we have this one already and it wasn't modified.
                        hadThemAlready.add(ce);
                        // Remove it from the set to be fetched fully.
                        iterator.remove();
                        if (DEBUG) {
                            System.out.printf("No need to fetch %s: %s\n", ai.getHref(), date.toString());
                        }
                    } else {
                        if (DEBUG) {
                            System.out.printf("Need to fetch %s: my time: %s, server time: %s\n",
                                              ai.getHref(),
                                              (date != null ? date.toString() : "NULL"),
                                              ai.getLastModified().toString());
                        }
                    }
                } else {
                    if (DEBUG) {
                        System.out.printf("Need to fetch (don't have the URL) %s\n", ai.getHref());
                    }
                }
            }
        }

        if (DEBUG) {
            System.out.printf("Still %d annotations to fetch\n", ail.getAnnotationInfo().size());
        }

        return getCommentEnvelopes(ail, urn);
    }

    /**
     * Extract the first target (in DWAN-speak) from the annotationInfo. A target is the server-side representation of a
     * source (our URI). Since we only ever add one target for each source, we will be surprised to find more than one. Do
     * this mainly to know how the server refers to our Transcription.
     *
     * @param ai
     *
     * @return
     */
    private String getFirstTarget(AnnotationInfo ai) {
        ReferenceList list = ai.getTargets();
        List<String> list2 = list.getHref();

        if (list2 != null && !list2.isEmpty()) {
            return list2.get(0);
        }

        return "";
    }

    /**
     * See {@link #getFirstTarget(AnnotationInfo)}.
     */
    private String getFirstTarget(Annotation replyA) {
        TargetInfoList replyTIL = replyA.getTargets();

        if (replyTIL != null) {
            List<TargetInfo> lti = replyTIL.getTargetInfo();

            if (lti != null && !lti.isEmpty()) {
                TargetInfo ti2 = lti.get(0);
                return ti2.getHref();
            }
        }

        return null;
    }

    /**
     * Return whether the URL of an annotation seems to be known. Also try to test whether it is still up-to-date wrt service
     * URL changes (but this check may be imperfect).
     *
     * @param ce The CommentEnvelope we wish to check.
     */
    private boolean annotationURLIsKnown(CommentEnvelope ce) {
        String ceURL = ce.getMessageURL();

        if (ceURL.startsWith("http")) {
            // Check absolute URL
            return ceURL.startsWith(serviceURL + "api/");
        } else {
            // serviceURL should be known by now, and absolute.
            if (serviceURL == null || !serviceURL.isAbsolute()) {
                return false;
            }

            // Check relative URL
            return ceURL.startsWith(serviceUrlPath);
        }
    }

    /**
     * Take the (possibly relative) URL from a CommentEnvelope, and resolve it relative to the serviceURL. The "extra" part
     * is added too.
     *
     * @param ce the comment envelope
     * @param extra additional text to add to the URI
     *
     * @return the resolved URI
     */
    public URI resolveEnvelopeURL(CommentEnvelope ce, String extra) {
        if (annotationURLIsKnown(ce)) {
            try {
                return resolveBaseURL.resolve(ce.getMessageURL() + extra);
            } catch (IllegalArgumentException e) {
                LOG.log(Level.SEVERE, "Argument not accepted.", e);
            }
        }

        return null;
    }

    /**
     * From our CommentEnvelope, create a JAXB-annotated data structure in DWAN terms.
     */
    private Annotation createAnnotation(CommentEnvelope ce, AnnPostParams h) {
        // Some of the changes must not be made in the original,
        // so make annotation clone.
        CommentEnvelope cloneCE = ce.clone();

        h.sourceURL = ce.getAnnotationURIBase().toString();
        h.targetURL = /*ce.getAnnotationFileURL();*/ urnToTargetURL.get(h.sourceURL);

        h.annotationURLIsKnown = annotationURLIsKnown(ce);
        h.targetIsKnown = (h.targetURL != null && !h.targetURL.isEmpty());

        /*
         * If we have some placeholder values, change them in annotation copy of the CommentEnvelope.
         * The idea is that the server will put the real values into its database,
         * doing annotation global search and replace. We will pick them up as well and put them
         * in our local copies of the comments.
         */
        if (!h.annotationURLIsKnown) {
            cloneCE.setMessageURL(TEMP_ANNOTATION_REF);
        }
        if (!h.targetIsKnown) {
            cloneCE.setAnnotationFileURL(TEMP_TARGET_REF);
            h.targetURL = TEMP_TARGET_REF;
        } else {
            ce.setAnnotationFileURL(h.targetURL);
            cloneCE.setAnnotationFileURL(h.targetURL);
        }

        Element e;
        try {
            e = CommentManager.getElement(cloneCE);
        } catch (ParserConfigurationException pce) {
            LOG.log(Level.SEVERE, "Parsing issue", pce);
            return null;
        }

        // Check what "target" the backend has associated with this
        // URI. When the URI is completely new, there is no target yet;
        // it will be generated as soon as the annotation is posted.
        // Chicken and egg problem: we want the target recorded inside
        // the annotation.
        TargetInfo ti = new TargetInfo();
        ti.setHref(h.targetURL);
        ti.setLink(h.sourceURL);
        ti.setVersion(""); // TODO?

        Annotation annotation = new eu.dasish.annotation.schema.Annotation();

        annotation.setOwnerHref(loggedInPincipalURIString);
        annotation.setHref(cloneCE.getMessageURL());
        annotation.setId(lastPathPart(cloneCE.getMessageURL())); // xml:id is annotation required attribute

        AnnotationBody annotationBody = new AnnotationBody();
        XmlBody xmlBody = new XmlBody();
        xmlBody.setMimeType("text/xml");
        xmlBody.setAny(e);
        annotationBody.setXmlBody(xmlBody);

        annotation.setBody(annotationBody);
        String headline = cloneCE.getMessage();
        headline = headline.substring(0, Math.min(40, headline.length()));
        annotation.setHeadline(headline);
        annotation.setLastModified(datatypeFactory.newXMLGregorianCalendar(cloneCE.getCreationDateString()));

        TargetInfoList til = new TargetInfoList();
        til.getTargetInfo().add(ti);

        annotation.setTargets(til);

        // Set permissions to some default. Their presence is required.
        // Make the annotation world-readable, and writable by ourselves.
        PermissionList pl = new PermissionList();
        pl.setPublic(Access.READ);    // the public may READ this
        Permission p = new Permission();
        p.setPrincipalHref(loggedInPincipalURIString);
        p.setLevel(Access.WRITE);    // the user may WRITE this
        pl.getPermission().add(p);

        annotation.setPermissions(pl);

        return annotation;
    }


    /**
     * Common code for processing the ResponseBody after trying to update an annotation on the server.
     * <p>If the request was successful, it extracts the Last-Modified time from it.<br/>
     * If there was a 404, it retries with a POST to create a new Annotation.
     *
     * @param ce
     * @param rb
     *
     * @return rb
     */
    private ResponseBody process(CommentEnvelope ce, ResponseBody rb) {
        if (rb != null) {
            Annotation replyA = rb.getAnnotation();
            if (replyA != null) {
                ce.setLastModifiedOnServer(replyA.getLastModified());
            }
        } else {
            // Failed? Maybe somebody deleted the comment in the meantime.
            // Retry it as a POST.
            if (lastStatusLine.getStatusCode() == HTTP_NOT_FOUND) {
                ce.setMessageURL(""); // make URL unknown
                return putCommentEnvelope(ce);
            }
        }

        return rb;
    }

    /**
     * PUT a whole Annotation. <br/> This is the simple approach, but it does overwrite the permissions that may have been
     * changed outside our knowledge. It also updates the target.
     */

    @SuppressWarnings("unused")
    private ResponseBody putAnnotation(CommentEnvelope ce, Annotation a, AnnPostParams h) {
        URI uri = resolveEnvelopeURL(ce, "");

        if (uri == null) {
            return null;
        }
        JAXBElement<Annotation> ja = objectFactory.createAnnotation(a);

        ResponseBody rb = doPUT(uri, ja);
        return process(ce, rb);
    }

    /**
     * PUT just a Body (part of an Annotation).
     *
     * @param ce
     * @param a
     * @param h
     *
     * @return
     */
    private ResponseBody putBody(CommentEnvelope ce, Annotation a, AnnPostParams h) {
        URI uri = resolveEnvelopeURL(ce, "/body");
        if (uri == null) {
            return null;
        }

        AnnotationBody annotationBody = a.getBody();
        JAXBElement<AnnotationBody> ja = objectFactory.createAnnotationBody(annotationBody);

        ResponseBody rb = doPUT(uri, ja);
        return process(ce, rb);
    }

    /**
     * PUT just a Headline (part of an Annotation).
     *
     * @param ce
     * @param headline
     * @param h
     *
     * @return
     */
    private ResponseBody putHeadline(CommentEnvelope ce, String headline, AnnPostParams h) {
        URI uri = resolveEnvelopeURL(ce, "/headline");
        if (uri == null) {
            return null;
        }

        ResponseBody rb = doPUT(uri, headline);
        return process(ce, rb);
    }

    /**
     * Update the Annotation on the server by Putting the Body, and if needed, also the Headline. This leaves other aspects
     * of the Annotation unchanged, in particular the permissions.
     *
     * @param ce
     * @param a
     * @param h
     *
     * @return
     */

    private ResponseBody putBodyAndHeadline(CommentEnvelope ce, Annotation a, AnnPostParams h) {
        ResponseBody rb = putBody(ce, a, h);
        if (rb != null) {
            // Check if we need to update the headline too; if the change in the text is
            // restricted to the end, it may not be necessary.
            // Note that if the putBody() did a retry as a POST, the headline
            // would register as correctly updated by this point.
            String headline = a.getHeadline();
            Annotation replyA = rb.getAnnotation();

            if (!headline.equals(replyA.getHeadline())) {
                rb = putHeadline(ce, headline, h);
            }
        }

        return rb;
    }

    /**
     * POST a whole Annotation. This creates a new one at the server. After it has succeeded, update our knowledge of the
     * Annotation's URL and (if needed) the Transcription's URL (which is a DWAN "Target").
     *
     * @return the new Annotation as the server knows it now, wrapped up with some extra information which we ignore.
     */

    private ResponseBody postAnnotation(CommentEnvelope ce, Annotation a, AnnPostParams h) {
        JAXBElement<Annotation> ja = objectFactory.createAnnotation(a);

        // serviceURL + "api/annotations"
        ResponseBody rb = doPOST(apiAnnotationsServiceURL, ja);
        if (rb != null) {
            // Pick up the URI this annotation has been assigned.
            Annotation replyA = rb.getAnnotation();
            if (replyA != null) {
                if (DEBUG) {
                    System.out.printf("Annotation was assigned the URI %s\n", replyA.getHref());
                }
                // Next time this annotation is saved, it will contain the
                // server-assigned annotation URI and target URI.
                ce.setMessageURL(replyA.getHref());
                ce.setLastModifiedOnServer(replyA.getLastModified());

                // Pick up the target's URI, if it was new.
                // Remember it globally and in the comment.
                if (!h.targetIsKnown) {
                    String replyTarget = getFirstTarget(replyA);
                    if (replyTarget != null) {
                        if (DEBUG) {
                            System.out.printf("Transcription was assigned the URI %s\n", replyTarget);
                        }

                        urnToTargetURL.put(h.sourceURL, replyTarget);
                        ce.setAnnotationFileURL(replyTarget);
                    }
                }
            }
        }
        return rb;
    }

    /**
     * Copy one of our comments to the server.
     * <p>Tries to see if it is new or a modification (in which case its URL is
     * already known).
     *
     * @param ce the comment envelope
     *
     * @return the response body
     */
    public ResponseBody putCommentEnvelope(CommentEnvelope ce) {
        if (DEBUG) {
            System.out.println("putCommentEnvelope: " + ce.toString());
        }
        if (ce.isReadOnly()) {
            LOG.warning("Cannot putCommentEnvelope: it is READ ONLY");
            ce.setToBeSavedToServer(false);
            return null;
        }

        AnnPostParams h = new AnnPostParams();
        Annotation a = createAnnotation(ce, h);
        ResponseBody rb;

        // The URL of the comment (aka annotation) itself is kept as ColTime/@ColTimeMessageURL.
        // If this is a known comment, it would already look like a URL.
        if (h.annotationURLIsKnown) {
            //rb = putAnnotation(ce, a, h); // PUTs the whole object, including Permissions
            rb = putBodyAndHeadline(ce, a, h); // leaves things like Permissions untouched
        } else {
            // If this is a new comment, POST it (the whole thing)
            rb = postAnnotation(ce, a, h);
        }

        if (rb != null) {
            // This one has been saved now.
            ce.setToBeSavedToServer(false);

            checkActionList(ce, rb);
        }
        return rb;
    }

    // look at the actionList and do any CREATE_CACHED_REPRESENTATION(s).
    // Actually, the value of those is quite limited for our purposes, and a lot of work.
    // What would we actually use as a cached representation anyway?
    // And if it is there, it is useless if we can't show it to the user in some way.

    private void checkActionList(CommentEnvelope ce, ResponseBody rb) {
        if (ALWAYS_CREATE_CACHED_REPRESENTATION) { // for testing
            String object = ce.getAnnotationFileURL();
            createCachedRepresentation(ce, object);
        } else {
            ActionList actionList = rb.getActionList();
            if (actionList.getAction() != null) {
                for (Action action : actionList.getAction()) {
                    if (CREATE_CACHED_REPRESENTATION && action.getMessage().equals("CREATE_CACHED_REPRESENTATION")) {
                        String object = action.getObject();
                        createCachedRepresentation(ce, object);
                    }
                }
            }
        }
    }

    /**
     * Create a cached representation of the current Transcription, and register it for the given server object (given as a
     * URL).
     *
     * @param ce
     * @param object
     */
    private void createCachedRepresentation(CommentEnvelope ce, String object) {
        URI targetURI = resolveBaseURL.resolve(object + "/");
        String frag = ce.getFragment();
        if (DEBUG) {
            System.out.printf("Fragment string: %s\n", frag);
        }
        // Encode the fragment (which is likely to contain a '/') THREE times.
        // Twice because the web server always performs a URL decode, and we
        // want to have no bare / left over.
        // A third time, probably needed because Tomcat or some such driver
        // program performs an additional decode.
        String encFrag = CommentEnvelope.fragmentEncode(frag);
        if (!frag.equals(encFrag)) {
            // If a single encoding doesn't change it, two more won't do anything either.
            encFrag = CommentEnvelope.fragmentEncode(encFrag);
            encFrag = CommentEnvelope.fragmentEncode(encFrag);
        }
        URI cachedRepresentationURI = targetURI.resolve("fragment/" + encFrag + "/cached");
        if (DEBUG) {
            System.out.printf("URI: %s\n", cachedRepresentationURI.toASCIIString());
        }

        createCachedRepresentation(ce, cachedRepresentationURI);

    }

    /**
     * Create a cached representation of the current Transcription, and use the given URI to POST it.
     *
     * @param ce
     * @param cachedRepresentationURI
     */
    private void createCachedRepresentation(CommentEnvelope ce, URI cachedRepresentationURI) {

        CachedRepresentationInfo cri = new CachedRepresentationInfo();
        cri.setMimeType("application/xml");
        cri.setTool("ELAN");
        cri.setType("EAF");
        cri.setHref(ce.getAnnotationFileURL());
        cri.setId(lastPathPart(ce.getAnnotationFileURL()));    // the id part of the Target URL

        final JAXBElement<CachedRepresentationInfo> jcri = objectFactory.createCachedRepresentationInfo(cri);

        // create a multipart/mixed request with [0] = cri and [1] = the Transcription
        // then POST it

        // TODO: that is the unsaved file... put something else... like a screenshot.
        HttpEntity reqEntity = createMultipartEntity(jcri, new File(transcription.getPathName()));

        HttpPost req = new HttpPost(cachedRepresentationURI);
        System.out.println("POST: " + cachedRepresentationURI.toASCIIString());
        req.setEntity(reqEntity);
        doPOSTorPUTWithResult(req);
    }

    /**
     * Create a multipart entity.
     * <p>We do this "manually". The org.apache.http.entity.mime.MultipartEntityBuilder
     * always creates multipart/form-data, and we really want multipart/mixed.
     *
     * @param part1 a JAXBElement<?> which will be serialized
     * @param part2 a File to be added as the second part
     *
     * @return the entity containing both
     */
    private HttpEntity createMultipartEntity(final JAXBElement<?> part1, final File part2) {
        ContentProducer producer = outStream -> {
            byte[] crlf = CommentWebClient.CRLF.getBytes(ISO_8859_1);
            byte[] boundary = BOUNDARY_LINE.getBytes(ISO_8859_1);

            try {
                outStream.write(boundary);
                outStream.write("Content-Type: application/xml".getBytes(ISO_8859_1));
                outStream.write(crlf);
                outStream.write(crlf); // empty line, ending headers

                // Serialize the XML argument
                marshal(part1, outStream);
                if (DEBUG) {
                    marshal(part1, System.out);
                }
                outStream.write(crlf); // empty line, part of boundary
                outStream.write(boundary);
                outStream.write("Content-Type: application/octet-stream".getBytes(ISO_8859_1));
                outStream.write(crlf);
                outStream.write(crlf); // empty line, ending headers

                // copy the file
                try (FileInputStream in = new FileInputStream(part2)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        outStream.write(buffer, 0, len);
                    }
                }

                outStream.write(crlf); // empty line, part of boundary
                outStream.write(BOUNDARY_END.getBytes(ISO_8859_1)); // incl. 1 empty line
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        };

        AbstractHttpEntity reqEntity = new EntityTemplate(producer);
        reqEntity.setContentType("multipart/mixed; boundary=\"" + BOUNDARY + "\"");

        return reqEntity;
    }

    /**
     * Delete one of our comments from the server.
     *
     * @param ce the comment envelope
     */
    public void deleteCommentEnvelope(CommentEnvelope ce) {
        if (DEBUG) {
            System.out.println("CommentWebClient.deleteCommentEnvelope: " + ce.toString());
        }
        if (ce.isReadOnly()) {
            LOG.warning("Cannot deleteCommentEnvelope: it is READ ONLY");
            ce.setToBeSavedToServer(false);
            return;
        }
        URI uri = resolveEnvelopeURL(ce, "");

        if (uri != null) {
            doDELETE(uri);
        } else {
            LOG.warning("Can't DELETE (URL unknown/out of date) id " + ce.getMessageID());
        }
    }

    /**
     * A helper class to pass some shared variables through all functions that are associated with updating an Annotation to
     * the server. This saves us from making them class fields, which would be ugly.
     */
    private static class AnnPostParams {
        String sourceURL;
        String targetURL;

        boolean annotationURLIsKnown;
        boolean targetIsKnown;
    }

}
