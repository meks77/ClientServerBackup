package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.ClientBackupException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

class JsonHttpClient {

    private ObjectMapper mapper = new ObjectMapper();
    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    <I, R> R post(String url, I input, Class<R> resultClass) {
        try {
            HttpEntity httpEntity = EntityBuilder.create().setText(mapper.writeValueAsString(input)).build();
            HttpPost post = new HttpPost(url);
            post.setEntity(httpEntity);
            CloseableHttpResponse response = httpClient.execute(post);
            return mapper.readValue(response.getEntity().getContent(), resultClass);
        } catch (IOException e) {
            throw new ClientBackupException("error while invoking restservice " + url, e);
        }
    }
}
