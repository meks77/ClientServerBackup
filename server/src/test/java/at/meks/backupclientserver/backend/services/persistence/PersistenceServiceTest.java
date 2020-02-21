package at.meks.backupclientserver.backend.services.persistence;

import io.jsondb.JsonDBTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PersistenceServiceTest {

    @Mock
    private JsonDBTemplate dbTemplate;

    @InjectMocks
    private PersistenceService service = new PersistenceService();

    @Test
    public void whenGetJsonDBTemplateThenExpectedTemplateIsReturned() {
        assertThat(service.getJsonDBTemplate()).isEqualTo(dbTemplate);
    }
}
