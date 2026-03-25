package mpi.eudico.client.annotator.util;

import java.awt.datatransfer.DataFlavor;

/**
 * A DataFlavor for AnnotationDataRecords (Singleton).
 */
public class AnnotationDataFlavor extends DataFlavor {
    private static AnnotationDataFlavor flavor = null;
    
    private AnnotationDataFlavor() throws ClassNotFoundException {
        super(DataFlavor.javaSerializedObjectMimeType + ";class=" + 
                    AnnotationDataRecord.class.getName());
    }
    
    /**
     * Singleton method
     * @return the singleton AnnotationDataFlavor instance
     */
    public static AnnotationDataFlavor getInstance() {
        if (flavor == null) {
            createFlavor();
        }
        
        return flavor;
    }
    
    private static void createFlavor() {
        try {
            flavor = new AnnotationDataFlavor();
        } catch (ClassNotFoundException cnfe) {
            ClientLogger.LOG.warning("Flavor class not found: " + cnfe.getMessage());
        }
    }
}
