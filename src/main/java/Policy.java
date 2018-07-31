// Import PDF Tool Box
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
// Import RTFEditorSuite
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
// Java IO
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
// Date formating
import java.text.SimpleDateFormat;
import java.util.Date;
// User input
import java.util.HashMap;

public abstract class Policy {
    // Constants
    public static final String WORK_FOLDER = System.getProperty("user.home") + "policies/send-out/";
    public static final String RENEWAL_LETTER = "../RenewalLetter.rtf";
    public static final String PRINT_FOLDER = "/Users/bryce/print";

    // Policy info
    public String name;
    public String policyNumber;
    protected String fileLocation;
    protected String address;
    protected String effectiveDate;
    protected String expirationDate;
    protected HashMap<Integer, Double> coverages;
    protected double premium;
    protected double deductible;
    protected double hurricaneDeductible;
    protected boolean renewal;
    protected String company;
    protected boolean dwelling;

    private ApplicationWindow applicationWindow;

    // Settings for renewal
    private boolean updateInQQ;
    private boolean printMortgagee;
    private boolean mailToInsured;

    public Policy(String fileLocation, boolean updateInQQ, boolean printMortgage, boolean mailToInsured, ApplicationWindow applicationWindow) {
        this.fileLocation = fileLocation;
        this.updateInQQ = updateInQQ;
        this.applicationWindow = applicationWindow;
        this.printMortgagee = printMortgage;
        this.mailToInsured = mailToInsured;
        this.updatePolicy(fileLocation);
    }

    public static String getPDFText(String fileLocation) throws IOException{
        File policy = new File(fileLocation);
        RandomAccessRead randomAccessFile = new RandomAccessFile(policy, "r");
        PDFParser parser = new PDFParser(randomAccessFile);
        parser.parse();
        COSDocument cosDoc = parser.getDocument();
        PDFTextStripper pdfStripper = new PDFTextStripper();
        PDDocument pdDoc = new PDDocument(cosDoc);
        pdfStripper.setStartPage(1);
        pdfStripper.setEndPage(5);
        String pdfText = pdfStripper.getText(pdDoc);
        cosDoc.close();
        pdDoc.close();
        return pdfText;
    }

    public abstract void getInfoFromPolicy(String fileLocation) throws IOException;

    public void makeLetter(){
        try {
            // Create policyFile input Stream
            File letterTemplate = new File(Policy.RENEWAL_LETTER);
            InputStream targetStream = new FileInputStream(letterTemplate);
            // Create RTFEditorKit
            RTFEditorKit rtfeditor = new RTFEditorKit();
            // Create empty document
            DefaultStyledDocument rtfLetter = new DefaultStyledDocument();
            // Have editor read from Stream
            rtfeditor.read(targetStream, rtfLetter, 0);
            //  get text from Editor
            String letter = rtfLetter.getText(0, rtfLetter.getLength());



            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("E MMM d, yyyy");
            // Set date
            letter = letter.replaceAll("DATE", df.format(date));
            // Set First name
            letter = letter.replaceAll("FIRST_NAME", name.split(" ")[0]);
            // Set Name
            letter = letter.replaceAll("FULL_NAME", name);
            // Set Address
            letter = letter.replaceAll("ADDRESS", address);
            // Set Policy Number
            letter = letter.replaceAll("NUMBER_OF_POLICY", policyNumber);
            // Write to policyFile
            rtfLetter.replace(0, rtfLetter.getLength(), letter, null);
            OutputStream outputStream = new FileOutputStream(Policy.WORK_FOLDER + policyNumber + ".rtf");
            rtfeditor.write(outputStream, rtfLetter, 0, rtfLetter.getLength());

        } catch (Exception ex) {
            System.out.println("Failed to read letter");
            System.out.println(ex.getClass());
            return;
        }
    }

    public abstract String getOldPolicyNum(String currentPolicyNum);

    public void updatePolicy(String fileLocation) {
        // Gets insured info from policy
        try {
            this.getInfoFromPolicy(fileLocation);
        } catch(IOException ioEx) {
            System.out.println("Unable to open PDF");
            return;
        }
        QQUpdater updater = new QQUpdater(applicationWindow.getUsername(), applicationWindow.getPassword());

        String email = updater.getEmail(this.getOldPolicyNum(this.policyNumber));

        //Create scanner
        if(updateInQQ) {
            updater.updatePolicy(this.policyNumber, this.premium, this.coverages, new double[]{this.deductible, this.hurricaneDeductible}, this.dwelling);
        }
        // Check if email was received
        if(mailToInsured) {
            if (this.letter || email.equals("")) {
                // Make letter using insured info
                this.makeLetter();
            } else {
                // TODO check email
                // Send email
                this.getEmailPassword(email, fileLocation, this.applicationWindow);
            }
        }


        this.displayInfo();
        try {
            if(printMortgagee) {
                this.printMortgagee(fileLocation);
            }
        }catch (IOException ex) {
            System.out.println("Could not print PDF");
        }

        try {
            Files.move(Paths.get(fileLocation), Paths.get(WORK_FOLDER + this.policyNumber + ".pdf"));
        } catch (Exception ex) {
            System.out.println("Unable to move policyFile");
        }
    }

    public abstract void printMortgagee(String fileLocation) throws  IOException;

    public abstract String printCoverages();

    public void displayInfo() {
        // TODO display Info on application
        System.out.println(String.join(
                System.getProperty("line.separator"),
                "Insured Name: " + this.name,
                "Address: " + this.address,
                "Policy Number: " + this.policyNumber,
                "Coverages: " + this.printCoverages(),
                "Premium: " + this.premium,
                "Deductible: " + this.deductible,
                "Hurricane Deductible: " + this.hurricaneDeductible
                )
        );
    }

    public void getEmailPassword(String to, String fileLocation, ApplicationWindow applicationWindow) {
        applicationWindow.getEmailPassword(to, fileLocation, this);
    }

    public void sendEmail(String to, String fileLocation, String password) {
        // Create email class
        Email email = new Email(password);
        // Get subject line
        String subject = renewal ? "Insurance Renewal" : "Insurance Policy";
        // Get email body
        String body = email.emailBody(this.name, this.policyNumber, this.company, this.effectiveDate, this.expirationDate, this.renewal);
        // Send email
        email.sendEmail(to, subject, body, fileLocation, this.policyNumber);
    }

    public String cutSection(String str, String start, int length) {
        int index = str.indexOf(start);
        if (index == -1) {
            return "0";
        } else {
            StringBuilder string = new StringBuilder(str.substring(index + start.length(), index + start.length() + length).trim());
           return this.trimMoneyValues(string);
        }
    }

    private String trimMoneyValues(StringBuilder string) {
        int indexOfComma = string.indexOf(",");
        int indexOfDollarSign = string.indexOf("$");
        if (indexOfComma != -1) {
            string.deleteCharAt(indexOfComma);
        }
        if(indexOfDollarSign != -1) {
            string.deleteCharAt(indexOfDollarSign);
        }
        return string.toString();
    }

    public String cutToFrom(String str, String from, String to) {
        int fromIndex = str.indexOf(from) + from.length();
        int toIndex = str.indexOf(to, fromIndex);
        if (fromIndex == -1 || toIndex == -1) {
            return "0";
        } else {
            StringBuilder string = new StringBuilder(str.substring(fromIndex, toIndex).trim());
            return this.trimMoneyValues(string);
        }

    }
}
