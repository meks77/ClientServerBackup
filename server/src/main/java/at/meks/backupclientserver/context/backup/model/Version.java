package at.meks.backupclientserver.context.backup.model;

import lombok.Value;

import java.nio.file.Path;
import java.time.ZonedDateTime;

@Value
public class Version {

    int versionIndex;

    ZonedDateTime timestampOfBackup;

    Path relativePathToContent;

    String checkSum;

}
