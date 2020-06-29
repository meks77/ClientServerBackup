package at.meks.backupclientserver.context.backup.adapter.persistence;

import at.meks.backupclientserver.context.backup.model.BackupedFile;
import at.meks.backupclientserver.context.backup.model.Client;
import at.meks.backupclientserver.context.backup.model.Directory;
import at.meks.backupclientserver.context.backup.model.Version;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Collectors;

@Singleton
class Translator {

    BackupedFileEntity toNewEntity(BackupedFile backupedFile) {
        BackupedFileEntity fileEntity = new BackupedFileEntity();
        fileEntity.id = backupedFile.id();
        fileEntity.clientId = backupedFile.client().getId();
        fileEntity.containingDirectory = backupedFile.containingDirectory().getClientPath().toString();
        fileEntity.fileName = backupedFile.fileName();
        fileEntity.versions = new ArrayList<>();
        backupedFile.versions().forEach(version -> fileEntity.versions.add(toNewEntity(version, fileEntity)));
        return fileEntity;
    }

    VersionEntity toNewEntity(Version version, BackupedFileEntity backupedFile) {
        VersionEntity entity = new VersionEntity();
        entity.backupedFile = backupedFile;
        entity.checkSum = version.getCheckSum();
        entity.relativePathToContent = version.getRelativePathToContent().toString();
        entity.timestampOfBackup = version.getTimestampOfBackup();
        entity.version = version.getVersionIndex();
        return entity;
    }

    DeletionDate toNewEntity(ZonedDateTime deletionTime, BackupedFileEntity backupedFile) {
        DeletionDate entity = new DeletionDate();
        entity.deletionTime = deletionTime;
        entity.backupedFile = backupedFile;
        return entity;
    }

    BackupedFile toDomain(BackupedFileEntity entity) {
        return BackupedFile.aPersistedEntity()
                .client(new Client(entity.clientId))
                .containingDirectory(new Directory(Path.of(entity.containingDirectory)))
                .fileName(entity.fileName)
                .id(entity.id)
                .versions(new LinkedList<>(entity.versions.stream()
                        .map(this::toDomain).collect(Collectors.toList())))
                .deletedTimestamps(new LinkedList<>(entity.deletedTimestamps.stream()
                        .map(this::toDomain).collect(Collectors.toList())))
                .build();
    }

    private Version toDomain(VersionEntity entity) {
        return new Version(entity.version, entity.timestampOfBackup,
                Path.of(entity.relativePathToContent), entity.checkSum);
    }

    private ZonedDateTime toDomain(DeletionDate entity) {
        return entity.deletionTime;
    }

}
