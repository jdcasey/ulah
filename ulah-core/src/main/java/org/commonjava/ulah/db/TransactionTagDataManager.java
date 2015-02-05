package org.commonjava.ulah.db;

import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.commonjava.ulah.model.TransactionTag;

@ApplicationScoped
public class TransactionTagDataManager {

    @Inject
    private EntityFunctionWrappers wrappers;

    protected TransactionTagDataManager() {
    }

    public TransactionTagDataManager(EntityFunctionWrappers wrappers) {
        this.wrappers = wrappers;
    }

    public List<TransactionTag> getAllTags() {
        return getAllTags(null);
    }

    public List<TransactionTag> getAllTags(
            Consumer<List<TransactionTag>> consumer) {
        return wrappers.withTransaction(
                entityManager -> {
                    TypedQuery<TransactionTag> query = entityManager
                            .createNamedQuery(TransactionTag.ALL_TAGS,
                                    TransactionTag.class);
                    return query.getResultList();
                }, () -> consumer);
    }

    public TransactionTag getOrCreateTagByName(TransactionTag tag) {
        return getOrCreateTagByName(tag, null);
    }

    public TransactionTag getOrCreateTagByName(TransactionTag tag,
            Consumer<TransactionTag> consumer) {

        TransactionTag result = null;
        if (tag.getId() != null) {
            result = wrappers.withTransaction(entityManager -> {
                return entityManager.getReference(TransactionTag.class,
                        tag.getId());
            }, () -> null);
        } else if (tag.getName() != null) {
            result = getTag(tag.getName());
        }

        if (result == null) {
            return storeTag(tag, consumer);
        } else {
            if (consumer != null) {
                consumer.accept(result);
            }

            return result;
        }
    }

    public TransactionTag storeTag(TransactionTag tag) {
        return storeTag(tag, null);
    }

    public TransactionTag storeTag(TransactionTag tag,
            Consumer<TransactionTag> consumer) {
        return wrappers.withTransaction(entityManager -> {
            return entityManager.merge(tag);
        }, () -> consumer);
    }

    public TransactionTag getTag(String name) {
        return getTag(name, null);
    }

    public TransactionTag getTag(String name, Consumer<TransactionTag> consumer) {
        TransactionTag result = null;
        try {
            result = wrappers.withTransaction(
                    entityManager -> {
                        TypedQuery<TransactionTag> query = entityManager
                                .createNamedQuery(TransactionTag.FIND_BY_NAME,
                                        TransactionTag.class);
                        query.setParameter(TransactionTag.NAME_PARAM, name);
                        return query.getSingleResult();
                    }, () -> consumer);
        } catch (NoResultException e) {
            if (consumer != null) {
                consumer.accept(result);
            }
        }

        return result;
    }

}
