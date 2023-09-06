package com.mindhub.homebanking.dtos;

import com.mindhub.homebanking.models.Account;
import com.mindhub.homebanking.models.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class AccountDTO {
    private Long id;
    private String number;
    private LocalDateTime creationDate;
    private Double balance;
    private Set<TransactionDTO> transactions;

    public AccountDTO(Account account){
        this.id = account.getId();
        this.number = account.getNumber();
        this.creationDate = account.getCreationDate();
        this.balance = account.getBalance();
        this.transactions = account.getTransactions().stream().map(TransactionDTO::new).collect(toSet());
    }



    public Set<TransactionDTO> getTransactions() {
        return transactions;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public Double getBalance() {
        return balance;
    }
}
