package com.dtstack.taier.datasource.plugin.common.operation;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A seekable buffer reader, if the previous file path lines do not satisfy the limit size
 * the reader will fetch line from next file path.
 *
 * Note that, this reader is always read forwards so call side should always new instance if
 * want to replay the lines which has been fetched.
 */
public class SeekableBufferedReader implements Closeable {

    private final List<BufferedReader> bufferedReaders;
    private long linePos = 0L;
    private int readerIndex = 0;
    private final int numReaders;
    private BufferedReader currentReader;
    private String currentValue;

    public SeekableBufferedReader(List<Path> paths) throws IOException {
        bufferedReaders = new ArrayList<>();
        for (Path path : paths) {
            bufferedReaders.add(Files.newBufferedReader(path, StandardCharsets.UTF_8));
        }
        numReaders = bufferedReaders.size();
        currentReader = bufferedReaders.get(0);
    }

    private void nextLine() throws IOException {
        currentValue = currentReader.readLine();
        while (currentValue == null && readerIndex < numReaders - 1) {
            readerIndex++;
            currentReader = bufferedReaders.get(readerIndex);
            currentValue = currentReader.readLine();
        }
        if (currentValue != null) {
            linePos++;
        }
    }

    /**
     * @param from include
     * @param limit exclude
     */
    public Iterator<String> readLine(long from, long limit) throws IOException {
        if (from < 0) throw new IOException("Negative seek offset");

        return new Iterator<String>() {
            private long numLines = 0L;

            @Override
            public boolean hasNext() {
                if (numLines >= limit) {
                    return false;
                }
                try {
                    nextLine();
                    while (linePos <= from && currentValue != null) {
                        nextLine();
                    }
                    numLines++;
                    return currentValue != null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return currentValue;
            }
        };
    }

    @Override
    public void close() throws IOException {
        for (BufferedReader reader : bufferedReaders) {
            reader.close();
        }
    }
}
