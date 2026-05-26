package com.ensam.projet.service.impl;

import com.ensam.projet.dto.response.CategoryStatDto;
import com.ensam.projet.dto.response.DashboardStatsResponse;
import com.ensam.projet.dto.response.MonthlyStatDto;
import com.ensam.projet.entity.Book;
import com.ensam.projet.entity.Category;
import com.ensam.projet.entity.Loan;
import com.ensam.projet.entity.LoanStatus;
import com.ensam.projet.repository.BookRepository;
import com.ensam.projet.repository.CategoryRepository;
import com.ensam.projet.repository.LoanRepository;
import com.ensam.projet.repository.UserRepository;
import com.ensam.projet.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        long totalBooks = bookRepository.count();
        long totalUsers = userRepository.count();
        long activeLoans = loanRepository.countByStatus(LoanStatus.ACTIVE);
        long overdueLoans = loanRepository.countByStatus(LoanStatus.OVERDUE);
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        long totalLoansThisMonth = loanRepository.countByLoanDateBetween(startOfMonth, now);
        long availableBooks = bookRepository.findAll().stream().mapToLong(book -> book.getStock() == null ? 0 : book.getStock()).sum();
        List<MonthlyStatDto> loansByMonth = buildLoansByMonth();
        List<CategoryStatDto> booksByCategory = buildBooksByCategory();
        return new DashboardStatsResponse(totalBooks, totalUsers, activeLoans, overdueLoans, totalLoansThisMonth, availableBooks, loansByMonth, booksByCategory);
    }

    private List<MonthlyStatDto> buildLoansByMonth() {
        List<MonthlyStatDto> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            YearMonth month = YearMonth.from(now).minusMonths(i);
            LocalDate start = month.atDay(1);
            LocalDate end = month.atEndOfMonth();
            long count = loanRepository.countByLoanDateBetween(start, end);
            result.add(new MonthlyStatDto(month.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH) + " " + month.getYear(), count));
        }
        return result;
    }

    private List<CategoryStatDto> buildBooksByCategory() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category -> new CategoryStatDto(category.getName(), category.getBooks().size()))
                .sorted(Comparator.comparingLong(CategoryStatDto::getBookCount).reversed())
                .toList();
    }
}
