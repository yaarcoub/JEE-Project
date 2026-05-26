package com.ensam.projet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private Integer stock;
    private LocalDate publishedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<CategoryResponse> categories;
    private long activeLoansCount;

}
