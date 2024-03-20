package io.vavr.collection;

import java.io.*;

/**
 * Non-empty {@code List}, consisting of a {@code head} and a {@code tail}.
 *
 * @param <T> Component type of the List.
 */
// DEV NOTE: class declared final because of serialization proxy pattern (see Effective Java, 2nd ed., p. 315)
public record Cons<T>(T head, List<T> tail) implements List<T>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public T head() {
        return head;
    }

    @Override
    public int length() {
        List<T> these = this;
        var len = 0;
        while (!these.isEmpty()) {
            len += 1;
            these = these.tail();
        }
        return len;
    }

    @Override
    public List<T> tail() {
        return tail;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        return Collections.equals(this, o);
    }

    @Override
    public int hashCode() {
        return Collections.hashOrdered(this);
    }

    @Override
    public String toString() {
        return mkString(stringPrefix() + "(", ", ", ")");
    }

    /**
     * {@code writeReplace} method for the serialization proxy pattern.
     * <p>
     * The presence of this method causes the serialization system to emit a SerializationProxy instance instead of
     * an instance of the enclosing class.
     *
     * @return A SerializationProxy for this enclosing class.
     */
    private Object writeReplace() {
        return new SerializationProxy<>(this);
    }

    /**
     * {@code readObject} method for the serialization proxy pattern.
     * <p>
     * Guarantees that the serialization system will never generate a serialized instance of the enclosing class.
     *
     * @param stream An object serialization stream.
     * @throws java.io.InvalidObjectException This method will throw with the message "Proxy required".
     */
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    /**
     * A serialization proxy which, in this context, is used to deserialize immutable, linked Lists with final
     * instance fields.
     *
     * @param <T> The component type of the underlying list.
     */
    // DEV NOTE: The serialization proxy pattern is not compatible with non-final, i.e. extendable,
    // classes. Also, it may not be compatible with circular object graphs.
    private static final class SerializationProxy<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        // the instance to be serialized/deserialized
        private transient Cons<T> list;

        /**
         * Constructor for the case of serialization, called by {@link Cons#writeReplace()}.
         * <p/>
         * The constructor of a SerializationProxy takes an argument that concisely represents the logical state of
         * an instance of the enclosing class.
         *
         * @param list a Cons
         */
        SerializationProxy(Cons<T> list) {
            this.list = list;
        }

        /**
         * Write an object to a serialization stream.
         *
         * @param s An object serialization stream.
         * @throws java.io.IOException If an error occurs writing to the stream.
         */
        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeInt(list.length());
            for (List<T> l = list; !l.isEmpty(); l = l.tail()) {
                s.writeObject(l.head());
            }
        }

        /**
         * Read an object from a deserialization stream.
         *
         * @param s An object deserialization stream.
         * @throws ClassNotFoundException If the object's class read from the stream cannot be found.
         * @throws InvalidObjectException If the stream contains no list elements.
         * @throws IOException            If an error occurs reading from the stream.
         */
        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            final int size = s.readInt();
            if (size <= 0) {
                throw new InvalidObjectException("No elements");
            }
            List<T> temp = Nil.instance();
            for (int i = 0; i < size; i++) {
                @SuppressWarnings("unchecked") final T element = (T) s.readObject();
                temp = temp.prepend(element);
            }
            list = (Cons<T>) temp.reverse();
        }

        /**
         * {@code readResolve} method for the serialization proxy pattern.
         * <p>
         * Returns a logically equivalent instance of the enclosing class. The presence of this method causes the
         * serialization system to translate the serialization proxy back into an instance of the enclosing class
         * upon deserialization.
         *
         * @return A deserialized instance of the enclosing class.
         */
        private Object readResolve() {
            return list;
        }
    }
}