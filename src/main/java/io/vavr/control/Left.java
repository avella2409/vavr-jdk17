package io.vavr.control;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * The {@code Left} version of an {@code Either}.
 *
 * @param <L> left component type
 * @param <R> right component type
 */
public record Left<L, R>(L value) implements Either<L, R>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public R get() {
        throw new NoSuchElementException("get() on Left");
    }

    @Override
    public L getLeft() {
        return value;
    }

    @Override
    public boolean isLeft() {
        return true;
    }

    @Override
    public boolean isRight() {
        return false;
    }

    @Override
    public String stringPrefix() {
        return "Left";
    }
}