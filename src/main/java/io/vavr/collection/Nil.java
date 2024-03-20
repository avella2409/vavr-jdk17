package io.vavr.collection;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Representation of the singleton empty {@code List}.
 *
 * @param <T> Component type of the List.
 */
public record Nil<T>() implements List<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Nil<?> INSTANCE = new Nil<>();

    /**
     * Returns the singleton instance of the linked list.
     *
     * @param <T> Component type of the List
     * @return the singleton instance of the linked list.
     */
    @SuppressWarnings("unchecked")
    static <T> Nil<T> instance() {
        return (Nil<T>) INSTANCE;
    }

    @Override
    public T head() {
        throw new NoSuchElementException("head of empty list");
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public List<T> tail() {
        throw new UnsupportedOperationException("tail of empty list");
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        return Collections.equals(this, o);
    }

    @Override
    public int hashCode() {
        return Collections.hashOrdered(this);
    }

    @Override
    public String toString() {
        return stringPrefix() + "()";
    }

    /**
     * Instance control for object serialization.
     *
     * @return The singleton instance of Nil.
     * @see java.io.Serializable
     */
    private Object readResolve() {
        return INSTANCE;
    }
}
