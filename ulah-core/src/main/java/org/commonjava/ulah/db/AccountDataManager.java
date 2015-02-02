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
	
	protected AccountDataManager(){}
	
	public AccountDataManager( EntityFunctionWrappers wrappers )
	{
		this.wrappers = wrappers;
	}
	
	public Account getAccount( String name )
	{
		return getAccount(name, null);
	}
	
	public Account getAccount( String name, Consumer<Account> consumer )
	{
		return wrappers.withoutTransaction( entityManager -> {
			TypedQuery<Account> query = entityManager.createNamedQuery(Account.FIND_BY_NAME, Account.class);
			query.setMaxResults(1);
			query.setParameter(0, name);
			Account account = query.getSingleResult();
			return account;
		}, () -> consumer );
	}
	
	public Account getAccount( Integer accountId )
	{
		return getAccount( accountId, null );
	}
	
	public Account getAccount( Integer accountId, Consumer<Account> consumer )
	{
		return wrappers.withoutTransaction( entityManager -> {
			Account account = entityManager.find(Account.class, accountId);
			return account;
		}, () -> consumer );
	}
	
	public Account storeAccount( Account account )
	{
		return storeAccount( account, null );
	}
	
	public Account storeAccount( Account account, Consumer<Account> consumer )
	{
		return wrappers.withTransaction( entityManager -> {
			if (entityManager.contains(account))
			{
				entityManager.merge(account);
			}
			else
			{
				entityManager.persist(account);
			}
			
			// TODO: Is the accountId set in the persist method??
//			entityManager.refresh(account);
			return account;
		}, () -> consumer );
	}
	
	public void deleteAccount( Account account )
	{
		wrappers.withTransaction( entityManager -> {
			entityManager.remove(account);
			
			return null;
		}, () -> null);
	}
	
	public List<Account> listAccounts()
	{
		return listAccounts( null );
	}

	public List<Account> listAccounts( Consumer<List<Account>> consumer )
	{
		return wrappers.withoutTransaction( entityManager ->{
			TypedQuery<Account> query = entityManager.createNamedQuery(Account.FIND_ALL, Account.class);
			List<Account> result = query.getResultList();

			return result;
		}, () -> consumer);
	}

	public List<Account> listAccountsStartingWith(String prefix)
	{
		return listAccountsStartingWith( prefix, null );
	}

	public List<Account> listAccountsStartingWith( String prefix, Consumer<List<Account>> consumer )
	{
		return wrappers.withoutTransaction( entityManager ->{
			TypedQuery<Account> query = entityManager.createNamedQuery(Account.FIND_ALL, Account.class);
			query.setParameter(0, prefix + "%" );
			
			List<Account> result = query.getResultList();
			return result;
		}, () -> consumer);
	}

}
