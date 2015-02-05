package org.commonjava.ulah.db;

import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import org.commonjava.ulah.model.Account;

@ApplicationScoped
public class AccountDataManager {

    @Inject
    private EntityFunctionWrappers wrappers;

    protected AccountDataManager() {
    }

    public AccountDataManager(EntityFunctionWrappers wrappers) {
        this.wrappers = wrappers;
    }

    public Account getAccount(String name) {
        return getAccount(name, null);
    }

    public Account getAccount(String name, Consumer<Account> consumer) {
        return wrappers.withTransaction(
                entityManager -> {
                    TypedQuery<Account> query = entityManager.createNamedQuery(
                            Account.FIND_BY_NAME, Account.class);
                    query.setMaxResults(1);
                    query.setParameter(Account.NAME_PARAM, name);
                    Account account = query.getSingleResult();
                    return account;
                }, () -> consumer);
    }

    public Account getAccount(Integer accountId) {
        return getAccount(accountId, null);
    }

    public Account getAccount(Integer accountId, Consumer<Account> consumer) {
        return wrappers.withTransaction(entityManager -> {
            return entityManager.find(Account.class, accountId);
        }, () -> consumer);
    }

    public Account storeAccount(Account account) {
        return storeAccount(account, null);
    }

    public Account storeAccount(Account account, Consumer<Account> consumer) {
        return wrappers.withTransaction(entityManager -> {
            return entityManager.merge(account);
        }, () -> consumer);
    }

    public void deleteAccount(Account account) {
        if (account.getId() == null) {
            return;
        }

        wrappers.withTransaction(entityManager -> {
            Account acct = account;
            if (!entityManager.contains(acct)) {
                acct = entityManager.merge(account);
            }
            entityManager.remove(acct);

            return null;
        }, () -> null);
    }

    public List<Account> listAccounts() {
        return listAccounts(null);
    }

    public List<Account> listAccounts(Consumer<List<Account>> consumer) {
        return wrappers.withTransaction(
                entityManager -> {
                    TypedQuery<Account> query = entityManager.createNamedQuery(
                            Account.FIND_ALL, Account.class);
                    List<Account> result = query.getResultList();

                    return result;
                }, () -> consumer);
    }

    public List<Account> listAccountsStartingWith(String prefix) {
        return listAccountsStartingWith(prefix, null);
    }

    public List<Account> listAccountsStartingWith(String prefix,
            Consumer<List<Account>> consumer) {
        return wrappers.withTransaction(
                entityManager -> {
                    TypedQuery<Account> query = entityManager.createNamedQuery(
                            Account.FIND_BY_NAME_PREFIX, Account.class);
                    query.setParameter(Account.PREFIX_PARAM, prefix + "%");

                    List<Account> result = query.getResultList();
                    return result;
                }, () -> consumer);
    }

}
