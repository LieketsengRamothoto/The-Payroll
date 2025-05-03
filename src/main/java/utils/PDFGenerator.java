package utils;

import models.Payroll;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PDFGenerator {
    public static void generate(Payroll payroll, String filePath) {
        try {
            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph("Payslip for: " + payroll.getEmployeeName()));
            document.add(new Paragraph(String.format("Gross Salary: %.2f", payroll.getGrossSalary())));
            document.add(new Paragraph(String.format("Deductions: %.2f", payroll.getDeductions())));
            document.add(new Paragraph(String.format("Net Salary: %.2f", payroll.getNetSalary())));
            document.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateMultiple(List<Payroll> payrollList, String filePath) {
        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            for (Payroll payroll : payrollList) {
                document.add(new Paragraph("Payslip for: " + payroll.getEmployeeName()));
                document.add(new Paragraph(String.format("Gross Salary: %.2f", payroll.getGrossSalary())));
                document.add(new Paragraph(String.format("Deductions: %.2f", payroll.getDeductions())));
                document.add(new Paragraph(String.format("Net Salary: %.2f", payroll.getNetSalary())));
                document.add(new Paragraph("------------------------------------------------------------"));
            }

            document.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
