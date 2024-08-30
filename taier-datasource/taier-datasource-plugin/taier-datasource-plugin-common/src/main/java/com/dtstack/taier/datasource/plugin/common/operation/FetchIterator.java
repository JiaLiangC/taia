package com.dtstack.taier.datasource.plugin.common.operation;


import java.util.Iterator;
import java.util.stream.Stream;

public interface FetchIterator<A> extends Iterator<A> {

    /**
     * Begin a fetch block, forward from the current position.
     * Resets the fetch start offset.
     */
    void fetchNext();

    /**
     * Begin a fetch block, moving the iterator back by offset from the start of the previous fetch
     * block start.
     * Resets the fetch start offset.
     *
     * @param offset the amount to move a fetch start position toward the prior direction.
     */
    default void fetchPrior(long offset) {
        fetchAbsolute(getFetchStart() - offset);
    }

    /**
     * Begin a fetch block, moving the iterator to the given position.
     * Resets the fetch start offset.
     *
     * @param pos index to move a position of iterator.
     */
    void fetchAbsolute(long pos);

    long getFetchStart();

    long getPosition();

    Stream<A> take(int n);

}





class FetchIteratorFactory {
    public static <A> FetchIterator<A> fromIterator(Iterator<A> iter) {
        return new FetchIterator<A>() {
            private long fetchStart = 0;
            private long position = 0;

            @Override
            public void fetchNext() {
                fetchStart = position;
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public A next() {
                position++;
                return iter.next();
            }

            @Override
            public void fetchAbsolute(long pos) {
                // No implementation
            }

            @Override
            public long getFetchStart() {
                return fetchStart;
            }

            @Override
            public long getPosition() {
                return position;
            }

            @Override
            public Stream<A> take(int n) {
                return Stream.empty();
            }
        };
    }
}
