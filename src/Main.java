public class Main {
    public static void main(String[] args) {
        Email e = new Email("");
        e.sendEmail("bthuilot@gmail.com", "Subject", e.formatRenewalEmail());
    }
}
