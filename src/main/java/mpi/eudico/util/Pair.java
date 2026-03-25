package mpi.eudico.util;

/**
 * A class to combine two arbitrary objects, where it isn't worth the trouble
 * to use a specially named class.
 * 
 * @author olasei, after an example on stackoverflow.
 *
 * @param <A> first type
 * @param <B> second type
 */

public class Pair<A,B> {
	/**
	 * Utility method to create a new {@code Pair}.
	 * 
	 * @param <P> the first type
	 * @param <Q> the second type
	 * @param p an instance of the first type
	 * @param q an instance of the second type
	 * @return a new pair
	 */
    public static <P, Q> Pair<P, Q> makePair(P p, Q q) {
        return new Pair<P, Q>(p, q);
    }

    private final A a;
    private final B b;

    /**
     * Creates a new pair instance.
     * 
     * @param a first object
     * @param b second object
     */
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Returns the first object.
     * 
     * @return the first object
     */
    public A getFirst() {
    	return a;
    }
    
    /**
     * Returns the second object.
     * 
     * @return the second object
     */
    public B getSecond() {
    	return b;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((a == null) ? 0 : a.hashCode());
        result = prime * result + ((b == null) ? 0 : b.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        Pair other = (Pair) obj;
        if (a == null) {
            if (other.a != null) {
                return false;
            }
        } else if (!a.equals(other.a)) {
            return false;
        }
        if (b == null) {
            if (other.b != null) {
                return false;
            }
        } else if (!b.equals(other.b)) {
            return false;
        }
        return true;
    }

    /**
     * Tests if classes {@code A} and {@code B} are instances of the first and 
     * second type (respectively) of this pair.
     *  
     * @param classA the class of the first type
     * @param classB the class of the second type
     * 
     * @return {@code true} if both the first class is an instance of the first
     * type and the second class an instance of the second type
     */
    public boolean isInstance(Class<?> classA, Class<?> classB) {
        return classA.isInstance(a) && classB.isInstance(b);
    }

    /**
     * Casts a pair to a pair of the specified two classes, throws a 
     * {@code ClassCastException} if this fails.
     * 
     * @param <P> the first type
     * @param <Q> the second type
     * @param pair the pair to cast
     * @param pClass the class of the first part of the pair
     * @param qClass the class of the second part of the pair
     * @return the input pair casted to the requested type
     */
    @SuppressWarnings("unchecked")
    public static <P, Q> Pair<P, Q> cast(Pair<?, ?> pair, Class<P> pClass, Class<Q> qClass) {

        if (pair.isInstance(pClass, qClass)) {
            return (Pair<P, Q>) pair;
        }

        throw new ClassCastException();

    }

    @Override
    public String toString() {
    	return a.toString() + " " + b.toString();
    }
}