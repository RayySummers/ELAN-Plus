package nl.mpi.recognizer.local.whisper;

import mpi.eudico.client.annotator.recognizer.api.LocalRecognizer;
import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.annotator.recognizer.data.Param;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A recognizer which calls a local installation of 
 * <a href="https://openai.com/blog/whisper/">Whisper</a> to transcribe or translate an
 * audio file. <p>
 * The implementation is inspired by the {@link LocalRecognizer} but creates the 
 * local run command in a different way.
 * 
 * @author Han Sloetjes
 */
public class WhisperRecognizer implements Recognizer {
	private final System.Logger LOG = System.getLogger("whisper");
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
	private File outputDir;
	/** segments extracted from the console output of whisper */
	private ArrayList<RSelection> segments;
	/*
	 * [00:16.360 --> 00:17.360]  tree
	 * or 
	 * [00:01:36.940 --> 00:01:54.140] There
	 */
	private Pattern timePat = Pattern.compile("([0-9]{2}[:,.]{1}){1,2}[0-9]{2}[:,.]{1}[0-9]{2,3}");
	/** to limit the number of error messages of failed time conversions */
	private int numTimeLogs = 0;
	
	/**
	 * No-argument constructor.
	 */
	public WhisperRecognizer() {
		super();
		paramMapString = new HashMap<String, String>(10);
		paramMapFloat = new HashMap<String, Float>(10);
	}

	@Override
	public boolean setMedia(List<String> mediaFilePaths) {
		// the wave file will be one of the parameters
		return false;
	}

	/**
	 * The media file will be processed by {@code ffmpeg}, assume here that it
	 * is supported.
	 * 
	 * @return {@code true}
	 */
	@Override
	public boolean canHandleMedia(String mediaFilePath) { 
		return true;
	}

	/**
	 * Returns {@code false}. Although {@code whisper} can be called with
	 * multiple audio files, they are still processed individually.
	 * 
	 * @return {@code false}
	 */
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

