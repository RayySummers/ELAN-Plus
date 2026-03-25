package mpi.eudico.server.corpora.clomimpl.wordlist;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;

import static mpi.eudico.server.corpora.util.ServerLogger.LOG;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationDocEncoder;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.ProcessReporter;
import mpi.eudico.util.MutableInt;

/**
 * Extracts unique words or unique annotation values from a selection of tiers
 * and writes the results to a text file. The input can either be a single 
 * transcription or a list of {@code EAF} files.
 * This class is not thread-safe, for each word list a new instance should be
 * created.
 * 
 * @author Han Sloetjes
 * @version 1.0
 * @version 2.0 April 2021 added word count and count reporting 
 */
public class Transcription2WordList implements AnnotationDocEncoder, ProcessReporter {
    final private String NEWLINE = "\n";
    private String delimiters = " \t\n\r\f.,!?\"\'";
    
    private ProcessReport report;
    private int wordCount;
    private int annCount;
    private int uniqueCount;
    private int tierCount;
    private int fileCount;

    /**
     * Creates a new Transcription2WordList instance.
     */
    public Transcription2WordList() {
        super();
    }

    /**
     * Exports the unique words or annotations from a selection of tiers and
     * from a single transcription. <p>
     * When an empty string is passed as the {@code delimiters} parameter, 
     * embedded in the encoder settings, unique annotations are exported (the
     * annotations are not split into tokens first).
     * 
     * @param transcription the transcription to export, not null
     * @param encoderInfo the encoder info object containing export settings,
     * not null
     * @param tierOrder ignored, the encoder object should contain a list of 
     * tier names
     * @param path ignored if the encoder info contains an export file
     * 
     * @throws IOException if any IO related error occurs
     */
    @Override
	public void encodeAndSave(Transcription transcription, EncoderInfo encoderInfo, List<TierImpl> tierOrder,
			String path) throws IOException {
		if (transcription == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error while exporting the list: transcription is null");
			}
			throw new IllegalArgumentException("The transcription is null");
		}
		
