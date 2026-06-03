package com.ensam.projet.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    private String name;

    private String description;
    private String color;
}
