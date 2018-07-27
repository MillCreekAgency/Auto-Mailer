import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public abstract class Policy {
    // Constants
    public static final String WORK_FOLDER = "/Users/bryce/policies/send-out/";
    public static final String RENEWAL_LETTER = "../RenewalLetter.rtf";

    // Policy info
    public String name;
    public String policyNumber;
    private String fileLocation;
    private String address;
    private String effectiveDate;
    private String expirationDate;
    private int[] coverages;
    private int premium;
    private int deductible;
    private int hurricaneDeductible;
    private boolean renewal;
    private String company;

    // Settings for renewal
    private boolean letter;
    private boolean emailOnly;

    public Policy(String fileLocation, boolean letter, boolean emailOnly) {
        this.fileLocation = fileLocation;
        this.letter = letter;
        this.emailOnly = emailOnly;
        this.mailToInsured(fileLocation);
    }

    public abstract void getInfoFromPolicy(String fileLocation);

    public void makeLetter(String name, String address, String policyNumber){
        try {
            // TODO fix
            String letter = new String(Files.readAllBytes(Paths.get(RENEWAL_LETTER)));
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("E M d, yyyy");
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
            System.out.println("RanY");

            // Write to file
            PrintWriter writer = new PrintWriter(WORK_FOLDER + policyNumber + ".rtf", "UTF-8");
            writer.println(letter);

            // TODO Add Print support
        } catch (Exception ex) {
            System.out.println("Failed to read letter");
            System.out.println(ex.getLocalizedMessage());
            return;
        }
    }

    public void mailToInsured(String fileLocation) {
        // Gets insured info from policy
        this.getInfoFromPolicy(fileLocation);

        // TODO Launch Web Driver
        String email = "";

        //Create scanner
        Scanner scanner = new Scanner(System.in);

        // Check if email was recieved
        if(email.equals("") || this.letter) {
            // Ask if sending a letter is what to if no email is present
            System.out.println("Send letter to insured? [Y/n]");
            String sendLetter = scanner.nextLine();
            // See what user responeded
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
            this.sendEmail(email, fileLocation);
        }

        // TODO add remote support

        this.displayInfo();

        // TODO Print Support

        try {
            Files.move(Paths.get(fileLocation), Paths.get(WORK_FOLDER + this.policyNumber + ".pdf"));
        } catch (Exception ex) {
            System.out.println("Unable to move file");
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

    public void sendEmail(String to, String fileLocation) {
        // Get the password to the email account
        System.out.println("Please enter your password to " + Email.FROM + ":");
        String password = String.valueOf(System.console().readPassword());
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
            return "";
        } else {
            return str.substring(index + start.length(), index + start.length() + length);
        }
    }

    public String cutToFrom(String str, String from, String to) {
        int fromIndex = str.indexOf(from);
        int toIndex = str.indexOf(to, fromIndex);
        return str.substring(fromIndex + from.length(), toIndex);
    }

    public abstract int[] findCoverages(String policy);



}
