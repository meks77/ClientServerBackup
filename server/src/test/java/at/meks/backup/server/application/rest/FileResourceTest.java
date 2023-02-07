package at.meks.backup.server.application.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Produces;

@QuarkusTest
class FileResourceTest {

    private final MemoryFileRespository fileRespository = new MemoryFileRespository();

    @Produces
    MemoryFileRespository fileRepository() {
        return fileRespository;
    }

    @BeforeEach
    void cleanup() {
        fileRespository.clear();
    }

    @Test
    void bla() {
        Assertions.fail("TODO: implement tests");
    }

}