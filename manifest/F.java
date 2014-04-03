package com.redriverstorm.how.manifest;

import java.util.Collections;
import java.util.Iterator;

// Taken from Play! framework v2.0.4 source code
public class F {

    /**
     * A Callback with no arguments.
     */
    public static interface Callback0 {
        public void invoke() throws Throwable;
    }

    /**
     * A Callback with a single argument.
     */
    public static interface Callback<A> {
        public void invoke(A a) throws Throwable;
    }

    /**
     * A Callback with 2 arguments.
     */
    public static interface Callback2<A,B> {
        public void invoke(A a, B b) throws Throwable;
    }

    /**
     * A Callback with 3 arguments.
     */
    public static interface Callback3<A,B,C> {
        public void invoke(A a, B b, C c) throws Throwable;
    }
    
    /**
     * A Function with no arguments.
     */
    public static interface Function0<R> {
        public R apply() throws Throwable;
    }

    /**
     * A Function with a single argument.
     */
    public static interface Function<A,R> {
        public R apply(A a) throws Throwable;
    }

    /**
     * A Function with 2 arguments.
     */
    public static interface Function2<A,B,R> {
        public R apply(A a, B b) throws Throwable;
    }

    /**
     * A Function with 3 arguments.
     */
    public static interface Function3<A,B,C,R> {
        public R apply(A a, B b, C c) throws Throwable;
    }

    /**
     * Represents optional values. Instances of <code>Option</code> are either an instance of <code>Some</code> or the object <code>None</code>.
     */
    public static abstract class Option<T> implements Iterable<T> {

        /**
         * Is the value of this option defined?
         *
         * @return <code>true</code> if the value is defined, otherwise <code>false</code>.
         */
        public abstract boolean isDefined();

        /**
         * Returns the value if defined.
         *
         * @return The value if defined, otherwise <code>null</code>.
         */
        public abstract T get();

        /**
         * Constructs a <code>None</code> value.
         *
         * @return None
         */
        public static <T> None<T> None() {
            return new None<T>();
        }

        /**
         * Construct a <code>Some</code> value.
         *
         * @param value The value to make optional
         * @return Some <code>T</code>.
         */
        public static <T> Some<T> Some(T value) {
            return new Some<T>(value);
        }

        /**
         * Get the value if defined, otherwise return the supplied <code>defaultValue</code>.
         *
         * @param defaultValue The value to return if the value of this option is not defined
         * @return The value of this option, or <code>defaultValue</code>.
         */
        public T getOrElse(T defaultValue) {
            if(isDefined()) {
                return get();
            } else {
                return defaultValue;
            }
        }

        /**
         * Map this option to another value.
         *
         * @param function The function to map the option using.
         * @return The mapped option.
         * @throws RuntimeException if <code>function</code> threw an Exception.  If the exception is a
         *      <code>RuntimeException</code>, it will be rethrown as is, otherwise it will be wrapped in a
         *      <code>RuntimeException</code>.
         */
        public <A> Option<A> map(Function<T,A> function) {
            if(isDefined()) {
                try {
                    return Some(function.apply(get()));
                } catch (RuntimeException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            } else {
                return None();
            }
        }
        
    }

    /**
     * Construct a <code>Some</code> value.
     *
     * @param value The value
     * @return Some value.
     */
    public static <A> Some<A> Some(A value) {
        return new Some<A>(value);
    }

    /**
     * Constructs a <code>None</code> value.
     *
     * @return None.
     */
    public static None None() {
        return new None();
    }

    /**
     * Represents non-existent values.
     */
    public static class None<T> extends Option<T> {

        @Override
        public boolean isDefined() {
            return false;
        }

        @Override
        public T get() {
            throw new IllegalStateException("No value");
        }

        public Iterator<T> iterator() {
            return Collections.<T>emptyList().iterator();
        }

        @Override
        public String toString() {
            return "None";
        }
    }

    /**
     * Represents existing values of type <code>T</code>.
     */
    public static class Some<T> extends Option<T> {

        final T value;

        public Some(T value) {
            this.value = value;
        }

        @Override
        public boolean isDefined() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        public Iterator<T> iterator() {
            return Collections.singletonList(value).iterator();
        }

