package com.ensam.projet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanDetailResponse {
    private Long id;
    private String notes;
    private String condition;
    private Integer renewalCount;
    private String returnedBy;
}
