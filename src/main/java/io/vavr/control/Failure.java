package io.vavr.control;

import java.io.Serializable;
import java.util.Objects;

import static io.vavr.control.TryModule.isFatal;
import static io.vavr.control.TryModule.sneakyThrow;

/**
 * A failed Try.
 *
 * @param <T> component type of this Failure
 */
public record Failure<T>(Throwable cause) implements Try<T>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a Failure.
     *
     * @param cause A cause of type Throwable, may not be null.
     * @throws NullPointerException if {@code cause} is null
     * @throws Throwable            if the given {@code cause} is fatal, i.e. non-recoverable
     */
    public Failure {
        Objects.requireNonNull(cause, "cause is null");
        if (isFatal(cause)) {
            sneakyThrow(cause);
        }
    }

    @Override
    public T get() {
        return sneakyThrow(cause);
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public String stringPrefix() {
        return "Failure";
    }
}