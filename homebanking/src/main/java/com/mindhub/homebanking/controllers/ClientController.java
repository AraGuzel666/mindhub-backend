package com.mindhub.homebanking.controllers;

import com.mindhub.homebanking.dtos.ClientDTO;
import com.mindhub.homebanking.models.Client;
import com.mindhub.homebanking.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class ClientController {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/clients/current")
    public ResponseEntity<ClientDTO> getCurrentClient(Authentication authentication) {
        String currentEmail = authentication.getName();
        Client currentClient = clientRepository.findByEmail(currentEmail);

        if (currentClient != null) {
            return ResponseEntity.ok(new ClientDTO(currentClient));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/register")
    public ResponseEntity<Object> registerClient(
            @RequestParam String firstName, @RequestParam String lastName,
            @RequestParam String email, @RequestParam String password) {

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.BAD_REQUEST);
        }

        if (clientRepository.findByEmail(email) != null) {
            return new ResponseEntity<>("Email already in use", HttpStatus.BAD_REQUEST);
        }

        Client client = new Client(email, firstName, lastName, passwordEncoder.encode(password));
        clientRepository.save(client);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

@RequestMapping("/clients")
    public List<ClientDTO> getClient() {
        return clientRepository.findAll().stream().map(ClientDTO::new).collect(toList());
    }

    @RequestMapping("/clients/{id}")
    public ClientDTO getClient(@PathVariable Long id) {
        return clientRepository.findById(id).map(ClientDTO::new).orElse(null);
    }
}
