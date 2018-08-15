import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Letter {


    // Store file
    private PDDocument letter;
    private PDDocument policy;

    // Editors Setting
    private static int fontSize = 11;

    // Holds Data
    private String policyNumber;
    private String name;
    private String address;
    private String dateString;



    public Letter(String policyNumber, String name, String address) {
        this.policyNumber = policyNumber;
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("E MMM d, yyyy");
        this.dateString =  df.format(date);
        this.name = name;
        this.address = address;
    }

    public void addPolicyPages(PDDocument policy) {
        for(int page = 0; page < policy.getNumberOfPages(); page++){
            letter.addPage(policy.getPage(page));
        }
    }


    public void printLetter() {
        Printer.printFile(this.letter);
    }

    public void newLine(PDPageContentStream stream, int amount) {
        for(int i = 0; i < amount; i++) {
            try {
                stream.newLine();
            }catch (IOException io) {
                System.out.println("Unable to add new line");
            }
        }
    }

    public void createLetter() {
        // Create a document and add a page to it
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage( page );


        try {
            // Start a new content stream which will "hold" the to be created content
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            //Fonts
            PDFont boldFont = PDType1Font.TIMES_BOLD;
            PDFont normalFont = PDType1Font.TIMES_ROMAN;

            // Start Text
            contentStream.beginText();
            contentStream.setFont(normalFont, fontSize);
            contentStream.setLeading(14.5f);

            // Date
            contentStream.newLineAtOffset(75, 625);
            contentStream.showText(this.dateString);

            //Name and Address
            contentStream.newLineAtOffset(0, -50);
            contentStream.showText(this.name);
            this.newLine(contentStream, 1);
            contentStream.showText(this.address);

            //Company and Policy Number
            contentStream.newLineAtOffset(0,-50);
            contentStream.showText("RE:   Company: " + "Ocean Harbor Casualty");
            this.newLine(contentStream, 1);
            contentStream.showText("         PolicyNumber: " + this.policyNumber);

            // Name
            contentStream.newLineAtOffset(0,-50);
            contentStream.showText("Dear " + this.name + ",");

            // Body
            contentStream.newLineAtOffset(0, -25);
            contentStream.showText("Enclosed, please find a copy of your policy renewal.");
            this.newLine(contentStream, 2);
            contentStream.showText("Because we value you as a client and policyholder, we would appreciate the opportunity to");
            this.newLine(contentStream, 1);
            contentStream.showText("serving all your insurances needs. This is also a great time to discuss your coverage options,");
            this.newLine(contentStream, 1);
            contentStream.showText("any changes that may need to be made, and any you may be qualified to receive.");
            this.newLine(contentStream, 2);
            contentStream.showText("Please feel free to call us at (631)751-4653 any time Monday through Friday 9 am and 5 pm");
            this.newLine(contentStream, 1);
            contentStream.showText("9 am and 5 pm");
            this.newLine(contentStream, 2);

            contentStream.setFont(boldFont, fontSize);
            contentStream.showText("If you have not done so already, please kindly e-mail us at info@MillCreekAgency.com");
            this.newLine(contentStream, 1);
            contentStream.showText("your current E-mail address so that we may have it on file for future communications.");
            this.newLine(contentStream, 2);

            contentStream.setFont(normalFont, fontSize);
            contentStream.showText("I hope we've done everything possible to earn your future insurance business.");
            this.newLine(contentStream, 1);
            contentStream.showText("We forward to speaking with you soon, and would like say thank you again for choosing");
            this.newLine(contentStream, 1);
            contentStream.showText("Mill Creek Agency, Inc.");
            contentStream.newLineAtOffset(0, -50);
            contentStream.setFont(normalFont, 12);
            contentStream.showText("Sincerely,");
            this.newLine(contentStream, 2);
            contentStream.showText("The Mill Creek Agency Inc.");
            contentStream.endText();
            contentStream.close();

            this.letter = document;
        }catch(IOException io) {
            System.out.println("Help");

        }
    }
}
