import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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

    public void sendEmail(String to, String subject, String body , String attachment) {
        Transport transport;
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);

        MimeMessage message = new MimeMessage(session);
        try {
            MimeBodyPart messageBody = new MimeBodyPart();
            Multipart multipart = new MimeMultipart();
            message.setFrom(new InternetAddress(FROM, FROMNAME));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setContent(body, "text/html");
            messageBody.setFileName(attachment);
            javax.activation.DataSource source = new FileDataSource(attachment);
            messageBody.setDataHandler(new DataHandler(source));
            multipart.addBodyPart(messageBody);
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
        }

    }

    public String formatRenewalEmail() {
        return String.join(
                System.getProperty("line.separator"),
                "<h1> Hello </h1>"
        );
    }
}
