package com.tumm.account.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumm.account.dto.Transaction;
import com.tumm.account.entities.Account;
import com.tumm.account.entities.AccountMessageLog;
import com.tumm.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class AccountEvents {

    @Autowired
    private StreamBridge streamBridge;

    private final AccountService accountService;

    /*@Bean
    public Supplier<String> accProducer(){
        return () -> "";
    }*/

    //Create Account Processor - Request Queue
    @Bean
    public Function<Message<Account>,Message<AccountMessageLog>> accProcessor(){
        return val-> {
            return createAccount(val);
        };
    }

    //Create Account Processor - Response Queue
    @Bean
    public Consumer<Message<AccountMessageLog>> accConsumer(){

        return (messageLog) -> {
            AccountMessageLog accountMessageLog = messageLog.getPayload();

            if("Failed".equals(accountMessageLog.getStatus())) {
                accountService.updateMessageLog(accountMessageLog);
            }
        };


    }

    //Update Account Processor - Request Queue
    @Bean
    public Function<Message<Transaction>,Message<AccountMessageLog>> updateAccProcessor(){
        return val-> {
            return updateAccount(val);
        };
    }

    //Update Account Processor - Response Queue
    @Bean
    public Consumer<Message<AccountMessageLog>> updateAccConsumer(){
        return (messageLog) -> {
            AccountMessageLog accountMessageLog = messageLog.getPayload();

            Transaction trans = new Transaction();
            trans.setAccountId(accountMessageLog.getAccountId());
            trans.setTransactionId(accountMessageLog.getTransactionId());

            Message<Transaction> transactionMessage = MessageBuilder.withPayload(trans)
                    .setHeader("correlationId", accountMessageLog.getMessageId())
                    .build();

            if("Failed".equals(accountMessageLog.getStatus())) {
                accountService.updateMessageLog(accountMessageLog);
                streamBridge.send("transRollbackProcessor-in-0", transactionMessage);
            } else if("Success".equals(accountMessageLog.getStatus())) {
                streamBridge.send("transUpdateProcessor-in-0", transactionMessage);
            }
        };
    }

    //Create Accounts
    public Message<AccountMessageLog> createAccount(Message<Account> accountMessage) {

        String messageId = (String)accountMessage.getHeaders().get("correlationId");
        Account account = accountMessage.getPayload();

        AccountMessageLog accountMessageLog = new AccountMessageLog();
        ObjectMapper mapper = new ObjectMapper();
        accountMessageLog.setMessageId(messageId);
        accountMessageLog.setTranType("CREATE_ACCOUNT");

        try {
            accountMessageLog.setMessageContent(mapper.writeValueAsString(account));
            accountService.create(messageId, account);
        } catch (Exception se) {
            accountMessageLog.setCustomerId(account.getCustomerId());
            accountMessageLog.setStatus("Failed");
            accountMessageLog.setErrorMessage(se.getMessage());
        }

        Message<AccountMessageLog> accountML = MessageBuilder.withPayload(accountMessageLog)
                .setHeader("correlationId", messageId)
                .build();

        return accountML;
    }


    //Update Accounts
    public Message<AccountMessageLog> updateAccount(Message<Transaction> accTransactionMessage) {

        String messageId = (String)accTransactionMessage.getHeaders().get("correlationId");
        Transaction accountTransaction = accTransactionMessage.getPayload();

        AccountMessageLog accountMessageLog = new AccountMessageLog();
        ObjectMapper mapper = new ObjectMapper();
        accountMessageLog.setMessageId(messageId);
        accountMessageLog.setAccountId(accountTransaction.getAccountId());
        accountMessageLog.setTransactionId(accountTransaction.getTransactionId());
        accountMessageLog.setTranType("UPDATE_ACCOUNT");

        try {
            accountMessageLog.setMessageContent(mapper.writeValueAsString(accountTransaction));
            accountService.update(messageId, accountTransaction);
            accountMessageLog.setStatus("Success");
        } catch (Exception se) {
            accountMessageLog.setStatus("Failed");
            accountMessageLog.setErrorMessage(se.getMessage());
        }

        Message<AccountMessageLog> accountML = MessageBuilder.withPayload(accountMessageLog).build();

        return accountML;
    }
}
