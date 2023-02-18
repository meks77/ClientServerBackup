package at.meks.backup.client.application;

import lombok.Value;
import lombok.experimental.Accessors;

import java.nio.file.Path;

@Value
@Accessors(fluent = true, chain = true)
public class FileNeedsBackupEvent {

    Path file;

}
