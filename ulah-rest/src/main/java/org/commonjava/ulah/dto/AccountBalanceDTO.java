package org.commonjava.ulah.dto;

import java.math.BigDecimal;

public class AccountBalanceDTO {

    private Integer accountId;
    private BigDecimal balance;

    public AccountBalanceDTO() {
    }

    public AccountBalanceDTO(Integer accountId, BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

}
