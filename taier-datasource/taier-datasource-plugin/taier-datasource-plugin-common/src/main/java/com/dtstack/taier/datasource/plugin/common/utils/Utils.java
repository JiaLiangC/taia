package com.dtstack.taier.datasource.plugin.common.utils;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateFormatUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils  {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final String TAIER_CONF_FILE_NAME = "taier-defaults.conf";
    private static final String TAIER_CONF_DIR = "TAIER_CONF_DIR";
    private static final String TAIER_HOME = "TAIER_HOME";
    private static final String CONF = "--conf";
    private static final String REDACTION_REPLACEMENT_TEXT = "*********(redacted)";
    private static final Pattern PATTERN_FOR_KEY_VALUE_ARG = Pattern.compile("(.+)=(.+)");

    private static final AtomicLong tempFileIdCounter = new AtomicLong(0);
    public static List<String> strToSeq(String s, String sp) {
        if (s == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }
        return Arrays.stream(s.split(sp))
                .map(String::trim)
                .filter(str -> !str.isEmpty())
                .collect(Collectors.toList());
    }

    /*public static Map<String, String> getSystemProperties() {
        return new HashMap<>(System.getProperties().stringPropertyNames().stream()
                .collect(Collectors.toMap(
                        prop -> prop,
                        System::getProperty
                )));
    }

    public static Optional<File> getDefaultPropertiesFile() {
        return getPropertiesFile(TAIER_CONF_FILE_NAME, System.getenv());
    }

    public static Optional<File> getPropertiesFile(String fileName, Map<String, String> env) {
        Optional<File> file = Optional.ofNullable(env.get(TAIER_CONF_DIR))
                .or(() -> Optional.ofNullable(env.get(TAIER_HOME)).map(home -> home + File.separator + "conf"))
                .map(d -> new File(d + File.separator + fileName))
                .filter(File::exists);

        if (!file.isPresent()) {
            file = Optional.ofNullable(Utils.class.getClassLoader().getResource(fileName))
                    .map(url -> new File(url.getFile()))
                    .filter(File::exists);
        }

        return file;
    }
*/
   /* public static Map<String, String> getPropertiesFromFile(Optional<File> file) {
        return file.map(f -> {
            info("Loading Taier properties from " + f.getAbsolutePath());
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
                Properties properties = new Properties();
                properties.load(reader);
                return properties.stringPropertyNames().stream()
                        .collect(Collectors.toMap(
                                k -> k,
                                k -> properties.getProperty(k).trim()
                        ));
            } catch (IOException e) {
                throw new TaierException("Failed when loading Taier properties from " + f.getAbsolutePath(), e);
            }
        }).orElse(Collections.emptyMap());
    }

    private static final int MAX_DIR_CREATION_ATTEMPTS = 10;

    public static Path createDirectory(String root, String namePrefix) {
        IOException lastException = null;
        for (int attempt = 0; attempt < MAX_DIR_CREATION_ATTEMPTS; attempt++) {
            try {
                Path candidate = Paths.get(root, namePrefix + "-" + UUID.randomUUID());
                return Files.createDirectories(candidate);
            } catch (IOException e) {
                lastException = e;
            }
        }
        throw new IOException("Failed to create a temp directory (under " + root + ") after "
                + MAX_DIR_CREATION_ATTEMPTS + " attempts!", lastException);
    }
*/
    public static Path getAbsolutePathFromWork(String pathStr) {
        return getAbsolutePathFromWork(pathStr, System.getenv());
    }

    public static Path getAbsolutePathFromWork(String pathStr, Map<String, String> env) {
        Path path = Paths.get(pathStr);
        if (path.isAbsolute()) {
            return path;
        } else {
            String workDir = env.getOrDefault("TAIER_WORK_DIR_ROOT", System.getProperty("user.dir"));
            return Paths.get(workDir, pathStr);
        }
    }

    public static boolean deleteDirectoryRecursively(File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectoryRecursively(file);
                }
            }
        }
        return f.delete();
    }

