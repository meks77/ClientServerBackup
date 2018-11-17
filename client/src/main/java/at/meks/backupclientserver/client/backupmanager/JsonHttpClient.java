package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.ClientBackupException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class JsonHttpClient {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ObjectMapper mapper = new ObjectMapper();
    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    <I, R> R post(String url, I input, Class<R> resultClass) {
        CloseableHttpResponse response = null;
        try {
            response = getResponse(input, new HttpPost(url));
            return mapper.readValue(response.getEntity().getContent(), resultClass);
        } catch (IOException e) {
            throw new ClientBackupException("error while invoking restservice " + url, e);
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

    private <I> CloseableHttpResponse getResponse(I input, HttpEntityEnclosingRequestBase post) throws IOException {
        HttpEntity httpEntity =
                EntityBuilder.create().setContentType(ContentType.APPLICATION_JSON.withCharset("utf8"))
                        .setText(mapper.writeValueAsString(input)).build();
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        post.setEntity(httpEntity);
        CloseableHttpResponse response = httpClient.execute(post);
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() != 200) {
            String errorMessage = "Received error from rest service. Code: {}, Message: {}";
            logger.error(errorMessage, statusLine.getStatusCode(), statusLine.getReasonPhrase());
            throw new ClientBackupException(errorMessage);
        }
        return response;
    }

    <I> void delete(String url, I input) {
        CloseableHttpResponse response = null;
        try {
            response = getResponse(input, new HttpDelete(url));
        } catch (IOException e) {
            throw new ClientBackupException("error while invoking restservice " + url, e);
        } finally {
            closeResponse(response);
        }
    }
}