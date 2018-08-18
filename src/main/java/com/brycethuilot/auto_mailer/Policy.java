package com.brycethuilot.auto_mailer;// Import PDF Tool Box
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

    // com.brycethuilot.auto_mailer.Policy info

    /**
     * Stores the information about the policy
     */
    // Name of insured
    public String name;
    // Name of company
    public String company;
    // policy number of policy
    public String policyNumber;
    // Address of Insured (location being insured, not necessarily mailing address)
    protected String address;
    // Effective date of policy
    protected String effectiveDate;
    // Expiration date of policy
    protected String expirationDate;
    // Coverages, Mapped by the option number in QQ
    protected HashMap<Integer, Double> coverages;
    // Premium of policy
    protected double premium;
    // Deductible of policy
    protected int deductible;
    // Hurricane Deductible of policy
    protected int hurricaneDeductible;
    // If the policy is a renew or not
    protected boolean renewal;
    // If the the policy is dwelling, if not it is homeowners
    protected boolean dwelling;


    // Save policy file
    protected File policyFile;


    // Application window for GUI, used to call error windows
    private ApplicationWindow applicationWindow;


    /**
     * Settings for renewal
     */
    // if the policy should be updated in QQ
    private boolean updateInQQ;
    // if the policy should be printed for mortgagee
    private boolean printMortgagee;
    // If the policy should be sent to the insured (either by email or letter)
    private boolean mailToInsured;

    /**
     * Policy class is a parent class of the other types of Policies @see com.brycethuilot.auto_mailer.OceanHarbor or @see com.brycethuilot.auto_mailer.NarragansetBay
     *
     * @param policyFile File object that points to the PDF to read in
     * @param updateInQQ Whenether or not to update in QQ Catalyst using Selenium webdriver
     * @param printMortgage
     * @param mailToInsured
     * @param applicationWindow
     */
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

        // Get com.brycethuilot.auto_mailer.Policy Number
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
            applicationWindow.errorPopup("Unable to read PDF");
            return;
        }



        QQUpdater updater = new QQUpdater(applicationWindow.getUsername(), applicationWindow.getPassword());;
        String email = null;
        if(mailToInsured || updateInQQ) {
            email = updater.getEmail(this.getOldPolicyNum(this.policyNumber));
        }


        //Create scanner
        if(updateInQQ) {
            updater.updatePolicy(this.policyNumber, this.premium, this.coverages, new int[]{this.deductible, this.hurricaneDeductible}, this.dwelling);
        }

        updater.close();
        // Check if email was received
        if(mailToInsured) {
           applicationWindow.sendToInsured(email, this, applicationWindow);
        }

        if(this.printMortgagee) {
            try {
                this.printMortgagee();
            } catch (IOException ex) {
                applicationWindow.errorPopup("Unable to print PDF for mortgage");
            }
        }
    }

    public void sendLetter() {
        Letter letter = new Letter(this.policyNumber, this.name, this.address);
        try {
            letter.createLetter();
        } catch (IOException io) {
            applicationWindow.errorPopup("Unable to create letter for Insured");
        }

        try {
            letter.addPolicy(this.getLetterPages(this.policyFile));
        }catch (IOException io) {
            applicationWindow.errorPopup("Unable to read PDF (Maybe its location changed?");
        }
        System.out.println("Running");
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
        String subject = renewal ? "Insurance Renewal" : "Insurance com.brycethuilot.auto_mailer.Policy";
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
