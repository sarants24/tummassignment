package com.tumm.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    private Integer transactionId;
    private Integer accountId;
    private Double amount;
    private String currency;
    private String tranDirection;
    private String tranDescription;
    private Double balanceAfterTransaction;

}
