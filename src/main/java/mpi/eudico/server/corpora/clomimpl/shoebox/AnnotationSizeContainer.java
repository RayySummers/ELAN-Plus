package mpi.eudico.server.corpora.clomimpl.shoebox;

import mpi.eudico.server.corpora.clom.Annotation;

import java.awt.Rectangle;


/**
 * An object for storing the required size of an annotation, either in pixels 
 * or in number of spaces.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class AnnotationSizeContainer implements Comparable<Object> {
	/** a constant for metrics in pixels */
    public static final int PIXELS = 1;
    /** a constant for metrics in spaces / characters */
    public static final int SPACES = 2;
    private int _size;
    private Annotation _ann;
    private int _type = 0;
    private long _stime = 0;

    /** the start time as a {@code Long} */
    public Long _lstime = null;
    private long _etime = 0;
    private Rectangle _rect = null;

    /**
     * Creates a new AnnotationSizeContainer instance
     * 
     * @param ann the annotation
     * @param size the size
     * @param st start time
     * @param et end time
     * @param type one of {@code #PIXELS} or {@code #SPACES}
     */
    public AnnotationSizeContainer(Annotation ann, Integer size, long st,
        long et, int type) {
        _ann = ann;
        _size = size.intValue();
        _type = type;
        _stime = st;
        _etime = et;
        _lstime = Long.valueOf(_stime);
    }

    /**
     * Creates a new AnnotationSizeContainer instance
     *
     * @param ann the annotation
     * @param size the size
     * @param type one of {@code #PIXELS} or {@code #SPACES}
     */
    public AnnotationSizeContainer(Annotation ann, Integer size, int type) {
        _ann = ann;
        _size = size.intValue();
        _type = type;

        if (ann != null) {
            _lstime = Long.valueOf(ann.getBeginTimeBoundary());
        }
    }

    /**
     * Creates a new AnnotationSizeContainer instance
     *
     * @param ann the annotation
     * @param size the size
     * @param type one of {@code #PIXELS} or {@code #SPACES}
     */
    public AnnotationSizeContainer(Annotation ann, int size, int type) {
        _ann = ann;
        _size = size;
        _type = type;

        if (ann != null) {
            _lstime = Long.valueOf(ann.getBeginTimeBoundary());
        }
    }

    /**
     * Sets the bounds for this container.
     * 
     * @param rect the Rectangle for this container
     */
    public void setRect(Rectangle rect) {
        _rect = rect;
    }

    /**
     * Returns the embedded annotation.
     * 
     * @return the annotation
     */
    public Annotation getAnnotation() {
        return _ann;
    }

    /**
     * Returns the size of the container.
     * 
     * @return the size
     */
    public int getSize() {
        return _size;
    }

    /**
     * Returns the metrics type of the container.
     * 
     * @return the container type
     */
    public int getType() {
        return _type;
    }

    /**
     * Returns the start time of the annotation.
     * 
     * @return the start time
     */
    public long getStartTime() {
        return _stime;
    }

    /**
     * Returns the end time of the annotation.
     * 
     * @return the end time
     */
    public long getEndTime() {
        return _etime;
    }

    // compare to interface
    @Override
	public int compareTo(Object o) {
        if (_lstime == null) {
            System.out.println("NULL STIME");

            return -1;
        }

        Long l = null;
        Long l1 = null;

        if (_ann != null) {
            l1 = Long.valueOf(_ann.getBeginTimeBoundary());
        }

        if (o instanceof Long) {
            l = (Long) o;
        } else {
            l = Long.valueOf(((AnnotationSizeContainer) o).getAnnotation()
                          .getBeginTimeBoundary());
        }

        //System.out.println(l1 + " " + l1);
        if (l1 != null && l != null) {
        	return l1.compareTo(l);
        } else {
        	return -1;
        }
    }
}
