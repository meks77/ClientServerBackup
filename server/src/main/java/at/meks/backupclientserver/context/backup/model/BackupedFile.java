package at.meks.backupclientserver.context.backup.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

import static at.meks.validation.args.ArgValidator.validate;
import static java.lang.String.join;
import static java.lang.String.valueOf;
@Builder(builderMethodName = "aPersistedEntity")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BackupedFile {

    private String id;

    private Client client;

    private Directory containingDirectory;

    private String fileName;

    private final LinkedList<Version> versions;

    private final LinkedList<ZonedDateTime> deletedTimestamps;

    private BackupedFile() {
        versions = new LinkedList<>();
        deletedTimestamps = new LinkedList<>();
    }

    public static String getIdFor(Client client, Directory containingDirectory, String fileName) {
        return join("-",
                client.getId(), valueOf(containingDirectory.hashCode()), valueOf(fileName.hashCode()));
    }

    public static BackupedFile backupNewFile(DirectoryConfig directoryConfig, FileSystem fileSystem, Client client,
                                             Directory containingDirectory, String fileName,
                                             ZonedDateTime backupTime, InputStream fileContent) {
        BackupedFile backupedFile = new BackupedFile();
        backupedFile.id = getIdFor(client, containingDirectory, fileName);
        backupedFile.client = client;
        backupedFile.containingDirectory = containingDirectory;
        backupedFile.fileName = fileName;

        backupedFile.addVersion(directoryConfig, fileSystem, backupTime, fileContent);
        return backupedFile;
    }

    private Path getPathToContentFile(DirectoryConfig directoryConfig, ZonedDateTime backupTime) {
        return directoryConfig.getBackupRootDirectory()
                    .resolve(client.getId())
                    .resolve(StringUtils.strip(containingDirectory.getClientPath().toString(), "/"))
                    .resolve(fileName)
                    .resolve(backupTime.format(DateTimeFormatter.ofPattern("yyyyddMM_HHmmss.n")));
    }

    @SneakyThrows(IOException.class)
    private void addVersion(DirectoryConfig directoryConfig, FileSystem fileSystem, ZonedDateTime backupTime, InputStream fileContent) {
        Path pathToContent = getPathToContentFile(directoryConfig, backupTime);
        try {
            fileSystem.writeToFile(pathToContent, fileContent);
            if (!versions.isEmpty() && !isDeleted()) {
                String checksumOfCurrentVersion = latestMd5Hex();
                String checksumOfNewVersion = md5HexFor(pathToContent);
                validate().that(checksumOfNewVersion)
                        .withMessage(() -> "file content didn't change")
                        .isNotEqualTo(checksumOfCurrentVersion);
            }
            versions.add(new Version(versions.size() + 1, backupTime, pathToContent, md5HexFor(pathToContent)));
        } catch (IllegalArgumentException e) {
            Files.deleteIfExists(pathToContent);
            throw e;
        }
    }

    public String latestMd5Hex() {
        return versions.getLast().getCheckSum();
    }

    private boolean isDeleted() {
        if (deletedTimestamps.isEmpty()) {
            return false;
        }
        return deletedTimestamps.getLast().isAfter(versions.getLast().getTimestampOfBackup());
    }

    public void updateBackupedFile(DirectoryConfig directoryConfig, FileSystem fileSystem, ZonedDateTime backupTime, InputStream fileContent) {
        addVersion(directoryConfig, fileSystem, backupTime, fileContent);
    }

    @SneakyThrows(IOException.class)
    private String md5HexFor(Path file) {
        try (InputStream fis = Files.newInputStream(file, StandardOpenOption.READ)) {
            return DigestUtils.md5Hex(fis);
        }
    }

    public void markDeleted(ZonedDateTime timestamp) {
        deletedTimestamps.add(timestamp);
    }

    public String id() {
        return id;
    }

    public Client client() {
        return client;
    }

    public Directory containingDirectory() {
        return containingDirectory;
    }

    public String fileName() {
        return fileName;
    }

    public LinkedList<Version> versions() {
        return versions;
    }

    public LinkedList<ZonedDateTime> deletedTimestamps() {
        return deletedTimestamps;
    }
}
