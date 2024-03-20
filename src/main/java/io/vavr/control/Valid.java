package io.vavr.control;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * A valid Validation
 *
 * @param <E> type of the error of this Validation
 * @param <T> type of the value of this Validation
 */
public record Valid<E, T>(T value) implements Validation<E, T>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public E getError() throws RuntimeException {
        throw new NoSuchElementException("error of 'valid' Validation");
    }

    @Override
    public String stringPrefix() {
        return "Valid";
    }

}