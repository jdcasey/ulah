package org.commonjava.ulah.db;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.commonjava.ulah.model.Account;
import org.junit.Before;
import org.junit.Test;

public class AccountDataManagerTest extends AbstractDataManagerTest {

    private AccountDataManager accounts;

    @Before
    public void setupAccounts() throws Exception {
        accounts = new AccountDataManager(wrappers);
    }

    @Test
    public void storeAndRetrieveMinimalAccount() {
        Account acct = new Account("test");

        Account result = accounts.storeAccount(acct);

        assertThat(result.getName(), equalTo(acct.getName()));
        assertThat(result.getId(), equalTo(1));
    }

    @Test
    public void storeAndRetrieveMinimalAccountByName() {
        Account acct = new Account("test");

        accounts.storeAccount(acct);
        Account result = accounts.getAccount(acct.getName());

        assertThat(result.getName(), equalTo(acct.getName()));
        assertThat(result.getId(), equalTo(1));
    }

    @Test
    public void storeAndRetrieveMinimalAccountInListing() {
        Account acct = new Account("test");

        accounts.storeAccount(acct);
        List<Account> listing = accounts.listAccounts();
        Account result = listing.get(0);

        assertThat(result.getName(), equalTo(acct.getName()));
        assertThat(result.getId(), equalTo(1));
    }

    @Test
    public void storeTwoAndRetrieveOneMinimalAccountInNamePrefixListing() {
        accounts.storeAccount(new Account("test"));
        accounts.storeAccount(new Account("rest"));
        List<Account> listing = accounts.listAccountsStartingWith("t");
        Account result = listing.get(0);

        assertThat(result.getName(), equalTo("test"));
        assertThat(result.getId(), equalTo(1));
    }

    @Test
    public void storeRetrieveThenDeleteMinimalAccount() {
        Account acct = new Account("test");

        Account result = accounts.storeAccount(acct);

        assertThat(result.getName(), equalTo(acct.getName()));
        assertThat(result.getId(), equalTo(1));

        Integer id = result.getId();

        accounts.deleteAccount(result);

        result = accounts.getAccount(id);
        assertThat(result, nullValue());
    }

}
