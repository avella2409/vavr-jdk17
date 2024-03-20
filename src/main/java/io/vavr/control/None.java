package io.vavr.control;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * None is a singleton representation of the undefined {@link Option}.
 *
 * @param <T> The type of the optional value.
 */
public record None<T>() implements Option<T>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The singleton instance of None.
     */
    static final None<?> INSTANCE = new None<>();

    @Override
    public T get() {
        throw new NoSuchElementException("No value present");
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public String stringPrefix() {
        return "None";
    }

    // -- Serializable implementation

    /**
     * Instance control for object serialization.
     *
     * @return The singleton instance of None.
     * @see Serializable
     */
    private Object readResolve() {
        return INSTANCE;
    }
}