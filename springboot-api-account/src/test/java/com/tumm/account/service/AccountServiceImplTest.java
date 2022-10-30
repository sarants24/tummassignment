package com.tumm.account.service;

import com.tumm.account.entities.Account;
import com.tumm.account.entities.AccountCurrencies;
import com.tumm.account.repositories.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.AdditionalAnswers.*;
import static org.assertj.core.api.Java6Assertions.*;


import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private AccountServiceImpl accountServiceImpl;

    @Test
    void validateAccountById() {
        AccountCurrencies accountCurrencies = new AccountCurrencies();
        accountCurrencies.setCurrencyId("EUR");
        List<AccountCurrencies> alCurrList = new ArrayList<AccountCurrencies>();
        alCurrList.add(accountCurrencies);

        Account account = new Account();
        account.setAccountId(1);
        account.setCustomerId("123");
        account.setCountry("Estonia");
        account.setAccountCurrenciesList(alCurrList);

        when(repository.findByAccountId(any(Integer.class))).thenReturn(account);

        Account savedAccount = accountServiceImpl.getByAccountId(1);
        assertThat(savedAccount.getAccountId()).isEqualTo(1);
        assertThat(savedAccount.getCustomerId()).isEqualTo("123");
        assertThat(savedAccount.getCountry()).isEqualTo("Estonia");

        List<AccountCurrencies> currencyList = savedAccount.getAccountCurrenciesList();
        AccountCurrencies accCurrencies = currencyList.get(0);

        assertThat(accCurrencies.getCurrencyId()).isEqualTo("EUR");
    }
}
