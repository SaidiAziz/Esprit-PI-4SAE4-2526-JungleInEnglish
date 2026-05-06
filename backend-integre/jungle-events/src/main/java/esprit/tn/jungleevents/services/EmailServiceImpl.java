package esprit.tn.jungleevents.services;

import com.lowagie.text.pdf.draw.LineSeparator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.ByteArrayOutputStream;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.rib.bank}")
    private String bank;

    @Value("${app.rib.iban}")
    private String iban;

    @Value("${app.rib.bic}")
    private String bic;

    @Value("${app.rib.account.name}")
    private String accountName;

    @Override
    public void sendRibEmail(String toEmail, String eventTitle,
                             String sessionDate, double amount) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Bank Transfer Details — " + eventTitle);
            helper.setText(buildEmailContent(eventTitle, sessionDate, amount), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    private String buildEmailContent(String eventTitle, String sessionDate, double amount) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <div style="background: linear-gradient(135deg, #667eea, #764ba2); padding: 30px; text-align: center; border-radius: 12px 12px 0 0;">
                <h1 style="color: white; margin: 0;">🏦 Bank Transfer Details</h1>
              </div>
              <div style="padding: 30px; background: #f9f9f9;">
                <p>Thank you for registering for <strong>%s</strong> on <strong>%s</strong>.</p>
                <p>Please transfer <strong>%.2f TND</strong> to the following account:</p>
                <div style="background: white; border-radius: 10px; padding: 20px; margin: 20px 0; border-left: 4px solid #667eea;">
                  <table style="width: 100%%;">
                    <tr><td style="color: #888; padding: 6px 0;">Bank</td><td><strong>%s</strong></td></tr>
                    <tr><td style="color: #888; padding: 6px 0;">Account Name</td><td><strong>%s</strong></td></tr>
                    <tr><td style="color: #888; padding: 6px 0;">IBAN</td><td><strong>%s</strong></td></tr>
                    <tr><td style="color: #888; padding: 6px 0;">BIC/SWIFT</td><td><strong>%s</strong></td></tr>
                    <tr><td style="color: #888; padding: 6px 0;">Amount</td><td><strong style="color: #667eea;">%.2f TND</strong></td></tr>
                  </table>
                </div>
                <p style="color: #e53935;">⚠️ Please use your name and event title as payment reference.</p>
                <p style="color: #888; font-size: 13px;">Your registration will be confirmed once payment is received.</p>
              </div>
            </body>
            </html>
            """.formatted(eventTitle, sessionDate, amount, bank, accountName, iban, bic, amount);
    }
    @Override
    public void sendCertificateEmail(String to, String participantName,
                                     String eventTitle, String sessionDate) {
        try {
            // ── 1. Générer le PDF ──────────────────────────────────────
            ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate(), 40, 40, 40, 40);
            PdfWriter.getInstance(doc, pdfOut);   // une seule fois
            doc.open();

            // Couleurs (java.awt.Color, pas BaseColor)
            java.awt.Color darkGreen  = new java.awt.Color(26, 60, 46);
            java.awt.Color gold       = new java.awt.Color(201, 168, 76);
            java.awt.Color lightGreen = new java.awt.Color(45, 106, 79);
            java.awt.Color textGray   = new java.awt.Color(55, 65, 81);
            java.awt.Color white      = java.awt.Color.WHITE;
            java.awt.Color subtleGreen = new java.awt.Color(200, 220, 210);
            java.awt.Color lightGray  = new java.awt.Color(107, 114, 128);
            java.awt.Color footerGray = new java.awt.Color(156, 163, 175);

            // Polices
            BaseFont bf      = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false);
            BaseFont bfLight = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);

            // ── Header vert ──
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell hCell = new PdfPCell();
            hCell.setBackgroundColor(darkGreen);
            hCell.setPadding(24);
            hCell.setBorder(Rectangle.NO_BORDER);
            Paragraph logo = new Paragraph("JUNGLE",
                    new Font(bf, 22, Font.BOLD, white));
            logo.setAlignment(Element.ALIGN_CENTER);
            hCell.addElement(logo);
            Paragraph sub = new Paragraph("Plateforme d'apprentissage & developpement",
                    new Font(bfLight, 9, Font.NORMAL, subtleGreen));
            sub.setAlignment(Element.ALIGN_CENTER);
            hCell.addElement(sub);
            header.addCell(hCell);
            doc.add(header);

            // Ligne dorée
            PdfPTable goldLine = new PdfPTable(1);
            goldLine.setWidthPercentage(100);
            PdfPCell gCell = new PdfPCell();
            gCell.setBackgroundColor(gold);
            gCell.setFixedHeight(4);
            gCell.setBorder(Rectangle.NO_BORDER);
            goldLine.addCell(gCell);
            doc.add(goldLine);

            // ── Corps ──
            doc.add(new Paragraph(" "));

            Paragraph label = new Paragraph("* CERTIFICAT OFFICIEL *",
                    new Font(bf, 10, Font.BOLD, gold));
            label.setAlignment(Element.ALIGN_CENTER);
            label.setSpacingAfter(6);
            doc.add(label);

            Paragraph title = new Paragraph("Certificat de Participation",
                    new Font(bf, 32, Font.BOLD, darkGreen));
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            doc.add(title);

            Paragraph subtitle = new Paragraph("& Reussite",
                    new Font(bfLight, 14, Font.ITALIC, lightGreen));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            doc.add(subtitle);

            LineSeparator ls = new LineSeparator(1, 60, gold, Element.ALIGN_CENTER, -2);
            doc.add(new Chunk(ls));
            doc.add(new Paragraph(" "));

            Paragraph presented = new Paragraph("Decerne a",
                    new Font(bfLight, 10, Font.NORMAL, lightGray));
            presented.setAlignment(Element.ALIGN_CENTER);
            presented.setSpacingAfter(4);
            doc.add(presented);

            Paragraph nameP = new Paragraph(participantName,
                    new Font(bf, 30, Font.BOLD, darkGreen));
            nameP.setAlignment(Element.ALIGN_CENTER);
            nameP.setSpacingAfter(16);
            doc.add(nameP);

            doc.add(new Chunk(new LineSeparator(1, 60, gold, Element.ALIGN_CENTER, -2)));
            doc.add(new Paragraph(" "));

            Paragraph desc = new Paragraph(
                    "Ce certificat est remis en reconnaissance de la participation active\n" +
                            "et de l'assiduite demontre lors de l'evenement :",
                    new Font(bfLight, 11, Font.NORMAL, textGray));
            desc.setAlignment(Element.ALIGN_CENTER);
            desc.setSpacingAfter(8);
            doc.add(desc);

            Paragraph evtTitle = new Paragraph(eventTitle,
                    new Font(bf, 16, Font.BOLD, darkGreen));
            evtTitle.setAlignment(Element.ALIGN_CENTER);
            evtTitle.setSpacingAfter(6);
            doc.add(evtTitle);

            Paragraph evtDate = new Paragraph("Session du " + sessionDate,
                    new Font(bfLight, 11, Font.NORMAL, lightGreen));
            evtDate.setAlignment(Element.ALIGN_CENTER);
            evtDate.setSpacingAfter(20);
            doc.add(evtDate);

            String today = new java.text.SimpleDateFormat("dd MMMM yyyy",
                    java.util.Locale.FRENCH).format(new java.util.Date());
            Paragraph footer = new Paragraph(
                    "Delivre le " + today + "   |   Direction Jungle   |   Responsable pedagogique",
                    new Font(bfLight, 8, Font.NORMAL, footerGray));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(10);
            doc.add(footer);

            doc.close();

            // ── 2. Email avec PDF en pièce jointe ──────────────────────
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Votre Certificat de Participation - " + eventTitle);
            helper.setText(
                    "<div style='font-family:Arial,sans-serif;color:#1a3c2e;padding:24px;max-width:600px;margin:auto;'>"
                            + "<div style='background:linear-gradient(135deg,#1a3c2e,#2d6a4f);padding:28px;text-align:center;border-radius:10px 10px 0 0;'>"
                            + "<h2 style='color:#fff;margin:0;'>Félicitations " + participantName + " !</h2></div>"
                            + "<div style='padding:28px;background:#f9f9f9;border-radius:0 0 10px 10px;'>"
                            + "<p>Vous avez complété avec succès l'événement <strong>" + eventTitle
                            + "</strong> (session du " + sessionDate + ").</p>"
                            + "<p>Votre <strong>certificat officiel</strong> est disponible en pièce jointe au format PDF.<br/>"
                            + "Vous pouvez le télécharger et le conserver.</p>"
                            + "<p style='color:#888;font-size:12px;margin-top:24px;'>— L'équipe Jungle</p>"
                            + "</div></div>", true);

            helper.addAttachment(
                    "Certificat_" + participantName.replace(" ", "_") + ".pdf",
                    new ByteArrayResource(pdfOut.toByteArray()),
                    "application/pdf"
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi certificat : " + e.getMessage(), e);
        }
    }



}