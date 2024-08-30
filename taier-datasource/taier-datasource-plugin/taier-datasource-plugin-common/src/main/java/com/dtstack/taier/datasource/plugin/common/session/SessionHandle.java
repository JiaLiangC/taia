package com.dtstack.taier.datasource.plugin.common.session;

import java.util.UUID;

public class SessionHandle {
    private final UUID identifier;

    private SessionHandle(UUID identifier) {
        this.identifier = identifier;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return "SessionHandle [" + identifier + "]";
    }


    public static SessionHandle apply() {
        return new SessionHandle(UUID.randomUUID());
    }

    public static SessionHandle fromUUID(String uuid) {
        return new SessionHandle(UUID.fromString(uuid));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionHandle that = (SessionHandle) o;
        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
}
