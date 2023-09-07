package com.mindhub.homebanking.dtos;

import com.mindhub.homebanking.models.Loan;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import java.util.List;

public class LoanDTO {

    private Long LoanId;
    private String name;
    private Double maxAmount;
    @ElementCollection
    @CollectionTable(name = "loan_payments", joinColumns = @JoinColumn(name = "loan_id"))
    @Column(name = "payment")
    private List<Integer> payments;

    public LoanDTO(Loan loan) {
        this.LoanId = loan.getId();
        this.maxAmount = loan.getMaxAmount();
        this.payments = loan.getPayments();
        this.name = loan.getName();
    }

    public Long getLoanId() {
        return LoanId;
    }

    public String getName() {
        return name;
    }

    public Double getMaxAmount() {
        return maxAmount;
    }

    public List<Integer> getPayments() {
        return payments;
    }
}


