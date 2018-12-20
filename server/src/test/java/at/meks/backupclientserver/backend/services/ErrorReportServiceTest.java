package at.meks.backupclientserver.backend.services;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.domain.ErrorLog;
import at.meks.backupclientserver.backend.services.file.DirectoryService;
import at.meks.backupclientserver.backend.services.file.FileService;
import at.meks.backupclientserver.backend.services.persistence.ErrorLogRepository;
import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorReportServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ErrorLogRepository errorLogRepository;

    @Mock
    private DirectoryService directoryService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ErrorReportService service = new ErrorReportService();

    private Path errorsDirectory = TestDirectoryProvider.createTempDirectory();
    private Client client = Client.aClient().name("the host of the client which was processed").build();
    private String message = "a message with detail information";
    private Exception occuredException = new IllegalStateException("just a exception", new IOException("the cause"));
    private Path errorFile;

    @Before
    public void mockDefaults() throws IOException {
        errorFile = Files.createTempFile(errorsDirectory, "ut-error-file", ".txt");
        Files.deleteIfExists(errorFile);
        Mockito.when(directoryService.getErrorDirectory()).thenReturn(errorsDirectory);
        Mockito.when(fileService.createFileWithRandomName(errorsDirectory)).thenReturn(errorFile);
    }

    @After
    public void deleteDir() throws IOException {
        FileUtils.deleteDirectory(errorsDirectory.toFile());
    }

    @Test
    public void givenNullExceptionWhenAddErrorThenNoFileIsWritten() {
        service.addError(client, message, null);

        assertThat(errorFile).doesNotExist();
    }

    @Test
    public void givenExceptionWithCauseWhenAddErrorThenExceptionIsWrittenToFile() {
        service.addError(client, message, occuredException);

        Throwable rootCause = ExceptionUtils.getRootCause(occuredException);

        String expectedFileContent = ExceptionUtils.getMessage(occuredException) + System.lineSeparator() +
                ExceptionUtils.getStackTrace(occuredException) + System.lineSeparator() +
                "causing exception: " +
                ExceptionUtils.getMessage(rootCause) + System.lineSeparator() +
                ExceptionUtils.getStackTrace(rootCause);
        assertThat(errorFile).hasContent(expectedFileContent);
    }

    @Test
    public void givenExceptionWithoutCauseWhenAddErrorThenExceptionIsWrittenToFile() {
        occuredException = new FileNotFoundException("this file wasn't found");
        service.addError(client, message, occuredException);

        String expectedFileContent = ExceptionUtils.getMessage(occuredException) + System.lineSeparator() +
                ExceptionUtils.getStackTrace(occuredException) + System.lineSeparator();
        assertThat(errorFile).hasContent(expectedFileContent);
    }

    @Test
    public void givenExceptionWhenAddErrorThenExceptionFileIsPersistedInErrorLog() {
        ErrorLog errorLog = invokeAndVerifyInsert();
        assertThat(errorLog.getErrorFilePath()).isNotNull();
        assertThat(Paths.get(errorLog.getErrorFilePath())).exists();
    }

    private ErrorLog invokeAndVerifyInsert() {
        service.addError(client, message, occuredException);

        ArgumentCaptor<ErrorLog> captor = ArgumentCaptor.forClass(ErrorLog.class);
        Mockito.verify(errorLogRepository).insert(captor.capture());
        return captor.getValue();
    }

    @Test
    public void givenClientWhenAddErrorThenErrorLogContainsClient() {
        ErrorLog errorLog = invokeAndVerifyInsert();
        assertThat(errorLog.getClient()).isSameAs(client);
    }

    @Test
    public void givenMessageWhenAddErrorThenErrorLogContainsMessage() {
        ErrorLog errorLog = invokeAndVerifyInsert();
        assertThat(errorLog.getErrorMessage()).isEqualTo(message);
    }
}
