package com.tumm.account.web.controller;

import com.tumm.account.common.exception.BadRequestException;
import com.tumm.account.common.exception.ServiceException;
import com.tumm.account.dto.Transaction;
import com.tumm.account.entities.AccountCurrencies;
import com.tumm.account.entities.AccountTransaction;
import com.tumm.account.entities.AccountTransactionLog;
import com.tumm.account.service.TransactionService;
import com.tumm.account.web.response.ErrorResponse;
import com.tumm.account.web.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    @Autowired
    private StreamBridge streamBridge;

    private final TransactionService transactionService;

    @Operation(summary = "Get a List of transactions by Account Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the Account",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountTransaction.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content) })
    @GetMapping("/account/{id}")
    public ResponseEntity<?> getAllAccountsById(@PathVariable Integer id ) {

        if(ObjectUtils.isEmpty(id)){
            throw new BadRequestException(MessageFormat.format("Account Id is required", id));
        }

        List<AccountTransaction> accountTranList = transactionService.getByAccountId(id);

        if(!ObjectUtils.isEmpty(accountTranList)){
            return new ResponseEntity<>(new SuccessResponse(accountTranList, "Transaction Found"), HttpStatus.OK);
        }

        return new ResponseEntity<>(new ErrorResponse("404", "Transaction Not Found", Instant.now()), HttpStatus.NOT_FOUND);

    }

    @Operation(summary = "Get a transaction by Transaction Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the Account",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountTransaction.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content) })
    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Integer id ) {

        if(ObjectUtils.isEmpty(id)){
            throw new BadRequestException(MessageFormat.format("Transaction Id is required", id));
        }

        AccountTransaction accountTran = transactionService.getByTransactionId(id);

        if(!ObjectUtils.isEmpty(accountTran)){
            return new ResponseEntity<>(new SuccessResponse(accountTran, "Transaction Found"), HttpStatus.OK);
        }

        return new ResponseEntity<>(new ErrorResponse("404", "Transaction Not Found", Instant.now()), HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Create a new transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New Transaction Created",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountTransaction.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content) })
    @PostMapping()
    public DeferredResult<ResponseEntity<?>> createTransaction(@RequestBody @Valid AccountTransaction accountTransaction) {

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

        if("OUT".equalsIgnoreCase(accountTransaction.getTranDirection())) {
            AccountCurrencies accCurrencyDB = getAccountCurrencies(
                    accountTransaction.getAccountId(), accountTransaction.getCurrencyId());
            Double tempBalance = accCurrencyDB.getBalanceAmount() - accountTransaction.getTranAmount();
            if (tempBalance <= 0) {
                throw new BadRequestException(MessageFormat.format("Insufficient Amount", accountTransaction.getTranAmount()));
            }
        }

        String correlationId = UUID.randomUUID().toString();
        Message<AccountTransaction> accountMessage = MessageBuilder.withPayload(accountTransaction)
                .setHeader("correlationId", correlationId)
                .build();

        streamBridge.send("transProcessor-in-0", accountMessage);

        DeferredResult<ResponseEntity<?>> output = new DeferredResult<>();

       // ForkJoinPool.commonPool().submit(() -> {
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            ResponseEntity<?> outputEntity = null;

            AccountTransactionLog accountTransactionLog = transactionService.getByMessageId(correlationId);

            if(ObjectUtils.isEmpty(accountTransactionLog)) {
                outputEntity = new ResponseEntity<>(new ErrorResponse(
                        "500", "Transaction Not Created", Instant.now()), HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                Integer tranId = accountTransactionLog.getTransactionId();
                String status = accountTransactionLog.getStatus();

                if ("Success".equals(status)) {
                    AccountTransaction createdTransaction = transactionService.getByTransactionId(tranId);

                    Transaction dtoTrans = new Transaction();
                    dtoTrans.setTransactionId(createdTransaction.getTransactionId());
                    dtoTrans.setAccountId(createdTransaction.getAccountId());
                    dtoTrans.setAmount(createdTransaction.getTranAmount());
                    dtoTrans.setCurrency(createdTransaction.getCurrencyId());
                    dtoTrans.setTranDirection(createdTransaction.getTranDirection());
                    dtoTrans.setTranDescription(createdTransaction.getTranDirection());

                    AccountCurrencies accCurrency = getAccountCurrencies(
                                createdTransaction.getAccountId(),createdTransaction.getCurrencyId());
                    dtoTrans.setBalanceAfterTransaction(accCurrency.getBalanceAmount());

                    outputEntity = new ResponseEntity<>(
                            new SuccessResponse(dtoTrans,"Transaction Created  Successfully"),
                            HttpStatus.CREATED);
                } else {
                    outputEntity = new ResponseEntity<>(new ErrorResponse(
                            "500", "Transaction Not Created" + accountTransactionLog.getErrorMessage()
                            , Instant.now()), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            output.setResult(outputEntity);
        }).start();

        return output;
    }


    private AccountCurrencies getAccountCurrencies
            (Integer accountId, String currencyId) throws ServiceException {

        String url = "http://localhost:8080/api/account/" + accountId + "/currency/" + currencyId;
        System.out.println("URL..." + url);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<AccountCurrencies> resEntity =
                restTemplate.getForEntity(url, AccountCurrencies.class );

        AccountCurrencies accCurrencies = resEntity.getBody();

        if(ObjectUtils.isEmpty(resEntity.getBody())) {
            throw new BadRequestException(MessageFormat.format("Account not found", accountId));
        }

        return accCurrencies;
    }
}