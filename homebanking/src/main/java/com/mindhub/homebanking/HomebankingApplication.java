package com.mindhub.homebanking;

import com.mindhub.homebanking.models.Account;
import com.mindhub.homebanking.models.Client;
import com.mindhub.homebanking.models.Transaction;
import com.mindhub.homebanking.models.TransactionType;
import com.mindhub.homebanking.repositories.AccountRepository;
import com.mindhub.homebanking.repositories.ClientRepository;
import com.mindhub.homebanking.repositories.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class HomebankingApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomebankingApplication.class, args);

	}

	@Bean
	public CommandLineRunner initData(ClientRepository clientRepository, AccountRepository accountRepository, TransactionRepository transactionRepository){
		return (args) -> {
			LocalDate date = LocalDate.now();
			Client client = new Client("melba@mindhub.com", "Melba", "Morel");
			Client client2 = new Client("araguzel66@gmail.com", "Aram", "Guzelian");

			clientRepository.save(client);
			clientRepository.save(client2);

			Account account1 = new Account("VIN001", date, 5000.00 );
			Account account2 = new Account("VIN002", date.plusDays(1), 7500.00);
			Account account3 = new Account("VIN003", date.plusDays(5), 100000000.00);


			client.addAccount(account1);
			client.addAccount(account2);
			client2.addAccount(account3);

			accountRepository.save(account1);
			accountRepository.save(account2);
			accountRepository.save(account3);

			Transaction transaction1 = new Transaction(TransactionType.CREDITO, 50000.00, "VARIOS", date);
			Transaction transaction2 = new Transaction(TransactionType.DEBITO, 50055500.00, "VARIOS", date);
			Transaction transaction3 = new Transaction(TransactionType.CREDITO, 512340.00, "VARIOS", date);

			account1.addTransaction(transaction1);
			account1.addTransaction(transaction2);
			account2.addTransaction(transaction3);


			transactionRepository.save(transaction1);
			transactionRepository.save(transaction2);
			transactionRepository.save(transaction3);

		};
	}
}
