package com.tumm.account.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountMessageLog {

    private String messageId;
    private String customerId;
    private Integer transactionId;
    private Integer accountId;
    private String messageContent;
    private String tranType;
    private String status;
    private String errorMessage;
    private Timestamp createdDate;
    private Timestamp lastUpdatedDate;

}