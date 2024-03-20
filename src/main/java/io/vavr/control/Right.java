package io.vavr.control;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * The {@code Right} version of an {@code Either}.
 *
 * @param <L> left component type
 * @param <R> right component type
 */
public record Right<L, R>(R value) implements Either<L, R>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public R get() {
        return value;
    }

    @Override
    public L getLeft() {
        throw new NoSuchElementException("getLeft() on Right");
    }

    @Override
    public boolean isLeft() {
        return false;
    }

    @Override
    public boolean isRight() {
        return true;
    }

    @Override
    public String stringPrefix() {
        return "Right";
    }
}