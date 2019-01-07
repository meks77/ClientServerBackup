package at.meks.backupclientserver.client.http;

import at.meks.backupclientserver.client.ApplicationConfig;
import com.google.inject.Singleton;

import javax.inject.Inject;

@Singleton
public class HttpUrlResolver {

    @Inject
    private ApplicationConfig config;

    public String getWebserviceUrl(String module, String methodUrl) {
        return String.format("http://%s:%s/api/v1.0/%s/%s",
                config.getServerHost(), config.getServerPort(), module, methodUrl);
    }
}
