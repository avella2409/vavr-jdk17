package io.vavr.control;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static io.vavr.API.*;
import static org.junit.Assert.*;

public class DirectStyleTest {

    @Test
    public void directStyleOption() {
        assertEquals(
                Some("v1v2v3"),
                option($ -> {
                    var v1 = Some("v1").value($);
                    var v2 = Some("v2").value($);
                    var v3 = Some("v3").value($);
                    return v1 + v2 + v3;
                })
        );
    }

    @Test
    public void shortCircuitOnNone() {
        var processedV3 = new AtomicBoolean(false);
        assertEquals(
                None(),
                option($ -> {
                    var v1 = Some("v1").value($);
                    var v2 = None().value($);
                    processedV3.set(true);
                    var v3 = Some("v3").value($);
                    return v1 + v2 + v3;
                })
        );
        assertFalse(processedV3.get());
    }

    @Test
    public void directStyleEither() {
        assertEquals(
                Right("v1v2v3"),
                either($ -> {
                    var v1 = Right("v1").value($);
                    var v2 = Right("v2").value($);
                    var v3 = Right("v3").value($);
                    return v1 + v2 + v3;
                })
        );
    }

    @Test
    public void shortCircuitOnLeft() {
        var processedV3 = new AtomicBoolean(false);
        assertEquals(
                Left("Short Circuit"),
                // Most of the time specifying the type of the EitherExtractor will not be necessary
                // it will be directly inferred from the function return type
                either((EitherExtractor<String> $) -> {
                    var v1 = Either.<String, String>right("v1").value($);
                    var v2 = Either.<String, String>left("Short Circuit").value($);
                    processedV3.set(true);
                    var v3 = Either.<String, String>right("v3").value($);

                    return v1 + v2 + v3;
                })
        );
        assertFalse(processedV3.get());
    }

    @Test
    public void combineEitherAndOption() {
        assertEquals(
                Right(Some("v1v2v3")),
                either(eth ->
                        option(opt -> {
                            var v1 = Right("v1").value(eth);
                            var v2 = Some("v2").value(opt);
                            var v3 = Right("v3").value(eth);
                            return v1 + v2 + v3;
                        }))
        );
    }

    @Test
    public void combineEitherAndOptionWithOptionFailure() {
        assertEquals(
                Right(None()),
                either(eth ->
                        option(opt -> {
                            var v1 = Right("v1").value(eth);
                            var v2 = None().value(opt);
                            var v3 = Right("v3").value(eth);
                            return v1 + v2 + v3;
                        }))
        );
    }

    @Test
    public void combineEitherAndOptionWithEitherFailure() {
        assertEquals(
                Left("Error"),
                either((EitherExtractor<String> eth) ->
                        option(opt -> {
                            var v1 = Either.<String, String>right("v1").value(eth);
                            var v2 = Some("v2").value(opt);
                            var v3 = Either.<String, String>left("Error").value(eth);
                            return v1 + v2 + v3;
                        }))
        );
    }

    @Test
    public void directStyleTry() {
        assertEquals(
                Success("v1v2v3"),
                tryFrom($ -> {
                    var v1 = Success("v1").value($);
                    var v2 = Success("v2").value($);
                    var v3 = Success("v3").value($);
                    return v1 + v2 + v3;
                })
        );
    }

    @Test
    public void shortCircuitOnFailure() {
        var processedV3 = new AtomicBoolean(false);
        var t = tryFrom($ -> {
            var v1 = Success("v1").value($);
            var v2 = Failure(new RuntimeException("Short Circuit")).value($);
            processedV3.set(true);
            var v3 = Success("v3").value($);
            return v1 + v2 + v3;
        });

        assertTrue(t.isFailure());
        assertEquals("Short Circuit", t.getCause().getMessage());
        assertFalse(processedV3.get());
    }

    @Test
    public void directStyleValidation() {
        assertEquals(
                Valid("v1v2v3"),
                validation($ -> {
                    var v1 = Valid("v1").value($);
                    var v2 = Valid("v2").value($);
                    var v3 = Valid("v3").value($);
                    return v1 + v2 + v3;
                })
        );
    }

    @Test
    public void shortCircuitOnInvalid() {
        var processedV3 = new AtomicBoolean(false);
        assertEquals(
                Invalid("Short Circuit"),
                validation((ValidationExtractor<String> $) -> {
                    var v1 = Validation.<String, String>valid("v1").value($);
                    var v2 = Validation.<String, String>invalid("Short Circuit").value($);
                    processedV3.set(true);
                    var v3 = Validation.<String, String>valid("v3").value($);

                    return v1 + v2 + v3;
                })
        );
        assertFalse(processedV3.get());
    }
}
