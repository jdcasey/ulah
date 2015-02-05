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
@Table(name = "TXTAG")
@NamedQueries({
        @NamedQuery(name = TransactionTag.ALL_TAGS, query = "Select t from TransactionTag t"),
        @NamedQuery(name = TransactionTag.FIND_BY_NAME, query = "SELECT t from TransactionTag t WHERE t.name = :name") })
public class TransactionTag {

    public static final String ALL_TAGS = "TransactionTag.allTags";
    public static final String FIND_BY_NAME = "TransactionTag.findByName";

    public static final String NAME_PARAM = "name";

    @Id
    @Column(name = "TAG_ID")
    @GeneratedValue(generator = "tag_increment")
    @GenericGenerator(name = "tag_increment", strategy = "increment")
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;

    public TransactionTag() {
    }

    public TransactionTag(String name) {
        this.name = name.toLowerCase();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

}
