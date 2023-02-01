package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.application.html.SomePage;
import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class BackupedFileServiceTest {

    @InjectMocks BackupedFileService service;

    @Mock BackupedFileRepository fileRepository;

    @Test void backupNewFile() {
        byte[] bytes = new byte[5];
        FileId fileId = FileId.idFor(new ClientId("client1"), new PathOnClient(Path.of("whatever")));
        Mockito.when(fileRepository.get(fileId))
                        .thenReturn(Optional.empty());

        service.backup(fileId, bytes);

        Mockito.verify(fileRepository)
                .save(BackupedFile.newFileForBackup(fileId));
    }

}
