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
import java.util.Scanner;

public abstract class Policy {
    // Constants
    public static final String WORK_FOLDER = "/Users/brycethuilot/work/send-out/";
    public static final String RENEWAL_LETTER = "/Users/brycethuilot/GitHub/Auto-Mailer-Gui/RenewalLetter.rtf";

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

    // Settings for renewal
    private boolean letter;
    private boolean emailOnly;

    public Policy(String fileLocation, boolean letter, boolean emailOnly) {
        this.fileLocation = fileLocation;
        this.letter = letter;
        this.emailOnly = emailOnly;
        this.mailToInsured(fileLocation);
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

    public void makeLetter(String name, String address, String policyNumber){
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

            // TODO Add Print support
        } catch (Exception ex) {
            System.out.println("Failed to read letter");
            System.out.println(ex.getClass());
            return;
        }
    }

    public abstract String getOldPolicyNum(String currentPolicyNum);

    public void mailToInsured(String fileLocation) {
        // Gets insured info from policy
        try {
            this.getInfoFromPolicy(fileLocation);
        } catch(IOException ioEx) {
            System.out.println("Unable to open PDF");
            return;
        }
        QQUpdater updater = new QQUpdater();

        // TODO Launch Web Driver
        String email = updater.getEmail(this.getOldPolicyNum(this.policyNumber));

        //Create scanner
        Scanner scanner = new Scanner(System.in);
        if(!emailOnly) {
            updater.updatePolicy(this.policyNumber, this.premium, this.coverages, new double[]{this.deductible, this.hurricaneDeductible}, this.dwelling);
        }
        // Check if email was recieved
        if(this.letter) {
            // Ask if sending a letter is what to if no email is present
            System.out.println("Send letter to insured? [Y/n]");
            String sendLetter = scanner.nextLine();
            // See what user responded
            if (!sendLetter.equals("n")) {
                // Make and send letter
                this.makeLetter(this.name, this.address, this.policyNumber);
            }
        }else {
            // See if Web Driver got the correct email
            System.out.println("Does " + email + " look correct? [Y/n]");
            String useEmail = scanner.nextLine();
            // If not ask the user for the correct email
            if(useEmail.equals("n")) {
                System.out.println("Please enter email:");
                email = scanner.nextLine();
            }
            // Send email
            this.sendEmail(email, fileLocation, "Hello");
        }

        // TODO add remote support

        this.displayInfo();

        // TODO Print Support

        try {
            Files.move(Paths.get(fileLocation), Paths.get(WORK_FOLDER + this.policyNumber + ".pdf"));
        } catch (Exception ex) {
            System.out.println("Unable to move policyFile");
        }
    }

    public abstract String printCoverages();

    public void displayInfo() {
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

    public void sendEmail(String to, String fileLocation, String password) {
        // Get the password to the email account
        // TODO fix get password
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
