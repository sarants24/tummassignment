package com.tumm.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumm.account.common.exception.BadRequestException;
import com.tumm.account.common.exception.ServiceException;
import com.tumm.account.entities.AccountTransaction;
import com.tumm.account.entities.AccountTransactionLog;
import com.tumm.account.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;

    /**
     * This method is used to get list of Transaction  details using Account Id
     *
     * @param accountId account id
     * @return List<AccountTransaction>
     */
    @Override
    public List<AccountTransaction> getByAccountId(Integer accountId) {

        if(ObjectUtils.isEmpty(accountId)){
            throw new BadRequestException(MessageFormat.format("Account Id is required", accountId));
        }

        return repository.findByAccountId(accountId);
    }

    /**
     * This method is used to get Transaction  details using Transaction Id
     *
     * @param transactionId
     * @return AccountTransaction
     */
    @Override
    public AccountTransaction getByTransactionId(Integer transactionId) {

        if(ObjectUtils.isEmpty(transactionId)){
            throw new BadRequestException(MessageFormat.format("Transaction Id is required", transactionId));
        }

        return repository.findByTransactionId(transactionId);
    }

    /**
     * This method is used to get Account Transaction Log details using Message Id
     *
     * @param messageId
     * @return AccountTransactionLog
     */
    @Override
    public AccountTransactionLog getByMessageId(String messageId) {

        if(ObjectUtils.isEmpty(messageId)){
            throw new BadRequestException(MessageFormat.format("Message Id is required", messageId));
        }

        return repository.findByMessageId(messageId);
    }

    /**
     * This method is used to create a new transaction
     *
     * @param accountTransaction
     * @return String
     */
    @Override
    @Transactional
    public String create(String messageId, AccountTransaction accountTransaction) throws ServiceException {

        if(ObjectUtils.isEmpty(accountTransaction.getAccountId())) {
            throw new BadRequestException(MessageFormat.format("Account Id is required", accountTransaction.getAccountId()));
        }

        if(ObjectUtils.isEmpty(accountTransaction.getCurrencyId())) {
            throw new BadRequestException(MessageFormat.format("Currency Id is required", accountTransaction.getCurrencyId()));
        }

        if(ObjectUtils.isEmpty(accountTransaction.getTranAmount())) {
            throw new BadRequestException(MessageFormat.format("Transaction Amount is required", accountTransaction.getTranAmount()));
        } else if (accountTransaction.getTranAmount() <= 0) {
            throw new BadRequestException(MessageFormat.format("Invalid Transaction Amount", accountTransaction.getTranAmount()));
        }

        if(ObjectUtils.isEmpty(accountTransaction.getTranDirection())) {
            throw new BadRequestException(MessageFormat.format("Transaction Direction is required", accountTransaction.getTranDirection()));
        }

        if(ObjectUtils.isEmpty(accountTransaction.getTranDescription())) {
            throw new BadRequestException(MessageFormat.format("Transaction Description is required", accountTransaction.getTranDescription()));
        }

        //Insufficient Amount

        try {

            //Create Transaction
            Timestamp currentTimestamp = new Timestamp(new Date().getTime());
            accountTransaction.setCreatedDate(currentTimestamp);
            accountTransaction.setLastUpdatedDate(currentTimestamp);
            repository.insertTransaction(accountTransaction);

            //Create Message Log
            AccountTransactionLog accountTransactionLog = new AccountTransactionLog();
            ObjectMapper mapper = new ObjectMapper();

            currentTimestamp = new Timestamp(new Date().getTime());
            accountTransactionLog.setMessageId(messageId);
            accountTransactionLog.setAccountId(accountTransaction.getAccountId());
            accountTransactionLog.setTransactionId(accountTransaction.getTransactionId());
            accountTransactionLog.setTranType("CREATE_TRANSACTION");
            accountTransactionLog.setStatus("In-Progress");
            accountTransactionLog.setMessageContent(mapper.writeValueAsString(accountTransaction));

            accountTransactionLog.setCreatedDate(currentTimestamp);
            accountTransactionLog.setLastUpdatedDate(currentTimestamp);
            repository.insertAccountTransactionLog(accountTransactionLog);

        } catch (Exception ex) {
            throw new ServiceException("Error Message : " + ex.getMessage());
        }

        return "Success";
    }

    public void updateMessageLog(AccountTransactionLog accountTransactionLog) {
        Timestamp currentTimestamp = new Timestamp(new Date().getTime());
        accountTransactionLog.setCreatedDate(currentTimestamp);
        accountTransactionLog.setLastUpdatedDate(currentTimestamp);
        repository.insertAccountTransactionLog(accountTransactionLog);
    }

    //Update Final Status
    public void updateAccountTransactionLog(AccountTransactionLog accountTransactionLog) {
        Timestamp currentTimestamp = new Timestamp(new Date().getTime());
        accountTransactionLog.setLastUpdatedDate(currentTimestamp);
        repository.updateAccountTransactionLog(accountTransactionLog);
    }

    //Rollback
    @Transactional
    public void deleteTransaction(AccountTransactionLog accountTransactionLog) {

        Timestamp currentTimestamp = new Timestamp(new Date().getTime());
        //Delete Transaction
        repository.deleteTransactionLog(accountTransactionLog);

        //Update Log Status
        accountTransactionLog.setLastUpdatedDate(currentTimestamp);
        repository.updateAccountTransactionLog(accountTransactionLog);
    }
}