/* ____  ______________  ________________________  __________
 * \   \/   /      \   \/   /   __/   /      \   \/   /      \
 *  \______/___/\___\______/___/_____/___/\___\______/___/\___\
 *
 * The MIT License (MIT)
 *
 * Copyright 2023 Vavr, https://vavr.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.vavr.control;

import io.vavr.Boundary;
import io.vavr.Label;
import io.vavr.collection.Iterator;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Either represents a value of two possible types. An Either is either a {@link Left} or a
 * {@link Right}.
 * <p>
 * If the given Either is a Right and projected to a Left, the Left operations have no effect on the Right value.<br>
 * If the given Either is a Left and projected to a Right, the Right operations have no effect on the Left value.<br>
 * If a Left is projected to a Left or a Right is projected to a Right, the operations have an effect.
 * <p>
 * <strong>Example:</strong> A compute() function, which results either in an Integer value (in the case of success) or
 * in an error message of type String (in the case of failure). By convention the success case is Right and the failure
 * is Left.
 *
 * <pre>
 * <code>
 * Either&lt;String,Integer&gt; value = compute().right().map(i -&gt; i * 2).toEither();
 * </code>
 * </pre>
 * <p>
 * If the result of compute() is Right(1), the value is Right(2).<br>
 * If the result of compute() is Left("error"), the value is Left("error").
 *
 * @param <L> The type of the Left value of an Either.
 * @param <R> The type of the Right value of an Either.
 */
@SuppressWarnings("deprecation")
public sealed interface Either<L, R> extends Iterable<R>, io.vavr.Value<R>, Serializable permits Right, Left {

    static <L, R> Either<L, R> from(Function<EitherExtractor<L>, R> body) {
        return Boundary.apply(label -> right(body.apply(new LabelEitherExtractor<>(label))));
    }

    default R value(EitherExtractor<L> $) {
        return $.value(this);
    }

    /**
     * Constructs a {@link Right}
     *
     * <pre>{@code
     * // Creates Either instance initiated with right value 1
     * Either<?, Integer> either = Either.right(1);
     * }</pre>
     *
     * @param right The value.
     * @param <L>   Type of left value.
     * @param <R>   Type of right value.
     * @return A new {@code Right} instance.
     */
    static <L, R> Either<L, R> right(R right) {
        return new Right<>(right);
    }

    /**
     * Constructs a {@link Left}
     *
     * <pre>{@code
     * // Creates Either instance initiated with left value "error message"
     * Either<String, ?> either = Either.left("error message");
     * }</pre>
     *
     * @param left The value.
     * @param <L>  Type of left value.
     * @param <R>  Type of right value.
     * @return A new {@code Left} instance.
     */
    static <L, R> Either<L, R> left(L left) {
        return new Left<>(left);
    }

    /**
     * Narrows a widened {@code Either<? extends L, ? extends R>} to {@code Either<L, R>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * <pre>{@code
     * // It's ok, Integer inherits from Number
     * Either<?, Number> answer = Either.right(42);
     *
     * // RuntimeException is an Exception
     * Either<Exception, ?> failed = Either.left(new RuntimeException("Vogon poetry recital"));
     * }</pre>
     *
     * @param either A {@code Either}.
     * @param <L>    Type of left value.
     * @param <R>    Type of right value.
     * @return the given {@code either} instance as narrowed type {@code Either<L, R>}.
     */
    @SuppressWarnings("unchecked")
    static <L, R> Either<L, R> narrow(Either<? extends L, ? extends R> either) {
        return (Either<L, R>) either;
    }

    /**
     * Returns the left value.
     *
     * <pre>{@code
     * // prints "error"
     * System.out.println(Either.left("error").getLeft());
     *
     * // throws NoSuchElementException
     * System.out.println(Either.right(42).getLeft());
     * }</pre>
     *
     * @return The left value.
     * @throws NoSuchElementException if this is a {@code Right}.
     */
    L getLeft();

    /**
     * Returns whether this Either is a Left.
     *
     * <pre>{@code
     * // prints "true"
     * System.out.println(Either.left("error").isLeft());
     *
     * // prints "false"
     * System.out.println(Either.right(42).isLeft());
     * }</pre>
     *
     * @return true, if this is a Left, false otherwise
     */
    boolean isLeft();

