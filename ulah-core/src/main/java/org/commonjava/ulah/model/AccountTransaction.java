package org.commonjava.ulah.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "ENTRY")
@NamedQueries({
        @NamedQuery(name = AccountTransaction.SUMMARIZE_AMOUNT_BY_TO_ACCOUNT, query = "SELECT SUM(t.amount) from AccountTransaction t WHERE t.toAccount.id = :accountId"),
        @NamedQuery(name = AccountTransaction.SUMMARIZE_AMOUNT_BY_FROM_ACCOUNT, query = "SELECT SUM(t.amount) from AccountTransaction t WHERE t.fromAccount.id = :accountId"),
        @NamedQuery(name = AccountTransaction.ALL_TRANSACTIONS_WITHIN_DATES, query = "SELECT t from AccountTransaction t WHERE t.date <= :beforeDate AND t.date >= :afterDate ORDER BY t.date"),
        @NamedQuery(name = AccountTransaction.ACCOUNT_TRANSACTIONS_WITHIN_DATES, query = "SELECT t from AccountTransaction t WHERE (t.fromAccount.id=:accountId OR t.toAccount.id=:accountId) AND t.date <= :beforeDate AND t.date >= :afterDate ORDER BY t.date") })
public class AccountTransaction {

    public static final String SUMMARIZE_AMOUNT_BY_TO_ACCOUNT = "AccountTransaction.summarizeAmountByToAccount";
    public static final String SUMMARIZE_AMOUNT_BY_FROM_ACCOUNT = "AccountTransaction.summarizeAmountByFromAccount";
    public static final String ACCOUNT_TRANSACTIONS_WITHIN_DATES = "AccountTransaction.accountAccountsEntrysWithinDates";
    public static final String ALL_TRANSACTIONS_WITHIN_DATES = "AccountTransaction.allAccountsEntrysWithinDates";

    public static final String ACCOUNT_ID_PARAM = "accountId";
    public static final String AFTER_DATE_PARAM = "afterDate";
    public static final String BEFORE_DATE_PARAM = "beforeDate";

    @Id
    @Column(name = "ENTRY_ID")
    @GeneratedValue(generator = "txn_increment")
    @GenericGenerator(name = "txn_increment", strategy = "increment")
    private Long id;

    @ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    private Account fromAccount;

    @ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    private Account toAccount;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "RECONCILED")
    private Boolean reconciled;

    private String memo;

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    protected AccountTransaction() {
    }

    public AccountTransaction(Account from, Account to, BigDecimal amount,
            String memo) {
        fromAccount = from;
        toAccount = to;
        this.amount = amount;
        this.memo = memo;
        date = new Date();
    }

    public Boolean getReconciled() {
        return reconciled;
    }

    public void setReconciled(Boolean reconciled) {
        this.reconciled = reconciled;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account from) {
        fromAccount = from;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account to) {
        toAccount = to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}
