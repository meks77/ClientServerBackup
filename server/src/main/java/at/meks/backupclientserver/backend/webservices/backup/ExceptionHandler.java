package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.services.ErrorReportService;
import at.meks.backupclientserver.backend.services.ServerBackupException;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ErrorReportService errorReportService;

    <R> R runReportingException(Supplier<String> executionInformation, Callable<R> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            errorReportService.addError(getHostname(), executionInformation.get(), e);
            throw new ServerBackupException(e);
        }
    }

    void runReportingException(Supplier<String> executionInformation, Runnable invocation) {
        try {
            invocation.run();
        } catch (Exception e) {
            errorReportService.addError(getHostname(), executionInformation.get(), e);
            throw new ServerBackupException(e);
        }
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error("Couldn't get Hostname", e);
            return SystemUtils.getHostName();
        }
    }
}
