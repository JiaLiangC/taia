package com.dtstack.taier.datasource.plugin.common.operation;

import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.session.Session;
import com.dtstack.taier.datasource.plugin.common.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class OperationLog  {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);
    private final Path path;
    private BufferedWriter writer;
    private BufferedReader reader;
    private volatile boolean initialized = false;
    private final List<Path> extraPaths = new ArrayList<>();
    private final List<BufferedReader> extraReaders = new ArrayList<>();
    private int lastSeekReadPos = 0;
    private SeekableBufferedReader seekableReader;
    private final ReentrantLock lock = new ReentrantLock();

    public OperationLog(Path path) {
        this.path = path;
    }

    private BufferedReader getReader() throws IOException, TaierSQLException {
        if (reader == null) {
            try {
                reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
            } catch (IOException e) {
                handleFileNotFound(e);
            }
        }
        return reader;
    }

    public void addExtraLog(Path path) {
        lock.lock();
        try {
            try {
                extraReaders.add(Files.newBufferedReader(path, StandardCharsets.UTF_8));
                extraPaths.add(path);
                if (seekableReader != null) {
                    seekableReader.close();
                }
                seekableReader = null;
            } catch (IOException e) {
                // Ignore
            }
        } finally {
            lock.unlock();
        }
    }

    public void write(String msg) {
        lock.lock();
        try {
            try {
                if (writer == null) {
                    writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
                }
                writer.write(msg);
                writer.flush();
                initOperationLogIfNecessary();
            } catch (IOException e) {
                // TODO: better do nothing?
            }
        } finally {
            lock.unlock();
        }
    }

    private void initOperationLogIfNecessary() {
        if (!initialized) initialized = true;
    }

    private Pair<List<String>, Integer> readLogs(BufferedReader reader, int lastRows, int maxRows) throws TaierSQLException {
        List<String> logs = new ArrayList<>();
        int i = 0;
        try {
            String line;
            do {
                line = reader.readLine();
                if (line != null) {
                    logs.add(line);
                    i++;
                }
            } while ((i < lastRows || maxRows <= 0) && line != null);
        } catch (IOException e) {
            handleFileNotFound(e);
        }
        return new Pair<>(logs, i);
    }

    private void handleFileNotFound(IOException e) throws TaierSQLException {
        Path absPath = path.toAbsolutePath();
        Path opHandle = absPath.getFileName();
        throw new TaierSQLException("Operation[" + opHandle + "] log file " + absPath + " is not found", e);
    }
    private void resetReader() throws IOException {
        trySafely(() -> {
            if (reader != null) {
                reader.close();
            }
        });
        reader = null;
        closeExtraReaders();
        extraReaders.clear();
        for (Path path : extraPaths) {
            extraReaders.add(Files.newBufferedReader(path, StandardCharsets.UTF_8));
        }
    }

    public void close() {
        lock.lock();
        try {
            closeExtraReaders();

            trySafely(() -> {
                if (reader != null) {
                    reader.close();
                }
            });
            trySafely(() -> {
                if (writer != null) {
                    writer.close();
                }
            });

            if (seekableReader != null) {
                lastSeekReadPos = 0;
                trySafely(seekableReader::close);
            }

            trySafely(() -> Files.deleteIfExists(path));
        } finally {
            lock.unlock();
        }
    }

    private void trySafely(IORunnable runnable) {
        try {
            runnable.run();
        } catch (NoSuchFileException e) {
            // Ignore
        } catch (IOException e) {
            throw new RuntimeException("Failed to remove corresponding log file of operation: " + path.toAbsolutePath(), e);
        }
    }

    private void closeExtraReaders() {
        for (BufferedReader extraReader : extraReaders) {
            try {
                extraReader.close();
            } catch (IOException e) {
                // for the outside log file reader, ignore it
            }
        }
    }

    @FunctionalInterface
    private interface IORunnable {
        void run() throws IOException;
    }

    private static class Pair<L, R> {
        private final L left;
        private final R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        public L getLeft() { return left; }
        public R getRight() { return right; }
    }

    public static class OperationLogManager {
        private static final InheritableThreadLocal<OperationLog> OPERATION_LOG = new InheritableThreadLocal<OperationLog>() {
            @Override
            protected OperationLog initialValue() {
                return null;
            }
        };

        public static void setCurrentOperationLog(OperationLog operationLog) {
            OPERATION_LOG.set(operationLog);
        }

        public static Optional<OperationLog> getCurrentOperationLog() {
            return Optional.ofNullable(OPERATION_LOG.get());
        }

        public static void removeCurrentOperationLog() {
            OPERATION_LOG.remove();
        }

        public static void createOperationLogRootDirectory(Session session) {
            session.getSessionManager().getOperationLogRoot().ifPresent(operationLogRoot -> {
                Path path = Paths.get(operationLogRoot, session.getHandle().getIdentifier().toString());
                try {
                    Files.createDirectories(path);
                    path.toFile().deleteOnExit();
                } catch (IOException e) {
                    LOGGER.error("Failed to create operation log root directory: " + path, e);
                }
            });
        }

        public static OperationLog createOperationLog(Session session, OperationHandle opHandle) {
            return session.getSessionManager().getOperationLogRoot().map(operationLogRoot -> {
                Path logPath = Paths.get(operationLogRoot, session.getHandle().getIdentifier().toString());
                Path logFile = Paths.get(logPath.toAbsolutePath().toString(), opHandle.getIdentifier().toString());
                LOGGER.info("Creating operation log file " + logFile);
                return new OperationLog(logFile);
            }).orElse(null);
        }
    }
}
