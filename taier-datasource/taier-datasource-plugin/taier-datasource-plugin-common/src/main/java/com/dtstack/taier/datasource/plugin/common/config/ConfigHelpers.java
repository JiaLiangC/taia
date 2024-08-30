package com.dtstack.taier.datasource.plugin.common.config;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ConfigHelpers {

    public static <T> List<T> strToSeq(String str, Function<String, T> converter, String sp) {
        return Utils.strToSeq(str, sp).stream()
                .map(converter)
                .collect(Collectors.toList());
    }

    public static <T> Set<T> strToSet(String str, Function<String, T> converter, String sp, boolean skipBlank) {
        return Utils.strToSeq(str, sp).stream()
                .filter(s -> !skipBlank || StringUtils.isNotBlank(s))
                .map(converter)
                .collect(Collectors.toSet());
    }

    public static <T> String iterableToStr(Iterable<T> v, Function<T, String> stringConverter) {
        return iterableToStr(v, stringConverter, ",");
    }

    public static <T> String iterableToStr(Iterable<T> v, Function<T, String> stringConverter, String sp) {
        return StreamSupport.stream(v.spliterator(), false)
                .map(stringConverter)
                .collect(Collectors.joining(sp));
    }
}
