package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.ClientBackupException;
import at.meks.backupclientserver.client.ServerStatusService;
import at.meks.backupclientserver.client.SystemService;
import at.meks.backupclientserver.common.Md5CheckSumGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Singleton
class BackupService {

    @Inject
    SystemService systemService;

    @Inject
    ServerStatusService serverStatusService;

    @Inject
    @RestClient
    RemoteBackupService remoteBackupService;

    private Md5CheckSumGenerator md5CheckSumGenerator = new Md5CheckSumGenerator();

    void backupFile(Path changedFile) {
        try (InputStream fileInputStream = Files.newInputStream(changedFile)){
            Response response = remoteBackupService.backupFile(systemService.getHostname(),
                    encodeString(changedFile.getParent()), encodeString(changedFile.getFileName()), fileInputStream);
            if (response.getStatus() != HttpStatus.SC_NO_CONTENT) {
                throw new IllegalStateException("http backup returned with " + response.getStatus() +
                        " \nmessage: " + response.readEntity(String.class));
            }
        } catch (Exception e) {
            if (ExceptionUtils.getRootCause(e) instanceof ConnectException) {
                serverStatusService.setServerAvailable(false);
            }
            throw new ClientBackupException("couldn't backup file " + changedFile, e);
        }
    }

    private String encodeString(Path parent) {
        return URLEncoder.encode(parent.toString(), StandardCharsets.UTF_8);
    }

    boolean isFileUpToDate(Path file) {
        return remoteBackupService.isFileUp2date(
                systemService.getHostname(), encodeString(file.getParent()), encodeString(file.getFileName()),
                md5CheckSumGenerator.md5HexFor(file.toFile())).isUp2date();
    }

    void delete(Path file) {
        remoteBackupService.deletePath(
                systemService.getHostname(), encodeString(file.getParent()), encodeString(file.getFileName()));
    }

}