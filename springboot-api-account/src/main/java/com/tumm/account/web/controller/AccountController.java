package com.tumm.account.web.controller;

import com.tumm.account.common.exception.BadRequestException;
import com.tumm.account.entities.Account;
import com.tumm.account.entities.AccountCurrencies;
import com.tumm.account.entities.AccountMessageLog;
import com.tumm.account.service.AccountService;
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
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private StreamBridge streamBridge;

    private final AccountService accountService;

    @Operation(summary = "Get a Account by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the Account",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Account.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content) })
    @GetMapping("/{id}")
    public ResponseEntity<?> getAllAccountsById(@PathVariable Integer id ) {

        if(ObjectUtils.isEmpty(id)){
            throw new BadRequestException(MessageFormat.format("Account Id is required", id));
        }

        Account account = accountService.getByAccountId(id);

        if(!ObjectUtils.isEmpty(account)){
            return new ResponseEntity<>(new SuccessResponse(account, "Account Found"), HttpStatus.OK);
        }

        return new ResponseEntity<>(new ErrorResponse("404", "Account Not Found", Instant.now()), HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Get a Account by Customer Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the Account",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Account.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Customer Id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content) })
    @GetMapping("/customer/{id}")
    public ResponseEntity<?> getAllAccountsByCustomerId(@PathVariable String id ) {

        if(ObjectUtils.isEmpty(id)){
            throw new BadRequestException(MessageFormat.format("Customer Id is required", id));
        }

        Account account = accountService.getByCustomerId(id);

        if(!ObjectUtils.isEmpty(account)){
            return new ResponseEntity<>(new SuccessResponse(account, "Account Found"), HttpStatus.OK);
        }

        return new ResponseEntity<>(new ErrorResponse("404", "Account Not Found", Instant.now()), HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Get a Account by its id and Currency Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the Account",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountCurrencies.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content) })
    @GetMapping("/{id}/currency/{currencyId}")
    public ResponseEntity<AccountCurrencies> getAccountByCurrencyAndId(@PathVariable Integer id, @PathVariable String currencyId ) {

        if(ObjectUtils.isEmpty(id)){
            throw new BadRequestException(MessageFormat.format("Account Id is required", id));
        }

        if(ObjectUtils.isEmpty(currencyId)){
            throw new BadRequestException(MessageFormat.format("Currency Id is required", currencyId));
        }

        AccountCurrencies accountCurrencies = accountService.getAccountByCurrencyAndId(id,currencyId);

        return new ResponseEntity<>(accountCurrencies,HttpStatus.OK);

        /*if(!ObjectUtils.isEmpty(accountCurrencies)){
           return new ResponseEntity<>(new SuccessResponse(accountCurrencies, "Account Found"), HttpStatus.OK);
        }

        return new ResponseEntity<>(new ErrorResponse("404", "Account Not Found", Instant.now()), HttpStatus.NOT_FOUND);
        */
    }


    @PostMapping()
    public DeferredResult<ResponseEntity<?>> createQueueAccount(@RequestBody @Valid Account account) {

        if (!ObjectUtils.isEmpty(account.getAccountId())) {
            throw new BadRequestException("A new account cannot already have an Account Id");
        }

        if(ObjectUtils.isEmpty(account.getCustomerId())){
            throw new BadRequestException(MessageFormat.format("Customer Id is required", account.getCustomerId()));
        }

        if(ObjectUtils.isEmpty(account.getCountry())){
            throw new BadRequestException(MessageFormat.format("Country Id is required", account.getCountry()));
        }

        if(ObjectUtils.isEmpty(account.getAccountCurrenciesList())){
            throw new BadRequestException(MessageFormat.format("Account Currency Id is required", account.getCountry()));
        } else if (validateCurrencies(account.getAccountCurrenciesList())) {
            throw new BadRequestException(MessageFormat.format("Invalid Account Currency", account.getCountry()));
        }

        String correlationId = UUID.randomUUID().toString();
        Message<Account> accountMessage = MessageBuilder.withPayload(account)
                .setHeader("correlationId", correlationId)
                .build();

        streamBridge.send("accProducer-out-0", accountMessage);

        DeferredResult<ResponseEntity<?>> output = new DeferredResult<>();

       // ForkJoinPool.commonPool().submit(() -> {
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            ResponseEntity<?> outputEntity = null;

            AccountMessageLog accountMessageLog = accountService.getByMessageId(correlationId);

            if(ObjectUtils.isEmpty(accountMessageLog)) {
                outputEntity = new ResponseEntity<>(new ErrorResponse(
                        "500", "Account Not Created", Instant.now()), HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                String status = accountMessageLog.getStatus();
                if ("Success".equals(status)) {
                    outputEntity = new ResponseEntity<>(
                            new SuccessResponse(accountService.getByAccountId(accountMessageLog.getAccountId()),
                                    "Account Created Successfully"),
                            HttpStatus.CREATED);
                } else {
                    outputEntity = new ResponseEntity<>(new ErrorResponse(
                            "500", "Account Not Created " + accountMessageLog.getErrorMessage()
                            , Instant.now()), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            output.setResult(outputEntity);
        }).start();

        return output;
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