		if (!(encoderInfo instanceof WordListEncoderInfo)) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error while exporting the list: the encoder information is null or of the wrong type");
			}
			throw new IllegalArgumentException("The encoder settings object is null or of the wrong type");
		}
		
		exportWords((TranscriptionImpl) transcription, (WordListEncoderInfo) encoderInfo);
	}
    
    /**
     * Exports the unique words or annotations from a selection of tiers from
     * multiple EAF files. <p>
     * When an empty string is passed as the {@code delimiters} parameter, 
     * embedded in the encoder settings, unique annotations are exported (the
     * annotations are not split into tokens first).
     * 
     * @param files a list of EAF files to export
     * @param encoderInfo the encoder information object containing export
     * settings
     * 
     * @throws IOException any IO related error
     */
	public void encodeAndSave(List<File> files, EncoderInfo encoderInfo) throws IOException {
		if (files == null || files.size() == 0) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error while exporting the list: the list of files is null or empty");
			}
			throw new IllegalArgumentException("The list of files is null or empty");
		}
		
		if (!(encoderInfo instanceof WordListEncoderInfo)) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error while exporting the list: the encoder information is null or of the wrong type");
			}
			throw new IllegalArgumentException("The encoder settings object is null or of the wrong type");
		}
		
		exportWords(files, (WordListEncoderInfo) encoderInfo);
	}
	
	/**
     * Initiates the detection and counting of unique words or annotations and
     * starts the writing of the result file.
     * 
	 * @param transcription the transcription containing the annotations to 
	 * export, verified to not be {@code null} 
	 * @param encoderInfo the collection of configuration settings, verified to
	 * not be {@code null} 
	 * 
	 * @throws IOException if no destination file has been passed or when writing to the
     *         file fails
	 */
    private void exportWords(TranscriptionImpl transcription, WordListEncoderInfo encoderInfo)
            throws IOException {
    	if (encoderInfo.getExportFile() == null) {
    		if (LOG.isLoggable(Level.WARNING)) {
    			LOG.warning("No destination file specified for export");
    		}
            throw new IOException("No destination file specified for export");
    	}
    	
    	if (encoderInfo.getDelimiters() != null) {
    		this.delimiters = encoderInfo.getDelimiters();
    	} // otherwise the default delimiters are used

        if (encoderInfo.getSelectedTiers() == null) {
        	if (LOG.isLoggable(Level.WARNING)) {
        		LOG.log(Level.WARNING, "No tiers specified for the wordlist: using all tiers");
        	}
        }
        
        // now always count words and always use the map based approach
        fileCount = 1;
        Map<String, MutableInt> uniqueWords = new TreeMap<String, MutableInt>();
        addUniqueWordsAndCount(uniqueWords, transcription, encoderInfo.getSelectedTiers());
        
        writeResults(encoderInfo, uniqueWords);

        createReport(!this.delimiters.isEmpty());
    }
    
	/**
     * Initiates the detection and counting of unique words or annotations from
     * a list of files and starts the writing of the result file.
     * 
	 * @param files a list of EAF files containing the annotations to 
	 * export, verified to not be {@code null} and not empty
	 * @param encoderInfo the collection of configuration settings, verified to
	 * not be {@code null} 
	 * 
	 * @throws IOException if no destination file has been passed or when writing to the
     *         file fails
	 */
    private void exportWords(List<File> files, WordListEncoderInfo encoderInfo)
            throws IOException {
    	if (encoderInfo.getExportFile() == null) {
    		if (LOG.isLoggable(Level.WARNING)) {
    			LOG.warning("No destination file specified for export");
    		}
            throw new IOException("No destination file specified for export");
    	}
    	
    	if (encoderInfo.getDelimiters() != null) {
    		this.delimiters = encoderInfo.getDelimiters();
    	} // otherwise the default delimiters are used

        if (encoderInfo.getSelectedTiers() == null) {
        	if (LOG.isLoggable(Level.WARNING)) {
        		LOG.log(Level.WARNING, "No tiers specified for the wordlist: using all tiers");
        	}
        }
        
        Map<String, MutableInt> uniqueWords = new TreeMap<String, MutableInt>();

        for (int i = 0; i < files.size(); i++) {
        	File file = files.get(i);

            if (file == null) {
                continue;
            }

            try {
            	TranscriptionImpl trans = new TranscriptionImpl(file.getAbsolutePath());
                addUniqueWordsAndCount(uniqueWords, trans, encoderInfo.getSelectedTiers());

            } catch (Exception ex) {
                // catch any exception that could occur and continue
            	if (LOG.isLoggable(Level.SEVERE)) {
            		LOG.log(Level.SEVERE, "Could not handle file: " + file.getAbsolutePath());
            		LOG.log(Level.SEVERE, ex.getMessage());
            	}
            }
            fileCount++;
        }

        writeResults(encoderInfo, uniqueWords);

        createReport(!this.delimiters.isEmpty());
    }
    
    /**
     * After collection of words or annotations in a map, including their
     * counts, the results are written to file here.
     * 
     * @param encoderInfo the output settings object
     * @param uniqueWords the map with the words as the keys and their number
     * of occurrence as values
     * 
     * @throws IOException any IO related exception that can occur during 
     * writing
     */
    private void writeResults(WordListEncoderInfo encoderInfo, Map<String, MutableInt> uniqueWords) 
    		throws IOException {
    	uniqueCount = uniqueWords.size();
        // write the words
        FileOutputStream out = new FileOutputStream(encoderInfo.getExportFile());
        OutputStreamWriter osw = null;

        try {
        	String encoding = encoderInfo.getEncoding() != null ? 
        			encoderInfo.getEncoding() : "UTF-8";
            osw = new OutputStreamWriter(out, encoding);
        } catch (UnsupportedCharsetException uce) {
            osw = new OutputStreamWriter(out, "UTF-8");
        }

        BufferedWriter writer = new BufferedWriter(osw);

        // Use the fact that a TreeMap is ordered
        for (Map.Entry<String, MutableInt> e : uniqueWords.entrySet()) {
        	String key = e.getKey();
        	writer.write(key);
        	
        	if (encoderInfo.isCountOccurrences()) {
            	MutableInt val = e.getValue();
	        	writer.write("\t" + val.intValue);
	        	
	        	if (encoderInfo.isIncludeFreqPercent()) {
	        		writer.write(String.format("\t%4.3f", 100 * (val.intValue / (float) wordCount)));
	        	}
        	}
        	
            writer.write(NEWLINE);
        }

        if (encoderInfo.isIncludeCounts()) {
        	writer.write(NEWLINE);
        	writer.write("Statistics:");
        	writer.write(NEWLINE);
        	writer.write(String.format("Files:\t%d", fileCount));
        	writer.write(NEWLINE);
        	writer.write(String.format("Tiers:\t%d", tierCount));
        	writer.write(NEWLINE);
        	writer.write(String.format("Annotations:\t%d", annCount));
        	writer.write(NEWLINE);
    		if (encoderInfo.getExportMode() == WordListEncoderInfo.WORDS) {
    			writer.write(String.format("Unique Words:\t%d", uniqueCount));
    			writer.write(NEWLINE);
    			writer.write(String.format("Total Words:\t%d", wordCount));
    		} else {
    			writer.write(String.format("Unique Annotation Values:\t%d", uniqueCount));
    			writer.write(NEWLINE);
    			writer.write(String.format("Total Annotation Values:\t%d", wordCount));
    		}
        }
        
        try {
        	writer.close();
        } catch (Throwable t) {}
    }
    
    /**
     * Adds the (unique) words in the specified tiers from the specified
     * transcription to the specified map and updates their frequency.
     *
     * @param uniqueWords a map containing word - frequency pairs
     * @param transcription the transcription
     * @param tierNames the tiers
     */
    private void addUniqueWordsAndCount(Map<String, MutableInt> uniqueWords,
    		TranscriptionImpl transcription, List<String> tierNames) {
    	
        if (transcription == null) {
        	if (LOG.isLoggable(Level.SEVERE)) {
        		LOG.severe("The transcription to extract words from is null");
        	}
            return;
        }

        List<? extends Tier> tierList;
        
        if (tierNames != null) {
        	tierList = transcription.getTiersWithIds(tierNames);
        } else {
        	tierList = transcription.getTiers();
        }
        
        for (Tier t : tierList) {
            if (t != null) {
            	for (Annotation ann : t.getAnnotations()) {
                    if (ann != null) {
                        final String value = ann.getValue();
						if (value.length() > 0) {
                            if (delimiters.length() > 0) {
                            	StringTokenizer tokenizer = new StringTokenizer(value,
                                        delimiters);

                                while (tokenizer.hasMoreTokens()) {
                                    String token = tokenizer.nextToken();

                                    if (!uniqueWords.containsKey(token)) {
                                    	uniqueWords.put(token, new MutableInt(1));
                                    } else {
                                    	uniqueWords.get(token).intValue++;
                                    }
                                    wordCount++;
                                }
                            } else {
                                if (!uniqueWords.containsKey(value)) {
                                    uniqueWords.put(value, new MutableInt(1));
                                } else {
                                	uniqueWords.get(value).intValue++;
                                }
                                wordCount++;// non-empty annotation count 
                            }
                        }
						annCount++;
                    } else {
                    	if (LOG.isLoggable(Level.WARNING)) {
                    		LOG.log(Level.WARNING, "Annotation is null");
                    	}
                    }
                }
            }
            tierCount++;
        }
    }
    
    /**
     * Adds several count results to the report, if present.
     * 
     * @param wordMode if {@code true}, unique words are exported, otherwise
     * unique annotations
     */
    private void createReport(boolean wordMode) {
    	if (report != null) {
    		report.append("Statistics:\n");
    		report.append(String.format("Files:\t%d", fileCount));
    		report.append(String.format("Tiers:\t%d", tierCount));
    		report.append(String.format("Annotations:\t%d", annCount));
    		if (wordMode) {
    			report.append(String.format("Unique Words:\t%d", uniqueCount));
    			report.append(String.format("Total Words:\t%d", wordCount));
    		} else {
    			report.append(String.format("Unique Annotation Values:\t%d", uniqueCount));
    			report.append(String.format("Total Annotation Values:\t%d", wordCount));
    		}
    	}
    }

	@Override
	public void setProcessReport(ProcessReport report) {
		this.report = report;
	}

	@Override
	public ProcessReport getProcessReport() {
		return report;
	}

	@Override
	public void report(String message) {
		if (report != null) {
			report.append(message);
		}		
	}
}
