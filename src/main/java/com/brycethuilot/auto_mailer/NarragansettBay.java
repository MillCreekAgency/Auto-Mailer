package com.brycethuilot.auto_mailer;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;


/**
 * NarragansettBay is a subclass of Policy. It can support PDF's created by Narragansett Bay Insurance Company in HO3 format
 * @author Bryce Thuilot
 * @version %I%, %G%
 * @since 1.0
 */
public class NarragansettBay extends Policy {

    /**
     * Returns a NarragansettBay Object to be used by {@link ApplicationWindow}
     * @param policyFile the PDF Policy fil
     * @throws IOException if the PDF could not be read
     */
    NarragansettBay(File policyFile) throws IOException {
        super(policyFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PDDocument getLetterPages(File policy) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendToInsrured() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendToMortgagee() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setName(String pdfText) {
        this.name = this.cutToFrom(pdfText, "Name Insured\n", "\n");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setAddress(String pdfText) {
        this.address = this.cutToFrom(pdfText, "Insured Residence Premises Location", "Section I");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setPolicyNumber(String pdfText) {
        this.policyNumber = this.cutSection(pdfText, "Policy Number, Type Your Agent\n", 13);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCompany() {
        this.company = "Narraganset Bay";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setDates(String pdfText) {
        String[] dates = this.cutToFrom(pdfText, "Policy Period  ", "12:01AM EST").split(" to ");
        this.effectiveDate = dates[0];
        this.expirationDate = dates[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setRenewal(String pdfText) {
        this.renewal = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setPremium(String pdfText) {
        this.premium = this.getDoubleValue(this.cutToFrom(pdfText, "Total Premium:  $", "\n"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setDeductibles(String pdfText) {
        this.deductible = this.getIntValue(this.cutToFrom(pdfText, "All Perils Other than Hurricane $", "\n"));
        this.hurricaneDeductible = this.getIntValue(this.cutToFrom(pdfText ," (Hurricane Deductible Dollar Amount)  $", "\n"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setDwelling(String pdfText) {
        this.dwelling = false;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCoverages(String pdfText) {
        HashMap<Integer, Double> coverages = new HashMap<>();
        coverages.put(315, this.getDoubleValue(this.cutToFrom(pdfText, "Coverage A. Dwelling  ", "\n")));
        coverages.put(683, this.getDoubleValue(this.cutToFrom(pdfText, "Coverage B. Other Structures  ", "\n")));
        coverages.put(753, this.getDoubleValue(this.cutToFrom(pdfText, "Coverage C. Personal Property  ", "\n")));
        System.out.println(this.cutToFrom(pdfText, "Coverage D. Loss of Use  ", "\n"));
        coverages.put(576, this.getDoubleValue(this.cutToFrom(pdfText, "Coverage D. Loss Of Use  ", "\n")));
        coverages.put(732, this.getDoubleValue(this.cutToFrom(pdfText, "Coverage E. Personal Liability  ", "\n")));
        coverages.put(602, this.getDoubleValue(this.cutToFrom(pdfText, "Coverage F. Medical Payments  ", "\n")));
        this.coverages =  coverages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOldPolicyNum() {
        return policyNumber.substring(0, policyNumber.indexOf(','));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printMortgagee() throws IOException { }
}
