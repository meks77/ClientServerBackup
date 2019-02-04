package at.meks.backupclientserver.client.http;

import at.meks.backupclientserver.client.ClientBackupException;
import at.meks.backupclientserver.client.ServerStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;

@Singleton
public class JsonHttpClient {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ObjectMapper mapper = new ObjectMapper();
    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    @Inject
    private ServerStatusService serverStatusService;

    public <I, R> R post(String url, I input, Class<R> resultClass) {
        return invokeHttpRequestAndCatchError(input, resultClass, new HttpPost(url));
    }

    private <I, R> R invokeHttpRequestAndCatchError(I input, Class<R> resultClass, HttpEntityEnclosingRequestBase request) {
        return invokeHttpRequestAndCatchError(input, resultClass, request, true);
    }

    private <I, R> R invokeHttpRequestAndCatchError(I input, Class<R> resultClass,
            HttpEntityEnclosingRequestBase request, boolean waitForServerAvailable) {
        try {
            if (waitForServerAvailable) {
                return serverStatusService.runWhenServerIsAvailable(() -> invokHttpRequest(input, resultClass, request));
            } else {
                return invokHttpRequest(input, resultClass, request);
            }
        } catch (Exception e) {
            if (ExceptionUtils.getRootCause(e) instanceof ConnectException) {
                serverStatusService.setServerAvailable(false);
            }
            throw new ClientBackupException("error while invoking restservice " + request.getURI(), e);
        }
    }

    private <I, R> R invokHttpRequest(I input, Class<R> resultClass, HttpEntityEnclosingRequestBase request) throws IOException {
        CloseableHttpResponse response = null;
        try {
            response = getResponse(input, request);
            if (resultClass == null || resultClass.equals(Void.TYPE)) {
                //noinspection unchecked
                return (R) Void.TYPE;
            }
            return mapper.readValue(response.getEntity().getContent(), resultClass);
        } finally {
            closeResponse(response);
        }
    }

    private void closeResponse(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                logger.error("couldn't close response!");
            }
        }
    }

    private <I> CloseableHttpResponse getResponse(I input, HttpEntityEnclosingRequestBase request) throws IOException {
        if (input != null) {
            HttpEntity httpEntity =
                    EntityBuilder.create().setContentType(ContentType.APPLICATION_JSON.withCharset("utf8"))
                            .setText(mapper.writeValueAsString(input)).build();
            request.setEntity(httpEntity);
        }
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        CloseableHttpResponse response = httpClient.execute(request);
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            String errorMessage = "Received error from rest service. Code: {}, Message: {}";
            logger.error(errorMessage, statusLine.getStatusCode(), statusLine.getReasonPhrase());
            throw new ClientBackupException(errorMessage);
        }
        return response;
    }

    public <I> void delete(String url, I input) {
        invokeHttpRequestAndCatchError(input, Void.TYPE, new HttpDelete(url));
    }

    public <I, R> R put(String url, I input, Class<R> resultClass) {
        return put(url, input, resultClass, true);
    }

    public <I, R> R put(String url, I input, Class<R> resultClass, boolean waitForServerAvailable) {
        return invokeHttpRequestAndCatchError(input, resultClass, new HttpPut(url), waitForServerAvailable);
    }
}
