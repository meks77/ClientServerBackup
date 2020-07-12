package at.meks.backupclientserver.client.backup.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


class BackupCandidateTest {

    private Path file;

    @BeforeEach
    void initFile(@TempDir Path tempDir) throws IOException {
        file = Files.createFile(tempDir.resolve("testFile.txt"));
    }

    @Test
    void givenDifferentRemoteHexWhenIsBackupNeeded(@TempDir Path tempDir) throws IOException {
        BackupCandidate candidate = new BackupCandidate(new Client("clientId"), file, EventType.MODIFIED, "remoteHex");

        assertTrue(candidate.isBackupNeeded());
    }

    @Test
    void givenSameRemoteHexWhenIsBackupNeeded(@TempDir Path tempDir) throws IOException {
        String remoteHex = DigestUtils.md5Hex(Files.newInputStream(file));

        BackupCandidate candidate = new BackupCandidate(new Client("clientId"), file, EventType.MODIFIED, remoteHex);

        assertFalse(candidate.isBackupNeeded());
    }

    @Test
    void client() {
        Client expectedClient = new Client("clientId");
        BackupCandidate candidate = new BackupCandidate(expectedClient, file, EventType.MODIFIED, "remoteHex");
        assertEquals(expectedClient, candidate.client());
    }

    @Test
    void file() {
        BackupCandidate candidate = new BackupCandidate(new Client("clientId"), file, EventType.MODIFIED, "remoteHex");
        assertEquals(file, candidate.file());
    }

}