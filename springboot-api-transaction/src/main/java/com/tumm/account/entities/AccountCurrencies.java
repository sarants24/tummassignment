package com.tumm.account.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountCurrencies {

    private Integer accountId;

    @NotBlank
    private String currencyId;

    @NotBlank
    private Double balanceAmount;

    private Timestamp createdDate;

    private Timestamp lastUpdatedDate;
}
