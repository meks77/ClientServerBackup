package at.meks.backupclientserver.context.backup.adapter.filesystem;

import at.meks.backupclientserver.context.backup.model.FileSystem;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Named
@ApplicationScoped
public class FileSystemService implements FileSystem {

    @SneakyThrows(IOException.class)
    @Override
    public void writeToFile(Path targetFile, InputStream fileContent) {
        Files.createDirectories(targetFile.getParent());
        Files.createFile(targetFile);
        OutputStream outputStream = Files.newOutputStream(targetFile);
        IOUtils.copy(fileContent, outputStream);
    }

    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }
}
