package org.commonjava.ulah.db;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import org.commonjava.ulah.model.AccountTransaction;
import org.commonjava.ulah.model.TransactionTag;

@ApplicationScoped
public class AccountTransactionDataManager {

    @Inject
    private EntityFunctionWrappers wrappers;

    @Inject
    private AccountDataManager accounts;

    private TransactionTagDataManager tags;

    protected AccountTransactionDataManager() {
    }

    public AccountTransactionDataManager(EntityFunctionWrappers wrappers,
            AccountDataManager accounts, TransactionTagDataManager tags) {
        this.wrappers = wrappers;
        this.accounts = accounts;
        this.tags = tags;
    }

    public AccountTransaction getTransaction(Long transactionId) {
        return getTransaction(transactionId, null);
    }

    public AccountTransaction getTransaction(Long transactionId,
            Consumer<AccountTransaction> consumer) {
        return wrappers.withTransaction(entityManager -> {
            return entityManager.find(AccountTransaction.class, transactionId);
        }, () -> consumer);
    }

    public AccountTransaction storeTransaction(AccountTransaction transaction) {
        return storeTransaction(transaction, null);
    }

    public AccountTransaction storeTransaction(AccountTransaction transaction,
            Consumer<AccountTransaction> consumer) {
        return wrappers
                .withTransaction(
                        entityManager -> {
                            if (transaction.getFromAccount() != null) {
                                transaction.setFromAccount(accounts
                                        .getOrCreateAccountByName(transaction
                                                .getFromAccount()));
                            }

                            if (transaction.getToAccount() != null) {
                                transaction.setToAccount(accounts
                                        .getOrCreateAccountByName(transaction
                                                .getToAccount()));
                            }

                            Set<TransactionTag> t = transaction.getTags();
                            if (t != null) {
                                Set<TransactionTag> mergedTags = new HashSet<TransactionTag>();
                                for (TransactionTag tag : t) {
                                    mergedTags.add(tags
                                            .getOrCreateTagByName(tag));
                                }

                                transaction.setTags(mergedTags);
                            }

                            return entityManager.merge(transaction);
                        }, () -> consumer);
    }

    public void deleteTransaction(AccountTransaction transaction) {
        wrappers.withTransaction(entityManager -> {
            AccountTransaction tx = entityManager.merge(transaction);
            entityManager.remove(tx);
            return null;
        }, () -> null);
    }

    public BigDecimal getAccountBalance(Integer accountId) {
        return getAccountBalance(accountId, null);
    }

    public BigDecimal getAccountBalance(Integer accountId,
            Consumer<BigDecimal> consumer) {
        return wrappers
                .withTransaction(
                        entityManager -> {
                            TypedQuery<BigDecimal> subtractions = entityManager
                                    .createNamedQuery(
                                            AccountTransaction.SUMMARIZE_AMOUNT_BY_FROM_ACCOUNT,
                                            BigDecimal.class);
                            subtractions.setParameter(
                                    AccountTransaction.ACCOUNT_ID_PARAM,
                                    accountId);
                            BigDecimal sub = subtractions.getSingleResult();
                            if (sub == null) {
                                sub = new BigDecimal(0);
                            }

                            TypedQuery<BigDecimal> additions = entityManager
                                    .createNamedQuery(
                                            AccountTransaction.SUMMARIZE_AMOUNT_BY_TO_ACCOUNT,
                                            BigDecimal.class);
                            additions.setParameter(
                                    AccountTransaction.ACCOUNT_ID_PARAM,
                                    accountId);
                            BigDecimal add = additions.getSingleResult();
                            if (add == null) {
                                add = new BigDecimal(0);
                            }

                            return add.subtract(sub);
                        }, () -> consumer);
    }

    public List<AccountTransaction> getAccountTransactions(Integer accountId,
            Date after, Date before, int skip, int limit) {
        return getAccountTransactions(accountId, after, before, skip, limit,
                null);
    }

    public List<AccountTransaction> getAccountTransactions(Integer accountId,
            Date after, Date before, int skip, int limit,
            Consumer<List<AccountTransaction>> consumer) {
        return wrappers
                .withTransaction(
                        entityManager -> {
                            TypedQuery<AccountTransaction> query = entityManager
                                    .createNamedQuery(
                                            AccountTransaction.ACCOUNT_TRANSACTIONS_WITHIN_DATES,
                                            AccountTransaction.class);

                            query.setParameter(
                                    AccountTransaction.ACCOUNT_ID_PARAM,
                                    accountId);
                            query.setParameter(
                                    AccountTransaction.BEFORE_DATE_PARAM,
                                    before);
                            query.setParameter(
                                    AccountTransaction.AFTER_DATE_PARAM, after);

                            query.setFirstResult(skip);
                            query.setMaxResults(limit);

                            return query.getResultList();
                        }, () -> consumer);
    }

    public List<AccountTransaction> getAllTransactions(Date after, Date before,
            int skip, int limit) {
        return getAllTransactions(after, before, skip, limit, null);
    }

    public List<AccountTransaction> getAllTransactions(Date after, Date before,
            int skip, int limit, Consumer<List<AccountTransaction>> consumer) {
        return wrappers
                .withTransaction(
                        entityManager -> {
                            TypedQuery<AccountTransaction> query = entityManager
                                    .createNamedQuery(
                                            AccountTransaction.ALL_TRANSACTIONS_WITHIN_DATES,
                                            AccountTransaction.class);

                            query.setParameter(
                                    AccountTransaction.BEFORE_DATE_PARAM,
                                    before);
                            query.setParameter(
                                    AccountTransaction.AFTER_DATE_PARAM, after);

                            query.setFirstResult(skip);
                            query.setMaxResults(limit);

                            return query.getResultList();
                        }, () -> consumer);
    }

    // TODO: Transactions for account with given tag(s)
    // TODO: All transactions with given tag(s)

}
