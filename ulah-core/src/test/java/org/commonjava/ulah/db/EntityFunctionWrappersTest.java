package org.commonjava.ulah.db;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.commonjava.ulah.model.Account;
import org.junit.Before;
import org.junit.Test;

public class EntityFunctionWrappersTest {
	
	private EntityFunctionWrappers wrappers;
	private EntityManagerFactory entityManagerFactory;
	
	@Before
	public void setup()
	{
	    entityManagerFactory = Persistence.createEntityManagerFactory( Account.class.getPackage().getName() );
		wrappers = new EntityFunctionWrappers(entityManagerFactory);
	}
	
	public void teardown()
	{
		if ( entityManagerFactory != null ) entityManagerFactory.close();
	}

	@Test
	public void storingAccountPopulatesId() {
		Account acct = new Account("test");
		Account result = wrappers.withTransaction(entityManager -> {
			entityManager.persist(acct);
			return acct;
		}, () -> null );
		
		assertThat( result.getId(), notNullValue() );
	}

}
