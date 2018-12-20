package at.meks.backupclientserver.backend.services.persistence;

import at.meks.backupclientserver.backend.domain.ErrorLog;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ErrorLogRepository extends AbstractRepository<ErrorLog, Path> {

    @Override
    Class<ErrorLog> getEntityClass() {
        return ErrorLog.class;
    }

}
