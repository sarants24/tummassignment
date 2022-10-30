package com.tumm.account.service;

import com.tumm.account.common.exception.ServiceException;
import com.tumm.account.dto.Transaction;
import com.tumm.account.entities.Account;
import com.tumm.account.entities.AccountCurrencies;
import com.tumm.account.entities.AccountMessageLog;

public interface AccountService {

    Account getByCustomerId(String customerId);

    Account getByAccountId(Integer accountId);

    AccountCurrencies getAccountByCurrencyAndId(Integer accountId,String currencyId);

    AccountMessageLog getByMessageId(String messageId);

    String create(String messageId, Account account) throws ServiceException;

    void updateMessageLog(AccountMessageLog accountMessageLog);

    void update(String messageId, Transaction accountTransaction) throws ServiceException;
}
