package io.vavr.control;

public interface TryExtractor {
    <T> T value(Try<T> t);
}