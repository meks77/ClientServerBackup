package at.meks.backupclientserver.backend.services.persistence;

import at.meks.backupclientserver.backend.domain.ErrorLog;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.nio.file.Path;

@Named
@ApplicationScoped
public class ErrorLogRepository extends AbstractRepository<ErrorLog, Path> {

    @Override
    Class<ErrorLog> getEntityClass() {
        return ErrorLog.class;
    }

}
