package com.tumm.account.repositories;

import com.tumm.account.entities.AccountTransaction;
import com.tumm.account.entities.AccountTransactionLog;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This is a repository class for Account object. This class is used to
 * * 1. Get an Account by Customer Id
 * 2. Get an Account by Account Id
 * 3. Create an Account with multi currency
 *
 */

@Mapper
@Repository
public interface TransactionRepository {

    @Select("SELECT * FROM account_transaction WHERE account_id = #{accountId}")
    @Results(value = {
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "transactionId", column = "transaction_id"),
            @Result(property = "currencyId", column = "currency_id"),
            @Result(property = "tranAmount", column = "tran_amount"),
            @Result(property = "tranDirection", column = "tran_direction"),
            @Result(property = "tranDescription", column = "tran_description")
    })
    List<AccountTransaction> findByAccountId(Integer accountId);

    @Select("SELECT * FROM account_transaction WHERE transaction_id = #{transactionId}")
    @Results(value = {
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "transactionId", column = "transaction_id"),
            @Result(property = "currencyId", column = "currency_id"),
            @Result(property = "tranAmount", column = "tran_amount"),
            @Result(property = "tranDirection", column = "tran_direction"),
            @Result(property = "tranDescription", column = "tran_description")
    })
    AccountTransaction findByTransactionId(Integer transactionId);

    @Select("SELECT * FROM account_transaction_log WHERE message_id = #{messageId}")
    @Results(value = {
            @Result(property = "messageId", column = "message_id"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "transactionId", column = "tran_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "errorMessage", column = "error_message")
    })
    AccountTransactionLog findByMessageId(String messageId);

    //Create Accounts
    @Insert("INSERT INTO account_transaction(account_id,currency_id,tran_amount," +
            "tran_direction,tran_description,created_date,last_updated_date) " +
            " VALUES (#{accountId}, #{currencyId}, #{tranAmount}," +
            " #{tranDirection}, #{tranDescription}, #{createdDate}, #{lastUpdatedDate})")
    @Options(useGeneratedKeys = true, keyProperty = "transactionId", keyColumn = "transaction_id")
    void insertTransaction(AccountTransaction accountTransaction);

    @Insert("INSERT INTO account_transaction_log(message_id,account_id,tran_id,message_content," +
            "tran_type,status,error_message,created_date,last_updated_date) " +
            " VALUES (#{messageId},#{accountId}, #{transactionId}, #{messageContent}," +
            " #{tranType},#{status}, #{errorMessage}, #{createdDate}, #{lastUpdatedDate})")
    void insertAccountTransactionLog(AccountTransactionLog accountTransactionLog);

    @Insert("Update account_transaction_log set status = #{status}," +
            "last_updated_date = #{lastUpdatedDate} where message_id = #{messageId}")
    void updateAccountTransactionLog(AccountTransactionLog accountTransactionLog);

    @Insert("delete from account_transaction where transaction_id = #{transactionId}")
    void deleteTransactionLog(AccountTransactionLog accountTransactionLog);
}