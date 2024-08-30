package com.dtstack.taier.datasource.plugin.common.config;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public interface ConfigEntry<T> {
    String key();
    List<String> alternatives();
    Function<String, T> valueConverter();
    Function<T, String> strConverter();
    String doc();
    String version();
    String typ();
    boolean internal();
    boolean serverOnly();

    String defaultValStr();
    Optional<T> defaultVal();


    default Optional<String> readString(ConfigProvider provider) {
        Optional<String> result = provider.get(key());
        for (String nextKey : alternatives()) {
            if (result.isPresent()) break;
            result = provider.get(nextKey);
        }
        return result;
    }

    T readFrom(ConfigProvider conf);

    static void registerEntry(ConfigEntry<?> entry) {
        ConfigEntry.knownConfigs.put(entry.key(), entry);
    }

    String UNDEFINED = "<undefined>";
    ConcurrentHashMap<String, ConfigEntry<?>> knownConfigs = new ConcurrentHashMap<>();
}






