package nl.mpi.avf.player;

/**
 * An interface defining constants for native log levels.
 */
public interface JAVFLogLevel {
	/** log all messages */
	public static final int ALL = 0;
	/** log debug level messages */
	public static final int FINE = 3;
	/** log general info messages */
	public static final int INFO = 5;
	/** log only warning messages */
	public static final int WARNING = 8;
	/** log no messages at all */
	public static final int OFF = 10;

	/*
	ALL(0),
	FINE(3),
	INFO(5),
	WARNING(8),
	OFF(10);
	
	public int level;
	private JAVFLogLevel(int level) {
		this.level = level;
	}
	*/
	
}