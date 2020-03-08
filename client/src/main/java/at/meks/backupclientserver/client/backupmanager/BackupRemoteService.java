package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.ClientBackupException;
import at.meks.backupclientserver.client.ServerStatusService;
import at.meks.backupclientserver.client.SystemService;
import at.meks.backupclientserver.client.http.HttpUrlResolver;
import at.meks.backupclientserver.client.http.JsonHttpClient;
import at.meks.backupclientserver.common.Md5CheckSumGenerator;
import at.meks.backupclientserver.common.service.BackupCommandArgs;
import at.meks.backupclientserver.common.service.fileup2date.FileInputArgs;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

@Singleton
class BackupRemoteService {

    @Inject
    private JsonHttpClient jsonHttpClient;

    @Inject
    private HttpUrlResolver urlResolver;

    @Inject
    private SystemService systemService;

    @Inject
    private ServerStatusService serverStatusService;

    private final Md5CheckSumGenerator md5CheckSumGenerator = new Md5CheckSumGenerator();

    void backupFile(Path backupSetPath, Path changedFile) {
        try {
            String uploadedFile = jsonHttpClient.post(urlResolver.getWebserviceUrl("file"),
                    Files.newInputStream(changedFile), String.class);
            jsonHttpClient.post(urlResolver.getWebserviceUrl("backup"), new BackupCommandArgs(uploadedFile,
                    systemService.getHostname(), getRelativePathAsStringArray(backupSetPath, changedFile),
                    backupSetPath.toString(), changedFile.toFile().getName()), Void.TYPE);
        } catch (Exception e) {
            if (ExceptionUtils.getRootCause(e) instanceof ConnectException) {
                serverStatusService.setServerAvailable(false);
            }
            throw new ClientBackupException("couldn't backup file " + changedFile, e);
        }
    }

    boolean isFileUpToDate(Path backupSetPath, Path file) {
        return jsonHttpClient.post(
                getBackupMethodUrl("isFileUpToDate"),
                getFileUp2DateRequestInput(backupSetPath, file),
                FileUp2dateResult.class)
                .isUp2date();
    }

    private FileUp2dateInput getFileUp2DateRequestInput(Path backupSetPath, Path file)  {
        return FileUp2dateInput.builder()
                .md5Checksum(md5CheckSumGenerator.md5HexFor(file.toFile()))
                .backupedPath(backupSetPath.toString())
                .relativePath(getRelativePathAsStringArray(backupSetPath, file))
                .fileName(file.getFileName().toString())
                .hostName(systemService.getHostname())
                .build();
    }

    private FileInputArgs createFileInputArgs(Path backupSetPath, Path file) {
        return FileInputArgs.aFileInputArgs().backupedPath(backupSetPath.toString())
                .relativePath(getRelativePathAsStringArray(backupSetPath, file))
                .fileName(file.toFile().getName())
                .hostName(systemService.getHostname()).build();
    }

    private String[] getRelativePathAsStringArray(Path backupSetPath, Path file) {
        return StreamSupport.stream(backupSetPath.relativize(file.getParent()).spliterator(), false)
                .map(Path::toString)
                .toArray(String[]::new);
    }

    private String getBackupMethodUrl(String method) {
        return urlResolver.getWebserviceUrl("backup", method);
    }

    void delete(Path backupSetPath, Path file) {
        FileInputArgs fileInputArgs = createFileInputArgs(backupSetPath, file);
        jsonHttpClient.delete(getBackupMethodUrl("delete"), fileInputArgs);
    }
}
