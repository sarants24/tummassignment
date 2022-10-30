package com.tumm.account.service;

import com.tumm.account.common.exception.ServiceException;
import com.tumm.account.entities.AccountTransaction;
import com.tumm.account.entities.AccountTransactionLog;

import java.util.List;

public interface TransactionService {

    List<AccountTransaction> getByAccountId(Integer transactionId);

    AccountTransaction getByTransactionId(Integer transactionId);

    AccountTransactionLog getByMessageId(String messageId);

    String create(String messageId, AccountTransaction accountTransaction) throws ServiceException;

    void updateMessageLog(AccountTransactionLog accountTransactionLog);

    void updateAccountTransactionLog(AccountTransactionLog accountTransactionLog);

    void deleteTransaction(AccountTransactionLog accountTransactionLog);

}
