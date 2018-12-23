package at.meks.backupclientserver.backend.services;

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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static at.meks.clientserverbackup.testutils.DateTestUtils.fromLocalDateTime;
import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.extractProperty;
import static org.mockito.Mockito.when;

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
    private String hostName = "the host of the client which was processed";
    private String message = "a message with detail information";
    private Exception occuredException = new IllegalStateException("just a exception", new IOException("the cause"));
    private Path errorFile;

    @Before
    public void mockDefaults() throws IOException {
        errorFile = Files.createTempFile(errorsDirectory, "ut-error-file", ".txt");
        Files.deleteIfExists(errorFile);
        when(directoryService.getErrorDirectory()).thenReturn(errorsDirectory);
        when(fileService.createFileWithRandomName(errorsDirectory)).thenReturn(errorFile);
    }

    @After
    public void deleteDir() throws IOException {
        FileUtils.deleteDirectory(errorsDirectory.toFile());
    }

    @Test
    public void givenNullExceptionWhenAddErrorThenNoFileIsWritten() {
        service.addError(hostName, message, null);

        assertThat(errorFile).doesNotExist();
    }

    @Test
    public void givenExceptionWithCauseWhenAddErrorThenExceptionIsWrittenToFile() {
        service.addError(hostName, message, occuredException);

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
        service.addError(hostName, message, occuredException);

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
        service.addError(hostName, message, occuredException);

        ArgumentCaptor<ErrorLog> captor = ArgumentCaptor.forClass(ErrorLog.class);
        Mockito.verify(errorLogRepository).insert(captor.capture());
        return captor.getValue();
    }

    @Test
    public void givenHostNametWhenAddErrorThenErrorLogContainsExpectedHostName() {
        ErrorLog errorLog = invokeAndVerifyInsert();
        assertThat(errorLog.getHostName()).isSameAs(hostName);
    }

    @Test
    public void givenMessageWhenAddErrorThenErrorLogContainsMessage() {
        ErrorLog errorLog = invokeAndVerifyInsert();
        assertThat(errorLog.getErrorMessage()).isEqualTo(message);
    }

    @Test
    public void givenMaxListSize15WhenGetErrorThenReturns5NewestEntries() {
        List<ErrorLog> unsortedList = Arrays.asList(
                createErrorLog("file1", of(2018, 1, 2)),
                createErrorLog("file2", of(2018, 1, 7)),
                createErrorLog("file3", of(2018, 1, 3)),
                createErrorLog("file4", of(2018, 1, 4)),
                createErrorLog("file5", of(2018, 1, 6)),
                createErrorLog("file6", of(2018, 1, 5))
        );
        when(errorLogRepository.getAll()).thenReturn(unsortedList);

        List<ErrorLog> errors = service.getErrors(5);
        assertThat(extractProperty("errorFilePath").from(errors)).containsExactly("file2", "file5", "file6", "file4", "file3");
    }

    private ErrorLog createErrorLog(String filePath, LocalDate dateOfError) {
        return ErrorLog.anErrorLog().errorFilePath(filePath).errorTimestamp(fromLocalDateTime(dateOfError.atStartOfDay())).build();
    }
}
