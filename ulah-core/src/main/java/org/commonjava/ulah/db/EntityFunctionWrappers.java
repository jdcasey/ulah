package org.commonjava.ulah.db;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

@ApplicationScoped
public class EntityFunctionWrappers {
	
	@Inject
	private EntityManagerFactory entityManagerFactory;
	
	protected EntityFunctionWrappers(){}
	
	public EntityFunctionWrappers(EntityManagerFactory emf)
	{
		this.entityManagerFactory = emf;
	}

	public <T> T withTransaction(Function<EntityManager, T> fn, Supplier<Consumer<T>> supplier)
	{
		EntityManager entityManager = null;
		try
		{
			entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();
			
			T result = fn.apply(entityManager);
			
			entityManager.getTransaction().commit();
			
			Consumer<T> consumer = supplier.get();
			if ( consumer != null )
			{
				consumer.accept( result );
			}
			
			return result;
		}
		finally
		{
			if ( entityManager != null )
			{
				entityManager.close();
			}
		}
	}
	
	public <T> T withoutTransaction(Function<EntityManager, T> fn, Supplier<Consumer<T>> supplier)
	{
		EntityManager entityManager = null;
		try
		{
			entityManager = entityManagerFactory.createEntityManager();
			T result = fn.apply(entityManager);
			
			Consumer<T> consumer = supplier.get();
			if ( consumer != null )
			{
				consumer.accept( result );
			}
			
			return result;
		}
		finally
		{
			if ( entityManager != null )
			{
				entityManager.close();
			}
		}
	}
	
}
