package com.dtstack.taier.develop.dto.devlop;


import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class SessionOpenRequest {
    private Map<String, String> configs;

    public SessionOpenRequest() {}

    public SessionOpenRequest(Map<String, String> configs) {
        this.configs = configs;
    }

    public Map<String, String> getConfigs() {
        if (null == configs) {
            return Collections.emptyMap();
        }
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionOpenRequest that = (SessionOpenRequest) o;
        return Objects.equals(getConfigs(), that.getConfigs());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConfigs());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
