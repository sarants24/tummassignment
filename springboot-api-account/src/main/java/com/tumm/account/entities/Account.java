package com.tumm.account.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    private Integer accountId;

    @NotBlank
    private String customerId;

    @NotBlank
    private String country;

    private Timestamp createdDate;

    private Timestamp lastUpdatedDate;

    //@NotBlank
    @Schema(type = "string", allowableValues = { "EUR", "SEK", "GBP", "USD" })
    private List<AccountCurrencies> accountCurrenciesList;
}