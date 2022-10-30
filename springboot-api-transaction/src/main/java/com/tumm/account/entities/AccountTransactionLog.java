package com.tumm.account.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountTransactionLog {

    private String messageId;
    private Integer transactionId;
    private Integer accountId;
    private String messageContent;
    private Double tranAmount;
    private String tranDirection;
    private String currencyId;
    private String tranType;
    private String status;
    private String errorMessage;
    private Timestamp createdDate;
    private Timestamp lastUpdatedDate;

}