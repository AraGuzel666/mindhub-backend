package com.mindhub.homebanking.controllers;

import com.mindhub.homebanking.dtos.LoanApplicationDTO;
import com.mindhub.homebanking.dtos.LoanDTO;
import com.mindhub.homebanking.models.*;
import com.mindhub.homebanking.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class LoanController {

    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ClientLoanRepository clientLoanRepository;

    @PostMapping("/loans")
    @Transactional
    public ResponseEntity<String> applyForLoan(
            @RequestBody LoanApplicationDTO loanApplicationDTO,
            Authentication authentication) {

        // Verificar que los datos de la solicitud no estén vacíos y sean válidos
        if (loanApplicationDTO.getAmount() <= 0 || loanApplicationDTO.getPayments() <= 0) {
            return new ResponseEntity<>("Datos de solicitud inválidos", HttpStatus.BAD_REQUEST);
        }

        // Verificar que el préstamo exista
// Verificar que el préstamo exista
        Long loanId = loanApplicationDTO.getLoanId();
        if (loanId == null) {
            return new ResponseEntity<>("ID de préstamo nulo", HttpStatus.BAD_REQUEST);
        }

        Optional<Loan> optionalLoan = loanRepository.findById(loanId);
        if (!optionalLoan.isPresent()) {
            return new ResponseEntity<>("Préstamo no encontrado", HttpStatus.NOT_FOUND);
        }
        Loan loan = optionalLoan.get();


        // Verificar que el monto solicitado no exceda el monto máximo del préstamo
        if (loanApplicationDTO.getAmount() > loan.getMaxAmount()) {
            return new ResponseEntity<>("Monto solicitado excede el monto máximo del préstamo", HttpStatus.BAD_REQUEST);
        }

        // Verificar que la cantidad de cuotas esté dentro de las disponibles del préstamo
        if (!loan.getPayments().contains(loanApplicationDTO.getPayments())) {
            return new ResponseEntity<>("Cantidad de cuotas no disponibles", HttpStatus.BAD_REQUEST);
        }

        // Verificar que la cuenta de destino exista
        if (!verificarExistenciaCuentaDestino(loanApplicationDTO.getToAccountNumber())) {
            return new ResponseEntity<>("Cuenta de destino no existe", HttpStatus.BAD_REQUEST);
        }

        // Obtener el cliente autenticado
        String currentEmail = authentication.getName();
        Client currentClient = clientRepository.findByEmail(currentEmail);

        // Verificar que el cliente esté autenticado
        if (currentClient == null) {
            return new ResponseEntity<>("Cliente no autenticado", HttpStatus.UNAUTHORIZED);
        }

        // Verificar que la cuenta de destino pertenezca al cliente autenticado
        if (!verificarPropiedadCuentaDestino(currentClient, loanApplicationDTO.getToAccountNumber())) {
            return new ResponseEntity<>("Cuenta de destino no pertenece al cliente", HttpStatus.BAD_REQUEST);
        }

        // Realizar la operación de solicitud de préstamo y registrar una transacción
        LocalDateTime time = LocalDateTime.now();
        Transaction loanTransaction = new Transaction(TransactionType.CREDIT, loanApplicationDTO.getAmount(),
                "Préstamo aprobado: " + loan.getName(), time);

        // Actualizar el balance de la cuenta de destino
        actualizarBalanceCuentaDestino(loanApplicationDTO.getToAccountNumber(), loanApplicationDTO.getAmount());

        // Guardar la transacción y actualizar la cuenta en la base de datos
        transactionRepository.save(loanTransaction);

        // Calcular el monto total incluyendo el 20%
        Double totalAmount = loanApplicationDTO.getAmount() * 1.20;

        // Crear la solicitud de préstamo
        ClientLoan loanRequest = new ClientLoan();
        loanRequest.setAmount(totalAmount);
        loanRequest.setPayments(loanApplicationDTO.getPayments());
        loanRequest.setClient(currentClient);
        loanRequest.setLoan(loan);

        // Guardar la solicitud de préstamo en la base de datos
        clientLoanRepository.save(loanRequest);

        // Crear la transacción "CREDIT" asociada a la cuenta de destino
        Transaction loanTransactions = new Transaction(TransactionType.CREDIT, totalAmount,
                loan.getName() + " loan approved", time);

        // Actualizar el balance de la cuenta de destino
        //actualizarBalanceCuentaDestino(loanApplicationDTO.getToAccountNumber(), totalAmount);

        Account destinationAccount = accountRepository.findByNumber(loanApplicationDTO.getToAccountNumber());
        destinationAccount.addTransaction(loanTransactions);

        // Guardar la transacción y actualizar la cuenta en la base de datos
        transactionRepository.save(loanTransactions);

        return new ResponseEntity<>("Solicitud de préstamo exitosa", HttpStatus.CREATED);
    }

    // Método para verificar la existencia de la cuenta de destino
    private boolean verificarExistenciaCuentaDestino(String accountNumber) {
        Account destinationAccount = accountRepository.findByNumber(accountNumber);
        return destinationAccount != null;
    }

    // Método para verificar la propiedad de la cuenta de destino
    private boolean verificarPropiedadCuentaDestino(Client client, String accountNumber) {
        Account destinationAccount = accountRepository.findByNumber(accountNumber);

        // Verificar si la cuenta existe y si pertenece al cliente autenticado
        if (destinationAccount != null && destinationAccount.getClient().equals(client)) {
            return true;
        }
        return false;
    }


    // Método para actualizar el balance de la cuenta de destino
    private void actualizarBalanceCuentaDestino(String toAccountNumber, Double amount) {
        Account destinationAccount = accountRepository.findByNumber(toAccountNumber);

        if (destinationAccount != null) {
            Double newBalance = destinationAccount.getBalance() + amount;
            destinationAccount.setBalance(newBalance);

            accountRepository.save(destinationAccount);
        } else {
            throw new RuntimeException("La cuenta de destino no existe");
        }
    }

    @GetMapping("/loans")
    public List<LoanDTO> getAvailableLoans(Authentication authentication) {
        String currentEmail = authentication.getName();
        Client currentClient = clientRepository.findByEmail(currentEmail);

        if (currentClient != null) {
            List<Loan> loans = loanRepository.findAll();
            List<LoanDTO> loanDTOs = loans.stream()
                    .map(loan -> new LoanDTO(loan))
                    .collect(Collectors.toList());

            return loanDTOs;
        } else {
            return Collections.emptyList();
        }
    }
}



