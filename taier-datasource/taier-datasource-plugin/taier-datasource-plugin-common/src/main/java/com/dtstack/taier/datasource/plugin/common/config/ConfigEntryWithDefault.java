package com.dtstack.taier.datasource.plugin.common.config;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

class ConfigEntryWithDefault<T> implements ConfigEntry<T> {
    private final String key;
    private final List<String> alternatives;
    private final T defaultVal;
    private final Function<String, T> valueConverter;
    private final Function<T, String> strConverter;
    private final String doc;
    private final String version;
    private final String type;
    private final boolean internal;
    private final boolean serverOnly;

    public ConfigEntryWithDefault(String key, List<String> alternatives, T defaultVal,
                                  Function<String, T> valueConverter, Function<T, String> strConverter,
                                  String doc, String version, String type, boolean internal, boolean serverOnly) {
        this.key = key;
        this.alternatives = alternatives;
        this.defaultVal = defaultVal;
        this.valueConverter = valueConverter;
        this.strConverter = strConverter;
        this.doc = doc;
        this.version = version;
        this.type = type;
        this.internal = internal;
        this.serverOnly = serverOnly;
        ConfigEntry.registerEntry(this);
    }

    @Override
    public String toString() {
        return String.format("ConfigEntry(key=%s, defaultValue=%s, doc=%s, version=%s, type=%s)",
                key(), defaultValStr(), doc(), version(), typ());
    }


    @Override
    public String defaultValStr() {
        return strConverter.apply(defaultVal);
    }

    @Override
    public Optional<T> defaultVal() {
        return Optional.ofNullable(defaultVal);
    }

    @Override
    public T readFrom(ConfigProvider conf) {
        return readString(conf).map(valueConverter).orElse(defaultVal);
    }

    @Override public String key() { return key; }
    @Override public List<String> alternatives() { return alternatives; }
    @Override public Function<String, T> valueConverter() { return valueConverter; }
    @Override public Function<T, String> strConverter() { return strConverter; }
    @Override public String doc() { return doc; }
    @Override public String version() { return version; }
    @Override public String typ() { return type; }
    @Override public boolean internal() { return internal; }
    @Override public boolean serverOnly() { return serverOnly; }
}