package com.mindhub.homebanking.controllers;


import com.mindhub.homebanking.dtos.AccountDTO;
import com.mindhub.homebanking.models.Account;
import com.mindhub.homebanking.models.Client;
import com.mindhub.homebanking.repositories.AccountRepository;
import com.mindhub.homebanking.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ClientRepository clientRepository;


    @GetMapping("/accounts")
    public List<AccountDTO> getAccount() {
        return accountRepository.findAll().stream().map(AccountDTO::new).collect(toList());
    }

    @GetMapping("/accounts/{id}")
    public AccountDTO getAccount(@PathVariable Long id) {
        return accountRepository.findById(id).map(AccountDTO::new).orElse(null);
    }

    @PostMapping("/clients/current/accounts")
    public ResponseEntity<String> createAccount(Principal principal) {
        String clientName = principal.getName();
        Client client = clientRepository.findByEmail(clientName);

        if (client != null) {
            if (client.getAccount().size() >= 3) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Prohibido: El cliente ya tiene 3 cuentas registradas");
            }

            String accountNumber = generateAccountNumber();
            Account newAccount = new Account();
            newAccount.setNumber(accountNumber);
            newAccount.setClient(client);
            newAccount.setBalance(0.0);
            newAccount.setCreationDate(LocalDateTime.now());

            accountRepository.save(newAccount);

            return ResponseEntity.status(HttpStatus.CREATED).body("Cuenta creada exitosamente");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Cliente no encontrado");
        }
    }
    @GetMapping("/clients/current/accounts")
    public List<AccountDTO> getCurrentClientAccounts(Authentication authentication) {
        String currentEmail = authentication.getName();
        Client currentClient = clientRepository.findByEmail(currentEmail);

        if (currentClient != null) {
            return currentClient.getAccount().stream().map(AccountDTO::new).collect(toList());
        } else {
            return Collections.emptyList();
        }
    }



    private String generateAccountNumber() {
        String accountNumber = "VIN-" + (int) (Math.random() * 1000000);
        return accountNumber;
    }
}

