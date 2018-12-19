package at.meks.backupclientserver.backend.services;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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

    public ReentrantLock getLockForPath(@NotNull Path path) {
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
