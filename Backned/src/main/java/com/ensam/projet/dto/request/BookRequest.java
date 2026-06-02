package com.ensam.projet.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class BookRequest {
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne peut dépasser 255 caractères")
    private String title;

    @NotBlank(message = "L'auteur est obligatoire")
    private String author;

    @Pattern(regexp = "^(97(8|9))?\\-?\\d{1,5}\\-?\\d{1,7}\\-?\\d{1,7}\\-?\\d{1}$", message = "ISBN invalide")
    private String isbn;

    private String description;

    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stock;

    private LocalDate publishedDate;
    private Set<Long> categoryIds;
}
