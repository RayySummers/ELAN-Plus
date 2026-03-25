package nl.mpi.recognizer.local.hecate;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.annotator.recognizer.data.Param;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;

/**
 * A recognizer which calls a locally installed {@code Hecate} instance to 
 * perform video shot boundary detection. 
 */
public class HecateRecognizer implements Recognizer {
	private final System.Logger LOG = System.getLogger("hecate");
	/** the recognizer host */
	protected RecognizerHost host;
	/** the run command */
	protected String runCommand;
	/** the parameter list */
	protected List<Param> paramList;
	/** a map for textual parameters */
	protected Map<String, String>paramMapString;
	/** a map for numeric parameters */
	protected Map<String, Float>paramMapFloat;
	/* the list of media paths */
	//protected List<String> mediaPaths;
	/** the recognizer name */
	protected String name;
	/** the recognizer id */
	protected String id;// the short, internal identifier string from the cmdi
	/** the type of recognizer */
	protected int recognizerType;
	/** the base directory for the {@code ProcessBuilder} */
	protected File baseDir;
	/** the recognizer {@code Process} */
	protected Process process;
	/** the process input stream */
	protected InputStream inStream;
	/** the output reader */
	protected BufferedReader reader;
	/** flag for the {@code running} state */
	protected boolean isRunning = false;
	/** the start time of the last run */
	protected long lastStartTime = 0L;
	/** the time of the last successful read action */
	protected volatile long lastReadSucces = 0L;
	/** the output directory, can be null/not specified */
	//private File outputDir;
	/** segments extracted from the console output of whisper */
	private ArrayList<RSelection> segments;
	private float fps;
	private float frameDur;
	
	/**
	 * Constructor.
	 */
	public HecateRecognizer() {
		super();
		paramMapString = new HashMap<String, String>(10);
		paramMapFloat = new HashMap<String, Float>(10);
	}

	@Override
	public boolean setMedia(List<String> mediaFilePaths) {
		// the video file will be one of the parameters
		return false;
	}

	@Override
	public boolean canHandleMedia(String mediaFilePath) {
		return true;
	}

