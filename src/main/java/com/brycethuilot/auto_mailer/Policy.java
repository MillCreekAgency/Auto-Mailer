package com.brycethuilot.auto_mailer;
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

/**
 * Policy is the abstract base class for spefic policy types by company to extend from
 * It is used to read a policy from a PDF and extract its contents, update the policy in QQ Catalyst, Send to the insured and print for a mortgagee
 * Currently when extracting information from the PDF it can read
 * <ul>
 *     <li>Insured name</li>
 *     <li>Company name</li>
 *     <li>Insured address</li>
 *     <li>Policy number</li>
 *     <li>Expiration and Effective Dates</li>
 *     <li>Coverages</li>
 *     <li>premium</li>
 *     <li>Deductibles</li>
 *     <li>If it is a dwelling policy</li>
 *     <li>If it is a renewal or not</li>
 * </ul>
 * It is abstract so that the other classes can implement the specifics for different companies policy types and how
 * they format their PDFs differently but can provide an interface for {@link ApplicationWindow ApplicationWindow} to call the
 * {@link #updateOnQQ()} {@link #mailToInsured(ApplicationWindow)} and {@link #printMortgagee()} functions regardless of company
 *
 * @author Bryce Thuilot
 * @version  %I%, %G%
 * @since 1.0
 */
public abstract class Policy {

    /**
     * Stores the information about the policy
     */
    // Name of insured
    String name;
    // Name of company
    String company;
    // policy number of policy
    String policyNumber;
    // Address of Insured (location being insured, not necessarily mailing address)
    String address;
    // Effective date of policy
     String effectiveDate;
    // Expiration date of policy
     String expirationDate;
    // Coverages, Mapped by the option number in QQ
     HashMap<Integer, Double> coverages;
    // Premium of policy
     double premium;
    // Deductible of policy
     int deductible;
    // Hurricane Deductible of policy
     int hurricaneDeductible;
    // If the policy is a renew or not
     boolean renewal;
    // If the the policy is dwelling, if not it is homeowners
    boolean dwelling;


    // QQUpdater to update policy on QQCatalyst
    private QQUpdater updater;


    // Save policy file
    File policyFile;


    // Store user email if it has one
    private String insuredEmail;

    /**
     * Policy class is a parent class of the other types of Policies @see com.brycethuilot.auto_mailer.OceanHarbor,  com.brycethuilot.auto_mailer.NarragansetBay
     *
     * @param policyFile File object that points to the PDF to read in
     * @throws IOException if unable to read policyFile
     */
    public Policy(File policyFile) throws IOException{
        this.policyFile = policyFile;
        String pdfText = getPDFText(this.policyFile);
        this.getInfoFromPolicy(pdfText);
    }


