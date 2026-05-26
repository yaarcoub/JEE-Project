package com.ensam.projet.service.impl;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.ensam.projet.entity.Book;
import com.ensam.projet.entity.Loan;
import com.ensam.projet.service.interfaces.ExportService;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    @Override
    public byte[] exportBooksToPdf(List<Book> books) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            document.add(new Paragraph("Export des livres").setFont(font).setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Date de génération : " + java.time.LocalDate.now()).setFont(font).setFontSize(10));
            Table table = new Table(new float[]{4, 3, 3, 2, 4});
            table.addHeaderCell(new Cell().add(new Paragraph("Titre")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Auteur")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("ISBN")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Stock")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Catégories")).setBold());
            for (Book book : books) {
                String categories = book.getCategories() == null ? "" : book.getCategories().stream().map(c -> c.getName()).collect(Collectors.joining(", "));
                table.addCell(new Cell().add(new Paragraph(book.getTitle())).setFont(font));
                table.addCell(new Cell().add(new Paragraph(book.getAuthor())).setFont(font));
                table.addCell(new Cell().add(new Paragraph(book.getIsbn())).setFont(font));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(book.getStock()))).setFont(font));
                table.addCell(new Cell().add(new Paragraph(categories)).setFont(font));
            }
            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Impossible d'exporter le PDF", ex);
        }
    }

    @Override
    public byte[] exportBooksToExcel(List<Book> books) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Livres");
            XSSFRow headerRow = sheet.createRow(0);
            XSSFFont headerFont = ((XSSFWorkbook) workbook).createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFont(headerFont);
            String[] headers = {"ID", "Titre", "Auteur", "ISBN", "Stock", "Catégories", "Créé le"};
            for (int i = 0; i < headers.length; i++) {
                XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowIndex = 1;
            for (Book book : books) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(book.getId());
                row.createCell(1).setCellValue(book.getTitle());
                row.createCell(2).setCellValue(book.getAuthor());
                row.createCell(3).setCellValue(book.getIsbn());
                row.createCell(4).setCellValue(book.getStock() == null ? 0 : book.getStock());
                String categories = book.getCategories() == null ? "" : book.getCategories().stream().map(c -> c.getName()).collect(Collectors.joining(", "));
                row.createCell(5).setCellValue(categories);
                row.createCell(6).setCellValue(book.getCreatedAt() == null ? "" : book.getCreatedAt().format(formatter));
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Impossible d'exporter l'Excel", ex);
        }
    }

    @Override
    public byte[] exportLoansToPdf(List<Loan> loans) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            document.add(new Paragraph("Export des emprunts").setFont(font).setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Date de génération : " + java.time.LocalDate.now()).setFont(font).setFontSize(10));
            Table table = new Table(new float[]{3, 3, 3, 2, 2});
            table.addHeaderCell(new Cell().add(new Paragraph("Livre")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Utilisateur")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Statut")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Début")).setBold());
            table.addHeaderCell(new Cell().add(new Paragraph("Retour prévu")).setBold());
            for (Loan loan : loans) {
                table.addCell(new Cell().add(new Paragraph(loan.getBook().getTitle())).setFont(font));
                table.addCell(new Cell().add(new Paragraph(loan.getUser().getUsername())).setFont(font));
                table.addCell(new Cell().add(new Paragraph(loan.getStatus().name())).setFont(font));
                table.addCell(new Cell().add(new Paragraph(loan.getLoanDate() == null ? "" : loan.getLoanDate().toString())).setFont(font));
                table.addCell(new Cell().add(new Paragraph(loan.getExpectedReturnDate() == null ? "" : loan.getExpectedReturnDate().toString())).setFont(font));
            }
            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Impossible d'exporter le PDF des emprunts", ex);
        }
    }
}
