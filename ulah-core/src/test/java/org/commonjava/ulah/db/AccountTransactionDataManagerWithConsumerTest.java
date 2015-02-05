package org.commonjava.ulah.db;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;

import org.commonjava.ulah.model.Account;
import org.commonjava.ulah.model.AccountTransaction;
import org.junit.Before;
import org.junit.Test;

public class AccountTransactionDataManagerWithConsumerTest extends
        AbstractDataManagerTest {

    private AccountTransactionDataManager transactions;
    private AccountDataManager accounts;
    private Account from;
    private Account to;

    @Before
    public void setupTransactions() {
        transactions = new AccountTransactionDataManager(wrappers);
        accounts = new AccountDataManager(wrappers);

        from = accounts.storeAccount(new Account("from"));
        to = accounts.storeAccount(new Account("to"));
    }

    @Test
    public void storeAndRetrieveTransactionById() {
        AccountTransaction tx = new AccountTransaction(
                from,
                to,
                new BigDecimal(10.22, new MathContext(4, RoundingMode.HALF_UP)),
                "test transaction");

        transactions.storeTransaction(tx, result -> {
            assertThat(result.getAmount(), equalTo(tx.getAmount()));
            assertThat(result.getMemo(), equalTo(tx.getMemo()));
            assertThat(result.getId(), notNullValue());

            tx.setId(result.getId());
        });

        Long id = tx.getId();
        transactions.getTransaction(id, result -> {
            assertThat(result.getAmount(), equalTo(tx.getAmount()));
            assertThat(result.getMemo(), equalTo(tx.getMemo()));
            assertThat(result.getId(), equalTo(id));
        });
    }

    @Test
    public void storeDeleteAndFailToRetrieveTransactionById() {
        AccountTransaction tx = new AccountTransaction(
                from,
                to,
                new BigDecimal(10.22, new MathContext(4, RoundingMode.HALF_UP)),
                "test transaction");

        transactions.storeTransaction(tx, result -> {
            assertThat(result.getAmount(), equalTo(tx.getAmount()));
            assertThat(result.getMemo(), equalTo(tx.getMemo()));
            assertThat(result.getId(), notNullValue());
            tx.setId(result.getId());
        });

        Long id = tx.getId();
        transactions.deleteTransaction(tx);

        transactions.getTransaction(id, result -> {
            assertThat(result, nullValue());
        });
    }

    @Test
    public void newTransactionDeductedFromSourceAccount() {
        AccountTransaction tx = new AccountTransaction(
                from,
                to,
                new BigDecimal(10.22, new MathContext(4, RoundingMode.HALF_UP)),
                "test transaction");

        transactions.storeTransaction(tx, result -> {
            assertThat(result.getAmount(), equalTo(tx.getAmount()));
            assertThat(result.getMemo(), equalTo(tx.getMemo()));
            assertThat(result.getId(), notNullValue());
        });

        transactions.getAccountBalance(from.getId(), balance -> {
            assertThat(balance.doubleValue(), equalTo(-10.22));
        });
    }

    @Test
    public void newTransactionAddedToTargetAccount() {
        AccountTransaction tx = new AccountTransaction(
                from,
                to,
                new BigDecimal(10.22, new MathContext(4, RoundingMode.HALF_UP)),
                "test transaction");

        transactions.storeTransaction(tx, result -> {
            assertThat(result.getAmount(), equalTo(tx.getAmount()));
            assertThat(result.getMemo(), equalTo(tx.getMemo()));
            assertThat(result.getId(), notNullValue());
        });

        transactions.getAccountBalance(to.getId(), balance -> {
            assertThat(balance.doubleValue(), equalTo(10.22));
        });
    }

    @Test
    public void newTransactionsSummedAppropriatelyInTargetAccount() {
        transactions
        .storeTransaction(new AccountTransaction(from, to,
                new BigDecimal(10.22, new MathContext(4,
                        RoundingMode.HALF_UP)), "test transaction"));

        transactions.storeTransaction(new AccountTransaction(to, from,
                new BigDecimal(1.22, new MathContext(4, RoundingMode.HALF_UP)),
                "test refund transaction"));

        transactions.getAccountBalance(to.getId(), balance -> {
            assertThat(balance.doubleValue(), equalTo(9.0));
        });
    }

    @Test
    public void newTransactionsSummedAppropriatelyInSourceAccount() {
        transactions
        .storeTransaction(new AccountTransaction(from, to,
                new BigDecimal(10.22, new MathContext(4,
                        RoundingMode.HALF_UP)), "test transaction"));

        transactions.storeTransaction(new AccountTransaction(to, from,
                new BigDecimal(1.22, new MathContext(4, RoundingMode.HALF_UP)),
                "test refund transaction"));

        transactions.getAccountBalance(from.getId(), balance -> {
            assertThat(balance.doubleValue(), equalTo(-9.0));
        });
    }

    public void accountTransactionsExcludeThoseBetweenOtherAccounts() {
        Account third = accounts.storeAccount(new Account("third"));

        transactions.storeTransaction(new AccountTransaction(to, third,
                new BigDecimal(1.10), "excluded transaction"));
        transactions
                .storeTransaction(new AccountTransaction(from, to,
                        new BigDecimal(10.00, new MathContext(4,
                                RoundingMode.HALF_UP)), "included transaction"));

        transactions.getAccountTransactions(
                from.getId(),
                new Date(0),
                new Date(),
                0,
                3,
                txns -> {
                    assertThat(txns, notNullValue());
                    assertThat(txns.size(), equalTo(1));
                    assertThat(txns.get(0).getAmount().doubleValue(),
                            equalTo(10.00));
                });
    }

    public void allTransactionsIncludeThoseBetweenAnyAccounts() {
        Account third = accounts.storeAccount(new Account("third"));

        transactions.storeTransaction(new AccountTransaction(to, third,
                new BigDecimal(1.10), "excluded transaction"));

        transactions
                .storeTransaction(new AccountTransaction(from, to,
                        new BigDecimal(10.00, new MathContext(4,
                                RoundingMode.HALF_UP)), "included transaction"));

        transactions.getAllTransactions(
                new Date(0),
                new Date(),
                0,
                3,
                txns -> {
                    assertThat(txns, notNullValue());
                    assertThat(txns.size(), equalTo(2));
                    assertThat(txns.get(0).getAmount().doubleValue(),
                            equalTo(1.10));
                    assertThat(txns.get(1).getAmount().doubleValue(),
                            equalTo(10.00));
                });
    }

}
