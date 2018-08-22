package com.brycethuilot.auto_mailer;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * OceanHarbor is a subclass of Policy. It can support PDF's created by Ocean Harbor Casualty Insurance in both HO3 and Dwelling format
 * @author Bryce Thuilot
 * @version %I%, %G%
 * @since 1.0
 */
public class OceanHarbor extends Policy {


    /**
     * Returns a OceanHarbor Object which can be used by ApplicationWindow
     * @param policyFile the PDF OceanHarbor Policy
     * @throws IOException if the PDF cannot be read
     */
    OceanHarbor(File policyFile) throws IOException {
        super(policyFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendToInsrured() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendToMortgagee() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOldPolicyNum() {
        String endingNumber = policyNumber.substring(policyNumber.length() - 2);
        endingNumber = (Integer.parseInt(endingNumber) - 1) + "";
        if(endingNumber.length() == 1) {
            endingNumber  = "0" + endingNumber;
        }else if(endingNumber.equals("-1")) {
            return policyNumber;
        }
        return policyNumber.substring(0, policyNumber.length() - 2) + endingNumber;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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


    /**
     *  Maps Dwelling coverages to their QQ Catalyst css option number
     * @param pdfText text extracts of PDF
     * @return Each coverage value mapped to the CSS option number
     */
    public HashMap<Integer, Double> findDwellingCoverages(String pdfText) {
        HashMap<Integer, Double> coverages = new HashMap<Integer, Double>();
        coverages.put(315, this.getDoubleValue(this.cutToFrom(pdfText, "Dwelling $", "Fire")));
        coverages.put(683, this.getDoubleValue(this.cutToFrom(pdfText, "Other Structure", "  ")));
        coverages.put(753, this.getDoubleValue(this.cutToFrom(pdfText, "Personal Property","  " )));
        coverages.put(576 , this.getDoubleValue(this.cutToFrom(pdfText, "Fair Rental Value", "\n")));
        coverages.put(576,  coverages.get(576) + this.getDoubleValue(this.cutToFrom(pdfText, "Additional Living Expense", "\n")));
        coverages.put(732, this.getDoubleValue(this.cutToFrom(pdfText, "Personal Liability", "\n")));
        coverages.put(602, this.getDoubleValue(this.cutToFrom(pdfText, "Medical Payments to Others", "\n")));
        //coverages.put(728, Double.parseDouble(this.cutToFrom(pdfText, "Personal Injury",  "\n")));
        return coverages;
    }

    /**
     * Maps Homeowners coverages to their QQ Catalyst css option number
     * @param pdfText text contents of the PDF
     * @return Each coverage value mapped to the CSS option number
     */
    public HashMap<Integer, Double> findHOCoverages(String pdfText) {
        HashMap<Integer, Double> coverages = new HashMap<>();
        coverages.put(315, this.getDoubleValue(this.cutSection(pdfText, "Building", 12)));
        coverages.put(683, this.getDoubleValue(this.cutSection(pdfText, "Other Structure", 10)));
        coverages.put(753, this.getDoubleValue(this.cutSection(pdfText, "Personal Property", 11)));
        coverages.put(576, this.getDoubleValue(this.cutSection(pdfText, "Loss of Use", 11)));
        coverages.put(732, this.getDoubleValue(this.cutSection(pdfText, "Personal Liability", 8)));
        coverages.put(602, this.getDoubleValue(this.cutSection(pdfText, "Medical Payments to Others", 7)));
        return coverages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setName(String pdfText) {
        String nameAndAddress = pdfText.substring(pdfText.indexOf("Insured\n") + 8);
        nameAndAddress = nameAndAddress.substring(0, nameAndAddress.indexOf("OCEAN HARBOR CASUALTY"));

        this.name = nameAndAddress.substring(0, nameAndAddress.indexOf("\n"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setAddress(String pdfText) {
        String nameAndAddress = pdfText.substring(pdfText.indexOf("Insured\n") + 8);
        nameAndAddress = nameAndAddress.substring(0, nameAndAddress.indexOf("OCEAN HARBOR CASUALTY"));

        this.address = nameAndAddress.substring(nameAndAddress.indexOf("\n"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setPolicyNumber(String pdfText) {
        this.policyNumber = pdfText.substring(pdfText.indexOf("POLICY NUMBER") - 13,pdfText.indexOf("POLICY NUMBER") - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCoverages(String pdfText) {
        this.coverages = this.dwelling ? this.findDwellingCoverages(pdfText) : this.findHOCoverages(pdfText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setDates(String pdfText) {
        int dateStartIndex = pdfText.indexOf("FROM TO");
        String[] dates = pdfText.substring(dateStartIndex + 9, pdfText.indexOf("\n", dateStartIndex + 9)).split(" ");
        this.effectiveDate = dates[0];
        this.expirationDate = dates[0];

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCompany() {
        this.company = "Ocean Harbor Casualty";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setRenewal(String pdfText) {
        this.renewal = !this.policyNumber.equals(this.getOldPolicyNum());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setPremium(String pdfText) {
        this.premium = this.getDoubleValue(cutSection(pdfText, "Total Policy Premium: ", 8));
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setDwelling(String pdfText) {
        this.dwelling = pdfText.contains("DWELLING PROPERTY POLICY");
    }
}
