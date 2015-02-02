package org.commonjava.ulah.inject;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.commonjava.ulah.model.Account;

@ApplicationScoped
public class HibernateProducer {
	
	private EntityManagerFactory emf;
	
	@PostConstruct
	public void init()
	{
	    emf = Persistence.createEntityManagerFactory( Account.class.getPackage().getName() );		
	}
	
	@Produces
	@Default
	public EntityManagerFactory getEntityManagerFactory()
	{
		return emf;
	}

}
