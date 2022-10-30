package com.tumm.account.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumm.account.dto.Transaction;
import com.tumm.account.entities.AccountTransaction;
import com.tumm.account.entities.AccountTransactionLog;
import com.tumm.account.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class TransactionEvents {

    @Autowired
    private StreamBridge streamBridge;

    private final TransactionService transactionService;

    //Create Account Processor - Request Queue
    @Bean
    public Function<Message<AccountTransaction>,Message<AccountTransactionLog>> transProcessor(){
        return val-> {
            return createTransaction(val);
        };
    }

    //Create Account Processor - Response Queue
    @Bean
    public Consumer<Message<AccountTransactionLog>> transConsumer(){
        return (messageLog) -> {
            AccountTransactionLog accountTransactionLog = messageLog.getPayload();

            if("Failed".equals(accountTransactionLog.getStatus())) {
                transactionService.updateMessageLog(accountTransactionLog);
            } else if("In-Progress".equals(accountTransactionLog.getStatus())) {
                Transaction trans = new Transaction();
                trans.setAccountId(accountTransactionLog.getAccountId());
                trans.setAmount(accountTransactionLog.getTranAmount());
                trans.setTranDirection(accountTransactionLog.getTranDirection());
                trans.setCurrency(accountTransactionLog.getCurrencyId());

                String correlationId = UUID.randomUUID().toString();
                Message<Transaction> transactionMessage = MessageBuilder.withPayload(trans)
                        .setHeader("correlationId", accountTransactionLog.getMessageId())
                        .build();

                streamBridge.send("updateAccProcessor-in-0", transactionMessage);
            }
        };
    }

    //Rollback Account Processor - Request Queue
    @Bean
    public Consumer<Message<Transaction>> transUpdateProcessor(){
        return val-> {
            AccountTransactionLog accountTransactionLog = new AccountTransactionLog();
            String messageId = (String)val.getHeaders().get("correlationId");
            accountTransactionLog.setMessageId(messageId);
            accountTransactionLog.setStatus("Success");
            transactionService.updateAccountTransactionLog(accountTransactionLog);
        };
    }

    //Update Account Processor - Response Queue
    @Bean
    public Consumer<Message<Transaction>> transRollbackProcessor(){
        return (messageLog) -> {
            AccountTransactionLog accountTransactionLog = new AccountTransactionLog();
            String messageId = (String)messageLog.getHeaders().get("correlationId");
            accountTransactionLog.setMessageId(messageId);
            accountTransactionLog.setTransactionId(messageLog.getPayload().getTransactionId());
            accountTransactionLog.setStatus("Failed");
            transactionService.deleteTransaction(accountTransactionLog);
        };
    }

    //Create Accounts
    public Message<AccountTransactionLog> createTransaction(Message<AccountTransaction> accountTransactionMsg) {

        String messageId = (String)accountTransactionMsg.getHeaders().get("correlationId");
        AccountTransaction accountTransaction = accountTransactionMsg.getPayload();

        AccountTransactionLog accountTransactionLog = new AccountTransactionLog();
        ObjectMapper mapper = new ObjectMapper();
        accountTransactionLog.setMessageId(messageId);
        accountTransactionLog.setAccountId(accountTransaction.getAccountId());
        accountTransactionLog.setTranAmount(accountTransaction.getTranAmount());
        accountTransactionLog.setTranDirection(accountTransaction.getTranDirection());
        accountTransactionLog.setCurrencyId(accountTransaction.getCurrencyId());
        accountTransactionLog.setTranType("CREATE_Transaction");

        try {
            accountTransactionLog.setMessageContent(mapper.writeValueAsString(accountTransaction));
            transactionService.create(messageId, accountTransaction);
            accountTransactionLog.setStatus("In-Progress");
        } catch (Exception se) {
            accountTransactionLog.setAccountId(accountTransaction.getAccountId());
            accountTransactionLog.setStatus("Failed");
            accountTransactionLog.setErrorMessage(se.getMessage());
        }

        Message<AccountTransactionLog> accountML = MessageBuilder.withPayload(accountTransactionLog)
                .setHeader("correlationId", messageId)
                .build();

        return accountML;
    }

}
