package com.dtstack.taier.datasource.plugin.common.operation;

import org.testng.annotations.Test;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Assert;

public class OperationHandleTest {

    @Test
    public void testEquals_sameIdentifier() {
        UUID id = UUID.randomUUID();
        OperationHandle handle1 = new OperationHandle(id);
        OperationHandle handle2 = new OperationHandle(id);

        assertEquals(handle1, handle2);
    }

    @Test
    public void testNotEquals_differentIdentifier() {
        OperationHandle handle1 = new OperationHandle(UUID.randomUUID());
        OperationHandle handle2 = new OperationHandle(UUID.randomUUID());

        assertNotEquals(handle1, handle2);
    }

    @Test
    public void testEquals_sameUUIDString() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.fromString(id1.toString());
        OperationHandle handle1 = new OperationHandle(id1);
        OperationHandle handle2 = new OperationHandle(id2);

        assertEquals(handle1, handle2);
    }

    @Test
    public void testNotEquals_differentIdentifierAndToString() {
        OperationHandle handle1 = new OperationHandle(UUID.randomUUID());
        OperationHandle handle2 = new OperationHandle(UUID.randomUUID());

        assertNotEquals(handle1, handle2);
    }
}