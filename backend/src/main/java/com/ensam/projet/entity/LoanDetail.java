package com.ensam.projet.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_details")
@Getter
@Setter
@NoArgsConstructor
public class LoanDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String itemcondition;

    private Integer renewalCount = 0;

    private String returnedBy;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "loan_id", unique = true)
    private Loan loan;
}
