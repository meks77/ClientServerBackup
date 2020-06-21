package at.meks.backupclientserver.context.backup.model;

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

public class BackupedFile {

    private String id;

    private Client client;

    private Directory containingDirectory;

    private String fileName;

    private final LinkedList<Version> versions = new LinkedList<>();

    private final LinkedList<ZonedDateTime> deletedTimestamps = new LinkedList<>();

    private BackupedFile() {

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
        if (versions.isEmpty()) {
            validate().that(pathToContent.getParent())
                    .withMessage(() -> "directory " + pathToContent.getParent() + " mustn't exist.")
                    .matches(value -> !fileSystem.exists(value));
        }
        try {
            fileSystem.writeToFile(pathToContent, fileContent);
            if (!versions.isEmpty() && !isDeleted()) {
                String checksumOfCurrentVersion = versions.getLast().getCheckSum();
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

    public boolean isCurrentChecksumEqualTo(String md5Checksum) {
        return versions.getLast().getCheckSum().equals(md5Checksum);
    }

    public void markDeleted(ZonedDateTime timestamp) {
        deletedTimestamps.add(timestamp);
    }

    public String id() {
        return id;
    }
}
