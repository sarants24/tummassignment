package com.tumm.account.repositories;

import com.tumm.account.entities.Account;
import com.tumm.account.entities.AccountCurrencies;
import com.tumm.account.entities.AccountMessageLog;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This is a repository class for Account object. This class is used to
 *
 * 1. Get an Account by Customer Id
 * 2. Get an Account by Account Id
 * 3. Create an Account with multi currency
 *
 */

@Mapper
@Repository
public interface AccountRepository {

    @Select("SELECT * FROM account WHERE customer_id = #{customerId}")
    @Results(value = {
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "customerId", column = "customer_id"),
            @Result(property = "country", column = "country"),
            @Result(property="accountCurrenciesList", javaType=List.class, column="account_id",
                    many=@Many(select="findAccountsByCurrencies"))
    })
    Account findByCustomerId(String customerId);

    @Select("SELECT * FROM account WHERE account_id = #{accountId}")
    @Results(value = {
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "customerId", column = "customer_id"),
            @Result(property = "country", column = "country"),
            @Result(property="accountCurrenciesList", javaType=List.class, column="account_id",
                    many=@Many(select="findAccountCurrencies"))
    })
    Account findByAccountId(Integer accountId);

    @Select("SELECT account_id, currency_id, balance_amount" +
            " FROM account_currencies WHERE account_id = #{accountId}")
    @Results(value = {
            @Result(property="accountId", column="account_id"),
            @Result(property="currencyId", column="currency_id"),
            @Result(property="balanceAmount", column="balance_amount")})
    List<AccountCurrencies> findAccountCurrencies(Integer accountId);

    @Select("SELECT account_id, currency_id, balance_amount" +
            " FROM account_currencies WHERE account_id = #{accountId} and" +
            " currency_id = #{currencyId}")
    @Results(value = {
            @Result(property="accountId", column="account_id"),
            @Result(property="currencyId", column="currency_id"),
            @Result(property="balanceAmount", column="balance_amount")})
    AccountCurrencies findAccountByCurrencyAndId(Integer accountId,String currencyId);

    @Select("SELECT * FROM account_message_log WHERE message_id = #{messageId}")
    @Results(value = {
            @Result(property = "messageId", column = "message_id"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "customerId", column = "customer_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "errorMessage", column = "error_message")
    })
    AccountMessageLog findByMessageId(String messageId);

    //Create Accounts
    @Insert("INSERT INTO account(customer_id,country,created_date,last_updated_date) " +
            " VALUES (#{customerId}, #{country}, #{createdDate}, #{lastUpdatedDate})")
    @Options(useGeneratedKeys = true, keyProperty = "accountId", keyColumn = "account_id")
    void insertAccount(Account account);

    @Insert("INSERT INTO account_currencies(account_id,currency_id,balance_amount,created_date,last_updated_date) " +
            " VALUES (#{accountId},#{currencyId}, #{balanceAmount}, #{createdDate}, #{lastUpdatedDate})")
    void insertAccountCurrencies(AccountCurrencies accountCurrencies);

    @Insert("INSERT INTO account_message_log(message_id,customer_id,account_id,message_content," +
            "tran_type,status,error_message,created_date,last_updated_date) " +
            " VALUES (#{messageId},#{customerId}, #{accountId}, #{messageContent}," +
            " #{tranType},#{status}, #{errorMessage}, #{createdDate}, #{lastUpdatedDate})")
    void insertAccountMessageLog(AccountMessageLog accountMessageLog);

    //Update Accounts - Debit
    @Update("Update account_currencies set balance_amount = CASE WHEN " +
            "(balance_amount -  #{balanceAmount}) > 0 THEN balance_amount -  #{balanceAmount} " +
            "ELSE balance_amount END," +
            " last_updated_date = CASE WHEN " +
            "(balance_amount -  #{balanceAmount}) > 0 THEN #{lastUpdatedDate} " +
            "ELSE last_updated_date END" +
            " where account_id = #{accountId} and currency_id = #{currencyId} ")
    void updateAccountDebit(AccountCurrencies accountCurrencies);

    //Update Accounts - Credit
    @Update("Update account_currencies set balance_amount = balance_amount +  #{balanceAmount}," +
            "last_updated_date = #{lastUpdatedDate} " +
            " where account_id = #{accountId} and currency_id = #{currencyId}")
    void updateAccountCredit(AccountCurrencies accountCurrencies);
}