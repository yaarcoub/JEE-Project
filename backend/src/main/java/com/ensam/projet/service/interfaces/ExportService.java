package com.ensam.projet.service.interfaces;

import com.ensam.projet.entity.Book;
import com.ensam.projet.entity.Loan;

import java.util.List;

public interface ExportService {
    byte[] exportBooksToPdf(List<Book> books);
    byte[] exportBooksToExcel(List<Book> books);
    byte[] exportLoansToPdf(List<Loan> loans);
}
