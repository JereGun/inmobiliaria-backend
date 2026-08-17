package dev.jgunsett.inmobiliaria.application.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import dev.jgunsett.inmobiliaria.application.dto.company.CompanyResponse;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.InvoiceLine;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "AR"));
    private static final java.awt.Color BRAND = new java.awt.Color(15, 23, 42);
    private static final java.awt.Color MUTED = new java.awt.Color(100, 116, 139);
    private static final java.awt.Color LIGHT = new java.awt.Color(248, 250, 252);

    private final CompanyService companyService;

    public byte[] generate(Invoice invoice) {
        return generate(invoice, companyService.get());
    }

    byte[] generate(Invoice invoice, CompanyResponse company) {
        Document document = new Document(PageSize.A4, 42, 42, 36, 46);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, output);
            document.open();

            addHeader(document, company, invoice);
            addInformation(document, invoice);
            addLines(document, invoice);
            addTotal(document, invoice);
            addFooter(document, company);
            document.close();
            return output.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("No se pudo generar el PDF de la factura", ex);
        } finally {
            if (document.isOpen()) document.close();
        }
    }

    private void addHeader(Document document, CompanyResponse company, Invoice invoice) throws DocumentException {
        Font companyFont = font(FontFactory.HELVETICA_BOLD, 16, java.awt.Color.WHITE);
        Font titleFont = font(FontFactory.HELVETICA_BOLD, 14, java.awt.Color.WHITE);
        Font smallWhite = font(FontFactory.HELVETICA, 8, java.awt.Color.WHITE);
        PdfPTable header = new PdfPTable(new float[]{3, 2});
        header.setWidthPercentage(100);

        PdfPCell companyCell = darkCell();
        companyCell.addElement(new Paragraph(value(company == null ? null : company.getName()), companyFont));
        String address = companyAddress(company);
        if (!address.equals("-")) companyCell.addElement(new Paragraph(address, smallWhite));
        String contact = join(company == null ? null : company.getPhone(), company == null ? null : company.getEmail());
        if (!contact.equals("-")) companyCell.addElement(new Paragraph(contact, smallWhite));
        if (company != null && company.getTaxId() != null && !company.getTaxId().isBlank()) companyCell.addElement(new Paragraph("CUIT " + company.getTaxId(), smallWhite));
        header.addCell(companyCell);

        PdfPCell documentCell = darkCell();
        documentCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        documentCell.addElement(right("FACTURA", titleFont));
        documentCell.addElement(right(value(invoice.getCode()), smallWhite));
        documentCell.addElement(right("Emitida el " + formatDate(invoice.getDate() == null ? null : invoice.getDate().toLocalDate()), smallWhite));
        header.addCell(documentCell);
        document.add(header);
    }

    private void addInformation(Document document, Invoice invoice) throws DocumentException {
        document.add(section("INFORMACIÓN DE LA FACTURA"));
        Customer customer = invoice.getCustomer();
        PdfPTable table = new PdfPTable(new float[]{1, 2, 1, 2});
        table.setWidthPercentage(100);
        addInfo(table, "Cliente", customer == null ? "-" : customer.getFullName());
        addInfo(table, "Estado", invoiceStatus(invoice));
        addInfo(table, "Documento", customer == null ? "-" : join(customer.getDocumentType() == null ? null : customer.getDocumentType().name(), customer.getDocumentNumber()));
        addInfo(table, "Vencimiento", formatDate(invoice.getDueDate()));
        addInfo(table, "Contacto", customer == null ? "-" : join(customer.getEmail(), customer.getPhone()));
        addInfo(table, "Período", value(invoice.getBillingPeriod()));
        addInfo(table, "Contrato", invoice.getContract() == null ? "-" : "#" + invoice.getContract().getId());
        addInfo(table, "Tipo", invoice.getType() == null ? "-" : invoice.getType().name());
        document.add(table);
    }

    private void addLines(Document document, Invoice invoice) throws DocumentException {
        document.add(section("DETALLE FACTURADO"));
        PdfPTable lines = new PdfPTable(new float[]{4, 1, 2, 2});
        lines.setWidthPercentage(100);
        addHeaderCell(lines, "Concepto", Element.ALIGN_LEFT);
        addHeaderCell(lines, "Cant.", Element.ALIGN_CENTER);
        addHeaderCell(lines, "Precio unitario", Element.ALIGN_RIGHT);
        addHeaderCell(lines, "Subtotal", Element.ALIGN_RIGHT);
        for (InvoiceLine line : invoice.getLines()) {
            addLineCell(lines, value(line.getConcept()), Element.ALIGN_LEFT);
            addLineCell(lines, String.valueOf(line.getQuantity() == null ? 0 : line.getQuantity()), Element.ALIGN_CENTER);
            addLineCell(lines, money(line.getUnitPrice()), Element.ALIGN_RIGHT);
            addLineCell(lines, money(line.getSubtotal()), Element.ALIGN_RIGHT);
        }
        document.add(lines);
    }

    private void addTotal(Document document, Invoice invoice) throws DocumentException {
        PdfPTable total = new PdfPTable(new float[]{1, 1});
        total.setWidthPercentage(48);
        total.setHorizontalAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(12);
        PdfPCell label = darkCell();
        label.setPadding(10);
        label.addElement(new Paragraph("TOTAL", font(FontFactory.HELVETICA_BOLD, 9, new java.awt.Color(226, 232, 240))));
        PdfPCell amount = darkCell();
        amount.setPadding(8);
        amount.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amount.addElement(right(money(invoice.getTotal()), font(FontFactory.HELVETICA_BOLD, 15, java.awt.Color.WHITE)));
        total.addCell(label);
        total.addCell(amount);
        document.add(total);
    }

    private void addFooter(Document document, CompanyResponse company) throws DocumentException {
        Paragraph footer = new Paragraph(value(company == null ? null : firstNonBlank(company.getWebsite(), company.getEmail(), company.getName())), font(FontFactory.HELVETICA, 8, MUTED));
        footer.setSpacingBefore(28);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private Paragraph section(String value) {
        Paragraph paragraph = new Paragraph(value, font(FontFactory.HELVETICA_BOLD, 9, BRAND));
        paragraph.setSpacingBefore(14);
        paragraph.setSpacingAfter(7);
        return paragraph;
    }

    private void addInfo(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font(FontFactory.HELVETICA_BOLD, 8, MUTED)));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setPadding(4);
        PdfPCell valueCell = new PdfPCell(new Phrase(value(value), font(FontFactory.HELVETICA, 9, BRAND)));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setPadding(4);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String value, int alignment) {
        PdfPCell cell = darkCell();
        cell.setPadding(7);
        cell.setHorizontalAlignment(alignment);
        cell.setPhrase(new Phrase(value, font(FontFactory.HELVETICA_BOLD, 9, java.awt.Color.WHITE)));
        table.addCell(cell);
    }

    private void addLineCell(PdfPTable table, String value, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value(value), font(FontFactory.HELVETICA, 9, BRAND)));
        cell.setBackgroundColor(LIGHT);
        cell.setBorderColor(new java.awt.Color(226, 232, 240));
        cell.setPadding(7);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private PdfPCell darkCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setBackgroundColor(BRAND);
        cell.setPadding(9);
        return cell;
    }

    private Font font(String family, float size, java.awt.Color color) {
        int style = FontFactory.HELVETICA_BOLD.equals(family) ? Font.BOLD : Font.NORMAL;
        return FontFactory.getFont(family, size, style, color);
    }

    private Paragraph right(String value, Font font) {
        Paragraph paragraph = new Paragraph(value, font);
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        return paragraph;
    }

    private String formatDate(LocalDate date) { return date == null ? "-" : date.format(DATE_FORMAT); }
    private String value(String value) { return value == null || value.isBlank() ? "-" : value; }
    private String join(String first, String second) { return firstNonBlank(first, second, "-").equals("-") ? "-" : java.util.stream.Stream.of(first, second).filter(v -> v != null && !v.isBlank()).reduce((a, b) -> a + " | " + b).orElse("-"); }
    private String firstNonBlank(String first, String second, String fallback) { return first != null && !first.isBlank() ? first : second != null && !second.isBlank() ? second : fallback; }
    private String companyAddress(CompanyResponse company) { return company == null ? "-" : java.util.stream.Stream.of(join(company.getStreet(), company.getNumber()), company.getCity(), company.getProvince(), company.getCountry()).filter(v -> v != null && !v.isBlank() && !v.equals("-")).reduce((a, b) -> a + ", " + b).orElse("-"); }
    private String money(BigDecimal value) { return value == null ? "-" : NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(value); }
    private String invoiceStatus(Invoice invoice) { return switch (invoice.getStatus()) { case DRAFT -> "Borrador"; case ISSUED -> "Emitida"; case PARTIALLY_PAID -> "Pago parcial"; case PAID -> "Pagada"; case CANCELED -> "Anulada"; }; }
}
