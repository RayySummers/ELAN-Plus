package mpi.eudico.client.annotator.interannotator;

import mpi.eudico.server.corpora.clom.AnnotationCore;

import java.util.List;

/**
 * Simple class to store some information and objects for annotator comparisons.
 */
public class CompareUnit {
    // only public members
    /**
     * filename
     */
    public String fileName;
    /**
     * tier name
     */
    public String tierName;
    /**
     * annotator
     */
    public String annotator;
    /**
     * list of annotations
     */
    public List<AnnotationCore> annotations;

    /**
     * No argument constructor
     */
    public CompareUnit() {
        super();
    }

    /**
     * Constructor
     *
     * @param fileName the file name
     * @param tierName the tier name
     * @param annotator the annotator
     */
    public CompareUnit(String fileName, String tierName, String annotator) {
        super();
        this.fileName = fileName;
        this.tierName = tierName;
        this.annotator = annotator;
    }
}