	/**
	 * This recognizer identifies as an audio recognizer (although video files
	 * are also supported).
	 * 
	 * @return {@code Recognizer#AUDIO_TYPE}
	 */
	@Override
	public int getRecognizerType() {
		return Recognizer.AUDIO_TYPE;
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
		String audioPath = paramMapString.get("audio");
		if (audioPath == null || audioPath.isEmpty()) {
			if (host != null) {
				LOG.log(Level.ERROR, "No audio input specified");
				host.errorOccurred("No audio input has been specified");
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
			// if the run command consists of more than 1 "word" check if part of the command is within quotes
			int fqIndex = runCommand.indexOf('"');
			if (fqIndex > -1) {
				cmds.addAll(parseCommand(runCommand));
			} else {
				String[] tokens = runCommand.split("\\s");
				for (String s : tokens) {
					if (s.length() > 0) cmds.add(s);
				}
			}
			
			// add options and the audio file to the array
			Iterator<String> keyIt = paramMapString.keySet().iterator();
			while (keyIt.hasNext()) {
				String key = keyIt.next();
				// the first and last parameter
				if (key.equals("run-command") || key.equals("audio")) {
					continue;
				}
				// special case for the output folder
				if (key.equals("--output_dir")) {
					String outFold = paramMapString.get(key);
					// output folder can be left out, then (.) is used
					if (outFold != null && !outFold.isEmpty()) {
						File f = new File(outFold);
						// the folder should or should not exist yet?,
						// on Windows Python/Whisper sometimes seems to be unable
						// to create a missing folder
	
						outputDir = f;
						cmds.add(key);
						cmds.add(outFold);
					}
					continue;
				}
				// if no language selected don't add the parameter
				if (key.equals("--language")) {
					if (! "--".equals(paramMapString.get(key))) {
						cmds.add(key);
						cmds.add(paramMapString.get(key));
					}
					continue;
				}
				
				if (paramMapString.get(key) != null) {
					cmds.add(key);
					cmds.add(paramMapString.get(key));
				}
			}
			
			if (!paramMapFloat.isEmpty()) {
				Iterator<String> numKeyIt = paramMapFloat.keySet().iterator();
				while (numKeyIt.hasNext()) {
					String key = numKeyIt.next();
					if (paramMapFloat.get(key) != null) {
						
						// best_of and beam_size must be integers
						if (key.equals("--best_of") || key.equals("--beam_size")) {
							cmds.add(key);
							int best = (int) paramMapFloat.get(key).floatValue();
							cmds.add(String.valueOf(best));
						} else if (key.equals("--length_penalty")) {
							 Float fval = paramMapFloat.get(key);
							 // not passing the default value, could be considered for other parameters as well
							 if (fval.floatValue() != 0.5f) {
								cmds.add(key);
								cmds.add(String.format(Locale.US, "%.2f", paramMapFloat.get(key)));
							 }
						} else {
							cmds.add(key);
							cmds.add(String.format(Locale.US, "%.2f", paramMapFloat.get(key)));
						}
					}
				}
			}

			// finally add the audio file
			if (audioPath != null) {
				if (audioPath.startsWith("file")) {
					audioPath = audioPath.substring(5);
				}
				if (audioPath.length() > 5) {
					if (audioPath.substring(0, 5).matches("///[a-zA-Z]:")) {
						// assume Windows but no need to replace forward slashes
						//audioPath = audioPath.substring(3).replace('/', '\\');// remove the 3 backslashes and replace slashes
						audioPath = audioPath.substring(3);
					} 
				}
			}
			paramMapString.put("audio", audioPath);
			cmds.add(audioPath);
			
			ProcessBuilder pBuilder = new ProcessBuilder(cmds);
			pBuilder.redirectErrorStream(true);
//			pBuilder.directory(baseDir);
//			LOG.info("Setting directory: " + baseDir);
			// make sure the Python environment is initialized in UTF-8 mode
			pBuilder.environment().put("PYTHONUTF8", "1");
			// and make sure the Python environment is unbuffered mode
			pBuilder.environment().put("PYTHONUNBUFFERED", "TRUE");//True, 1
			
			Map<String, String> sysenv = pBuilder.environment();
			// on macOS and Linux try to get a more complete environment than, especially a more complete
			// PATH variable (otherwise the path might only contain: /usr/bin:/bin:/usr/sbin:/sbin ).
			if (sysenv.containsKey("SHELL")) {
				Map<String, String> shellEnv = getShellEnv(sysenv.get("SHELL"));
				for (String sk : shellEnv.keySet()) {
					// or only add the PATH variable?
					sysenv.put(sk, shellEnv.get(sk));
					//System.out.println("SK: " + sk + " SV: " + shellEnv.get(sk));
				}
			}
			
			/*
			for (String k : sysenv.keySet()) {
				System.out.println("K: " + k + "  V: " + sysenv.get(k));
			}
			*/
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

	/**  
	 * To be called when the command contains quoted parts and should not 
	 * simply be tokenized based on white spaces. Tries to extract the 
	 * quoted parts first and tokenizes the remainder. 
	 */
	private List<String> parseCommand(String command) {
		List<String> tokenList = new ArrayList<String>();
		
		List<Integer> qList = new ArrayList<Integer>();
		int qi = command.indexOf('"');
		while (qi > -1) {
			qList.add(qi);
			qi = command.indexOf('"', qi + 1);
		}
		if (qList.size() == 0 || qList.size() % 2 != 0) {
			tokenList.add(command);
			return tokenList;
		}
		
		int sta = -1;
		int end = -1;
		for (int i = 0; i < qList.size() - 1; i += 2) {
			int oldEnd = end; 
			sta = qList.get(i);
			end = qList.get(i + 1);
			
			if (i == 0) {
				if (sta > 0) {
					String[] toks = command.substring(0, sta).split("\\s");
					for (String s : toks) {
						if (s.length() > 0) tokenList.add(s);
					}
				} 	
			} else if (sta > oldEnd + 1) {
				String[] toks = command.substring(oldEnd + 1, sta).split("\\s");
				for (String s : toks) {
					if (s.length() > 0) tokenList.add(s);
				}
			}
			
			tokenList.add(command.substring(sta, end + 1));
			
			if (i == qList.size() - 1) {
				if (end < command.length() - 1) {
					String[] toks = command.substring(end + 1).split("\\s");
					for (String s : toks) {
						if (s.length() > 0) tokenList.add(s);
					}
				}
			}
		}
		
		return tokenList;
	}

	/**
	 * Tries to load environment variables for the user's login shell. Especially the PATH
	 * variable might be necessary to make sure whisper 'finds' ffmpeg.
	 * 
	 * @param shell the user's default shell (e.g. /usr/zsh or /usr/bash)
	 * 
	 * @return a map with variable name-value pairs
	 */
	private Map<String, String> getShellEnv(String shell) {
		Map<String, String> envMap = new HashMap<String, String>();
		try {
			
			ProcessBuilder pBuilder = new ProcessBuilder(shell, "-l", "-c", "env");
			pBuilder.redirectErrorStream(true);
			Process proc = pBuilder.start();

			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("=");
				if (parts .length == 2) {
					envMap.put(parts[0], parts[1]);
				}
			}
		} catch (Throwable t) {
			LOG.log(Level.WARNING, "Error reading environment variables: " + t.getMessage());
		}
		
		return envMap;
	}
	
	
	/**
	 * Instead of adding the collected segments from the console/terminal 
	 * output, here an attempt could be made to discover the correct .srt file
	 * in the output directory and load that contents instead.
	 */
	private void convertTiers() {
		String outDir = "";
		if (outputDir != null) {
			outDir = outputDir.getAbsolutePath();
		} else {
			String uDir = System.getProperty("user.dir");
			if (uDir != null)
				outDir = uDir;
		}
		String audioName = paramMapString.get("audio");
		int slashInd = audioName.lastIndexOf('/'); 
		if (slashInd > 0 && slashInd < audioName.length()) {
			audioName = audioName.substring(slashInd);
		} else {
			slashInd = audioName.lastIndexOf('\\');
			if (slashInd > 0 && slashInd < audioName.length()) {
				audioName = audioName.substring(slashInd);
			}
		}
		String outPath = outDir + audioName;
		outPath = outPath.replace('\\', '/');
		host.appendToReport("Output files may have been created here:\n");
		host.appendToReport(outPath + ".srt (/.txt/.vtt/.tsv/.json)\n");
		host.appendToReport("The .srt and .vtt file can maybe be imported via the Import->Subtitle File menu\n");
		host.appendToReport("The .tsv file can maybe be imported via the Import->CSV / Tab-delimited Text File  menu\n");
		
		if (segments != null && !segments.isEmpty()) {
			final Segmentation segmentation  = new Segmentation(paramMapString.get("--task"), 
					segments, paramMapString.get("audio"));
			// add the segmentation on the event dispatch thread
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					host.addSegmentation(segmentation);
				}
			});	
		}
		String wordMode = paramMapString.get("--word_timestamps");
		if (wordMode != null && "true".equalsIgnoreCase(wordMode)) {
			ArrayList<RSelection> wordSegments = loadWords(outPath); 
			
			if (wordSegments != null && !wordSegments.isEmpty()) {
				final Segmentation wordSegmentation  = new Segmentation("words",
						        wordSegments, paramMapString.get("audio"));
				// add the segmentation on the event dispatch thread
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						host.addSegmentation(wordSegmentation);
					}
				});
			} else {
				host.appendToReport("Word_timestamps was selected but no word segmentation was loaded\n");
			}
		}
		host.setProgress(1.0f);
	}
	
	/*
	 * Checks a line of output for the existence of time stamps and label and
	 * extracts these when found.
	 */
	private void extractSegment(String line) {		
		if (line != null && line.startsWith("[") && line.length() > 22) {
			Matcher m = timePat.matcher(line);
			try {
				if (m.find()) {
					long bt = 0, et = 0;
					String label = "";
					bt = toMs(line.substring(m.start(), m.end()));
					if (m.find()) {
						et = toMs(line.substring(m.start(), m.end()));
						label = line.substring(m.end() + 1).trim();
					}
					
					if (et > bt) {
						addSegment(new Segment(bt, et, label));
					}
				}
			} catch (IndexOutOfBoundsException iobe) {
				if (numTimeLogs++ < 10) {
					LOG.log(Level.WARNING, iobe.getMessage());
				}
			}
		}
	}
	
	/*
	 * Converts a time string to a milliseconds long value.
	 */
	private long toMs(String time) {
		long t = 0;
		try {
			if (time.length() == 9) {
				int m = Integer.parseInt(time.substring(0, 2));
				t += m * (60000);//60 * 1000
				int s = Integer.parseInt(time.substring(3, 5));
				t += s * 1000;
				int ms = Integer.parseInt(time.substring(6));
				t += ms;
			} else if (time.length() == 12) {
				int h = Integer.parseInt(time.substring(0, 2));
				t += h * 3600000;//60 * 60 * 1000
				int m = Integer.parseInt(time.substring(3, 5));
				t += m * (60000);//60 * 1000
				int s = Integer.parseInt(time.substring(6, 8));
				t += s * 1000;
				int ms = Integer.parseInt(time.substring(9));
				t += ms;
			}
		} catch (NumberFormatException nfe) {
			if (numTimeLogs++ < 10) {
				LOG.log(Level.WARNING, nfe.getMessage());
			}
		}
		
		return t;
		
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
	
	/*
	 * Format of Whisper word boundary .srt
	 * 
	 * 27
	 * 00:00:07,680 --> 00:00:07,880
	 * <u>Und</u> das hat mir was eingefallen.
	 */
	private ArrayList<RSelection> loadWords(String outPath) {
		// outPath may end with .wav or similar
		if (outPath == null) {
			return null;
		}
		int dotIndex = outPath.lastIndexOf(".");
		// assume file extension is not longer than 4 characters
		if (dotIndex > 0 && outPath.length() - dotIndex < 5) {
			outPath = outPath.substring(0, dotIndex + 1) + "srt";
		}
		
		BufferedReader bufRead = null;
        try {
			File sourceFile = new File(outPath);
			String charSet = "UTF-8";
			
			InputStreamReader isr = new InputStreamReader(new FileInputStream(sourceFile), charSet);
			bufRead = new BufferedReader(isr);
			ArrayList<RSelection> wordSegments = new ArrayList<RSelection>();
			final String FROM_TO = "-->";
			// the regular expression for HTML tags and the {b}-style variant
			// thereof. Use of ".+?" (reluctant quantifier) to prevent removal
			// of everything between the first "<" and the ">" of the closing
			// tag.
			final String US = "<u>";
			final String UE = "</u>";
			
			String line = null;
			
			long curBT = 0, curET = 0;
			String curWord = "";
			// start read
			while ((line = bufRead.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				int tindex = line.indexOf(FROM_TO); 
				if (tindex > 0) {
					curBT = toMs(line.substring(0, tindex).strip());
					curET = toMs(line.substring(tindex + 3).strip());
					continue;
				}
				int usIndex = line.indexOf(US);
				int ueIndex = line.indexOf(UE);
				if (usIndex >= 0) {
					if (ueIndex > usIndex) {
						// default case, the word is on a single line
						curWord = line.substring(usIndex + US.length(), ueIndex).strip();
					} else {
						// only begin tag on this line (probably not possible)
						curWord += line.substring(usIndex +US.length()).strip();
						continue;
					}
				} else if (ueIndex >= 0) {
					// only end tag on this line (probably not possible)
					curWord += line.substring(0, ueIndex).strip();
				}
				
				if (curBT >= 0 && curET > curBT && !curWord.isEmpty()) {
					wordSegments.add(new Segment(curBT, curET, curWord));
					curWord = "";
					curBT = 0;
					curET = 0;
				}
			}
		
			return wordSegments;
		} catch (FileNotFoundException fnfe) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Import of words failed: " + fnfe.getMessage());
			}
			host.appendToReport("The file with word boundaries could not be loaded: " + fnfe.getMessage() + "\n");
		} catch (IOException ioe) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Import of words failed: " + ioe.getMessage());
			}
			host.appendToReport("The file with word boundaries could not be read: " + ioe.getMessage() + "\n");
		} catch (Throwable t) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Import of words failed: " + t.getMessage());
			}
			host.appendToReport("The file with word boundaries could not be processed: " + t.getMessage() + "\n");
		} finally {
			if (bufRead != null)
			try {
				bufRead.close();
			} catch (Throwable tt) {/*ignore*/}
		}
        
		return null;
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

						extractSegment(line);
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
						// end of stream
						int exitVal = process.exitValue();
						if (exitVal == 0) {
							LOG.log(Level.INFO, "Recognizer terminated successfully with exit code: " + exitVal);
							host.appendToReport("Recognizer terminated successfully (exit code: " + exitVal + ")\n");
							host.setProgress(0.9f);// or 0.9 to leave time for passing the segmentation?
						} else {
							LOG.log(Level.WARNING, "Recognizer stopped unexpectedly with exit code: " + exitVal);
							host.errorOccurred("Recognizer stopped unexpectedly with exit code: " + exitVal + " (there may be partial results)");
							host.appendToReport("Recognizer stopped unexpectedly with exit code: " + exitVal + " (there may be partial results)\n");		
						}
						break;
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
