package at.meks.backupclientserver.context.backup.model;

import java.io.InputStream;
import java.nio.file.Path;

public interface FileSystem {

    void writeToFile(Path targetFile, InputStream fileContent);

}