    /**
     * Returns whether this Either is a Right.
     *
     * <pre>{@code
     * // prints "true"
     * System.out.println(Either.right(42).isRight());
     *
     * // prints "false"
     * System.out.println(Either.left("error").isRight());
     * }</pre>
     *
     * @return true, if this is a Right, false otherwise
     */
    boolean isRight();

    /**
     * Maps either the left or the right side of this disjunction.
     *
     * <pre>{@code
     * Either<?, AtomicInteger> success = Either.right(new AtomicInteger(42));
     *
     * // prints "Right(42)"
     * System.out.println(success.bimap(Function1.identity(), AtomicInteger::get));
     *
     * Either<Exception, ?> failure = Either.left(new Exception("error"));
     *
     * // prints "Left(error)"
     * System.out.println(failure.bimap(Exception::getMessage, Function1.identity()));
     * }</pre>
     *
     * @param leftMapper  maps the left value if this is a Left
     * @param rightMapper maps the right value if this is a Right
     * @param <X>         The new left type of the resulting Either
     * @param <Y>         The new right type of the resulting Either
     * @return A new Either instance
     */
    default <X, Y> Either<X, Y> bimap(Function<? super L, ? extends X> leftMapper, Function<? super R, ? extends Y> rightMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        Objects.requireNonNull(rightMapper, "rightMapper is null");
        if (isRight()) {
            return new Right<>(rightMapper.apply(get()));
        } else {
            return new Left<>(leftMapper.apply(getLeft()));
        }
    }

    /**
     * Folds either the left or the right side of this disjunction.
     *
     * <pre>{@code
     * Either<Exception, Integer> success = Either.right(3);
     *
     * // prints "Users updated: 3"
     * System.out.println(success.fold(Exception::getMessage, count -> "Users updated: " + count));
     *
     * Either<Exception, Integer> failure = Either.left(new Exception("Failed to update users"));
     *
     * // prints "Failed to update users"
     * System.out.println(failure.fold(Exception::getMessage, count -> "Users updated: " + count));
     * }</pre>
     *
     * @param leftMapper  maps the left value if this is a Left
     * @param rightMapper maps the right value if this is a Right
     * @param <U>         type of the folded value
     * @return A value of type U
     */
    default <U> U fold(Function<? super L, ? extends U> leftMapper, Function<? super R, ? extends U> rightMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        Objects.requireNonNull(rightMapper, "rightMapper is null");
        if (isRight()) {
            return rightMapper.apply(get());
        } else {
            return leftMapper.apply(getLeft());
        }
    }

    /**
     * Reduces many {@code Either}s into a single {@code Either} by transforming an
     * {@code Iterable<Either<L, R>>} into a {@code Either<Seq<L>, Seq<R>>}.
     * <p>
     * If any of the given {@code Either}s is a {@link Left} then {@code sequence} returns a
     * {@link Left} containing a non-empty {@link Seq} of all left values.
     * <p>
     * If none of the given {@code Either}s is a {@link Left} then {@code sequence} returns a
     * {@link Right} containing a (possibly empty) {@link Seq} of all right values.
     *
     * <pre>{@code
     * // = Right(Seq())
     * Either.sequence(List.empty())
     *
     * // = Right(Seq(1, 2))
     * Either.sequence(List.of(Either.right(1), Either.right(2)))
     *
     * // = Left(Seq("x"))
     * Either.sequence(List.of(Either.right(1), Either.left("x")))
     * }</pre>
     *
     * @param eithers An {@link Iterable} of {@code Either}s
     * @param <L>     closure of all left types of the given {@code Either}s
     * @param <R>     closure of all right types of the given {@code Either}s
     * @return An {@code Either} of a {@link Seq} of left or right values
     * @throws NullPointerException if {@code eithers} is null
     */
    @SuppressWarnings("unchecked")
    static <L, R> Either<Seq<L>, Seq<R>> sequence(Iterable<? extends Either<? extends L, ? extends R>> eithers) {
        Objects.requireNonNull(eithers, "eithers is null");
        return Iterator.ofAll((Iterable<Either<L, R>>) eithers)
                .partition(Either::isLeft)
                .apply((leftPartition, rightPartition) -> leftPartition.hasNext()
                        ? Either.left(leftPartition.map(Either::getLeft).toVector())
                        : Either.right(rightPartition.map(Either::get).toVector())
                );
    }

