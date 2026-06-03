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
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.UnitValue;
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

    public byte[] exportBooksToPdf(List<Book> books) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.setMargins(40, 40, 40, 40);

            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            DeviceRgb primaryColor = new DeviceRgb(59, 130, 246); // Blue 500
            DeviceRgb headerBgColor = new DeviceRgb(241, 245, 249); // Slate 100
            DeviceRgb evenRowColor = new DeviceRgb(248, 250, 252); // Slate 50

            Paragraph title = new Paragraph("CATALOGUE DES LIVRES")
                    .setFont(boldFont)
                    .setFontSize(22)
                    .setFontColor(primaryColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(title);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            Paragraph dateText = new Paragraph("Généré le : " + java.time.LocalDateTime.now().format(formatter))
                    .setFont(font)
                    .setFontSize(10)
                    .setFontColor(new DeviceRgb(100, 116, 139))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(dateText);

            Table table = new Table(UnitValue.createPercentArray(new float[]{4, 3, 3, 1.5f, 3.5f})).useAllAvailableWidth();

            String[] headers = {"Titre", "Auteur", "ISBN", "Stock", "Catégories"};
            for (String header : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(header).setFont(boldFont).setFontSize(11))
                        .setBackgroundColor(headerBgColor)
                        .setFontColor(new DeviceRgb(15, 23, 42))
                        .setPadding(10)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(primaryColor, 2));
                table.addHeaderCell(headerCell);
            }

            int rowIndex = 0;
            for (Book book : books) {
                boolean isEven = (rowIndex % 2 == 0);
                Color rowBg = isEven ? evenRowColor : ColorConstants.WHITE;

                String categories = book.getCategories() == null ? "-" : book.getCategories().stream().map(c -> c.getName()).collect(Collectors.joining(", "));

                table.addCell(createCell(book.getTitle(), font, rowBg));
                table.addCell(createCell(book.getAuthor(), font, rowBg));
                table.addCell(createCell(book.getIsbn(), font, rowBg));

                Cell stockCell = createCell(String.valueOf(book.getStock()), font, rowBg);
                if (book.getStock() == null || book.getStock() == 0) {
                    stockCell.setFontColor(new DeviceRgb(220, 38, 38)).setBold();
                }
                table.addCell(stockCell);

                table.addCell(createCell(categories, font, rowBg));

                rowIndex++;
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

    private Cell createCell(String content, PdfFont font, Color bgColor) {
        return new Cell()
                .add(new Paragraph(content != null ? content : "-").setFont(font).setFontSize(10))
                .setBackgroundColor(bgColor)
                .setPadding(8)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(226, 232, 240), 1));
    }
}
