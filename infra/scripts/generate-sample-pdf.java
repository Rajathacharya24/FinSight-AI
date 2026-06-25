import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.io.File;

public class generate-sample-pdf {
    public static void main(String[] args) throws Exception {
        File out = new File(args.length > 0 ? args[0] : "samples/loan-app.pdf");
        out.getParentFile().mkdirs();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                cs.beginText();
                cs.setFont(font, 16);
                cs.newLineAtOffset(50, 750);
                cs.showText("LOAN APPLICATION FORM");
                cs.setFont(font, 11);
                cs.newLineAtOffset(0, -30);
                cs.showText("Applicant: John Doe");
                cs.newLineAtOffset(0, -20);
                cs.showText("Email: john.doe@email.com");
                cs.newLineAtOffset(0, -20);
                cs.showText("Annual Income: $85,000");
                cs.newLineAtOffset(0, -20);
                cs.showText("Credit Score: 720");
                cs.newLineAtOffset(0, -20);
                cs.showText("Loan Amount: $250,000");
                cs.newLineAtOffset(0, -20);
                cs.showText("Loan Purpose: Home Purchase");
                cs.newLineAtOffset(0, -20);
                cs.showText("Employment: Employed (5 years)");
                cs.newLineAtOffset(0, -20);
                cs.showText("DTI Ratio: 36%");
                cs.endText();
            }
            doc.save(out);
        }
        System.out.println("Created: " + out.getAbsolutePath());
    }
}