    /**
     * Maps the values of an iterable to a sequence of mapped values into a single {@code Either} by
     * transforming an {@code Iterable<? extends T>} into a {@code Either<Seq<U>>}.
     *
     * <pre>{@code
     * Function<Integer, Either<Exception, Double>> validatingMapper =
     *         i -> i < 0 ? Either.left(new Exception("invalid value")) :
     *                         Either.right(Math.sqrt(i));
     *
     * // prints "Right(Vector(2.0, 0.0, 3.0))"
     * System.out.println(Either.traverse(Seq(4, 0, 9), validatingMapper));
     *
     * // prints "Left(Vector(java.lang.Exception: invalid value))"
     * System.out.println(Either.traverse(Seq(4, 0, -12), validatingMapper));
     * }</pre>
     *
     * @param values An {@code Iterable} of values.
     * @param mapper A mapper of values to Eithers
     * @param <L>    The mapped left value type.
     * @param <R>    The mapped right value type.
     * @param <T>    The type of the given values.
     * @return A {@code Either} of a {@link Seq} of results.
     * @throws NullPointerException if values or f is null.
     */
    static <L, R, T> Either<Seq<L>, Seq<R>> traverse(Iterable<? extends T> values, Function<? super T, ? extends Either<? extends L, ? extends R>> mapper) {
        Objects.requireNonNull(values, "values is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return sequence(Iterator.ofAll(values).map(mapper));
    }

    /**
     * Reduces many {@code Either}s into a single {@code Either} by transforming an
     * {@code Iterable<Either<L, R>>} into a {@code Either<L, Seq<R>>}.
     * <p>
     * If any of the given {@code Either}s is a {@link Left} then {@code sequenceRight} returns a
     * {@link Left} containing the first left value (in iteration order).
     * <p>
     * If none of the given {@code Either}s is a {@link Left} then {@code sequenceRight} returns a
     * {@link Right} containing a (possibly empty) {@link Seq} of all right values.
     *
     * <pre>{@code
     * // = Right(Seq())
     * Either.sequenceRight(List.empty())
     *
     * // = Right(Seq(1, 2))
     * Either.sequenceRight(List.of(Either.right(1), Either.right(2)))
     *
     * // = Left("x1")
     * Either.sequenceRight(List.of(Either.right(1), Either.left("x1"), Either.left("x2")))
     * }</pre>
     *
     * @param eithers An {@link Iterable} of {@code Either}s
     * @param <L>     closure of all left types of the given {@code Either}s
     * @param <R>     closure of all right types of the given {@code Either}s
     * @return An {@code Either} of either a {@link Seq} of right values or the first left value, if present.
     * @throws NullPointerException if {@code eithers} is null
     */
    static <L, R> Either<L, Seq<R>> sequenceRight(Iterable<? extends Either<? extends L, ? extends R>> eithers) {
        Objects.requireNonNull(eithers, "eithers is null");
        Vector<R> rightValues = Vector.empty();
        for (Either<? extends L, ? extends R> either : eithers) {
            if (either.isRight()) {
                rightValues = rightValues.append(either.get());
            } else {
                return Either.left(either.getLeft());
            }
        }
        return Either.right(rightValues);
    }

    /**
     * Maps the values of an iterable to a sequence of mapped values into a single {@code Either} by
     * transforming an {@code Iterable<? extends T>} into a {@code Either<Seq<U>>}.
     *
     * <pre>{@code
     * Iterable<String> values = List.of("a", "b", "c");
     * // prints Right(Vector(a, b, c))
     * System.out.println(Either.traverseRight(values, Either::right));
     *
     * // prints Left(a)
     * System.out.println(Either.traverseRight(values, Either::left));
     * }</pre>
     *
     * @param values An {@code Iterable} of values.
     * @param mapper A mapper of values to Eithers
     * @param <L>    The mapped left value type.
     * @param <R>    The mapped right value type.
     * @param <T>    The type of the given values.
     * @return A {@code Either} of a {@link Seq} of results.
     * @throws NullPointerException if values or f is null.
     */
    static <L, R, T> Either<L, Seq<R>> traverseRight(Iterable<? extends T> values, Function<? super T, ? extends Either<? extends L, ? extends R>> mapper) {
        Objects.requireNonNull(values, "values is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return sequenceRight(Iterator.ofAll(values).map(mapper));
    }

    /**
     * Transforms this {@code Either}.
     *
     * <pre>{@code
     * // prints "Anwser is 42"
     * System.out.println(Either.right(42).<String> transform(e -> "Anwser is " + e.get()));
     * }</pre>
     *
     * @param f   A transformation
     * @param <U> Type of transformation result
     * @return An instance of type {@code U}
     * @throws NullPointerException if {@code f} is null
     */
    default <U> U transform(Function<? super Either<L, R>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    /**
     * Gets the Right value or an alternate value, if the projected Either is a Left.
     *
     * <pre>{@code
     * // prints "42"
     * System.out.println(Either.right(42).getOrElseGet(l -> -1));
     *
     * // prints "13"
     * System.out.println(Either.left("error message").getOrElseGet(String::length));
     * }</pre>
     *
     * @param other a function which converts a Left value to an alternative Right value
     * @return the right value, if the underlying Either is a Right or else the alternative Right value provided by
     * {@code other} by applying the Left value.
     */
    default R getOrElseGet(Function<? super L, ? extends R> other) {
        Objects.requireNonNull(other, "other is null");
        if (isRight()) {
            return get();
        } else {
            return other.apply(getLeft());
        }
    }

    /**
     * Runs an action in the case this is a projection on a Left value.
     *
     * <pre>{@code
     * // prints "no value found"
     * Either.left("no value found").orElseRun(System.out::println);
     * }</pre>
     *
     * @param action an action which consumes a Left value
     */
    default void orElseRun(Consumer<? super L> action) {
        Objects.requireNonNull(action, "action is null");
        if (isLeft()) {
            action.accept(getLeft());
        }
    }

    /**
     * Gets the Right value or throws, if the projected Either is a Left.
     *
     * <pre>{@code
     * Function<String, RuntimeException> exceptionFunction = RuntimeException::new;
     * // prints "42"
     * System.out.println(Either.<String, Integer>right(42).getOrElseThrow(exceptionFunction));
     *
     * // throws RuntimeException("no value found")
     * Either.left("no value found").getOrElseThrow(exceptionFunction);
     * }</pre>
     *
     * @param <X>               a throwable type
     * @param exceptionFunction a function which creates an exception based on a Left value
     * @return the right value, if the underlying Either is a Right or else throws the exception provided by
     * {@code exceptionFunction} by applying the Left value.
     * @throws X if the projected Either is a Left
     */
    default <X extends Throwable> R getOrElseThrow(Function<? super L, X> exceptionFunction) throws X {
        Objects.requireNonNull(exceptionFunction, "exceptionFunction is null");
        if (isRight()) {
            return get();
        } else {
            throw exceptionFunction.apply(getLeft());
        }
    }

    /**
     * Converts a {@code Left} to a {@code Right} vice versa by wrapping the value in a new type.
     *
     * <pre>{@code
     * // prints "Right(42)"
     * System.out.println(Either.left(42).swap());
     *
     * // prints "Left(message)"
     * System.out.println(Either.right("message").swap());
     * }</pre>
     *
     * @return a new {@code Either}
     */
    default Either<R, L> swap() {
        if (isRight()) {
            return new Left<>(get());
        } else {
            return new Right<>(getLeft());
        }
    }

    /**
     * Calls recoveryFunction if the projected Either is a Left, performs no operation if this is a Right. This is
     * similar to {@code getOrElseGet}, but where the fallback method also returns an Either.
     *
     * <pre>{@code
     * Either<Integer, String> tryGetString() { return Either.left(1); }
     *
     * Either<Integer, String> tryGetStringAnotherWay(Integer lvalue) { return Either.right("yo " + lvalue); }
     *
     * = Right("yo 1")
     * tryGetString().recover(this::tryGetStringAnotherWay);
     * }</pre>
     *
     * @param recoveryFunction a function which accepts a Left value and returns an Either
     * @return an {@code Either<L, R>} instance
     * @throws NullPointerException if the given {@code recoveryFunction} is null
     */
    @SuppressWarnings("unchecked")
    default Either<L, R> recoverWith(Function<? super L, ? extends Either<? extends L, ? extends R>> recoveryFunction) {
        Objects.requireNonNull(recoveryFunction, "recoveryFunction is null");
        if (isLeft()) {
            return (Either<L, R>) recoveryFunction.apply(getLeft());
        } else {
            return this;
        }
    }

    /**
     * Calls {@code recoveryFunction} if the projected Either is a Left, or returns {@code this} if Right. The result
     * of {@code recoveryFunction} will be projected as a Right.
     *
     * <pre>{@code
     * Either<Integer, String> tryGetString() { return Either.left(1); }
     *
     * String getStringAnotherWay() { return "yo"; }
     *
     * = Right("yo")
     * tryGetString().recover(this::getStringAnotherWay);
     * }</pre>
     *
     * @param recoveryFunction a function which accepts a Left value and returns a Right value
     * @return an {@code Either<L, R>} instance
     * @throws NullPointerException if the given {@code recoveryFunction} is null
     */
    default Either<L, R> recover(Function<? super L, ? extends R> recoveryFunction) {
        Objects.requireNonNull(recoveryFunction, "recoveryFunction is null");
        if (isLeft()) {
            return Either.right(recoveryFunction.apply(getLeft()));
        } else {
            return this;
        }
    }

    // -- Adjusted return types of Monad methods

    /**
     * FlatMaps this right-biased Either.
     *
     * <pre>{@code
     * // prints "Right(42)"
     * System.out.println(Either.right(21).flatMap(v -> Either.right(v * 2)));
     *
     * // prints "Left(error message)"
     * System.out.println(Either.left("error message").flatMap(Either::right));
     * }</pre>
     *
     * @param mapper A mapper
     * @param <U>    Component type of the mapped right value
     * @return this as {@code Either<L, U>} if this is a Left, otherwise the right mapping result
     * @throws NullPointerException if {@code mapper} is null
     */
    @SuppressWarnings("unchecked")
    default <U> Either<L, U> flatMap(Function<? super R, ? extends Either<L, ? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isRight()) {
            return (Either<L, U>) mapper.apply(get());
        } else {
            return (Either<L, U>) this;
        }
    }

    /**
     * Maps the value of this Either if it is a Right, performs no operation if this is a Left.
     *
     * <pre>{@code
     * // = Right("A")
     * Either.right("a").map(String::toUpperCase);
     *
     * // = Left(1)
     * Either.left(1).map(String::toUpperCase);
     * }</pre>
     *
     * @param mapper A mapper
     * @param <U>    Component type of the mapped right value
     * @return a mapped {@code Monad}
     * @throws NullPointerException if {@code mapper} is null
     */
    @SuppressWarnings("unchecked")
    @Override
    default <U> Either<L, U> map(Function<? super R, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isRight()) {
            return Either.right(mapper.apply(get()));
        } else {
            return (Either<L, U>) this;
        }
    }

    /**
     * Maps the value of this Either if it is a Left, performs no operation if this is a Right.
     *
     * <pre>{@code
     * // = Left(2)
     * Either.left(1).mapLeft(i -> i + 1);
     *
     * // = Right("a")
     * Either.right("a").mapLeft(i -> i + 1);
     * }</pre>
     *
     * @param leftMapper A mapper
     * @param <U>        Component type of the mapped right value
     * @return a mapped {@code Monad}
     * @throws NullPointerException if {@code mapper} is null
     */
    @SuppressWarnings("unchecked")
    default <U> Either<U, R> mapLeft(Function<? super L, ? extends U> leftMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        if (isLeft()) {
            return Either.left(leftMapper.apply(getLeft()));
        } else {
            return (Either<U, R>) this;
        }
    }

    // -- Adjusted return types of Value methods

    /**
     * Filters this right-biased {@code Either} by testing a predicate.
     *
     *
     * @param predicate A predicate
     * @return a new {@code Option} instance
     * @throws NullPointerException if {@code predicate} is null
     */
    default Option<Either<L, R>> filter(Predicate<? super R> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return isLeft() || predicate.test(get()) ? Option.some(this) : Option.none();
    }

    /**
     * Filters this right-biased {@code Either} by testing a predicate.
     *
     * @param predicate A predicate
     * @return a new {@code Either}
     * @throws NullPointerException if {@code predicate} is null
     */
    default Option<Either<L, R>> filterNot(Predicate<? super R> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filter(predicate.negate());
    }

    /**
     * Filters this right-biased {@code Either} by testing a predicate.
     * If the {@code Either} is a {@code Right} and the predicate doesn't match, the
     * {@code Either} will be turned into a {@code Left} with contents computed by applying
     * the zero function to the {@code Either} value.
     *
     * <pre>{@code
     * // = Left("bad: a")
     * Either.right("a").filterOrElse(i -> false, val -> "bad: " + val);
     *
     * // = Right("a")
     * Either.right("a").filterOrElse(i -> true, val -> "bad: " + val);
     * }</pre>
     *
     * @param predicate A predicate
     * @param zero      A function that turns a right value into a left value if the right value does not make it through the filter.
     * @return an {@code Either} instance
     * @throws NullPointerException if {@code predicate} is null
     */
    default Either<L, R> filterOrElse(Predicate<? super R> predicate, Function<? super R, ? extends L> zero) {
        Objects.requireNonNull(predicate, "predicate is null");
        Objects.requireNonNull(zero, "zero is null");
        if (isLeft() || predicate.test(get())) {
            return this;
        } else {
            return Either.left(zero.apply(get()));
        }
    }

    /**
     * Gets the right value if this is a {@code Right} or throws if this is a {@code Left}.
     *
     * @return the right value
     * @throws NoSuchElementException if this is a {@code Left}.
     */
    @Override
    R get();

    @Override
    default boolean isEmpty() {
        return isLeft();
    }

    @SuppressWarnings("unchecked")
    default Either<L, R> orElse(Either<? extends L, ? extends R> other) {
        Objects.requireNonNull(other, "other is null");
        return isRight() ? this : (Either<L, R>) other;
    }

    @SuppressWarnings("unchecked")
    default Either<L, R> orElse(Supplier<? extends Either<? extends L, ? extends R>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return isRight() ? this : (Either<L, R>) supplier.get();
    }

    /**
     * A right-biased {@code Either}'s value is computed synchronously.
     *
     * @return false
     */
    @Override
    default boolean isAsync() {
        return false;
    }

    /**
     * A right-biased {@code Either}'s value is computed eagerly.
     *
     * @return false
     */
    @Override
    default boolean isLazy() {
        return false;
    }

    /**
     * A right-biased {@code Either} is single-valued.
     *
     * @return {@code true}
     */
    @Override
    default boolean isSingleValued() {
        return true;
    }

    @Override
    default Iterator<R> iterator() {
        if (isRight()) {
            return Iterator.of(get());
        } else {
            return Iterator.empty();
        }
    }

    /**
     * Performs the given {@code leftAction} on the left element if this is Left.
     * Performs the given {@code rightAction} on the right element if this is Right.
     *
     * @param leftAction  The action that will be performed on the left element
     * @param rightAction The action that will be performed on the right element
     * @return this instance
     */
    default Either<L, R> peek(Consumer<? super L> leftAction, Consumer<? super R> rightAction) {
        Objects.requireNonNull(leftAction, "leftAction is null");
        Objects.requireNonNull(rightAction, "rightAction is null");

        if (isLeft()) {
            leftAction.accept(getLeft());
        } else { // this isRight() by definition
            rightAction.accept(get());
        }

        return this;
    }

    @Override
    default Either<L, R> peek(Consumer<? super R> action) {
        Objects.requireNonNull(action, "action is null");
        if (isRight()) {
            action.accept(get());
        }
        return this;
    }

    default Either<L, R> peekLeft(Consumer<? super L> action) {
        Objects.requireNonNull(action, "action is null");
        if (isLeft()) {
            action.accept(getLeft());
        }
        return this;
    }

    /**
     * Returns this as {@code Validation}.
     *
     * @return {@code Validation.valid(get())} if this is right, otherwise {@code Validation.invalid(getLeft())}.
     */
    default Validation<L, R> toValidation() {
        return isRight() ? Validation.valid(get()) : Validation.invalid(getLeft());
    }

    /**
     * Transforms this {@code Either} into a {@link Try} instance.
     * <p>
     * Map this {@code left} value to a {@link Throwable} using the {@code leftMapper}
     * or return a {@link Try#success(Object) Success} of this {@code right} value.
     * </p>
     *
     * @param leftMapper A function that maps the left value of this {@code Either} to a {@link Throwable} instance
     * @return A {@link Try} instance that represents the result of the transformation
     * @throws NullPointerException if {@code leftMapper} is {@code null}
     */
    default Try<R> toTry(Function<? super L, ? extends Throwable> leftMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        return mapLeft(leftMapper).fold(Try::failure, Try::success);
    }

}

final class LabelEitherExtractor<L> implements EitherExtractor<L> {

    private final Label label;

    LabelEitherExtractor(Label label) {
        this.label = label;
    }

    @Override
    public <R> R value(Either<L, R> either) {
        if (either.isLeft()) {
            // Break flow / Short Circuit, go to boundary definition
            Boundary.breakNow(either, label);
            return null;
        } else return either.get();
    }
}
