package com.dtstack.taier.datasource.plugin.common.config;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConfigBuilder {

    public String key;
    public String doc = "";
    public String version = "";

    public Optional<Consumer<ConfigEntry<?>>> onCreate = Optional.empty();
    public String type = "";
    public boolean internal = false;
    public boolean serverOnly = false;
    public List<String> alternatives = new ArrayList<>();

    public ConfigBuilder(String key) {
        this.key = key;
    }



    public Optional<Consumer<ConfigEntry<?>>> getOnCreate() {
        return onCreate;
    }

    public void setOnCreate(Optional<Consumer<ConfigEntry<?>>> onCreate) {
        this.onCreate = onCreate;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public boolean isServerOnly() {
        return serverOnly;
    }

    public void setServerOnly(boolean serverOnly) {
        this.serverOnly = serverOnly;
    }

    public List<String> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(List<String> alternatives) {
        this.alternatives = alternatives;
    }

    public ConfigBuilder internal() {
        this.internal = true;
        return this;
    }

    public ConfigBuilder serverOnly() {
        this.serverOnly = true;
        return this;
    }

    public ConfigBuilder doc(String s) {
        this.doc = s;
        return this;
    }

    public ConfigBuilder version(String s) {
        this.version = s;
        return this;
    }

    public ConfigBuilder onCreate(Consumer<ConfigEntry<?>> callback) {
        this.onCreate = Optional.of(callback);
        return this;
    }

    public ConfigBuilder withAlternative(String key) {
        this.alternatives.add(key);
        return this;
    }

    private <T> T toNumber(String s, Function<String, T> converter) {
        try {
            return converter.apply(s.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " should be " + type + ", but was " + s);
        }
    }

    public TypedConfigBuilder<Integer> intConf() {
        type = "int";
        return new TypedConfigBuilder<>(this, s -> toNumber(s, Integer::parseInt));
    }

    public TypedConfigBuilder<Long> longConf() {
        type = "long";
        return new TypedConfigBuilder<>(this, s -> toNumber(s, Long::parseLong));
    }

    public TypedConfigBuilder<Double> doubleConf() {
        type = "double";
        return new TypedConfigBuilder<>(this, s -> toNumber(s, Double::parseDouble));
    }

    public TypedConfigBuilder<Boolean> booleanConf() {
        type = "boolean";
        return new TypedConfigBuilder<>(this, s -> {
            try {
                return Boolean.parseBoolean(s.trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(key + " should be boolean, but was " + s, e);
            }
        });
    }

    public TypedConfigBuilder<String> stringConf() {
        type = "string";
        return new TypedConfigBuilder<>(this, Function.identity());
    }

    public TypedConfigBuilder<Long> timeConf() {
        type = "duration";
        Function<String, Long> timeFromStr = str -> {
            String trimmed = str.trim();
            try {
                return Duration.parse(trimmed).toMillis();
            } catch (Exception e1) {
                try {
                    return Long.parseLong(trimmed);
                } catch (Exception e2) {
                    throw new IllegalArgumentException(
                            "The formats accepted are 1) based on the ISO-8601 duration format `PnDTnHnMn.nS` " +
                                    "with days considered to be exactly 24 hours. 2). A plain long value represents " +
                                    "total milliseconds, e.g. 2000 means 2 seconds " + trimmed + " for " + key + " is not valid",
                            e2);
                }
            }
        };

        Function<Long, String> timeToStr = v -> Duration.ofMillis(v).toString();

        return new TypedConfigBuilder<>(this, timeFromStr, timeToStr);
    }

    public <T> ConfigEntry<T> fallbackConf(ConfigEntry<T> fallback) {
        ConfigEntryFallback<T> entry = new ConfigEntryFallback<>(
                key,
                alternatives,
                doc,
                version,
                internal,
                serverOnly,
                fallback);
        onCreate.ifPresent(callback -> callback.accept(entry));
        return entry;
    }

    public TypedConfigBuilder<Pattern> regexConf() {
        Function<String, Pattern> regexFromString = (str) -> {
            try {
                return Pattern.compile(str);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(key + " should be a regex, but was " + str, e);
            }
        };

        return new TypedConfigBuilder<>(this, regexFromString, Pattern::toString);
    }
}

