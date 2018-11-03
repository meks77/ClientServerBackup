package at.meks.clientserverbackup.client.backupmanager;

import at.meks.clientserverbackup.client.ApplicationConfig;
import at.meks.clientserverbackup.client.ClientBackupException;
import com.google.inject.Inject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class BackupManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private BlockingDeque<TodoEntry> backupQueue = new LinkedBlockingDeque<>();
    private Thread queueReaderThread;
    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    @Inject
    private ApplicationConfig config;

    @PostConstruct
    private void start() {
        if (queueReaderThread == null || !queueReaderThread.isAlive()) {
            queueReaderThread = new Thread(this::backupQueueItems);
            queueReaderThread.setDaemon(true);
            queueReaderThread.start();
        }
    }

    private void backupQueueItems() {
        try {
            //noinspection InfiniteLoopStatement
            do {
                backup(backupQueue.take());
            } while (true);
        } catch (InterruptedException e) {
            logger.error("listening for Backup items was interrupted", e);
        }
    }

    private void backup(TodoEntry item) {
        logger.info("backup {}", item);
        if (item.getChangedFile().toFile().isFile()) {
            if (item.getType() == PathChangeType.DELETED) {
                deleteFileOnServer(item);
            } else {
                backupFile(item);
            }
        }
        //TODO ask server if backup is necessary
        //TODO send file for backup to server
    }

    private void deleteFileOnServer(TodoEntry item) {
        // TODO delete file on server
    }

    private void backupFile(TodoEntry item) {
        try {
            HttpEntity httpEntity =
                    MultipartEntityBuilder.create()
                            .addTextBody("relativePath", getRelativePath(item), ContentType.TEXT_PLAIN)
                            .addTextBody("hostName", InetAddress.getLocalHost().getHostName(), ContentType.TEXT_PLAIN)
                            .addTextBody("backupedPath", item.getWatchedPath().toString(), ContentType.TEXT_PLAIN)
                            .addBinaryBody("file", item.getChangedFile().toFile(),
            ContentType.APPLICATION_OCTET_STREAM, item.getChangedFile().toFile().getName())
                            .build();
            HttpPost httpPost = new HttpPost("http://" + config.getServerHost() + ":" + config.getServerPort() +
                    "/api/v1.0/backup");
            httpPost.setEntity(httpEntity);
            httpClient.execute(httpPost);
        } catch (Exception e) {
            throw new ClientBackupException("couldn't backup file " + item.getChangedFile(), e);
        }
    }

    private String getRelativePath(TodoEntry item) {
        return item.getWatchedPath().relativize(item.getChangedFile().getParent()).toString();
    }

    public void addForBackup(TodoEntry item) {
        backupQueue.add(item);
    }
}
