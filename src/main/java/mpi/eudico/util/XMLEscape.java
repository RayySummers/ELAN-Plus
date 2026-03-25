package mpi.eudico.util;
/**
 * Class that performs basic xml escaping for "hand made" xml.
 * Temporary facility, probably.
 * 
 * @author Han Sloetjes
 */
public class XMLEscape {
	private static final String LESS = "&lt;";
	private static final String GREAT = "&gt;";
	private static final String AMP = "&amp;";
	private static final String QUOT = "&quot;";
	private static final String APOS = "&apos;";
    
	private static final String LESS_NUM = "&#60;";
	private static final String GREAT_NUM = "&#62;";
	private static final String AMP_NUM = "&#38;";
	private static final String QUOT_NUM = "&#34;";
	private static final String APOS_NUM = "&#39;";
    
    private static final char CH_LESS = '<';
    private static final char CH_GREAT = '>';
    private static final char CH_AMP = '&';
    private static final char CH_QUOT = '\"';
    private static final char CH_APOS = '\'';
    private static final char CH_SPACE = '\u0020';
    
    /**
     * Creates a new XMLEscape instance.
     */
	public XMLEscape() {
		super();
	}

    /**
     * Replaces reserved characters (e.g. "&gt;" and "&lt;" etc.) in the input 
     * string by standard xml entity strings (e.g. {@code "&gt;"} or {@code "&lt;"}).
     * 
     * @param input the input text
     * @return the escaped string
     */
    public String escape(String input) {
    	if (input == null) {
    		return null;
    	}
    	
    	StringBuilder builder = new StringBuilder();
    	char c;
    	
    	for (int i = 0; i < input.length(); i++) {
    		c = input.charAt(i);
    		if (c < CH_SPACE) {
    			continue;
    		}
    		switch (c) {
    		case CH_LESS:
    			builder.append(LESS);
    			break;
    		case CH_GREAT:
    			builder.append(GREAT);
    			break;
    		case CH_AMP:
    			builder.append(AMP);
    			break;
    		case CH_APOS:
    			builder.append(APOS);
    			break;
    		case CH_QUOT:
    			builder.append(QUOT);
    			break;
    			default:
    				builder.append(c);
    		}
    	}
    	
    	if (builder.length() != input.length()) {
    		return builder.toString();
    	} else {
    		return input;
    	}
    } 

    /**
     * Replaces reserved characters (e.g. "&gt;" and "&lt;" etc.) in the input string by decimal representation of 
     * the character code points (e.g. {@code "&gt;"} or {@code "&lt;"}).
     * 
     * @param input the input text
     * @return the escaped string
     */
    public String escapeNumeric(String input) {
    	if (input == null) {
    		return null;
    	}
    	
    	StringBuilder builder = new StringBuilder();
    	char c;
    	
    	for (int i = 0; i < input.length(); i++) {
    		c = input.charAt(i);
    		if (c < CH_SPACE) {
    			continue;
    		}
    		switch (c) {
    		case CH_LESS:
    			builder.append(LESS_NUM);
    			break;
    		case CH_GREAT:
    			builder.append(GREAT_NUM);
    			break;
    		case CH_AMP:
    			builder.append(AMP_NUM);
    			break;
    		case CH_APOS:
    			builder.append(APOS_NUM);
    			break;
    		case CH_QUOT:
    			builder.append(QUOT_NUM);
    			break;
    			default:
    				builder.append(c);
    		}
    	}
    	
    	if (builder.length() != input.length()) {
    		return builder.toString();
    	} else {
    		return input;
    	}
    } 
}
