import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Letter {

    public static String LETTER_LOCATION = "./RenewalLetter.rtf";
    private String outputLocation;

    // RTF Editors
    private RTFEditorKit rtfEditor;
    private DefaultStyledDocument rtfLetter;

    // Holds Data
    private String policyNumber;
    private String name;
    private String address;
    private String dateString;



    public Letter(String policyNumber, String name, String address) {
        this.policyNumber = policyNumber;
        this.outputLocation = Policy.WORK_FOLDER + policyNumber + ".rtf";
        this.rtfEditor = new RTFEditorKit();
        this.rtfLetter = new DefaultStyledDocument();
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("E MMM d, yyyy");
        this.dateString =  df.format(date);
        this.name = name;
        this.address = address;
    }

    private String replacePlaceholderText(String letter) {

        // Set date
        letter = letter.replaceAll("DATE", dateString);
        // Set First name
        letter = letter.replaceAll("FIRST_NAME", name.split(" ")[0]);
        // Set Name
        letter = letter.replaceAll("FULL_NAME", name);
        // Set Address
        letter = letter.replaceAll("ADDRESS", address);
        // Set Policy Number
        letter = letter.replaceAll("NUMBER_OF_POLICY", policyNumber);
        return letter;
    }

    public void makeLetter(){
        try {

            this.name = name;
            this.address = address;

            // Create policyFile input Stream
            File letterTemplate = new File(LETTER_LOCATION);
            InputStream targetStream = new FileInputStream(letterTemplate);
            // Create empty document
            rtfLetter = new DefaultStyledDocument();
            // Have editor read from Stream
            rtfEditor.read(targetStream, rtfLetter, 0);
            //  get text from Editor
            String letter = replacePlaceholderText(rtfLetter.getText(0, rtfLetter.getLength()));

            // Write to policyFile
            rtfLetter.replace(0, rtfLetter.getLength(), letter, null);
            OutputStream outputStream = new FileOutputStream(this.outputLocation);
            rtfEditor.write(outputStream, rtfLetter, 0, rtfLetter.getLength());

        } catch (Exception ex) {
            System.out.println("Failed to read letter");
            System.out.println(ex.getClass());
            System.out.println(ex.getMessage());
            return;
        }
    }

    public void printLetter() {
        try {
            Desktop.getDesktop().print(new File(this.outputLocation));
        } catch (IOException io) {
            System.out.println("Unable to read created letter");
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
            PDFont boldFont = PDType1Font.HELVETICA_BOLD;
            PDFont normalFont = PDType1Font.HELVETICA;

            // Start Text
            contentStream.beginText();
            contentStream.setFont(normalFont, 12);

            // Date
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText(this.dateString);

            //Name and Address
            contentStream.newLine();
            contentStream.newLine();
            contentStream.showText(this.name);
            contentStream.newLine();
            contentStream.showText(this.address);

            //Company and Policy Number
            contentStream.newLineAtOffset(0,100);
            contentStream.showText("RE:   Company: " + "Ocean Harbor Casualty");
            contentStream.newLine();
            contentStream.showText("       PolicyNumber: " + this.policyNumber);

            // Name
            contentStream.newLineAtOffset(0,150);
            contentStream.showText("Dear " + this.name);

            // Body
            contentStream.newLineAtOffset(0, 100);
            contentStream.showText("Enclosed, please find a copy of your policy renewal.");
            contentStream.newLine();
            contentStream.showText("Because we value you as a client and policyholder, we would appreciate the opportunity to continue serving all your insurances needs. This is also a great time to discuss your coverage options, any changes that may need to be made, and any discounts you may be qualified to receive.");
            contentStream.newLine();
            contentStream.showText("Please feel free to call us at (631)751-4653 any time Monday through Friday between 9 am and 5 pm");
            contentStream.newLine();

            contentStream.setFont(boldFont, 12);
            contentStream.showText("If you have not done so already, please kindly E-mail us at Info@MillCreekAgency.com with your current E-mail address so that we may have it on policyFile for future communications.");
            contentStream.newLine();
            contentStream.showText("I hope we've done everything possible to earn your future insurance business. I look forward to speaking with you soon, and would like say thank you again for choosing The Mill Creek Agency, Inc.");
            contentStream.newLine();
            contentStream.setFont(normalFont, 12);
            contentStream.showText("Sincerely,");
            contentStream.newLine();
            contentStream.newLine();
            contentStream.newLine();
            contentStream.showText("The Mill Creek Agency Inc.");
            contentStream.endText();

// Make sure that the content stream is closed:
            contentStream.close();

// Save the results and ensure that the document is properly closed:
            document.save("Hello World.pdf");
            document.close();
        }catch(IOException io) {
            System.out.println("Help");

        }
    }
}
