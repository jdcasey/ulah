package org.commonjava.ulah.db;

import java.sql.DriverManager;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.commonjava.ulah.model.Account;
import org.junit.After;
import org.junit.Before;

public class AbstractDataManagerTest {

    private EntityManagerFactory entityManagerFactory;
    protected EntityFunctionWrappers wrappers;

    public AbstractDataManagerTest() {
        super();
    }

    @Before
    public void setup() throws Exception {
        entityManagerFactory = Persistence
                .createEntityManagerFactory(Account.class.getPackage()
                        .getName());
        wrappers = new EntityFunctionWrappers(entityManagerFactory);
    }

    @After
    public void teardown() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }

        try {
            DriverManager.getConnection("jdbc:derby:memory:ulah;shutdown=true")
                    .close();
        } catch (Exception e) {
        }
    }

}