    /**
     * Takes in a PDF and extracts and returns the text contents of it
     *
     * @param pdf the PDF file to extract the contents of
     * @return the text contents of the PDF
     * @throws IOException If Java is unable to open the PDF file
     */
    private static String getPDFText(File pdf) throws IOException{
        RandomAccessRead randomAccessFile = new RandomAccessFile(pdf, "r");
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

    /**
     * Returns a PDDocument containing only the pages needed to be sent to the insured if sent in a letter
     *
     * @param policy PDF Policy File object
     * @return PDF with only nessecary pages
     * @throws IOException If the policy file cannot be opened
     */
    public abstract PDDocument getLetterPages(File policy) throws IOException;

    // Sets for policy info

    /**
     * Extracts and sets the {@link #name name} variable
     *
     * @param pdfText Text contents of the PDF File
     */
    protected abstract void setName(String pdfText);

    /**
     * Extracts and sets the {@link #address address} variable
     *
     * @param pdfText Text contents of the PDF File
     */
    protected abstract void setAddress(String pdfText);

    /**
     * Extracts and sets the {@link #policyNumber policyNumber} variable
     *
     * @param pdfText  Text contents of the PDF File
     */
    protected abstract void setPolicyNumber(String pdfText);

    /**
     * Extracts and sets the {@link #effectiveDate effectiveDate} and {@link #expirationDate expirationDate} variables
     *
     * @param pdfText Text contents of the PDF File
     */
    protected abstract void setDates(String pdfText);

    /**
     * Extracts and sets the {@link #renewal renewal} variable
     *
     * @param pdfText Text contents of the PDF File
     */
    protected abstract void setRenewal(String pdfText);

    /**
     * Extracts and sets the {@link #premium premium} variable
     *
     * @param pdfText Text contents of the PDF File
     */
    protected abstract void setPremium(String pdfText);

    /**
     * Extracts and sets the {@link #deductible deductible} and {@link #hurricaneDeductible hurricaneDeductible} variables
     *
     * @param pdfText Text contents of the PDF File
     */
    protected abstract void setDeductibles(String pdfText);

    /**
     * Extracts and sets the {@link #dwelling dwelling} variable
     *
     * @param pdfText  Text contents of the PDF File
     */
    protected abstract void setDwelling(String pdfText);

    /**
     * Extracts and sets the {@link #coverages coverages} variable
     *
     * @param pdfText  Text contents of the PDF File
     */
    protected abstract void setCoverages(String pdfText);

    /**
     * Sets the name of the company
     */
    protected abstract void setCompany();

    /**
     * Gathers all info needed from the PDF's extracted text, and sets the class variables
     *
     * @param pdfText extracted text from policy
     */
    private void getInfoFromPolicy(String pdfText) {
        // Get Insured Name
        this.setName(pdfText);

        // Get insured Address
        this.setAddress(pdfText);

        // Get policy Number
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

        // Sets the name of the company
        this.setCompany();

    }

    /**
     * Tells if this policy type needs to be sent to the insured
     * @return a boolean telling whether or not to send to insured
     */
    public abstract boolean sendToInsrured();

    /**
     * Tells if this policy type needs to be sent to the mortgagee
     * @return a boolean telling whether or not to send to mortgagee
     */
    public abstract boolean sendToMortgagee();

    /**
     * Returns the previous policies number based on the current policy number
     *
     * @return the old policy number
     */
    public abstract String getOldPolicyNum();

    /**
     * Gets the insured's email in QQ. sets it to null if no email is found. Must be called after {@link #qqSignIn(String, String) qqSignIn}.
      * @throws NotSignedInException if QQ is not signed in
     */
    private void getEmail() throws NotSignedInException {
        if (this.updater == null) {
            throw new NotSignedInException();
        }
        this.insuredEmail = updater.getEmail(this.getOldPolicyNum());
    }

    /**
     * Creates the QQUpdater object and signs in
     * @param username username for QQ Catalyst
     * @param password password for QQ Catalyst
     */
    void qqSignIn(String username, String password) {
        if(this.updater == null) {
            this.updater = new QQUpdater(username, password);
        }
    }


    /**
     * Updates a policy on QQ with the gather information from {@link #policyFile policyFile} must be called after {@link #qqSignIn(String, String) qqSignIn}
     * @throws NotSignedInException if QQ is not singed into
     */
    void updateOnQQ() throws NotSignedInException {
        if (this.updater == null) {
            throw new NotSignedInException();
        }
        updater.updatePolicy(this.policyNumber, this.getOldPolicyNum(), this.premium, this.coverages, new int[]{this.deductible, this.hurricaneDeductible}, this.dwelling);
    }

    /**
     * Called to begin asking the user to pick which method to use to send to the insured. Must be called after {@link #qqSignIn(String, String) qqSignIn}
     * @param app application Window of application to create popups
     * @throws NotSignedInException if QQ is not signed in
     */
    void mailToInsured(ApplicationWindow app) throws NotSignedInException {
        this.getEmail();
        app.sendToInsured(this.insuredEmail, this, app);
    }

    /**
     * Creates and prints a letter addressed to the Insured
     * @throws IOException if the letter is unable to be created
     */
    void sendLetter() throws IOException {
        Letter letter = new Letter(this.policyNumber, this.name, this.address);
        letter.addPolicy(this.getLetterPages(this.policyFile));
        System.out.println("Running");
        letter.print();
    }

    /**
     * Closes the WebDriver visiting QQ
     */
    void closeQQ(){
        if(this.updater != null) {
            this.updater.close();
        }
    }

    /**
     * Prints the pages of the policy that needs to be sent to the mortgagee
     * @throws IOException if unable to read PDF
     */
    public abstract void printMortgagee() throws  IOException;

    /**
     * Opens the {@link ApplicationWindow#changeEmail(String, Policy) changeEmail} popup from application window
     * @param to the email to send to in case the user doesn't change
     * @param applicationWindow the application window to open the popup
     */
    void getEmailInfo(String to, ApplicationWindow applicationWindow) {
        applicationWindow.changeEmail(to, this);
    }


    /**
     * Sends an email to the address given using the password given
     *
     * @param to email address to send the email to
     * @param password password to the email set in the config
     * @return if email was successfully sent
     */
    boolean sendEmail(String to, String password) {
        // Create email class
        Email email = new Email(password);
        // Get subject line
        String subject = renewal ? "Insurance Renewal" : "Insurance Policy";
        // Get email body
        String body = email.emailBody(this.name, this.policyNumber, this.company, this.effectiveDate, this.expirationDate, this.renewal);
        // Send email
        return email.sendEmail(to, subject, body, this.policyFile.getAbsolutePath(), this.policyNumber + ".pdf");
    }

    /**
     * Calls {@link #trimMoneyValues(String) trimMoneyValues} on the argument given and parse's the string as a double
     *
     * @param string String to parse
     * @return A double value of the string given, 0 if could not be parsed
     */
    double getDoubleValue(String string) {
        return Double.parseDouble(trimMoneyValues(string));
    }

    /**
     * Calls {@link #trimMoneyValues(String) trimMoneyValues} on the argument given and parse's the string as a int
     *
     * @param string String parse
     * @return An int value of the string given, 0 if could not be parsed
     */
    int getIntValue(String string) {
        return Integer.parseInt(trimMoneyValues(string));
    }


    /**
     * Cuts a section out of str being with the index of start and going for length
     *
     * @param str the string to cut from
     * @param start the string to find in str and start cutting from that index
     * @param length how long of a string to cut out
     * @return the cut string
     */
    String cutSection(String str, String start, int length) {
        int index = str.indexOf(start);
        if (index == -1) {
            return "";
        } else {
            return str.substring(index + start.length(), index + start.length() + length).trim();
        }
    }

    /**
     * Removes and $ and , from a string to be parsed into a double or int
     * @param str the string to remove the characters from
     * @return a string without $ or , , 0 if it
     */
    private String trimMoneyValues(String str) {
        if(str.equals("")) {
            return "0";
        }
        StringBuilder string  = new StringBuilder(str);
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

    /**
     * Cuts a string out of srt starting at index of from and going to index of to
     *
     * @param str the string to cut from
     * @param from the starting index string
     * @param to the ending index string
     * @return the string cut or "" if to or from isnt found
     */
    String cutToFrom(String str, String from, String to) {
        int fromIndex = str.indexOf(from);
        int toIndex = str.indexOf(to, fromIndex + from.length());
        if (fromIndex == -1 || toIndex == -1) {
            return "";
        } else {
            return str.substring(fromIndex + from.length(), toIndex).trim();
        }

    }
}
