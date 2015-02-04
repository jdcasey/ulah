package org.commonjava.ulah.db;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

@ApplicationScoped
public class EntityFunctionWrappers {

    @Inject
    private EntityManagerFactory entityManagerFactory;

    protected EntityFunctionWrappers() {
    }

    public EntityFunctionWrappers(EntityManagerFactory emf) {
        entityManagerFactory = emf;
    }

    public <T> T withTransaction(Function<EntityManager, T> fn,
            Supplier<Consumer<T>> supplier) {
        EntityManager entityManager = null;
        EntityTransaction tx = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            tx = entityManager.getTransaction();

            tx.begin();

            T result = fn.apply(entityManager);

            tx.commit();

            Consumer<T> consumer = supplier.get();
            if (consumer != null) {
                consumer.accept(result);
            }

            return result;
        } catch (RuntimeException e) {
            if (tx != null) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        } finally {
            if (entityManager != null) {
                entityManager.clear();
                entityManager.close();
            }
        }
    }

}
