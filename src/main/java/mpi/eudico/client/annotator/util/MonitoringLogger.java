package mpi.eudico.client.annotator.util;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Logger to log all the events that are being monitored.
 * 
 * @author aarsom
 *
 */
public class MonitoringLogger {
	
	/* events that are been monitored */
	/** change annotation times event */
	public static final String CHANGE_ANNOTATION_TIME = ElanLocale.getString("MonitorLoggingEvent.Change_Ann_Time");
	/** change annotation value event */ 
	public static final String CHANGE_ANNOTATION_VALUE = ElanLocale.getString("MonitorLoggingEvent.Change_Ann_Val");
	/** file closing event */
	public static final String CLOSE_FILE = ElanLocale.getString("MonitorLoggingEvent.Close_File");
	/** create dependent annotations event */
	public static final String CREATE_DEPENDING_ANNOTATIONS = ElanLocale.getString("MonitorLoggingEvent.Create_Depending_Annotations"); 
	/** delete annotation event */
	public static final String DELETE_ANNOTATION = ElanLocale.getString("MonitorLoggingEvent.Delete_Annotation");
	/** delete multiple annotations event */
	public static final String DELETE_MULTIPLE_ANNOTATION = ElanLocale.getString("MonitorLoggingEvent.Delete_Multiple_Annotation");
	/** ELAN exiting event */
	public static final String EXIT_ELAN = ElanLocale.getString("MonitorLoggingEvent.Exit_Elan");
	/** merge annotations event */	
	public static final String MERGE_ANNOTATION = ElanLocale.getString("MonitorLoggingEvent.Merge_Annotation");
	/** monitoring paused event */
	public static final String MONITORING_PAUSED = ElanLocale.getString("MonitorLoggingEvent.Monitoring_Paused");
	/** monitoring started event */
	public static final String MONITORING_STARTED = ElanLocale.getString("MonitorLoggingEvent.Monitoring_Started");
	/** monitoring stopped event */ 
	public static final String MONITORING_STOPPED = ElanLocale.getString("MonitorLoggingEvent.Monitoring_Stopped");
	/** new annotation event */
	public static final String NEW_ANNOTATION = ElanLocale.getString("MonitorLoggingEvent.New_Annotation");
	/** new file event */
    public static final String NEW_FILE = ElanLocale.getString("MonitorLoggingEvent.New_File");
    /** open file event */
    public static final String OPEN_FILE = ElanLocale.getString("MonitorLoggingEvent.Open_File");
    /** save file event */
    public static final String SAVE_FILE = ElanLocale.getString("MonitorLoggingEvent.Save_File");
    /** split annotation event */
    public static final String SPLIT_ANNOTATION = ElanLocale.getString("MonitorLoggingEvent.Split_Annotation");
    /** recognizer started event */
    public static final String RECOGNIZER_STARTED = ElanLocale.getString("MonitorLoggingEvent.Recognizer_Started");
    /** recursive annotation creation event */
    public static final String RECURSIVE_ANNOTATIONS = ElanLocale.getString("MonitorLoggingEvent.Recursive_Annotations");  
    /** redo event */
    public static final String REDO = ElanLocale.getString("MonitorLoggingEvent.Redo");
    /** undo event */
    public static final String UNDO = ElanLocale.getString("MonitorLoggingEvent.Undo");    
    
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS");	
    private static final DateFormat dateFormat1 = new SimpleDateFormat("mm:ss.SSS");	
    private static final DateFormat dateFormat2 = new SimpleDateFormat("ss.SSS");	   	
    private static final DateFormat dateFormat3 = new SimpleDateFormat("SSS");	
    
	private static HashMap<Transcription, MonitoringLogger> loggerMap;
	
	private static boolean appendDataToFile = true;
	
	private static boolean newFilesPerSession = false;
	
	private static boolean monitoringStarted= false;
	
	private static boolean monitoringInitiated= false;
	
	private static Calendar cal;
	
	// path of the log file
	private static String path;
	
	// global logger
	private static MonitoringLogger globalLogger;
	
	// contains the logged events
	private StringBuilder buffer;
	
	private HashMap<Integer, StringBuilder> bufferMap;
	
	private int noOfSessions = 0;
	
	private int fileIndex = -1;
	
	private Transcription transcription;
	
	private boolean useDefaultPath = true;
	
	private MonitoringLogger(boolean global){
		buffer = new StringBuilder();
	}
	
