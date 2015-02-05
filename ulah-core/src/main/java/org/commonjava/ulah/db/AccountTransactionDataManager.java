package org.commonjava.ulah.db;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import org.commonjava.ulah.model.AccountTransaction;

@ApplicationScoped
public class AccountTransactionDataManager {

    @Inject
    private EntityFunctionWrappers wrappers;

    protected AccountTransactionDataManager() {
    }

    public AccountTransactionDataManager(EntityFunctionWrappers wrappers) {
        this.wrappers = wrappers;
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
        return wrappers.withTransaction(entityManager -> {
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

}
