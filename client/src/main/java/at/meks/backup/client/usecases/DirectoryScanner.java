package at.meks.backup.client.usecases;

import at.meks.backup.client.model.Config;
import at.meks.backup.client.model.DirectoryForBackup;
import at.meks.backup.client.model.ScanDirectoryCommandListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DirectoryScanner implements ScanDirectoryCommandListener {

    private final Config config;
    private final BackupEachFileScanner backupEachFileScanner;
    private final FileChangeListener fileChangeListener;

    public DirectoryScanner(Config config, BackupEachFileScanner backupEachFileScanner, FileChangeListener fileChangeListener) {
        this.config = config;
        this.backupEachFileScanner = backupEachFileScanner;
        this.fileChangeListener = fileChangeListener;
    }

    @Override
    public void scanDirectories() {
        log.info("start scanning directories");
        for (DirectoryForBackup folder : config.backupedDirectories()) {
            log.info("start scanning folder " + folder.file());
            fileChangeListener.listenToChangesAsync(folder);
            backupEachFileScanner.fireChangedEventForEachFileAsync(folder);
        }
    }

}
