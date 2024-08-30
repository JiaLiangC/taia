package com.dtstack.taier.datasource.plugin.common.config;

import com.dtstack.taier.datasource.plugin.common.exception.TaierException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class TaierConf {
    private static Logger LOGGER = LoggerFactory.getLogger(TaierConf.class);

    private final ConcurrentHashMap<String, String> settings;
    private final ConfigProvider reader;

    public TaierConf() {
        this(true);
    }

    public TaierConf(boolean loadSysDefault) {
        this.settings = new ConcurrentHashMap<>();
        this.reader = new ConfigProvider(settings);

        if (loadSysDefault) {
            Map<String, String> fromSysDefaults = Utils.getSystemProperties().entrySet().stream()
                    .filter(e -> e.getKey().startsWith("taier."))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            loadFromMap(fromSysDefaults);
        }
    }

    private void loadFromMap(Map<String, String> props) {
        settings.putAll(props);
    }

    public TaierConf loadFileDefaults() {
        Optional<File> maybeConfigFile = Utils.getDefaultPropertiesFile();
        maybeConfigFile.ifPresent(file -> {
            try {
                loadFromMap(Utils.loadPropertiesFromFile(Optional.of(file).get()));
            } catch (TaierException e) {
                throw new RuntimeException(e);
            }
        });
        return this;
    }

    public <T> TaierConf set(ConfigEntry<T> entry, T value) {
        Objects.requireNonNull(entry, "entry cannot be null");
        Objects.requireNonNull(value, "value cannot be null for key: " + entry.key());
        if (!containsConfigEntry(entry)) {
            throw new IllegalArgumentException(entry + " is not registered");
        }
        if (settings.put(entry.key(), entry.strConverter().apply(value)) == null) {
            logDeprecationWarning(entry.key());
        }
        return this;
    }

    public TaierConf set(String key, String value) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null for key: " + key);
        if (settings.put(key, value) == null) {
            logDeprecationWarning(key);
        }
        return this;
    }

    public <T> T get(ConfigEntry<T> config) {
        if (!containsConfigEntry(config)) {
            throw new IllegalArgumentException(config + " is not registered");
        }
        return config.readFrom(reader);
    }

    public Optional<String> getOption(String key) {
        return Optional.ofNullable(settings.get(key));
    }

    public TaierConf unset(String key) {
        settings.remove(key);
        return this;
    }

    public TaierConf unset(ConfigEntry<?> entry) {
        if (!containsConfigEntry(entry)) {
            throw new IllegalArgumentException(entry + " is not registered");
        }
        return unset(entry.key());
    }

    public Map<String, String> getAll() {
        return new TreeMap<>(settings);
    }

    @Override
    public TaierConf clone() {
        try {
            TaierConf cloned = (TaierConf) super.clone();
            cloned.settings.putAll(this.settings);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }


    private void logDeprecationWarning(String key) {
        DeprecatedConfig config = deprecatedConfigs.get(key);
        if (config != null) {
            LOGGER.warn("The Taier config '{}' has been deprecated in Taier v{} and may be removed in the future. {}",
                    config.key, config.version, config.comment);
        }
    }

    // Static members and methods

    private static final Map<String, ConfigEntry<?>> taierConfEntries = new ConcurrentHashMap<>();
    private static final Set<ConfigEntry<?>> serverOnlyConfEntries = new HashSet<>();

    public static ConfigBuilder buildConf(String key) {
        return new ConfigBuilder(key).onCreate(TaierConf::register);
    }

    private static void register(ConfigEntry<?> entry) {
        synchronized (TaierConf.class) {
            if (taierConfEntries.containsKey(entry.key())) {
                throw new IllegalArgumentException("Duplicate ConfigEntry. " + entry.key() + " has been registered");
            }
            taierConfEntries.put(entry.key(), entry);
            if (entry.serverOnly()) {
                serverOnlyConfEntries.add(entry);
            }
        }
    }

    private static boolean containsConfigEntry(ConfigEntry<?> entry) {
        return taierConfEntries.get(entry.key()) == entry;
    }

    // Configuration entries
    public static final ConfigEntry<Integer> FRONTEND_BIND_PORT = buildConf("taier.frontend.bind.port")
            .doc("Port of the machine on which to run the frontend service.")
            .version("1.5.0")
            .serverOnly()
            .intConf()
            .createWithDefault(18888);

    public static final ConfigEntry<Long> SESSION_IDLE_TIMEOUT = buildConf("taier.session.idle.timeout")
            .doc("(deprecated)session timeout, it will be closed when it's not accessed for this duration")
            .version("1.5.0")
            .timeConf()
            .checkValue(v -> v >= Duration.ofSeconds(3).toMillis(), "Minimum 3 seconds")
            .createWithDefault(Duration.ofHours(6).toMillis());

    public static final ConfigEntry<Integer> SERVER_EXEC_POOL_SIZE = buildConf("taier.backend.server.exec.pool.size")
            .doc("Number of threads in the operation execution thread pool of Taier server")
            .version("1.5.0")
            .intConf()
            .createWithDefault(100);

    public static final ConfigEntry<Integer> SERVER_EXEC_WAIT_QUEUE_SIZE = buildConf("taier.backend.server.exec.pool.wait.queue.size")
            .doc("Size of the wait queue for the operation execution thread pool of Taier server")
            .version("1.5.0")
            .intConf()
            .createWithDefault(100);

    public static final ConfigEntry<Long> SERVER_EXEC_KEEPALIVE_TIME =
    buildConf("taier.server.exec.pool.keepalive.time")
      .doc("Time(ms) that an idle async thread of the operation execution thread pool will wait" +
                   " for a new task to arrive before terminating in Kyuubi server")
      .version("1.0.0")
      .timeConf()
      .createWithDefault(Duration.ofSeconds(60).toMillis());



    public static final ConfigEntry<Long> SERVER_EXEC_POOL_SHUTDOWN_TIMEOUT =
    buildConf("taier.server.exec.pool.shutdown.timeout")
      .doc("Timeout(ms) for the operation execution thread pool to terminate in taier server")
      .version("1.0.0")
      .timeConf()
      .createWithDefault(Duration.ofSeconds(10).toMillis());

    public static final ConfigEntry<Long> SESSION_CHECK_INTERVAL = buildConf("taier.session.check.interval")
            .doc("The check interval for session timeout.")
            .version("1.5.0")
            .timeConf()
            .checkValue(v -> v > Duration.ofSeconds(3).toMillis(), "Minimum 3 seconds")
            .createWithDefault(Duration.ofMinutes(5).toMillis());

    public static final ConfigEntry<Long> OPERATION_IDLE_TIMEOUT = buildConf("taier.operation.idle.timeout")
            .doc("Operation will be closed when it's not accessed for this duration of time")
            .version("1.5.0")
            .timeConf()
            .createWithDefault(Duration.ofHours(3).toMillis());

    public static final ConfigEntry<Integer> ENGINE_JDBC_FETCH_SIZE = buildConf("taier.engine.jdbc.fetch.size")
            .doc("The fetch size of JDBC engine")
            .version("1.5.0")
            .intConf()
            .createWithDefault(1000);


    private static final Map<String, DeprecatedConfig> deprecatedConfigs = new HashMap<>();
    private static class DeprecatedConfig {
        final String key;
        final String version;
        final String comment;

        DeprecatedConfig(String key, String version, String comment) {
            this.key = key;
            this.version = version;
            this.comment = comment;
        }
    }
}
