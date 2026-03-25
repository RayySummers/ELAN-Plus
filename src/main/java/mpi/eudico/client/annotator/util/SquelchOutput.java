package mpi.eudico.client.annotator.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * A utility class to temporarily suppress all messages to {@code System#out}
 * and {@code System#err}.
 */
public class SquelchOutput {
	PrintStream oldStdout = null;
	PrintStream oldStderr = null;
	
	/**
	 * Creates a new instance of the utility.
	 */
	public SquelchOutput() {
		super();
	}

	private class SilentPrintStream extends ByteArrayOutputStream {
		@Override
		public void flush() throws IOException {
			super.reset();
		}
	}
	
	// replaces the output writers to a null version
	/**
	 * Replaces the standard output writers to a silent version of an
	 * {@code OutputStream}. The original streams are cached to be
	 * able to restore them later.
	 * 
	 * @throws IOException any IO exception
	 */
	@SuppressWarnings("resource") // close()d in restoreOutput().
	public void squelchOutput() throws IOException {
		// sanity check
		if ( oldStdout != null) {
			throw new IOException("Output already squelched");
		}
		
		oldStdout = System.out;
		oldStderr = System.err;
		
		System.setOut( new PrintStream( new SilentPrintStream(), true ));
	    System.setErr( new PrintStream( new SilentPrintStream(), true ));
	}
	
	/**
	 * Restores the original "standard" output streams.
	 *  
	 * @throws IOException any IO exception
	 */
	public void restoreOutput() throws IOException {
		// sanity check
		if (oldStdout == null) {
			throw new IOException("Output was not squelched");
		}
		
		System.out.close();
		System.err.close();
		
		System.setOut(oldStdout);
		System.setErr(oldStderr);
		
		oldStdout = null;
		oldStderr = null;
	}
}