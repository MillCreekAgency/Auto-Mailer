package com.brycethuilot.auto_mailer;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Letter {


    // Store file
    private PDDocument letter;
    private PDDocument policy;

    // Editors Setting
    private static int fontSize = 11;

    // Holds Data
    private String policyNumber;
    private String name;
    private String address;
    private String dateString;

    // Fonts
    static PDFont BOLD_FONT = PDType1Font.TIMES_BOLD;
    static PDFont NORMAL_FONT = PDType1Font.TIMES_ROMAN;



    public Letter(String policyNumber, String name, String address) {
        this.policyNumber = policyNumber;
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("E MMM d, yyyy");
        this.dateString =  df.format(date);
        this.name = name;
        this.address = address;
    }

    public void addPolicy(PDDocument policy) throws IOException {
        this.policy = new PDDocument();

        Splitter splitter = new Splitter();
        splitter.setStartPage(2);
        splitter.setEndPage(4);
        List<PDDocument> pages = splitter.split(policy);

        PDFMergerUtility merger = new PDFMergerUtility();

        for(PDDocument page : pages) {
            merger.appendDocument(this.policy, page);
        }
    }


    public void printLetter() {
        List<PDDocument> toPrint = new ArrayList<PDDocument>(2);
        toPrint.add(this.letter);
        toPrint.add(this.policy);

        List<String> names = new ArrayList<String>(2);
        names.add("Letter to insured");
        names.add("Policy for Insured");

        Printer.printFiles(toPrint, names);
    }

    public void createLetter() throws IOException{
        // Create a document and add a page to it
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage( page );

        // Start a new content stream which will "hold" the to be created content
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Start Text
        contentStream.beginText();
        contentStream.setFont(NORMAL_FONT, fontSize);
        contentStream.setLeading(14.5f);

        this.addHeader(contentStream);

        this.addBody(contentStream);

        contentStream.endText();
        contentStream.close();

        this.letter = document;
    }


    public void newLine(PDPageContentStream stream, int amount) throws IOException{
        for(int i = 0; i < amount; i++) {
            stream.newLine();
        }
    }


    private void addName(PDPageContentStream contentStream) throws IOException{
        //Name
        contentStream.newLineAtOffset(0, -50);
        contentStream.showText(this.name);
    }

    private void addAddress(PDPageContentStream contentStream) throws IOException {
        // Address
        contentStream.newLine();
        String[] addressLines = this.address.split("\n");
        for(String addressLine : addressLines) {
            contentStream.showText(addressLine);
            contentStream.newLine();
        }
    }

    private void addDate(PDPageContentStream contentStream) throws IOException{
        // Date
        contentStream.newLineAtOffset(75, 580);
        contentStream.showText(this.dateString);
    }

    private void addCompanyAndPolicy(PDPageContentStream contentStream) throws IOException {
        //Company and com.brycethuilot.auto_mailer.Policy Number
        this.newLine(contentStream, 2);
        contentStream.showText("RE:   Company: " + "Ocean Harbor Casualty");
        this.newLine(contentStream, 1);
        contentStream.showText("         Policy Number: " + this.policyNumber);
    }

    private void addHeader(PDPageContentStream contentStream) throws IOException{
        this.addDate(contentStream);
        this.addName(contentStream);
        this.addAddress(contentStream);
        this.addCompanyAndPolicy(contentStream);
    }

    private void addBody(PDPageContentStream contentStream) throws IOException {
        // Name
        contentStream.newLineAtOffset(0,-50);
        contentStream.showText("Dear " + this.name + ",");

        // Body
        contentStream.newLineAtOffset(0, -25);
        contentStream.showText("Enclosed, please find a copy of your policy renewal.");
        this.newLine(contentStream, 2);
        contentStream.showText("Because we value you as a client and policyholder, we would appreciate the opportunity to");
        this.newLine(contentStream, 1);
        contentStream.showText("serving all your insurances needs. This is also a great time to discuss your coverage options,");
        this.newLine(contentStream, 1);
        contentStream.showText("any changes that may need to be made, and any you may be qualified to receive.");
        this.newLine(contentStream, 2);
        contentStream.showText("Please feel free to call us at (631)751-4653 any time Monday through Friday 9 am and 5 pm");
        this.newLine(contentStream, 1);
        contentStream.showText("9 am and 5 pm");
        this.newLine(contentStream, 2);

        contentStream.setFont(BOLD_FONT, fontSize);
        contentStream.showText("If you have not done so already, please kindly e-mail us at info@MillCreekAgency.com");
        this.newLine(contentStream, 1);
        contentStream.showText("your current E-mail address so that we may have it on file for future communications.");
        this.newLine(contentStream, 2);

        contentStream.setFont(NORMAL_FONT, fontSize);
        contentStream.showText("I hope we've done everything possible to earn your future insurance business.");
        this.newLine(contentStream, 1);
        contentStream.showText("We forward to speaking with you soon, and would like say thank you again for choosing");
        this.newLine(contentStream, 1);
        contentStream.showText("Mill Creek Agency, Inc.");
        contentStream.newLineAtOffset(0, -50);
        contentStream.setFont(NORMAL_FONT, 12);
        contentStream.showText("Sincerely,");
        this.newLine(contentStream, 2);
        contentStream.showText("The Mill Creek Agency Inc.");
    }
}
