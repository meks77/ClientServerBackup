package at.meks.backupclientserver.client.filechangehandler;

import at.meks.backupclientserver.client.ClientBackupException;
import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.backupmanager.BackupManager;
import at.meks.backupclientserver.client.backupmanager.PathChangeType;
import at.meks.backupclientserver.client.backupmanager.TodoEntry;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class FileChangeHandlerImpl implements FileChangeHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Set<Path> changedPathSet = new HashSet<>();

    private ReentrantLock changedPathSetLock = new ReentrantLock();

    private DelayQueue<DelayedFileChange> delyedQueue = new DelayQueue<>();

    private Thread queueReadThread;

    private ReentrantLock queueReadThreadStartLock = new ReentrantLock();

    @Inject
    private BackupManager backupManager;

    @Inject
    private ErrorReporter errorReporter;

    @Override
    public void fileChanged(Path watchedRootPath, WatchEvent.Kind kind, Path changedFile) {
        startQueueReaderIfNecessary();
        try {
            if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                delyedQueue.put(createDelayedFileChange(watchedRootPath, changedFile, PathChangeType.DELETED));
            } else if (changedFile.toFile().isFile()) {
                addFileToQueue(watchedRootPath, kind, changedFile);
            } else if (changedFile.toFile().isDirectory()) {
                addDirectoryToQueue(watchedRootPath, kind, changedFile);
            }
        } catch (Exception e) {
            String message = "error while adding file change to queue. backupSetPath: " + watchedRootPath + " kind: " +
                    kind + " changedFile: " + changedFile;
            errorReporter.reportError(message, e);
        }
    }

    private void addDirectoryToQueue(Path watchedRootPath, WatchEvent.Kind kind, Path changedDirectory) throws IOException {
        if (!kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(changedDirectory)) {
                directoryStream.forEach(path -> fileChanged(watchedRootPath, kind, path));
            }
        }
    }

    private void addFileToQueue(Path watchedRootPath, WatchEvent.Kind kind, Path changedFile) {
        if (!isFileAlreadyInQueue(changedFile)) {
            forKind(kind,
                    pathChangeType -> delyedQueue.put(createDelayedFileChange(watchedRootPath, changedFile, pathChangeType)));
        }
    }

    private DelayedFileChange createDelayedFileChange(Path watchedRootPath, Path changedFile, PathChangeType pathChangeType) {
        return new DelayedFileChange(new TodoEntry(pathChangeType, changedFile, watchedRootPath));
    }

    private void forKind(WatchEvent.Kind kind, Consumer<PathChangeType> consumer) {
        PathChangeType pathChangeType = PathChangeType.from(kind);
        if (pathChangeType == null) {
            errorReporter.reportError("unknown WatchEvent.Kind " + kind);
            return;
        }
        consumer.accept(pathChangeType);
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
                } else {
                    backupManager.addForBackup(todoEntry);
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
