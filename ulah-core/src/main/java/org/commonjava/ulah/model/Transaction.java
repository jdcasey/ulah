package org.commonjava.ulah.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table( name="TRANSACTION")
@NamedQueries({
	@NamedQuery( name=Transaction.SUMMARIZE_AMOUNT_BY_TO_ACCOUNT, query="SELECT SUM(t.amount) from Transaction t WHERE t.toAccountId = :accountId"),
	@NamedQuery( name=Transaction.SUMMARIZE_AMOUNT_BY_FROM_ACCOUNT, query="SELECT SUM(t.amount) from Transaction t WHERE t.fromAccountId = :accountId"),
})
public class Transaction {
	
	public static final String SUMMARIZE_AMOUNT_BY_TO_ACCOUNT = "Transaction.summarizeAmountByToAccount";
	public static final String SUMMARIZE_AMOUNT_BY_FROM_ACCOUNT = "Transaction.summarizeAmountByFromAccount";
	public static final String ACCOUNT_ID_PARAM = "accountId";
	
	@Id
	@Column( name="TRANSACTION_ID" )
	@GeneratedValue( generator="txn_increment")
	@GenericGenerator(name="txn_increment", strategy="increment")
	private Long id;
	
	@Column( name="FROM_ACCOUNT_ID" )
	private Integer fromAccountId;
	
	@Column( name="TO_ACCOUNT_ID" )
	private Integer toAccountId;
	
	@Column( name="AMOUNT" )
	private BigDecimal amount;
	
	@Column( name="DATE" )
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	
	@Column( name="RECONCILED" )
	private Boolean reconciled;
	
	private String memo;
	
	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	protected Transaction(){}
	
	public Transaction( Account from, Account to, BigDecimal amount, String memo )
	{
		this.fromAccountId = from.getId();
		this.toAccountId = to.getId();
		this.amount = amount;
		this.memo = memo;
		this.date = new Date();
	}

	public Transaction( Integer fromAccount, Integer toAccount, BigDecimal amount, String memo )
	{
		this.fromAccountId = fromAccount;
		this.toAccountId = toAccount;
		this.amount = amount;
		this.memo = memo;
		this.date = new Date();
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

	public Integer getFromAccountId() {
		return fromAccountId;
	}

	public void setFromAccountId(Integer from) {
		this.fromAccountId = from;
	}

	public Integer getToAccountId() {
		return toAccountId;
	}

	public void setToAccountId(Integer to) {
		this.toAccountId = to;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

}