	/**
	 * Get the logger of the given transcription.
	 * 
	 * @param trans transcription for which the logger is to be return
	 * @return the logger
	 */
	public static synchronized MonitoringLogger getLogger(Transcription trans){
		if(monitoringInitiated){
			if(trans == null){
				if(globalLogger==null){
					globalLogger = new MonitoringLogger(true);
					globalLogger.transcription = trans;
				}
				return globalLogger;
			}
		
			if(loggerMap == null){
				loggerMap = new HashMap<Transcription, MonitoringLogger>();
			}
		
			if(loggerMap.get(trans) != null){
				return loggerMap.get(trans);
			}
		
			MonitoringLogger logger = new MonitoringLogger(false);
			logger.transcription = trans;
			loggerMap.put(trans, logger);
			return logger;
		}
		 return null;
	}
	
	/**
	 * Returns whether the monitoring process has been initialized.
	 * 
	 * @return {@code true} if monitoring is initiated
	 */
	public static boolean isInitiated(){
		return monitoringInitiated;
	}
	
	/**
	 * Starts/ stops the monitoring process 
	 * 
	 * @param start if {@code true} starts the process, 
	 * 				if {@code false}, the process is stopped / paused
	 */
	public static void startMonitoring(boolean start){
		monitoringStarted = start;
		monitoringInitiated = true;
		readPreferrences();
	}
	
	private static void readPreferrences(){
		Boolean boolPref = Preferences.getBool("ActivityMonitoring.AppendToFile", null);
		if(boolPref != null){
			appendDataToFile = boolPref.booleanValue();		
		}
		
		boolPref = Preferences.getBool("ActivityMonitoring.FilesPerSession", null);
		if(boolPref != null){
			newFilesPerSession = boolPref.booleanValue();
		}	
		
		boolPref = Preferences.getBool("ActivityMonitoring.UseLocation", null);
		if(boolPref != null && boolPref.booleanValue()){
			String stringPref = Preferences.getString("ActivityMonitoring.PathLocation", null);
			if (stringPref != null) {
				path = stringPref;
	    	}
    	}
	}
	
	/**
	 * Set the path for writing the log files
	 * 
	 * @param path the path where the log file is to be written
	 */
	public static void setDirectory(String path){
		if(MonitoringLogger.path == null || !MonitoringLogger.path.equals(path)){
			MonitoringLogger.path = path;
			
			if(loggerMap != null){
				MonitoringLogger logger = null;
				Iterator<Transcription> keyIt = loggerMap.keySet().iterator();

				while (keyIt.hasNext()) {
					logger = loggerMap.get(keyIt.next());
					logger.fileIndex = -1;
				}
			}			
		}
	}
	
	/**
	 * Set the flag to append to the file.
	 * 
	 * @param append if {@code true} messages are appended to the file,
	 * otherwise the file is recreated
	 */
	public static void setAppendFileFlag(boolean append){
		MonitoringLogger.appendDataToFile = append;
	}
	
	/**
	 * Set the flag to create new files per session.
	 * 
	 * @param create if {@code true} a new monitoring file is created for each session
	 */
	public static void createNewFilesPerSession(boolean create){
		MonitoringLogger.newFilesPerSession = create;
	}
		
	/**
	 * Method which logs the event. 
	 * 
	 * @param event  event description to be logged
	 * @param args parameters related to the event
	 */
	public void log(String event, String... args){
		if(!monitoringStarted){
			return;
		}
		cal = Calendar.getInstance();	
//		buffer.append(new java.sql.Timestamp(cal.getTime().getTime()).getTime());
		buffer.append(System.currentTimeMillis());
		buffer.append('\t');
		buffer.append(dateFormat.format(cal.getTime()));
		buffer.append('\t');
		buffer.append(dateFormat1.format(cal.getTime()));
		buffer.append('\t');
		buffer.append(dateFormat2.format(cal.getTime()));
		buffer.append('\t');
		buffer.append(dateFormat3.format(cal.getTime()));
		buffer.append('\t');
		buffer.append(event);
		for(String i : args)
		{
			buffer.append('\t');
			i = i.replaceAll("\\n", " ");			
			buffer.append(i);
		}
		buffer.append('\n');
		
		if(CLOSE_FILE.equals(event)){
			writeFile();
			if(transcription != null){
				loggerMap.remove(transcription);
			}
			transcription = null;
			buffer = null;
		}		

		if(newFilesPerSession && MONITORING_STOPPED.equals(event) ){
			createNewSessionFile();
		}
	}
	
	private void createNewSessionFile(){
		if(this == globalLogger){
			return;
		}
		

		if(bufferMap == null){
			bufferMap = new HashMap<Integer, StringBuilder>();
		}
		
		bufferMap.put(++noOfSessions, buffer);
		
		buffer = new StringBuilder();
	}
	
