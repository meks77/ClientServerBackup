package at.meks.backupclientserver.client;

import com.google.inject.Singleton;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class ServerStatusService {

    private AtomicBoolean serverAvailable = new AtomicBoolean(false);

    private ReentrantLock lock = new ReentrantLock();
    private Condition serverAvailableCondition = lock.newCondition();


    public void setServerAvailable(boolean available) {
        boolean oldaAlue = serverAvailable.getAndSet(available);
        if (oldaAlue != available) {
            lock.lock();
            try {
                serverAvailableCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    public <T> T runWhenServerIsAvailable(Callable<T> runnable) {
        lock.lock();
        try {
            while(!serverAvailable.get()){
                serverAvailableCondition.await();
            }
            return runnable.call();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new ClientBackupException(e);
        } finally {
            lock.unlock();
        }
        return null;
    }
}
