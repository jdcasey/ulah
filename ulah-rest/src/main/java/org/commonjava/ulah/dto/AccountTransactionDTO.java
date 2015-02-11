package org.commonjava.ulah.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.commonjava.ulah.db.AccountDataManager;
import org.commonjava.ulah.model.Account;
import org.commonjava.ulah.model.AccountTransaction;
import org.commonjava.ulah.model.TransactionTag;

public class AccountTransactionDTO {

    private Long id;

    private Integer fromAccount;

    private Integer toAccount;

    private BigDecimal amount;

    private Date date;

    private Boolean reconciled;

    private String memo;

    private Set<String> tags;

    public AccountTransactionDTO() {
    }

    public AccountTransactionDTO(AccountTransaction txn) {
        id = txn.getId();
        amount = txn.getAmount();
        date = txn.getDate();
        reconciled = txn.getReconciled();
        memo = txn.getMemo();

        fromAccount = txn.getFromAccount() == null ? null : txn
                .getFromAccount().getId();

        toAccount = txn.getToAccount() == null ? null : txn.getToAccount()
                .getId();

        Set<String> tags = null;
        if (txn.getTags() != null) {
            tags = new HashSet<>();

            for (TransactionTag tag : txn.getTags()) {
                tags.add(tag.getName());
            }
        }

        this.tags = tags;
    }

    public AccountTransaction toTransaction(AccountDataManager accounts) {
        Account from = null;
        if (fromAccount != null) {
            from = accounts.getAccount(fromAccount);
        }

        Account to = null;
        if (toAccount != null) {
            to = accounts.getAccount(toAccount);
        }

        AccountTransaction txn = new AccountTransaction(from, to, amount, memo);
        txn.setDate(date);
        txn.setReconciled(reconciled);

        if (tags != null) {
            Set<TransactionTag> ttags = new HashSet<>();
            for (String tag : tags) {
                ttags.add(new TransactionTag(tag));
            }

            txn.setTags(ttags);
        }

        return txn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Integer fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Integer getToAccount() {
        return toAccount;
    }

    public void setToAccount(Integer toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getReconciled() {
        return reconciled;
    }

    public void setReconciled(Boolean reconciled) {
        this.reconciled = reconciled;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

}
