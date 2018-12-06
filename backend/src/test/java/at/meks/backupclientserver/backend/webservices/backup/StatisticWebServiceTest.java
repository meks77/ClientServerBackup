package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.services.FileService;
import at.meks.backupclientserver.backend.services.FileStatistics;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatisticWebServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private FileService fileService;

    @InjectMocks
    private StatisticWebService service = new StatisticWebService();

    @Test
    public void testGetFileStatistics() {
        FileStatistics fileStatistics = mock(FileStatistics.class);
        when(fileService.getBackupFileStatistics()).thenReturn(fileStatistics);
        FileStatistics result = service.getFileStatistics();
        assertThat(result).isSameAs(fileStatistics);
    }

}
