import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.activation.*;

public class Email {
    static final String FROM = "bryce@millcreekagency.com";
    static final String FROMNAME = "Bryce Thuilot";
    static final String SMTP_USERNAME = FROM;
    static final String CONFIGSET = "ConfigSet";
    static final String HOST = "smtp.office365.com";
    static final int PORT = 587;
    private String stmpPassword;



    public Email(String stmpPassword) {
        this.stmpPassword = stmpPassword;
    }

    public boolean sendEmail(String to, String subject, String body , String attachment, String fileName) {
        Transport transport;
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);

        MimeMessage message = new MimeMessage(session);
        try {
            MimeBodyPart emailAttachment = new MimeBodyPart();
            MimeBodyPart emailBody = new MimeBodyPart();
            Multipart multipart = new MimeMultipart();
            message.setFrom(new InternetAddress(FROM, FROMNAME));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            emailBody.setContent(body, "text/html");
            multipart.addBodyPart(emailBody);

            emailAttachment.setFileName(fileName);
            javax.activation.DataSource source = new FileDataSource(attachment);
            emailAttachment.setDataHandler(new DataHandler(source));
            multipart.addBodyPart(emailAttachment);

            message.setContent(multipart);
            message.setHeader("X-SES-CONFIGURATION-SET", CONFIGSET);

            transport = session.getTransport();

            System.out.println("Sending... ");
            transport.connect(HOST, SMTP_USERNAME, stmpPassword);

            transport.sendMessage(message, message.getAllRecipients());
            System.out.println("Email Sent");
            transport.close();

        }
        catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
            return false;
        }

        return true;

    }

    private String formatRenewalEmail() {
        return String.join(
                System.getProperty("line.separator"),
                "<p>Enclosed, please find a copy of your policy renewal.</p>",
                "<br>",
                "<p>Because we value you as a client and policyholder, we would appreciate the opportunity to continue serving all your insurances needs. This is also a great time to discuss your coverage options, any changes that ma y need to be made, and any discounts you may be qualified to receive.</p>",
                "<br>",
                "<p>Kindly review enclosed documents for accuracy. Please feel free to call us at (631)751-4653 any time Monday through Friday between 9 a.m.and 5 p.m. We can take care of everything by telephone, or schedule an appointment to meet at your convenience.</p>",
                "<br>",
                "<p>I hope we've done everything possible to earn your future insurance business. I look forward to speaking with you soon and would like to say thank you again for choosing The Mill Creek Agency, Inc. </p>",
                "<br>",
                "<p>If you have any further questions please contact Info@millcreekagency.com. Please do not reply to this email.</p>"
        );
    }

    public String emailBody(String name, String policyNumber, String company, String effectiveDate, String expirationDate, boolean renewal) {
        return String.join(
                System.getProperty("line.separator"),
                "<span style='font-family: \"Times New Roman\", Times, serif'>",
                "<center><h2>**DO NOT REPLY TO THIS EMAIL**</h2></center>",
                "<br>",
                "<center><p>***ANY QUESTIONS/CONCERNS PLEASE CONTACT OUR OFFICE DIRECTLY***</p></center>",
                "<center><p>**Please ignore this message if you have already received this**</p></center>",
                "<br>",
                "<p>Dear " + name + ",</p>",
                "<br>",
                "<p>RE: Company: <strong> + " + company + "</strong></p>",
                "<br>",
                "<p>Policy Number: <strong>" + policyNumber + "</strong></p>",
                "<br>",
                renewal ? this.formatRenewalEmail() : this.formatNewBusinessEmail(effectiveDate, expirationDate),
                "<br><br>",
                "<span style='color: #64191E; line-height: 50%;'>",
                "<p>Thank you,</p>",
                "<h2>Bryce S. Thuilot</h2>",
                "<h3>Administrative Assistant</h3>",
                "<p>129A Main St.</p>",
                "<p>Stony Brook, NY 11790</p>",
                "<p>Phone: <a href=tel:6317514653>631-751-4653</a></p>",
                "<p>Fax: 631-751-4512</p>",
                "</span>",
                "</span>",
                "<span style='font-size: 10px;'><p>This email was sent from an automated mail distrubutor. Please let us know if there are any issues <a href='mailto:info@maillcreekagency.com'>here</a></p>"
        );
    }

    private String formatNewBusinessEmail(String effectiveDate, String expirationDate) {
        return String.join(
                System.getProperty("line.separator"),
                "<p>Thank you for choosing the Mill Creek Agency for your insurance necessities! We appreciate folks who share our sensibilities and believe there is value in keeping things personal. We believe that comfort is parallel to security, and we will always make sure that you feel protected by accurate coverage and friendly, familiar customer service. To us, you aren’t a number, you’re our valued client.</p>",
                "<br>",
                "<p>Enclosed, please find a copy of your new insurance policy with the effective date of " + effectiveDate + "and an expiration date of " + expirationDate +". If you ever need assistance understanding your policy, counsel on any life changes that may affect your coverage needs, or simply need to pay a bill, we will be more than happy to provide you with these services and anything else within our scope of knowledge if you give us a call at <a href=tel:\"+16317514653\">631-751-4653.</a></p>",
                "<br>",
                "<p>Kindly review enclosed documents for accuracy. Please feel free to call us at (631)751-4653 any time Monday through Friday between 9 a.m.and 5 p.m. We can take care of everything by telephone, or schedule an appointment to meet at your convenience.</p>",
                "<br>",
                "<p>I hope we've done everything possible to earn your future insurance business. We look forward to speaking with you soon, and would like say thank you again for choosing The Mill Creek Agency, Inc. </p>",
                "<br>",
                "<p>If you have any further questions please contact <a href=\"mailto:info@millcreekagency.com\">Info@millcreekagency.com</a>. <strong>Please do not reply to this email</strong>.</p>"
        );
    }
}
