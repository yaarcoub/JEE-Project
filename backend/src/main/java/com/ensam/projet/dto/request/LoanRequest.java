package com.ensam.projet.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LoanRequest {
    @NotNull(message = "L'identifiant du livre est obligatoire")
    private Long bookId;

    @NotNull(message = "La date de retour attendue est obligatoire")
    @FutureOrPresent(message = "La date de retour attendue doit être aujourd'hui ou ultérieure")
    private LocalDate expectedReturnDate;

    private String notes;
    private String condition;
}
