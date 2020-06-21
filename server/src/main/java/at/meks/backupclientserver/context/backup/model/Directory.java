package at.meks.backupclientserver.context.backup.model;

import lombok.Value;

import java.nio.file.Path;

@Value
public class Directory {

    Path clientPath;

}
