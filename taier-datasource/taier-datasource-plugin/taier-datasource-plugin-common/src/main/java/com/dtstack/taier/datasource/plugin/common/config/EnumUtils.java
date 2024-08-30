package com.dtstack.taier.datasource.plugin.common.config;


import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.StreamSupport;

public class EnumUtils {

    public static boolean isValidEnum(Enum<?>[] enumValues, Object enumName) {
        if (enumName == null) {
            return false;
        }
        String name = enumName.toString();
        return Arrays.stream(enumValues)
                .anyMatch(e -> e.name().equals(name));
    }

    public static boolean isValidEnums(Enum<?>[] enumValues, Iterable<?> enumNames) {
        return enumNames != null &&
                enumNames.iterator().hasNext() && // Check if not empty
                StreamSupport.stream(enumNames.spliterator(), false)
                        .allMatch(name -> isValidEnum(enumValues, name));
    }

    // Overloaded method for Enumeration (which is different in Java compared to Scala)
    public static boolean isValidEnum(Enumeration<?> enumeration, Object enumName) {
        if (enumName == null) {
            return false;
        }
        String name = enumName.toString();
        return Collections.list(enumeration).stream()
                .anyMatch(e -> e.toString().equals(name));
    }

    // Overloaded method for Enumeration and Iterable
    public static boolean isValidEnums(Enumeration<?> enumeration, Iterable<?> enumNames) {
        if (enumNames == null || !enumNames.iterator().hasNext()) {
            return false;
        }
        List<?> enumList = Collections.list(enumeration);
        return StreamSupport.stream(enumNames.spliterator(), false)
                .allMatch(name -> isValidEnum(enumList, name));
    }

    // Helper method for Enumeration validation
    private static boolean isValidEnum(List<?> enumList, Object enumName) {
        if (enumName == null) {
            return false;
        }
        String name = enumName.toString();
        return enumList.stream()
                .anyMatch(e -> e.toString().equals(name));
    }
}


