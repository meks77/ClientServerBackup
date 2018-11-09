package at.meks.backupclientserver.client.filechangehandler;

import at.meks.backupclientserver.client.ClientBackupException;
import at.meks.backupclientserver.client.backupmanager.BackupManager;
import at.meks.backupclientserver.client.backupmanager.PathChangeType;
import at.meks.backupclientserver.client.backupmanager.TodoEntry;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.locks.ReentrantLock;

public class FileChangeHandlerImpl implements FileChangeHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Set<Path> changedPathSet = new HashSet<>();

    private ReentrantLock changedPathSetLock = new ReentrantLock();

    private DelayQueue<DelayedFileChange> delyedQueue = new DelayQueue<>();

    private Thread queueReadThread;

    private ReentrantLock queueReadThreadStartLock = new ReentrantLock();

    @Inject
    private BackupManager backupManager;

    @Override
    public void fileChanged(Path watchedRootPath, WatchEvent.Kind kind, Path changedFile) {
        startQueueReaderIfNecessary();
        if (!isFileAlreadyInQueue(changedFile)) {
            PathChangeType pathChangeType = PathChangeType.from(kind);
            if (pathChangeType == null) {
                logger.error("unknown WatchEvent.Kind {}", kind);
                return;
            }
            delyedQueue.put(new DelayedFileChange(new TodoEntry(pathChangeType, changedFile, watchedRootPath)));
        }
    }

    private void startQueueReaderIfNecessary() {
        queueReadThreadStartLock.lock();
        try {
            if (queueReadThread == null) {
                queueReadThread = new Thread(this::readQueue);
                queueReadThread.setDaemon(true);
                queueReadThread.start();
            }
        } finally {
            queueReadThreadStartLock.unlock();
        }
    }

    private boolean isFileAlreadyInQueue(Path changedFile) {
        changedPathSetLock.lock();
        try {
            if (changedPathSet.contains(changedFile)) {
                return true;
            } else {
                changedPathSet.add(changedFile);
                return false;
            }
        } finally {
            changedPathSetLock.unlock();
        }
    }

    private void readQueue() {
        try {
            //noinspection InfiniteLoopStatement
            do {
                DelayedFileChange delayedFileChange = delyedQueue.take();
                TodoEntry todoEntry = delayedFileChange.getTodoEntry();
                if (todoEntry.getType() != PathChangeType.DELETED) {
                    FileTime lastModifiedTime = Files.getLastModifiedTime(todoEntry.getChangedFile());
                    long now = System.currentTimeMillis();
                    if (lastModifiedTime.toMillis() <= (now - delayedFileChange.getDelayInMilliseconds())) {
                        FileChannel fileLock = tryLock(todoEntry.getChangedFile());
                        if (fileLock == null) {
                            queueForLater(todoEntry);
                        } else {
                            try {
                                logger.info("addForBackup {}", todoEntry.getChangedFile());
                                removeFromChangedPathSet(todoEntry);
                                backupManager.addForBackup(new TodoEntry(todoEntry.getType(), todoEntry.getChangedFile(), todoEntry.getWatchedPath()));
                            } finally {
                                IOUtils.closeQuietly(fileLock);
                            }
                        }
                    } else {
                        queueForLater(todoEntry);
                    }
                }
            } while (true);
        } catch (InterruptedException e) {
            logger.info("delayedQueueReader has been interrupted", e);
        } catch (IOException e) {
            throw new ClientBackupException("error while trying to backup file", e);
        }
    }

    private void queueForLater(TodoEntry todoEntry) {
        logger.info("delay for further {} ms: {}", DelayedFileChange.DELAY_IN_MILLISECONDS,
                todoEntry.getChangedFile());
        delyedQueue.put(new DelayedFileChange(todoEntry));
    }

    private FileChannel tryLock(Path changedFile) {
        try {
            return FileChannel.open(changedFile, StandardOpenOption.READ);
        } catch (Exception e) {
            return null;
        }
    }

    private void removeFromChangedPathSet(TodoEntry todoEntry) {
        changedPathSetLock.lock();
        try {
            changedPathSet.remove(todoEntry.getChangedFile());
        } finally {
            changedPathSetLock.unlock();
        }
    }


}
