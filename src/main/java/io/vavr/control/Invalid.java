package io.vavr.control;


import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * An invalid Validation
 *
 * @param <E> type of the error of this Validation
 * @param <T> type of the value of this Validation
 */
public record Invalid<E, T>(E error) implements Validation<E, T>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean isInvalid() {
        return true;
    }

    @Override
    public T get() throws RuntimeException {
        throw new NoSuchElementException("get of 'invalid' Validation");
    }

    @Override
    public E getError() {
        return error;
    }

    @Override
    public String stringPrefix() {
        return "Invalid";
    }

}