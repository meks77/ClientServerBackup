package at.meks.backupclientserver.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorReporter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void reportError(String message, Exception e) {
        logger.error(message, e);
    }
}
