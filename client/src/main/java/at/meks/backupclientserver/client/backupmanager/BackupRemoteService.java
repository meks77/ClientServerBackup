package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.ApplicationConfig;
import at.meks.backupclientserver.client.ClientBackupException;
import at.meks.backupclientserver.common.Md5CheckSumGenerator;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import com.google.inject.Inject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

class BackupRemoteService {

    @Inject
    private ApplicationConfig config;

    @Inject
    private JsonHttpClient jsonHttpClient;

    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    private final Md5CheckSumGenerator md5CheckSumGenerator = new Md5CheckSumGenerator();

    void backupFile(Path backupSetPath, Path changedFile) {
        try {
            HttpEntity httpEntity =
                    MultipartEntityBuilder.create()
                            .addTextBody("relativePath", getRelativePath(backupSetPath, changedFile), ContentType.TEXT_PLAIN.withCharset("utf8"))
                            .addTextBody("hostName", InetAddress.getLocalHost().getHostName(), ContentType.TEXT_PLAIN.withCharset("utf8"))
                            .addTextBody("backupedPath", backupSetPath.toString(), ContentType.TEXT_PLAIN.withCharset("utf8"))
                            .addBinaryBody("file", changedFile.toFile(),
                                    ContentType.APPLICATION_OCTET_STREAM, changedFile.toFile().getName())
                            .build();
            HttpPost httpPost = new HttpPost(getBackupMethodUrl("file"));
            httpPost.setEntity(httpEntity);
            httpClient.execute(httpPost);
        } catch (Exception e) {
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
        input.setBackupedPath(backupSetPath.toString());
        input.setRelativePath(getRelativePathAsStringArray(backupSetPath, file));
        input.setFileName(file.toFile().getName());
        try {
            input.setHostName(InetAddress.getLocalHost().getHostName());
            input.setMd5Checksum(md5CheckSumGenerator.md5HexFor(file.toFile()));
        } catch (IOException e) {
            throw new ClientBackupException("error while preparing the json input hostname and md5 checksum", e);
        }
        return input;
    }

    private String[] getRelativePathAsStringArray(Path backupSetPath, Path file) {
        return StreamSupport.stream(backupSetPath.relativize(file.getParent()).spliterator(), false)
                .map(Path::toString)
                .toArray(String[]::new);
    }

    private String getBackupMethodUrl(String method) {
        return "http://" + config.getServerHost() + ":" + config.getServerPort() + "/api/v1.0/backup/" + method;
    }

}
