package com.tumm.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumm.account.common.exception.BadRequestException;
import com.tumm.account.common.exception.ServiceException;
import com.tumm.account.dto.Transaction;
import com.tumm.account.entities.Account;
import com.tumm.account.entities.AccountCurrencies;
import com.tumm.account.entities.AccountMessageLog;
import com.tumm.account.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository repository;

    /**
     * This method is used to get Account details using Customer Id
     *
     * @param customerId
     * @return Account
     */
    @Override
    public Account getByCustomerId(String customerId) {

        if(ObjectUtils.isEmpty(customerId)){
            throw new BadRequestException(MessageFormat.format("Customer Id is required", customerId));
        }

        return repository.findByCustomerId(customerId);
    }

    /**
     * This method is used to get Account details using Account Id
     *
     * @param accountId
     * @return Account
     */
    @Override
    public Account getByAccountId(Integer accountId) {

        if(ObjectUtils.isEmpty(accountId)){
            throw new BadRequestException(MessageFormat.format("Account Id is required", accountId));
        }

        return repository.findByAccountId(accountId);
    }

    /**
     * This method is used to get Account Currency details using Account Id and Currency
     *
     * @param accountId
     * @return Account
     */
    @Override
    public AccountCurrencies getAccountByCurrencyAndId(Integer accountId,String currencyId) {

        if(ObjectUtils.isEmpty(accountId)){
            throw new BadRequestException(MessageFormat.format("Account Id is required", accountId));
        }

        if(ObjectUtils.isEmpty(currencyId)){
            throw new BadRequestException(MessageFormat.format("Currency Id is required", currencyId));
        }

        return repository.findAccountByCurrencyAndId(accountId,currencyId);
    }

    /**
     * This method is used to get Account Message details using Message Id
     *
     * @param messageId
     * @return AccountMessageLog
     */
    @Override
    public AccountMessageLog getByMessageId(String messageId) {

        if(ObjectUtils.isEmpty(messageId)){
            throw new BadRequestException(MessageFormat.format("Message Id is required", messageId));
        }

        return repository.findByMessageId(messageId);
    }

    /**
     * This method is used to create a new account
     *
     * @param account
     * @return Account
     */
    @Override
    @Transactional
    public String create(String messageId, Account account) throws ServiceException {

        if(ObjectUtils.isEmpty(account.getCustomerId())) {
            throw new BadRequestException(MessageFormat.format("Customer Id is required", account.getCustomerId()));
        }

        if(ObjectUtils.isEmpty(account.getCountry())) {
            throw new BadRequestException(MessageFormat.format("Country Id is required", account.getCountry()));
        }

        if(ObjectUtils.isEmpty(account.getAccountCurrenciesList())) {
            throw new BadRequestException(MessageFormat.format("Account Currency Id is required", account.getCountry()));
        }  else if (validateCurrencies(account.getAccountCurrenciesList())) {
            throw new BadRequestException(MessageFormat.format("Invalid Account Currency", account.getCountry()));
        }

        //Validate account already exists for given customer
        /*
        Account accountByCustomerId = getByCustomerId(account.getCustomerId());
        if(!ObjectUtils.isEmpty(accountByCustomerId)){
            throw new DuplicateException(MessageFormat.format("Customer {0} already exists in the system", account.getCustomerId()));
        }
        */

        try {

            //Create Account
            Timestamp currentTimestamp = new Timestamp(new Date().getTime());
            account.setCreatedDate(currentTimestamp);
            account.setLastUpdatedDate(currentTimestamp);
            repository.insertAccount(account);

            //Create Account Currencies
            if (!ObjectUtils.isEmpty(account.getAccountCurrenciesList())) {
                for (AccountCurrencies accountCurrencies : account.getAccountCurrenciesList()) {
                    accountCurrencies.setAccountId(account.getAccountId());
                    accountCurrencies.setBalanceAmount(0.0);
                    currentTimestamp = new Timestamp(new Date().getTime());
                    accountCurrencies.setCreatedDate(currentTimestamp);
                    accountCurrencies.setLastUpdatedDate(currentTimestamp);
                    repository.insertAccountCurrencies(accountCurrencies);
                }
            }

            //Create Message Log
            AccountMessageLog accountMessageLog = new AccountMessageLog();
            ObjectMapper mapper = new ObjectMapper();

            currentTimestamp = new Timestamp(new Date().getTime());
            accountMessageLog.setMessageId(messageId);
            accountMessageLog.setAccountId(account.getAccountId());
            accountMessageLog.setCustomerId(account.getCustomerId());
            accountMessageLog.setTranType("CREATE_ACCOUNT");
            accountMessageLog.setStatus("Success");
            accountMessageLog.setMessageContent(mapper.writeValueAsString(account));

            accountMessageLog.setCreatedDate(currentTimestamp);
            accountMessageLog.setLastUpdatedDate(currentTimestamp);
            repository.insertAccountMessageLog(accountMessageLog);

        } catch (Exception ex) {
            throw new ServiceException("Error Message : " + ex.getMessage());
        }

        return "Success";
    }

    public void updateMessageLog(AccountMessageLog accountMessageLog) {
        Timestamp currentTimestamp = new Timestamp(new Date().getTime());
        accountMessageLog.setCreatedDate(currentTimestamp);
        accountMessageLog.setLastUpdatedDate(currentTimestamp);
        repository.insertAccountMessageLog(accountMessageLog);
    }

    public void update(String messageId, Transaction accountTransaction) throws ServiceException {
        if(ObjectUtils.isEmpty(accountTransaction.getAccountId())) {
            throw new BadRequestException(MessageFormat.format("Account Id is required", accountTransaction.getAccountId()));
        }

        if(ObjectUtils.isEmpty(accountTransaction.getAmount())) {
            throw new BadRequestException(MessageFormat.format("Amount  is required", accountTransaction.getAmount()));
        }

        if(accountTransaction.getAmount() <= 0) {
            throw new BadRequestException(MessageFormat.format("Invalid Amount", accountTransaction.getAmount()));
        }

        try {
            AccountCurrencies accountCurrencies = new AccountCurrencies();
            accountCurrencies.setAccountId(accountTransaction.getAccountId());
            accountCurrencies.setBalanceAmount(accountTransaction.getAmount());
            accountCurrencies.setCurrencyId(accountTransaction.getCurrency());

            Timestamp currentTimestamp = new Timestamp(new Date().getTime());
            accountCurrencies.setLastUpdatedDate(currentTimestamp);

            System.out.println("accountTransaction.getTranDirection()"+accountTransaction.getTranDirection());

            if("IN".equals(accountTransaction.getTranDirection())) {
                repository.updateAccountCredit(accountCurrencies);
            } else  if("OUT".equals(accountTransaction.getTranDirection())) {
                repository.updateAccountDebit(accountCurrencies);
            }

            //Create Message Log
            AccountMessageLog accountMessageLog = new AccountMessageLog();
            ObjectMapper mapper = new ObjectMapper();

            currentTimestamp = new Timestamp(new Date().getTime());
            accountMessageLog.setMessageId(messageId);
            accountMessageLog.setAccountId(accountTransaction.getAccountId());
            accountMessageLog.setTranType("UPDATE_ACCOUNT");
            accountMessageLog.setStatus("Success");
            accountMessageLog.setMessageContent(mapper.writeValueAsString(accountTransaction));
            accountMessageLog.setCreatedDate(currentTimestamp);
            accountMessageLog.setLastUpdatedDate(currentTimestamp);
            repository.insertAccountMessageLog(accountMessageLog);
        } catch (Exception ex) {
            throw new ServiceException("Error Message : " + ex.getMessage());
        }
    }

    private boolean validateCurrencies(List<AccountCurrencies> accountCurrenciesList) {
        boolean flag = false;

        String[] currValues = {"USD","GBP","EUR","SEK"};

        for (AccountCurrencies accCurr : accountCurrenciesList)
        {
            if(!Arrays.stream(currValues).anyMatch(accCurr.getCurrencyId()::equals)) {
                flag = true;
             }
        }

        System.out.println("validateCurrencies" + flag);
        return flag;
    }
}