	/**
	 * Method to log the event in all the available loggers
	 * 
	 * @param event  event description to be logged
	 * @param args parameters related to the event
	 */
	public static void logInAllLoggers(String event, String... args){				
		if(!monitoringStarted){
			return;
		}
		
		if(globalLogger != null){
			globalLogger.log(event, args);
		}
		
		if(loggerMap == null){
			return;
		}
		
		MonitoringLogger logger = null;
		Iterator<Transcription> keyIt = loggerMap.keySet().iterator();

		while (keyIt.hasNext()) {
			logger = loggerMap.get(keyIt.next());
			if(logger != null){
				logger.log(event, args);
			}
		}
		
		
	}
	
	/**
	 * When ELAN is closed, log the event to the file.
	 */
	public static void exitElan(){
		if(globalLogger != null){
			globalLogger.log(EXIT_ELAN);
			//writeGlobalFile();
		}
		
		if(loggerMap == null){
			return;
		}
				
		MonitoringLogger logger = null;
		Iterator<Transcription> keyIt = loggerMap.keySet().iterator();

		while (keyIt.hasNext()) {
			logger = loggerMap.get(keyIt.next());
			if(logger != null){
				logger.log(EXIT_ELAN);
			}
		}
	}
	
//	private void writeGlobalFile(){
//		String globalFileName = "%h/Library/Preferences/ELAN/activityMonitoring%u_.txt";		
//		FileWriter fstream = null;
//		
//		File logFile = new File(globalFileName);
//		try {
//			if(logFile.exists()){
//				fstream = new FileWriter(globalFileName , true);
//			}else {
//				fstream = new FileWriter(globalFileName);
//			}
//			BufferedWriter out = new BufferedWriter(fstream);
//			out.write(globalBuffer.toString());
//			out.close();
//			globalBuffer.delete(0, globalBuffer.length()-1);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * Writes the logged events to the file.
	 */
	private void writeFile(){	
		if(this == globalLogger){
			return;
		}
		
		final String  transName = ((TranscriptionImpl) transcription).getName();	
		if(!transName.endsWith(".eaf")){
			return;
			
		}
		final String fileName = transName.substring(0,transName.length() - 4);		
		
		if(path != null){
			File logFile = new File(path);
			if(logFile.isDirectory() && logFile.exists()){
				useDefaultPath = false;
			} else {
				useDefaultPath = true;
			}
		}
		
		String filePath = null;
		if(useDefaultPath){
			filePath =  ((TranscriptionImpl) transcription).getPathName();	
			filePath = filePath.substring(0,(filePath.length()-transName.length()-1));			
		} else {			
			filePath = path;
		}
		
		String filePrefix = null;
		String file = null;
		if(noOfSessions > 0){	
			filePrefix = fileName +"-";
			 while(noOfSessions > 0){	
				 if(fileIndex < 0){
						computeFileIndex(filePath, filePrefix);
				 }		
				 fileIndex = fileIndex+1;
				 file = filePath.trim() + "/" + filePrefix + fileIndex +".txt";
				 write(file, bufferMap.get(noOfSessions));
				 noOfSessions = noOfSessions - 1;
			}
		}		
		
		if(newFilesPerSession){		
			if(filePrefix == null){
				filePrefix = fileName +"-";	
			}
				if(fileIndex < 0){
					computeFileIndex(filePath, filePrefix);
				}		
				fileIndex = fileIndex+1;
				file = filePath.trim() + "/" + filePrefix + fileIndex +".txt";		
		}	else {
				file = filePath.trim() + "/" +  fileName +".txt";
		}			
		write(file, buffer);
	}

	private void write(String file, StringBuilder buffer){
		FileWriter fstream = null;
		
		File logFile = new File(file);	
		
		try {
			if(logFile.exists()){
				fstream = new FileWriter(file , appendDataToFile);
			}else {
				fstream = new FileWriter(file);
			}
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(buffer.toString());
			out.close();
			buffer.delete(0, buffer.length()-1);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fstream != null) {
				try {
					fstream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * Compute the last used file index number of the given transcription under the given folder.
	 * 
	 * @param folder the folder in which the index is to be computed
	 * @param filePreffix prefix of the files from which the index is computed
	 */
	private void computeFileIndex(String folder, final String filePreffix){		
		File dir = new File(folder);
		String[] files = dir.list(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
		        return name.startsWith(filePreffix) && name.endsWith(".txt");
		    }
		});
		
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		if(files != null){
			Arrays.sort(files);
			for(int i=0; i< files.length; i++){
				String name = files[i];
				name = name.substring(filePreffix.length(), name.length()-4);
				if(name.length() > 0){
					try{
						int n = Integer.parseInt(name);
						indexes.add(n);
					}catch(NumberFormatException e){
						
					}
				}
			}
		}
		
		if(indexes.size() > 0){
			Collections.sort(indexes);
			fileIndex = indexes.get(indexes.size()-1);
		} else {
			fileIndex = -1;
		}
	}
}
