package mpi.eudico.client.annotator;

import mpi.eudico.server.corpora.clom.Annotation;

/**
 * Interface that defines methods for ActiveAnnotation users, objects that
 * need to set and get the active annotation.
 */
public interface ActiveAnnotationUser extends ActiveAnnotationListener {
    /**
     * Sets the {@code ActiveAnnotation} object.
     *
     * @param activeAnnotation the {@code ActiveAnnotation} object
     */
    public void setActiveAnnotationObject(ActiveAnnotation activeAnnotation);

    @Override
	public void updateActiveAnnotation();

    /**
     * Returns the active annotation.
     *
     * @return the active annotation, can be {@code null}
     */
    public Annotation getActiveAnnotation();

    /**
     * Sets the active annotation.
     *
     * @param annotation the new active annotation
     */
    public void setActiveAnnotation(Annotation annotation);
}
