// Import PDF Tool Box
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
// Java IO
import java.io.*;
// User input
import java.util.HashMap;

public abstract class Policy {
    // Policy info
    public String name;
    public String policyNumber;
    protected String address;
    protected String effectiveDate;
    protected String expirationDate;
    protected HashMap<Integer, Double> coverages;
    protected double premium;
    protected int deductible;
    protected int hurricaneDeductible;
    protected boolean renewal;
    protected String company;
    protected boolean dwelling;

    // Save policy file
    protected File policyFile;

    // Application window for GUI
    private ApplicationWindow applicationWindow;

    // Settings for renewal
    private boolean updateInQQ;
    private boolean printMortgagee;
    private boolean mailToInsured;

    public Policy(File policyFile, boolean updateInQQ, boolean printMortgage, boolean mailToInsured, ApplicationWindow applicationWindow) {
        this.policyFile = policyFile;
        this.updateInQQ = updateInQQ;
        this.applicationWindow = applicationWindow;
        this.printMortgagee = printMortgage;
        this.mailToInsured = mailToInsured;
        this.updatePolicy();
    }

    public static String getPDFText(File policy) throws IOException{
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

    public abstract PDDocument getLetterPages(File policy) throws IOException;

    // Sets for policy info
    protected abstract void setName(String pdfText);
    protected abstract void setAddress(String pdfText);
    protected abstract void setPolicyNumber(String pdfText);
    protected abstract void setDates(String pdfText);
    protected abstract void setRenewal(String pdfText);
    protected abstract void setPremium(String pdfText);
    protected abstract void setDeductibles(String pdfText);
    protected abstract void setDwelling(String pdfText);
    protected abstract void setCoverages(String pdfText);

    public void getInfoFromPolicy(String pdfText) {
        // Get Insured Name
        this.setName(pdfText);

        // Get insured Address
        this.setAddress(pdfText);

        // Get Policy Number
        this.setPolicyNumber(pdfText);

        // Get Effective and Expiration Dates
        this.setDates(pdfText);

        // Sets coverages of policy
        this.setCoverages(pdfText);

        // Gets whether is renewal or now
        this.setRenewal(pdfText);

        // Gets Premium
        this.setPremium(pdfText);

        // Get deductible and hurricane deductible
        this.setDeductibles(pdfText);

        // Sets if dwelling policy or not
        this.setDwelling(pdfText);

    }

    public abstract String getOldPolicyNum(String currentPolicyNum);


    public void updatePolicy() {
        // Gets insured info from policy
        try {
            String pdfText = getPDFText(this.policyFile);
            this.getInfoFromPolicy(pdfText);
        } catch (IOException io) {
            System.out.println("Was unable to open pdf");
            return;
        }


        QQUpdater updater = new QQUpdater(applicationWindow.getUsername(), applicationWindow.getPassword());

        String email = updater.getEmail(this.getOldPolicyNum(this.policyNumber));

        //Create scanner
        if(updateInQQ) {
            updater.updatePolicy(this.policyNumber, this.premium, this.coverages, new int[]{this.deductible, this.hurricaneDeductible}, this.dwelling);
        }

        updater.close();
        // Check if email was received
        if(mailToInsured) {
           applicationWindow.sendToInsured(email, this, applicationWindow);
        }

        /*try {
            if(printMortgagee) {
                this.printMortgagee();
            }
        }catch (IOException ex) {
            System.out.println("Could not print PDF");
        }*/
    }

    public void sendLetter() {
        Letter letter = new Letter(this.policyNumber, this.name, this.address);
        letter.createLetter();

        try {
            letter.addPolicyPages(this.getLetterPages(this.policyFile));
        }catch (IOException io) {
            System.out.println("Unable to add policy pages to letter");
        }
        letter.printLetter();
    }

    public abstract void printMortgagee() throws  IOException;

    public void getEmailInfo(String to, ApplicationWindow applicationWindow) {
        applicationWindow.changeEmail(to, this);
    }


    public void sendEmail(String to, String password) {
        // Create email class
        Email email = new Email(password);
        // Get subject line
        String subject = renewal ? "Insurance Renewal" : "Insurance Policy";
        // Get email body
        String body = email.emailBody(this.name, this.policyNumber, this.company, this.effectiveDate, this.expirationDate, this.renewal);
        // Send email
        email.sendEmail(to, subject, body, this.policyFile.getAbsolutePath(), this.policyNumber + ".pdf");
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
