package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.ApplicationConfig;
import at.meks.backupclientserver.client.ClientBackupException;
import at.meks.backupclientserver.common.Md5CheckSumGenerator;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;

class BackupRemoteService {

    @Inject
    private ApplicationConfig config;

    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    private final Md5CheckSumGenerator md5CheckSumGenerator = new Md5CheckSumGenerator();

    void backupFile(Path backupSetPath, Path changedFile) {
        try {
            HttpEntity httpEntity =
                    MultipartEntityBuilder.create()
                            .addTextBody("relativePath", getRelativePath(backupSetPath, changedFile), ContentType.TEXT_PLAIN)
                            .addTextBody("hostName", InetAddress.getLocalHost().getHostName(), ContentType.TEXT_PLAIN)
                            .addTextBody("backupedPath", backupSetPath.toString(), ContentType.TEXT_PLAIN)
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
        return backupSetPath.relativize(changedFile.getParent()).toString();
    }

    boolean isFileUpToDate(Path backupSetPath, Path file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            FileUp2dateInput input = getFileUp2DateRequestInput(backupSetPath, file);
            String inputJson = mapper.writeValueAsString(input);
            Client client = Client.create();
            WebResource webResource = client.resource(getBackupMethodUrl("isFileUpToDate"));
            String result = webResource.type("application/json").post(String.class, inputJson);
            if (Strings.isNullOrEmpty(result)) {
                return false;
            }
            FileUp2dateResult deserializedResult = mapper.readValue(result, FileUp2dateResult.class);
            return deserializedResult.isUp2date();
        } catch (IOException e) {
            throw new ClientBackupException("error while asking if file is up2date", e);
        }
    }

    private FileUp2dateInput getFileUp2DateRequestInput(Path backupSetPath, Path file) throws IOException {
        FileUp2dateInput input = new FileUp2dateInput();
        input.setBackupedPath(backupSetPath.toString());
        input.setRelativePath(backupSetPath.relativize(file.getParent()).toString());
        input.setFileName(file.toFile().getName());
        input.setHostName(InetAddress.getLocalHost().getHostName());
        input.setMd5Checksum(md5CheckSumGenerator.md5HexFor(file.toFile()));
        return input;
    }

    private String getBackupMethodUrl(String method) {
        return "http://" + config.getServerHost() + ":" + config.getServerPort() + "/api/v1.0/backup/" + method;
    }


}
