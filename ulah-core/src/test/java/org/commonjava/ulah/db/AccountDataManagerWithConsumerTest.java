package org.commonjava.ulah.db;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.commonjava.ulah.model.Account;
import org.junit.Before;
import org.junit.Test;

public class AccountDataManagerWithConsumerTest extends AbstractDataManagerTest {

    AccountDataManager accounts;

    @Before
    public void setupAccounts() {
        accounts = new AccountDataManager(wrappers);
    }

    @Test
    public void storeAndRetrieveMinimalAccount() {
        Account acct = new Account("test");

        accounts.storeAccount(acct, result -> {
            assertThat(result.getName(), equalTo(acct.getName()));
            assertThat(result.getId(), equalTo(1));
        });

    }

    @Test
    public void storeAndRetrieveMinimalAccountByName() {
        Account acct = new Account("test");

        accounts.storeAccount(acct);
        accounts.getAccount(acct.getName(), result -> {
            assertThat(result.getName(), equalTo(acct.getName()));
            assertThat(result.getId(), equalTo(1));
        });

    }

    @Test
    public void storeAndRetrieveMinimalAccountInListing() {
        Account acct = new Account("test");

        accounts.storeAccount(acct);
        accounts.listAccounts(listing -> {
            Account result = listing.get(0);

            assertThat(result.getName(), equalTo(acct.getName()));
            assertThat(result.getId(), equalTo(1));
        });
    }

    @Test
    public void storeTwoAndRetrieveOneMinimalAccountInNamePrefixListing() {
        accounts.storeAccount(new Account("test"));
        accounts.storeAccount(new Account("rest"));
        accounts.listAccountsStartingWith("t", listing -> {
            Account result = listing.get(0);

            assertThat(result.getName(), equalTo("test"));
            assertThat(result.getId(), equalTo(1));
        });
    }

    @Test
    public void storeRetrieveThenDeleteMinimalAccount() {
        Account acct = new Account("test");

        accounts.storeAccount(acct, result -> {
            assertThat(result.getName(), equalTo(acct.getName()));
            assertThat(result.getId(), equalTo(1));

            acct.setId(result.getId());
        });

        accounts.deleteAccount(acct);

        accounts.getAccount(acct.getId(), result -> {
            assertThat(result, nullValue());
        });
    }

}
