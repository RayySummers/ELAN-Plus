package mpi.eudico.client.annotator.util;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;

/**
 * Utility class for per locale (language) export of property keys that have
 * not been translated yet.
 *  
 * @author Han Sloetjes
 */
public class LocaleDiff {
    private String outputPath;
    
    /**
     * Creates a new LocaleDiff instance with an optional output path.
     * If no output path is specified, the output is stored in the user's home
     * folder.
     * 
     * @param outputPath the output path, can be {@code null}
     */
    public LocaleDiff(String outputPath) {
        if (outputPath != null) {
        	this.outputPath = outputPath;
        } else {
        	this.outputPath = System.getProperty("user.home");
        }
    }
    
    /**
     * Performs the {@code diff} on the properties files.
     */
    public void run() {
        ResourceBundle english = ResourceBundle.getBundle(
                //"mpi.search.resources.SearchLanguage", Locale.of("", "", ""));
    			"mpi.eudico.client.annotator.resources.ElanLanguage", Locale.of("", "", ""));
        
        PropertyResourceBundle other = null;
        Collection<Locale> allLocs = ELANCommandFactory.getLocales();
        Iterator<Locale> locIt = allLocs.iterator();
        while (locIt.hasNext()) {
        	Locale loc = locIt.next();
            if (loc.equals(Locale.ENGLISH)) {
                continue;
            }
            try {
                other = (PropertyResourceBundle) ResourceBundle.getBundle(
                        //"mpi.search.resources.SearchLanguage", loc);
                		"mpi.eudico.client.annotator.resources.ElanLanguage", loc);
            } catch (Exception ee) {
                System.out.println("Could not load resource: " + ee.getMessage());
                continue;
            }
            
            Enumeration<String> engEn = english.getKeys();
            List<String> list = new ArrayList<String>();
            while (engEn.hasMoreElements()) {
            	String key = engEn.nextElement();
                
                // filter out integer mnemonics ?
                try {
                    Integer.valueOf(english.getString(key));
                    continue;
                } catch (NumberFormatException nfe) {
                    // do nothing, ok
                }
                // or all mnemonics ?
//                if (key.startsWith("MNEMONIC")) {
//                	continue;
//                }
                
                if (other.handleGetObject(key) == null) {
                    list.add((key + "=" + english.getString(key)));
                }
            }
            Collections.sort(list);
            writeProperties(loc, list);
        }
    }
    
    private void writeProperties(Locale loc, List<String> props) {
    	// write .properties file
        try (FileWriter writer = new FileWriter(new File(outputPath + File.separator +
        		getSuffix(loc) + ".properties"), StandardCharsets.UTF_8)) {
        	
            for (int i = 0; i < props.size(); i++) {
            	if (props.get(i).indexOf('\n') >= 0) {
            		writer.write(props.get(i).replaceAll("\n", "\\\\n") + "\n");
            	} else {
            		writer.write(props.get(i) + "\n");
            	}
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("Could not write .properties file: " + e.getMessage());
        }
        
        // write .csv file
        try (FileWriter cw = new FileWriter(new File(outputPath + File.separator +
                loc.getLanguage() + ".csv"), StandardCharsets.UTF_8)) {    
            cw.write("\"English\",\"Translation\"\n\n");
            
            for (int i = 0; i < props.size(); i++) {
            	String prop = props.get(i);
            	// replace " by ""           	
            	if (prop.indexOf('"') >= 0) {
            		prop = props.get(i).replaceAll("\"", "\"\"");
            	}
            	if (prop.indexOf('\n') >= 0) {
            		prop = prop.replaceAll("\n", "\\\\n");
            	} 
            	cw.write("\"");
            	cw.write(prop);
            	cw.write("\",\"");
            	cw.write(prop);
            	cw.write("\"\n");
            }
            
            cw.close();
        } catch (Exception e) {
            System.out.println("Could not write .csv file: " + e.getMessage());
        }
    }
    
    private String getSuffix(Locale loc) {
    	if (loc.getCountry() != null && !loc.getCountry().isEmpty()) {
    		return loc.getLanguage() + "_" + loc.getCountry();
    	} else {
    		return loc.getLanguage();
    	}
    }

    /**
     * Runs the utility.
     * 
     * @param args an optional output path, {@code user.home} by default
     */
    public static void main(String[] args) {
    	if (args.length > 0) {
    		new LocaleDiff(args[0]).run();
    	} else {
    		new LocaleDiff(null).run();
    	}
    }
}
