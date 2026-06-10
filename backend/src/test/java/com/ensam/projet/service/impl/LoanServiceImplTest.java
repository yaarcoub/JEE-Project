package com.ensam.projet.service.impl;

import com.ensam.projet.dto.request.LoanRequest;
import com.ensam.projet.dto.response.LoanResponse;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LoanMapper loanMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private LoanServiceImpl loanService;

    private User user;
    private Book book;
    private Loan loan;
    private LoanRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        book = new Book();
        book.setId(1L);
        book.setTitle("Spring Boot in Action");
        book.setStock(5);

        loan = new Loan();
        loan.setId(1L);
        loan.setUser(user);
        loan.setBook(book);
        loan.setStatus(LoanStatus.ACTIVE);
        
        LoanDetail detail = new LoanDetail();
        detail.setNotes("Test notes");
        loan.setDetail(detail);

        request = new LoanRequest();
        request.setBookId(1L);
        request.setExpectedReturnDate(LocalDate.now().plusDays(14));
        request.setNotes("New notes");
        request.setCondition("Good");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void shouldGetAllLoans() {
        Page<Loan> page = new PageImpl<>(Collections.singletonList(loan));
        when(loanRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(loanMapper.toResponse(loan)).thenReturn(new LoanResponse());

        var response = loanService.getAllLoans(0, 10, null, null);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void shouldGetLoanById() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanMapper.toResponse(loan)).thenReturn(new LoanResponse());

        var response = loanService.getLoanById(1L);

        assertThat(response).isNotNull();
    }

    @Test
    void shouldThrowWhenGetLoanByIdNotFound() {
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loanService.getLoanById(1L));
    }

    @Test
    void shouldCreateLoan() {
        mockSecurityContext();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(loanRepository.existsByUserIdAndBookIdAndStatus(1L, 1L, LoanStatus.ACTIVE)).thenReturn(false);
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(loanMapper.toResponse(loan)).thenReturn(new LoanResponse());

        var response = loanService.createLoan(request);

        assertThat(response).isNotNull();
        assertThat(book.getStock()).isEqualTo(4);
        verify(notificationService).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    void shouldThrowWhenCreateLoanWithInsufficientStock() {
        mockSecurityContext();
        book.setStock(0);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(BadRequestException.class, () -> loanService.createLoan(request));
    }

    @Test
    void shouldThrowWhenCreateLoanAlreadyBorrowed() {
        mockSecurityContext();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(loanRepository.existsByUserIdAndBookIdAndStatus(1L, 1L, LoanStatus.ACTIVE)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> loanService.createLoan(request));
    }

    @Test
    void shouldUpdateLoan() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);
        when(loanMapper.toResponse(loan)).thenReturn(new LoanResponse());

        var response = loanService.updateLoan(1L, request);

        assertThat(response).isNotNull();
        assertThat(loan.getDetail().getNotes()).isEqualTo("New notes");
    }

    @Test
    void shouldReturnLoan() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);
        when(loanMapper.toResponse(loan)).thenReturn(new LoanResponse());

        var response = loanService.returnLoan(1L, "Admin");

        assertThat(response).isNotNull();
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(book.getStock()).isEqualTo(6);
        verify(notificationService).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    void shouldThrowWhenReturnLoanAlreadyReturned() {
        loan.setStatus(LoanStatus.RETURNED);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThrows(BadRequestException.class, () -> loanService.returnLoan(1L, "Admin"));
    }

    @Test
    void shouldDeleteLoan() {
        when(loanRepository.existsById(1L)).thenReturn(true);

        loanService.deleteLoan(1L);

        verify(loanRepository).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeleteLoanNotFound() {
        when(loanRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> loanService.deleteLoan(1L));
    }

    @Test
    void shouldGetMyLoans() {
        mockSecurityContext();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Page<Loan> page = new PageImpl<>(Collections.singletonList(loan));
        when(loanRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(loanMapper.toResponse(loan)).thenReturn(new LoanResponse());

        var response = loanService.getMyLoans(0, 10);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetAllLoansWithStatusAndUserId() {
        Page<Loan> page = new PageImpl<>(Collections.singletonList(loan));
        // On utilise org.springframework.data.jpa.domain.Specification pour mocker le lambda
        when(loanRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class))).thenReturn(page);
        when(loanMapper.toResponse(loan)).thenReturn(new LoanResponse());

        var response = loanService.getAllLoans(0, 10, LoanStatus.ACTIVE, 1L);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetAllLoansWithStatusOnly() {
        Page<Loan> page = new PageImpl<>(Collections.singletonList(loan));
        when(loanRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class))).thenReturn(page);
        when(loanMapper.toResponse(loan)).thenReturn(new LoanResponse());

        var response = loanService.getAllLoans(0, 10, LoanStatus.ACTIVE, null);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetAllLoansWithUserIdOnly() {
        Page<Loan> page = new PageImpl<>(Collections.singletonList(loan));
        when(loanRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class))).thenReturn(page);
        when(loanMapper.toResponse(loan)).thenReturn(new LoanResponse());

        var response = loanService.getAllLoans(0, 10, null, 1L);

        assertThat(response.getContent()).hasSize(1);
    }

    // --- NOUVEAUX TESTS POUR LES NULL CHECKS (Couvre les if(loan.getDetail() == null) ---

    @Test
    void shouldUpdateLoanWhenDetailIsNull() {
        // On force le détail à null pour entrer dans le fameux 'if'
        loan.setDetail(null); 
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);
        when(loanMapper.toResponse(loan)).thenReturn(new LoanResponse());

        var response = loanService.updateLoan(1L, request);

        assertThat(response).isNotNull();
        // On vérifie que le service a bien créé le détail qui manquait
        assertThat(loan.getDetail()).isNotNull(); 
        assertThat(loan.getDetail().getNotes()).isEqualTo("New notes");
    }

    @Test
    void shouldReturnLoanWhenDetailIsNull() {
        // On force le détail à null
        loan.setDetail(null);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);
        when(loanMapper.toResponse(loan)).thenReturn(new LoanResponse());

        var response = loanService.returnLoan(1L, "Admin");

        assertThat(response).isNotNull();
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        // On vérifie que le détail a été recréé
        assertThat(loan.getDetail()).isNotNull();
        assertThat(loan.getDetail().getReturnedBy()).isEqualTo("Admin");
    }

    // --- NOUVELLES EXCEPTIONS MANQUANTES ---

    @Test
    void shouldThrowWhenUpdateLoanNotFound() {
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> loanService.updateLoan(1L, request));
    }

    @Test
    void shouldThrowWhenReturnLoanNotFound() {
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> loanService.returnLoan(1L, "Admin"));
    }

    @Test
    void shouldCreateLoanWithPrincipalAsString() {
        // Simule le cas où le principal de Spring Security est un simple String ("anonymousUser") au lieu d'un objet UserDetails
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn("testuser"); // <-- String classique
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(loanRepository.existsByUserIdAndBookIdAndStatus(1L, 1L, LoanStatus.ACTIVE)).thenReturn(false);
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(loanMapper.toResponse(loan)).thenReturn(new LoanResponse());

        var response = loanService.createLoan(request);

        assertThat(response).isNotNull();
    }
}