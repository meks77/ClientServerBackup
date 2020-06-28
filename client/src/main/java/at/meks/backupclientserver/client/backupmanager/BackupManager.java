package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.excludes.FileExcludeService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Singleton
@Slf4j
public class BackupManager {

    private BlockingDeque<TodoEntry> backupQueue = new LinkedBlockingDeque<>(100);

    private Thread queueReaderThread;

    @Inject
    BackupService backupService;

    @Inject
    ErrorReporter errorReporter;

    @Inject
    FileExcludeService fileExcludeService;

    @PostConstruct
    private void start() {
        if (queueReaderThread == null || !queueReaderThread.isAlive()) {
            queueReaderThread = new Thread(this::backupQueueItems);
            queueReaderThread.setName("backupQueueReader");
            queueReaderThread.setDaemon(true);
            queueReaderThread.start();
        }
    }

    private void backupQueueItems() {
        try {
            do {
                backup(backupQueue.take());
            } while (true);
        } catch (InterruptedException e) {
            errorReporter.reportError("listening for Backup items was interrupted", e);
            Thread.currentThread().interrupt();

        }
    }

    private void backup(TodoEntry item) {
        log.debug("backup {}", item);
        try {
            if (item.getChangedFile().toFile().isFile() &&
                    item.getType() != PathChangeType.DELETED &&
                    !isFileUpToDate(item)) {
                backupFile(item);
            } else if (item.getType() == PathChangeType.DELETED) {
                deleteBackupedFile(item);
            }
        } catch (Exception e) {
            errorReporter.reportError("error while backing up file " + item.getChangedFile(), e);
        }
    }

    private boolean isFileUpToDate(TodoEntry item) {
        boolean fileUpToDate = backupService.isFileUpToDate(item.getChangedFile());
        log.debug("the file {} is update2date: {}", item.getChangedFile(), fileUpToDate);
        return fileUpToDate;
    }

    private void backupFile(TodoEntry item) {
        log.debug("backup file {}", item.getChangedFile());
        backupService.backupFile(item.getChangedFile());
    }

    private void deleteBackupedFile(TodoEntry item) {
        log.debug("delete file {}", item.getChangedFile());
        backupService.delete(item.getChangedFile());
    }

    public void addForBackup(TodoEntry item) {
        if (!fileExcludeService.isFileExcludedFromBackup(item.getChangedFile())) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("adding for backup: %s", item.getChangedFile().toString()));
            }
            try {
                backupQueue.put(item);
            } catch (InterruptedException e) {
                errorReporter.reportError("adding backup item " + item + " to queue was interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
