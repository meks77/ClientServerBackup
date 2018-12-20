package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.http.HttpUrlResolver;
import at.meks.backupclientserver.client.http.JsonHttpClient;
import at.meks.backupclientserver.common.service.health.ErrorReport;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorReporter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private JsonHttpClient jsonHttpClient;

    @Inject
    private HttpUrlResolver urlResolver;

    @Inject
    private SystemService systemService;
    private String url;

    public void reportError(String message, Exception exc) {
        logger.error(message, exc);
        reportToServer(message, exc);
    }

    private void reportToServer(String message, Exception exc) {
        try {
            if (url == null) {
                url = urlResolver.getWebserviceUrl("health", "error/" + systemService.getHostname());
            }
            jsonHttpClient.put(url, ErrorReport.anErrorReport().message(message).exception(exc).build(), Void.TYPE);
        } catch (Exception e) {
            logger.error("couldn't report error to server", e);
        }
    }

}
