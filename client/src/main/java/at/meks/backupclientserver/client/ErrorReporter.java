package at.meks.backupclientserver.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;
import java.net.ConnectException;

@ApplicationScoped
@Slf4j
public class ErrorReporter {

    public void reportError(String message, Exception exc) {
        log.error(message, exc);
        // if the error is that the server isn't available it doesn't make sense to report it to the server
        if (!(ExceptionUtils.getRootCause(exc) instanceof ConnectException)) {
            reportToServer(message, exc);
        }
    }

    private void reportToServer(String message, Exception exc) {
        try {
            // TODO use RemoteService class instead
//            jsonHttpClient.put(url, ErrorReport.anErrorReport().message(message).exception(exc).build(), Void.TYPE);
        } catch (Exception e) {
            log.error("couldn't report error to server", e);
        }
    }

}