	@Override
	public boolean canCombineMultipleFiles() {
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	@Override
	public int getRecognizerType() {
		return Recognizer.VIDEO_TYPE;
	}

	@Override
	public void setRecognizerHost(RecognizerHost host) {
		this.host = host;

	}

	/**
	 * This recognizer does not provide its own panel.
	 * 
	 * @return {@code null}
	 */
	@Override
	public JPanel getControlPanel() {
		return null;
	}
	
    // required are a run command, an input file path and frames-per-second
	// value (i.e. duration per frame)
	// (boundary) shots are always calculated (parameter: --print_shot_info)
	// key frame extraction is optional (or not supported at all) same for 
	// thumbnail images and summary video
	// optional is an output file path 
	@Override
	public void setParameterValue(String param, String value) {
		paramMapString.put(param, value);	
	}

	@Override
	public void setParameterValue(String param, float value) {
		paramMapFloat.put(param, value);
	}

	@Override
	public Object getParameterValue(String param) {
		if (param == null) {
			return null;
		}
		
		if (paramMapString.containsKey(param)) {
			return paramMapString.get(param);
		} else if (paramMapFloat.containsKey(param)) {
			return paramMapFloat.get(param);// a Float
		}
		
		return null;
	}

	@Override
	public void updateLocale(Locale locale) {
		// method stub
	}

	@Override
	public void updateLocaleBundle(ResourceBundle bundle) {
		// method stub
	}

	@Override
	public void start() {
		runCommand = paramMapString.get("run-command");
		if (runCommand == null || runCommand.isEmpty()) {
			if (host != null) {
				LOG.log(Level.ERROR, "No run command found");
				host.errorOccurred("No run command found");
			}
			return;
		}
		String videoPath = paramMapString.get("--in_video");
		if (videoPath == null || videoPath.isEmpty()) {
			if (host != null) {
				LOG.log(Level.ERROR, "No video input specified");
				host.errorOccurred("No video input has been specified");
			}
			return;
		}
		// remove possible previous results
		if (segments != null) {
			segments.clear();
		}

		try {
			isRunning = true;
			host.setProgress(-1f);
			// round to whole seconds, some OS's do that for the last modified flag
			lastStartTime = (System.currentTimeMillis() / 1000) * 1000;
			
			List<String> cmds = new ArrayList<String>();
			
			String[] tokens = runCommand.split("\\s");
			for (String s : tokens) {
				if (s.length() > 0) cmds.add(s);
			}
			cmds.add("--in_video");
			cmds.add(videoPath);
			
			cmds.add("--print_shot_info");
			
			String outPath = paramMapString.get("--out_dir");
			if (outPath != null && !outPath.isEmpty()) {
				cmds.add("--out_dir");
				cmds.add(outPath);
			}
			
			String fpsString = paramMapString.get("fps");
			if (fpsString != null && !fpsString.isEmpty()) {
				try {
					fps = Float.parseFloat(fpsString);
				} catch (NumberFormatException nfe) {
					fps = 25.0f;
				}
			} else {
				// use a default or throw an exception?
				fps = 25.0f;
			}
			frameDur = 1000 / fps;
			
			ProcessBuilder pBuilder = new ProcessBuilder(cmds);
			pBuilder.redirectErrorStream(true);
//			pBuilder.directory(baseDir);
//			LOG.info("Setting directory: " + baseDir);
			process = pBuilder.start();
			LOG.log(Level.INFO, "Created process with command: " + runCommand);
			host.appendToReport(new Date().toString() + "\n");
			host.appendToReport("Starting process with command:\n");
			host.appendToReport(String.join(" ", cmds) + "\n");
			//process = Runtime.getRuntime().exec(runCommand, null, baseDir);//runCommand
			
			reader = new BufferedReader(new InputStreamReader(
						process.getInputStream(), "UTF-8"));
			new ReaderThread().start();
			
		} catch (IOException ioe) {
			final String msg = "Could not run the recognizer: " + ioe.getMessage();
			LOG.log(Level.ERROR, msg);
			host.appendToReport(msg + '\n');
			host.errorOccurred(msg);
		}
	}

	@Override
	public void stop() {
		// if there is a process stop it?
		if (isRunning && process != null) {			
			// send a message first? Is there a way of closing gracefully?
			LOG.log(Level.INFO, "Stopping recognizer...");
			host.appendToReport("Trying to stop the running recognizer\n");
			process.destroy();
			isRunning = false;
		}
	}

	@Override
	public void dispose() {
		if (isRunning) {
			stop();
		}
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException ioe) {
				// ignore
			}
		}
		host = null;
	}

	@Override
	public void validateParameters() throws RecognizerConfigurationException {
		String rc = paramMapString.get("run-command");
		if (rc == null || rc.isEmpty()) {
			throw new RecognizerConfigurationException("The command to start Hecate has to be provided");
		}
		
		try {
			Float.parseFloat(paramMapString.get("fps"));
		} catch (NumberFormatException nfe) {
			throw new RecognizerConfigurationException("Invalid value for frames per second");
		} catch(NullPointerException npe) {
			throw new RecognizerConfigurationException("A frames per second value is required");
		}
		
	}
	
	/*
	 * Passes the already created segments, if any, to the host.
	 */
	private void convertTiers() {
		if (segments != null && !segments.isEmpty()) {
			final Segmentation segmentation  = new Segmentation("shots", 
					segments, paramMapString.get("in_video"));
			// add the segmentation on the event dispatch thread
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					host.addSegmentation(segmentation);
				}
			});	
		}
		host.setProgress(1.0f);
	}
	
	/**
	 * The shots are provided in a single string.
	 * 
	 * @param line the formatted shots boundaries string
	 */
	private void extractSegments(String line) {	
		// convert the output which looks like this
		// shots: [0:105],[108:198],[201:270],
		if (line.startsWith("shots:")) {
			String[] shotParts = line.substring(6).split(",");
			for (String shot : shotParts) {
				String[] frames = shot.trim().split(":");
				if (frames.length == 2) {
					try {
						int startFr = Integer.parseInt(frames[0].substring(1));
						int endFr = Integer.parseInt(frames[1].substring(0, frames[1].length() - 1));
						
						addSegment(new Segment((long)(startFr * frameDur), (long)(endFr * frameDur), "" ));
					} catch (NumberFormatException nfe) {
						LOG.log(Level.WARNING, nfe.getMessage());
					}
				}
			}
		}
	}
	
	/*
	 * Adds a segment to the list of segments, creates the list if it hasn't
	 * been created yet.
	 */
	private void addSegment(Segment segm) {
		if (segments == null) {
			segments = new ArrayList<RSelection>();
		}
		segments.add(segm);
	}

	/**
	 * Internal thread class for reading messages from the recognizer.
	 * 
	 * @author Han Sloetjes
	 */
	class ReaderThread extends Thread {
		
		@Override
		public void run() {
			//System.out.println("Start Reading...");
			while (isRunning && reader != null) {
				try {
					String line = reader.readLine();	
					//System.out.println("Read: " + line);
					if (line != null) {
						lastReadSucces = System.currentTimeMillis();
						host.appendToReport(line + "\n");
	
						extractSegments(line);
						//int numSegments = segments != null ? segments.size() : 0;
						// try to detect an error in a recognizer, end of file, end of transmission codes
						if (line.length() == 1) {
							if (line.charAt(0) == '\u0004') {// end of transmission
								LOG.log(Level.WARNING, "Recognizer failed... end of transmission");
								host.errorOccurred("Recognizer failed, end of transmission (there may be partial results).");
								host.appendToReport("Recognizer failed, end of transmission (there may be partial results).\n");
								break;
							} else {
								try {
									int eof = Integer.parseInt(line);
									if (eof == -1) {// != 0 ??
										LOG.log(Level.WARNING, "Recognizer failed... end of transmission");
										host.errorOccurred("Recognizer failed, end of transmission (there may be partial results).");
										host.appendToReport("Recognizer failed, end of transmission (there may be partial results).\n");
										break;
									} else if (eof == 0){
										LOG.log(Level.INFO ,"Recognizer terminated successfully.");
										host.appendToReport("Recognizer terminated successfully...\n");
										host.setProgress(0.9f);// or 0.9 to leave time for passing the segmentation?
										break;
									}
								} catch (NumberFormatException nfe) {
									//ignore
								}
							}
						}
					} else {
						// end of stream?
						if (!process.isAlive()) {
							int exitVal = process.exitValue();
							if (exitVal == 0) {
								LOG.log(Level.INFO, "Recognizer terminated successfully with exit code: " + exitVal);
								host.appendToReport("Recognizer terminated successfully (exit code: " + exitVal + ")\n");
								host.setProgress(0.9f);// or 0.9 to leave time for passing the segmentation?
							} else {
								LOG.log(Level.WARNING, "Recognizer stopped unexpectedly with exit code: " + exitVal);
								host.errorOccurred("Recognizer stopped unexpectedly with exit code: " + exitVal);
								host.appendToReport("Recognizer stopped unexpectedly with exit code: " + exitVal + "\n");		
							}
							break;
						}
					}
				} catch (IOException ioe) {
					LOG.log(Level.INFO, "Exception while reading the recognizer output: " + ioe.getMessage());
					// break;??
				}
			}
			
			convertTiers();
			
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					// ignore
				}
			}
			isRunning = false;
		}
	}

}
