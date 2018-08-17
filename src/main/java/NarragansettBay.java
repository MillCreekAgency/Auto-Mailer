import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class NarragansettBay extends Policy {

    public NarragansettBay(File policyFile, boolean updateInQQ, ApplicationWindow applicationWindow) {
        super(policyFile, updateInQQ, false, false, applicationWindow);
    }

    @Override
    public PDDocument getLetterPages(File policy) throws IOException {
        return null;
    }

    @Override
    protected void setName(String pdfText) {
        this.name = this.cutToFrom(pdfText, "Name Insured\n", "\n");
    }

    @Override
    protected void setAddress(String pdfText) {
        this.address = this.cutToFrom(pdfText, "Insured Residence Premises Location", "Section I");
    }

    @Override
    protected void setPolicyNumber(String pdfText) {
        this.policyNumber = this.cutSection(pdfText, "Policy Number, Type Your Agent\n", 13);
    }

    @Override
    protected void setDates(String pdfText) {
        String[] dates = this.cutToFrom(pdfText, "Policy Period  ", "12:01AM EST").split(" to ");
        this.effectiveDate = dates[0];
        this.expirationDate = dates[0];
    }

    @Override
    protected void setRenewal(String pdfText) {
        this.renewal = true;
    }

    @Override
    protected void setPremium(String pdfText) {
        this.premium = this.getDoubleValue(this.cutToFrom(pdfText, "Total Premium:  $", "\n"));
    }

    @Override
    protected void setDeductibles(String pdfText) {
        this.deductible = this.getIntValue(this.cutToFrom(pdfText, "All Perils Other than Hurricane $", "\n"));
        this.hurricaneDeductible = this.getIntValue(this.cutToFrom(pdfText ," (Hurricane Deductible Dollar Amount)  $", "\n"));
    }

    @Override
    protected void setDwelling(String pdfText) {
        this.dwelling = false;

    }

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

    @Override
    public String getOldPolicyNum(String currentPolicyNum) {
        return currentPolicyNum.substring(0, currentPolicyNum.indexOf(','));
    }

    @Override
    public void printMortgagee() throws IOException {

    }
}
