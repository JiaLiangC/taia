package com.dtstack.taier.datasource.plugin.common.operation;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IterableFetchIterator<A> implements FetchIterator<A> {
    private final Iterable<A> iterable;
    private Iterator<A> iter;
    private long fetchStart = 0;
    private long position = 0;

    public IterableFetchIterator(Iterable<A> iterable) {
        this.iterable = iterable;
        this.iter = iterable.iterator();
    }

    @Override
    public void fetchNext() {
        fetchStart = position;
    }

    @Override
    public void fetchAbsolute(long pos) {
        long newPos = Math.max(0, pos);
        if (newPos < position) resetPosition();
        while (position < newPos && hasNext()) next();
        fetchStart = position;
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
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public A next() {
        position++;
        return iter.next();
    }

    private void resetPosition() {
        if (position != 0) {
            iter = iterable.iterator();
            position = 0;
            fetchStart = 0;
        }
    }

    @Override
    public Stream<A> take(int n) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new Iterator<A>() {
                            private int count = 0;

                            @Override
                            public boolean hasNext() {
                                return count < n && IterableFetchIterator.this.hasNext();
                            }

                            @Override
                            public A next() {
                                if (!hasNext()) {
                                    throw new NoSuchElementException();
                                }
                                count++;
                                return IterableFetchIterator.this.next();
                            }
                        },
                        Spliterator.ORDERED
                ),
                false
        );
    }
}