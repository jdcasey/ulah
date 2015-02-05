package org.commonjava.ulah.db;

import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
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
        Account result = null;
        try {
            result = wrappers.withTransaction(
                    entityManager -> {
                        TypedQuery<Account> query = entityManager
                                .createNamedQuery(Account.FIND_BY_NAME,
                                        Account.class);
                        query.setMaxResults(1);
                        query.setParameter(Account.NAME_PARAM, name);
                        Account account = query.getSingleResult();
                        return account;
                    }, () -> consumer);
        } catch (NoResultException e) {
            if (consumer != null) {
                consumer.accept(result);
            }
        }

        return result;
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

    public Account getOrCreateAccountByName(Account acct) {
        return getOrCreateAccountByName(acct, null);
    }

    public Account getOrCreateAccountByName(Account acct,
            Consumer<Account> consumer) {
        Account result = null;
        if (acct.getId() != null) {
            result = wrappers.withTransaction(entityManager -> {
                return entityManager.getReference(Account.class, acct.getId());
            }, () -> null);
        } else if (acct.getName() != null) {
            result = getAccount(acct.getName());
        }

        if (result == null) {
            return storeAccount(acct, consumer);
        } else {
            if (consumer != null) {
                consumer.accept(result);
            }

            return result;
        }
    }

}
