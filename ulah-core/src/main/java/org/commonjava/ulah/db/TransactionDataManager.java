package org.commonjava.ulah.db;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import org.commonjava.ulah.model.Transaction;

@ApplicationScoped
public class TransactionDataManager {
	
	@Inject
	private EntityFunctionWrappers wrappers;
	
	protected TransactionDataManager(){}
	
	public TransactionDataManager( EntityFunctionWrappers wrappers )
	{
		this.wrappers = wrappers;
	}
	
	public Transaction getTransaction( Long transactionId )
	{
		return getTransaction( transactionId, null );
	}
	
	public Transaction getTransaction( Long transactionId, Consumer<Transaction> consumer )
	{
		return wrappers.withoutTransaction(entityManager -> {
			return entityManager.find(Transaction.class, transactionId);
		}, () -> consumer );
	}
	
	public Transaction storeTransaction( Transaction transaction )
	{
		return storeTransaction( transaction, null );
	}
	
	public Transaction storeTransaction( Transaction transaction, Consumer<Transaction> consumer )
	{
		return wrappers.withTransaction( entityManager -> {
			if (entityManager.contains(transaction))
			{
				entityManager.merge(transaction);
			}
			else
			{
				entityManager.persist(transaction);
			}
			
			return transaction;
		}, () -> consumer );
	}
	
	public void deleteTransaction( Transaction transaction )
	{
		wrappers.withTransaction( entityManager -> {
			entityManager.remove(transaction);
			return null;
		}, () -> null );
	}
	
	public BigDecimal getAccountBalance( Integer accountId )
	{
		return getAccountBalance( accountId, null );
	}
	
	public BigDecimal getAccountBalance( Integer accountId, Consumer<BigDecimal> consumer )
	{
		return wrappers.withTransaction(entityManager ->{
			TypedQuery<BigDecimal> subtractions= entityManager.createNamedQuery(Transaction.SUMMARIZE_AMOUNT_BY_FROM_ACCOUNT, BigDecimal.class);
			subtractions.setParameter(Transaction.ACCOUNT_ID_PARAM, accountId);
			BigDecimal sub = subtractions.getSingleResult();
			
			TypedQuery<BigDecimal> additions = entityManager.createNamedQuery(Transaction.SUMMARIZE_AMOUNT_BY_TO_ACCOUNT, BigDecimal.class);
			additions.setParameter(Transaction.ACCOUNT_ID_PARAM, accountId);
			BigDecimal add = additions.getSingleResult();
			
			return add.subtract(sub);
		}, () -> consumer );
	}
	
	public List<Transaction> getAccountTransactions( Integer accountId, Date after, Date before, int skip, int limit )
	{
		return getAccountTransactions( accountId, after, before, skip, limit, null );
	}
	
	public List<Transaction> getAccountTransactions( Integer accountId, Date after, Date before, int skip, int limit, Consumer<List<Transaction>> consumer )
	{
		return wrappers.withTransaction( entityManager -> {
			TypedQuery<Transaction> query = entityManager.createNamedQuery(Transaction.ACCOUNT_TRANSACTIONS_WITHIN_DATES, Transaction.class);
			query.setParameter(Transaction.ACCOUNT_ID_PARAM, accountId);
			query.setParameter(Transaction.BEFORE_DATE_PARAM, before);
			query.setParameter(Transaction.AFTER_DATE_PARAM, after);
			
			query.setFirstResult(skip);
			query.setMaxResults(limit);
			
			return query.getResultList();
		}, () -> consumer );
	}

	public List<Transaction> getAllTransactions( Date after, Date before, int skip, int limit )
	{
		return getAllTransactions( after, before, skip, limit, null );
	}
	
	public List<Transaction> getAllTransactions( Date after, Date before, int skip, int limit, Consumer<List<Transaction>> consumer )
	{
		return wrappers.withTransaction( entityManager -> {
			TypedQuery<Transaction> query = entityManager.createNamedQuery(Transaction.ALL_TRANSACTIONS_WITHIN_DATES, Transaction.class);
			query.setParameter(Transaction.BEFORE_DATE_PARAM, before);
			query.setParameter(Transaction.AFTER_DATE_PARAM, after);
			
			query.setFirstResult(skip);
			query.setMaxResults(limit);
			
			return query.getResultList();
		}, () -> consumer );
	}

}
