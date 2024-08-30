package com.dtstack.taier.datasource.plugin.common.config;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

class OptionalConfigEntry<T> implements ConfigEntry<Optional<T>> {
    private final String key;
    private final List<String> alternatives;
    private final Function<String, T> rawValueConverter;
    private final Function<T, String> rawStrConverter;
    private final String doc;
    private final String version;
    private final String type;
    private final boolean internal;
    private final boolean serverOnly;

    public OptionalConfigEntry(String key, List<String> alternatives, Function<String, T> rawValueConverter,
                               Function<T, String> rawStrConverter, String doc, String version, String type,
                               boolean internal, boolean serverOnly) {
        this.key = key;
        this.alternatives = alternatives;
        this.rawValueConverter = rawValueConverter;
        this.rawStrConverter = rawStrConverter;
        this.doc = doc;
        this.version = version;
        this.type = type;
        this.internal = internal;
        this.serverOnly = serverOnly;
        ConfigEntry.registerEntry(this);
    }

    @Override
    public Function<String, Optional<T>> valueConverter() {
        return s -> Optional.ofNullable(rawValueConverter.apply(s));
    }

    @Override
    public String toString() {
        return String.format("ConfigEntry(key=%s, defaultValue=%s, doc=%s, version=%s, type=%s)",
                key(), defaultValStr(), doc(), version(), typ());
    }


    @Override
    public Function<Optional<T>, String> strConverter() {
        return v -> v.map(rawStrConverter).orElse(null);
    }

    @Override
    public String defaultValStr() {
        return ConfigEntry.UNDEFINED;
    }

    @Override
    public Optional<T> readFrom(ConfigProvider conf) {
        return readString(conf).map(rawValueConverter);
    }

    @Override
    public Optional<Optional<T>> defaultVal() {
        return Optional.empty();
    }

    @Override public String key() { return key; }
    @Override public List<String> alternatives() { return alternatives; }
    @Override public String doc() { return doc; }
    @Override public String version() { return version; }
    @Override public String typ() { return type; }
    @Override public boolean internal() { return internal; }
    @Override public boolean serverOnly() { return serverOnly; }
}
