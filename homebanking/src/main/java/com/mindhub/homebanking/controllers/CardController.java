package com.mindhub.homebanking.controllers;

import com.mindhub.homebanking.models.Card;
import com.mindhub.homebanking.models.CardColor;
import com.mindhub.homebanking.models.CardType;
import com.mindhub.homebanking.models.Client;
import com.mindhub.homebanking.repositories.CardRepository;
import com.mindhub.homebanking.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class CardController {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private ClientRepository clientRepository;

    @PostMapping("/clients/current/cards")
    public ResponseEntity<String> createCard(
            @RequestParam CardType cardType,
            @RequestParam CardColor cardColor,
            Authentication authentication
    ) {
        String clientEmail = authentication.getName();
        Client client = clientRepository.findByEmail(clientEmail);

        if (client == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Cliente no encontrado");
        }

        List<Card> existingCards = client.getCards().stream()
                .filter(card -> card.getType() == cardType)
                .collect(Collectors.toList());

        if (existingCards.size() >= 3) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Prohibido: El cliente ya tiene 3 tarjetas creadas del tipo especificado");
        }

        Card newCard = new Card();
        newCard.setColor(cardColor);
        newCard.setType(cardType);
        newCard.setNumber(generateCardNumber());
        newCard.setCardHolder(client.getFirstName() + " " + client.getLastName());
        newCard.setCvv(generateRandomCvv());
        newCard.setFromDate(LocalDate.now());
        newCard.setThruDate(LocalDate.now().plusYears(5));
        newCard.setClient(client);

        cardRepository.save(newCard);

        return ResponseEntity.status(HttpStatus.CREATED).body("Tarjeta creada exitosamente");
    }

    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                cardNumber.append(random.nextInt(10));
            }
            if (i < 3) {
                cardNumber.append("-");
            }
        }
        return cardNumber.toString();
    }

    private int generateRandomCvv() {
        return new Random().nextInt(1000);
    }
}
