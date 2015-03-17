package org.commonjava.ulah.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "ACCOUNT")
@NamedQueries({
        @NamedQuery(name = Account.FIND_BY_NAME, query = "SELECT a from Account a WHERE a.name = :name ORDER BY a.name"),
        @NamedQuery(name = Account.FIND_BY_NAME_PREFIX, query = "SELECT a from Account a WHERE a.name LIKE :prefix ORDER BY a.name"),
        @NamedQuery(name = Account.FIND_ALL, query = "SELECT a from Account a ORDER BY a.name") })
public class Account {

    public static final String FIND_BY_NAME_PREFIX = "Account.findByNamePrefix";

    public static final String FIND_BY_NAME = "Account.findByName";

    public static final String FIND_ALL = "Account.findAll";

    public static final String ACCOUNT_ID = "ACCOUNT_ID";

    public static final String NAME_PARAM = "name";
    public static final String PREFIX_PARAM = "prefix";

    @Id
    @SequenceGenerator(name = "acct_increment")
    @GeneratedValue(generator = "acct_increment")
    @Column(name = Account.ACCOUNT_ID)
    private Integer id;

    @Column(name = "ACCOUNT_NAME", unique = true, length = 30)
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
