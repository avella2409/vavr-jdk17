package io.vavr.control;

import java.io.Serializable;

/**
 * Some represents a defined {@link Option}. It contains a value which may be null. However, to
 * create an Option containing null, {@code new Some(null)} has to be called. In all other cases
 * {@link Option#of(Object)} is sufficient.
 *
 * @param <T> The type of the optional value.
 */
public record Some<T>(T value) implements Option<T>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String stringPrefix() {
        return "Some";
    }
}
