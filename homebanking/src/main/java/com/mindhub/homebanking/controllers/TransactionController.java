package com.mindhub.homebanking.controllers;

import com.mindhub.homebanking.dtos.TransactionDTO;
import com.mindhub.homebanking.models.Account;
import com.mindhub.homebanking.models.Client;
import com.mindhub.homebanking.models.Transaction;
import com.mindhub.homebanking.models.TransactionType;
import com.mindhub.homebanking.repositories.AccountRepository;
import com.mindhub.homebanking.repositories.ClientRepository;
import com.mindhub.homebanking.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class TransactionController {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private ClientRepository clientRepository;

    @PostMapping("/transactions")
    @Transactional
    public ResponseEntity<String> createTransaction(
            @RequestParam double amount,
            @RequestParam String description,
            @RequestParam String fromAccountNumber,
            @RequestParam String toAccountNumber,
            Authentication authentication) {

        LocalDateTime time = LocalDateTime.now();
            // Verificar que los parámetros no estén vacíos
        if (amount <= 0 || description.isEmpty() || fromAccountNumber.isEmpty() || toAccountNumber.isEmpty()) {
            return new ResponseEntity<>("Parámetros inválidos", HttpStatus.BAD_REQUEST);
        }
        // Verificar que los números de cuenta no sean iguales
        if (fromAccountNumber.equals(toAccountNumber)) {
            return new ResponseEntity<>("Cuenta de Origen y Cuenta de Destino no puede ser la misma", HttpStatus.BAD_REQUEST);
        }
        // Obtener el cliente autenticado
        String currentEmail = authentication.getName();
        Client currentClient = clientRepository.findByEmail(currentEmail);

        // Verificar que la cuenta de origen exista y pertenezca al cliente autenticado
        Account fromAccount = accountRepository.findByNumber(fromAccountNumber);
        if (fromAccount == null || !fromAccount.getClient().equals(currentClient)) {
            return new ResponseEntity<>("Cuenta de Origen inválida", HttpStatus.BAD_REQUEST);
        }

        // Verificar que la cuenta de destino exista
        Account toAccount = accountRepository.findByNumber(toAccountNumber);
        if (toAccount == null) {
            return new ResponseEntity<>("Cuenta de Destino inválida", HttpStatus.BAD_REQUEST);
        }

        // Verificar que la cuenta de origen tenga suficiente saldo
        if (fromAccount.getBalance() < amount) {
            return new ResponseEntity<>("Saldo insuficiente en la cuenta de origen", HttpStatus.BAD_REQUEST);
        }

        // Crear las transacciones (DEBIT y CREDIT)
        Transaction debitTransaction = new Transaction(TransactionType.DEBIT, amount, fromAccount.getNumber() + " " + description, time);
        Transaction creditTransaction = new Transaction(TransactionType.CREDIT, amount, toAccount.getNumber() + " " + description, time);


        fromAccount.addTransaction(debitTransaction);
        toAccount.addTransaction(creditTransaction);


        // Actualizar los saldos de las cuentas
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        // Guardar las transacciones y actualizar las cuentas en la base de datos
        transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return new ResponseEntity<>("Transacción satisfactoria", HttpStatus.CREATED);
    }
    @GetMapping("/transactions")
    public List<TransactionDTO> getAllTransactions(Authentication authentication) {
        String currentEmail = authentication.getName();
        Client currentClient = clientRepository.findByEmail(currentEmail);

        if (currentClient != null) {
            List<Account> accounts = accountRepository.findByClient(currentClient);
            List<Transaction> transactions = transactionRepository.findByAccountIn(accounts);

            return transactions.stream().map(TransactionDTO::new).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}


