package com.brycethuilot.auto_mailer;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class OceanHarbor extends Policy {

    public OceanHarbor(File policyFile, boolean updateInQQ, boolean printMortgage, boolean mailToInsured, ApplicationWindow applicationWindow) {
        super(policyFile, updateInQQ, printMortgage, mailToInsured, applicationWindow);
    }

    @Override
    public String getOldPolicyNum(String currentPolicyNum) {
        String endingNumber = currentPolicyNum.substring(currentPolicyNum.length() - 2);
        endingNumber = (Integer.parseInt(endingNumber) - 1) + "";
        if(endingNumber.length() == 1) {
            endingNumber  = "0" + endingNumber;
        }
        return currentPolicyNum.substring(0,currentPolicyNum.length() - 2) + endingNumber;
    }

    @Override
    public void printMortgagee() throws IOException {
        PDDocument doc = PDDocument.load(this.policyFile);

        Splitter splitter = new Splitter();
        splitter.setStartPage(3);
        splitter.setEndPage(4);
        List<PDDocument> pages = splitter.split(doc);

        PDFMergerUtility merger = new PDFMergerUtility();

        PDDocument mortgagePDF = new PDDocument();
        for(PDDocument page : pages) {
             merger.appendDocument(mortgagePDF, page);
        }

        Printer.printFile(mortgagePDF, "File for Mortgagee");

    }

    @Override
    public PDDocument getLetterPages(File policy) throws IOException{
        PDDocument doc = PDDocument.load(this.policyFile);

        Splitter splitter = new Splitter();
        splitter.setStartPage(2);
        splitter.setEndPage(4);
        List<PDDocument> pages = splitter.split(doc);

        PDFMergerUtility merger = new PDFMergerUtility();

        PDDocument letterPDF = new PDDocument();
        for(PDDocument page : pages) {
            merger.appendDocument(letterPDF, page);
        }

        return letterPDF;
    }

    private boolean isDwelling(String pdfText) {
        return pdfText.contains("DWELLING PROPERTY POLICY");
    }

    public HashMap<Integer, Double> findDwellingCoverages(String policy) {
        HashMap<Integer, Double> coverages = new HashMap<Integer, Double>();
        coverages.put(315, this.getDoubleValue(this.cutToFrom(policy, "Dwelling $", "Fire")));
        coverages.put(683, this.getDoubleValue(this.cutToFrom(policy, "Other Structure", "  ")));
        coverages.put(753, this.getDoubleValue(this.cutToFrom(policy, "Personal Property","  " )));
        coverages.put(576 , this.getDoubleValue(this.cutToFrom(policy, "Fair Rental Value", "\n")));
        coverages.put(576,  coverages.get(576) + this.getDoubleValue(this.cutToFrom(policy, "Additional Living Expense", "\n")));
        coverages.put(732, this.getDoubleValue(this.cutToFrom(policy, "Personal Liability", "\n")));
        coverages.put(602, this.getDoubleValue(this.cutToFrom(policy, "Medical Payments to Others", "\n")));
        //coverages.put(728, Double.parseDouble(this.cutToFrom(policy, "Personal Injury",  "\n")));
        return coverages;
    }

    public HashMap<Integer, Double> findHOCoverages(String policy) {
        HashMap<Integer, Double> coverages = new HashMap<>();
        coverages.put(315, this.getDoubleValue(this.cutSection(policy, "Building", 12)));
        coverages.put(683, this.getDoubleValue(this.cutSection(policy, "Other Structure", 10)));
        coverages.put(753, this.getDoubleValue(this.cutSection(policy, "Personal Property", 11)));
        coverages.put(576, this.getDoubleValue(this.cutSection(policy, "Loss of Use", 11)));
        coverages.put(732, this.getDoubleValue(this.cutSection(policy, "Personal Liability", 8)));
        coverages.put(602, this.getDoubleValue(this.cutSection(policy, "Medical Payments to Others", 7)));
        return coverages;
    }

    @Override
    protected void setName(String pdfText) {
        String nameAndAddress = pdfText.substring(pdfText.indexOf("Insured\n") + 8);
        nameAndAddress = nameAndAddress.substring(0, nameAndAddress.indexOf("OCEAN HARBOR CASUALTY"));

        this.name = nameAndAddress.substring(0, nameAndAddress.indexOf("\n"));
    }

    @Override
    protected void setAddress(String pdfText) {
        String nameAndAddress = pdfText.substring(pdfText.indexOf("Insured\n") + 8);
        nameAndAddress = nameAndAddress.substring(0, nameAndAddress.indexOf("OCEAN HARBOR CASUALTY"));

        this.address = nameAndAddress.substring(nameAndAddress.indexOf("\n"));
    }

    @Override
    protected void setPolicyNumber(String pdfText) {
        this.policyNumber = pdfText.substring(pdfText.indexOf("POLICY NUMBER") - 13,pdfText.indexOf("POLICY NUMBER") - 1);
    }

    @Override
    protected void setCoverages(String pdfText) {
        this.coverages = this.dwelling ? this.findDwellingCoverages(pdfText) : this.findHOCoverages(pdfText);
    }

    @Override
    protected void setDates(String pdfText) {
        int dateStartIndex = pdfText.indexOf("FROM TO");
        String[] dates = pdfText.substring(dateStartIndex + 9, pdfText.indexOf("\n", dateStartIndex + 9)).split(" ");
        this.effectiveDate = dates[0];
        this.expirationDate = dates[0];

    }

    @Override
    protected void setRenewal(String pdfText) {

    }

    @Override
    protected void setPremium(String pdfText) {
        this.premium = this.getDoubleValue(cutSection(pdfText, "Total Policy Premium: ", 8));
    }

    @Override
    protected void setDeductibles(String pdfText) {
        this.deductible = this.getIntValue(this.cutSection(pdfText, "otherwise\n", 6));

        String hurricaneDed = this.cutSection(pdfText, "Ded.: ", 2);
        try {
            this.hurricaneDeductible = this.getIntValue(hurricaneDed.replace("%", ""));
        } catch(NumberFormatException ex) {
            this.hurricaneDeductible = 0;
        }
    }

    @Override
    protected void setDwelling(String pdfText) {
        this.dwelling = this.isDwelling(pdfText);
    }
}