/*
    public static Path createTempDir() {
        return createTempDir("taier", System.getProperty("java.io.tmpdir"));
    }
*/

   /* public static Path createTempDir(String prefix, String root) {
        Path dir = createDirectory(root, prefix);
        dir.toFile().deleteOnExit();
        return dir;
    }*/
    public static List<File> listFilesRecursively(File file) {
        if (!file.isDirectory()) {
            return Collections.singletonList(file);
        } else {
            List<File> result = new ArrayList<>();
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    result.addAll(listFilesRecursively(f));
                }
            }
            return result;
        }
    }

    public static File writeToTempFile(InputStream source, Path dir, String fileName) throws IOException {
        if (source == null) {
            throw new IOException("the source inputstream is null");
        }
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        String[] parts = fileName.split("\\.", 2);
        String prefix = parts[0];
        String suffix = parts.length > 1 ? "." + parts[1] : "";
        String currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String identifier = currentTime + "-" + tempFileIdCounter.incrementAndGet();
        Path filePath = dir.resolve(prefix + "-" + identifier + suffix);
        try {
            Files.copy(source, filePath, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            source.close();
        }
        File file = filePath.toFile();
        file.deleteOnExit();
        return file;
    }

/*
    public static String currentUser() {
        try {
            return UserGroupInformation.getCurrentUser().getShortUserName();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get current user", e);
        }
    }

    public static <T> T doAs(String proxyUser, UserGroupInformation realUser, Callable<T> action) throws Exception {
        UserGroupInformation proxyUserUgi = UserGroupInformation.createProxyUser(proxyUser, realUser);
        return proxyUserUgi.doAs((PrivilegedExceptionAction<T>) action::call);
    }

    public static boolean isTesting() {
        return System.getProperty(Tests.IS_TESTING.key()) != null;
    }
*/


    public static final int DEFAULT_SHUTDOWN_PRIORITY = 100;
    public static final int SERVER_SHUTDOWN_PRIORITY = 75;
    public static final int SPARK_CONTEXT_SHUTDOWN_PRIORITY = 50;
    public static final int FLINK_ENGINE_SHUTDOWN_PRIORITY = 50;
    public static final int TRINO_ENGINE_SHUTDOWN_PRIORITY = 50;
    public static final int JDBC_ENGINE_SHUTDOWN_PRIORITY = 50;

   /* public static void addShutdownHook(Runnable hook) {
        addShutdownHook(hook, DEFAULT_SHUTDOWN_PRIORITY);
    }
*/
  /*  public static void addShutdownHook(Runnable hook, int priority) {
        ShutdownHookManager.get().addShutdownHook(hook, priority);
    }*/

    public static String getDateFromTimestamp(long time) {
        return DateFormatUtils.format(time, "yyyyMMdd", TimeZone.getDefault());
    }

    public static String stringifyException(Throwable e) {
        StringWriter stm = new StringWriter();
        PrintWriter wrt = new PrintWriter(stm);
        e.printStackTrace(wrt);
        wrt.close();
        return stm.toString();
    }

    public static void tryLogNonFatalError(Runnable block) {
        try {
            block.run();
        } catch (Exception t) {
            LOGGER.error("Uncaught exception in thread " + Thread.currentThread().getName(), t);
        }
    }

  /*  public static void fromCommandLineArgs(String[] args, TaierConf conf) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Illegal size of arguments.");
        }
        for (int i = 0; i < args.length; i += 2) {
            if (!args[i].equals(CONF)) {
                throw new IllegalArgumentException("Unrecognized main arguments prefix " + args[i] +
                        ", the argument format is '--conf k=v'.");
            }
            String[] kv = args[i + 1].split("=", 2);
            if (kv.length != 2) {
                throw new IllegalArgumentException("Illegal argument: " + args[i + 1]);
            }
            conf.set(kv[0].trim(), kv[1].trim());
        }
    }

    public static List<String> redactCommandLineArgs(TaierConf conf, List<String> commands) {
        Optional<Pattern> redactionPattern = conf.get(TaierConf.SERVER_SECRET_REDACTION_PATTERN);
        if (!redactionPattern.isPresent()) {
            return commands;
        }

        boolean nextKV = false;
        List<String> redactedCommands = new ArrayList<>();
        for (String cmd : commands) {
            if (nextKV) {
                Matcher matcher = PATTERN_FOR_KEY_VALUE_ARG.matcher(cmd);
                if (matcher.matches()) {
                    String key = matcher.group(1);
                    String value = matcher.group(2);
                    List<Pair<String, String>> redacted = redact(redactionPattern.get(), Collections.singletonList(new Pair<>(key, value)));
                    String newValue = redacted.get(0).getValue();
                    redactedCommands.add(genKeyValuePair(key, newValue));
                } else {
                    redactedCommands.add(cmd);
                }
                nextKV = false;
            } else if (cmd.equals(CONF)) {
                nextKV = true;
                redactedCommands.add(cmd);
            } else {
                redactedCommands.add(cmd);
            }
        }
        return redactedCommands;
    }*/

    public static <K, V> List<Pair<K, V>> redact(Optional<Pattern> regex, List<Pair<K, V>> kvs) {
        return regex.map(r -> redact(r, kvs)).orElse(kvs);
    }

    private static <K, V> List<Pair<K, V>> redact(Pattern redactionPattern, List<Pair<K, V>> kvs) {
        return kvs.stream().map(pair -> {
            if (pair.getKey() instanceof String && pair.getValue() instanceof String) {
                String key = (String) pair.getKey();
                String value = (String) pair.getValue();
                if (redactionPattern.matcher(key).find() || redactionPattern.matcher(value).find()) {
                    return new Pair<K, V>(pair.getKey(), (V) REDACTION_REPLACEMENT_TEXT);
                }
            } else if (pair.getValue() instanceof String) {
                String value = (String) pair.getValue();
                if (redactionPattern.matcher(value).find()) {
                    return new Pair<K, V>(pair.getKey(), (V) REDACTION_REPLACEMENT_TEXT);
                }
            }
            return pair;
        }).collect(Collectors.toList());
    }

    public static boolean isCommandAvailable(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"which", cmd});
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static ClassLoader getTaierClassLoader() {
        return Utils.class.getClassLoader();
    }

    public static ClassLoader getContextOrTaierClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return (cl == null) ? getTaierClassLoader() : cl;
    }

    public static boolean isOnK8s() {
        return Files.exists(Paths.get("/var/run/secrets/kubernetes.io"));
    }

    public static String prettyPrint(Throwable e) {
        if (e == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static <T> T withLockRequired(Lock lock, Supplier<T> block) {
        lock.lock();
        try {
            return block.get();
        } finally {
            lock.unlock();
        }
    }

    public static Optional<Integer> terminateProcess(Process process, long gracefulPeriod) {
        process.destroy();
        try {
            if (process.waitFor(gracefulPeriod, TimeUnit.MILLISECONDS)) {
                return Optional.of(process.exitValue());
            } else {
                LOGGER.warn("Process does not exit after " + gracefulPeriod + " ms, try to forcibly kill. " +
                        "Staging files generated by the process may be retained!");
                process.destroyForcibly();
                return Optional.empty();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
}
