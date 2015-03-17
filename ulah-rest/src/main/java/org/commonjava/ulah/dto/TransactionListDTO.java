package org.commonjava.ulah.dto;

import java.util.List;

public class TransactionListDTO {

    private List<AccountTransactionDTO> items;

    public TransactionListDTO() {
    }

    public TransactionListDTO(List<AccountTransactionDTO> items) {
        this.items = items;
    }

    public List<AccountTransactionDTO> getItems() {
        return items;
    }

    public void setItems(List<AccountTransactionDTO> items) {
        this.items = items;
    }

}
