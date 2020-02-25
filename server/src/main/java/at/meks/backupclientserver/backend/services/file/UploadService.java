package at.meks.backupclientserver.backend.services.file;

import org.apache.commons.io.IOUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Named
@ApplicationScoped
public class UploadService {

    @Inject
    DirectoryService directoryService;

    public Path addNewFile(InputStream inputStream) {
        try {
            final String fileName = UUID.randomUUID().toString();
            final Path filePath = getFilePath(fileName);
            Files.createDirectories(filePath.getParent());
            Files.createFile(filePath);
            appendToFile(filePath, inputStream);
            return directoryService.getUploadDir().relativize(filePath);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Path getFilePath(String fileName) {
        return directoryService.getUploadDir().resolve(fileName);
    }

    private void appendToFile(Path filePath, InputStream is) {
        try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.APPEND)) {
            IOUtils.copy(is, outputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Path getAbsolutePath(String relativePath) {
        return directoryService.getUploadDir().resolve(relativePath);
    }

}
