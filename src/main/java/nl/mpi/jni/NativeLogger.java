package nl.mpi.jni;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that native players or other native components can use to print
 * messages to the same file the Java classes are logging to. 
 * 
 * @author Han Sloetjes
 */
public class NativeLogger {
	private final static Logger LOG = Logger.getLogger("NativeLogger");

	/**
	 * Constructor.
	 */
	public NativeLogger() {
	}
	
	/**
	 * To be called from native libraries, logs all incoming messages unless 
	 * logging has been switched off. It is assumed that determining whether
	 * a message has to be logged, based on the level of severity, is 
	 * done in the native code. 
	 * 
	 * @param msg the message to log
	 */
	static void nlog(String msg) {
		if (LOG.getLevel() == null || LOG.getLevel() != Level.OFF) {
			LOG.log(NativeLogger.NATIVE, msg);
		}
	}
	
	/** a level just "above" SEVERE, so it will be logged unless logging is switched off */
	public static final NLevel NATIVE = new NLevel("NATIVE", Level.SEVERE.intValue() + 1);
	
	@SuppressWarnings("serial")
	static class NLevel extends Level {
		protected NLevel(String name, int value) {
			super(name, value);
		}		
	}
	
}
