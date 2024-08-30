package com.dtstack.taier.datasource.plugin.common.session;
import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.operation.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertThrows;

public class OperationManagerTest {

    private OperationManager operationManager;
    private Session session;

    @Before
    public void setUp() {
        operationManager = new NoopOperationManager();
        session = mock(Session.class);
    }

    @Test
    public void testOperationDoesNotExist() {
        OperationHandle handle = OperationHandle.create();
        assertThrows(TaierSQLException.class, () -> operationManager.cancelOperation(handle));
        assertThrows(TaierSQLException.class, () -> operationManager.closeOperation(handle));
        assertThrows(TaierSQLException.class, () -> operationManager.getOperationResultSetSchema(handle));
        assertThrows(TaierSQLException.class, () -> operationManager.getOperationNextRowSet(handle, FetchOrientation.FETCH_NEXT, 10));
    }

    @Test
    public void testOperationAlreadyClosed() throws TaierSQLException {
        Operation operation = new NoopOperation(session);
        OperationHandle handle = operation.getHandle();
        operationManager.addOperation(operation);
        operationManager.closeOperation(handle);
        assertThrows(TaierSQLException.class, () -> operationManager.cancelOperation(handle));
        assertThrows(TaierSQLException.class, () -> operationManager.closeOperation(handle));
        assertThrows(TaierSQLException.class, () -> operationManager.getOperationResultSetSchema(handle));
        assertThrows(TaierSQLException.class, () -> operationManager.getOperationNextRowSet(handle, FetchOrientation.FETCH_NEXT, 10));
    }

    @Test
    public void testOperationAutoCloseAfterTimeout() throws InterruptedException, TaierSQLException {
        Operation operation = new NoopOperation(session);
        OperationHandle handle = operation.getHandle();
        operationManager.addOperation(operation);
        List<OperationHandle> handles = new ArrayList<>();
        handles.add(handle);
        operationManager.removeExpiredOperations(handles);

        Thread.sleep(1000); // Simulate time passing
        assertTrue(operation.isTimedOut());
        assertThrows(TaierSQLException.class, () -> operationManager.getOperation(handle));
    }

    @Test
    public void testConcurrentCloseSameOperation() throws InterruptedException {
        Operation operation = new NoopOperation(session);
        OperationHandle handle = operation.getHandle();
        operationManager.addOperation(operation);
        Runnable closeTask = () -> {
            try {
                operationManager.closeOperation(handle);
            } catch (TaierSQLException e) {
                // Ignore
            }
        };
        Thread t1 = new Thread(closeTask);
        Thread t2 = new Thread(closeTask);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        assertSame(operation.getStatus().getState(), OperationState.CLOSED);
    }

    @Test
    public void testConcurrentCloseDifferentOperations() throws InterruptedException {
        Operation operation1 = new NoopOperation(session);
        Operation operation2 = new NoopOperation(session);
        OperationHandle handle1 = operation1.getHandle();
        OperationHandle handle2 = operation2.getHandle();
        operationManager.addOperation(operation1);
        operationManager.addOperation(operation2);
        Runnable closeTask1 = () -> {
            try {
                operationManager.closeOperation(handle1);
            } catch (TaierSQLException e) {
                // Ignore
            }
        };
        Runnable closeTask2 = () -> {
            try {
                operationManager.closeOperation(handle2);
            } catch (TaierSQLException e) {
                // Ignore
            }
        };
        Thread t1 = new Thread(closeTask1);
        Thread t2 = new Thread(closeTask2);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        assertSame(operation1.getStatus().getState(), OperationState.CLOSED);
        assertSame(operation2.getStatus().getState(), OperationState.CLOSED);
    }

    @Test
    public void testCloseAfterTimeout() throws InterruptedException, TaierSQLException {
        Operation operation = new NoopOperation(session);
        OperationHandle handle = operation.getHandle();
        operationManager.addOperation(operation);
        List<OperationHandle> handles = new ArrayList<>();
        handles.add(handle);
        operationManager.removeExpiredOperations(handles);
        Thread.sleep(1000); // Simulate time passing
        assertTrue(operation.isTimedOut());
        assertThrows(TaierSQLException.class, () -> operationManager.closeOperation(handle));
    }

    @Test
    public void testConnectionClosedAfterOperationClose() throws TaierSQLException {
        Operation operation = new NoopOperation(session);
        OperationHandle handle = operation.getHandle();
        operationManager.addOperation(operation);
        operationManager.closeOperation(handle);
        verify(session, times(1)).close();
    }

    @Test
    public void testOperationRemovedFromHashMapAfterClose() throws TaierSQLException {
        Operation operation = new NoopOperation(session);
        OperationHandle handle = operation.getHandle();
        operationManager.addOperation(operation);
        operationManager.closeOperation(handle);
        assertThrows(TaierSQLException.class, () -> operationManager.getOperation(handle));
    }
}