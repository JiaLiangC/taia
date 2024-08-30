package com.dtstack.taier.datasource.plugin.common.session;

import com.dtstack.taier.datasource.plugin.common.config.TaierConf;
import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.jdbc.JdbcSessionManager;
import com.dtstack.taier.datasource.plugin.common.jdbc.TaierJdbcUtils;
import com.dtstack.taier.datasource.plugin.common.jdbc.operation.JdbcOperationManager;
import com.dtstack.taier.datasource.plugin.common.operation.OperationManager;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import com.dtstack.taier.datasource.plugin.common.operation.*;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;
import org.mockito.MockedStatic;

public class TestSessionManager {


    private SessionManager sessionManager;
    private OperationManager operationManager;
    @Mock
    private Connection mockConnection;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TaierConf tconf = new TaierConf().loadFileDefaults();
        //operationManager = new NoopOperationManager();
        //sessionManager = new NoopSessionManager();
        sessionManager = new JdbcSessionManager();
        sessionManager.initialize(tconf);
        sessionManager.start();
    }

    @Test
    public void testCloseSessionEnsuresConnectionClosed() throws SQLException {
        try (MockedStatic<TaierJdbcUtils> mockedStatic = Mockito.mockStatic(TaierJdbcUtils.class)) {
            mockedStatic.when(() -> TaierJdbcUtils.initializeJdbcSession(any(Connection.class), anyList()))
                    .thenAnswer(invocation -> null);

            SessionHandle handle = sessionManager.openSession(mockConnection, "user", "password", "127.0.0.1", new HashMap<>());
            sessionManager.closeSession(handle);
            verify(mockConnection, times(1)).close();
        }
    }


    @Test
    public void testSessionTimeoutAutoClose() throws TaierSQLException, InterruptedException {
        operationManager = new NoopOperationManager();
        sessionManager = new NoopSessionManager();
        TaierConf tconf = new TaierConf().loadFileDefaults();
        tconf.set("taier.session.idle.timeout","PT10S");
        tconf.set("taier.session.check.interval","PT4S");

        sessionManager.initialize(tconf);
        sessionManager.start();

        Map<String, String> conf = new HashMap<>();
        conf.put("session.timeout", "1");
        SessionHandle handle = sessionManager.openSession(null, "user", "password", "127.0.0.1", conf);
        Session session = sessionManager.getSession(handle);
        assert ((Collection<?>) sessionManager.allSessions()).size() == 1;
        for(int i=0;i<8;i++){
            System.out.println("session id:"+session.getHandle().getIdentifier().toString());
            Thread.sleep(2000); // Wait for timeout
            System.out.println("create time:"+session.getCreateTime());
            System.out.println("LastAccessTime:"+session.getLastAccessTime());
            System.out.println("LastIdleTime:"+session.getLastIdleTime());
            System.out.println("NoOperationTime:"+session.getNoOperationTime());
            System.out.println("IdleTimeoutThreshold:"+session.getSessionIdleTimeoutThreshold());
            System.out.println("\n");
        }
        assert ((Collection<?>) sessionManager.allSessions()).isEmpty();
    }

    @Test
    public void testConcurrentCloseSameSession() throws InterruptedException, SQLException {
        Session session = sessionManager.createSession(mockConnection, "user", "password", "127.0.0.1", new HashMap<>());
        SessionHandle handle = session.getHandle();
        CountDownLatch latch = new CountDownLatch(2);

        Runnable closeTask = () -> {
            try {
                sessionManager.closeSession(handle);
            } catch (TaierSQLException e) {
                // Ignore
            } finally {
                latch.countDown();
            }
        };

        new Thread(closeTask).start();
        new Thread(closeTask).start();

        latch.await();
        verify(mockConnection, times(1)).close();
    }

    @Test
    public void testConcurrentCloseDifferentSessions() throws InterruptedException, SQLException {
        Session session1 = sessionManager.createSession(mockConnection, "user1", "password1", "127.0.0.1", new HashMap<>());
        Session session2 = sessionManager.createSession(mockConnection, "user2", "password2", "127.0.0.1", new HashMap<>());
        CountDownLatch latch = new CountDownLatch(2);

        Runnable closeTask1 = () -> {
            try {
                sessionManager.closeSession(session1.getHandle());
            } catch (TaierSQLException e) {
                // Ignore
            } finally {
                latch.countDown();
            }
        };

        Runnable closeTask2 = () -> {
            try {
                sessionManager.closeSession(session2.getHandle());
            } catch (TaierSQLException e) {
                // Ignore
            } finally {
                latch.countDown();
            }
        };

        new Thread(closeTask1).start();
        new Thread(closeTask2).start();

        latch.await();
        verify(mockConnection, times(2)).close();
    }

    @Test
    public void testConcurrentCloseNonExistentSession() throws InterruptedException {
        SessionHandle handle = SessionHandle.apply();
        CountDownLatch latch = new CountDownLatch(2);

        Runnable closeTask = () -> {
            try {
                sessionManager.closeSession(handle);
                fail("Expected TaierSQLException");
            } catch (TaierSQLException e) {
                // Expected
            } finally {
                latch.countDown();
            }
        };

        new Thread(closeTask).start();
        new Thread(closeTask).start();

        latch.await();
    }

    @Test
    public void testConcurrentCloseAlreadyClosedSession() throws TaierSQLException, InterruptedException {
        Session session = sessionManager.createSession(mockConnection, "user", "password", "127.0.0.1", new HashMap<>());
        SessionHandle handle = session.getHandle();
        sessionManager.closeSession(handle);

        CountDownLatch latch = new CountDownLatch(2);

        Runnable closeTask = () -> {
            try {
                sessionManager.closeSession(handle);
                fail("Expected TaierSQLException");
            } catch (TaierSQLException e) {
                // Expected
            } finally {
                latch.countDown();
            }
        };

        new Thread(closeTask).start();
        new Thread(closeTask).start();

        latch.await();
    }



    @Test
    public void testSessionTimeoutCloseFails() throws TaierSQLException, InterruptedException {
        Map<String, String> conf = new HashMap<>();
        conf.put("session.timeout", "1"); // 1 second timeout
        SessionHandle handle = sessionManager.openSession(null, "user", "password", "127.0.0.1", conf);
        Thread.sleep(2000); // Wait for timeout
        try {
            sessionManager.closeSession(handle);
            fail("Expected TaierSQLException");
        } catch (TaierSQLException e) {
            // Expected
        }
    }

    @Test
    public void testSessionTimeoutExecuteFails() throws TaierSQLException, InterruptedException {
        Map<String, String> conf = new HashMap<>();
        conf.put("session.timeout", "1"); // 1 second timeout
        SessionHandle handle = sessionManager.openSession(null, "user", "password", "127.0.0.1", conf);
        Thread.sleep(2000); // Wait for timeout
        try {
            sessionManager.getSession(handle).executeStatement("SELECT 1",new HashMap<>(),false,10000);
            fail("Expected TaierSQLException");
        } catch (TaierSQLException e) {
            // Expected
        }
    }

    @Test
    public void testSessionTimeoutAutoCloseEnsuresConnectionClosedAndOperationsCancelled() throws SQLException, InterruptedException {
        Map<String, String> conf = new HashMap<>();
        conf.put("session.timeout", "1"); // 1 second timeout
        SessionHandle handle = sessionManager.openSession(null, "user", "password", "127.0.0.1", conf);
        Session session = sessionManager.getSession(handle);
        Operation operation = mock(Operation.class);
        when(operation.getHandle()).thenReturn(OperationHandle.create());
        sessionManager.getOperationManager().addOperation(operation);
        Thread.sleep(2000); // Wait for timeout
        verify(mockConnection, times(1)).close();
        verify(operation, times(1)).cancel();
    }

    @Test
    public void testCloseSessionEnsuresAllOperationsClosed() throws TaierSQLException {
        Session session = sessionManager.createSession(mockConnection, "user", "password", "127.0.0.1", new HashMap<>());
        Operation operation = mock(Operation.class);
        when(operation.getHandle()).thenReturn(OperationHandle.create());
        sessionManager.getOperationManager().addOperation(operation);
        sessionManager.closeSession(session.getHandle());
        verify(operation, times(1)).close();
    }

    @Test
    public void testCloseSessionEnsuresAllOperationsCancelled() throws TaierSQLException {
        Session session = sessionManager.createSession(mockConnection, "user", "password", "127.0.0.1", new HashMap<>());
        Operation operation = mock(Operation.class);
        when(operation.getHandle()).thenReturn(OperationHandle.create());
        sessionManager.getOperationManager().addOperation(operation);
        sessionManager.closeSession(session.getHandle());
        verify(operation, times(1)).cancel();
    }

    @Test
    public void testExecuteStatementWithClosedSessionFails() throws TaierSQLException {
        Session session = sessionManager.createSession(mockConnection, "user", "password", "127.0.0.1", new HashMap<>());
        sessionManager.closeSession(session.getHandle());
        session.executeStatement("SELECT 1", new HashMap<>(), false, 10000);
        fail("Expected TaierSQLException");
    }

