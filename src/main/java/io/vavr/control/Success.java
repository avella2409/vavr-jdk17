package io.vavr.control;

import java.io.Serializable;

/**
 * A succeeded Try.
 *
 * @param <T> component type of this Success
 */
public record Success<T>(T value) implements Try<T>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public T get() {
        return value;
    }

    @Override
    public Throwable getCause() {
        throw new UnsupportedOperationException("getCause on Success");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public String stringPrefix() {
        return "Success";
    }
}