        @Override
        public String toString() {
            return "Some(" + value + ")";
        }
    }

    /**
     * Represents a value of one of two possible types (a disjoint union)
     */
    public static class Either<A, B> {

        /**
         * The left value.
         */
        final public Option<A> left;

        /**
         * The right value.
         */
        final public Option<B> right;

        private Either(Option<A> left, Option<B> right) {
            this.left = left;
            this.right = right;
        }

        /**
         * Constructs a left side of the disjoint union, as opposed to the Right side.
         *
         * @param value The value of the left side
         * @return A left sided disjoint union
         */
        public static <A, B> Either<A, B> Left(A value) {
            return new Either<A, B>(Some(value), None());
        }

        /**
         * Constructs a right side of the disjoint union, as opposed to the Left side.
         *
         * @param value The value of the right side
         * @return A right sided disjoint union
         */
        public static <A, B> Either<A, B> Right(B value) {
            return new Either<A, B>(None(), Some(value));
        }

        @Override
        public String toString() {
            return "Either(left: " + left + ", right: " + right + ")";
        }
    }

    /**
     * A pair - a tuple of the types <code>A</code> and <code>B</code>.
     */
    public static class Tuple<A, B> {

        final public A _1;
        final public B _2;

        public Tuple(A _1, B _2) {
            this._1 = _1;
            this._2 = _2;
        }

        @Override
        public String toString() {
            return "Tuple2(_1: " + _1 + ", _2: " + _2 + ")";
        }
    }

    /**
     * Constructs a tuple of A,B
     *
     * @param a The a value
     * @param b The b value
     * @return The tuple
     */
    public static <A, B> Tuple<A, B> Tuple(A a, B b) {
        return new Tuple<A, B>(a, b);
    }

    /**
     * A tuple of A,B,C
     */
    public static class Tuple3<A, B, C> {

        final public A _1;
        final public B _2;
        final public C _3;

        public Tuple3(A _1, B _2, C _3) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
        }

        @Override
        public String toString() {
            return "Tuple3(_1: " + _1 + ", _2: " + _2 + ", _3:" + _3 + ")";
        }
    }

    /**
     * Constructs a tuple of A,B,C
     *
     * @param a The a value
     * @param b The b value
     * @param c The c value
     * @return The tuple
     */
    public static <A, B, C> Tuple3<A, B, C> Tuple3(A a, B b, C c) {
        return new Tuple3<A, B, C>(a, b, c);
    }

    /**
     * A tuple of A,B,C,D
     */
    public static class Tuple4<A, B, C, D> {

        final public A _1;
        final public B _2;
        final public C _3;
        final public D _4;

        public Tuple4(A _1, B _2, C _3, D _4) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
        }

        @Override
        public String toString() {
            return "Tuple4(_1: " + _1 + ", _2: " + _2 + ", _3:" + _3 + ", _4:" + _4 + ")";
        }
    }

    /**
     * Constructs a tuple of A,B,C,D
     *
     * @param a The a value
     * @param b The b value
     * @param c The c value
     * @param d The d value
     * @return The tuple
     */
    public static <A, B, C, D> Tuple4<A, B, C, D> Tuple4(A a, B b, C c, D d) {
        return new Tuple4<A, B, C, D>(a, b, c, d);
    }

    /**
     * A tuple of A,B,C,D,E
     */
    public static class Tuple5<A, B, C, D, E> {

        final public A _1;
        final public B _2;
        final public C _3;
        final public D _4;
        final public E _5;

        public Tuple5(A _1, B _2, C _3, D _4, E _5) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
            this._5 = _5;
        }

        @Override
        public String toString() {
            return "Tuple5(_1: " + _1 + ", _2: " + _2 + ", _3:" + _3 + ", _4:" + _4 + ", _5:" + _5 + ")";
        }
    }

    /**
     * Constructs a tuple of A,B,C,D,E
     *
     * @param a The a value
     * @param b The b value
     * @param c The c value
     * @param d The d value
     * @param e The e value
     * @return The tuple
     */
    public static <A, B, C, D, E> Tuple5<A, B, C, D, E> Tuple5(A a, B b, C c, D d, E e) {
        return new Tuple5<A, B, C, D, E>(a, b, c, d, e);
    }

}
