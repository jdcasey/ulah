package org.commonjava.ulah.dto;

import java.util.List;

import org.commonjava.ulah.model.Account;

public class AccountListDTO {

    private List<Account> items;

    public AccountListDTO() {
    }

    public AccountListDTO(List<Account> items) {
        this.items = items;
    }

    public List<Account> getItems() {
        return items;
    }

    public void setItems(List<Account> items) {
        this.items = items;
    }

}
