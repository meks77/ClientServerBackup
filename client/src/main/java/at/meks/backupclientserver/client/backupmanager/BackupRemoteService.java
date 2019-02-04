package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.ClientBackupException;
import at.meks.backupclientserver.client.ServerStatusService;
import at.meks.backupclientserver.client.SystemService;
import at.meks.backupclientserver.client.http.HttpUrlResolver;
import at.meks.backupclientserver.client.http.JsonHttpClient;
import at.meks.backupclientserver.common.Md5CheckSumGenerator;
import at.meks.backupclientserver.common.service.fileup2date.FileInputArgs;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.ConnectException;
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

    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    private final Md5CheckSumGenerator md5CheckSumGenerator = new Md5CheckSumGenerator();

    void backupFile(Path backupSetPath, Path changedFile) {
        try {
            ContentType textContentType = ContentType.TEXT_PLAIN.withCharset(Consts.UTF_8);
            HttpEntity httpEntity =
                    MultipartEntityBuilder.create()
                            .addTextBody("relativePath", getRelativePath(backupSetPath, changedFile), textContentType)
                            .addTextBody("hostName", systemService.getHostname(), textContentType)
                            .addTextBody("backupedPath", backupSetPath.toString(), textContentType)
                            .addTextBody("fileName", changedFile.toFile().getName(), textContentType)
                            .addBinaryBody("file", changedFile.toFile(),
                                    ContentType.APPLICATION_OCTET_STREAM, changedFile.toFile().getName())
                            .build();
            HttpPost httpPost = new HttpPost(getBackupMethodUrl("file"));
            httpPost.setEntity(httpEntity);
            serverStatusService.runWhenServerIsAvailable(() -> httpClient.execute(httpPost));
        } catch (Exception e) {
            if (ExceptionUtils.getRootCause(e) instanceof ConnectException) {
                serverStatusService.setServerAvailable(false);
            }
            throw new ClientBackupException("couldn't backup file " + changedFile, e);
        }
    }

    private String getRelativePath(Path backupSetPath, Path changedFile) {
        return String.join(", ", getRelativePathAsStringArray(backupSetPath, changedFile));
    }

    boolean isFileUpToDate(Path backupSetPath, Path file) {
        return jsonHttpClient.post(
                getBackupMethodUrl("isFileUpToDate"),
                getFileUp2DateRequestInput(backupSetPath, file),
                FileUp2dateResult.class)
                .isUp2date();
    }

    private FileUp2dateInput getFileUp2DateRequestInput(Path backupSetPath, Path file)  {
        FileUp2dateInput input = new FileUp2dateInput();
        setFileInputArgsProps(backupSetPath, file, input);
        try {
            input.setMd5Checksum(md5CheckSumGenerator.md5HexFor(file.toFile()));
        } catch (IOException e) {
            throw new ClientBackupException("error while preparing the json input hostname and md5 checksum", e);
        }
        return input;
    }

    private void setFileInputArgsProps(Path backupSetPath, Path file, FileInputArgs input) {
        input.setBackupedPath(backupSetPath.toString());
        input.setRelativePath(getRelativePathAsStringArray(backupSetPath, file));
        input.setFileName(file.toFile().getName());
        input.setHostName(systemService.getHostname());
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
        FileInputArgs fileInputArgs = new FileInputArgs();
        setFileInputArgsProps(backupSetPath, file, fileInputArgs);
        jsonHttpClient.delete(getBackupMethodUrl("delete"), fileInputArgs);
    }
}
