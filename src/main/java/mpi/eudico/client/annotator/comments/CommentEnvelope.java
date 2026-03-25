package mpi.eudico.client.annotator.comments;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.util.TimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * A comment envelope or data carrier. Contains the properties of the comment itself as well as metadata and information
 * concerning the sender and recipient.
 */
public class CommentEnvelope implements Comparable<CommentEnvelope>,
                                        Cloneable,
                                        ClientLogger {

    private static final Date defaultDate = new Date();
    /*
     * The fields expressed in the external representation.
     */
    private String messageID = "";
    private String messageURL = "";    // the server's URL for this CommentEnvelope
    private String initials = "";
    private String sender = "";
    private String recipient = "";
    private Date creationDate = defaultDate;
    private Date modificationDate = defaultDate;
    private String category = "unknown";
    private String status = "unknown";
    private String annotationFileType = "EAF";
    private String annotationFileURL = "";    // the server's URL for this Transcription
    // <AnnotationFile> is not stored as a whole, but in parts.
    private String message = "";
    private String threadID = "";
    /*
     * The parts of <AnnotationFile>
     */
    private URI annotationURIBase = URI.create("urn:unknown");
    private long startTime = -1;
    private long endTime = -1;
    private String tierName = "";
    //    private String annotationID = "";

    // Some administrative fields that are not stored externally.
    // They are for managing synchronization with the outside world.
    private transient boolean toBeSavedToFile = false;
    private transient boolean toBeSavedToServer = false;
    private transient XMLGregorianCalendar lastModifiedOnServer;
    private transient boolean readOnly = false;

    @Override
    public CommentEnvelope clone() {
        try {
            return (CommentEnvelope) super.clone();
        } catch (CloneNotSupportedException e) {
            // won't happen! Object.clone() exists and we implement Cloneable.
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the message id.
     *
     * @return the ColTime/@ColTimeMessageID: The UUID which defines the uniqueness of this comment.
     */
    public String getMessageID() {
        return messageID;
    }

    /**
     * Sets the message id.
     *
     * @param messageID the ColTime/@ColTimeMessageID to set: The UUID which defines the uniqueness of this comment.
     */
    void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    /**
     * Automatically choose a new ColTime/@ColTimeMessageID.
     */
    public void setMessageID() {
        setMessageID(UUID.randomUUID().toString());
    }

    /**
     * Returns the message URL. This is going to be a relative URL which needs to be resolved relative to the web service
     * URL.
     *
     * @return the ColTime/@URL: the URL of the &lt;annotation&gt; on the DWAN server.
     */
    public String getMessageURL() {
        return messageURL;
    }

    /**
     * Sets the message URL.
     *
     * @param url the ColTime/@URL to set
     */
    public void setMessageURL(String url) {
        this.messageURL = url;
    }

    /**
     * Returns the initials of the sender.
     *
     * @return the ColTime/Metadata/Initials
     */
    public String getInitials() {
        return initials;
    }

    /**
     * Sets the initials of the sender.
     *
     * @param initials the ColTime/Metadata/Initials to set
     */
    public void setInitials(String initials) {
        this.initials = initials;
    }

    /**
     * Returns the sender of the message.
     *
     * @return the ColTime/Metadata/Sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * Sets the sender of the message.
     *
     * @param sender the ColTime/Metadata/Sender to set
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Returns the recipient of the message.
     *
     * @return the ColTime/Metadata/Recipient
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Sets the recipient of the message.
     *
     * @param recipient the ColTime/Metadata/Recipient to set
     */
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /**
     * Adds more recipients of the messages.
     *
     * @param recipient the ColTime/Metadata/Recipient to set
     */
    public void addRecipient(String recipient) {
        if (this.recipient == null || this.recipient.isEmpty()) {
            this.recipient = recipient;
        } else {
            this.recipient += "," + recipient;
        }
    }

    /**
     * Returns the array of recipients.
     *
     * @return array of recipients
     */
    public String[] getRecipients() {
        return this.recipient.split("\\s*,\\s*");
    }

    /**
     * Returns the creation {@code Date} object.
     *
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the creation date as a formatted string.
     *
     * @return the ColTime/Metadata/CreationDate in ISO 8601 format.
     */
    public String getCreationDateString() {
        return getDateFormat().format(creationDate);
    }

    private DateFormat getDateFormat() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat;
    }

    /**
     * Set ColTime/Metadata/CreationDate to now.
     */
    public void setCreationDate() {
        this.creationDate = nowRoundedToMilliSeconds();
    }

    /**
     * Sets the creation date to the specified {@code Date}. s
     *
     * @param creationDate the ColTime/Metadata/CreationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Sets the creation date to the date specified by a string.
     *
     * @param creationDate the ColTime/Metadata/CreationDate to set, in ISO 8601 format. (possibly we want to restrict
     *     this to the exact same format we output).
     */
    public void setCreationDate(String creationDate) {
        Calendar cal = javax.xml.bind.DatatypeConverter.parseDateTime(creationDate);
        this.setCreationDate(cal.getTime());
    }

    /**
     * Returns the modification {@code Date} object.
     *
     * @return the ColTime/Metadata/ModificationDate
     */
    public Date getModificationDate() {
        return modificationDate;
    }

    /**
     * Returns the modification date as a string.
     *
     * @return the ColTime/Metadata/ModificationDate in ISO 8601 format.
     */
    public String getModificationDateString() {
        return getDateFormat().format(modificationDate);
    }

    /**
     * Sets the modification date to the specified {@code Date}.
     *
     * @param modificationDate the ColTime/Metadata/ModificationDate to set
     */
    private void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    /**
     * Sets the modification date to the date in the string.
     *
     * @param modificationDate the creationDate to set, in ISO 8601 format. (possibly we want to restrict this to the
     *     exact same format we output).
     */
    public void setModificationDate(String modificationDate) {
        Calendar cal = javax.xml.bind.DatatypeConverter.parseDateTime(modificationDate);
        this.setModificationDate(cal.getTime());
    }

    /**
     * Sets the ColTime/Metadata/ModificationDate date (and time) to now. Also sets the flags that the comment needs to be
     * saved.
     */
    public void setModificationDate() {
        setModificationDate(nowRoundedToMilliSeconds());
        setToBeSaved(true);
    }

    /**
     * Returns the annotation file type.
     *
     * @return the ColTime/AnnotationFile/@type
     */
    public String getAnnotationFileType() {
        return annotationFileType;
    }

    /**
     * Sets the annotation file type.
     *
     * @param annotationFileType the ColTime/AnnotationFile/@type to set: typically EAF.
     */
    public void setAnnotationFileType(String annotationFileType) {
        this.annotationFileType = annotationFileType;
    }

    /**
     * Returns the annotation file URL.
     *
     * @return the ColTime/AnnotationFile/@URL: the DWAN target URL for the EAF file.
     */
    public String getAnnotationFileURL() {
        return annotationFileURL;
    }

    /**
     * Sets the annotation file URL.
     *
     * @param annotationFileURL the ColTime/AnnotationFile/@URL to set (the DWAN target URL for the EAF file).
     */
    public void setAnnotationFileURL(String annotationFileURL) {
        this.annotationFileURL = annotationFileURL;
    }

    /**
     * Returns the annotation file URL as a string. It is stored in parts for easy processing, so compose them to a complete
     * URI including fragment identifier for the appropriate time (and/or tier).<br> Example:
     * "urn:nl-mpi-tools-elan-eaf:59d08e6a-5cd9-4aed-8aa4-7074c270e635#t=0.960/1.960".
     *
     * @return the ColTime/AnnotationFile element
     */
    public String getAnnotationFile() {
        URI uri = getTimeBasedURI(getAnnotationURIBase(), getStartTime(), getEndTime());
        String fragment = uri.getFragment();
        if (tierName != null && !tierName.isEmpty()) {
            fragment = fragment + ";tier=" + tierName;
        }
        //        if (annotationID != null && !annotationID.isEmpty()) {
        //            fragment = fragment + ";anno=" + annotationID;
        //        }
        try {
            return new URI(uri.getScheme(), uri.getSchemeSpecificPart(), fragment).toString();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "URI syntax problem.", e);
        }
        return "";
    }

    /**
     * Sets the annotation file string.
     *
     * @param annotationFile the ColTime/AnnotationFile to set
     */
    public void setAnnotationFile(String annotationFile) {
        try {
            URI uri = new URI(annotationFile);
            deriveFieldsFromAnnotationFile(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the annotation file URI.
     *
     * @param annotationFile the ColTime/AnnotationFile to set
     */
    public void setAnnotationFile(URI annotationFile) {
        deriveFieldsFromAnnotationFile(annotationFile);
    }

    /**
     * Derive the derived fields from the explicit ones by interpreting the fragment part of the URL.
     *
     * @param uri the file URI
     */
    private void deriveFieldsFromAnnotationFile(URI uri) {
        // Look for # t=<seconds>/<seconds>
        // or       # t=<seconds>
        try {
            setAnnotationURIBase(new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "URISyntaxException", e);
        }
        setTierName("");
        //        setAnnotationID("");

        String fragment = uri.getFragment();
        if (fragment != null) {
            String[] parts = fragment.split(";");

            for (String p : parts) {
                if (p.startsWith("t=")) {
                    p = p.substring(2);
                    String[] times = p.split("/");

                    long start = 0;
                    long end = 0;

                    if (times.length > 0) {
                        start = TimeFormatter.toMilliSeconds(times[0]);
                        end = start;
                    }
                    if (times.length > 1) {
                        end = TimeFormatter.toMilliSeconds(times[1]);
                    }

                    setStartEndTime(start, end);
                } else if (p.startsWith("tier=")) {
                    p = p.substring(5);
                    setTierName(p);
                }
                //                else if (p.startsWith("anno=")) {
                //                    p = p.substring(5);
                //                    setAnnotationID(p);
                //                }
            }
        }
    }

    /**
     * Returns the message.
     *
     * @return the ColTime/Message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param message the ColTime/Message to set as part of the ColTime/AnnotationFile/text()
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the basic URI for the comment, excluding the fragment. We generally don't want external databases to know
     * precisely what time our different comments refer to; we just want them to group all comments on the same transcription
     * together.
     *
     * @return the basic URI
     */
    public URI getAnnotationURIBase() {
        return annotationURIBase;
    }

    /**
     * Sets the annotation base URI.
     *
     * @param s the uri
     */
    public void setAnnotationURIBase(URI s) {
        annotationURIBase = s;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Gets the end time.
     *
     * @return the end time
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set as part of the ColTime/AnnotationFile/text()
     */
    //    public void setEndTime(long endTime) {
    //        this.endTime = endTime;
    //    }

    /**
     * Sets both the start and end time.
     *
     * @param s the start Time to set as part of the ColTime/AnnotationFile/text()
     * @param e the end Time to set as part of the ColTime/AnnotationFile/text()
     */
    public void setStartEndTime(long s, long e) {
        startTime = s;
        endTime = e;
    }

    /**
     * Returns the tier name.
     *
     * @return the tier name as part of the ColTime/AnnotationFile/text()
     */
    public String getTierName() {
        return tierName;
    }

    /**
     * Sets the tier name.
     *
     * @param s the name
     */
    public void setTierName(String s) {
        tierName = s;
    }

    //    public String getAnnotationID() {
    //        return annotationID;
    //    }
    //
    //    public void setAnnotationID(String s) {
    //        annotationID = s;
    //    }

    /**
     * Returns the category property.
     *
     * @return the ColTime/Metadata/Category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category.
     *
     * @param category the ColTime/Metadata/Category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the status.
     *
     * @return the ColTime/Metadata/Status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status string.
     *
     * @param status the ColTime/Metadata/Status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns whether the comment should be saved to file.
     *
     * @return whether the comment must be saved to the default file.
     */
    public boolean getToBeSavedToFile() {
        return toBeSavedToFile;
    }

    /**
     * Returns whether the comment should be saved to the server.
     *
     * @return whether the comment must be saved to the server.
     */
    public boolean getToBeSavedToServer() {
        return toBeSavedToServer;
    }

    /**
     * Set whether the comment must be saved to the default file.
     *
     * @param flag the new value for the save to default file flag
     */
    public void setToBeSavedToFile(boolean flag) {
        toBeSavedToFile = flag;
    }

    /**
     * Set whether the comment must be saved to the server.
     *
     * @param flag whether the comment must be saved to the server
     */
    public void setToBeSavedToServer(boolean flag) {
        toBeSavedToServer = flag;
    }

    /**
     * Sets both ToBeSaved flags at once.
     *
     * @param flag the new value for both the save to file and the save the server flags
     */
    public void setToBeSaved(boolean flag) {
        setToBeSavedToFile(flag);
        setToBeSavedToServer(flag);
    }

    private Date nowRoundedToMilliSeconds() {
        return new Date();
    }

    /**
     * Sets the last modified time
     *
     * @param xmlGregorianCalendar the xmlGregorianCalendar
     */
    public void setLastModifiedOnServer(XMLGregorianCalendar xmlGregorianCalendar) {
        lastModifiedOnServer = xmlGregorianCalendar;
    }

    /**
     * Get the Date when the server thinks the annotation was last modified. If we don't know, returns null.
     *
     * @return the last modified date or {@code null}
     */
    public XMLGregorianCalendar getLastModifiedOnServer() {
        return lastModifiedOnServer;
    }

    /**
     * Gets the thread id
     *
     * @return the thread id
     */
    public String getThreadID() {
        return threadID;
    }

    /**
     * Sets the thread id
     *
     * @param id the thread id
     */
    public void setThreadID(String id) {
        threadID = id;
    }

    /**
     * On the server, comments may be readonly because of the stated access permissions. Generally they are set to r/w for
     * the owner and r/o for the world. Merging of external changes uses r/o status to accept changes to comments that the
     * user has no permissions for anyway.
     *
     * <p>However, in locally stored files there is no such concept of permissions.
     * So here is an idea: let the {@code <Sender>} field determine the owner of the comments. If the current user's settings
     * for Sender match that of the comment, it is writable. Otherwise it is read-only.
     *
     * @return the readOnly flag
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets the read-only flag.
     *
     * @param readOnly the readOnly to set
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Copy the fields that are not looked at in valueEquals(), because they may get modified on the server without being a
     * real change.
     *
     * <p>They change for instance if the server's base URL changes.
     * Or if you delete, then undelete, a comment, its MessageURL changes.
     *
     * <p>If we do allow a Target for each CommentEnvelope, then delete-undelete
     * will also change the Target ID / AnnotationFileURL.
     *
     * @param other the envelope to copy from
     */
    public void setServerModifiableFields(CommentEnvelope other) {
        this.setMessageURL(other.getMessageURL());
        this.setAnnotationFileURL(other.getAnnotationFileURL());
        this.setLastModifiedOnServer(other.getLastModifiedOnServer());
        this.setReadOnly(other.isReadOnly());
    }

    /**
     * Copy the field(s) that are not looked at in interestingValueEquals(), because they may get modified on the server
     * without being a real change.
     *
     * <p>The modification date falls in this category: a user may change a field
     * and then change it back, but the modification date is still different.
     *
     * @param other the envelope to copy from
     */
    public void setUninterestingFields(CommentEnvelope other) {
        this.setModificationDate(other.getModificationDate());
    }

    /**
     * Convert a comment into an element of a DOM tree.
     *
     * @param nameSpaceURI the namespace URI
     * @param doc the document that creates elements and nodes
     *
     * @return the created ColTime Element
     */
    public Element getElement(String nameSpaceURI, Document doc) {
        Element coltime = doc.createElementNS(nameSpaceURI, "ColTime");
        coltime.setAttribute("ColTimeMessageID", this.messageID);
        coltime.setAttribute("URL", this.messageURL);

        Element initials = doc.createElementNS(nameSpaceURI, "Initials");
        initials.appendChild(doc.createTextNode(this.initials));

        Element threadid = doc.createElementNS(nameSpaceURI, "ThreadID");
        threadid.appendChild(doc.createTextNode(this.threadID));

        Element sender = doc.createElementNS(nameSpaceURI, "Sender");
        sender.appendChild(doc.createTextNode(this.sender));

        Element creationdate = doc.createElementNS(nameSpaceURI, "CreationDate");
        creationdate.appendChild(doc.createTextNode(this.getCreationDateString()));

        Element modificationdate = doc.createElementNS(nameSpaceURI, "ModificationDate");
        modificationdate.appendChild(doc.createTextNode(this.getModificationDateString()));

        Element category = doc.createElementNS(nameSpaceURI, "Category");
        category.appendChild(doc.createTextNode(this.category));

        Element status = doc.createElementNS(nameSpaceURI, "Status");
        status.appendChild(doc.createTextNode(this.status));

        Element metadata = doc.createElementNS(nameSpaceURI, "Metadata");
        metadata.appendChild(initials);
        metadata.appendChild(threadid);
        metadata.appendChild(sender);
        {
            String[] recipients = getRecipients();
            if (recipients != null) {
                for (String r : recipients) {
                    Element recipient = doc.createElementNS(nameSpaceURI, "Recipient");
                    recipient.appendChild(doc.createTextNode(r));
                    metadata.appendChild(recipient);
                }
            }
        }
        metadata.appendChild(creationdate);
        metadata.appendChild(modificationdate);
        metadata.appendChild(category);
        metadata.appendChild(status);

        Element annotationfile = doc.createElementNS(nameSpaceURI, "AnnotationFile");
        annotationfile.setAttribute("URL", this.annotationFileURL);
        annotationfile.setAttribute("type", this.annotationFileType);
        annotationfile.appendChild(doc.createTextNode(getAnnotationFile()));

        Element message = doc.createElementNS(nameSpaceURI, "Message");
        // Just to be safe, even though current serializers don't seem to add line breaks:
        // message.setAttribute("xml:space", "preserve");
        // However that means that that attribute needs to be allowed in the schema,
        // because it is actually put in the serialized output.
        message.appendChild(doc.createTextNode(this.message));

        coltime.appendChild(metadata);
        coltime.appendChild(annotationfile);
        coltime.appendChild(message);

        return coltime;
    }

    /**
     * Default constructor. Initializes a comment to safe values. Actually, most of them have been given in their
     * declaration.
     */
    public CommentEnvelope() {
    }

    /**
     * Construct a CommentEnvelope based on a DOM tree. There must be a better way... Fortunately it is only 2 levels deep.
     *
     * @param e the root element
     */
    public CommentEnvelope(Element e) {
        this();

        this.messageID = e.getAttribute("ColTimeMessageID");
        this.messageURL = e.getAttribute("URL");

        NodeList l1 = e.getChildNodes();
        for (int i1 = 0; i1 < l1.getLength(); i1++) {
            Node n1 = l1.item(i1);
            if (n1 instanceof Element e1) {
                String name = e1.getLocalName();
                if ("Metadata".equals(name)) {
                    NodeList l2 = e1.getChildNodes();
                    for (int i2 = 0; i2 < l2.getLength(); i2++) {
                        Node n2 = l2.item(i2);
                        if (n2 instanceof Element e2) {
                            String name2 = e2.getLocalName();

                            if ("Initials".equals(name2)) {
                                this.initials = e2.getTextContent();
                            } else if ("ThreadID".equals(name2)) {
                                this.threadID = e2.getTextContent();
                            } else if ("Sender".equals(name2)) {
                                this.sender = e2.getTextContent();
                            } else if ("Recipient".equals(name2)) {
                                this.addRecipient(e2.getTextContent());
                            } else if ("CreationDate".equals(name2)) {
                                this.setCreationDate(e2.getTextContent());
                            } else if ("ModificationDate".equals(name2)) {
                                this.setModificationDate(e2.getTextContent());
                            } else if ("Category".equals(name2)) {
                                this.setCategory(e2.getTextContent());
                            } else if ("Status".equals(name2)) {
                                this.setStatus(e2.getTextContent());
                            }
                        }
                    }
                } else if ("AnnotationFile".equals(name)) {
                    this.setAnnotationFile(e1.getTextContent());
                    this.annotationFileURL = e1.getAttribute("URL");
                    //                    if (this.annotationFileURL == null) {
                    //                        // accept old attribute name
                    //                        this.annotationFileURL = e1.getAttribute("ColTimeID");
                    //                    }
                    this.annotationFileType = e1.getAttribute("type");
                } else if ("Message".equals(name)) {
                    this.message = e1.getTextContent();
                }
            }
        }
    }

    /**
     * Compare two comments based on their start and end times. First compare by the start time. If it is equal, the end time
     * decides. No other fields are considered.
     */
    @Override
    public int compareTo(CommentEnvelope other) {
        int result = Long.compare(this.startTime, other.startTime);
        if (result == 0) {
            result = Long.compare(this.endTime, other.endTime);
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommentEnvelope that = (CommentEnvelope) o;
        return getStartTime() == that.getStartTime()
               && getEndTime() == that.getEndTime()
               && getToBeSavedToFile() == that.getToBeSavedToFile()
               && getToBeSavedToServer() == that.getToBeSavedToServer()
               && isReadOnly() == that.isReadOnly()
               && Objects.equals(getMessageID(), that.getMessageID())
               && Objects.equals(getMessageURL(), that.getMessageURL())
               && Objects.equals(getInitials(), that.getInitials())
               && Objects.equals(getSender(), that.getSender())
               && Objects.equals(getRecipient(), that.getRecipient())
               && Objects.equals(getCreationDate(), that.getCreationDate())
               && Objects.equals(getModificationDate(), that.getModificationDate())
               && Objects.equals(getCategory(), that.getCategory())
               && Objects.equals(getStatus(), that.getStatus())
               && Objects.equals(getAnnotationFileType(), that.getAnnotationFileType())
               && Objects.equals(getAnnotationFileURL(), that.getAnnotationFileURL())
               && Objects.equals(getMessage(), that.getMessage())
               && Objects.equals(getThreadID(), that.getThreadID())
               && Objects.equals(getAnnotationURIBase(), that.getAnnotationURIBase())
               && Objects.equals(getTierName(), that.getTierName())
               && Objects.equals(getLastModifiedOnServer(), that.getLastModifiedOnServer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMessageID(),
                            getMessageURL(),
                            getInitials(),
                            getSender(),
                            getRecipient(),
                            getCreationDate(),
                            getModificationDate(),
                            getCategory(),
                            getStatus(),
                            getAnnotationFileType(),
                            getAnnotationFileURL(),
                            getMessage(),
                            getThreadID(),
                            getAnnotationURIBase(),
                            getStartTime(),
                            getEndTime(),
                            getTierName(),
                            getToBeSavedToFile(),
                            getToBeSavedToServer(),
                            getLastModifiedOnServer(),
                            isReadOnly());
    }

    /**
     * Compare the modification dates.
     *
     * @param other the date to compare with
     *
     * @return {@code true} if the modification date of this object is after that of the other
     */
    public boolean isNewerThan(Date other) {
        return this.getModificationDate().after(other);
    }

    /**
     * Compare the modification dates
     *
     * @param other the envelope to compare with
     *
     * @return {@code true} if the modification date of this object is after that of the other
     */
    public boolean isNewerThan(CommentEnvelope other) {
        return this.getModificationDate().after(other.getModificationDate());
    }

    /**
     * Straightforward element-wise equals method. Do not call it "equals" so we don't need to override hashCode(), and
     * collections work a bit more sensibly.
     *
     * <p>Don't look at messageURL and annotationFileURL: they are assigned by
     * the server, and the first time round they may change before they settle down. They may also change if the server's
     * location URL changes. Likewise for lastModifiedOnServer.
     *
     * <p>Modification time is not considered interesting either.
     *
     * @param other compare with this CommentEnvelope
     *
     * @return {@code true} if most of the fields are equal, {@code false} otherwise
     */
    public boolean interestingValueEquals(CommentEnvelope other) {
        if (this == other) {
            return true;
        }

        return this.messageID.equals(other.messageID)
               && this.initials.equals(other.initials)
               && this.threadID.equals(other.threadID)
               && this.sender.equals(other.sender)
               && this.recipient.equals(other.recipient)
               && this.creationDate.equals(other.creationDate)
               && this.annotationFileType.equals(other.annotationFileType)
               && this.category.equals(other.category)
               && this.status.equals(other.status)
               && this.message.equals(other.message)
               &&
            /*
             * <AnnotationFile> is not stored as a whole, but in parts.
             * The parts of <AnnotationFile>
             */
               this.annotationURIBase.equals(other.annotationURIBase)
               && this.startTime == other.startTime
               && this.endTime == other.endTime
               && this.tierName.equals(other.tierName);
               // && this.annotationID.equals(other.annotationID);
    }

    /**
     * Straightforward element-wise equals method. Do not call it "equals" so we don't need to override hashCode(), and
     * collections work a bit more sensibly.
     *
     * <p>Don't look at messageURL and annotationFileURL: they are assigned by
     * the server, and the first time round they may change before they settle down. They may also change if the server's
     * location URL changes. Likewise for lastModifiedOnServer.
     *
     * @param other compare with this CommentEnvelope
     *
     * @return {@code true} if the interesting fields and the modification date are equal
     */
    public boolean valueEquals(CommentEnvelope other) {
        return interestingValueEquals(other) && this.modificationDate.equals(other.modificationDate);
    }

    @Override
    public String toString() {
        String sb = "[CommentEnvelope: "
                    + " messageID="
                    + messageID
                    + ",startTime="
                    + startTime
                    + ",endTime="
                    + endTime
                    + ",message="
                    + message
                    + ",modificationDate="
                    + modificationDate.toString()
                    + "]";

        return sb;
    }

    /**
     * Encode a fragment string. Nearly the same as URLEncoder.encode() but that uses the irregular + instead of the regular
     * %20.
     *
     * @param s the string to encode
     *
     * @return the encoded string
     *
     * @see URLEncoder#encode(String, String)
     */
    public static String fragmentEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * Gets the fragmented string
     *
     * @return the fragment string
     */
    public String getFragment() {
        StringBuilder sb = new StringBuilder();

        if (startTime >= 0) {
            sb.append("t=");
            sb.append(TimeFormatter.toSSMSString(startTime));

            if (endTime >= 0 && endTime != startTime) {
                sb.append("/");
                sb.append(TimeFormatter.toSSMSString(endTime));
            }
        }

        if (tierName != null && !tierName.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(";");
            }
            sb.append("tier=");
            sb.append(fragmentEncode(tierName));
        }

        //        if (annotationID != null & !annotationID.isEmpty()) {
        //            if (sb.length() > 0) {
        //                sb.append(";");
        //            }
        //            sb.append("anno=");
        //            sb.append(fragmentEncode(annotationID));
        //        }

        return sb.toString();
    }

    /**
     * URI generation support: generate a time-URI for use with the comment server.
     *
     * <p>Form of the URI:
     * {@code <transcription-uri>#t=<secs> <transcription-uri>#t=<secs>/<secs> }
     *
     * @param urn the base uri
     * @param moment the time value for the fragment
     *
     * @return the time-URI
     */
    public static URI getTimeBasedURI(URI urn, long moment) {
        try {
            String fragment = "t=" + TimeFormatter.toSSMSString(moment);
            return new URI(urn.getScheme(), urn.getSchemeSpecificPart(), fragment);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * URI generation support: generate a time-URI for use with the comment server.
     *
     * @param urn the base uri
     * @param from the start time value for the fragment
     * @param to the end time value for the fragment
     *
     * @return the time-URI
     */
    public static URI getTimeBasedURI(URI urn, long from, long to) {

        if (to == from) {
            return getTimeBasedURI(urn, from);
        } else {
            String fragment = "t=" + TimeFormatter.toSSMSString(from) + '/' + TimeFormatter.toSSMSString(to);
            try {
                return new URI(urn.getScheme(), urn.getSchemeSpecificPart(), fragment);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * URI generation support: generate a time-URI for use with the comment server.
     *
     * @param urn the base uri
     * @param tier the tier
     * @param moment the time value for the fragment
     *
     * @return time-URI
     */
    public static URI getTierTimeBasedURI(URI urn, String tier, long moment) {
        String fragment = "t=" + TimeFormatter.toSSMSString(moment) + ";tier=" + fragmentEncode(tier);

        try {
            return new URI(urn.getScheme(), urn.getSchemeSpecificPart(), fragment);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * URI generation support: generate a time-URI for use with the comment server.
     *
     * @param urn the base uri
     * @param tier the tier
     * @param from the start time value for the fragment
     * @param to the end time value for the fragment
     *
     * @return the time-URI
     */
    public static URI getTierTimeBasedURI(URI urn, String tier, long from, long to) {
        String fragment =
            "t=" + TimeFormatter.toSSMSString(from) + '/' + TimeFormatter.toSSMSString(to) + ";tier=" + fragmentEncode(tier);
        try {
            return new URI(urn.getScheme(), urn.getSchemeSpecificPart(), fragment);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    //    public static URI getAnnotationBasedURI(URI urn, String annotation) {
    //        String fragment = "anno=" + annotation;
    //        try {
    //            return new URI(urn.getScheme(), urn.getSchemeSpecificPart(), fragment);
    //        } catch (URISyntaxException e) {
    //            e.printStackTrace();
    //        }
    //        return null;
    //    }
}
