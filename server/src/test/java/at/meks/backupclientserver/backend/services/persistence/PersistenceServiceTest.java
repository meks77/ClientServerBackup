package at.meks.backupclientserver.backend.services.persistence;

import io.jsondb.JsonDBTemplate;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.assertj.core.api.Assertions.assertThat;

public class PersistenceServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private JsonDBTemplate dbTemplate;

    @InjectMocks
    private PersistenceService service = new PersistenceService();

    @Test
    public void whenGetJsonDBTemplateThenExpectedTemplateIsReturned() {
        assertThat(service.getJsonDBTemplate()).isEqualTo(dbTemplate);
    }
}
