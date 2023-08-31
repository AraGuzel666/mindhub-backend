package com.mindhub.homebanking;

import com.mindhub.homebanking.models.*;
import com.mindhub.homebanking.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootApplication
public class HomebankingApplication {
	@Autowired
	private PasswordEncoder passwordEncoder;
	public static void main(String[] args) {
		SpringApplication.run(HomebankingApplication.class, args);

	}

	@Bean
	public CommandLineRunner initData(ClientRepository clientRepository, AccountRepository accountRepository, TransactionRepository transactionRepository, LoanRepository loanRepository,
									  ClientLoanRepository clientLoanRepository, CardRepository cardRepository){
		return (args) -> {
			LocalDateTime date = LocalDateTime.now();
			Client client = new Client("melba@mindhub.com", "Melba", "Morel", passwordEncoder.encode("123123"));
			Client client2 = new Client("araguzel66@gmail.com", "Aram", "Guzelian", passwordEncoder.encode("321321"));

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

			Transaction transaction1 = new Transaction(TransactionType.CREDIT, 50000.00, "VARIOS", date);
			Transaction transaction2 = new Transaction(TransactionType.DEBIT, 50055500.00, "VARIOS", date);
			Transaction transaction3 = new Transaction(TransactionType.CREDIT, 512340.00, "VARIOS", date);

			account1.addTransaction(transaction1);
			account1.addTransaction(transaction2);
			account2.addTransaction(transaction3);

			transactionRepository.save(transaction1);
			transactionRepository.save(transaction2);
			transactionRepository.save(transaction3);

			Loan hipotecario = new Loan();
			hipotecario.setName("Hipotecario");
			hipotecario.setMaxAmount(500000.00);
			hipotecario.setPayments(Arrays.asList(12, 24, 36, 48, 60));

			Loan personal = new Loan();
			personal.setName("Personal");
			personal.setMaxAmount(100000.0);
			personal.setPayments(Arrays.asList(6, 12, 24));

			Loan automotriz = new Loan();
			automotriz.setName("Automotriz");
			automotriz.setMaxAmount(300000.0);
			automotriz.setPayments(Arrays.asList(6, 12, 24, 36));

			loanRepository.save(hipotecario);
			loanRepository.save(personal);
			loanRepository.save(automotriz);

			ClientLoan melbaHipotecario = new ClientLoan(400000.0, 60, client, hipotecario);
			ClientLoan melbaPersonal = new ClientLoan(50000.0, 12, client, personal);
			ClientLoan aramPersonal = new ClientLoan(100000.00, 24, client2, personal);
			ClientLoan aramAutomotriz = new ClientLoan(200000.00, 36, client2, automotriz);

			clientLoanRepository.save(melbaHipotecario);
			clientLoanRepository.save(melbaPersonal);
			clientLoanRepository.save(aramPersonal);
			clientLoanRepository.save(aramAutomotriz);

			Card melbaGold = new Card((client.getFirstName() + " " + client.getLastName()), CardType.DEBIT, CardColor.GOLD, "1111-2222-3333-4444", 666, date.plusYears(5), date);
			Card melbaTitanium = new Card((client.getFirstName() + " " + client.getLastName()), CardType.CREDIT, CardColor.TITANIUM, "5555-6666-7777-8888", 999, date.plusYears(5), date);
			Card aramSilver = new Card((client2.getFirstName() + " " + client2.getLastName()), CardType.CREDIT, CardColor.SILVER, "1234-1234-1234-1234", 123, date.plusYears(5), date);

			client.addCards(melbaGold);
			client.addCards(melbaTitanium);
			client2.addCards(aramSilver);

			cardRepository.save(melbaGold);
			cardRepository.save(melbaTitanium);
			cardRepository.save(aramSilver);

		};
	}
}
