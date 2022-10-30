package com.tumm.account.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountTransaction {

	private Integer transactionId;

	private Integer accountId;

	@NotBlank
    private String currencyId;

	private Double tranAmount;
	
	@NotBlank
    private String tranDirection;

	@NotBlank
    private String tranDescription;

    private Timestamp createdDate;

    private Timestamp lastUpdatedDate;

}