package at.meks.backupclientserver.client.http;

import at.meks.backupclientserver.client.ApplicationConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class HttpUrlResolverTest {

    private static final String DEFAULT_HOST = "theDefaultHost";
    private static final int DEFAULT_PORT = 87492;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ApplicationConfig config;

    @InjectMocks
    private HttpUrlResolver urlResolver = new HttpUrlResolver();

    @Before
    public void defaultMocks() {
        when(config.getServerHost()).thenReturn(DEFAULT_HOST);
        when(config.getServerPort()).thenReturn(DEFAULT_PORT);
    }

    @Test
    public void whenGetWebserviceUrlThenHostOfConfigIsReturned() {
        when(config.getServerHost()).thenReturn(DEFAULT_HOST, "anotherHostName");

        String url = urlResolver.getWebserviceUrl("mod", "method");
        assertThat(url).startsWith("http://" + DEFAULT_HOST);

        url = urlResolver.getWebserviceUrl("mod", "method");
        assertThat(url).startsWith("http://anotherHostName");
    }

    @Test
    public void whenGetWebserviceUrlThenPortOfConfigIsReturned() {
        when(config.getServerPort()).thenReturn(DEFAULT_PORT, 36282);

        String url = urlResolver.getWebserviceUrl("mod", "method");
        assertThat(url).startsWith("http://" + DEFAULT_HOST + ":" + DEFAULT_PORT);

        url = urlResolver.getWebserviceUrl("mod", "method");
        assertThat(url).startsWith("http://" + DEFAULT_HOST + ":" + 36282);
    }

    @Test
    public void givenModuleNullWhenGetWebserviceUrlThenReturnsNullInUrl() {
        assertThat(urlResolver.getWebserviceUrl(null, "somewhat")).endsWith("null/somewhat");
    }

    @Test
    public void givenMethodUrlNullWhenGetWebserviceUrlThenReturnsNullInUrl() {
        assertThat(urlResolver.getWebserviceUrl("somewhat", null)).endsWith("/null");
    }

    @Test
    public void givenModuleAndMethodUrlWhenGetWebserviceUrlThenReturnsExpectedUrlEnd() {
        String url = urlResolver.getWebserviceUrl("moduleName", "methodPart/somewhat");
        assertThat(url).isEqualTo("http://" + DEFAULT_HOST + ":" + DEFAULT_PORT + "/api/v1.0/moduleName/methodPart/somewhat");
    }

}