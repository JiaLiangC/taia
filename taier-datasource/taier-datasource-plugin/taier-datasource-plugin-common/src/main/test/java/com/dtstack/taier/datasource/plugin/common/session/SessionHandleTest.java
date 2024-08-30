package com.dtstack.taier.datasource.plugin.common.session;


import org.testng.annotations.Test;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SessionHandleTest {

    @Test
    public void testEquals_sameIdentifier() {
        UUID id = UUID.randomUUID();
        SessionHandle handle1 = SessionHandle.fromUUID(id.toString());
        SessionHandle handle2 = SessionHandle.fromUUID(id.toString());

        assertEquals(handle1, handle2);
    }

    @Test
    public void testNotEquals_differentIdentifier() {
        SessionHandle handle1 = SessionHandle.apply();
        SessionHandle handle2 = SessionHandle.apply();

        assertNotEquals(handle1, handle2);
    }

    @Test
    public void testEquals_sameUUIDString() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.fromString(id1.toString());
        SessionHandle handle1 = SessionHandle.fromUUID(id1.toString());
        SessionHandle handle2 = SessionHandle.fromUUID(id2.toString());

        assertEquals(handle1, handle2);
    }

    @Test
    public void testNotEquals_differentIdentifierAndToString() {
        SessionHandle handle1 = SessionHandle.apply();
        SessionHandle handle2 = SessionHandle.apply();

        assertNotEquals(handle1, handle2);
    }
}