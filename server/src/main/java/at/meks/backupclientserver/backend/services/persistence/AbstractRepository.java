package at.meks.backupclientserver.backend.services.persistence;

import io.jsondb.JsonDBTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public abstract class AbstractRepository<E, I> {

    @Inject
    PersistenceService persistenceService;

    @PostConstruct
    public void initDb() {
        if (!persistenceService.getJsonDBTemplate().collectionExists(getEntityClass())) {
            persistenceService.getJsonDBTemplate().createCollection(getEntityClass());
        }
    }

    abstract Class<E> getEntityClass();

    JsonDBTemplate getJsonDBTemplate() {
        return persistenceService.getJsonDBTemplate();
    }

    public List<E> getAll() {
        return getJsonDBTemplate().getCollection(getEntityClass());
    }

    public void update(E entity) {
        getJsonDBTemplate().save(entity, getEntityClass());
    }

    public void insert(E entity) {
        getJsonDBTemplate().insert(entity);
    }

    public Optional<E> getById(I id) {
        return ofNullable(getJsonDBTemplate().findById(id, getEntityClass()));
    }

    public int getSize() {
        return getJsonDBTemplate().getCollection(getEntityClass()).size();
    }
}
