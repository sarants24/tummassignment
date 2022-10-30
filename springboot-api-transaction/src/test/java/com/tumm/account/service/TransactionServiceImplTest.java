package com.tumm.account.service;

import com.tumm.account.entities.AccountTransaction;
import com.tumm.account.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionServiceImpl transactionServiceImpl;

    @Test
    void validateAccountById() {

        AccountTransaction accountTransaction = new AccountTransaction();
        accountTransaction.setTransactionId(1);
        accountTransaction.setAccountId(22);
        accountTransaction.setTranAmount(100d);
        accountTransaction.setCurrencyId("EUR");

        when(repository.findByTransactionId(any(Integer.class))).thenReturn(accountTransaction);

        AccountTransaction savedAccount = transactionServiceImpl.getByTransactionId(1);
        assertThat(savedAccount.getTransactionId()).isEqualTo(1);
        assertThat(savedAccount.getAccountId()).isEqualTo(22);
        assertThat(savedAccount.getCurrencyId()).isEqualTo("EUR");
    }
}