/*    @Test
    public void testFetchResultWithClosedSessionFails() throws TaierSQLException {
        Session session = sessionManager.createSession(mockConnection, "user", "password", "127.0.0.1", new HashMap<>());
        sessionManager.closeSession(session.getHandle());
        try {
            sessionManager.getOperationManager().getOperation("").fetchResult();
            fail("Expected TaierSQLException");
        } catch (TaierSQLException e) {
            // Expected
        }
    }*/

    @Test
    public void testCloseOperationWithClosedSessionFails() throws TaierSQLException {
        Session session = sessionManager.createSession(mockConnection, "user", "password", "127.0.0.1", new HashMap<>());
        sessionManager.closeSession(session.getHandle());
        session.closeOperation(OperationHandle.create());
        fail("Expected TaierSQLException");
    }

    @Test
    public void testCancelOperationWithClosedSessionFails() throws TaierSQLException {
        Session session = sessionManager.createSession(mockConnection, "user", "password", "127.0.0.1", new HashMap<>());
        sessionManager.closeSession(session.getHandle());
        session.cancelOperation(OperationHandle.create());
        fail("Expected TaierSQLException");
    }

/*    @Test
    public void testApplyOpActionWithClosedSessionFails() throws TaierSQLException {
        Session session = sessionManager.createSession(mockConnection, "user", "password", "127.0.0.1", new HashMap<>());
        sessionManager.closeSession(session.getHandle());
        try {
            session.applyOpAction(OperationHandle.create(), "action");
            fail("Expected TaierSQLException");
        } catch (TaierSQLException e) {
            // Expected
        }
    }*/

    @Test
    public void testSessionCloseOrTimeoutRemovesFromHashMap() throws TaierSQLException, InterruptedException {
        Map<String, String> conf = new HashMap<>();
        conf.put("session.timeout", "1"); // 1 second timeout
        SessionHandle handle = sessionManager.openSession(null, "user", "password", "127.0.0.1", conf);
        Thread.sleep(2000); // Wait for timeout
        assertNull(sessionManager.getSession(handle));
    }

    @Test
    public void testSessionCloseOrTimeoutRemovesOperationsFromHashMap() throws TaierSQLException, InterruptedException {
        Map<String, String> conf = new HashMap<>();
        conf.put("session.timeout", "1"); // 1 second timeout
        SessionHandle handle = sessionManager.openSession(null, "user", "password", "127.0.0.1", conf);
        Session session = sessionManager.getSession(handle);
        Operation operation = mock(Operation.class);
        when(operation.getHandle()).thenReturn(OperationHandle.create());
        sessionManager.getOperationManager().addOperation(operation);
        Thread.sleep(2000); // Wait for timeout
//        assertTrue(session.getOperations().isEmpty());
    }

}