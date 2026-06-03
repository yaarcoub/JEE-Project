package com.ensam.projet.service.impl;

import com.ensam.projet.dto.request.LoanRequest;
import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.entity.Book;
import com.ensam.projet.entity.Loan;
import com.ensam.projet.entity.LoanDetail;
import com.ensam.projet.entity.LoanStatus;
import com.ensam.projet.entity.User;
import com.ensam.projet.exception.BadRequestException;
import com.ensam.projet.exception.ResourceNotFoundException;
import com.ensam.projet.mapper.LoanMapper;
import com.ensam.projet.repository.BookRepository;
import com.ensam.projet.repository.LoanRepository;
import com.ensam.projet.repository.UserRepository;
import com.ensam.projet.service.NotificationService;
import com.ensam.projet.service.interfaces.LoanService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final LoanMapper loanMapper;
    private final NotificationService notificationService;

    @Override
    public PagedResponse<LoanResponse> getAllLoans(int page, int size, LoanStatus status, Long userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "loanDate"));
        Page<Loan> loans;
        if (status == null && userId == null) {
            loans = loanRepository.findAll(pageable);
        } else if (status != null && userId != null) {
            loans = loanRepository.findAll((root, query, cb) -> cb.and(
                    cb.equal(root.get("status"), status),
                    cb.equal(root.get("user").get("id"), userId)
            ), pageable);
        } else if (status != null) {
            loans = loanRepository.findAll((root, query, cb) -> cb.equal(root.get("status"), status), pageable);
        } else {
            loans = loanRepository.findAll((root, query, cb) -> cb.equal(root.get("user").get("id"), userId), pageable);
        }
        return new PagedResponse<>(loans.map(loanMapper::toResponse).getContent(), loans.getNumber(), loans.getTotalPages(), loans.getTotalElements(), loans.getSize(), loans.isLast());
    }

    @Override
    public LoanResponse getLoanById(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt introuvable"));
        return loanMapper.toResponse(loan);
    }

    @Override
    public LoanResponse createLoan(LoanRequest request) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Livre introuvable"));
        if (book.getStock() == null || book.getStock() <= 0) {
            throw new BadRequestException("Stock insuffisant");
        }

        boolean alreadyBorrowed = loanRepository.existsByUserIdAndBookIdAndStatus(user.getId(), book.getId(), LoanStatus.ACTIVE);
        if (alreadyBorrowed) {
            throw new BadRequestException("Vous avez déjà un emprunt en cours pour ce livre");
        }
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(LocalDate.now());
        loan.setExpectedReturnDate(request.getExpectedReturnDate());
        loan.setStatus(LoanStatus.ACTIVE);
        LoanDetail detail = new LoanDetail();
        detail.setNotes(request.getNotes());
        detail.setItemcondition(request.getCondition());
        detail.setRenewalCount(0);
        detail.setLoan(loan);
        loan.setDetail(detail);
        book.setStock(book.getStock() - 1);
        bookRepository.save(book);
        Loan savedLoan = loanRepository.save(loan);
        
        notificationService.sendNotification(
            "Nouvel Emprunt",
            "Le livre '" + book.getTitle() + "' a été emprunté par " + user.getUsername(),
            "INFO"
        );
        
        return loanMapper.toResponse(savedLoan);
    }

    @Override
    public LoanResponse updateLoan(Long id, LoanRequest request) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt introuvable"));
        loan.setExpectedReturnDate(request.getExpectedReturnDate());
        if (loan.getDetail() == null) {
            LoanDetail detail = new LoanDetail();
            detail.setLoan(loan);
            loan.setDetail(detail);
        }
        loan.getDetail().setNotes(request.getNotes());
        loan.getDetail().setItemcondition(request.getCondition());
        return loanMapper.toResponse(loanRepository.save(loan));
    }

    @Override
    public LoanResponse returnLoan(Long id, String returnedBy) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt introuvable"));
        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BadRequestException("Emprunt déjà retourné");
        }
        loan.setActualReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);
        loan.getBook().setStock(Optional.ofNullable(loan.getBook().getStock()).orElse(0) + 1);
        if (loan.getDetail() == null) {
            LoanDetail detail = new LoanDetail();
            detail.setLoan(loan);
            loan.setDetail(detail);
        }
        loan.getDetail().setReturnedBy(returnedBy);
        Loan savedLoan = loanRepository.save(loan);
        
        notificationService.sendNotification(
            "Livre Retourné",
            "Le livre '" + loan.getBook().getTitle() + "' a été retourné.",
            "SUCCESS"
        );
        
        return loanMapper.toResponse(savedLoan);
    }

    @Override
    public void deleteLoan(Long id) {
        if (!loanRepository.existsById(id)) {
            throw new ResourceNotFoundException("Emprunt introuvable");
        }
        loanRepository.deleteById(id);
    }

    @Override
    public PagedResponse<LoanResponse> getMyLoans(int page, int size) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "loanDate"));
        Page<Loan> loans = loanRepository.findByUserId(user.getId(), pageable);
        return new PagedResponse<>(loans.map(loanMapper::toResponse).getContent(), loans.getNumber(), loans.getTotalPages(), loans.getTotalElements(), loans.getSize(), loans.isLast());
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }
}
