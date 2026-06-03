package com.ensam.projet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalBooks;
    private long totalUsers;
    private long activeLoans;
    private long overdueLoans;
    private long totalLoansThisMonth;
    private long availableBooks;
    private List<MonthlyStatDto> loansByMonth;
    private List<CategoryStatDto> booksByCategory;
}
