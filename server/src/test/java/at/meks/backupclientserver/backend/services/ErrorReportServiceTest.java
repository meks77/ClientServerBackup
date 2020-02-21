package at.meks.backupclientserver.backend.services;

import at.meks.backupclientserver.backend.domain.ErrorLog;
import at.meks.backupclientserver.backend.services.file.DirectoryService;
import at.meks.backupclientserver.backend.services.file.FileService;
import at.meks.backupclientserver.backend.services.persistence.ErrorLogRepository;
import lombok.SneakyThrows;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
public class ErrorReportServiceTest {

    @Mock
    private ErrorLogRepository errorLogRepository;

    @Mock
    private DirectoryService directoryService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ErrorReportService service = new ErrorReportService();

    @TempDir
    Path errorsDirectory;
    private String hostName = "the host of the client which was processed";
    private String message = "a message with detail information";
    private Exception occuredException = new IllegalStateException("just a exception", new IOException("the cause"));
    private Path errorFile;

    @SneakyThrows
    private void mockDefaults() {
        when(directoryService.getErrorDirectory()).thenReturn(errorsDirectory);
        errorFile = Files.createFile(errorsDirectory.resolve("ut-error-file.txt"));
        when(fileService.createFileWithRandomName(errorsDirectory)).thenReturn(errorFile);
    }

    @Test
    public void givenExceptionWithCauseWhenAddErrorThenExceptionIsWrittenToFile() {
        mockDefaults();
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
        mockDefaults();
        occuredException = new FileNotFoundException("this file wasn't found");

        service.addError(hostName, message, occuredException);

        String expectedFileContent = ExceptionUtils.getMessage(occuredException) + System.lineSeparator() +
                ExceptionUtils.getStackTrace(occuredException) + System.lineSeparator();
        assertThat(errorFile).hasContent(expectedFileContent);
    }

    @Test
    public void givenExceptionWhenAddErrorThenExceptionFileIsPersistedInErrorLog() {
        mockDefaults();
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
        mockDefaults();
        ErrorLog errorLog = invokeAndVerifyInsert();
        assertThat(errorLog.getHostName()).isSameAs(hostName);
    }

    @Test
    public void givenMessageWhenAddErrorThenErrorLogContainsMessage() {
        mockDefaults();
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
