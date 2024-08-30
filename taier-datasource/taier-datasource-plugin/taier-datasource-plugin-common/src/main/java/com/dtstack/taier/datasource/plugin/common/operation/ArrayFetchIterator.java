package com.dtstack.taier.datasource.plugin.common.operation;

import java.util.Arrays;
import java.util.stream.Stream;

public  class ArrayFetchIterator<A> implements FetchIterator<A> {
    private final A[] src;
    private long fetchStart = 0;
    private long position = 0;

    public ArrayFetchIterator(A[] src) {
        this.src = src;
    }

    @Override
    public void fetchNext() {
        fetchStart = position;
    }

    @Override
    public void fetchAbsolute(long pos) {
        position = Math.max(0, Math.min(pos, src.length));
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
        return position < src.length;
    }

    @Override
    public A next() {
        position++;
        return src[(int) position - 1];
    }

    @Override
    public Stream<A> take(int n) {
        int remaining = (int) Math.min(n, src.length - position);
        Stream<A> result = Arrays.stream(src, (int) position, (int) position + remaining);
        position += remaining;
        return result;
    }

}