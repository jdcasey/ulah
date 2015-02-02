package org.commonjava.ulah.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "ACCOUNT")
@NamedQueries({
	@NamedQuery(name = Account.FIND_BY_NAME, query = "SELECT a from ACCOUNT a WHERE a.ACCOUNT_NAME = :name"),
	@NamedQuery(name = Account.FIND_BY_NAME_PREFIX, query = "SELECT a from ACCOUNT a WHERE a.ACCOUNT_NAME LIKE :prefix"),
	@NamedQuery(name = Account.FIND_ALL, query = "SELECT a from ACCOUNT a")
})
public class Account {

	public static final String FIND_BY_NAME_PREFIX = "Account.findByNamePrefix";

	public static final String FIND_BY_NAME = "Account.findByName";

	public static final String FIND_ALL = "Account.findAll";

	@Column( name="ACCOUNT_ID" )
	private Integer id;

	@Column( name="ACCOUNT_NAME" )
	private String name;
	
	private String description;

	protected Account() {
	}

	public Account(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	public Integer getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
