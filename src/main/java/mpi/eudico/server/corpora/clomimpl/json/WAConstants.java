package mpi.eudico.server.corpora.clomimpl.json;

/**
 * Defines constants for parsing and creating WebAnnotation JSON files.
 */
public class WAConstants {
	/** WA context URL */
	public static final String WA_CONTEXT        = "http://www.w3.org/ns/anno.jsonld";
	/** LDP context URL */
	public static final String LDP_CONTEXT       = "http://www.w3.org/ns/ldp.jsonld";
	/** media fragment specification URL */
	public static final String MEDIA_SELECTOR    = "http://www.w3.org/TR/media-frags/";
	/** plain text mime type */
	public static final String TEXT_FORMAT       = "text/plain";
	/** html mime type */
	public static final String HTML_FORMAT       = "text/html";
	/** {@code @context}  key */
	public static final String CONTEXT           = "@context";
	/** id key */
	public static final String ID                = "id";
	/** type key */
	public static final String TYPE              = "type";
	/** label key */
	public static final String LABEL             = "label";
	/** creator key */
	public static final String CREATOR           = "creator";
	/** nickname key */
	public static final String NICKNAME          = "nickname";// ?? no place in ELAN data model
	/** sha1 version of agent email key */
	public static final String EMAIL_SHA         = "email_sha1";// ?? no place
	/** total key */
	public static final String TOTAL             = "total";
	/** first key */
	public static final String FIRST             = "first";
	/** last key */
	public static final String LAST              = "last";
	/** start index key */
	public static final String START_INDEX       = "startIndex";
	/** items key */
	public static final String ITEMS             = "items";
	/** created key */
	public static final String CREATED           = "created"; // date
	/** generated key */
	public static final String GENERATED         = "generated"; // date
	/** motivation key */
	public static final String MOTIVATION        = "motivation";
	/** rights key */
	public static final String RIGHTS            = "rights";
	/** generator key */
	public static final String GENERATOR         = "generator";
	/** name key */
	public static final String NAME              = "name";
	/** homepage key */
	public static final String HOMEPAGE          = "homepage";
	/** body key */
	public static final String BODY              = "body";
	/** value key */
	public static final String VALUE             = "value";
	/** language key */
	public static final String LANGUAGE          = "language";
	/** purpose key */
	public static final String PURPOSE           = "purpose";
	/** format key */
	public static final String FORMAT            = "format";
	/** target key */
	public static final String TARGET            = "target";
	/** selector key */
	public static final String SELECTOR          = "selector";
	/** conforms to key */
	public static final String CONFORMS_TO       = "conformsTo";
	/** refined by key */
	public static final String REFINED_BY        = "refinedBy";
	/** foreground color key */
	public static final String FOREGROUND_COLOR  = "foregroundColor";
	/** background color key */
	public static final String BACKGROUND_COLOR  = "backgroundColor";
	/** key key */
	public static final String KEY               = "key";
	/** transcribing key */
	public static final String TRANSCRIBING		 = "transcribing";
	/** basic container key */
	public static final String BASIC_CONTAINER	 = "BasicContainer";
	
	// standard values
	/** Annotation type value */
	public static final String ANNOTATION        = "Annotation";
	/** Annotation Collection type value */
	public static final String ANN_COLLECTION    = "AnnotationCollection";
	/** Annotation Page type value */
	public static final String ANN_PAGE          = "AnnotationPage";
	/** Textual Body type value */
	public static final String TEXTUAL_BODY      = "TextualBody";
	/** Fragment Selector type value */
	public static final String FRAG_SELECTOR     = "FragmentSelector";
	/** partOf key */
	public static final String PART_OF           = "partOf";
	/** next key */
	public static final String NEXT              = "next";
	/** body value key */
	public static final String BODY_VALUE        = "bodyValue";
	/** Choice value */
	public static final String CHOICE            = "Choice";
	/** source key */
	public static final String SOURCE            = "source";
	/** Container type value */
	public static final String CONTAINER         = "Container";
	/** contains key */
	public static final String CONTAINS          = "contains";
	/** Audio type value */
	public static final String AUDIO             = "Audio";
	/** Video type value */
	public static final String VIDEO             = "Video";
	//public static final String SOUND             = "Sound";
	
	// some alternative values
	/** {@code @id}  key */
	public static final String AT_ID             = "@id";
	/** {@code @type}  key */
	public static final String AT_TYPE           = "@type";
	/** Text type value */
	public static final String TEXT              = "Text";
	/** Unnamed value */
	public static final String UNNAMED           = "Unnamed";
	
	/**
	 * Private constructor.
	 */
	private WAConstants() {
	}

}
