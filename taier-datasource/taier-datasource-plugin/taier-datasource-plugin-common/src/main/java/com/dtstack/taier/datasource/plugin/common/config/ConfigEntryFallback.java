package com.dtstack.taier.datasource.plugin.common.config;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

class ConfigEntryFallback<T> implements ConfigEntry<T> {
    private final String key;
    private final List<String> alternatives;
    private final String doc;
    private final String version;
    private final boolean internal;
    private final boolean serverOnly;
    private final ConfigEntry<T> fallback;

    @Override
    public String toString() {
        return String.format("ConfigEntry(key=%s, defaultValue=%s, doc=%s, version=%s, type=%s)",
                key(), defaultValStr(), doc(), version(), typ());
    }

    public ConfigEntryFallback(String key, List<String> alternatives, String doc, String version,
                               boolean internal, boolean serverOnly, ConfigEntry<T> fallback) {
        this.key = key;
        this.alternatives = alternatives;
        this.doc = doc;
        this.version = version;
        this.internal = internal;
        this.serverOnly = serverOnly;
        this.fallback = fallback;
        ConfigEntry.registerEntry(this);
    }

    @Override
    public String defaultValStr() {
        return fallback.defaultValStr();
    }

    @Override
    public Optional<T> defaultVal() {
        return fallback.defaultVal();
    }

    @Override
    public T readFrom(ConfigProvider conf) {
        return readString(conf).map(valueConverter()).orElseGet(() -> fallback.readFrom(conf));
    }

    @Override public String key() { return key; }
    @Override public List<String> alternatives() { return alternatives; }
    @Override public Function<String, T> valueConverter() { return fallback.valueConverter(); }
    @Override public Function<T, String> strConverter() { return fallback.strConverter(); }
    @Override public String doc() { return doc; }
    @Override public String version() { return version; }
    @Override public String typ() { return fallback.typ(); }
    @Override public boolean internal() { return internal; }
    @Override public boolean serverOnly() { return serverOnly; }
}
