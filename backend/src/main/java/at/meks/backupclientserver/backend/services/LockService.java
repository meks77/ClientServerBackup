package at.meks.backupclientserver.backend.services;

import org.glassfish.jersey.internal.guava.CacheBuilder;
import org.glassfish.jersey.internal.guava.CacheLoader;
import org.glassfish.jersey.internal.guava.LoadingCache;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LockService {

    private LoadingCache<String, ReentrantLock> clientLocks = CacheBuilder.newBuilder().build(
            new CacheLoader<String, ReentrantLock>() {
                @Override
                public ReentrantLock load(String hostName) {
                    return new ReentrantLock();
                }
            });

    ReentrantLock getLockForPath(@NotNull Path path) {
        try {
            return clientLocks.get(path.toString());
        } catch (ExecutionException e) {
            throw new ServerBackupException("couldn't get lock from cache", e);
        }
    }

    public <R> R runWithLock(@NotNull ReentrantLock lock, @NotNull Supplier<R> callable) {
        lock.lock();
        try {
            return callable.get();
        } finally {
            lock.unlock();
        }
    }
}
