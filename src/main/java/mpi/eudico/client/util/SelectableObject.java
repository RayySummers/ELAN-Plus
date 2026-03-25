package mpi.eudico.client.util;

/**
 * A class to wrap an object and store a {@code selected} property for that
 * object. This can be used in tables, lists and trees. 
 * 
 * @param <T> the type of the wrapped object
 */
public class SelectableObject<T> {
    private T value;
    private boolean selected;
    
    /**
     * Creates a new SelectableObject instance.
     * The value is {@code null}, the selected state {@code false}.
     */
    public SelectableObject() {
    }
    
    /**
     * Creates a new SelectableObject instance.
     * 
     * @param value the wrapped object
     * @param selected the initial selected state of the object
     */
    public SelectableObject(T value, boolean selected) {
        this.value = value;
        this.selected = selected;
    }
    
    /**
     * If the wrapped value not is {@code null}, its {@code toString()}
     * is returned.
     */
    @Override
	public String toString() {
        if (value != null) {
            return value.toString();    
        }
        return null;
    }
    
    /**
     * Returns the selected state of this object.
     * 
     * @return {@code true} if this object is selected, {@code false} otherwise
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * Sets the selected state of this object.
     * 
     * @param selected the new selected state
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    /**
     * Returns the wrapped value.
     * 
     * @return the wrapped object
     */
    public T getValue() {
        return value;
    }
    
    /**
     * Sets the value wrapped.
     * 
     * @param value the wrapped object
     */
    public void setValue(T value) {
        this.value = value;
    }
}
