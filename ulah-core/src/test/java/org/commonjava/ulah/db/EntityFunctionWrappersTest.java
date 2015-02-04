package org.commonjava.ulah.db;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.commonjava.ulah.model.Account;
import org.junit.Test;

public class EntityFunctionWrappersTest extends AbstractDataManagerTest {

    @Test
    public void storingAccountPopulatesId() {
        Account acct = new Account("test");
        Account result = wrappers.withTransaction(entityManager -> {
            entityManager.persist(acct);
            return acct;
        }, () -> null);

        assertThat(result.getId(), notNullValue());
    }

}
