package at.meks.backupclientserver.backend.services;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.domain.ErrorLog;
import at.meks.backupclientserver.backend.services.file.DirectoryService;
import at.meks.backupclientserver.backend.services.file.FileService;
import at.meks.backupclientserver.backend.services.persistence.ErrorLogRepository;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ErrorReportService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ErrorLogRepository errorLogRepository;

    public void addError(Client client, String message, Exception occuredException) {
        try {
            Path errorFile = fileService.createFileWithRandomName(directoryService.getErrorDirectory());
            writeExceptionReportToFile(errorFile, occuredException);
            errorLogRepository.insert(
                    ErrorLog.anErrorLog().client(client).errorFilePath(errorFile.toString())
                            .errorMessage(message).build());
        } catch (Exception e) {
            logger.error("couldn't write error to file and database", e);
        }

    }

    private void writeExceptionReportToFile(Path file, Exception occuredException) {
        try {
            StringBuilder outputBuilder = getExceptionReportText(occuredException);
            Files.write(file, outputBuilder.toString().getBytes());
        } catch (Exception e) {
            logger.error("Couldn't write exception to file", e);
            logger.error("exception for file: ", occuredException);
        }
    }

    private StringBuilder getExceptionReportText(Exception occuredException) {
        StringBuilder outputBuilder = new StringBuilder(ExceptionUtils.getMessage(occuredException));
        outputBuilder.append(System.lineSeparator())
                .append(ExceptionUtils.getStackTrace(occuredException)).append(System.lineSeparator());

        Throwable rootCause = ExceptionUtils.getRootCause(occuredException);
        if (rootCause != null && rootCause != occuredException) {
            outputBuilder.append("causing exception: ").append(ExceptionUtils.getMessage(rootCause))
                    .append(System.lineSeparator())
                    .append(ExceptionUtils.getStackTrace(rootCause));
        }
        return outputBuilder;
    }

}
