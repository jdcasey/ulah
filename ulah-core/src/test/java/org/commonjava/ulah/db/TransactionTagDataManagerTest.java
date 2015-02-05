package org.commonjava.ulah.db;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import org.commonjava.ulah.model.Account;
import org.commonjava.ulah.model.AccountTransaction;
import org.commonjava.ulah.model.TransactionTag;
import org.junit.Before;
import org.junit.Test;

public class TransactionTagDataManagerTest extends AbstractDataManagerTest {

    private AccountTransactionDataManager txns;
    private TransactionTagDataManager tags;

    @Before
    public void setupManagers() {
        tags = new TransactionTagDataManager(wrappers);
        txns = new AccountTransactionDataManager(wrappers,
                new AccountDataManager(wrappers), tags);
    }

    @Test
    public void addTransactionWithTagAndRetrieveTagViaAllTags() {
        AccountTransaction txn = new AccountTransaction(new Account("from"),
                new Account("to"), new BigDecimal(1.22, new MathContext(4,
                        RoundingMode.HALF_UP)), "test txn");
        txn.setTags(Collections.singleton(new TransactionTag("foo")));

        txns.storeTransaction(txn);

        List<TransactionTag> allTags = tags.getAllTags();
        assertThat(allTags, notNullValue());
        assertThat(allTags.size(), equalTo(1));
        assertThat(allTags.get(0).getName(), equalTo("foo"));
    }

    @Test
    public void addTransactionWithTagAndRetrieveTagViaAllTags_WithConsumer() {
        AccountTransaction txn = new AccountTransaction(new Account("from"),
                new Account("to"), new BigDecimal(1.22, new MathContext(4,
                        RoundingMode.HALF_UP)), "test txn");
        txn.setTags(Collections.singleton(new TransactionTag("foo")));

        txns.storeTransaction(txn);

        tags.getAllTags(allTags -> {
            assertThat(allTags, notNullValue());
            assertThat(allTags.size(), equalTo(1));
            assertThat(allTags.get(0).getName(), equalTo("foo"));
        });
    }

    @Test
    public void addTwoTransactionsWithTheSameTagAndRetrieveOneTagViaAllTags() {
        AccountTransaction txn = new AccountTransaction(new Account("from"),
                new Account("to"), new BigDecimal(1.22, new MathContext(4,
                        RoundingMode.HALF_UP)), "test txn");
        txn.setTags(Collections.singleton(new TransactionTag("foo")));

        txns.storeTransaction(txn);

        txn = new AccountTransaction(new Account("from"), new Account("to"),
                new BigDecimal(2.22, new MathContext(4, RoundingMode.HALF_UP)),
                "test txn 2");

        txn.setTags(Collections.singleton(new TransactionTag("foo")));

        txns.storeTransaction(txn);

        List<TransactionTag> allTags = tags.getAllTags();
        assertThat(allTags, notNullValue());
        assertThat(allTags.size(), equalTo(1));
        assertThat(allTags.get(0).getName(), equalTo("foo"));
    }

    @Test
    public void addTwoTransactionsWithTheSameTagAndRetrieveOneTagViaAllTags_WithConsumer() {
        AccountTransaction txn = new AccountTransaction(new Account("from"),
                new Account("to"), new BigDecimal(1.22, new MathContext(4,
                        RoundingMode.HALF_UP)), "test txn");
        txn.setTags(Collections.singleton(new TransactionTag("foo")));

        txns.storeTransaction(txn);

        txn = new AccountTransaction(new Account("from"), new Account("to"),
                new BigDecimal(2.22, new MathContext(4, RoundingMode.HALF_UP)),
                "test txn 2");

        txn.setTags(Collections.singleton(new TransactionTag("foo")));

        txns.storeTransaction(txn);

        tags.getAllTags(allTags -> {
            assertThat(allTags, notNullValue());
            assertThat(allTags.size(), equalTo(1));
            assertThat(allTags.get(0).getName(), equalTo("foo"));
        });
    }

}
