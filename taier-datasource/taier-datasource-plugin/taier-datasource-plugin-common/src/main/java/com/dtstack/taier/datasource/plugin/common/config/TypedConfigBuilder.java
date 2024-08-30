package com.dtstack.taier.datasource.plugin.common.config;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TypedConfigBuilder<T> {
    private ConfigBuilder parent;
    private Function<String, T> fromStr;
    private Function<T, String> toStr;

    public TypedConfigBuilder(ConfigBuilder parent, Function<String, T> fromStr) {
        this(parent, fromStr, obj -> Optional.ofNullable(obj).map(Object::toString).orElse(null));
    }

    public TypedConfigBuilder(ConfigBuilder parent, Function<String, T> fromStr, Function<T, String> toStr) {
        this.parent = parent;
        this.fromStr = fromStr;
        this.toStr = toStr;
    }

    public TypedConfigBuilder<T> transform(Function<T, T> fn) {
        try {
            Function<String, T> originalFromStr = this.fromStr;
            this.fromStr = s -> fn.apply(originalFromStr.apply(s));
        }catch (Exception e){
            System.out.println(e.getMessage()+"xxx");
        }

        return this;
    }

    public TypedConfigBuilder<T> transformToUpperCase() {
        return transformString(s -> s.toUpperCase(Locale.ROOT));
    }

    public TypedConfigBuilder<T> transformToLowerCase() {
        return transformString(s -> s.toLowerCase(Locale.ROOT));
    }

    @SuppressWarnings("unchecked")
    private TypedConfigBuilder<T> transformString(Function<String, String> fn) {
        if (!"string".equals(parent.type)) {
            throw new IllegalStateException("transformString can only be applied to string configs");
        }
        return ((TypedConfigBuilder<String>) this)
                .transform(fn)
                .asInstanceOf();
    }

    @SuppressWarnings("unchecked")
    private <R> TypedConfigBuilder<R> asInstanceOf() {
        return (TypedConfigBuilder<R>) this;
    }

    public TypedConfigBuilder<T> checkValue(Predicate<T> validator, String errMsg) {
        return transform(v -> {
            if (!validator.test(v)) {
                throw new IllegalArgumentException("'" + v + "' in " + parent.key + " is invalid. " + errMsg);
            }
            return v;
        });
    }

    public TypedConfigBuilder<T> checkValues(Set<T> validValues) {
        return transform(v -> {
            if (!validValues.contains(v)) {
                throw new IllegalArgumentException(
                        "The value of " + parent.key + " should be one of " +
                                String.join(", ", validValues.stream().map(Object::toString).toArray(String[]::new)) +
                                ", but was " + v);
            }
            return v;
        });
    }


    public TypedConfigBuilder<T> checkValues(Enumeration<?> enumeration) {
        return transform(v -> {
            boolean isValid;
            if (v instanceof Iterable) {
                isValid = EnumUtils.isValidEnums(enumeration, (Iterable<?>) v);
            } else {
                isValid = EnumUtils.isValidEnum(enumeration, v);
            }
            if (!isValid) {
                String actualValueStr;
                if (v instanceof Iterable) {
                    actualValueStr = StreamSupport.stream(((Iterable<?>) v).spliterator(), false)
                            .map(Object::toString)
                            .collect(Collectors.joining(","));
                } else {
                    actualValueStr = v.toString();
                }

                List<String> enumValues = Collections.list(enumeration).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());

                throw new IllegalArgumentException(
                        "The value of " + parent.key + " should be one of " +
                                String.join(", ", enumValues) +
                                ", but was " + actualValueStr);
            }
            return v;
        });
    }

    /*public TypedConfigBuilder<T> checkValues(Enumeration<?> enumeration) {
        return transform(v -> {
            boolean isValid;
            if (v instanceof Iterable) {
                isValid = EnumUtils.isValidEnums(enumeration, (Iterable<?>) v);
            } else {
                isValid = EnumUtils.isValidEnum(enumeration, v);
            }
            if (!isValid) {
                String actualValueStr = v instanceof Iterable ?
                        String.join(",", ((Iterable<?>) v).stream().map(Object::toString).toArray(String[]::new)) :
                        v.toString();
                throw new IllegalArgumentException(
                        "The value of " + parent.key + " should be one of " +
                                String.join(", ", Collections.list(enumeration.elements()).stream().map(Object::toString).toArray(String[]::new)) +
                                ", but was " + actualValueStr);
            }
            return v;
        });
    }*/

    public TypedConfigBuilder<List<T>> toSequence(String sp) {
        parent.type = "seq";
        return new TypedConfigBuilder<>(parent,
                s -> ConfigHelpers.strToSeq(s, fromStr, sp),
                list -> ConfigHelpers.iterableToStr(list, toStr));
    }

    public TypedConfigBuilder<Set<T>> toSet(String sp, boolean skipBlank) {
        parent.type = "set";
        return new TypedConfigBuilder<>(parent,
                s -> ConfigHelpers.strToSet(s, fromStr, sp, skipBlank),
                set -> ConfigHelpers.iterableToStr(set, toStr));
    }

    public OptionalConfigEntry<T> createOptional() {
        OptionalConfigEntry<T> entry = new OptionalConfigEntry<>(
                parent.key,
                parent.alternatives,
                fromStr,
                toStr,
                parent.doc,
                parent.version,
                parent.type,
                parent.internal,
                parent.serverOnly);
        parent.onCreate.ifPresent(callback -> callback.accept(entry));
        return entry;
    }

    public ConfigEntry<T> createWithDefault(T defaultValue) {
        if (defaultValue instanceof String) {
            return createWithDefaultString((String) defaultValue);
        } else {
            T d = fromStr.apply(toStr.apply(defaultValue));
            ConfigEntryWithDefault<T> entry = new ConfigEntryWithDefault<>(
                    parent.key,
                    parent.alternatives,
                    d,
                    fromStr,
                    toStr,
                    parent.doc,
                    parent.version,
                    parent.type,
                    parent.internal,
                    parent.serverOnly);
            parent.onCreate.ifPresent(callback -> callback.accept(entry));
            return entry;
        }
    }

    public ConfigEntryWithDefaultString<T> createWithDefaultString(String defaultValue) {
        ConfigEntryWithDefaultString<T> entry = new ConfigEntryWithDefaultString<>(
                parent.key,
                parent.alternatives,
                defaultValue,
                fromStr,
                toStr,
                parent.doc,
                parent.version,
                parent.type,
                parent.internal,
                parent.serverOnly);
        parent.onCreate.ifPresent(callback -> callback.accept(entry));
        return entry;
    }
}

