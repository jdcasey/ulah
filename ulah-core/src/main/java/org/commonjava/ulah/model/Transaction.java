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
@Table( name="TRANSACTIONS")
@NamedQueries({
	@NamedQuery( name=Transaction.SUMMARIZE_AMOUNT_BY_TO_ACCOUNT, query="SELECT SUM(t.AMOUNT) from TRANSACTION t WHERE t.TO_ACCOUNT_ID = :accountId"),
	@NamedQuery( name=Transaction.SUMMARIZE_AMOUNT_BY_FROM_ACCOUNT, query="SELECT SUM(t.AMOUNT) from TRANSACTION t WHERE t.FROM_ACCOUNT_ID = :accountId"),
})
public class Transaction {
	
	public static final String SUMMARIZE_AMOUNT_BY_TO_ACCOUNT = "Transaction.summarizeAmountByToAccount";
	public static final String SUMMARIZE_AMOUNT_BY_FROM_ACCOUNT = "Transaction.summarizeAmountByFromAccount";
	
	@Column( name="TRANSACTION_ID" )
	private Long id;
	
	@Column( name="FROM_ACCOUNT_ID" )
	private Account from;
	
	@Column( name="TO_ACCOUNT_ID" )
	private Account to;
	
	@Column( name="AMOUNT" )
	private BigDecimal amount;
	
	@Column( name="DATE" )
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
		this.from = from;
		this.to = to;
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

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TRANSACTION_DATE")
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Id
	@GeneratedValue( generator="increment")
	@GenericGenerator(name="increment", strategy="increment")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Account getFrom() {
		return from;
	}

	public void setFrom(Account from) {
		this.from = from;
	}

	public Account getTo() {
		return to;
	}

	public void setTo(Account to) {
		this.to = to;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

}
