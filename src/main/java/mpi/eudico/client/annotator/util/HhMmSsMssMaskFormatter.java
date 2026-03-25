package mpi.eudico.client.annotator.util;

import javax.swing.text.MaskFormatter;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * An extension of the MaskFormatter which prevents the first digit of
 * the minutes number and of the seconds number to be {@code > 5}.
 * This is only applied when committing the entered time value; implementation
 * of correction at input time would require a rewrite of this formatter 
 * (or another one) because most fields and methods are (package) private.
 */
@SuppressWarnings("serial")
public class HhMmSsMssMaskFormatter extends MaskFormatter {
	private DecimalFormat twoDigits = new DecimalFormat("00");
	
	/**
	 * Creates a new formatter instance.
	 */
	public HhMmSsMssMaskFormatter() {
		super();
		try {
			setMask("##:##:##.###");
			setPlaceholderCharacter('0');
		} catch (ParseException pe) {
			// should never happen with this fixed mask
		}
	}
	
	/**
	 * Returns whether a {@code char} is a valid first digit of the minutes or 
	 * seconds part of a time string.
	 * 
	 * @param ch the char to test
	 * @return true if the char is a digit between 0 and 5, inclusive,
	 * false otherwise
	 */
	private boolean isValidMSChar(char ch) {
		return (ch == '0' || ch == '1' || 
				ch == '2' || ch == '3' || 
				ch == '4' || ch == '5');
	}
	
	/**
	 * Tests the first digit of the minutes and seconds positions in the
	 * time string, after first calling the test of the super class.
	 * In case of values {@code > 5 (>59)} the seconds, minutes and/or hour
	 * fields are recalculated.
	 * 
	 * @param value the String to test
	 * @return the updated input string in case of seconds or minutes values
	 * {@code > 59}, otherwise the input string is returned
	 * @throws ParseException if the specified digits are invalid 
	 */
	@Override
	public Object stringToValue(String value) throws ParseException {
		Object temp = super.stringToValue(value);
		if (temp instanceof String) {
			String s = (String) temp;
			// the super's check already verified the length of the string
			if (!isValidMSChar(s.charAt(6))) {
				try {
					int isec = Integer.parseInt(s.substring(6, 7));
					int m = Integer.parseInt(s.substring(3, 5));
					isec -= 6;// decrease with 60 seconds
					m++;// increase one minute
					s = s.substring(0, 3) + twoDigits.format(m) + s.substring(5, 6) + 
							String.valueOf(isec) + s.substring(7);
				} catch (NumberFormatException nfe) {
					throw new ParseException("Invalid seconds value", 6);
				}
				// throw new ParseException("Invalid seconds value", 6);
			}
			
			if (!isValidMSChar(s.charAt(3))) {
				try {
					int imin = Integer.parseInt(s.substring(3, 4));
					int h = Integer.parseInt(s.substring(0, 2));
					imin -= 6;// decrease with 60 minutes
					h++;// increase one hour
					s = twoDigits.format(h) + s.substring(2, 3) + 
							String.valueOf(imin) + s.substring(4);
				} catch (NumberFormatException nfe) {
					throw new ParseException("Invalid minute value", 3);
				}				
				//throw new ParseException("Invalid minute value", 3);
			}
			temp = s;
		}
		return temp;
	}
}