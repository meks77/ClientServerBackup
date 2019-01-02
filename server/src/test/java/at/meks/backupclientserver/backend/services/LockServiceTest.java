package at.meks.backupclientserver.backend.services;

import org.junit.Test;
import org.mockito.InOrder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LockServiceTest {

    private LockService service = new LockService();

    @Test
    public void givenFirstCallWhenGetLockForPathReturnsALock() {
        assertThat(service.getLockForPath(Paths.get("myPath"))).isNotNull().isInstanceOf(ReentrantLock.class);
    }

    @Test
    public void givenManyThreadsWhenGetLockForPathThenAllwaysSameLockIsReturned() {
        Path pathForTest = Paths.get("pathForTest");
        Set<ReentrantLock> retrievedLocks = new HashSet<>();
        Collections.nCopies(60, pathForTest)
                .parallelStream()
                .forEach(path -> retrievedLocks.add(service.getLockForPath(path)));
        assertThat(retrievedLocks).hasSize(1);
    }

    @Test
    public void whenRunWithLockThenLockIsCreatedAndReleased() {
        ReentrantLock reentrantLock = mock(ReentrantLock.class);

        Supplier supplier = mock(Supplier.class);
        service.runWithLock(reentrantLock, supplier);

        InOrder inOrder = inOrder(reentrantLock, supplier, reentrantLock);
        inOrder.verify(reentrantLock).lock();
        inOrder.verify(supplier).get();
        inOrder.verify(reentrantLock).unlock();
    }

    @Test
    public void givenSupplierThrowsExceptionWhenRunWithLockThenLockIsReleased() {
        ReentrantLock reentrantLock = mock(ReentrantLock.class);

        Supplier supplier = mock(Supplier.class);
        when(supplier.get()).thenThrow(new RuntimeException());
        try {
            service.runWithLock(reentrantLock, supplier);
            fail("Exception was expected");
        } catch (RuntimeException re) {
            verify(reentrantLock).unlock();
        }



    }

}
