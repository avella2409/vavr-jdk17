package io.vavr.control;

public interface OptionExtractor {
    <T> T value(Option<T> option);
}