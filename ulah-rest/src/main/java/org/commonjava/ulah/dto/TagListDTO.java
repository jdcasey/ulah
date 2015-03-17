package org.commonjava.ulah.dto;

import java.util.List;

import org.commonjava.ulah.model.TransactionTag;

public class TagListDTO {

    private List<TransactionTag> items;

    public TagListDTO() {
    }

    public TagListDTO(List<TransactionTag> items) {
        this.items = items;
    }

    public List<TransactionTag> getItems() {
        return items;
    }

    public void setItems(List<TransactionTag> items) {
        this.items = items;
    }

}
